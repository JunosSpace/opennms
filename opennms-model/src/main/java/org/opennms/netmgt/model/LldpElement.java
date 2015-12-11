package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;

@Entity
@Table(name="lldpElement")
public final class LldpElement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3134355798509685991L;



    private Integer m_id;	
    private String m_lldpChassisId;
	private String m_lldpSysname;
    private LldpChassisIdSubType m_lldpChassisIdSubType;
    private Date m_lldpNodeCreateTime = new Date();
    private Date m_lldpNodeLastPollTime;
	private OnmsNode m_node;

    public LldpElement() {}

    public LldpElement(OnmsNode node, String chassisId, String sysName, LldpChassisIdSubType chassisIdSubType) {
        setNode(node);
        setLldpChassisId(chassisId);
        setLldpSysname(sysName);
        setLldpChassisIdSubType(chassisIdSubType);
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(nullable = false)
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    /**
     * The node this asset information belongs to.
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodeId")
    public OnmsNode getNode() {
        return m_node;
    }

    @Column(name="lldpChassisIdSubType", nullable = false)
    @Type(type="org.opennms.netmgt.model.LldpChassisIdSubTypeUserType")
    public LldpChassisIdSubType getLldpChassisIdSubType() {
		return m_lldpChassisIdSubType;
	}

    @Column(name="lldpSysname" , length=256, nullable = false)
	public String getLldpSysname() {
		return m_lldpSysname;
	}

    @Column(name="lldpChassisId" , length=256, nullable = false)
	public String getLldpChassisId() {
		return m_lldpChassisId;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lldpNodeCreateTime", nullable=false)
    public Date getLldpNodeCreateTime() {
		return m_lldpNodeCreateTime;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lldpNodeLastPollTime", nullable=false)
	public Date getLldpNodeLastPollTime() {
		return m_lldpNodeLastPollTime;
	}

    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(final Integer id) {
        m_id = id;
    }

    /**
     * Set the node associated with the Lldp Element record
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setNode(OnmsNode node) {
        m_node = node;
    }

	public void setLldpSysname(String lldpSysname) {
		m_lldpSysname = lldpSysname;
	}

	public void setLldpChassisId(String lldpChassisId) {
		m_lldpChassisId = lldpChassisId;
	}

	public void setLldpChassisIdSubType(LldpChassisIdSubType lldpChassisIdSubType) {
		m_lldpChassisIdSubType = lldpChassisIdSubType;
	}

	public void setLldpNodeCreateTime(Date lldpNodeCreateTime) {
		m_lldpNodeCreateTime = lldpNodeCreateTime;
	}

	public void setLldpNodeLastPollTime(Date lldpNodeLastPollTime) {
		m_lldpNodeLastPollTime = lldpNodeLastPollTime;
	}


	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return new ToStringBuilder(this)
			.append("Nodeid", m_node.getId())
			.append("lldpChassisSubType", LldpChassisIdSubType.getTypeString(m_lldpChassisIdSubType.getValue()))
			.append("lldpChassisId", m_lldpChassisId)
			.append("lldpSysName", m_lldpSysname)
			.append("lldpNodeCreateTime", m_lldpNodeCreateTime)
			.append("lldpNodeLastPollTime", m_lldpNodeLastPollTime)
			.toString();
	}
	
	public void merge(LldpElement element) {
		if (element == null)
			return;
		setLldpChassisId(element.getLldpChassisId());
		setLldpChassisIdSubType(element.getLldpChassisIdSubType());
		setLldpSysname(element.getLldpSysname());
		setLldpNodeLastPollTime(element.getLldpNodeCreateTime());
	}
}
