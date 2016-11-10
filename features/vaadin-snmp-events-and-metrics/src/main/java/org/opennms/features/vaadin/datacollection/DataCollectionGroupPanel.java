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
package org.opennms.features.vaadin.datacollection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.api.Logger;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class DataCollectionGroupPanel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
// FIXME: When renaming a datacollection-group, all the SNMP collections must be updated.
// FIXME: When a different group is selected and the current one is being edited, warn about discard the changes or save them before continue
@SuppressWarnings("serial")
public abstract class DataCollectionGroupPanel extends Panel implements TabSheet.SelectedTabChangeListener {

    /** The group name. */
    private final TextField groupName = new TextField("Data Collection Group Name");

    /** The resource types. */
    private final ResourceTypePanel resourceTypes;

    /** The groups. */
    private final GroupPanel groups;

    /** The system definitions. */
    private final SystemDefPanel systemDefs;

    /**
     * Instantiates a new data collection group panel.
     *
     * @param dataCollectionConfigDao the OpenNMS Data Collection Configuration DAO
     * @param group the data collection group object
     * @param logger the logger object
     */
    public DataCollectionGroupPanel(final DataCollectionConfigDao dataCollectionConfigDao, final DatacollectionGroup group, final Logger logger, final String fileName) {
        setCaption("Data Collection");
        addStyleName("light");

        // Data Collection Group - Main Fields

        groupName.setPropertyDataSource(new ObjectProperty<String>(group.getName()));
        groupName.setNullSettingAllowed(false);
        groupName.setImmediate(true);

        resourceTypes = new ResourceTypePanel(dataCollectionConfigDao, group, logger);
        groups = new GroupPanel(dataCollectionConfigDao, group, logger);
        systemDefs = new SystemDefPanel(dataCollectionConfigDao, group, logger);

        // Button Toolbar

        final HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addComponent(new Button("Save Data Collection File", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                processDataCollection(dataCollectionConfigDao, logger, fileName);
            }
        }));
        toolbar.addComponent(new Button("Cancel", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                cancel();
                logger.info("Data collection processing has been canceled");
            }
        }));

        // Tab Panel

        final TabSheet tabs = new TabSheet();
        tabs.addStyleName("light");
        tabs.setSizeFull();
        tabs.addTab(resourceTypes, "Resource Types");
        tabs.addTab(groups, "MIB Groups");
        tabs.addTab(systemDefs, "System Definitions");

        // Main Layout

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(toolbar);
        mainLayout.addComponent(groupName);
        mainLayout.addComponent(tabs);
        mainLayout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);
        setContent(mainLayout);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.TabSheet.SelectedTabChangeListener#selectedTabChange(com.vaadin.ui.TabSheet.SelectedTabChangeEvent)
     */
    @Override
    public void selectedTabChange(SelectedTabChangeEvent event) {
        TabSheet tabsheet = event.getTabSheet();
        Tab tab = tabsheet.getTab(tabsheet.getSelectedTab());
        if (tab != null) {
            Notification.show("Selected tab: " + tab.getCaption());
        }
    }    

    /**
     * Gets the OpenNMS data collection group.
     *
     * @return the OpenNMS data collection group
     */
    public DatacollectionGroup getOnmsDataCollection() {
        final DatacollectionGroup group = new DatacollectionGroup();
        group.setName((String) groupName.getValue());
        group.setGroups(groups.getGroups());
        group.setResourceTypes(resourceTypes.getResourceTypes());
        group.setSystemDefs(systemDefs.getSystemDefs());
        return group;
    }

    /**
     * Cancel.
     */
    public abstract void cancel();

    /**
     * Success.
     */
    public abstract void success();

    /**
     * Failure.
     *
     * @param reason the reason
     */
    public abstract void failure(String reason);

    /**
     * Process data collection.
     *
     * @param dataCollectionConfigDao the OpenNMS data collection configuration DAO
     * @param logger the logger
     */
    private void processDataCollection(final DataCollectionConfigDao dataCollectionConfigDao, final Logger logger, final String fileName) {
        final DatacollectionGroup dcGroup = getOnmsDataCollection();
        final File configDir = new File(ConfigFileConstants.getHome(), "etc/datacollection/");
        final File file = new File(configDir, fileName);
        if (file.exists()) {
            ConfirmDialog.show(getUI(),
                               "Are you sure?",
                               "Do you really want to override the existig file?\nAll current information will be lost.",
                               "Yes",
                               "No",
                               new ConfirmDialog.Listener() {
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        saveFile(file, dcGroup, logger);
                    }
                }
            });
        } else {
            if (dataCollectionConfigDao.getAvailableDataCollectionGroups().contains(dcGroup.getName())) {
                Notification.show("There is a group with the same name, please pick another one.");
            } else {
                saveFile(file, dcGroup, logger);
            }
        }
    }

    /**
     * Saves file.
     *
     * @param file the file
     * @param dcGroup the datacollection-group
     * @param logger the logger
     */
    private void saveFile(final File file, final DatacollectionGroup dcGroup, final Logger logger) {
        try {
            FileWriter writer = new FileWriter(file);
            JaxbUtils.marshal(dcGroup, writer);
            logger.info("Saving XML data into " + file.getAbsolutePath());
            logger.warn("Remember to update datacollection-config.xml to include the group " + dcGroup.getName() + " into an SNMP collection.");
            // Force reload datacollection-config.xml to be able to configure SNMP collections.
            try {
                final File configFile = ConfigFileConstants.getFile(ConfigFileConstants.DATA_COLLECTION_CONF_FILE_NAME);
                configFile.setLastModified(System.currentTimeMillis());
            } catch (IOException e) {
                logger.warn("Can't reach datacollection-config.xml: " + e.getMessage());
            }
            success();
        } catch (Exception e) {
            logger.error(e.getClass() + ": " + (e.getMessage() == null ? "[No Details]" : e.getMessage()));
            if (e.getMessage() == null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                logger.error(sw.toString());
            }
            failure(e.getMessage());
        }
    }
}
