/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.linkd.internal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.NeRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.features.topology.api.topo.*;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.model.*;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;

import java.util.*;

public class LldpLinkStatusProvider extends AbstractLinkStatusProvider {

    private LldpLinkDao m_lldpLinkDao;

    @Override
    public String getNameSpace() {
        return AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD + "::LLDP";
    }

    @Override
    protected Set<Integer> getLinkIds(Map<String, EdgeRef> mappedRefs) {
        Set<Integer> lldpLinkIds = new HashSet<Integer>();
        for (String edgeRefId : mappedRefs.keySet()) {
            if (edgeRefId.contains("|")) {
                int charIndex = edgeRefId.indexOf('|');
                int sourceId = Integer.parseInt(edgeRefId.substring(0, charIndex));
                int targetId = Integer.parseInt(edgeRefId.substring(charIndex + 1, edgeRefId.length()));
                lldpLinkIds.add(sourceId);
                lldpLinkIds.add(targetId);
            }
        }
        return lldpLinkIds;
    }

    @Override
    protected List<EdgeAlarmStatusSummary> getEdgeAlarmSummaries(List<Integer> linkIds) {
        List<LldpLink> links = m_lldpLinkDao.findLinksForIds(linkIds);

        Multimap<String, EdgeAlarmStatusSummary> summaryMap = HashMultimap.create();
        for (LldpLink sourceLink : links) {

            OnmsNode sourceNode = sourceLink.getNode();
            LldpElement sourceElement = sourceNode.getLldpElement();

            for (LldpLink targetLink : links) {
                OnmsNode targetNode = targetLink.getNode();
                LldpElement targetLldpElement = targetNode.getLldpElement();

                // Compare the remote data to the targetNode element data
                boolean bool1 = sourceLink.getLldpRemPortId().equals(targetLink.getLldpPortId())
                        && targetLink.getLldpRemPortId().equals(sourceLink.getLldpPortId());
                boolean bool2 = sourceLink.getLldpRemPortDescr().equals(targetLink.getLldpPortDescr())
                        && targetLink.getLldpRemPortDescr().equals(sourceLink.getLldpPortDescr());
                boolean bool3 = sourceLink.getLldpRemChassisId().equals(targetLldpElement.getLldpChassisId())
                        && targetLink.getLldpRemChassisId().equals(sourceElement.getLldpChassisId());
                boolean bool4 = sourceLink.getLldpRemSysname().equals(targetLldpElement.getLldpSysname())
                        && targetLink.getLldpRemSysname().equals(sourceElement.getLldpSysname());
                boolean bool5 = sourceLink.getLldpRemPortIdSubType() == targetLink.getLldpPortIdSubType()
                        && targetLink.getLldpRemPortIdSubType() == sourceLink.getLldpPortIdSubType();

                if (bool1 && bool2 && bool3 && bool4 && bool5) {
                    
                    if (sourceNode.getSysObjectId() != null && sourceNode.getSysObjectId().contains("1.3.6.1.4.1.119")) {
                        
                        Integer ifIndex = getIfIndexFromPortNum(sourceLink.getLldpLocalPortNum());
                        if (ifIndex == null) {
                            ifIndex = sourceLink.getLldpLocalPortNum();
                        }
                        
                        summaryMap.put(sourceNode.getNodeId() + ":" + ifIndex,
                                new EdgeAlarmStatusSummary(sourceLink.getId(), targetLink.getId(), null));
                    } else {
                        summaryMap.put(sourceNode.getNodeId() + ":" + sourceLink.getLldpPortIfindex(),
                                new EdgeAlarmStatusSummary(sourceLink.getId(), targetLink.getId(), null));

                    }
                    // if the LinkDown alarm is from physical interface, there
                    // won't have related interface index for the logical
                    // interface
                    // such as, the link is from logical interface ge-0/0/0.0
                    // but the alarm is from physical interface ge-0/0/0
                    if (sourceLink.getLldpPortDescr() != null && sourceLink.getLldpPortDescr().contains(".")) {
                        summaryMap.put(sourceNode.getNodeId() + ":" + sourceLink.getLldpPortDescr().split("\\.")[0],
                                new EdgeAlarmStatusSummary(sourceLink.getId(), targetLink.getId(), null));
                    }
                }
            }
        }

        List<OnmsAlarm> alarms = getLinkDownAlarms();

        for (OnmsAlarm alarm : alarms) {
            if (alarm.getIfIndex() != null) {
                String key = alarm.getNodeId() + ":" + alarm.getIfIndex();
                if (summaryMap.containsKey(key)) {
                    Collection<EdgeAlarmStatusSummary> summaries = summaryMap.get(key);
                    for (EdgeAlarmStatusSummary summary : summaries) {
                        if (getIpasolinkLinkAlarms().contains(alarm.getUei())) {
                            summary.setEventUEI(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);
                        } else {
                            summary.setEventUEI(alarm.getUei());
                        }
                    }
                } else {
                    String parms = alarm.getEventParms();
                    String ifName = "";
                    char separator = ';';
                    String[] parmArray = StringUtils.split(parms, separator);

                    for (String string : parmArray) {

                        char nameValueDelim = '=';
                        String[] nameValueArray = StringUtils.split(string, nameValueDelim);
                        String parmName = nameValueArray[0];
                        String parmValue = StringUtils.split(nameValueArray[1], '(')[0];

                        if (parmName.indexOf(".1.3.6.1.2.1.31.1.1.1.1.") > -1) {
                            ifName = parmValue;
                            break;
                        }
                    }
                    if (!ifName.isEmpty()) {
                        String keyWithIfName = alarm.getNodeId() + ":" + ifName;
                        if (summaryMap.containsKey(keyWithIfName)) {
                            Collection<EdgeAlarmStatusSummary> summaries = summaryMap.get(keyWithIfName);
                            for (EdgeAlarmStatusSummary summary : summaries) {
                                summary.setEventUEI(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);
                            }
                        }
                    }
                }
            } else {
                for (String key : summaryMap.keySet()) {
                    if (key.indexOf(alarm.getNodeId() + ":") > -1) {
                        Collection<EdgeAlarmStatusSummary> summaries = summaryMap.get(key);
                        for (EdgeAlarmStatusSummary summary : summaries) {
                            summary.setEventUEI(alarm.getUei());
                        }
                    }
                }
            }

        }
        return new ArrayList<EdgeAlarmStatusSummary>(summaryMap.values());
    }
    
