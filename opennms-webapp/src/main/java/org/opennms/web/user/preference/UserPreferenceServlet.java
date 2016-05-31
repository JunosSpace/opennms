
package org.opennms.web.user.preference;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.dao.api.UserPreferenceDao;
import org.springframework.util.Assert;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserPreferenceServlet extends HttpServlet {

	private static final Logger LOG = LoggerFactory.getLogger(UserPreferenceServlet.class);

	/** {@inheritDoc} */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String stateName = request.getParameter("stateName");
		String stateValue = request.getParameter("stateValue");
		String userName = getUsername();
		HttpSession session = request.getSession(true);
		
        WebApplicationContext beanFactory = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        UserPreferenceDao userPreferenceDao = beanFactory.getBean(UserPreferenceDao.class);

		if (userPreferenceDao != null) {
			try {
				userPreferenceDao.setUserPreference(userName , stateName, stateValue);
				session.setAttribute(stateName,stateValue);
			} catch (Exception e) {
				LOG.error("Exception occure while set userpreference: " + e);
			}
		}
	}

	protected String getUsername() {
		/*
		 * This should never be null, as the strategy should create a
		 * SecurityContext if one doesn't exist, but let's check anyway.
		 */
		SecurityContext context = SecurityContextHolder.getContext();
		Assert.state(context != null, "No security context found when calling SecurityContextHolder.getContext()");

		org.springframework.security.core.Authentication auth = context.getAuthentication();
		Assert.state(auth != null,
				"No Authentication object found when calling getAuthentication on our SecurityContext object");

		Object obj = auth.getPrincipal();
		Assert.state(obj != null, "No principal object found when calling getPrincipal on our Authentication object");

		if (obj instanceof UserDetails) {
			return ((UserDetails) obj).getUsername();
		} else {
			throw new IllegalStateException("principal should always be instanceof UserDetails");
		}
	}

}
