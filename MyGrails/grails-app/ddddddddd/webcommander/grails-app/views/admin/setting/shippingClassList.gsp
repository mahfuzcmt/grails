<div class="shipping-class">
    <div class="shipping-classes">
        <g:each in="${clazzs}" var="clazz">
            <div class="shipping-class-item ${shippingSettings.default_shipping_class == clazz.id.toString() ? 'default' : '' }" class-id="${clazz.id}">
                <div class="shipping-data">
                    <span class="shipping-class-actions"></span><div class="name">${clazz.name}</div><div class="description">${clazz.description}</div>
                </div>
            </div>
        </g:each>
    </div>
    <div class="shipping-class-form-template" action="${app.relativeBaseUrl()}" style="display: none">
        <input class="id" type="hidden" name="id" value=""/>
        <div class="form-row">
            <label><g:message code="class.name"/></label>
            <input type="text" name="name" class="class-name unique" validation="skip@if{self::hidden} required maxlength[100]"  maxlength="100" unique-action="shippingAdmin/isShippingClassUnique">
        </div>

        <div class="form-row"><label><g:message code="description"/></label>
            <input type="text" class="class-description" validation="maxlength[250]" maxlength="250">
        </div>
        <div class="button-line">
            <button class="submit-button" type="submit"><g:message code="save"/></button>
            <button class="cancel-button" type="button"><g:message code="cancel"/></button>
        </div>
    </div>

    <div class="form-row add-new-wrapper">
        <button class="add-new-button" type="button"><g:message code="add.new.class"/></button>
    </div>
</div>