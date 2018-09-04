<div class="gallery-config-view">
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="responsive"/></label>
            <input type="checkbox" value="true"class="single" name="responsive" uncheck-value="false" ${widgetConfig.responsive == "true" ? "checked"  : ""} toggle-target="multiple-item-config">
        </div><div class="form-row">
            <label><g:message code="responsive.refresh.rate"/></label>
            <input type="text" name="responsive_refresh_rate" value="${widgetConfig.responsive_refresh_rate ?: 200}" validation="required" restrict="numeric">
        </div>
    </div>
    <div class="form-row">
        <label><g:message code="items"/></label>
        <input type="text" name="items" value="${widgetConfig.items ?: 5}" restrict="numeric" validation="skip@if{self::hidden} required gt[0]">
    </div>
    <div class="multiple-item-config">
        <div class="form-row">
            <label><g:message code="items.mobile"/></label>
            <input type="checkbox" value="true" class="single" name="items_mobile" uncheck-value="false" ${widgetConfig.items_mobile == "false" ?  "": "checked"} toggle-target="items-mobile">
        </div>
        <div class="double-input-row items-mobile">
            <div class="form-row">
                <label><g:message code="min.width"/></label>
                <input type="text" name="items_mobile_max_width" value="${widgetConfig.items_mobile_max_width ?: 479}" restrict="numeric" validation="skip@if{self::hidden} required">
            </div><div class="form-row">
                <label><g:message code="no.of.item"/></label>
                <input type="text" name="items_mobile_no_of_item" value="${widgetConfig.items_mobile_no_of_item ?: 1}" restrict="numeric" validation="skip@if{self::hidden} required gt[0]">
            </div>
        </div>
        <div class="form-row">
            <label><g:message code="items.tablet.small"/></label>
            <input type="checkbox" value="true" class="single" name="items_tablet_small" uncheck-value="false" ${widgetConfig.items_tablet_small == "true" ?  "checked": ""} toggle-target="items-tablet-small">
        </div>
        <div class="double-input-row items-tablet-small">
            <div class="form-row">
                <label><g:message code="min.width"/></label>
                <input type="text" name="items_tablet_small_max_width" value="${widgetConfig.items_tablet_small_max_width}" restrict="numeric" validation="skip@if{self::hidden} required">
            </div><div class="form-row">
                <label><g:message code="no.of.item"/></label>
                <input type="text" name="items_tablet_small_no_of_item" value="${widgetConfig.items_tablet_small_no_of_item}" restrict="numeric" validation="skip@if{self::hidden} required gt[0]">
            </div>
        </div>
        <div class="form-row">
            <label><g:message code="items.tablet"/></label>
            <input type="checkbox" value="true" class="single" name="items_tablet" uncheck-value="false" ${widgetConfig.items_tablet == "false" ?  "": "checked"} toggle-target="items-tablet">
        </div>
        <div class="double-input-row items-tablet">
            <div class="form-row">
                <label><g:message code="min.width"/></label>
                <input type="text" name="items_tablet_max_width" value="${widgetConfig.items_tablet_max_width ?: 768}" restrict="numeric" validation="skip@if{self::hidden} required">
            </div><div class="form-row">
                <label><g:message code="no.of.item"/></label>
                <input type="text" name="items_tablet_no_of_item" value="${widgetConfig.items_tablet_no_of_item ?: 2}" restrict="numeric" validation="skip@if{self::hidden} required gt[0]">
            </div>
        </div>
        <div class="form-row">
            <label><g:message code="items.desktop.small"/></label>
            <input type="checkbox" value="true" class="single" name="items_desktop_small" uncheck-value="false" ${widgetConfig.items_desktop_small == "false" ?  "": "checked"} toggle-target="items-desktop-small">
        </div>
        <div class="double-input-row items-desktop-small">
            <div class="form-row">
                <label><g:message code="min.width"/></label>
                <input type="text" name="items_desktop_small_max_width" value="${widgetConfig.items_desktop_small_max_width ?: 979}" restrict="numeric" validation="skip@if{self::hidden} required">
            </div><div class="form-row">
                <label><g:message code="no.of.item"/></label>
                <input type="text" name="items_desktop_small_no_of_item" value="${widgetConfig.items_desktop_small_no_of_item ?: 3}" restrict="numeric" validation="skip@if{self::hidden} required gt[0]">
            </div>
        </div>
        <div class="form-row">
            <label><g:message code="items.desktop"/></label>
            <input type="checkbox" value="true" class="single" name="items_desktop" uncheck-value="false" ${widgetConfig.items_desktop == "false" ?  "": "checked"} toggle-target="items-desktop">
        </div>
        <div class="double-input-row items-desktop">
            <div class="form-row">
                <label><g:message code="min.width"/></label>
                <input type="text" name="items_desktop_max_width" value="${widgetConfig.items_desktop_max_width ?: 1199}" restrict="numeric" validation="skip@if{self::hidden} required">
            </div><div class="form-row">
                <label><g:message code="no.of.item"/></label>
                <input type="text" name="items_desktop_no_of_item" value="${widgetConfig.items_desktop_no_of_item ?: 4}" restrict="numeric" validation="skip@if{self::hidden} required gt[0]">
            </div>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="margin"/> </label>
            <input type="text" name="margin" value="${widgetConfig.margin ?: 10}" restrict="numeric" validation="required">
        </div><div class="form-row">
            <label><g:message code="pagination.speed"/><span class="suggestion">(ms)</span></label>
            <input type="text" name="pagination_speed" value="${widgetConfig.pagination_speed ?: 800}" restrict="numeric" validation="required">
        </div>
    </div>

    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="auto.play"/></label>
            <input type="checkbox" value="true" name="auto_play" class="single" uncheck-value="false"  ${widgetConfig.auto_play == "true" ? "checked" : ""} toggle-target="auto-play">
        </div><div class="form-row auto-play">
            <label><g:message code="auto.play.timeout"/><span class="suggestion">(ms)</span></label>
            <input type="text" name="autoplayTimeout" value="${widgetConfig.autoplayTimeout ?: 1000}" restrict="numeric" validation="skip@if{self::hidden required}">
        </div>
    </div>

    <div class="form-row">
        <label><g:message code="navigation"/></label>
        <input type="checkbox" value="true" class="single" name="navigation" uncheck-value="false" ${widgetConfig.navigation == "false" ?  "": "checked"} toggle-target="navigation-dependent">
    </div>
    <div class="double-input-row navigation-dependent">
        <div class="form-row">
            <label><g:message code="pre.button.text"/></label>
            <input type="text" name="pre_button_text" value="${widgetConfig.pre_button_text ?: "prev"}"  validation="skip@if{self::hidden} required">
        </div><div class="form-row">
            <label><g:message code="next.button.text"/></label>
            <input type="text" name="next_button_text" value="${widgetConfig.next_button_text ?: "next"}"  validation="skip@if{self::hidden} required">
        </div>
    </div>

    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="pause.on.hover"/></label>
            <input type="checkbox" value="true" class="single" name="stop_on_over" uncheck-value="false" ${widgetConfig.stop_on_over == "false" ?  "": "checked"} >
        </div><div class="form-row">
            <label><g:message code="lazy.load"/></label>
            <input type="checkbox" value="true" class="single" name="lazy_load" uncheck-value="false" ${widgetConfig.lazy_load == "true" ? "checked"  : ""} >
        </div>
    </div>

    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="pagination"/></label>
            <input type="checkbox" value="true" class="single" name="pagination" uncheck-value="false" ${widgetConfig.pagination == "true" ? "checked"  : ""} >
        </div><div class="form-row">
            <label><g:message code="pagination.numbers"/></label>
            <input type="checkbox" value="true" class="single" name="pagination_numbers" uncheck-value="false" ${widgetConfig.pagination_numbers == "true" ? "checked"  : ""} >
        </div>
    </div>
    <div class="button-line">
        <button type="button" class="previous"><g:message code="previous"/></button>
        <button type="submit" class="edit-popup-form-submit submit-button apply"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>
