<%@ page import="com.webcommander.util.StringUtil; com.webcommander.constants.NamedConstants; com.webcommander.constants.DomainConstants; com.webcommander.manager.HookManager " %>
<form action="${app.relativeBaseUrl()}order/saveShipment" method="post" class="create-edit-form edit-popup-form">
    <input type="hidden" name="order" value="${order}">
    <input type="hidden" name="shipmentId" value="${shipment?.id}">
    <table class="content add-shipment-table">
        <colgroup>
            <col style="width: 25%">
            <col style="width: 25%">
            <col style="width: 25%">
            <col style="width: 25%">
        </colgroup>
        <tr>
            <th><g:message code="name"/></th>
            <th><g:message code="undelivered.quantity"/></th>
            <g:if test="${shipment}"><th><g:message code="previous.quantity"/></th></g:if>
            <th><g:message code="shipment.quantity"/></th>
        </tr>
        <g:set var="remainingToShip" value="${0}"/>
        <g:each in="${items}" var="item" status="i">
            <tr>
                <g:set var="shipped" value="${shippedQuantity[i] == null ? 0 : shippedQuantity[i].deliveredQuantity}"/>
                <g:if test="${item.quantity > shipped}">
                    <g:set var="remainingToShip" value="${remainingToShip + 1}"/>
                </g:if>
                <td>${item.productName.encodeAsBMHTML()}</td>
                <td>${item.quantity - shipped}</td>
                <g:if test="${shipment}">
                    <g:set var="shippedItem" value="${shippedItems?.find(){it.orderItem.id == item.id}}"/>
                    <td>${shippedItem.quantity}</td>
                </g:if>
                <td>
                    <g:set var="id1" value="${StringUtil.uuid}"/>
                    <input type="hidden" id="${id1}" name="remaining" value="${shippedItem? (item.quantity - shipped + shippedItem.quantity) : (item.quantity - shipped)}">
                    <input type="hidden" name="orderItem" value="${item.id}">
                    <input type="text" class="td-full-width" name="quantity" restrict="numeric" ${!(shippedItem? (item.quantity - shipped + shippedItem.quantity) : (item.quantity - shipped))? "readonly" : ""} maxlength="9" validation="compare[${id1}, number, lte]" depends="#${id1}">
                </td>
            </tr>
        </g:each>
    </table>

    <input type="hidden" name="remainingToShip" value="${remainingToShip}">
    <div class="">
        <plugin:hookTag hookPoint="addShipmentBlock">
            <div class="form-row chosen-wrapper">
                <label><g:message code="shipping.method"/></label>
                <g:select toggle-target="shipping" from="${NamedConstants.SHIPPING_METHOD.values().collect {g.message(code: it)}}"  keys="${NamedConstants.SHIPPING_METHOD.values()}" class="shipping-method large" name="method"/>
            </div>
            <div class="form-row shipping-australianpost shipping-others">
                <label><g:message code="track.info"/></label>
                <input type="text" class="large" name="trackingInfo">
            </div>
            <div class="form-row mandatory">
                <label><g:message code="shipping.date"/></label>
                <input type="text" class="large timefield" no-next="false" name="shippingDate" validation="required" validate-on="call-only">
            </div>
            <g:if test="${shipment}">
                <div class="form-row mandatory">
                    <label><g:message code="change.note"/></label>
                    <textarea type="text" class="" no-next="false" name="changeNote" validation="required maxlength[500]" validate-on="call-only"></textarea>
                </div>
            </g:if>
        </plugin:hookTag>
    </div>
    <div class="button-line">
        <button type="submit" class="edit-popup-form-submit submit-button"><g:message code="submit"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>