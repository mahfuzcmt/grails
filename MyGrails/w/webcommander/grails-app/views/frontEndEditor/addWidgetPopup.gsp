<%@ page import="com.webcommander.constants.FrontEndEditorConstants" %>
<div class="content fee-noMargin">
    <div class="content-type-selection">
        <div class="floating-popup type-selector">
            <div class="action-navigator">Select Widget</div>
            <div class="popup-body context-menu">
                <div class="action-item" data-action="add-widget">Add Widget</div>
                <div class="action-item" data-action="add-column">Add Layout</div>
            </div>
        </div>
    </div>
    <g:set var="widgets" value="${FrontEndEditorConstants.widgets.findAll {(it.value.type == FrontEndEditorConstants.widgetContentType.WIDGET) && it.value.inPopUpTab}}"/>
    <div class="fee-widget-list-widgets">
        <div class="widget-tabs bmui-tab left-side-header">
            <div class="bmui-tab-header-container">
                <g:each in="${widgets}" var="widget">
                    <g:if test="${widget.key == 'html'}">
                        <div tab-id="${widget.key}" class="bmui-tab-header fee-widget-item fee-${widget.key}" widget-type="${widget.key}" data-type="widget" data-inline-edit="${widget.value.inlineEdit}">
                            <div class="fee-icon"></div>
                            <div class="fee-label"></div>
                        </div>
                    </g:if>
                    <g:else>
                        <div url="${app.baseUrl()}frontEndEditor/${widget.key.capitalize()}Config?type=${widget.key}" tab-id="${widget.key}" class="bmui-tab-header fee-widget-item fee-${widget.key}" widget-type="${widget.key}" data-type="widget" data-inline-edit="${widget.value.inlineEdit}">
                            <div class="fee-icon"></div>
                            <div class="fee-label"></div>
                        </div>
                    </g:else>
                </g:each>
            </div>
            <div class="bmui-tab-body-container">
                <div id="bmui-tab-html"><g:include view="frontEndEditor/contentEditor.gsp"/></div>
            </div>
        </div>
    </div>

    <div class="layout-list add-layout" style="display: none">
        <div class="fee-widget-list-columns">
            <div class="layout-header">Layout</div>
            <div class="layout-parts">
                <div class="fee-widget-item fee-twoColumn" widget-type="twoColumn" data-type="column" data-inline-edit="false">
                    <div class="fee-box box1" data-position="0"></div>
                    <div class="fee-box box2" data-position="1"></div>
                    <div class="fee-label">2 Column</div></div>
                <div class="fee-widget-item fee-threeColumn" widget-type="threeColumn" data-type="column" data-inline-edit="false">
                    <div class="fee-box box1" data-position="0"></div>
                    <div class="fee-box box2" data-position="1"></div>
                    <div class="fee-box box3" data-position="2"></div>
                    <div class="fee-label">3 Column</div></div>
                <div class="fee-widget-item fee-spacer" widget-type="spacer" data-type="widget" data-inline-edit="true">
                    <div class="fee-icon spacer"></div>
                    <div class="fee-label">Spacer</div>
                </div>
            </div>
        </div>
    </div>
</div>
