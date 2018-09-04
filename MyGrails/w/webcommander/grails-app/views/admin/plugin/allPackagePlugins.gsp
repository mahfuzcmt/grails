<div class="header">
    <h3><g:message code="available.plugin"/></h3>
</div>

<div class="content">
    <div class="bmui-tab">
        <div class="bmui-tab-header-container top-side-header">
            <g:each in="${pluginByPackage}" var="pack">
                <div class="bmui-tab-header" data-tabify-tab-id="${pack.name.sanitize()}">
                    <span class="title">${pack.name}</span>
                </div>
            </g:each>
            <div class="bmui-tab-header" data-tabify-tab-id="additional-plugins">
                <span class="title"><g:message code="additional.plugins"/></span>
            </div>
        </div>

        <div class="bmui-tab-body-container">
            <g:each in="${pluginByPackage}" var="pack">
                <div id="bmui-tab-${pack.name.sanitize()}" class="tab-body">
                    <div class="plugins">
                        <g:each in="${pack.plugins}" var="plugin">
                            <div class="plugin">
                                <h3 class="name">${plugin.name}</h3>
                                <div class="description">${plugin.description}</div>
                            </div>
                        </g:each>
                    </div>
                    <div class="bottom">
                        <g:if test="${pack.name != licenseConfig.package_name}">
                            <button weight="${pack.weight}" type="button" class="${pack.weight?.toInteger() > licenseConfig.package_weight ? "upgrade" : "downgrade"}-package update-package" pack-id="${pack.id}">
                                <g:message code="${pack.weight?.toInteger() >= licenseConfig.package_weight ? "upgrade" : "downgrade"}.your.package" />
                            </button>
                        </g:if>
                    </div>
                </div>
            </g:each>
            <div id="bmui-tab-additional-plugins" class="tab-body">
                <div class="plugins">
                    <g:each in="${addons}" var="plugin">
                        <div class="plugin">
                            <h3 class="name">${plugin.name}</h3>
                            <div class="description">${plugin.description}</div>
                            <button type="button" class="install" plugin-id="${plugin.plugin_id}"><g:message code="${"install"}"/></button>
                        </div>
                    </g:each>
                </div>
            </div>
        </div>
    </div>
</div>