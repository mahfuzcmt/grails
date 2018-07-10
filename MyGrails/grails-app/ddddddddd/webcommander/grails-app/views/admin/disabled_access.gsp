<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <title><g:message code="webcommander.error"/></title>
    </head>
    <style type="text/css">
        html {
            height : 100%;
        }
    </style>
    <body style="height: 100%; margin: 0px; text-align: center;">
        <span style="height: 100%; vertical-align: middle; display: inline-block;"></span>
        <div style="height: 300px; width: 800px; border: 1px solid #E6E6E6; display: inline-block; *display: inline; zoom: 1; vertical-align: middle;box-shadow: 0 0 5px #CCCCCC;">
            <div style="color: #5555FF; text-align: center; font-size: 30px; height: 58px; padding-top: 20px; border-bottom: 1px solid #CCCCCC; background-color: #F4F4F4;">
                <g:message code="${topType}"/> !!!
                <hr style="margin: 23px 0px;">
            </div>
            <div style="padding: 30px; text-align: center; color: #5555FF; font-size: 30px;">
                <div id="administrative-notification-wrapper">
                    <div class="administrative-notification notification-critical">
                        <div class="title"><g:message code="critical"/></div>
                        <div class="message">${message}</div>
                    </div>
                </div>
                <g:if test="${topType == 'operator'}">
                    <g:message code="disabled.access.for.operator.page.text" args="${[app.relativeBaseUrl() + 'userAuthentication/logout']}"/>
                </g:if>
                <g:else>
                    <g:message code="disabled.access.for.license.page.text" args="${[app.relativeBaseUrl() + 'license/refreshToDashboard']}"/>
                </g:else>
            </div>
        </div>
    </body>
</html>