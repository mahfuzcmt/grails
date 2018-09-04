<%@ page import="com.webcommander.plugin.news.NewsConstants; com.webcommander.util.StringUtil;" %>
<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="news.transition"/></div>
        <div class="sidebar-group-body">
            <g:set var="trId" value="${com.webcommander.util.StringUtil.uuid}"/>
            <ui:namedSelect id="${trId}" class="sidebar-input" toggle-target="display" name="news_transition" key="${NewsConstants.NEWS_WIDGET_TRANSITION_MESSAGE_KEYS}" value="${config["news_transition"]}"/>
        </div>
    </div>
    <div class="sidebar-group display-${NewsConstants.NEWS_WIDGET_TRANSITION.HORIZONTAL_SCROLL} display-${NewsConstants.NEWS_WIDGET_TRANSITION.VERTICAL_SCROLL} display-${NewsConstants.NEWS_WIDGET_TRANSITION.FADE}">
        <div class="sidebar-group-label"><g:message code="height"/></div>
        <div class="sidebar-group-body">
            <input validation="skip@if{global:#${trId} option[value='${NewsConstants.NEWS_WIDGET_TRANSITION.NO_TRANSITION}']:selected} required digits" restrict="numeric" type="text" class="sidebar-input" name="height" value="${config.height}" depends="#${trId}">
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="no.of.character"/></div>
        <div class="sidebar-group-body">
            <input type="text" class="sidebar-input" name="noOfChar" restrict="numeric" validation="number" value="${config.noOfChar ?: "150"}">
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="transition.speed"/></div>
        <div class="sidebar-group-body">
            <input type="text" class="small" class="sidebar-input" name="transition_speed" value="${config.transition_speed}">
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="transition.direction"/></div>
        <div class="sidebar-group-body">
            <ui:namedSelect class="sidebar-input" name="transition_direction" key="${NewsConstants.NEWS_WIDGET_TRANSITION_DIRECTION_MESSAGE_KEYS}" value="${config["transition_direction"]}" />
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="news.per.phase"/></div>
        <div class="sidebar-group-body">
            <input type="text" name="news_per_phase" class="sidebar-input" value="${config.news_per_phase}" validation="digits" restrict="numeric" maxlength="9">
        </div>
    </div>
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="news.selection" /></div>
        <div class="sidebar-group-body">
            <div>
                <input type="radio" class="news-date-range" name="selection" value="date_range" ${config.selection == "date_range" ? "checked" : ""} toggle-target="datefield-between">
                <label><g:message code="date.range"/></label>
            </div>
            <div class="datefield-between date-range" style="display: ${config.selection =="date_range" ? "" : "none"}">
                <input type="text" class="datefield-from smaller" name="news_from" value="${config.news_from ? config.news_from : ""}" validation="skip@if{self::hidden} required date">
                &nbsp; - &nbsp;
                <input type="text" class="datefield-to smaller" name="news_to" value="${config.news_to ? config.news_to :""}" validation="date">
            </div>
            <div>
                <input type="radio" name="selection" value="last_one_week" ${config.selection == "last_one_week" ? "checked" : ""}>
                <label><g:message code="last.one.week"/></label>
            </div>
            <div>
                <input type="radio" name="selection" value="last_one_month" ${config.selection == "last_one_month" ? "checked" : "" } >
                <label><g:message code="last.one.month"/></label>
            </div>
            <div>
                <input type="radio" name="selection" value="all" ${config.selection == "all" ? "checked" : ""} >
                <label><g:message code="all"/></label>
            </div>
        </div>
    </div>
</g:applyLayout>