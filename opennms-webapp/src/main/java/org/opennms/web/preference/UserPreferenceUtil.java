package org.opennms.web.preference;

import org.opennms.netmgt.dao.api.UserPreferenceDao;
import org.opennms.web.admin.discovery.ActionDiscoveryServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserPreferenceUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ActionDiscoveryServlet.class);

	public static int getPageSizeLimit(String userName, String page, UserPreferenceDao userPreferenceDao) {
		if (userPreferenceDao != null) {
			try {
				return userPreferenceDao.getUserPreference(userName, page + "_page_limit_size");
			} catch (Exception e) {
				LOG.error("Exception occure while getUserPreference for page " + page + " limit szie :" + e );
			}
		}
		return 20;
	}

}
