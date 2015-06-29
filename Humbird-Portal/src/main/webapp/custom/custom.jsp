<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head lang="en">
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta charset="UTF-8">
    <title>Humbird Plug-in Module</title>
    <link rel="stylesheet" href="${base.contextPath}/css/bootstrap.min.css">
    <link rel="stylesheet" href="${base.contextPath}/css/bootstrap-theme.min.css">
    <link rel="stylesheet" href="${base.contextPath}/css/style.css">

    <script src="${base.contextPath}/js/jquery-2.1.3.min.js"></script>
    <script src="${base.contextPath}/js/bootstrap.min.js"></script>
    <script src="${base.contextPath}/js/sbbu-cancel.js"></script>
</head>
<body>

<div class="header">
    <h1>Humbird Plug-in Module</h1>
</div>
<br/>

<ul class="nav nav-tabs">
    <li role="presentation"><img width="32" height="30" src="${base.contextPath}/img/logo_small_negative_tcm69-169865.png" alt="Telenor Logo"/></li>
    <li role="presentation"><a href="${base.contextPath}/index.xhtml">Home</a></li>
    <li role="presentation"><a href="${base.contextPath}/custom/index.xhtml">Config</a></li>
    <li role="presentation"><a href="../orders/index.html">Test data</a></li>
    <li role="presentation"><a href="../sessions/">Order sessions</a></li>
    <li role="presentation"><a href="../eoc-status/index.jsp">EOC status</a></li>
</ul>
<div class="tab-box">
<h3>Properties Table</h3>
<div class="tab-content">
    <form class="form-horizontal" action="sbbuterm" method="post">
    <c:forEach var="item" items="${props.props}" varStatus="s">
            <c:choose>
                <c:when test="${item eq null || empty item}">
                    <br/>
                </c:when>
                <c:otherwise>
                    <c:set var="comms" value="${props.comms[s.count-1]}"/>
                    <c:if test="${comms ne null && comms.rows gt 1}">
                        <div class="form-group" id="${item.key}-comments">
                            <dl class="col-sm-offset-2 hidden col-sm-10">
                                <c:forEach var="vals" items="${comms.comments}" varStatus="vs">
                                    <dd><mark>${vals}</mark></dd>
                                </c:forEach>
                            </dl>
                        </div>
                    </c:if>
                    <div class="form-group">
                        <label class="col-lg-4 control-label" for="${item.key}">${fn:substringAfter(item.key, 'org.humbird.soa.component.')}</label>
                        <div class="col-sm-7">
                            <c:choose>
                                <c:when test="${comms ne null && comms.rows eq 1}">
                                    <input type="text" class="form-control" id="${item.key}" placeholder="${comms.comments[0]}" name="${item.key}" value="${item.val}">
                                </c:when>
                                <c:otherwise>
                                    <input type="text" class="form-control" id="${item.key}" placeholder="" name="${item.key}" value="${item.val}">
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
    </c:forEach>
        <div class="form-group">
            <div class="col-sm-offset-4 col-sm-10">
                <div class="checkbox">
                    <label>
                        <input type="checkbox" name="cancel" id="cancelCheck"> Cancel
                    </label>
                </div>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-3 col-sm-10">
                <button type="submit" class="btn btn-default">Submit</button>
            </div>
        </div>
    </form>
</div>

</div>
</body>
</html>