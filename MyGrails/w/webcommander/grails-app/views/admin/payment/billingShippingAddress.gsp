<table class="billing-shipping-address-table" border="0" cellspacing="0" cellpadding="0" style="table-layout: fixed; margin: 50px; width: 600px;">
    <tr>
        <colgroup>
            <col style="width: 33%">
         </colgroup>
         <tr class="order_info_title">
            <g:if test="${billing != null}">
                <td><g:message code="billing.address"/></td>
            </g:if>
            <g:else>
                <td><g:message code="shipping.address"/></td>
            </g:else>
         </tr>
        <tr class="order_info_details">
            <g:if test="${billing != null}">
                <td valign="top">
                    <p><strong>
                        ${billing.firstName.encodeAsBMHTML() + (billing.lastName ? " " + billing.lastName.encodeAsBMHTML() : "")}</strong><br/>
                        ${billing.addressLine1.encodeAsBMHTML()}<br/>
                        <g:if test="${billing.addressLine2}">
                            ${billing.addressLine2.encodeAsBMHTML()}<br/>
                        </g:if>
                        <g:if test="${!billing.state}">
                            ${billing.city ? billing.city.encodeAsBMHTML() + ", " : ""} ${billing.postCode.encodeAsBMHTML()}<br/>
                        </g:if>
                        <g:else>
                            ${billing.city ? billing.city.encodeAsBMHTML() + ", " : ""} ${billing.state ? billing.state.code + ", "
                                : ""} ${billing.postCode ? billing.postCode : ""}<br/>
                        </g:else>
                        ${billing.country?.name.encodeAsBMHTML()}<br/>
                        ${billing.email.encodeAsBMHTML()} <br/>
                        <g:if test="${billing.phone}">
                            ${billing.phone.encodeAsBMHTML()}<br/>
                        </g:if>
                        <g:if test="${billing.fax}">
                            ${billing.fax.encodeAsBMHTML()}<br/>
                        </g:if>
                    </p>
                </td>
            </g:if>
            <g:else>
                <td valign="top">
                    <p>
                        <g:if test="${shipping}">
                            <strong>${shipping.firstName.encodeAsBMHTML() + (shipping.lastName ? " " + shipping.lastName.encodeAsBMHTML() : "")}</strong><br/>
                            ${shipping.addressLine1.encodeAsBMHTML()}<br/>
                            <g:if test="${shipping.addressLine2}">
                                ${shipping.addressLine2.encodeAsBMHTML()}<br/>
                            </g:if>
                            <g:if test="${!shipping.state}">
                                ${shipping.city ? shipping.city.encodeAsBMHTML() + ", " : ""} ${shipping.postCode.encodeAsBMHTML()}<br/>
                            </g:if>
                            <g:else>
                                ${shipping.city ? shipping.city.encodeAsBMHTML() + ", " : ""} ${shipping.state.code ?
                                    shipping.state.code + ", " : ""}  ${shipping.postCode}<br/>
                            </g:else>
                            ${shipping.country?.name.encodeAsBMHTML()}<br/>
                            ${shipping.email.encodeAsBMHTML()} <br/>
                            <g:if test="${shipping.phone}">
                                ${shipping.phone.encodeAsBMHTML()}<br/>
                            </g:if>
                            <g:if test="${shipping.fax}">
                                ${shipping.fax.encodeAsBMHTML()}<br/>
                            </g:if>
                        </g:if>
                    </p>
                </td>
            </g:else>
        </tr>
</table>
