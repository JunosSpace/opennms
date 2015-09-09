/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.browsers;
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

import org.hibernate.criterion.Restrictions;
import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.SqlRestriction;
import org.opennms.features.topology.api.VerticesUpdateManager;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Ref;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.osgi.EventConsumer;

import java.util.*;

public class AlarmDaoContainer extends OnmsDaoContainer<OnmsAlarm,Integer> {

    private static final long serialVersionUID = -4026870931086916312L;
    
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;

    public AlarmDaoContainer(AlarmDao dao) {
        super(OnmsAlarm.class, dao);
        addBeanToHibernatePropertyMapping("nodeLabel", "node.label");
    }

    @Override
    protected void updateContainerPropertyIds(Map<Object, Class<?>> properties) {
        // Causes problems because it is a map of values
        properties.remove("details");

        // Causes referential integrity problems
        // @see http://issues.opennms.org/browse/NMS-5750
        properties.remove("distPoller");
    }

    @Override
    protected Integer getId(OnmsAlarm bean){
        return bean == null ? null : bean.getId();
    }

    @Override
    public Collection<?> getSortableContainerPropertyIds() {
        Collection<Object> propertyIds = new HashSet<Object>();
        propertyIds.addAll(getContainerPropertyIds());

        // This column is a checkbox so we can't sort on it either
        propertyIds.remove("selection");

        return Collections.unmodifiableCollection(propertyIds);
    }

    @Override
    protected void addAdditionalCriteriaOptions(Criteria criteria, Page page, boolean doOrder) {
        criteria.setAliases(Arrays.asList(new Alias[] {
                new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
    }

    @Override
    @EventConsumer
    public void verticesUpdated(VerticesUpdateManager.VerticesUpdateEvent event) {
        final NodeIdFocusToRestrictionsConverter converter = new NodeIdFocusToRestrictionsConverter() {

            @Override
            protected Restriction createRestriction(Integer nodeId ) {
                return new EqRestriction("node.id", nodeId);
            }
        };
        
        List<Restriction> newRestrictions;
        List<Ref> refs = new ArrayList<Ref>();
        refs.addAll(event.getRefs());
        List<VertexRef> vertexRefs = new ArrayList<VertexRef>();
        EdgeRef edgeRef = null;
        if (refs != null && refs.size() > 0) {
            for (Ref ref : refs) {
                if (ref instanceof EdgeRef) {
                    edgeRef = (EdgeRef) ref;
                    break;
                } else if (ref instanceof VertexRef) {
                    VertexRef vertexRef = (VertexRef) ref;
                    vertexRefs.add(vertexRef);
                }
            }
            if (edgeRef != null) {
                
                DataLinkInterface dao = m_dataLinkInterfaceDao.findById(Integer.parseInt(edgeRef.getId()));
                
                
/*                EqRestriction node1Id = new EqRestriction("node.id", "(select nodeid from datalinkinterface where id = "+Integer.parseInt(edgeRef.getId())+")");
                EqRestriction node1Ifindex = new EqRestriction("ifIndex", "(select ifindex from datalinkinterface where id = "+Integer.parseInt(edgeRef.getId())+"))");
                
                EqRestriction node2Id = new EqRestriction("node.id", "(select nodeparentid from datalinkinterface where id = "+Integer.parseInt(edgeRef.getId())+")");
                EqRestriction node2Ifindex = new EqRestriction("ifIndex", "(select parentifindex from datalinkinterface where id = "+Integer.parseInt(edgeRef.getId())+"))");

*/                
                EqRestriction node1Id = new EqRestriction("node.id",dao.getNodeId());
                EqRestriction node1Ifindex = new EqRestriction("ifIndex", dao.getIfIndex());
                
                EqRestriction node2Id = new EqRestriction("node.id", dao.getNodeParentId());
                EqRestriction node2Ifindex = new EqRestriction("ifIndex", dao.getParentIfIndex());

                
                Restriction node1Restriction = org.opennms.core.criteria.restrictions.Restrictions.and(node1Id, node1Ifindex);
                Restriction node2Restriction = org.opennms.core.criteria.restrictions.Restrictions.and(node2Id, node2Ifindex);
                
                Restriction restriction = org.opennms.core.criteria.restrictions.Restrictions.or(node1Restriction, node2Restriction);

/*                String sql = "(node1_.nodeId = (select nodeid from datalinkinterface where id = "+edgeRef.getId()+") and "
                        + "this_.ifindex = (select ifindex from datalinkinterface where id = "+edgeRef.getId()+")) or "
                                + "(node1_.nodeId = (select nodeparentid from datalinkinterface where id = "+edgeRef.getId()+") and "
                        + "this_.ifindex = (select parentifindex from datalinkinterface where id = "+edgeRef.getId()+")) ";
                SqlRestriction sqlRestriction = new SqlRestriction(sql);*/
                
                newRestrictions = new ArrayList<Restriction>();
                newRestrictions.add(restriction);
            
            } else {
                newRestrictions = converter.getRestrictions(vertexRefs);
            }
        } else {
            
            newRestrictions = converter.getRestrictions(event.getVertexRefs());
        }
        
        

        if (!getRestrictions().equals(newRestrictions)) { // selection really changed
            setRestrictions(newRestrictions);
            getCache().reload(getPage());
            fireItemSetChangedEvent();
        }
    }

    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(DataLinkInterfaceDao dataLinkInterfaceDao) {
        m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }
}