/*******************************************************************************
* This file is part of OpenNMS(R).
*
* Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.reporting.jasperreports.svclayer;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;

import java.io.OutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRGzipVirtualizer;
import net.sf.jasperreports.engine.fill.JRParameterDefaultValuesEvaluator;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRPrintXmlLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportService;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.api.reporting.parameter.ReportDateParm;
import org.opennms.api.reporting.parameter.ReportDoubleParm;
import org.opennms.api.reporting.parameter.ReportFloatParm;
import org.opennms.api.reporting.parameter.ReportIntParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.api.reporting.parameter.ReportStringParm;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.DBUtils;
import org.opennms.features.reporting.repository.global.GlobalReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.text.ParseException;

import java.util.*;
import java.io.IOException;
import java.sql.SQLException;
import org.opennms.netmgt.dao.api.NodeDao;
/**
* <p>
* JasperReportService class.
* </p>
*
* @author jonathan@opennms.org
* @version $Id: $
*/
public class JasperReportService implements ReportService {
    private static final Logger LOG = LoggerFactory.getLogger(JasperReportService.class);

    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    private static final String STRING_INPUT_TYPE = "org.opennms.report.stringInputType";
    
    // Date format for an alarm events
    private static final SimpleDateFormat formater = new SimpleDateFormat("MM/dd/yy hh:mm:ss aaa",Locale.ENGLISH);

    private GlobalReportRepository m_globalReportRepository;

    @Autowired
    AlarmDao m_alarmDao;
    
    @Autowired
    EventDao m_eventDao;

    @Autowired
    AcknowledgmentDao m_ackDao;
    
    @Autowired
    NodeDao m_nodeDao; 
    

    /**
* <p>
* Constructor for JasperReportService.
* </p>
*/
    public JasperReportService() {
    }

    /**
* {@inheritDoc}
*/
    @Override
    public List<ReportFormat> getFormats(String reportId) {
        List<ReportFormat> formats = new ArrayList<ReportFormat>();
        formats.add(ReportFormat.PDF);
        formats.add(ReportFormat.CSV);
        return formats;
    }

    /**
* {@inheritDoc}
*
* @throws ReportException
*/
    @Override
    public ReportParameters getParameters(final String reportId) throws ReportException {
        try {
            return Logging.withPrefix(LOG4J_CATEGORY, new Callable<ReportParameters>() {
                @Override public ReportParameters call() throws Exception {
                    final ReportParameters reportParameters = new ReportParameters();

                    JasperReport jasperReport = null;
                    Map<?, ?> defaultValues = null;

                    try {
                        jasperReport = JasperCompileManager.compileReport(m_globalReportRepository.getTemplateStream(reportId));
                        defaultValues = JRParameterDefaultValuesEvaluator.evaluateParameterDefaultValues(jasperReport, null);
                    } catch (final JRException e) {
                        LOG.error("unable to compile jasper report", e);
                        throw new ReportException("unable to compile jasperReport", e);
                    }

                    final JRParameter[] reportParms = jasperReport.getParameters();

                    final List<ReportIntParm> intParms = new ArrayList<ReportIntParm>();
                    reportParameters.setIntParms(intParms);

                    final List<ReportFloatParm> floatParms = new ArrayList<ReportFloatParm>();
                    reportParameters.setFloatParms(floatParms);

                    final List<ReportDoubleParm> doubleParms = new ArrayList<ReportDoubleParm>();
                    reportParameters.setDoubleParms(doubleParms);

                    final List<ReportStringParm> stringParms = new ArrayList<ReportStringParm>();
                    reportParameters.setStringParms(stringParms);

                    final List<ReportDateParm> dateParms = new ArrayList<ReportDateParm>();
                    reportParameters.setDateParms(dateParms);

                    for (final JRParameter reportParm : reportParms) {

                        if (reportParm.isSystemDefined() == false) {

                            if (reportParm.isForPrompting() == false) {
                                LOG.debug("report parm {} is not for prompting - continuing", reportParm.getName());
                                continue;
                            } else {
                                LOG.debug("found promptable report parm {}", reportParm.getName());

                            }

                            if (reportParm.getValueClassName().equals("java.lang.String")) {
                                LOG.debug("adding a string parm name {}", reportParm.getName());
                                final ReportStringParm stringParm = new ReportStringParm();
                                if (reportParm.getDescription() != null) {
                                    stringParm.setDisplayName(reportParm.getDescription());
                                } else {
                                    stringParm.setDisplayName(reportParm.getName());
                                }
                                if (reportParm.getPropertiesMap().containsProperty(STRING_INPUT_TYPE)) {
                                    stringParm.setInputType(reportParm.getPropertiesMap().getProperty(STRING_INPUT_TYPE));
                                }
                                stringParm.setName(reportParm.getName());
                                if (defaultValues.containsKey(reportParm.getName()) && (defaultValues.get(reportParm.getName()) != null)) {
                                    stringParm.setValue((String) defaultValues.get(reportParm.getName()));
                                } else {
                                    stringParm.setValue(new String());
                                }
                                stringParms.add(stringParm);
                                continue;
                            }

                            if (reportParm.getValueClassName().equals("java.lang.Integer")) {
                                LOG.debug("adding a Integer parm name {}", reportParm.getName());
                                final ReportIntParm intParm = new ReportIntParm();
                                if (reportParm.getDescription() != null) {
                                    intParm.setDisplayName(reportParm.getDescription());
                                } else {
                                    intParm.setDisplayName(reportParm.getName());
                                }
                                intParm.setName(reportParm.getName());
                                if (defaultValues.containsKey(reportParm.getName()) && (defaultValues.get(reportParm.getName()) != null)) {
                                    intParm.setValue((Integer) defaultValues.get(reportParm.getName()));
                                } else {
                                    intParm.setValue(new Integer(0));
                                }
                                intParms.add(intParm);
                                continue;
                            }

                            if (reportParm.getValueClassName().equals("java.lang.Float")) {
                                LOG.debug("adding a Float parm name {}", reportParm.getName());
                                final ReportFloatParm floatParm = new ReportFloatParm();
                                if (reportParm.getDescription() != null) {
                                    floatParm.setDisplayName(reportParm.getDescription());
                                } else {
                                    floatParm.setDisplayName(reportParm.getName());
                                }
                                floatParm.setName(reportParm.getName());
                                if (defaultValues.containsKey(reportParm.getName()) && (defaultValues.get(reportParm.getName()) != null)) {
                                    floatParm.setValue((Float) defaultValues.get(reportParm.getName()));
                                } else {
                                    floatParm.setValue(new Float(0));
                                }
                                floatParms.add(floatParm);
                                continue;
                            }

                            if (reportParm.getValueClassName().equals("java.lang.Double")) {
                                LOG.debug("adding a Double parm name {}", reportParm.getName());
                                final ReportDoubleParm doubleParm = new ReportDoubleParm();
                                if (reportParm.getDescription() != null) {
                                    doubleParm.setDisplayName(reportParm.getDescription());
                                } else {
                                    doubleParm.setDisplayName(reportParm.getName());
                                }
                                doubleParm.setName(reportParm.getName());
                                if (defaultValues.containsKey(reportParm.getName()) && (defaultValues.get(reportParm.getName()) != null)) {
                                    doubleParm.setValue((Double) defaultValues.get(reportParm.getName()));
                                } else {
                                    doubleParm.setValue(new Double(0));
                                }
                                doubleParms.add(doubleParm);
                                continue;
                            }

                            if (reportParm.getValueClassName().equals("java.util.Date")) {
                                LOG.debug("adding a java.util.Date parm name {}", reportParm.getName());
                                final ReportDateParm dateParm = new ReportDateParm();
                                dateParm.setUseAbsoluteDate(false);
                                if (reportParm.getDescription() != null) {
                                    dateParm.setDisplayName(reportParm.getDescription());
                                } else {
                                    dateParm.setDisplayName(reportParm.getName());
                                }
                                dateParm.setName(reportParm.getName());
                                dateParm.setCount(new Integer(1));
                                dateParm.setInterval("day");
                                dateParm.setHours(0);
                                dateParm.setMinutes(0);
                                if (defaultValues.containsKey(reportParm.getName()) && (defaultValues.get(reportParm.getName()) != null)) {
                                    dateParm.setDate((Date) defaultValues.get(reportParm.getName()));
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(dateParm.getDate());
                                    dateParm.setMinutes(cal.get(Calendar.MINUTE));
                                    dateParm.setHours(cal.get(Calendar.HOUR_OF_DAY));
                                } else {
                                    final Calendar cal = Calendar.getInstance();
                                    cal.set(Calendar.HOUR_OF_DAY, 0);
                                    cal.set(Calendar.MINUTE, 0);
                                    cal.set(Calendar.SECOND, 0);
                                    cal.set(Calendar.MILLISECOND, 0);
                                    dateParm.setDate(cal.getTime());
                                }
                                dateParms.add(dateParm);
                                continue;
                            }

                            if (reportParm.getValueClassName().equals("java.sql.Date") || reportParm.getValueClassName().equals("java.sql.Timestamp")) {
                                LOG.debug("adding a java.sql.Date or Timestamp parm name {}", reportParm.getName());
                                final ReportDateParm dateParm = new ReportDateParm();
                                dateParm.setUseAbsoluteDate(false);
                                if (reportParm.getDescription() != null) {
                                    dateParm.setDisplayName(reportParm.getDescription());
                                } else {
                                    dateParm.setDisplayName(reportParm.getName());
                                }
                                dateParm.setName(reportParm.getName());
                                dateParm.setCount(new Integer(1));
                                dateParm.setInterval("day");
                                dateParm.setHours(0);
                                dateParm.setMinutes(0);
                                if (defaultValues.containsKey(reportParm.getName()) && (defaultValues.get(reportParm.getName()) != null)) {
                                    dateParm.setDate((Date) defaultValues.get(reportParm.getName()));
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(dateParm.getDate());
                                    dateParm.setMinutes(cal.get(Calendar.MINUTE));
                                    dateParm.setHours(cal.get(Calendar.HOUR_OF_DAY));
                                } else {
                                    final Calendar cal = Calendar.getInstance();
                                    cal.set(Calendar.HOUR_OF_DAY, 0);
                                    cal.set(Calendar.MINUTE, 0);
                                    cal.set(Calendar.SECOND, 0);
                                    cal.set(Calendar.MILLISECOND, 0);
                                    dateParm.setDate(cal.getTime());
                                }
                                dateParms.add(dateParm);
                                continue;
                            }
                            throw new ReportException("Unsupported report parameter type " + reportParm.getValueClassName());
                        }
                    }
                    return reportParameters; }
            });
        } catch (final Exception e) {
            if (e instanceof ReportException) throw (ReportException)e;
            throw new ReportException(e);
        }
    }

