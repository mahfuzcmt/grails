<div class="header" xmlns="http://www.w3.org/1999/html">
    <span class="item-group title">
        <g:message code="report"/>
    </span>
    <div class="toolbar toolbar-right">
        <label><g:message code="select.report"/></label>
        <g:select class="large report-type" name="report-type" from="${['status', 'customer', 'billing.address', 'shipping.address', 'period'].collectEntries({[(it): g.message(code: 'orders.by.' + it)]})}" optionKey="${{it.key}}" optionValue="${{it.value}}" value="${reportType}"  />
        <g:select class="medium date-filter" name="filter" from="${['today', 'yesterday', 'last.hours', 'last.seven.days', 'last.thirty.days', 'this.month', 'custom'].collectEntries({[(it): g.message(code: it)]})}" optionKey="${{it.key}}" optionValue="${{it.value}}" value="" />
        <div class="tool-group">
            <span class="toolbar-item export" title="<g:message code="export"/>"><i></i></span>
            <span class="toolbar-item add-favourite" title="<g:message code="add.to.favourite"/>"><i></i></span>
            <span class="toolbar-item favourite" title="<g:message code="favourite"/>"><i></i></span>
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <span class="title">
        <g:message code="${'orders.by.' + reportType}"/>
    </span>
    <div class="toolbar toolbar-right">
        <span class="action-menu action-tool">
            <sapn class="chart-option">Select Chart Option</sapn>
        </span>
        <span class="action-dropper collapsed"></span>
    </div>
    <div class="tabular-data">
        <g:if test="${reportType == 'status'}">
            <div class="chart">
                <reporting:order_by_status type="${params.chartType ?: 'bar'}"/>
                <reporting:chart/>
            </div>
            <div class="table">
                <g:include controller = "commanderReporting" action = "loadOrderByStatus"/>
            </div>
        </g:if>
        <g:if test="${reportType == 'customer'}">
            <g:include controller = "commanderReporting" action = "loadOrderByCustomer"/>
        </g:if>
        <g:if test="${reportType == 'billing.address'}">
            <g:include controller = "commanderReporting" action = "loadOrederByBillingAddress"/>
        </g:if>
        <g:if test="${reportType == 'shipping.address'}">
            <g:include controller = "commanderReporting" action = "loadOrderByShipping"/>
        </g:if>
        <g:if test="${reportType == 'period'}">
            <g:include controller = "commanderReporting" action = "loadProductByCustomer"/>
        </g:if>
    </div>
</div>

