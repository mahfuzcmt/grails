<g:form action="${app.relativeBaseUrl()}commaderReporting/save" method="post" class="edit-popup-form">
    <div class="multi-column three-column">
        <div class="columns first-column">
            <span>X Axis</span>
            <g:radioGroup values="${chartOptionList}" name="x-axis" labels="${chartOptionList}" value="[1,2,3,4,5,6]">
                <p>${it.radio} ${message(code: it.label)}</p>
            </g:radioGroup>
        </div><div class="columns">
        <span>Y Axis</span>
        <g:radioGroup values="['sale','discount','gift','tax','cancel','count']" name="y-axis" labels="['Total Sales','Total Discounts', 'Total Gift Cards Used', 'Total Taxes', 'Total Cancelled', 'Order Count']" value="[1,2,3,4,4,5,6]">
            <p>${it.radio} ${message(code: it.label)}</p>
        </g:radioGroup>
        </div><div class="columns last-column">
            <span>Chart Type</span>
            <g:radioGroup name="chart-type" values="['column','bar','pie','donut','line']" labels="['Column','Bar','Pie','Donut','Line']" value="[1,2,3,4,5]">
                <p>${it.radio} ${message(code: it.label)}</p>
            </g:radioGroup>
        </div>
    </div>
</g:form>