    /**
* {@inheritDoc}
*/
    @Override
    public void render(final String reportId, final String location, final ReportFormat format, final OutputStream outputStream) throws ReportException {
        try {
            Logging.withPrefix(LOG4J_CATEGORY, new Callable<Void>() {
                @Override public Void call() throws Exception {
                    try {
                        final JasperPrint jasperPrint = getJasperPrint(location);

                        switch (format) {
                        case PDF:
                            LOG.debug("rendering as PDF");
                            exportReportToPdf(jasperPrint, outputStream);
                            break;

                        case CSV:
                            LOG.debug("rendering as CSV");
                            exportReportToCsv(jasperPrint, outputStream);
                            break;

                        default:
                            LOG.debug("rendering as PDF as no valid format found");
                            exportReportToPdf(jasperPrint, outputStream);
                        }
                    } catch (final Exception e) {
                        LOG.error("Unable to render report {}", reportId, e);
                        throw new ReportException("Unable to render report " + reportId, e);
                    }

                    return null;
                }
            });
        } catch (final Exception e) {
            if (e instanceof ReportException) throw (ReportException)e;
            throw new ReportException(e);
        }
    }

    private JasperPrint getJasperPrint(String location) throws JRException {
        if (location.contains("jrpxml")) {
            return JRPrintXmlLoader.load(location);
        } else {
            return (JasperPrint) JRLoader.loadObject(location);
        }
    }

    /**
* {@inheritDoc}
*/
    @Override
    public String run(final HashMap<String, Object> reportParms, final String reportId) throws ReportException {
        try {
            return Logging.withPrefix(LOG4J_CATEGORY, new Callable<String>() {
                @Override public String call() throws Exception {
                    final String baseDir = System.getProperty("opennms.report.dir");
                    JasperReport jasperReport = null;

                    final DBUtils db = new DBUtils();

                    try {
                        jasperReport = JasperCompileManager.compileReport(m_globalReportRepository.getTemplateStream(reportId));
                    } catch (JRException e) {
                        LOG.error("Unable to compile jasper report {}", reportId, e);
                        throw new ReportException("Unable to compile jasperReport " + reportId, e);
                    }

                    final HashMap<String, Object> jrReportParms = buildJRparameters(reportParms, jasperReport.getParameters());

                    // Find sub reports and provide sub reports as parameter
                    jrReportParms.putAll(buildSubreport(reportId, jasperReport));

                    final String outputFileName = new String(baseDir + "/" + jasperReport.getName() + new SimpleDateFormat("-MMddyyyy-HHmm").format(new Date()) + ".jrprint");
                    LOG.debug("jrprint output file: {}", outputFileName);

                    try {
                        if ("jdbc".equalsIgnoreCase(m_globalReportRepository.getEngine(reportId))) {
                            try {
                                final Connection connection = DataSourceFactory.getDataSource().getConnection();
                                db.watch(connection);
                                JasperFillManager.fillReportToFile(jasperReport, outputFileName, reportParms, connection);
                            } finally {
                                db.cleanUp();
                            }
                        } else if (m_globalReportRepository.getEngine(reportId).equals("null")) {
                            JasperFillManager.fillReportToFile(jasperReport, outputFileName, reportParms, new JREmptyDataSource());
                        } else {
                            throw new ReportException("No suitable datasource configured for report " + reportId);
                        }
                    } catch (final Exception e) {
                        LOG.warn("Failed to run report " + reportId, e);
                        if (e instanceof ReportException) throw (ReportException)e;
                        throw new ReportException(e);
                    }
                    return outputFileName;
                }
            });
        } catch (final Exception e) {
            if (e instanceof ReportException) throw (ReportException)e;
            throw new ReportException("Failed to run Jasper report " + reportId, e);
        }

    }


