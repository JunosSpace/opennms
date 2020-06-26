/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer;

import org.opennms.web.svclayer.model.NodeListCommand;
import org.opennms.web.svclayer.model.NodeListModel;
import org.springframework.transaction.annotation.Transactional;


/**
 * <p>NodeListService interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@Transactional(readOnly = true)
public interface NodeListService {
    /**
     * <p>createNodeList</p>
     *
     * @param command a {@link org.opennms.web.command.NodeListCommand} object.
     * @return a {@link org.opennms.web.svclayer.model.NodeListModel} object.
     */
    public NodeListModel createNodeList(NodeListCommand command, boolean sanitizeLabels);

    /**
     * <p>createNodeList</p>
     *
     * @param command a {@link org.opennms.web.command.NodeListCommand} object.
     * @return a {@link org.opennms.web.svclayer.model.NodeListModel} object.
     */
    public NodeListModel createNodeList(NodeListCommand command);
}
