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

package org.opennms.features.topology.netutils.internal.operations;

import java.net.URL;
import java.util.List;

import org.opennms.features.topology.api.AbstractOperation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.OperationContext.DisplayLocation;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.netutils.internal.Node;

import com.vaadin.ui.JavaScript;
import com.vaadin.ui.UI;

public class PASOLINKOperation extends AbstractOperation {
    private String m_sshURL;

    @Override
    public Undoer execute(final List<VertexRef> targets, final OperationContext operationContext) {
        try {
            String label = "";
            int nodeID = -1;
            String address = "";

            if (targets != null) {
                for (final VertexRef target : targets) {
                    final String labelValue = getLabelValue(operationContext, target);
                    final Integer nodeValue = getNodeIdValue(operationContext, target);
                    final String addrValue = getIpAddrValue(operationContext, target);

                    if (nodeValue != null && nodeValue > 0) {
                        label = labelValue == null? "" : labelValue;
                        nodeID = nodeValue.intValue();
			address = addrValue;
                        break;
                    }
                }
            }

            final Node node = new Node(nodeID, address, label);

            final String url = getSshURL() + node.getNodeID() + "&ipaddress=" + address;
            
            final String fullUrl = getFullUrl(url);
            final String openstr = "var win=window.top.open('" + fullUrl + "','','width=800, height=600, resizable=yes, scrollbars=yes, toolbar=no, location=no, directories=no, status=no, menubar=no' ); parent.popupwindows.push(win);  ";
            UI mainWindow = operationContext.getMainWindow();
	    mainWindow.getPage().getJavaScript().execute(openstr);

            return null;
        } catch (final Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new RuntimeException("Failed to create node window.", e);
            }
        }
    }

    @Override
    public boolean display(final List<VertexRef> targets, final OperationContext operationContext) {
        if (targets == null || targets.size() < 2) {
            for (final VertexRef target : targets) {
                final Integer nodeId = getNodeIdValue(operationContext, target);
                final String nodeLabel = getLabelValue(operationContext, target);
                if (nodeId != null && nodeId > 0) {
                    if (nodeLabel.startsWith("space-") && nodeLabel.length() > 17) {
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }    
	
    @Override
    public boolean enabled(final List<VertexRef> targets, final OperationContext operationContext) {
        if (targets == null || targets.size() < 2) {
            for (final VertexRef target : targets) {
                final Integer nodeValue = getNodeIdValue(operationContext, target);
                //final String nodeLabel = getLabelValue(operationContext, target);
                final String nodeIconKey = getIconKeyValue(operationContext, target);
                if (nodeValue != null && nodeValue > 0) {
                    if (nodeIconKey.contains(".1.3.6.1.4.1.119.2.3.69")) {
                        return true;
                    }
                }
                
/*                if (nodeValue != null && nodeValue > 0) {
                    if (nodeLabel.startsWith("space-") && nodeLabel.length() > 17) { 
                        return false;
                    }
                    return true;
                }
*/            }
        }
        return false;
    }

    @Override
    public String getId() {
        return "contextNodeInfo";
    }

    public String getSshURL() {
        return m_sshURL;
    }

    public void setSshURL(final String sshURL) {
        m_sshURL = sshURL;
    }

}
