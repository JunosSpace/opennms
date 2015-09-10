package org.opennms.netmgt.linkd;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.eventd.datablock.EventUtil;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IPASOEventProcessor implements EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(IPASOEventProcessor.class);
    
    private static final String  RX_LEVEL_ALARM_100_200 = "uei.opennms.org/traps/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_rxLevelAlarm" ;
    private static final String  TX_POWER_ALARM_100_200 = "uei.opennms.org/traps/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_txPowerAlarm" ;
    private static final String  LOF_100_200 = "uei.opennms.org/traps/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_lof" ;
    private static final String  FRAME_ID_100_200 = "uei.opennms.org/traps/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_frameID" ;
    private static final String  TDM_RANGE_MISMATCH_100_200 = "uei.opennms.org/traps/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_tdmRangeMismatch" ;
    private static final String  L2_SYNC_LOSS_ALARM_100_200  = "uei.opennms.org/traps/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_l2SyncLossAlarm" ;
    private static final String  STM1_LOS_ALARM_100_200 = "uei.opennms.org/traps/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_stm1LosAlarm" ;
    private static final String  E1_LOS_ALARM_100_200 = "uei.opennms.org/traps/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_e1LosAlarm" ;
    private static final String  LP_UNEQUIPED_ALARM_100_200 = "uei.opennms.org/traps/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_lpUnequipedAlarm" ;
    private static final String  AS_ETH_PORT_LINK_STATUS_100_200 = "uei.opennms.org/traps/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_asETHPortLinkStatus" ;
    private static final String  AS_ETH_PORT_SFP_LOS_100_200 = "uei.opennms.org/traps/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_asETHPortSFPLos" ;
    private static final String  AS_ETH_PORT_SFP_TX_ERROR_100_200 = "uei.opennms.org/traps/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_asETHPortSFPTxError" ;

    private static final String  RX_LEVEL_ALARM_400 = "uei.opennms.org/traps/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_rxLevelAlarm" ;
    private static final String  TX_POWER_ALARM_400 = "uei.opennms.org/traps/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_txPowerAlarm" ;
    private static final String  LOF_400 = "uei.opennms.org/traps/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_lof" ;
    private static final String  FRAME_ID_400 = "uei.opennms.org/traps/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_frameID" ;
    private static final String  TDM_RANGE_MISMATCH_400 = "uei.opennms.org/traps/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_tdmRangeMismatch" ;
    private static final String  L2_SYNC_LOSS_ALARM_400 = "uei.opennms.org/traps/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_l2SyncLossAlarm" ;
    private static final String  STM1_LOS_ALARM_400 = "uei.opennms.org/traps/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_stm1LosAlarm" ;
    private static final String  E1_LOS_ALARM_400 = "uei.opennms.org/traps/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_e1LosAlarm" ;
    private static final String  LP_UNEQUIPED_ALARM_400 = "uei.opennms.org/traps/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_lpUnequipedAlarm" ;
    private static final String  AS_ETH_PORT_LINK_STATUS_400 = "uei.opennms.org/traps/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_asETHPortLinkStatus" ;
    private static final String  AS_ETH_PORT_SFP_LOS_400 = "uei.opennms.org/traps/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_asETHPortSFPLos" ;
    private static final String  AS_ETH_PORT_SFP_TX_ERROR_400 = "uei.opennms.org/traps/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_asETHPortSFPTxError" ;

    
    
    
    private static final String  RX_LEVEL_ALARM_CLEARE_100_200 = "uei.opennms.org/clears/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_rxLevelAlarm" ;
    private static final String  TX_POWER_ALARM_CLEARE_100_200 = "uei.opennms.org/clears/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_txPowerAlarm" ;
    private static final String  LOF_CLEARE_100_200 = "uei.opennms.org/clears/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_lof" ;
    private static final String  FRAME_ID_CLEARE_100_200 = "uei.opennms.org/clears/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_frameID" ;
    private static final String  TDM_RANGE_MISMATCH_CLEARE_100_200 = "uei.opennms.org/clears/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_tdmRangeMismatch" ;
    private static final String  L2_SYNC_LOSS_ALARM_CLEARE_100_200  = "uei.opennms.org/clears/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_l2SyncLossAlarm" ;
    private static final String  STM1_LOS_ALARM_CLEARE_100_200 = "uei.opennms.org/clears/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_stm1LosAlarm" ;
    private static final String  E1_LOS_ALARM_CLEARE_100_200 = "uei.opennms.org/clears/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_e1LosAlarm" ;
    private static final String  LP_UNEQUIPED_ALARM_CLEARE_100_200 = "uei.opennms.org/clears/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_lpUnequipedAlarm" ;
    private static final String  AS_ETH_PORT_LINK_STATUS_CLEARE_100_200 = "uei.opennms.org/clears/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_asETHPortLinkStatus" ;
    private static final String  AS_ETH_PORT_SFP_LOS_CLEARE_100_200 = "uei.opennms.org/clears/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_asETHPortSFPLos" ;
    private static final String  AS_ETH_PORT_SFP_TX_ERROR_CLEARE_100_200 = "uei.opennms.org/clears/MIB-PNMSj-IPASOLINK-100-200-COMMON-MIB/alarmTrap_asETHPortSFPTxError" ;

    private static final String  RX_LEVEL_ALARM_CLEARE_400 = "uei.opennms.org/clears/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_rxLevelAlarm" ;
    private static final String  TX_POWER_ALARM_CLEARE_400 = "uei.opennms.org/clears/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_txPowerAlarm" ;
    private static final String  LOF_CLEARE_400 = "uei.opennms.org/clears/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_lof" ;
    private static final String  FRAME_ID_CLEARE_400 = "uei.opennms.org/clears/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_frameID" ;
    private static final String  TDM_RANGE_MISMATCH_CLEARE_400 = "uei.opennms.org/clears/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_tdmRangeMismatch" ;
    private static final String  L2_SYNC_LOSS_ALARM_CLEARE_400 = "uei.opennms.org/clears/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_l2SyncLossAlarm" ;
    private static final String  STM1_LOS_ALARM_CLEARE_400 = "uei.opennms.org/clears/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_stm1LosAlarm" ;
    private static final String  E1_LOS_ALARM_CLEARE_400 = "uei.opennms.org/clears/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_e1LosAlarm" ;
    private static final String  LP_UNEQUIPED_ALARM_CLEARE_400 = "uei.opennms.org/clears/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_lpUnequipedAlarm" ;
    private static final String  AS_ETH_PORT_LINK_STATUS_CLEARE_400 = "uei.opennms.org/clears/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_asETHPortLinkStatus" ;
    private static final String  AS_ETH_PORT_SFP_LOS_CLEARE_400 = "uei.opennms.org/clears/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_asETHPortSFPLos" ;
    private static final String  AS_ETH_PORT_SFP_TX_ERROR_CLEARE_400 = "uei.opennms.org/clears/MIB-PNMSJ-IPASOLINK-400-COMMON-MIB/alarmTrap_asETHPortSFPTxError" ;

    
    private static final String  FRAME_ID_UEI_LINK = "uei.opennms.org/internal/linkd/trap/frameID" ;
    private static final String  TX_POWER_ALARM_UEI_LINK = "uei.opennms.org/internal/linkd/trap/txPowerAlarm" ;
    private static final String  RX_LEVEL_ALARM_UEI_LINK = "uei.opennms.org/internal/linkd/trap/rxLevelAlarm" ;
    private static final String  AS_ETH_PORT_SFP_LOS_UEI_LINK = "uei.opennms.org/internal/linkd/trap/asETHPortSFPLos" ;
    private static final String  LP_UNEQUIPED_ALARM_UEI_LINK = "uei.opennms.org/internal/linkd/trap/lpUnequipedAlarm" ;
    private static final String  AS_ETH_PORT_SFP_TX_ERROR_UEI_LINK = "uei.opennms.org/internal/linkd/trap/asETHPortSFPTxError" ;
    private static final String  E1_LOS_ALARM_UEI_LINK = "uei.opennms.org/internal/linkd/trap/e1LosAlarm" ;
    private static final String  TDM_RANGE_MISMATCH_UEI_LINK = "uei.opennms.org/internal/linkd/trap/tdmRangeMismatch" ;
    private static final String  STM1_LOS_ALARM_UEI_LINK = "uei.opennms.org/internal/linkd/trap/stm1LosAlarm" ;
    private static final String  AS_ETH_PORT_LINK_STATUS_UEI_LINK = "uei.opennms.org/internal/linkd/trap/asETHPortLinkStatus" ;
    private static final String  LOF_UEI_LINK = "uei.opennms.org/internal/linkd/trap/lof" ;
    private static final String  L2_SYNC_LOSS_ALARM_UEI_LINK = "uei.opennms.org/internal/linkd/trap/l2SyncLossAlarm" ;
    
    private static final String  FRAME_ID_UEI_LINK_CLEARE = "uei.opennms.org/internal/linkd/cleare/frameID" ;
    private static final String  TX_POWER_ALARM_UEI_LINK_CLEARE = "uei.opennms.org/internal/linkd/cleare/txPowerAlarm" ;
    private static final String  RX_LEVEL_ALARM_UEI_LINK_CLEARE = "uei.opennms.org/internal/linkd/cleare/rxLevelAlarm" ;
    private static final String  AS_ETH_PORT_SFP_LOS_UEI_LINK_CLEARE = "uei.opennms.org/internal/linkd/cleare/asETHPortSFPLos" ;
    private static final String  LP_UNEQUIPED_ALARM_UEI_LINK_CLEARE = "uei.opennms.org/internal/linkd/cleare/lpUnequipedAlarm" ;
    private static final String  AS_ETH_PORT_SFP_TX_ERROR_UEI_LINK_CLEARE = "uei.opennms.org/internal/linkd/cleare/asETHPortSFPTxError" ;
    private static final String  E1_LOS_ALARM_UEI_LINK_CLEARE = "uei.opennms.org/internal/linkd/cleare/e1LosAlarm" ;
    private static final String  TDM_RANGE_MISMATCH_UEI_LINK_CLEARE = "uei.opennms.org/internal/linkd/cleare/tdmRangeMismatch" ;
    private static final String  STM1_LOS_ALARM_UEI_LINK_CLEARE = "uei.opennms.org/internal/linkd/cleare/stm1LosAlarm" ;
    private static final String  AS_ETH_PORT_LINK_STATUS_UEI_LINK_CLEARE = "uei.opennms.org/internal/linkd/cleare/asETHPortLinkStatus" ;
    private static final String  LOF_UEI_LINK_CLEARE = "uei.opennms.org/internal/linkd/cleare/lof" ;
    private static final String  L2_SYNC_LOSS_ALARM_UEI_LINK_CLEARE = "uei.opennms.org/internal/linkd/cleare/l2SyncLossAlarm" ;
    
    
    private EventForwarder m_eventForwarder;
    
    IPASOEventProcessor(EventForwarder eventForwarder) {
        // Create the selector for the ueis this service is interested in
        //
        
        m_eventForwarder = eventForwarder;
        List<String> ueiList = new ArrayList<String>();

        ueiList.add( RX_LEVEL_ALARM_100_200 );
        ueiList.add( TX_POWER_ALARM_100_200 );
        ueiList.add( LOF_100_200 );
        ueiList.add( FRAME_ID_100_200 );
        ueiList.add( TDM_RANGE_MISMATCH_100_200 );
        ueiList.add( L2_SYNC_LOSS_ALARM_100_200  );
        ueiList.add( STM1_LOS_ALARM_100_200 );
        ueiList.add( E1_LOS_ALARM_100_200 );
        ueiList.add( LP_UNEQUIPED_ALARM_100_200 );
        ueiList.add( AS_ETH_PORT_LINK_STATUS_100_200 );
        ueiList.add( AS_ETH_PORT_SFP_LOS_100_200 );
        ueiList.add( AS_ETH_PORT_SFP_TX_ERROR_100_200 );

        ueiList.add( RX_LEVEL_ALARM_400 );
        ueiList.add( TX_POWER_ALARM_400 );
        ueiList.add( LOF_400 );
        ueiList.add( FRAME_ID_400 );
        ueiList.add( TDM_RANGE_MISMATCH_400 );
        ueiList.add( L2_SYNC_LOSS_ALARM_400 );
        ueiList.add( STM1_LOS_ALARM_400 );
        ueiList.add( E1_LOS_ALARM_400 );
        ueiList.add( LP_UNEQUIPED_ALARM_400 );
        ueiList.add( AS_ETH_PORT_LINK_STATUS_400 );
        ueiList.add( AS_ETH_PORT_SFP_LOS_400 );
        ueiList.add( AS_ETH_PORT_SFP_TX_ERROR_400 );
        
        ueiList.add( RX_LEVEL_ALARM_CLEARE_100_200 );
        ueiList.add( TX_POWER_ALARM_CLEARE_100_200 );
        ueiList.add( LOF_CLEARE_100_200 );
        ueiList.add( FRAME_ID_CLEARE_100_200 );
        ueiList.add( TDM_RANGE_MISMATCH_CLEARE_100_200 );
        ueiList.add( L2_SYNC_LOSS_ALARM_CLEARE_100_200  );
        ueiList.add( STM1_LOS_ALARM_CLEARE_100_200 );
        ueiList.add( E1_LOS_ALARM_CLEARE_100_200 );
        ueiList.add( LP_UNEQUIPED_ALARM_CLEARE_100_200 );
        ueiList.add( AS_ETH_PORT_LINK_STATUS_CLEARE_100_200 );
        ueiList.add( AS_ETH_PORT_SFP_LOS_CLEARE_100_200 );
        ueiList.add( AS_ETH_PORT_SFP_TX_ERROR_CLEARE_100_200 );
        
        ueiList.add( RX_LEVEL_ALARM_CLEARE_400 );
        ueiList.add( TX_POWER_ALARM_CLEARE_400 );
        ueiList.add( LOF_CLEARE_400 );
        ueiList.add( FRAME_ID_CLEARE_400 );
        ueiList.add( TDM_RANGE_MISMATCH_CLEARE_400 );
        ueiList.add( L2_SYNC_LOSS_ALARM_CLEARE_400  );
        ueiList.add( STM1_LOS_ALARM_CLEARE_400 );
        ueiList.add( E1_LOS_ALARM_CLEARE_400 );
        ueiList.add( LP_UNEQUIPED_ALARM_CLEARE_400 );
        ueiList.add( AS_ETH_PORT_LINK_STATUS_CLEARE_400 );
        ueiList.add( AS_ETH_PORT_SFP_LOS_CLEARE_400 );
        ueiList.add( AS_ETH_PORT_SFP_TX_ERROR_CLEARE_400 );
        
        EventIpcManagerFactory.init();
        EventIpcManagerFactory.getIpcManager().addEventListener(this, ueiList);
    }



    public String getName() {
        return "ipaso:LinkAlarmBroadCastEvent";
    }

    public void onEvent(Event e) {
        String eventUei = e.getUei();
        
        if (eventUei == null)
            return;

        LOG.debug("Received event: {}", eventUei);

        String linkUei = null;
        if (e.getSeverity().equals("Cleared")) {
            
            linkUei = getUeiForCleareEvent(eventUei);
        } else {
            linkUei = getUeiForEvent(eventUei);
        }
        
        if (linkUei == null) {
            return;
        }
        
        m_eventForwarder.sendNow(createEvent(e, linkUei));
        
        
    }
    
    private Event createEvent(Event e , String linkUei){
        EventBuilder eventBuilder = new EventBuilder(linkUei, "linkd");
        
        eventBuilder.setNodeid(e.getNodeid());
        String ifIndex = EventUtil.getValueOfParm("parm[#6]", e);
        if (ifIndex != null && ifIndex.matches("^([0-9]*)$")) {
            eventBuilder.setIfIndex(new Integer(ifIndex));
        }
        eventBuilder.setSeverity(e.getSeverity());
        
        eventBuilder.setParms(e.getParmCollection());
        
        return eventBuilder.getEvent();
    }
    
    private String getUeiForEvent(String uei){
        
        if (uei.equals(RX_LEVEL_ALARM_100_200) || uei.equals(RX_LEVEL_ALARM_400)) {
            return RX_LEVEL_ALARM_UEI_LINK;
        } else if (uei.equals(TX_POWER_ALARM_100_200) || uei.equals(TX_POWER_ALARM_400)) {
            return TX_POWER_ALARM_UEI_LINK;
        } else if (uei.equals(LOF_100_200) || uei.equals(LOF_400)) {
            return LOF_UEI_LINK;
        } else if (uei.equals(FRAME_ID_100_200) || uei.equals(FRAME_ID_400)) {
            return FRAME_ID_UEI_LINK;
        } else if (uei.equals(TDM_RANGE_MISMATCH_100_200) || uei.equals(TDM_RANGE_MISMATCH_400)) {
            return TDM_RANGE_MISMATCH_UEI_LINK;
        } else if (uei.equals(L2_SYNC_LOSS_ALARM_100_200) || uei.equals(L2_SYNC_LOSS_ALARM_400)) {
            return L2_SYNC_LOSS_ALARM_UEI_LINK;
        } else if (uei.equals(STM1_LOS_ALARM_100_200) || uei.equals(STM1_LOS_ALARM_400)) {
            return STM1_LOS_ALARM_UEI_LINK;
        } else if (uei.equals(E1_LOS_ALARM_100_200) || uei.equals(E1_LOS_ALARM_400)) {
            return E1_LOS_ALARM_UEI_LINK;
        } else if (uei.equals(LP_UNEQUIPED_ALARM_100_200) || uei.equals(LP_UNEQUIPED_ALARM_400)) {
            return LP_UNEQUIPED_ALARM_UEI_LINK;
        } else if (uei.equals(AS_ETH_PORT_LINK_STATUS_100_200) || uei.equals(AS_ETH_PORT_LINK_STATUS_400)) {
            return AS_ETH_PORT_LINK_STATUS_UEI_LINK;
        } else if (uei.equals(AS_ETH_PORT_SFP_LOS_100_200) || uei.equals(AS_ETH_PORT_SFP_LOS_400)) {
            return AS_ETH_PORT_SFP_LOS_UEI_LINK;
        } else if (uei.equals(AS_ETH_PORT_SFP_TX_ERROR_100_200) || uei.equals(AS_ETH_PORT_SFP_TX_ERROR_400)) {
            return AS_ETH_PORT_SFP_TX_ERROR_UEI_LINK;
        }
        
        return null;
        
    }
    
    private String getUeiForCleareEvent(String uei){
        if (uei.equals(RX_LEVEL_ALARM_CLEARE_100_200) || uei.equals(RX_LEVEL_ALARM_CLEARE_400)) {
            return RX_LEVEL_ALARM_UEI_LINK_CLEARE;
        } else if (uei.equals(TX_POWER_ALARM_CLEARE_100_200) || uei.equals(TX_POWER_ALARM_CLEARE_400)) {
            return TX_POWER_ALARM_UEI_LINK_CLEARE;
        } else if (uei.equals(LOF_CLEARE_100_200) || uei.equals(LOF_CLEARE_400)) {
            return LOF_UEI_LINK_CLEARE;
        } else if (uei.equals(FRAME_ID_CLEARE_100_200) || uei.equals(FRAME_ID_CLEARE_400)) {
            return FRAME_ID_UEI_LINK_CLEARE;
        } else if (uei.equals(TDM_RANGE_MISMATCH_CLEARE_100_200) || uei.equals(TDM_RANGE_MISMATCH_CLEARE_400)) {
            return TDM_RANGE_MISMATCH_UEI_LINK_CLEARE;
        } else if (uei.equals(L2_SYNC_LOSS_ALARM_CLEARE_100_200) || uei.equals(L2_SYNC_LOSS_ALARM_CLEARE_400)) {
            return L2_SYNC_LOSS_ALARM_UEI_LINK_CLEARE;
        } else if (uei.equals(STM1_LOS_ALARM_CLEARE_100_200) || uei.equals(STM1_LOS_ALARM_CLEARE_400)) {
            return STM1_LOS_ALARM_UEI_LINK_CLEARE;
        } else if (uei.equals(E1_LOS_ALARM_CLEARE_100_200) || uei.equals(E1_LOS_ALARM_CLEARE_400)) {
            return E1_LOS_ALARM_UEI_LINK_CLEARE;
        } else if (uei.equals(LP_UNEQUIPED_ALARM_CLEARE_100_200) || uei.equals(LP_UNEQUIPED_ALARM_CLEARE_400)) {
            return LP_UNEQUIPED_ALARM_UEI_LINK_CLEARE;
        } else if (uei.equals(AS_ETH_PORT_LINK_STATUS_CLEARE_100_200) || uei.equals(AS_ETH_PORT_LINK_STATUS_CLEARE_400)) {
            return AS_ETH_PORT_LINK_STATUS_UEI_LINK_CLEARE;
        } else if (uei.equals(AS_ETH_PORT_SFP_LOS_CLEARE_100_200) || uei.equals(AS_ETH_PORT_SFP_LOS_CLEARE_400)) {
            return AS_ETH_PORT_SFP_LOS_UEI_LINK_CLEARE;
        } else if (uei.equals(AS_ETH_PORT_SFP_TX_ERROR_CLEARE_100_200) || uei.equals(AS_ETH_PORT_SFP_TX_ERROR_CLEARE_400)) {
            return AS_ETH_PORT_SFP_TX_ERROR_UEI_LINK_CLEARE;
        }
        
        return null;
    }

}
