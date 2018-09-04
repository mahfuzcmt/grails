<div class="toolbar-share"></div>
<div class="app-tab-content-container">
    <g:if test="${packages.visibility}">
        <div class="top">
            <div class="left">
                <span class="title"><g:message code="package"/> </span>
            </div>
            <div class="right">
                <g:if test="${licenseConfig.is_trial_package == "true"}">
                    <span>Current Package trail. This is a 30 days trial version, with limited no of plugins. Upgrade your website with more interesting on appealing plugin</span>
                </g:if>
            </div>
        </div>
        <div class="content">
            <div class="packages">
                <g:each in="${packages.packages}" var="pack">
                    <div class="package-wrap ${pack.name == licenseConfig.package_name ? " active": ""}">
                        <div class="package">
                            <div class="name">${pack.displayName}</div>
                            <div class="price">${pack.price}</div>
                            <div class="features">
                                <g:each in="${pack.features}" var="feature">
                                    <div class="feature ${feature.allowed ? "allowed" : "not-allowed"}">
                                        <div class="title">${feature.title}</div>
                                        <div class="description">${feature.description}</div>
                                    </div>
                                </g:each>
                            </div>
                            <g:if test="${pack.name != licenseConfig.package_name}">
                                <div class="button-wrap">
                                    <button class="purchase-package" package-id="${pack.mapWithProvisioning}"><g:message code="purchase"/></button>
                                </div>
                            </g:if>
                            <g:else>
                                <div class="active-package"><g:message code="active.package"/></div>
                            </g:else>
                        </div>
                    </div>
                </g:each>
            </div>
        </div>
    </g:if>
</div>