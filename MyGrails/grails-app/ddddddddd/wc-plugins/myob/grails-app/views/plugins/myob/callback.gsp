<%--
  Created by IntelliJ IDEA.
  User: sanjoy
  Date: 3/4/14
  Time: 12:21 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>
        <g:if test="${success}"><g:message code="authorization.success"/> </g:if>
        <g:else><g:message code="authorization.failed"/> </g:else>
    </title>
    <script type="text/javascript">
        var success = ${success};
        if(success){
            window.opener.window.app.global_event.trigger("myob-auth-state-change");
            window.close();
        }
    </script>
</head>
    <g:if test="${success}">
        <g:message code="authorization.success.msg"/>
    </g:if>
    <g:else>
        <g:message code="authorization.failed.msg"/>
    </g:else>
<body>

</body>
</html>