    private Integer getIfIndexFromPortNum(Integer portNum) {
        if (portNum == null) {
            return null;
        }

        Map<Integer, Integer> portNumMap = new HashMap<Integer, Integer>();
        portNumMap.put(42, 25231360);
        portNumMap.put(43, 33619968);
        portNumMap.put(44, 42008576);
        portNumMap.put(45, 50397184);
        portNumMap.put(46, 58785792);
        portNumMap.put(47, 67174400);
        portNumMap.put(48, 75563008);
        portNumMap.put(49, 83951616);
        portNumMap.put(50, 92340224);
        portNumMap.put(51, 100728832);
        portNumMap.put(52, 109117440);
        portNumMap.put(53, 117506048);
        portNumMap.put(54, 125894656);

        return portNumMap.get(portNum);

    }

        

    private List<String> getIpasolinkLinkAlarms() {
        
        List<String> ueis = new ArrayList<String>();
        ueis.add(EventConstants.FRAME_ID_UEI_LINK);
        ueis.add(EventConstants.TX_POWER_ALARM_UEI_LINK);
        ueis.add(EventConstants.RX_LEVEL_ALARM_UEI_LINK);
        ueis.add(EventConstants.AS_ETH_PORT_SFP_LOS_UEI_LINK);
        ueis.add(EventConstants.LP_UNEQUIPED_ALARM_UEI_LINK);
        ueis.add(EventConstants.AS_ETH_PORT_SFP_TX_ERROR_UEI_LINK);
        ueis.add(EventConstants.E1_LOS_ALARM_UEI_LINK);
        ueis.add(EventConstants.TDM_RANGE_MISMATCH_UEI_LINK);
        ueis.add(EventConstants.STM1_LOS_ALARM_UEI_LINK);
        ueis.add(EventConstants.AS_ETH_PORT_LINK_STATUS_UEI_LINK);
        ueis.add(EventConstants.LOF_UEI_LINK);
        ueis.add(EventConstants.L2_SYNC_LOSS_ALARM_UEI_LINK);

        return ueis;

    }

    protected List<OnmsAlarm> getLinkDownAlarms() {
        org.opennms.core.criteria.Criteria criteria = new org.opennms.core.criteria.Criteria(OnmsAlarm.class);

        List<String> ueis = new ArrayList<String>();

        // Added For NEC iPASO device support
        ueis.add(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);
        ueis.addAll(getIpasolinkLinkAlarms());

        criteria.addRestriction(getUeirestictionByUeis(ueis));

        criteria.addRestriction(new NeRestriction("severity", OnmsSeverity.CLEARED));

        return getAlarmDao().findMatching(criteria);
    }

    private Restriction getUeirestictionByUeis(List<String> ueis) {
        Restriction restrictions = null;
        for (String uei : ueis) {
            if (restrictions == null) {
                restrictions = new EqRestriction("uei", uei);
            } else {
                restrictions = Restrictions.or(restrictions, new EqRestriction("uei", uei));
            }
        }
        return restrictions;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return "lldpLinkStatusProvider";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EdgeStatusProvider) {
            EdgeStatusProvider provider = (EdgeStatusProvider) obj;
            return provider.getClass().getSimpleName().equals(getClass().getSimpleName());
        } else {
            return false;
        }
    }

    public void setLldpLinkDao(LldpLinkDao lldpLinkDao) {
        m_lldpLinkDao = lldpLinkDao;
    }
}
