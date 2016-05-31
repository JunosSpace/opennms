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

package org.opennms.netmgt.model;

import java.io.Serializable;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.MapKey;
import org.hibernate.annotations.Type;
import org.springframework.core.style.ToStringCreator;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@XmlRootElement(name = "user-preference-state")
@Entity
@Table(name = "UserPreferenceState")
public class UserPreferenceState implements Serializable {

	private Integer m_id;
	private String m_stateName;
	private String m_stateValue;
	private String m_userName;
	
    @Id
	@SequenceGenerator(name = "userPreferenceSequence", sequenceName = "userPreferenceNxtId")
	@GeneratedValue(generator = "userPreferenceSequence")
	@Column(name = "userPreferenceId", nullable = false)
	@XmlAttribute(name = "id")
	public Integer getId() {
		return this.m_id;
	}
        
	@Column(name = "stateName", length = 256, nullable = false)
	@XmlElement(name = "stateName")
	public String getStateName() {
		return m_stateName;
	}   

	@Column(name = "stateValue", length = 1800000)
	@XmlElement(name = "stateValue")
	public String getStateValue() {
		return m_stateValue;
	}  

	@Column(name = "userName", length = 256, nullable = false)
	@XmlElement(name = "user-name")
	public String getUserName() {
		return m_userName;
	}  

	/**
	 * default constructor
	 */
	public UserPreferenceState() {
	}

	public UserPreferenceState(Integer id, String stateName, String stateValue, String userName) {
		this.m_id = id;
		this.m_stateName = stateName;
		this.m_stateValue = stateValue;
		this.m_userName = userName;
	}


	// ------------------------------------getters/setters---------------------------------

	public void setId(int id) {
		    this.m_id = id;
	}

	public void setStateName(String name) {
		this.m_stateName = name;
	}

	

	public void setStateValue(String stateValue) {
		this.m_stateValue = stateValue;
	}

	

	public void setUserName(String userName) {
		this.m_userName = userName;
	}
}
