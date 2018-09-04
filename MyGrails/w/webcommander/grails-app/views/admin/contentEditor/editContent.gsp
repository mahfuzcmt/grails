<%@ page import="com.webcommander.util.StringUtil; com.webcommander.constants.NamedConstants; com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="isResponsive" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.RESPONSIVE, "is_responsive") == "true"}"/>
<div class="left-bar bmui-tab ${isResponsive && !noResponsive ? 'responsive' : 'non-responsive'}">
    <div class="block-chooser bmui-tab-header-container">
        <span class="block widget bmui-tab-active bmui-tab-header" data-tabify-tab-id="widget"><g:message code="content"/></span>
        <span class="block setting bmui-tab-header" data-tabify-tab-id="setting"><g:message code="setting"/></span>
    </div>
    <div class="sidebar-contents bmui-tab-body-container">
        <div id="bmui-tab-widget" class="block-panel widget-block-panel">
            <form class="search-form tool-group widget-search-from">
                <input type="text" class="search-text" placeholder="Search"><button type="submit" class="icon-search"></button>
            </form>
            <div class="widget-separator favorite">
                <span class="title"><g:message code="favourite.widget"/></span>
            </div><g:each in="${favoriteWidgets}" var="type"><g:if test="${widgetLabels[type + '.title']}"><license:allowed id="${widgetLicense[type]}"><g:if test="${((DomainConstants.ECOMMERCE_WIDGET_TYPE_CHECKLIST[type] == true) && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')) || (DomainConstants.ECOMMERCE_WIDGET_TYPE_CHECKLIST[type] == null)}"><div class="widget-item ${type}" widget-type="${type}" title="<g:message code="${widgetLabels[type + '.title']}"/>">
                <span class="favorite-mark active"></span>
                <span class="widget-logo"></span>
                <span class="title"><g:message code="${widgetLabels[type + '.label']}"/></span>
            </div></g:if></license:allowed></g:if></g:each><div class="widget-separator other">
                <span class="title"><g:message code="other.widgets"/></span>
            </div><g:each in="${widgets}" var="type"><license:allowed id="${widgetLicense[type]}"><g:if test="${((DomainConstants.ECOMMERCE_WIDGET_TYPE_CHECKLIST[type] == true) && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')) || (DomainConstants.ECOMMERCE_WIDGET_TYPE_CHECKLIST[type] == null)}"><div class="widget-item ${type}" widget-type="${type}" title="<g:message code="${widgetLabels[type + '.title']}"/>">
                <span class="favorite-mark"></span>
                <span class="widget-logo"></span>
                <span class="title"><g:message code="${widgetLabels[type + '.label']}"/></span>
            </div></g:if></license:allowed></g:each></div>
        <div id="bmui-tab-setting" class="block-panel setting-block-panel">
            <g:if test="${!isAutoPage}">
                <div class='sidebar-group section-selector'>
                    <span class="sidebar-group-label"><g:message code="section"/></span>
                    <div class='sidebar-group-body'>
                        <g:if test="${isPage && layoutId}">
                            <g:select class="sidebar-input double-action active-section-selector" name="active-section" keys="${['body']}" from="${['body'].collect{g.message(code: it)}}" value="body"/>
                        </g:if>
                        <g:else>
                            <g:select class="sidebar-input double-action active-section-selector" name="active-section" keys="${['header', 'body', 'footer']}" from="${['header', 'body', 'footer'].collect{g.message(code: it)}}" value="${this.section}"/>
                        </g:else>
                        <span class="tool-icon add add-dock" title="<g:message code="add.dock.section"/>"></span>
                        <span class="tool-icon remove remove-dock disabled" title="<g:message code="remove.selected.section"/>"></span>
                    </div>
                </div>
            </g:if>
            <g:if test="${isResponsive && !noResponsive}">
                <div class="sidebar-group change-resolution">
                    <span class="sidebar-group-label"><g:message code="resolution"/></span>
                    <select class="sidebar-input">
                        <option value=""><g:message code="global"/></option>
                        <g:each in="${resolutions}" var="resolution">
                            <option value='${resolution.min}-${resolution.max}'><g:if test="${resolution.min}"><g:message code="min"/> ${resolution.min}</g:if> -> <g:if test="${resolution.max}"><g:message code="max"/> ${resolution.max}</g:if></option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <div class="leftbar-accordion">
                <div class="widget-mode-header label-bar">
                    <label><g:message code="widget"/></label>
                    <span class="tool-icon toggle-icon"></span>
                </div>
                <div class="widget-mode accordion-item">
                    <div class="sidebar-group widget-selector">
                        <span class="sidebar-group-label"><g:message code="widgets.in.section"/></span>
                        <div class='sidebar-group-body'>
                            <select class="sidebar-input single-action active-widget-selector"></select>
                            <span class="tool-icon remove remove-widget disabled" title="<g:message code="remove.selected.widget"/>"></span>
                        </div>
                    </div>
                    <div class="sidebar-group widget-ref-unit-selector" style="display: none">
                        <span class="sidebar-group-label"><g:message code="ref.position.unit"/></span>
                        <div class='sidebar-group-body'>
                            <select class="sidebar-input active-widget-ref-unit-selector">
                                <option value="px"><g:message code="pixel"/></option>
                                <option value="%"><g:message code="percent"/></option>
                            </select>
                        </div>
                    </div>
                    <g:if test="${!isAutoPage}">
                        <div class="widget-prop-configure">
                            <div class="tablike-button-group">
                                <div class="tablike-button basic">
                                    <g:message code="basic"/>
                                </div><div class="tablike-button css">
                                    <g:message code="css"/>
                                </div><div class="tablike-button js">
                                    <g:message code="js"/>
                                </div>
                            </div>
                            <div class="active-prop-view-container"></div>
                        </div>
                    </g:if>
                </div>
                <div class="layout-mode-header label-bar">
                    <label><g:message code="layout"/></label>
                    <span class="tool-icon toggle-icon"></span>
                </div>
                <div class="layout-mode accordion-item">
                    <div class="sidebar-group body-sections-selector">
                        <span class="sidebar-group-label"><g:message code="body.sections"/></span>
                        <div class="sidebar-group-body">
                            <select class="sidebar-input"></select>
                        </div>
                    </div>
                    <div class="sidebar-group section-tools">
                        <span class="tool-icon add add-section disabled" title='<g:message code="add.section.below"/>'></span>
                        <span class="tool-icon remove remove-section disabled" title='<g:message code="remove.selected.section"/>'></span>
                        <span class="tool-icon remove move-section-up disabled" title='<g:message code="move.section.up"/>'></span>
                        <span class="tool-icon remove move-section-down disabled" title='<g:message code="move.section.down"/>'></span>
                        <span class="tool-icon remove body-section-fluid-fixed-toggle disabled" title='<g:message code="fluid.fixed.width.toggle"/>'></span>
                    </div>
                    <div class="sidebar-group body-grids-selector">
                        <span class="sidebar-group-label"><g:message code="body.grids"/></span>
                        <div class="sidebar-group-body">
                            <select class="sidebar-input"></select>
                        </div>
                    </div>
                    <div class="sidebar-group layout-operators">
                        <span class="tool-icon l-fixed-layout disabled" title='<g:message code="splits.into.an.l.fixed.layout"/>'></span>
                        <span class="tool-icon r-fixed-layout disabled" title='<g:message code="splits.into.a.r.fixed.layout"/>'></span>
                        <span class="tool-icon fluid-splitted-layout disabled" title='<g:message code="splits.into.a.fluid.splitted.layout"/>'></span>
                        <span class="tool-icon v-split-layout disabled" title='<g:message code="splits.into.a.vertical.splitted.layout"/>'></span>
                        <span class="tool-icon h-f-c merge-left disabled" title='<g:message code="merge.left.splitted.part"/>'></span>
                        <span class="tool-icon h-f-c merge-right disabled" title='<g:message code="merge.right.splitted.part"/>'></span>
                        <span class='tool-icon h-f-c h-f-v fluid-in-resolutions disabled' title='<g:message code="fluid.in.resolutions"/>'></span>
                        <span class='tool-icon h-f-c h-f-v switch-width disabled' title='<g:message code="fluid.fixed.width.toggle"/>'></span>
                        <span class='tool-icon h-f-c hide-in-resolutions disabled' title='<g:message code="hidden.in.resolutions"/>'></span>
                        <span class='tool-icon h-f-c show-hide disabled' title='<g:message code="show.hide.toggle"/>'></span>
                        <span class='tool-icon fluid-v-split disabled' title='<g:message code="fluid.fixed.v-split.toggle"/>'></span>
                    </div>
                </div>
                <div class="page-props-header label-bar">
                    <label><g:message code="page.properties"/></label>
                    <span class="tool-icon toggle-icon"></span>
                </div>
                <div class="page-props accordion-item">
                    <div class="tablike-button-group">
                        <div class="tablike-button css">
                            <g:message code="css"/>
                        </div><div class="tablike-button js">
                            <g:message code="js"/>
                        </div>
                    </div>
                    <div class="active-prop-view-container"></div>
                </div>
                <div class="dock-css-header label-bar" style="display: none">
                    <label><g:message code="dock.css"/></label>
                    <span class="tool-icon toggle-icon"></span>
                </div>
                <div class="dock-css accordion-item">
                    <div class="dock-css-view-container"></div>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="app-tab-content-container body ${isResponsive && !noResponsive ? 'responsive' : 'non-responsive'}">
    <g:set var="editorFrameId" value="editor-frame-${StringUtil.uuid}"/>
    <div class="header">
        <g:if test="${!isAutoPage}">
            <div class="toolbar toolbar-left">
                <div class="tool-group search-box">
                    <g:if test="${!isPage || !layoutId}">
                        <span class="spinner-group">
                            <span class="spinner-label"><g:message code="short.section.width"/></span> - <input type="text" class="tiny width spinner" min="0" title="<g:message code="resize.width"/>">
                        </span>
                        <span class="spinner-group">
                            <span class="spinner-label"><g:message code="short.section.height"/></span> - <input type="text" class="tiny height spinner" min="100" title="<g:message code="resize.height"/>">
                        </span>
                    </g:if>
                    <span class="spinner-group">
                        <span class="spinner-label"><g:message code="short.left"/> - </span> <input type="text" class="tiny x-offset spinner" title="<g:message code="resize.widget.x.offset"/>">
                    </span>
                    <span class="spinner-group">
                        <span class="spinner-label"><g:message code="short.top"/> - </span><input type="text" class="tiny y-offset spinner" title="<g:message code="resize.widget.x.offset"/>">
                    </span>
                    <span class="spinner-group">
                        <span class="spinner-label"><g:message code="short.width"/>  - </span><input type="text" class="tiny widget-width spinner" min="50" title="<g:message code="resize.widget.width"/>">
                    </span>
                    <span class="spinner-group">
                        <span class="spinner-label"><g:message code="short.height"/> - </span><input type="text" class="tiny widget-height spinner" min="25" title="<g:message code="resize.widget.height"/>">
                    </span>
                </div>
            </div>
        </g:if>
        <div class="toolbar toolbar-right">
            <g:if test="${!isAutoPage && !(isPage && layoutId)}">
                <div class="tool-group multiselection">
                    <span class="toolbar-item align align-left disabled" title="<g:message code="align.left"/>"><i></i></span>
                    <span class="toolbar-item align align-right disabled" title="<g:message code="align.right"/>"><i></i></span>
                    <span class="toolbar-item align align-top disabled" title="<g:message code="align.top"/>"><i></i></span>
                    <span class="toolbar-item align align-bottom disabled" title="<g:message code="align.bottom"/>"><i></i></span>
                </div>
                <div class="tool-group multiselection">
                    <span class="toolbar-item engroup disabled" title="<g:message code="engroup.them"/>"><i></i></span>
                    <span class="toolbar-item ungroup disabled" title="<g:message code="ungroup.them"/>"><i></i></span>
                </div>
            </g:if>
            <div class="tool-group">
                <span class="toolbar-item undo disabled" title="<g:message code="undo"/>"><i></i></span>
                <span class="toolbar-item redo disabled" title="<g:message code="redo"/>"><i></i></span>
            </div>
            <div class="tool-group">
                <g:if  test="${isPage && pageFlag}" >
                    <span class="toolbar-item view-site tooltipstered" url="${page.url.encodeAsBMHTML()}"><i></i></span>
                </g:if>
            </div>
            <div class="tool-group toolbar-btn save disabled"><g:message code="save"/></div>
            <div class="tool-group action-tool action-menu">
                <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper collapsed"></span>
            </div>
        </div>
    </div>
    <iframe class="content-editor-iframe" id="${editorFrameId}" src="${editorUrl}&editorFrameId=${editorFrameId}"></iframe>
</div>