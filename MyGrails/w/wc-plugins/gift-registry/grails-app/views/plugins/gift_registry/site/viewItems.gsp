<div class="gift-registry-items">
    <h3><g:message code="product.added.to.registry" args="${[registry.name]}"/> </h3>
    <table>
        <colgroup>
            <col class="product-name"/>
            <col class="quantity"/>
            <col class="action"/>
        </colgroup>
        <tr>
            <th><g:message code="product.name"/></th>
            <th><g:message code="quantity"/></th>
            <th><g:message code="action"/></th>
        </tr>
        <g:each in="${registry.giftItems}" var="item">
            <tr>
               <td><div class="wrapper" data-label="<g:message code="product.name"/>:">${(item.product.name + (item.variations ? " ( " + item.variations.join(", ").encodeAsBMHTML() + " )" : "")).encodeAsBMHTML()}</div></td>
               <td><div class="wrapper" data-label="<g:message code="quantity"/>:">${item.quantity}</div></td>
               <td>
                   <div class="wrapper" data-label="<g:message code="action"/>:">
                       <span class="action-icon remove delete" item-id="${item.id}" title="<g:message code="remove"/>"></span>
                   </div>
               </td>
            </tr>
        </g:each>
        <g:if test="${registry.giftItems.size() == 0}">
            <tr>
                <td colspan="3">
                    <span class="no-data"><g:message code="no.product.added.to.gift.registry"/></span>
                </td>
            </tr>
        </g:if>
    </table>
    <div class="form-row btn-row">
        <button type="button" class="cancel-button"><g:message code="back"/></button>
    </div>
</div>