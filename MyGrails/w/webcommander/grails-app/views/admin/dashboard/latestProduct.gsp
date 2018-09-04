<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.constants.NamedConstants; com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<div class="dashlet-latest-statistics">
    <g:if test="${DomainConstants.ECOMMERCE_DASHLET_CHECKLIST["latest_product"] && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')}">
        <div class="product-data-table">
            <g:if test="${latestSolds.size() > 0}">
                <g:include view="admin/reporting/latestProductSold.gsp" model="${[latestSolds: latestSolds]}"/>
            </g:if>
            <g:else>
                <tr class="table-no-entry-row"><td colspan="5" ><g:message code="no.product.sold"/></td></tr>
            </g:else>
            </table>
        </div>
    </g:if>
</div>