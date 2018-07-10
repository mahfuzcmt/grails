<%@ page import="com.webcommander.plugin.PluginManager" %>
<div class="header multi-tab-shared-header">
    <span class="header-title"></span>
    <div class="left-tab-view-toolbar toolbar toolbar-right datefield-between">
        <label><g:message code="view.chart.between"/></label>
        <input type="text" class="datefield-from  medium from-date" value="${(new Date()-30).format("yyyy-MM-dd")}"> &nbsp; &nbsp; - &nbsp;<input type="text" class="datefield-to medium to-date" value="${new Date().format("yyyy-MM-dd")}">
        <g:select class="analytics-type" name="chart-type" from="${['hourly', 'daily' ,'weekly','monthly'].collectEntries({[(it): g.message(code: it)]})}" optionKey="${{it.key}}" optionValue="${{it.value}}" value="hourly"/>
        <g:select class="small chart-type" name="chart-type" from="${['bar', 'line' ,'radar'].collectEntries({[(it): g.message(code: it + '.chart')]})}" optionKey="${{it.key}}" optionValue="${{it.value}}" value="line"/>
        <div class="tool-group action-tool action-menu">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper collapsed"></span>
        </div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="bmui-tab left-side-header" >
    <div class="bmui-tab-header-container">
        <div class="bmui-tab-header" data-tabify-tab-id="summary" data-tabify-url="${app.relativeBaseUrl()}googleAnalytics/loadAnalytics?property=summary">
            <span class="title"><g:message code="summary"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="engagement" data-tabify-url="${app.relativeBaseUrl()}googleAnalytics/loadAnalytics?property=engagement">
            <span class="title"><g:message code="engahement"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="keyword" data-tabify-url="${app.relativeBaseUrl()}googleAnalytics/loadAnalytics?property=keyword">
            <span class="title"><g:message code="keyword"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="direct-traffic" data-tabify-url="${app.relativeBaseUrl()}googleAnalytics/loadAnalytics?property=direct-traffic">
            <span class="title"><g:message code="direct.traffic"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="referral-search-traffic" data-tabify-url="${app.relativeBaseUrl()}googleAnalytics/loadAnalytics?property=referral-search-traffic">
            <span class="title"><g:message code="referral.search.traffic"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="organic-search-traffic" data-tabify-url="${app.relativeBaseUrl()}googleAnalytics/loadAnalytics?property=organic-search-traffic">
            <span class="title"><g:message code="organic.search.traffic"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="paid-search-traffic" data-tabify-url="${app.relativeBaseUrl()}googleAnalytics/loadAnalytics?property=paid-search-traffic">
            <span class="title"><g:message code="paid.search.traffic"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="social-media-traffic" data-tabify-url="${app.relativeBaseUrl()}googleAnalytics/loadAnalytics?property=social-media-traffic">
            <span class="title"><g:message code="social.media.traffic"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="page-view" data-tabify-url="${app.relativeBaseUrl()}googleAnalytics/loadAnalytics?property=page-view">
            <span class="title"><g:message code="page.view"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="location" data-tabify-url="${app.relativeBaseUrl()}googleAnalytics/loadAnalytics?property=location">
            <span class="title"><g:message code="location"/></span>
        </div>
    </div>
    <div class="bmui-tab-body-container">
        <div id="bmui-tab-summary">
        </div>
        <div id="bmui-tab-engagement">
        </div>
        <div id="bmui-tab-keyword">
        </div>
        <div id="bmui-tab-direct-traffic">
        </div>
        <div id="bmui-tab-referral-search-traffic">
        </div>
        <div id="bmui-tab-organic-search-traffic">
        </div>
        <div id="bmui-tab-paid-search-traffic">
        </div>
        <div id="bmui-tab-social-media-traffic">
        </div>
        <div id="bmui-tab-page-view">
        </div>
        <div id="bmui-tab-location">
        </div>
    </div>
</div>

