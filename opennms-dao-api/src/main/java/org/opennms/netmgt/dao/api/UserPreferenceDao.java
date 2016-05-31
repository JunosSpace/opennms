/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import org.opennms.netmgt.model.UserPreferenceState;

/**
 * <p>UserPreferenceDao interface.</p>
 */
public interface UserPreferenceDao extends OnmsDao<UserPreferenceState,Integer> {

    /**
     * <p>setUserPreference</p>
     *
     * @param userName
     * @param stateName
     * @param stateValue
     * This Method is used to store the userpreference value in DB.
     */
    void setUserPreference(String userName ,String stateName ,String stateValue);
    
    /**
     * <p>getUserPreference</p>
     *
     * @param userName.
     * @param stateName
     * @return an int type.
     * This Method is used to retrive the userpreference value in DB.
     */
    int getUserPreference(String userName , String stateName );

    
}

