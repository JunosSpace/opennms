package org.opennms.netmgt.provision.service;

import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpasoIftypeTableTracker extends TableTracker {
	private final static Logger LOG = LoggerFactory.getLogger(IpasoIftypeTableTracker.class);

	public static final SnmpObjId IFTYPE_TABLE_ENTRY = SnmpObjId.get("1.3.6.1.4.1.119.2.3.69.501.4.1.1.3");

	public static final SnmpObjId[] s_iftypeptable_elemList = new SnmpObjId[] {

			IFTYPE_TABLE_ENTRY };

	public class IftypeRow extends SnmpRowResult {

		public IftypeRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
			LOG.debug("column count = {}, instance = {}", columnCount, instance);
		}

		public Integer getSlotNum() {
			return getInstance().getSubIdAt(0);
		}

		public Integer getIfType() {
			return getValue(IFTYPE_TABLE_ENTRY).toInt();
		}
	}

	public IpasoIftypeTableTracker() {
		super(s_iftypeptable_elemList);
	}

	public IpasoIftypeTableTracker(final RowCallback rowProcessor) {
		super(rowProcessor, s_iftypeptable_elemList);
	}

	@Override
	public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
		return new IftypeRow(columnCount, instance);
	}

	@Override
	public void rowCompleted(final SnmpRowResult row) {
		processIftypeRow((IftypeRow) row);
	}

	public void processIftypeRow(final IftypeRow row) {
	}
}
