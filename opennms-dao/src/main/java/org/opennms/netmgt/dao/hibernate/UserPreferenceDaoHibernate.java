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

package org.opennms.netmgt.dao.hibernate;

import java.util.List;

//need to import all nessary 
import org.opennms.netmgt.dao.api.UserPreferenceDao;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.model.UserPreferenceState;
import org.hibernate.cfg.Configuration;
import org.opennms.netmgt.model.OnmsCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>UserPreferenceDaoHibernate class.</p>
 *
 * @author bharani
 * @version $Id: $
 */
public class UserPreferenceDaoHibernate extends AbstractDaoHibernate<UserPreferenceState,Integer> implements UserPreferenceDao {
    private static final Logger logger = LoggerFactory.getLogger(UserPreferenceDaoHibernate.class);
	

	/**
	 * <p>Constructor for UserPreferenceDaoHibernate.</p>
	 */
	public UserPreferenceDaoHibernate() {
		super(UserPreferenceState.class);
	}

       public int getUserPreference(String userName , String stateName ) {
        try{
            final OnmsCriteria criteria = new OnmsCriteria(UserPreferenceState.class);
            criteria.add(Restrictions.and(Restrictions.eq("userName", userName ), Restrictions.eq("stateName", stateName)));
            List<UserPreferenceState> interfaces =  findMatching(criteria);	
            if (!interfaces.isEmpty()) {
            	return Integer.valueOf(interfaces.get(0).getStateValue());
            }
            return 0;
        } catch (final Exception e) {
            logger.error("Error message is : "+e);
        }
        return 0;
    } 

@Transactional
    public void setUserPreference(String userName ,String stateName ,String stateValue ) {  
      try{
    	  
    	  UserPreferenceState userPreferences =  findUnique("from UserPreferenceState as up where up.stateName = ? and up.userName = ?", stateName, userName);


    	  if (userPreferences == null) {
    		  userPreferences = new UserPreferenceState();
    		  userPreferences.setStateName(stateName);
    		  userPreferences.setStateValue(stateValue);
    		  userPreferences.setUserName(userName);
    	  }
    	  userPreferences.setStateValue(stateValue);

          saveOrUpdate(userPreferences);
      
      }
     catch(final Exception e){
 		logger.error("Error message is : "+e);
		}
   	 } 
}

