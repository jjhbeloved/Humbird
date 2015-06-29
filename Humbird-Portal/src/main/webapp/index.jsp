<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <title>Humbird Plug-in Module</title>
    <link rel="stylesheet" href="${base.contextPath}/css/bootstrap.min.css">
    <link rel="stylesheet" href="${base.contextPath}/css/bootstrap-theme.min.css">
    <link rel="stylesheet" href="${base.contextPath}/css/style.css">

    <script src="${base.contextPath}/js/jquery-2.1.3.min.js"></script>
    <script src="${base.contextPath}/js/bootstrap.min.js"></script>
</head>
<body>
<div class="header">
    <h1>Humbird Plug-in Module</h1>
</div>
<br/>
<ul class="nav nav-tabs">
    <li role="presentation"><img width="32" height="30" src="${base.contextPath}/img/logo_small_negative_tcm69-169865.png" alt="Telenor Logo"/></li>
    <li role="presentation" class="active"><a href="#">Home</a></li>
    <li role="presentation"><a href="${base.contextPath}/custom/index.xhtml">Config</a></li>
    <li role="presentation"><a href="orders/index.html">Test data</a></li>
    <li role="presentation"><a href="sessions/">Order sessions</a></li>
    <li role="presentation"><a href="eoc-status/index.jsp">EOC status</a></li>
</ul>
<div class="tab-box">
    <div class="btn-group" role="group" aria-label="...">
        <a class="btn btn-primary" href="changelog.html" role="button">Changelog</a>
        <a class="btn btn-primary" href="testcolumbine" role="button">WSDL</a>
    </div>
    <h3>Configured properties</h3>
    <table class="table table-striped table-bordered" style="width:100%">
    <thead>
        <tr>
            <td style="font-weight: bold;">Key</td>
            <td style="font-weight: bold;">Value</td>
        </tr>
    </thead>
        <c:forEach var="item" items="${props}">
            <tr><td>${item.key}</td><td>${item.value}</td></tr>
        </c:forEach>
    </table>
</div>
<div id="version">version:${version}</div>
</body>
</html>