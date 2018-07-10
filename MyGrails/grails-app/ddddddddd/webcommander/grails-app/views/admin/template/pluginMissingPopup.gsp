<form action="${app.relativeBaseUrl()}templateAdmin/installMissingPlugins" method="post" class="edit-popup-form">
    <input type="hidden" name="id" value="${params.id}">
    <input type="hidden" name="color" value="${params.color}">

    <div class="install-template-popup-content">
        <div class="warning-box first-option-warning">
            <g:message code="install.template.first.option.warning.message"/>
        </div>
        <br>

        <div>
            <header>
                <g:message code="missing.plugin.install.message"/>
            </header>
        </div>

        <div class="app-tab-content-container">
            <table class="content">
                <thead>
                <tr>
                    <th><g:message code="plugin.name"/></th>
                    <th><g:message code="status"/></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${installablePlugins}" var="plugin">
                    <div class="plugin">
                        <tr>
                            <td><div class="wrapper">${plugin}</div></td>
                            <td><div class="wrapper"><g:message code="installable"/></div></td>
                        </tr>
                    </div>
                </g:each>
                <g:each in="${systemUndefinedPlugins}" var="plugin">
                    <div class="plugin">
                        <tr>
                            <td><div class="wrapper">${plugin}</div></td>
                            <td><div class="wrapper"><g:message code="system.undefined"/></div></td>
                        </tr>
                    </div>
                </g:each>
                </tbody>
            </table>
        </div>
    </div>

    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="confirm"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>