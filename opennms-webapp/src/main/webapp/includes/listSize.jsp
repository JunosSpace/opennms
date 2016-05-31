<html>
<head>
<script type="text/javascript">
function UpdateLimitSize(value){
	<%String pageName =request.getParameter("pageName");%>
    var state_name ="<%=pageName%>" + "_page_limit_size";
	var state_value = value;

	var request;
	if(window.XMLHttpRequest){
	request=new XMLHttpRequest();
	}
	else if(window.ActiveXObject){
	request=new ActiveXObject("Microsoft.XMLHTTP");
	}

	try{
	request.open("POST","user/preference",false);
	request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	request.send("stateName="+state_name+"&stateValue="+state_value);

	}catch(e){alert("Unable to connect to server");}
    <%String url = request.getParameter("baseurl");%>
    var url_value ="<%=url%>";
    var reExp = /limit=[^&]+/;
    var newUrl = url_value.replace(reExp, "limit=" + value);
    newUrl = newUrl.replace(/&amp;/g, '&');
	window.location = newUrl;


}
</script>
</head>
<body>Show:
<select class="limit1"  id="limitSize" onchange="UpdateLimitSize(this.value)">
<%int limitSize = Integer.parseInt(request.getParameter("limitSize"));%>
<%if(limitSize == 10){%>
<option value="10" selected>10</option>
<%}else{%>
<option value="10" >10</option>
<%}%>
<%if(limitSize == 20){%>
<option value="20" selected>20</option>
<%}else{%>
<option value="20" >20</option>
<%}%>
<%if(limitSize == 30){%>
<option value="30" selected>30</option>
<%}else{%>
<option value="30" >30</option>
<%}%>
<%if(limitSize == 50){%>
<option value="50" selected>50</option>
<%}else{%>
<option value="50" >50</option>
<%}%>
<%if(limitSize == 80){%>
<option value="80" selected>80</option>
<%}else{%>
<option value="80" >80</option>
<%}%>
<%if(limitSize == 100){%>
<option value="100" selected>100</option>
<%}else{%>
<option value="100" >100</option>
<%}%>
<%if(limitSize == 500){%>
<option value="500" selected>500</option>
<%}else{%>
<option value="500" >500</option>
<%}%>
<%if(limitSize == 1000){%>
<option value="1000" selected>1000</option>
<%}else{%>
<option value="1000" >1000</option>
<%}%>
</select> Items
</body>
</html>
