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
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.InRestriction;
import org.opennms.core.criteria.restrictions.NeRestriction;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.api.IsIsLinkDao;
import org.opennms.netmgt.model.IsIsElement;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;

import java.util.*;

public class IsIsLinkStatusProvider extends AbstractLinkStatusProvider {

    public static final String ISIS_ADJACENCY_DOWN_EVENT_UEI = "uei.opennms.org/traps/ISIS-MIB/isisAdjacencyDown";
    private IsIsLinkDao m_isIsLinkDao;


    @Override
    public String getNameSpace() {
        return EnhancedLinkdTopologyProvider.ISIS_EDGE_NAMESPACE;
    }

    @Override
    protected List<EdgeAlarmStatusSummary> getEdgeAlarmSummaries(List<Integer> linkIds) {
        Criteria criteria = new Criteria(IsIsLink.class);
        criteria.addRestriction(new InRestriction("id", linkIds));

        List<IsIsLink> links = getIsisLinkDao().findMatching(criteria);
        Multimap<String, EdgeAlarmStatusSummary> summaryMap = HashMultimap.create();
        for (IsIsLink sourceLink : links) {
            OnmsNode sourceNode = sourceLink.getNode();
            IsIsElement sourceElement = sourceNode.getIsisElement();
            for (IsIsLink targetLink : links) {
                boolean isisAdjIndexCheck = sourceLink.getIsisISAdjIndex() == targetLink.getIsisISAdjIndex();
                boolean isisSysIdCheck = targetLink.getIsisISAdjNeighSysID().equals(sourceElement.getIsisSysID());
                if (isisAdjIndexCheck && isisSysIdCheck) {
                    summaryMap.put(sourceNode.getNodeId() + ":" + sourceLink.getIsisCircIfIndex(),
                            new EdgeAlarmStatusSummary(sourceLink.getId(), targetLink.getId(), null));
                }
            }
        }

        List<OnmsAlarm> alarms = getLinkDownAlarms();
        for (OnmsAlarm alarm : alarms) {
            if (alarm.getUei().equals(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI)) {
                if (alarm.getIfIndex() != null) {
                    String key = alarm.getNodeId() + ":" + alarm.getIfIndex();
                    if (summaryMap.containsKey(key)) {

                        Collection<EdgeAlarmStatusSummary> summaries = summaryMap.get(key);
                        for (EdgeAlarmStatusSummary summary : summaries) {
                            summary.setEventUEI(alarm.getUei());
                        }
                    }
                } else {
                    for (String key : summaryMap.keySet()){
                        if (key.indexOf(alarm.getNodeId() + ":") > -1) {

                            Collection<EdgeAlarmStatusSummary> summaries = summaryMap.get(key);
                            for (EdgeAlarmStatusSummary summary : summaries) {
                                summary.setEventUEI(alarm.getUei());
                            }
                        }
                    }
                }
            } else {
                String parms = alarm.getEventParms();
                String isisNotificationCircIfIndex = "";

                char separator = ';';
                String[] parmArray = StringUtils.split(parms, separator);
                for (String string : parmArray) {
                    
                    char nameValueDelim = '=';
                    String[] nameValueArray = StringUtils.split(string, nameValueDelim);
                    String parmName = nameValueArray[0];
                    String parmValue = StringUtils.split(nameValueArray[1], '(')[0];
                    
                    if (parmName.indexOf(".1.3.6.1.2.1.138.1.10.1.2") > -1) {
                        isisNotificationCircIfIndex = parmValue;
                        break;
                    }
                }
                if (!isisNotificationCircIfIndex.isEmpty()) {
                    String key = alarm.getNodeId() + ":" + isisNotificationCircIfIndex;
                    if (summaryMap.containsKey(key)) {

                        Collection<EdgeAlarmStatusSummary> summaries = summaryMap.get(key);
                        for (EdgeAlarmStatusSummary summary : summaries) {
                            summary.setEventUEI(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);
                        }
                    }
                }
            }

        }
        return new ArrayList<EdgeAlarmStatusSummary>(summaryMap.values());
    }

    @Override
    protected Set<Integer> getLinkIds(Map<String, EdgeRef> mappedRefs) {
        Set<Integer> linkIds = new HashSet<Integer>();
        for (String edgeRefId : mappedRefs.keySet()) {
            if (edgeRefId.contains("|")) {
                int charIndex = edgeRefId.indexOf('|');
                int sourceId = Integer.parseInt(edgeRefId.substring(0, charIndex));
                int targetId = Integer.parseInt(edgeRefId.substring(charIndex + 1, edgeRefId.length()));
                linkIds.add(sourceId);
                linkIds.add(targetId);
            }
        }
        return linkIds;
    }

    @Override
    protected List<OnmsAlarm> getLinkDownAlarms() {
        org.opennms.core.criteria.Criteria criteria = new org.opennms.core.criteria.Criteria(OnmsAlarm.class);
        String[] UEIs = new String[] {EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI, ISIS_ADJACENCY_DOWN_EVENT_UEI};
        criteria.addRestriction(new InRestriction("uei", UEIs));
        criteria.addRestriction(new NeRestriction("severity", OnmsSeverity.CLEARED));
        return getAlarmDao().findMatching(criteria);
    }

    public IsIsLinkDao getIsisLinkDao() {
        return m_isIsLinkDao;
    }

    public void setIsisLinkDao(IsIsLinkDao isIsLinkDao) {
        m_isIsLinkDao = isIsLinkDao;
    }
}
