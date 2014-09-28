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

  var user = window.localStorage.getItem('opennmsuser').toLowerCase();
  if (user.lastIndexOf('\\') > -1) {
     user = user.substr(user.lastIndexOf('\\') + 1);
   }

  if (window.top != window.self && user != "") {
    //window.top.location = '../mainui/unsecured/logout?appName=CMP';
    
    //var user = window.localStorage.getItem('opennmsuser').toLowerCase();
    //if (user.lastIndexOf('\\') > -1) {
    //    user = user.substr(user.lastIndexOf('\\') + 1);
    //}
    var password = window.localStorage.getItem('opennmspw');
    var groups = window.localStorage.getItem('opennmsdomainname');

    var form = document.createElement("form");
    form.setAttribute("method", "post");
    form.setAttribute("action", "j_spring_security_check");

    var hiddenFieldName = document.createElement("input");
    hiddenFieldName.setAttribute("type", "hidden");
    hiddenFieldName.setAttribute("name", "j_username");
    hiddenFieldName.setAttribute("value", user);
    form.appendChild(hiddenFieldName);

    var hiddenFieldPassword = document.createElement("input");
    hiddenFieldPassword.setAttribute("type", "hidden");
    hiddenFieldPassword.setAttribute("name", "j_password");
    hiddenFieldPassword.setAttribute("value", password);
    form.appendChild(hiddenFieldPassword);

    var hiddenFieldGroups = document.createElement("input");
    hiddenFieldGroups.setAttribute("type", "hidden");
    hiddenFieldGroups.setAttribute("name", "j_usergroups");
    hiddenFieldGroups.setAttribute("value", groups);
    form.appendChild(hiddenFieldGroups);

    document.body.appendChild(form);
    form.submit();

 <c:if test="${not empty param.login_error}">
  <p style="color:red;">
    <strong>Network Monitoring module is initializing for this user, please wait a moment... </strong>
  </p>

</c:if> 
  
   } else {
    window.top.location = '../mainui/';
   }
</script>
<%
     }
%>

<%-- this form-login-page form is also used as the 
         form-error-page to ask for a login again.
         --%>
<c:if test="${not empty param.login_error}">
  <p style="color:red;">
    <strong>Network Monitoring module is initializing for this user, please wait a moment... </strong>
  </p>

  <%-- This is: AbstractProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY
  <p>Reason: ${SPRING_SECURITY_LAST_EXCEPTION.message}</p>
  --%>
</c:if>
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
