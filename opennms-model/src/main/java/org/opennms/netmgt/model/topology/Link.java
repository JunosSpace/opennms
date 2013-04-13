package org.opennms.netmgt.model.topology;

import java.io.Serializable;


/**
 * This class represents a physical link between 2 network end points
 * such as an Ethernet connection or a virtual link between 2 end points
 * such as an IP address connection to a subnetwork.  Can also be used
 * represent a network service between to service end points.
 *  
 * @author antonio
 *
 */
public abstract class Link {

		public final static class LinkType extends AbstractType 
		implements Comparable<LinkType>, Serializable {

			private static final long serialVersionUID = 7220152765747623134L;

			public static final int LINK_TYPE_LLDP=1;
			
			public static LinkType LLDP = new LinkType(LINK_TYPE_LLDP);

			public LinkType(Integer linkType) {
				super(linkType);
			}
			
	        static {
	        	s_order.add(1, 1);
	        	s_typeMap.put(1, "lldp" );
	        }

	        @Override
	        public int compareTo(LinkType o) {
	            return getIndex(m_type) - getIndex(o.m_type);
	        }

	        @Override
	        public boolean equals(Object o) {
	            if (o instanceof LinkType) {
	                return m_type.intValue() == ((LinkType)o).m_type.intValue();
	            }
	            return false;
	        }

	        public static LinkType get(Integer code) {
	            if (code == null)
	                throw new IllegalArgumentException("Cannot create LinkType from null code");
	            switch (code) {
	            case LINK_TYPE_LLDP: 		return LLDP;
	            default:
	                throw new IllegalArgumentException("Cannot create LinkType from code "+code);
	            }
	        }		
		}

	private EndPoint m_a;
	
	private EndPoint m_b;
	
	private final LinkType m_linkType;
	
	public Link(LinkType linkType) {
		m_linkType=linkType;
	}
	
	public EndPoint getA() {
		return m_a;
	}

	public void setA(EndPoint a) {
		this.m_a = a;
	}

	public EndPoint getB() {
		return m_b;
	}

	public void setB(EndPoint b) {
		this.m_b = b;
	}
	
	public LinkType getLinkType() {
		return m_linkType;
	}

}