    /**
* Method to find all sub reports as parameter. Compile sub reports and put all compile sub reports in a parameter map.
* Returned map is compatible to common jasper report parameter map.
*
* @param mainReportId String for specific main report identified by a report id
* @param mainReport JasperReport a compiled main report
* @return a sub report parameter map as {@link java.util.HashMap<String,Object>} object
*/
    private HashMap<String, Object> buildSubreport(final String mainReportId, final JasperReport mainReport) {
        String repositoryId = mainReportId.substring(0, mainReportId.indexOf("_"));
        HashMap<String, Object> subreportMap = new HashMap<String, Object>();

        // Filter parameter for sub reports
        for (JRParameter parameter : mainReport.getParameters()) {
            // We need only parameter for Sub reports and we *DON'T* need the default parameter JASPER_REPORT
            if ("net.sf.jasperreports.engine.JasperReport".equals(parameter.getValueClassName()) && !"JASPER_REPORT".equals(parameter.getName())) {
                subreportMap.put(parameter.getName(), parameter.getValueClassName());
            }
        }

        for (final Map.Entry<String,Object> entry : subreportMap.entrySet()) {
            final String reportId = repositoryId + "_" + entry.getKey();
            try {
                entry.setValue(JasperCompileManager.compileReport(m_globalReportRepository.getTemplateStream(reportId)));
            } catch (final JRException e) {
                LOG.debug("failed to compile report {}", reportId, e);
            }
        }

        for (final Map.Entry<String,Object> entry : subreportMap.entrySet()) {
            LOG.debug("Key: {} - Value: {}", entry.getKey(), entry.getValue());
        }
        return subreportMap;
    }

