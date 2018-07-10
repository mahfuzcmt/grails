<g:if test="${fields.size()}">
    <g:applyLayout name="_productwidget">
        <g:include view="plugins/product_custom_fields/customFieldBlock.gsp" model="${pageScope.variables}"/>
    </g:applyLayout>
</g:if>