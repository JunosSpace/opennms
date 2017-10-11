<%--
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

--%>

<%
	response.setHeader("X-Frame-Options", "SAMEORIGIN");
	response.setHeader("Pragma", "no-cache");
	if (request.getProtocol().equals("HTTP/1.1")) {
		response.setHeader("Cache-Control", "no-store ");
	}
%>

<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core'%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Login" />
	<jsp:param name="nonavbar" value="true" />
</jsp:include>
<%
if (request.getServletPath().endsWith("login.jsp")) {
		// generate JavaScript to see if this is needed
		// if i am in an iframe, go to jmp to login
%>
<script type="text/javascript">
	var request;
	if (window.XMLHttpRequest) {
		request = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		request = new ActiveXObject("Microsoft.XMLHTTP");
	}

	try {
    		var cookies = (document.cookie).split(';');
    		var csrfCookie = "";
    		for(var i=0; i < cookies.length; i++) {
        		if (cookies[i].trim().indexOf("X-CSRF=") == 0) {
            			csrfCookie = cookies[i].trim().split("=")[1];
				break;
        		}   
    		}
		request.onreadystatechange = function() {
			var DONE =  (typeof XMLHttpRequest.Done !== 'undefined') ? XMLHttpRequest.DONE : 4;

			if (request.readyState == DONE ) {
				
				if (request.responseText.length > 0) {
					var cookies = (document.cookie).split(';');
			    		var csrfCookie = "";
			    		for(var i=0; i < cookies.length; i++) {
        					if (cookies[i].trim().indexOf("X-CSRF=") == 0) {
			            			csrfCookie = cookies[i].trim().split("=")[1];
							break;
			        		}   
    					}
					var xmlHttp = new XMLHttpRequest();
				    	xmlHttp.open( "GET", '../mainui/unsecured/logout.jsp', false );
					xmlHttp.setRequestHeader("X-CSRF",csrfCookie);
					xmlHttp.send();
					window.top.location = '../mainui/spaceLogout.jsp';
				}
			}
		}
		request.open("POST", "../mainui/ctrl/fmpm/OpenNMSLogin", true);
		request.setRequestHeader("X-CSRF",csrfCookie);
		request.setRequestHeader("Content-type",
				"application/x-www-form-urlencoded");
		request.send();
	} catch (e) {
		console.log("Unable to connect to server");
	}
</script>
<%
	}
%>

<%-- this form-login-page form is also used as the 
         form-error-page to ask for a login again.
         --%>
	<p style="color: red;">
		<strong>Network Monitoring module is initializing for this
			user, please wait a moment... </strong>
	</p>
<%-- comment for Space Only
<div class="formOnly">
  <form action="<c:url value='j_spring_security_check'/>" method="post">
    <p>
      User: <input type="text" id="input_j_username" name="j_username" <c:if test="${not empty param.login_error}">value='<c:out value="${SPRING_SECURITY_LAST_USERNAME}"/>'</c:if> /><br /><br />
      Password: <input type='password' name='j_password'>
    </p>
      
    <!--
    <p><input type="checkbox" name="_spring_security_remember_me"> Don't ask for my password for two weeks</p>
    -->
    
    <input name="Login" type="submit" value="Login" />
    <input name="j_usergroups" type="hidden" value=""/>
    <!-- <input name="reset" type="reset" value="Reset" /> -->
    <script type="text/javascript">
      if (window.top == window.self && document.getElementById) {
        document.getElementById('input_j_username').focus();
      }
    </script>
  
  </form>
</div>
<div> <p> Please login from Space UI </p> </div>
--%>
<hr />

<jsp:include page="/includes/footer.jsp" flush="false" />
