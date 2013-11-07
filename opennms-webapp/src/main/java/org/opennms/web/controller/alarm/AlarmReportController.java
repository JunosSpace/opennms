/*******************************************************************************
* This file is part of OpenNMS(R).
*
* Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with OpenNMS(R). If not, see:
* http://www.gnu.org/licenses/
*
* For more information contact:
* OpenNMS(R) Licensing <license@opennms.org>
* http://www.opennms.org/
* http://www.opennms.com/
*******************************************************************************/

package org.opennms.web.controller.alarm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.api.reporting.ReportFormat;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.reporting.core.svclayer.ReportWrapperService;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.event.Event;
import org.opennms.web.event.EventQueryParms;
import org.opennms.web.event.EventUtil;
import org.opennms.web.event.SortStyle;
import org.opennms.web.event.WebEventRepository;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import org.springframework.web.servlet.view.RedirectView;
import java.io.ByteArrayOutputStream;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.opennms.web.rest.AlarmBean;
import org.opennms.web.rest.AlarmRestResource;
import org.opennms.web.rest.Task;

/**
* This servlet receives an HTTP POST with a list of alarms and export or export all action
* for the selected alarms. and then it displays the alarm reports on current URL page
*
*/

public class AlarmReportController extends AbstractController implements InitializingBean {
    
    /** Constant <code>EXPORT_ACTION="1"</code> */
    public final static String EXPORT_ACTION = "1";
    
    /** Constant <code>EXPORTALL_ACTION="2"</code> */
    public final static String EXPORTALL_ACTION = "2";
    
    /** To hold report file name <code>EXPORT_LIMIT=100000</code> */
    public static int EXPORT_LIMIT = 100000;
    
    /**
         * OpenNMS alarm repository
         */
    private AlarmRepository m_webAlarmRepository;
    
    /**
* OpenNMS event repository
*/
    private WebEventRepository m_webEventRepository;
    
    /**
* OpenNMS report wrapper service
*/
    private ReportWrapperService m_reportWrapperService;
    
    /** To hold default redirectView page */
    private String m_redirectView;
    