    /**
* {@inheritDoc}
*/
    @Override
    public void runAndRender(final HashMap<String, Object> reportParms, final String reportId, final ReportFormat format, final OutputStream outputStream) throws ReportException {
        try {
            Logging.withPrefix(LOG4J_CATEGORY, new Callable<Void>() {
                @Override public Void call() throws Exception {
                    JasperReport jasperReport = null;

                    try {
                        jasperReport = JasperCompileManager.compileReport(m_globalReportRepository.getTemplateStream(reportId));
                    } catch (final JRException e) {
                        LOG.error("unable to compile jasper report", e);
                        throw new ReportException("unable to compile jasperReport", e);
                    }

                    final HashMap<String, Object> jrReportParms = buildJRparameters(reportParms, jasperReport.getParameters());
                    jrReportParms.putAll(buildSubreport(reportId, jasperReport));

                    if ("jdbc".equalsIgnoreCase(m_globalReportRepository.getEngine(reportId))) {
                        final DBUtils db = new DBUtils();
                        try {
                            final Connection connection = DataSourceFactory.getDataSource().getConnection();
                            db.watch(connection);

                            final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jrReportParms, connection);
                            exportReport(format, jasperPrint, outputStream);
                        } finally {
                            db.cleanUp();
                        }
                    } else if ("null".equalsIgnoreCase(m_globalReportRepository.getEngine(reportId))) {
                        final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jrReportParms, new JREmptyDataSource());
                        exportReport(format, jasperPrint, outputStream);

                    }

                    return null;
                }
            });
        } catch (final Exception e) {
            if (e instanceof ReportException) throw (ReportException)e;
            throw new ReportException("Failed to run Jasper report " + reportId, e);
        }

    }

    private void exportReport(ReportFormat format, JasperPrint jasperPrint,
            OutputStream outputStream) throws JRException {
        switch (format) {
        case PDF:
            exportReportToPdf(jasperPrint, outputStream);
            break;

        case CSV:
            exportReportToCsv(jasperPrint, outputStream);
            break;

        default:
            break;
        }

    }

    private void exportReportToPdf(final JasperPrint jasperPrint, final OutputStream outputStream) throws JRException {
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
    }

    private void exportReportToCsv(final JasperPrint jasperPrint, final OutputStream outputStream) throws JRException {
        JRCsvExporter exporter = new JRCsvExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);

        exporter.exportReport();
    }
    
    private void exportReportToXls(JasperPrint jasperPrint,
            OutputStream outputStream) throws JRException {
                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
                exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
                exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
                exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, Boolean.TRUE);
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
                
                exporter.exportReport();
        }

    private void exportReportToHtml(JasperPrint jasperPrint,
            OutputStream outputStream) throws JRException {
                JRHtmlExporter exporter = new JRHtmlExporter();
                exporter.setParameter(JRHtmlExporterParameter.BETWEEN_PAGES_HTML,"");
                exporter.setParameter(JRHtmlExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,Boolean.TRUE);
                exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN,Boolean.FALSE);
                exporter.setParameter(JRHtmlExporterParameter.IMAGES_MAP,new HashMap());
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
                
                exporter.exportReport();
        }
    

    private HashMap<String, Object> buildJRparameters(final HashMap<String, Object> onmsReportParms, final JRParameter[] reportParms) throws ReportException {
        final HashMap<String, Object> jrReportParms = new HashMap<String, Object>();

        for (final JRParameter reportParm : reportParms) {
            LOG.debug("found report parm {} of class {}", reportParm.getValueClassName(), reportParm.getName());
            if (reportParm.isSystemDefined() == false) {
                final String parmName = reportParm.getName();

                if (reportParm.isForPrompting() == false) {
                    LOG.debug("Required parameter {} is not for prompting - continuing", parmName);
                    continue;
                }

                if (onmsReportParms.containsKey(parmName) == false) {
                    throw new ReportException("Required parameter " + parmName + " not supplied to JasperReports by OpenNMS");
                }

                if (reportParm.getValueClassName().equals("java.lang.String")) {
                    jrReportParms.put(parmName, (String)onmsReportParms.get(parmName));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.lang.Integer")) {
                    jrReportParms.put(parmName, (Integer) onmsReportParms.get(parmName));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.lang.Float")) {
                    jrReportParms.put(parmName, (Float) onmsReportParms.get(parmName));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.lang.Double")) {
                    jrReportParms.put(parmName, (Double) onmsReportParms.get(parmName));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.util.Date")) {
                    jrReportParms.put(parmName, (Date)onmsReportParms.get(parmName));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.sql.Date")) {
                    final Date date = (Date)onmsReportParms.get(parmName);
                    jrReportParms.put(parmName, new java.sql.Date(date.getTime()));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.sql.Timestamp")) {
                    final Date date = (Date)onmsReportParms.get(parmName);
                    jrReportParms.put(parmName, new java.sql.Timestamp(date.getTime()));
                    continue;
                }

                throw new ReportException("Unsupported report parameter type " + reportParm.getValueClassName());
            }
        }

        return jrReportParms;

    }

    /**
* {@inheritDoc}
*/
    @Override
    public boolean validate(final HashMap<String, Object> reportParms, final String reportId) {
        // returns true until we can take parameters
        return true;
    }

    /**
* {@inheritDoc}
*/
	@Override
	public void runAndRender(List<Integer> eventIds, String reportId,
			ReportFormat format,String fileName,String dirName)
			throws ReportException {

		LOG.info("Enter into the rundAndRender action for event report");
    	// Get the event report details
        ArrayList<EventReportStructure> eventReportList = new ArrayList<EventReportStructure>();
        eventReportList = getEventReportList(eventIds,format);
		
        JasperReport jasperReport = null;
        JasperPrint jasperPrint = null;
        HashMap<String, Object> reportParms = new HashMap<String, Object>();
        try {
        	JasperDesign jasperDesign = JRXmlLoader.load(m_globalReportRepository.getTemplateStream(reportId));
            jasperReport = JasperCompileManager.compileReport(jasperDesign);
        } catch (JRException e) {
            LOG.error("unable to compile jasper report", e);
            throw new ReportException("unable to compile jasperReport", e);
        }
		
       
        
        // Create the event report folder if it's not exist already
        String baseDir = System.getProperty("opennms.report.dir")+"/event";
        File eventReportfolder = new File(baseDir);  
		if (!eventReportfolder.exists()){  
			if(eventReportfolder.mkdir()){
				LOG.debug("The event report folder is successfully created in "+baseDir+" location");
			} else {
				LOG.debug("unable to create the event report folder in "+baseDir+" location");
			}
		}else{  
			LOG.debug("The event report folder is already exist in server location");
		}
		
		// Store the event report into the local server
		String outputFileName = null;
		if(dirName != null)
			outputFileName = new String(baseDir + "/" + dirName+"/"+fileName);
		else
			outputFileName = new String(baseDir + "/"+fileName);
 		OutputStream outputReportStream = null;
		try{
			JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(eventReportList);
     		JRGzipVirtualizer virtualizer = new JRGzipVirtualizer(10240);
     		reportParms.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
     		jasperPrint = JasperFillManager.fillReport(jasperReport,reportParms,beanColDataSource);
     		
			outputReportStream = new FileOutputStream (outputFileName);
			LOG.info("Starting event export action for file " + outputFileName);
			if(ReportFormat.PDF == format || ReportFormat.CSV == format ){
     			exportReport(format, jasperPrint, outputReportStream);
     		} else if(ReportFormat.HTML == format) {
     			exportReportToHtml(jasperPrint,outputReportStream);
     		} else if(ReportFormat.XLS == format){
     			exportReportToXls(jasperPrint,outputReportStream);
     		} else {
     			LOG.error("Unknown file format : " + format);
     		}
			LOG.info("The event report "+ outputFileName  + " is successfully stored in the local server");
		} catch(JRException e){
			LOG.error("jasper report exception ", e);
		} catch (FileNotFoundException e) {
			LOG.error("unable to find the server location ", e);
		}finally{
			if(outputReportStream != null){
				try {
					outputReportStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		LOG.info("Terminated from the rundAndRender action for event report");
	}
	
	public ArrayList<EventReportStructure> getEventReportList(List<Integer> eventIds,ReportFormat format) throws ReportException{
    	
		// Date format for an alarm events
	    SimpleDateFormat formater = new SimpleDateFormat("MM/dd/yy hh:mm:ss aaa",Locale.ENGLISH);
	    
	    ArrayList<EventReportStructure> eventReportList = new ArrayList<EventReportStructure>();
	   
	    if(ReportFormat.CSV ==format)
			for(Integer eventId : eventIds){
				
				// Get the events by it's id's
				OnmsEvent onmsEvent = m_eventDao.get(eventId);
				eventReportList.add(getEventReportStructureforCSV(onmsEvent));
			}
	    else {
			for(Integer eventId : eventIds){
				
				// Get the events by it's id's
				OnmsEvent onmsEvent = m_eventDao.get(eventId);
		 		if(onmsEvent != null)
				eventReportList.add(getEventReportStructure(onmsEvent));
			}
	    }
		return eventReportList;
    }

	 public EventReportStructure getEventReportStructure(OnmsEvent onmsEvent) throws ReportException{
	    	
	    	// Date format for an events
		 SimpleDateFormat formater = new SimpleDateFormat("MM/dd/yy hh:mm:ss aaa",Locale.ENGLISH);
		 try{		    
		 EventReportStructure eventJasperReportStructure = new EventReportStructure();
		 
		 try{
			 if(onmsEvent.getNodeLabel() != null){
				 eventJasperReportStructure.setNodeLabel(onmsEvent.getNodeLabel());
			 }else{
				 eventJasperReportStructure.setNodeLabel(null);
			 }
		 } catch(Exception e){
			 if(onmsEvent.getNode()!= null){
				 eventJasperReportStructure.setNodeLabel(m_nodeDao.getLabelForId(onmsEvent.getNode().getId()));
 			} else {
 				eventJasperReportStructure.setNodeLabel(null);
 			}
		 }
		 
		 if(onmsEvent != null)
		 eventJasperReportStructure.setEventId(onmsEvent.getId());
		 OnmsAlarm onmsAlarm = onmsEvent.getAlarm();
		 if(onmsAlarm != null)
			 eventJasperReportStructure.setAlarmId(onmsAlarm.getId());
		 eventJasperReportStructure.setEventUEI(onmsEvent.getEventUei());
		 eventJasperReportStructure.setEventLogMsg(onmsEvent.getEventLogMsg());
		 if(onmsEvent.getEventCreateTime()!=null)
		 eventJasperReportStructure.setCreateTime(String.valueOf(formater.format(onmsEvent.getEventCreateTime())));
		 return eventJasperReportStructure;
		 }
		
		 catch(Exception e)
		 {
			 LOG.error("Error in EventReportStructure report", e);
	            throw new ReportException("Error in EventReportStructure report", e);
		 }
		 
	 }
	
	
	

         public EventReportStructure getEventReportStructureforCSV(OnmsEvent onmsEvent) throws ReportException{
                 
                 // Date format for an events
                 SimpleDateFormat formater = new SimpleDateFormat("MM/dd/yy hh:mm:ss aaa",Locale.ENGLISH);
                 try{
                                                          
                 EventReportStructure eventJasperReportStructure = new EventReportStructure();
                
                 try{
                         if(onmsEvent.getNodeLabel() != null){
                                 eventJasperReportStructure.setNodeLabel(onmsEvent.getNodeLabel());
                         }else{
                                 eventJasperReportStructure.setNodeLabel(null);
                         }
                 } catch(Exception e){
                         if(onmsEvent.getNode()!= null){
				 			eventJasperReportStructure.setNodeLabel(m_nodeDao.getLabelForId(onmsEvent.getNode().getId()));
 						} else {
 						eventJasperReportStructure.setNodeLabel(null);
 						}
                 }
                 if(onmsEvent != null)
                 eventJasperReportStructure.setEventId(onmsEvent.getId());
                 OnmsAlarm onmsAlarm = onmsEvent.getAlarm();
                 if(onmsAlarm != null)
                         eventJasperReportStructure.setAlarmId(onmsAlarm.getId());
                 eventJasperReportStructure.setEventUEI(onmsEvent.getEventUei());
                
                 try{
                         if(onmsEvent.getNode()!=null){
                                 eventJasperReportStructure.setNodeId(onmsEvent.getNode().getId());
                         }
                         else{
                                 eventJasperReportStructure.setNodeId(null);
                         }
                 } catch(Exception e){
                         eventJasperReportStructure.setNodeId(null);
                 }
                
                 if(onmsEvent.getEventTime()!=null)
                 eventJasperReportStructure.setEventTime(String.valueOf(formater.format(onmsEvent.getEventTime())));
                 eventJasperReportStructure.setEventHost(onmsEvent.getEventHost());
                 eventJasperReportStructure.setEventSource(onmsEvent.getEventSource());
                 if(onmsEvent.getIpAddr()!=null)
                 eventJasperReportStructure.setIpAddr(onmsEvent.getIpAddr().getHostAddress());
                 if(onmsEvent.getDistPoller()!=null)
                 eventJasperReportStructure.setEventDpName(onmsEvent.getDistPoller().getName());//needto confirm
         eventJasperReportStructure.setEventSnmpHost(onmsEvent.getEventSnmpHost());
         if(onmsEvent.getServiceType() !=null)
         eventJasperReportStructure.setServiceID(onmsEvent.getServiceType().getId());//need to clarify
         eventJasperReportStructure.setEventSnmp(onmsEvent.getEventSnmp());
         eventJasperReportStructure.setEventParms(onmsEvent.getEventParms());
         if(onmsEvent.getEventCreateTime()!=null)
         eventJasperReportStructure.setEventCreateTime(String.valueOf(formater.format(onmsEvent.getEventCreateTime())));
         eventJasperReportStructure.setEventDescr(onmsEvent.getEventDescr());
         eventJasperReportStructure.setEventLogGroup(onmsEvent.getEventLogGroup());
         eventJasperReportStructure.setEventLogMsg(onmsEvent.getEventLogMsg());
         eventJasperReportStructure.setEventSeverity(onmsEvent.getSeverityLabel().toString());
         eventJasperReportStructure.setEventPathOutage(onmsEvent.getEventPathOutage());
         eventJasperReportStructure.setEventCorrelation(onmsEvent.getEventCorrelation());
         eventJasperReportStructure.setEventSuppressedCount(onmsEvent.getEventSuppressedCount());
         eventJasperReportStructure.setEventOperInstruct(onmsEvent.getEventOperInstruct());
         eventJasperReportStructure.setEventAutoAction(onmsEvent.getEventAutoAction());
         eventJasperReportStructure.setEventOperAction(onmsEvent.getEventOperAction());
         eventJasperReportStructure.setEventOperActionMenuText(onmsEvent.getEventOperActionMenuText());
         eventJasperReportStructure.setEventNotification(onmsEvent.getEventNotification());
         eventJasperReportStructure.setEventTTicket(onmsEvent.getEventTTicket());
         eventJasperReportStructure.setEventTTicketState(onmsEvent.getEventTTicketState());
         eventJasperReportStructure.setEventForward(onmsEvent.getEventForward());
         eventJasperReportStructure.setEventMouseOverText(onmsEvent.getEventMouseOverText());
         eventJasperReportStructure.setEventLog(onmsEvent.getEventLog());
         eventJasperReportStructure.setEventDisplay(onmsEvent.getEventDisplay());
         eventJasperReportStructure.setEventAckUser(onmsEvent.getEventAckUser());
         if(onmsEvent.getEventAckTime()!=null)
         eventJasperReportStructure.setEventAckTime(String.valueOf(formater.format(onmsEvent.getEventAckTime())));        
         eventJasperReportStructure.setIfIndex(onmsEvent.getIfIndex());
         return eventJasperReportStructure;
                 }
                
                 catch(Exception e)
                 {
                         LOG.error("Error in EventReportStructure CSV report", e);
         throw new ReportException("Error in EventReportStructure CSV report ", e);
                        
                 }
         }
        
        public void setEventDao(EventDao m_eventDao) {
                this.m_eventDao = m_eventDao;
        }

    public void runAndRender(List<Integer> alarmIds,HashMap<Integer, List<Integer>> eventIdsForAlarms ,
    		String reportId, ReportFormat format, String fileName, String folderName) throws ReportException {
    	
    	LOG.info("Enter the runAndRender action to generate alarm report for the alarm list "+alarmIds+"and event list "+eventIdsForAlarms);
    	
    	JRBeanCollectionDataSource beanColDataSource = null;
    	JasperReport jasperReport = null;
        JasperPrint jasperPrint = null;
        HashMap<String, Object> reportParms = new HashMap<String, Object>();
    	
    	// Get the alarm report details
    	if(ReportFormat.CSV == format){
    		ArrayList<AlarmReportStructure> alarmReportList = getAlarmReportListForCSV(alarmIds, eventIdsForAlarms, format);
    		beanColDataSource = new JRBeanCollectionDataSource(alarmReportList);
    	} else {
    		ArrayList<AlarmReportBean> alarmReportList = getAlarmReportList(alarmIds, eventIdsForAlarms, format);
    		beanColDataSource = new JRBeanCollectionDataSource(alarmReportList);
    	}
        
        try {
        	JasperDesign jasperDesign = JRXmlLoader.load(m_globalReportRepository.getTemplateStream(reportId));
        	jasperReport = JasperCompileManager.compileReport(jasperDesign);
        } catch (JRException e) {
            LOG.error("unable to compile alarm jasper report", e);
            throw new ReportException("unable to compile alarm jasperReport", e);
        }
        
        if(!(ReportFormat.CSV == format)){
	        try {
	            JasperDesign jasperSubReportDesign = JRXmlLoader.load(m_globalReportRepository.getTemplateStream("local_alarm-subreport"));
	            JasperReport jasperSubReport = JasperCompileManager.compileReport(jasperSubReportDesign);
	            reportParms.put("subreportParameter", jasperSubReport);
	        } catch (JRException e) {
	            LOG.error("unable to compile alarm jasper subreport", e);
	            throw new ReportException("unable to compile alarm jasperReport", e);
	        }
        }
        
        String reportDir = System.getProperty("opennms.report.dir");
        String alarmReportDir = System.getProperty("opennms.alarm.report.dir");
        File alarmReportfolder = new File(alarmReportDir);
        
        // Create the alarm report folder if it's not exist already
        if (!alarmReportfolder.exists()){
        	alarmReportDir = reportDir+"/alarm";
        	alarmReportfolder = new File(alarmReportDir);
        	if(alarmReportfolder.mkdir()){
        		 LOG.debug("The alarm report folder is successfully created in the "+reportDir+" location");
			} else {
				 LOG.error("Unable to creat the alarm report folder in the "+reportDir+" location");
			}
        }
		
        // Create the alarm sub folder
        String alarmReportSubDir = alarmReportDir+"/"+folderName;
        if (alarmReportfolder.exists()){
            File alarmSubFolder = new File(alarmReportSubDir);
            if(!alarmSubFolder.exists()){
	            if(alarmSubFolder.mkdir()){
	       		 LOG.debug("The alarm sub folder is successfully created in the "+alarmReportfolder+" location");
				} else {
					 LOG.error("Unable to creat the alarm sub folder in the "+alarmReportfolder+" location");
				}
            }
        } else {
        	LOG.error("The alarm folder is not available in the "+reportDir+" location");
        }
        
		// To Store the alarm report in the local server
 		String outputFileName = new String(alarmReportSubDir + "/" + fileName);
 		OutputStream outputReportStream = null;
		try{
			LOG.info("The alarm report is currently exporting...");
			
     		JRGzipVirtualizer virtualizer = new JRGzipVirtualizer(10240);
     		reportParms.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
     		jasperPrint = JasperFillManager.fillReport(jasperReport,reportParms,beanColDataSource);
			
     		outputReportStream = new FileOutputStream (outputFileName);
			if(ReportFormat.PDF == format || ReportFormat.CSV == format ){
     			exportReport(format, jasperPrint, outputReportStream);
     			LOG.info("The alarm report is successfully stored in the local server");
     		} else if(ReportFormat.HTML == format) {
     			exportReportToHtml(jasperPrint,outputReportStream);
     			LOG.info("The alarm report is successfully stored in the local server");
     		} else {
     			LOG.error("Unknown file format : " + format);
     		}
		} catch(JRException e){
			LOG.error("jasper report exception ", e);
		} catch (FileNotFoundException e) {
			LOG.error("unable to find the server location ", e);
		} finally {
			if(outputReportStream != null){
				try {
					outputReportStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOG.error("Unable to close output report stream in jasper report service", e);
				}
			}
		}
		LOG.info("Terminated the runAndRender action after exported the alarm report for the alarm list "+alarmIds+"and event list "+eventIdsForAlarms);
    }
    
    public ArrayList<AlarmReportBean> getAlarmReportList(List<Integer> alarmIds, HashMap<Integer, List<Integer>> eventIdsForAlarms, ReportFormat format){
    	
	    ArrayList<AlarmReportBean> onmsAlarmListForSubReport = new ArrayList<AlarmReportBean>();
	    try{
			for(Integer alarmId : alarmIds){
				
				// Get the alarm and events by it's id's
				OnmsAlarm onmsAlarm = m_alarmDao.get(alarmId);
				List<OnmsEvent> onmsEventList = getEvents(eventIdsForAlarms, alarmId);				
				ArrayList<AlarmReportStructure> onmsEventListForSubReport = new ArrayList<AlarmReportStructure>();
				AlarmReportBean alarmWithEvent = new AlarmReportBean();
				for(int eventIterator = 0; eventIterator < onmsEventList.size() ; eventIterator++){
					onmsEventListForSubReport = getEventListForAlarm(onmsEventListForSubReport, onmsEventList, onmsAlarm, format, eventIterator);
				}
	
				alarmWithEvent.setAlarmReportStructure(onmsEventListForSubReport);
				onmsAlarmListForSubReport.add(alarmWithEvent);
			}
	    } catch(Exception ex) {
	    	ex.printStackTrace();
    		LOG.error("unable to get alarm report list for pdf/html format in getAlarmReportList method ", ex);
	    }
		return onmsAlarmListForSubReport;
    }
    
	public ArrayList<AlarmReportStructure> getAlarmReportListForCSV(List<Integer> alarmIds, HashMap<Integer, List<Integer>> eventIdsForAlarms, ReportFormat format){
		
	    ArrayList<AlarmReportStructure> alarmReportList = new ArrayList<AlarmReportStructure>();
	    try{
			for(Integer alarmId : alarmIds){
				
				// Get the alarm and events by it's id's
				OnmsAlarm onmsAlarm = m_alarmDao.get(alarmId);
				List<OnmsEvent> onmsEventList = getEvents(eventIdsForAlarms, alarmId);			
				
				for(int eventIterator = 0; eventIterator < onmsEventList.size() ; eventIterator++){
					alarmReportList = getEventListForAlarm(alarmReportList, onmsEventList, onmsAlarm, format, eventIterator);
				}
			}
	    } catch(Exception ex) {
	    	ex.printStackTrace();
    		LOG.error("unable to get alarm report list for csv format in getAlarmReportListForCSV method ", ex);
	    }
		return alarmReportList;
	}

    public ArrayList<AlarmReportStructure> getEventListForAlarm(ArrayList<AlarmReportStructure> alarmReportList, List<OnmsEvent> onmsEventList, OnmsAlarm onmsAlarm, ReportFormat format, int eventIterator){
    	
		OnmsEvent currOnmsEvent = onmsEventList.get(eventIterator);
		try{
			if(currOnmsEvent.getAlarm() != null){
				if(currOnmsEvent.getAlarm().getId()!= null && currOnmsEvent.getAlarm().getId()>0){
				
	    			Calendar eventCreatTime = null;
					try {
						eventCreatTime = this.getDateFormat(formater.parse(formater.format(currOnmsEvent.getEventCreateTime())));
					} catch (ParseException e) {
						e.printStackTrace();
					}
	    			
	    			// Find the duplicate alarm Id
					boolean	isAlarmsWithSameId = false;
					if(eventIterator>0) {
						isAlarmsWithSameId = getDuplicateIdStatus(onmsEventList.get(eventIterator-1), currOnmsEvent.getAlarm().getId(),eventIterator);
					}
					
					// Get the acknowledgment by it's id
					List<OnmsAcknowledgment> onmsAcknowledgmentList = getAcknowledgments(currOnmsEvent.getAlarm().getId());
					if(onmsAcknowledgmentList.size()>0){
						
						boolean isEmptyAcknowledgment = true;
						String[] getAckStatus = new String[3]; 
						int ackCount = 0;
						
						if(isAlarmsWithSameId){
							OnmsEvent preOnmsEvent = onmsEventList.get(eventIterator-1);
							Calendar preEventCreatTime = null;
							try {
								preEventCreatTime = this.getDateFormat(formater.parse(formater.format(preOnmsEvent.getEventCreateTime())));
							} catch (ParseException e) {
								e.printStackTrace();
							}
							
							for(OnmsAcknowledgment onmsAcknowledgment : onmsAcknowledgmentList){
								Calendar ackTime = null;
								try {
									ackTime = this.getDateFormat(formater.parse(formater.format(onmsAcknowledgment.getAckTime())));
								} catch (ParseException e) {
									e.printStackTrace();
								}
								
								//Comparison of event creation time with acknowledgment time
								if((((eventCreatTime.compareTo(ackTime)) < 0) && ((preEventCreatTime.compareTo(ackTime)) > 0))){
									if(ackCount == 0){
										getAckStatus[0] = String.valueOf(formater.format(onmsAcknowledgment.getAckTime()));
										getAckStatus[1] = "\n"+onmsAcknowledgment.getAckUser();
										getAckStatus[2] = "\n"+String.valueOf(onmsAcknowledgment.getAckAction());
										ackCount++;
									} else {
										getAckStatus[0] = getAckStatus[0] +"\n"+ String.valueOf(formater.format(onmsAcknowledgment.getAckTime()));
										getAckStatus[1] = getAckStatus[1] +"\n\n"+ onmsAcknowledgment.getAckUser();
										getAckStatus[2] = getAckStatus[2] +"\n\n"+ String.valueOf(onmsAcknowledgment.getAckAction());
									}
				        			isEmptyAcknowledgment = false;
								}
							}
						} else {
							
							for(OnmsAcknowledgment onmsAcknowledgment : onmsAcknowledgmentList){
								Calendar ackTime = null;
								try {
									ackTime = this.getDateFormat(formater.parse(formater.format(onmsAcknowledgment.getAckTime())));
								} catch (ParseException e) {
									e.printStackTrace();
								}
								//Comparison of event creation time with acknowledgment time
								if((eventCreatTime.compareTo(ackTime)) < 0){
									if(ackCount == 0){
										getAckStatus[0] = String.valueOf(formater.format(onmsAcknowledgment.getAckTime()));
										getAckStatus[1] = "\n"+onmsAcknowledgment.getAckUser();
										getAckStatus[2] = "\n"+String.valueOf(onmsAcknowledgment.getAckAction());
										ackCount++;
									} else {
										getAckStatus[0] = getAckStatus[0] +"\n"+ String.valueOf(formater.format(onmsAcknowledgment.getAckTime()));
										getAckStatus[1] = getAckStatus[1] +"\n\n"+ onmsAcknowledgment.getAckUser();
										getAckStatus[2] = getAckStatus[2] +"\n\n"+ String.valueOf(onmsAcknowledgment.getAckAction());
									}
				        			isEmptyAcknowledgment = false;
								}
			    			}
						}
						if(isEmptyAcknowledgment){
							alarmReportList.add(getAlarmWithEventValues(onmsAlarm, currOnmsEvent, null,format));
						} else {
							alarmReportList.add(getAlarmWithEventValues(onmsAlarm, currOnmsEvent, getAckStatus, format));
						}
					} else {
						alarmReportList.add(getAlarmWithEventValues(onmsAlarm, currOnmsEvent, null, format));
					}
				} else {
					alarmReportList.add(getAlarmWithEventValues(onmsAlarm, currOnmsEvent, null, format));
				}
			} else {
				alarmReportList.add(getAlarmWithEventValues(onmsAlarm, currOnmsEvent, null, format));
			}
		} catch(Exception ex) {
			ex.printStackTrace();
    		LOG.error("unable to get the event list for an alarm in getEventListForAlarm method ", ex);
		}
		return alarmReportList;
    }
    
    public AlarmReportStructure getAlarmWithEventValues(OnmsAlarm onmsAlarm, OnmsEvent onmsEvent, String[] ackStatus, ReportFormat format){
    	
    	AlarmReportStructure alarmJasperReportStructure = new AlarmReportStructure();
    	try {
    		
    		if(onmsAlarm.getId()!= null){
    			alarmJasperReportStructure.setAlarmId(onmsAlarm.getId());
    		} else {
    			alarmJasperReportStructure.setAlarmId(null);
    		}
    		
    		try {
	    		if(onmsAlarm.getNodeLabel()!= null) {
	    			alarmJasperReportStructure.setNodeLabel(onmsAlarm.getNodeLabel());
	    		} else {
	    			alarmJasperReportStructure.setNodeLabel(null);
	    		} 
    		} catch(Exception ex) {
    			if(onmsAlarm.getNode()!= null){
    				alarmJasperReportStructure.setNodeLabel(m_nodeDao.getLabelForId(onmsAlarm.getNode().getId()));
    			} else {
    				alarmJasperReportStructure.setNodeLabel(null);
    			}
    		}
    		
			alarmJasperReportStructure.setEventId(onmsEvent.getId());
			alarmJasperReportStructure.setEventLogMsg(onmsEvent.getEventLogMsg());
			alarmJasperReportStructure.setEventSeverity(onmsEvent.getSeverityLabel());
			alarmJasperReportStructure.setIpAddr(InetAddressUtils.str(onmsAlarm.getIpAddr()));
			
	    	if(onmsEvent.getAlarm()!= null){
				if(onmsEvent.getAlarm().getId() != 0){
					alarmJasperReportStructure.setEventAlarmId(onmsEvent.getAlarm().getId());
				} else{
					alarmJasperReportStructure.setEventAlarmId(null);
				}
			} else {
				alarmJasperReportStructure.setEventAlarmId(null);
			}
			
			if(ackStatus != null){
				alarmJasperReportStructure.setAckTime(ackStatus[0]);
				alarmJasperReportStructure.setAckUser(ackStatus[1]);
				alarmJasperReportStructure.setAckAction(ackStatus[2]);
			} else {
				alarmJasperReportStructure.setAckTime(null);
				alarmJasperReportStructure.setAckUser(null);
				alarmJasperReportStructure.setAckAction(null);
			}
			
			if(onmsEvent.getEventCreateTime() != null){
				alarmJasperReportStructure.setEventCreateTime(String.valueOf(formater.format(onmsEvent.getEventCreateTime())));
			} else {
				alarmJasperReportStructure.setEventCreateTime(null);
			}
			
			if(ReportFormat.CSV == format){
				getAlarmWithEventValuesForCSV(alarmJasperReportStructure, onmsAlarm, onmsEvent);
			}
			
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		LOG.error("unable to set the alarm or event values in getAlarmWithEventValues method ", ex);
    	}
    	return alarmJasperReportStructure;
    }
    
    public void getAlarmWithEventValuesForCSV(AlarmReportStructure alarmJasperReportStructure, OnmsAlarm onmsAlarm, OnmsEvent onmsEvent){
    	
    	try{
			if(onmsAlarm.getDistPoller() != null){
				alarmJasperReportStructure.setDpName(onmsAlarm.getDistPoller().getName());
			} else {
				alarmJasperReportStructure.setDpName(null);
			}
			
			if(onmsAlarm.getServiceType() != null){
				alarmJasperReportStructure.setServiceID(onmsAlarm.getServiceType().getId());
			} else {
				alarmJasperReportStructure.setServiceID(null);
			}
			
			if(onmsAlarm.getLastEvent() != null){
				alarmJasperReportStructure.setLastEventId(onmsAlarm.getLastEvent().getId());
			} else {
				alarmJasperReportStructure.setLastEventId(null);
			}
			
			if(onmsAlarm.getFirstAutomationTime() != null){
				alarmJasperReportStructure.setFirstAutomationTime(String.valueOf(formater.format(onmsAlarm.getFirstAutomationTime())));
			} else {
				alarmJasperReportStructure.setFirstAutomationTime(null);
			}
			
			if(onmsAlarm.getLastAutomationTime() != null){
				alarmJasperReportStructure.setLastAutomationTime(String.valueOf(formater.format(onmsAlarm.getLastAutomationTime())));
			} else {
				alarmJasperReportStructure.setLastAutomationTime(null);
			}
			
			alarmJasperReportStructure.setNodeId(onmsAlarm.getNodeId());
			alarmJasperReportStructure.setReductionKey(onmsAlarm.getReductionKey());
			alarmJasperReportStructure.setAlarmType(onmsAlarm.getAlarmType());
			alarmJasperReportStructure.setCounter(onmsAlarm.getCounter());
			alarmJasperReportStructure.setSeverity(onmsAlarm.getSeverityLabel());
			alarmJasperReportStructure.setFirstEventTime(String.valueOf(formater.format(onmsAlarm.getFirstEventTime())));
			alarmJasperReportStructure.setLastEventTime(String.valueOf(formater.format(onmsAlarm.getLastEventTime())));
			alarmJasperReportStructure.setDescription(onmsAlarm.getDescription());
			alarmJasperReportStructure.setLogMsg(onmsAlarm.getLogMsg());
			alarmJasperReportStructure.setOperInstruct(onmsAlarm.getOperInstruct());
			alarmJasperReportStructure.settTicketId(onmsAlarm.getTTicketId());
			
			if(onmsAlarm.getTTicketState()!= null){
				alarmJasperReportStructure.settTicketState(String.valueOf(onmsAlarm.getTTicketState()));
			} else {
				alarmJasperReportStructure.settTicketState(null);
			}
			
			alarmJasperReportStructure.setMouseOverText(onmsAlarm.getMouseOverText());
			
			if(onmsAlarm.getSuppressedUntil()!= null){
				alarmJasperReportStructure.setSuppressedUntil(String.valueOf(formater.format(onmsAlarm.getSuppressedUntil())));
			} else {
				alarmJasperReportStructure.setSuppressedUntil(null);
			}
			
			if(onmsAlarm.getSuppressedTime() != null){
				alarmJasperReportStructure.setSuppressedTime(String.valueOf(formater.format(onmsAlarm.getSuppressedTime())));
			} else {
				alarmJasperReportStructure.setSuppressedTime(null);
			}
			
			if(onmsAlarm.getAlarmAckTime() != null){
				alarmJasperReportStructure.setAlarmAckTime(String.valueOf(formater.format(onmsAlarm.getAlarmAckTime())));
			} else {
				alarmJasperReportStructure.setAlarmAckTime(null);
			}
			
			alarmJasperReportStructure.setAlarmAckUser(onmsAlarm.getAlarmAckUser());
			alarmJasperReportStructure.setSuppressedUser(onmsAlarm.getSuppressedUser());
			alarmJasperReportStructure.setManagedObjectInstance(onmsAlarm.getManagedObjectInstance());
			alarmJasperReportStructure.setManagedObjectType(onmsAlarm.getManagedObjectType());
			alarmJasperReportStructure.setApplicationDN(onmsAlarm.getApplicationDN());
			alarmJasperReportStructure.setOssPrimaryKey(onmsAlarm.getOssPrimaryKey());
			alarmJasperReportStructure.setX733AlarmType(onmsAlarm.getX733AlarmType());
			alarmJasperReportStructure.setX733ProbableCause(onmsAlarm.getX733ProbableCause());
			alarmJasperReportStructure.setQosAlarmState(onmsAlarm.getQosAlarmState());
			alarmJasperReportStructure.setClearKey(onmsAlarm.getClearKey());
			alarmJasperReportStructure.setIfIndex(onmsAlarm.getIfIndex());
			alarmJasperReportStructure.setEventParms(onmsAlarm.getEventParms());
			alarmJasperReportStructure.setEventUEI(onmsEvent.getEventUei());
			
			if(onmsAlarm.getStickyMemo() != null){
				alarmJasperReportStructure.setStickyMemo(onmsAlarm.getStickyMemo().getBody());
			} else {
				alarmJasperReportStructure.setStickyMemo(null);
			}
			
			if(onmsEvent.getEventTime() != null){
				alarmJasperReportStructure.setEventTime(String.valueOf(formater.format(onmsEvent.getEventTime())));
			} else {
				alarmJasperReportStructure.setEventTime(null);
			}
			
			if(onmsEvent.getDistPoller() != null){
				alarmJasperReportStructure.setEventDbName(onmsEvent.getDistPoller().getName());
			} else {
				alarmJasperReportStructure.setEventDbName(null);
			}
			
			if(onmsEvent.getServiceType() != null){
				alarmJasperReportStructure.setEventServiceID(onmsEvent.getServiceType().getId());
			} else {
				alarmJasperReportStructure.setEventServiceID(null);
			}
			
			alarmJasperReportStructure.setEventHost(onmsEvent.getEventHost());
			alarmJasperReportStructure.setEventSource(onmsEvent.getEventSource());
			alarmJasperReportStructure.setEventSnmpHost(onmsEvent.getEventSnmpHost());
			alarmJasperReportStructure.setEventParms(onmsEvent.getEventParms());
			alarmJasperReportStructure.setEventDescr(onmsEvent.getEventDescr());
			alarmJasperReportStructure.setEventLogGroup(onmsEvent.getEventLogGroup());
			alarmJasperReportStructure.setEventLog(onmsEvent.getEventLog());
			alarmJasperReportStructure.setEventPathOutage(onmsEvent.getEventPathOutage());
			alarmJasperReportStructure.setEventCorrelation(onmsEvent.getEventCorrelation());
			alarmJasperReportStructure.setEventSuppressedCount(onmsEvent.getEventSuppressedCount());
			alarmJasperReportStructure.setEventOperInstruct(onmsEvent.getEventOperInstruct());
			alarmJasperReportStructure.setEventAutoAction(onmsEvent.getEventAutoAction());
			alarmJasperReportStructure.setEventOperAction(onmsEvent.getEventOperAction());
			alarmJasperReportStructure.setEventOperActionMenuText(onmsEvent.getEventOperActionMenuText());
			alarmJasperReportStructure.setEventNotification(onmsEvent.getEventNotification());
			alarmJasperReportStructure.setEventTTicket(onmsEvent.getEventTTicket());
			alarmJasperReportStructure.setEventTTicketState(onmsEvent.getEventTTicketState());
			alarmJasperReportStructure.setEventForward(onmsEvent.getEventForward());
			alarmJasperReportStructure.setEventMouseOverText(onmsEvent.getEventMouseOverText());
			alarmJasperReportStructure.setEventDisplay(onmsEvent.getEventDisplay());
			alarmJasperReportStructure.setEventAckUser(onmsEvent.getEventAckUser());
			alarmJasperReportStructure.setEventIfIndex(onmsEvent.getIfIndex());
			
			if(onmsEvent.getEventAckTime() != null){
				alarmJasperReportStructure.setEventAckTime(String.valueOf(formater.format(onmsEvent.getEventAckTime())));
			} else {
				alarmJasperReportStructure.setEventAckTime(null);
			}
			
			if(onmsEvent.getIpAddr() != null){
				alarmJasperReportStructure.setEventIpAddr(InetAddressUtils.str(onmsEvent.getIpAddr()));
			} else {
				alarmJasperReportStructure.setEventIpAddr(null);
			}
			
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		LOG.error("unable to set the alarm or event values in getAlarmWithEventValuesForCSV method ", ex);
    	}
    }
    
    public boolean getDuplicateIdStatus(OnmsEvent onmsEvent, int currEventAlarmId, int eventIterator){
                if(onmsEvent.getAlarm() != null){
                        if((currEventAlarmId == onmsEvent.getAlarm().getId()) && currEventAlarmId != 0){
                                return true;
                        }
                }
            return false;
    }
    
    public List<OnmsEvent> getEvents(HashMap<Integer, List<Integer>> eventIdsForAlarms , Integer alarmId){
            List<OnmsEvent> onmsEventList= new ArrayList<OnmsEvent>();
            for(Integer eventId : eventIdsForAlarms.get(alarmId)){
                    OnmsEvent onmsEvent = m_eventDao.get(eventId);
                    onmsEventList.add(onmsEvent);
            }
            return onmsEventList;
    }
    
    public List<OnmsAcknowledgment> getAcknowledgments(int alarmId) {
        CriteriaBuilder cb = new CriteriaBuilder(OnmsAcknowledgment.class);
        cb.eq("refId", alarmId);
        cb.eq("ackType", AckType.ALARM);
        return m_ackDao.findMatching(cb.toCriteria());
    }
    
    public Calendar getDateFormat(Date date){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            Calendar calDate = new GregorianCalendar(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),calendar.get(Calendar.SECOND));
            return calDate;
    }

    public void setGlobalReportRepository(final GlobalReportRepository globalReportRepository) {
        m_globalReportRepository = globalReportRepository;
    }
}
