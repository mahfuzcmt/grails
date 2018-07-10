<%@ page import="com.webcommander.util.StringUtil" %>
<g:form class="edit-popup-form create-edit-form" controller="widget" action="saveFlashWidget" enctype="multipart/form-data">
    <span class="configure-btn" title="<g:message code="configuration"/>"><i></i></span>
    <div class="form-section">
        <div class="form-section-info">
            <div class="form-section-info">
                <h3><g:message code="flash.selection.info"/></h3>
                <div class="info-content"><g:message code="section.text.flash.selection"/></div>
            </div>
        </div>
        <div class="form-section-container">
            <div class="widget-config-panel">
                <div class="form-row">
                    <label><g:message code="title"/></label>
                    <input type="text" class="medium" name="title" value="${widget.title}">
                </div>

                <div class="form-row">
                    <label><g:message code="width"/></label>
                    <input type="text" class="medium" name="width" value="${config.width}">
                </div>

                <div class="form-row">
                    <label><g:message code="height"/></label>
                    <input type="text" class="medium" name="height" value="${config.height}">
                </div>
                <div class="form-row">
                    <label><g:message code="attributes"/></label>
                    <input type="text" class="medium" name="attribute" value="${config.attributes}">
                </div>
            </div>
            <g:set var="ref1" value="${StringUtil.uuid}"/>
            <g:set var="ref2" value="${StringUtil.uuid}"/>
            <g:set var="ref3" value="${StringUtil.uuid}"/>
            <g:set var="ref4" value="${StringUtil.uuid}"/>
            <div class="double-input-row">
                <div class="form-row">
                    <span class="label label-block asset-depend local-depend">
                        <input type="radio" id="${ref1}" name="upload_type" class="direct-upload" value="direct" ${config.upload_type ? (config.upload_type == "direct" ? "checked" : "") : "checked"}>
                        <span><g:message code="direct.url"/></span>
                    </span>
                    <input type="text" class="medium" name="direct_url" value="${config.upload_type == "direct" ? widget.content : ""}"
                           validation="skip@not{global:#${ref1}:checked} required partial_url" depends=".direct-depend">
                </div><div class="form-row">
                    <span class="label label-block direct-depend local-depend">
                        <input type="radio" id="${ref2}" name="upload_type" id="flash-widget-upload-type-asset-library" class="asset-library-upload" value="asset_library" ${config.upload_type == "asset_library" ? "checked" : ""}>
                        <span><g:message code="asset.library"/></span>
                    </span>
                    <input type="text" validation="required@if{global:#${ref2}:checked}" readonly class="medium" name="asset_library_url" value="${config.upload_type == "asset_library" ? widget.content : ""}" depends=".asset-depend">
                    <span class="tool-icon select-from-asset-library" title="<g:message code="asset.library"/> "></span>
                </div>
            </div>
            <div class="form-row">
                <span class="label label-block direct-depend asset-depend">
                    <input type="radio" id="${ref3}" class="local-flash-upload" name="upload_type" value="local" ${config.upload_type == "local" ? "checked" : ""}>
                    <span><g:message code="local.file"/></span>
                </span>
                <input type="hidden" name="local_file_path">
                <input type="hidden" name="local_file_name">
                <input type="hidden" class="${config.upload_type == "local" ? "has_url" : ""}" name="local_url" value="${config.upload_type == "local" ? widget.content : ""}" id="${ref4}">
                <input depends=".local-depend" type="file" name="local" file-type="flash" validation="skip@if{global:#${ref4}.has_url} drop-file-required@if{global:#${ref3}:checked}" submit="auto" ajax-url="${app.relativeBaseUrl()}widget/tempFlash?uuid=${widget.uuid}">
            </div>
            <div class="multi-column two-column">
                <div class="columns first-column">
                    <div class="column-content">
                        <fieldset>
                            <legend><g:message code="parameter"/></legend>
                            <table class="parameters content">
                                <colgroup>
                                    <col style="width: 40%">
                                    <col style="width: 40%">
                                    <col style="width: 20%">
                                </colgroup>
                                <tr>
                                    <th><g:message code="name"/></th>
                                    <th><g:message code="value"/></th>
                                    <th>
                                        <span class="tool-icon remove-all"></span>
                                    </th>
                                    <g:each in="${config.paramName}" status="i" var="param">
                                        <tr>
                                            <td><input class="td-full-width" type="text" name="paramName" value="${param}"></td>
                                            <td><input class="td-full-width" type="text" name="paramValue" value="${config.paramValue[i]}"></td>
                                            <td><span class="tool-icon remove"></span></td>
                                        </tr>
                                    </g:each>
                                </tr>
                            </table>
                            <div class="right-panel">
                                <button type="button" class="add-parameter"><g:message code="add.parameter"/></button>
                            </div>
                        </fieldset>
                    </div>
                </div><div class="columns last-column">
                    <div class="column-content">
                        <fieldset>
                            <legend><g:message code="preview"/></legend>
                            <div class="flash-widget-preview description-view-block"></div>
                        </fieldset>
                    </div>
                </div>
            </div>
            <div class="button-line btn-row">
                <button class="preview-button" type="button"><g:message code="preview"/></button>
                <button type="submit" class="edit-popup-form-submit submit-button apply"><g:message code="update"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</g:form>