    /**
* Logging
*/
    private Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + AlarmReportController.class.getName());

    /**
* <p>setWebAlarmRepository</p>
*
* @param webAlarmRepository a {@link org.opennms.netmgt.dao.AlarmRepository} object.
*/
    public void setAlarmRepository(AlarmRepository webAlarmRepository) {
        m_webAlarmRepository = webAlarmRepository;
        AlarmRestResource.setAlarmRepository(webAlarmRepository);
    }

    /**
* <p>setWebEventRepository</p>
*
* @param webEventRepository a {@link org.opennms.web.event.WebEventRepository} object.
*/
    public void setWebEventRepository(WebEventRepository webEventRepository) {
        m_webEventRepository = webEventRepository;
        AlarmRestResource.setWebEventRepository(webEventRepository);
    }
    
    /**
* <p>setReportWrapperService</p>
*
* @param reportWrapperService a {@link org.opennms.reporting.core.svclayer.ReportWrapperService} object.
*/
    public void setReportWrapperService(ReportWrapperService reportWrapperService) {
        m_reportWrapperService = reportWrapperService;
        AlarmRestResource.setReportWrapperService(reportWrapperService);
    }
    
    /**
     * <p>setRedirectView</p>
     *
     * @param redirectView a {@link java.lang.String} object.
     */
    public void setRedirectView(String redirectView) {
        m_redirectView = redirectView;
    }
    

    /**    
* <p>afterPropertiesSet</p>
*
* @throws java.lang.Exception if any.
*/
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_webAlarmRepository, "webAlarmRepository must be set");
        Assert.notNull(m_webEventRepository, "webEventRepository must be set");
        Assert.notNull(m_reportWrapperService, "webAlarmRepository must be set");
        Assert.notNull(m_redirectView, "redirectView must be set");
    }

    /**
* {@inheritDoc}
*
* Export or export all action of the selected alarms specified in the POST and
* then display the client to an appropriate URL.
*/
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        
    	// Handle the alarm bean class
        AlarmBean alarmBean = new AlarmBean();
    	
        // Handle the alarm id strings parameter
    	String[] alarmIdStrings = request.getParameterValues("alarm");
    	alarmBean.setAlarmids(alarmIdStrings);
    	
    	// Handle the actionCode parameter
        String action = request.getParameter("actionCode");
        alarmBean.setAction(action);
        
        // Handle the acknowledge type parameter
        String ackTypeString = request.getParameter("acktype");
        alarmBean.setAcktype(ackTypeString);
        
        // Handle the sortStyle type parameter
        String sortStyleString = request.getParameter("sortby");
        alarmBean.setSortStyle(sortStyleString);
        
        // Handle the filter parameter
        String[] filterStrings = request.getParameterValues("filter");
        alarmBean.setFilterStrings(filterStrings);
        
        // Handle the reportId parameter
        String reportId = request.getParameter("reportId");
        if(reportId!=null){
        	alarmBean.setReportId(reportId);
        }
        
        // Handle the report format parameter
        String requestFormat = request.getParameter("format");
        if(requestFormat!=null){
        	alarmBean.setRequestFormat(requestFormat);
        }
        
        // Handle the alarm report folder name parameter
        String folderName = request.getRemoteUser()+"_alarm_report_"+new SimpleDateFormat("MMddyyyy_HHmmss").format(new Date());
    	if(folderName!=null){
    		alarmBean.setFolderName(folderName);
    	}
        
    	// Handle the alarm bean marshal string 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String bean = null;
		try {
			JAXBContext m_context = JAXBContext.newInstance(AlarmBean.class);
			Marshaller m_marshaller = m_context.createMarshaller();
			m_marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m_marshaller = m_context.createMarshaller();
			m_marshaller.marshal(alarmBean, out);
			bean = out.toString();
			out.close();
		} catch(Exception ex) {
    		ex.printStackTrace();
    		logger.error("Unable to marshall your alarm bean class because of  "+ex.getMessage());
    	} finally {
    		if(out != null){
    			out.close();
    		}
    	}
		
        // Get the address for junos server
        String addressForServer = System.getProperty("junos.server.address");
		
		// Handle the rest client for junos rest api
    	ClientResponse clientResponse = null;
        try{	
    		if(addressForServer!=null && addressForServer!=" "){
    			Client client = Client.create();
    			client.setReadTimeout(60000);
    			WebResource service=client.resource("http://"+addressForServer+":8080/fmpm/restful/alarms/export");
    			clientResponse = service.type(MediaType.APPLICATION_XML).post(ClientResponse.class,bean);
    		} else {
    			logger.error("Unable to call junose rest api because junos server address ["+addressForServer+"] is invalid");
    		}
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		logger.error("Unable to call junose rest api from alarm report controller because of "+ex.getMessage());
    	}
        
        // Get the response status from junos rest api
        int taskId = -1;
        try {
	        Task restTask = clientResponse.getEntity(Task.class);
	        if(restTask!=null){
	        	taskId = restTask.getId();
	        }
        } catch(Exception ex) {
        	logger.error("Could not get the task response from junose rest api");
        }
        
    	// Handle the redirect parameters
        String redirectParms = request.getParameter("redirectParms");
        String redirectPage = request.getParameter("redirectPage");
        String viewName = m_redirectView;
        
        if(redirectParms!=null && redirectParms != "" && redirectParms != " "){
        	viewName = m_redirectView + "?" + redirectParms;
        } else if(redirectPage!= null){
        	viewName = redirectPage;
        }
        
        RedirectView view = new RedirectView(viewName, true);
        request.getSession().setAttribute("actionStatus","E"+","+taskId);
        
        logger.info("Job Id["+taskId+"] for your AlarmReportController action");
        return new ModelAndView(view);
    }
}

