<%@ page import="org.apache.commons.io.FilenameUtils; com.webcommander.plugin.general_event.constants.DomainConstants; com.webcommander.plugin.general_event.constants.NamedConstants; com.webcommander.webcommerce.TaxProfile" %>
<form action="${app.relativeBaseUrl()}generalEventAdmin/saveEvent" method="post" class="create-edit-form downloadable-spec-form" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${event?.id}">
    <g:set var="weekList" value="${['First', 'Second', 'Third', 'Fourth']}" />
    <g:set var="dayList" value="${['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']}" />
    <g:set var="monthList" value="${['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']}" />
    <div class="bmui-tab">
        <div class="bmui-tab-header-container top-side-header">
            <div class="bmui-tab-header" data-tabify-tab-id="general">
                <span class="title"><g:message code="general"/></span>
            </div>
            <div class="bmui-tab-header" data-tabify-tab-id="metatag">
                <span class="title"><g:message code="meta.tag"/></span>
            </div>
        </div>
        <div class="bmui-tab-body-container">
            <div id="bmui-tab-general">
                <div class="form-section">
                    <div class="form-section-info">
                        <h3><g:message code="event.info"/></h3>
                        <div class="info-content"><g:message code="form.section.text.event.info"/></div>
                    </div>
                    <div class="form-section-container">
                        <div class="double-input-row">
                            <div class="form-row mandatory">
                                <label><g:message code="event.name"/></label>
                                <input type="text" name="name" class="large" value="${event.name}" validation="required rangelength[2, 100]" maxlength="100" />
                            </div><div class="form-row chosen-wrapper">
                                <label><g:message code="title"/><span class="suggestion"><g:message code="suggestion.event.title"/></span></label>
                                <input type="text" name="title" class="large" value="${event.title.encodeAsBMHTML()}" validation="maxlength[200]" maxlength="200" />
                            </div>
                        </div>
                        <div class="double-input-row">
                            <div class="form-row">
                                <label><g:message code="heading"/></label>
                                <input type="text" name="heading" class="large" value="${event.heading.encodeAsBMHTML()}" validation="maxlength[200]" maxlength="200" />
                            </div><div class="form-row">
                                <label>&nbsp;</label>
                                <input class="single" type="checkbox" name="isPublic" value="true" uncheck-value="false" ${event.isPublic ? "checked='checked'" : ""} />
                                <g:message code="public"/>
                            </div>
                        </div>
                        <div class="personalized-program">
                            <div class="form-row">
                                <label><g:message code="personalized.program"/></label>
                                <div class="thicker-row">
                                    <input name="file" type="file" size-limit="9097152" class="large" text-helper="no" />
                                </div>
                            </div>
                            <div class="form-row">
                                <label></label>
                                <div class="personalized-file-block file-selection-queue">
                                    <g:if test="${event.file}">
                                        <span class="file ${event.file.substring(event.file.lastIndexOf(".") + 1, event.file.length())}">
                                            <span class="tree-icon"></span>
                                        </span>
                                        <span class="name">${event.file}</span>
                                        <span class="tool-icon remove" file-name="${event.file}"></span>
                                    </g:if>
                                </div>
                            </div>
                        </div>
                        <div class="form-row chosen-wrapper">
                            <label>&nbsp;</label>
                            <label><g:message code="recurring"/></label>
                            <g:select class="single" toggle-target="form-row-recurring" name="isRecurring" from="${['No', 'Yes']}" value="${event.isRecurring.toBoolean()? 1 : 0}" keys="${[0,1]}" ></g:select>
                        </div>
                        <div class="double-input-row form-row-recurring-0">
                            <div class="form-row mandatory">
                                <label><g:message code="start.time"/></label>
                                <g:set var="startTime" value="${UUID.randomUUID().toString()}"/>
                                <input type="text" id="${startTime}" class="timefield large" name="startDateTime" value="${event.startDateTime?.toDatePickerFormat(true, session.timezone)}" validation="skip@if{self::hidden} required" />
                            </div><div class="form-row mandatory">
                                <label><g:message code="end.time"/></label>
                                <input type="text" class="timefield large" name="endDateTime" value="${event.endDateTime?.toDatePickerFormat(true, session.timezone)}" validation="skip@if{self::hidden} required compare[${startTime}, date, gt]" depends="#${startTime}"/>
                            </div>
                        </div>
                        <div class="form-row-recurring-1">
                            <div class="double-input-row mandatory">
                                <div class="mandatory form-row mandatory-chosen-wrapper">
                                    <span><g:message code="start.time"/></span>
                                    <g:select name="eventStartTime" from="${timeList}"></g:select>
                                </div><div class="mandatory form-row mandatory-chosen-wrapper">
                                    <span><g:message code="end.time"/></span>
                                    <g:select name="eventEndTime" from="${timeList}"></g:select>
                                </div>
                            </div>
                            <div class="form-row">
                                <label><g:message code="recurrence.pattern"/></label>
                                <input type="radio" toggle-target="recurrence-type-daily" name="recurrencePattern" value="daily" ${event.recurrencePattern == DomainConstants.RECURRENCE_PATTERN.DAILY? "checked = 'true'" : ""} /><span><g:message code="daily" /></span>
                                <input type="radio" toggle-target="recurrence-type-weekly" name="recurrencePattern" value="weekly" ${event.recurrencePattern == DomainConstants.RECURRENCE_PATTERN.WEEKLY? "checked = 'true'" : ""} /><span><g:message code="weekly" /></span>
                                <input type="radio" toggle-target="recurrence-type-monthly" name="recurrencePattern" value="monthly" ${event.recurrencePattern == DomainConstants.RECURRENCE_PATTERN.MONTHLY? "checked = 'true'" : ""} /><span><g:message code="monthly" /></span>
                                <input type="radio" toggle-target="recurrence-type-yearly" name="recurrencePattern" value="yearly" ${event.recurrencePattern == DomainConstants.RECURRENCE_PATTERN.YEARLY? "checked = 'true'" : ""} /><span><g:message code="yearly" /></span>
                            </div>

                            <div class="double-input-row recurrence-type-daily">
                                <div class="form-row">
                                    <input type="radio" toggle-target="daily-event" name="dailyRecurrenceType" value="every" ${event.dailyRecurrenceType == DomainConstants.DAILY_RECURRENCE_TYPE.EVERY? "checked = 'true'" : ""} /><span><g:message code="every" /></span>
                                </div><div class="form-row daily-event">
                                    <span><g:message code="days"/></span>
                                    <div class="large multitxtchosen" data-placeholder="<g:message code="enter.event.dates"/>" chosen-validation='match[(^\d{1,2}(-\d{1,2})?$)] dayCheck[]' name="dailyEvents">
                                        <g:each in="${event?.dailyEvents}" var="dailyEvent">
                                            <input type="hidden" name="dailyEvents" value="${dailyEvent}">
                                        </g:each>
                                    </div>
                                </div>
                            </div>
                            <div class="form-row recurrence-type-daily">
                                <input type="radio" name="dailyRecurrenceType" value="everyday" ${event.dailyRecurrenceType == DomainConstants.DAILY_RECURRENCE_TYPE.EVERYDAY? "checked = 'true'" : ""} /><span><g:message code="every.week.day" /></span>
                            </div>

                            <div class="form-row recurrence-type-weekly">
                                <g:each var="day" status="i" in="${[[name: 'sunday', value: 1, flag: ''], [name: 'monday', value: 2, flag: ''], [name: 'tuesday', value: 3, flag: ''], [name: 'wednesday', value: 4, flag: ''], [name: 'thursday', value: 5, flag: ''], [name: 'friday', value: 6, flag: ''], [name:'saturday', value: 7, flag: '']]}">
                                    <g:set var="dayValue" value="${day.value}"/>
                                    <g:if test="${event.weekDays.any {it == day.value}}">
                                        <%
                                            day.flag = "checked"
                                        %>
                                    </g:if>
                                    <div class="form-row event-recurring${dayValue}">
                                        <input type="checkbox" class="multiple" name="weekDays" value="${day.value}" uncheck-value="" ${day.flag == 'checked' ? 'checked="checked"' : ''}>
                                        <span><g:message code="${day.name}" /></span>
                                    </div>
                                </g:each>
                            </div>

                            <div class="recurrence-type-monthly">
                                <div class="double-input-row">
                                    <div class="form-row">
                                        <input class="single" toggle-target="form-row-monthly-month-day" type="radio" name="monthlyRecurrenceType" value="day" ${event.monthlyRecurrenceType == DomainConstants.MONTHLY_RECURRENCE_TYPE.DAY ? "checked = 'true'" : "" } />
                                        <span><g:message code="day" /></span>
                                    </div><div class="form-row form-row-monthly-month-day">
                                        <div class="form-row month-selector chosen-wrapper">
                                            <g:set var="monthlyMonth" value="${UUID.randomUUID().toString()}"/>
                                            <g:select name="monthNameOfMonthlyRecurring" id="${monthlyMonth}" from="${monthList}" keys="${0..11}" value="${event.monthNameOfMonthlyRecurring}"/>
                                        </div><div class="form-row">
                                            <input type="text" restrict="numeric" class="single" name="dateOfMonthlyRecurring" value="${event.dateOfMonthlyRecurring}" validation="skip@if{self::hidden} required dateTimeCheck[${monthlyMonth}]" depends="#${monthlyMonth}"/>
                                        </div>
                                    </div>
                                </div>
                                <div class="double-input-row">
                                    <div class="form-row">
                                        <input class="single" toggle-target="form-row-monthly-week-day-month" type="radio" name="monthlyRecurrenceType" value="selected" ${event.monthlyRecurrenceType == DomainConstants.MONTHLY_RECURRENCE_TYPE.SELECTED ? "checked = 'true'" : "" }/>
                                        <span><g:message code="selected" /></span>
                                    </div><div class="form-row form-row-monthly-week-day-month">
                                        <div class="form-row mandatory-chosen-wrapper">
                                            <g:select name="selectedWeekOfMonthlyRecurring" from="${weekList}" keys="${1..4}" value="${event.selectedWeekOfMonthlyRecurring}" />
                                        </div><div class="form-row mandatory-chosen-wrapper">
                                            <g:select name="selectedDayOfMonthlyRecurring" from="${dayList}" keys="${1..7}" value="${event.selectedDayOfMonthlyRecurring}" />
                                        </div><div class="form-row mandatory-chosen-wrapper">
                                            <g:select name="selectedMonthNameOfMonthlyRecurring" from="${monthList}" keys="${0..11}" value="${event.selectedMonthNameOfMonthlyRecurring}" />
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="recurrence-type-yearly">
                                <div class="form-row mandatory">
                                    <span><g:message code="recur.every.years" /></span>
                                    <input type="text" restrict="numeric" class="single" name="recurringYear" validation="skip@if{self::hidden} required" value="${event.recurringYear}" />
                                </div>
                                <div class="double-input-row">
                                    <div class="form-row">
                                        <input class="single" toggle-target="form-row-yearly-month-day" type="radio" name="yearlyRecurrenceType" value="on" ${event.yearlyRecurrenceType == DomainConstants.YEARLY_RECURRENCE_TYPE.ON ? "checked = 'true'" : "" } />
                                        <span><g:message code="on" /></span>
                                    </div><div class="form-row form-row-yearly-month-day">
                                        <div class="form-row chosen-wrapper">
                                            <g:set var="yearlyMonth" value="${UUID.randomUUID().toString()}"/>
                                            <g:select name="monthNameOfYearlyRecurring" id="${yearlyMonth}" from="${monthList}" keys="${0..11}" value="${event.monthNameOfYearlyRecurring}" />
                                        </div><div class="form-row">
                                            <input type="text" restrict="numeric" class="single" name="dateOfYearlyRecurring" value="${event.dateOfYearlyRecurring}" validation="skip@if{self::hidden} required dateTimeCheck[${yearlyMonth}]" depends="#${yearlyMonth}"/>
                                        </div>
                                    </div>
                                </div>
                                <div class="double-input-row">
                                    <div class="form-row">
                                        <input class="single" toggle-target="form-row-yearly-week-day-month" type="radio" name="yearlyRecurrenceType" value="on_the" ${event.yearlyRecurrenceType == DomainConstants.YEARLY_RECURRENCE_TYPE.ON_THE ? "checked = 'true'" : "" }/>
                                        <span><g:message code="on.the" /></span>
                                    </div><div class="form-row form-row-yearly-week-day-month">
                                        <div class="form-row mandatory-chosen-wrapper">
                                            <g:select name="selectedWeekOfYearlyRecurring" from="${weekList}" keys="${1..4}" value="${event.selectedWeekOfYearlyRecurring}" />
                                        </div><div class="form-row mandatory-chosen-wrapper">
                                            <g:select name="selectedDayOfYearlyRecurring" from="${dayList}" keys="${1..7}" value="${event.selectedDayOfYearlyRecurring}" />
                                        </div><div class="form-row mandatory-chosen-wrapper">
                                            <g:select name="selectedMonthNameOfYearlyRecurring" from="${monthList}" keys="${0..11}" value="${event.selectedMonthNameOfYearlyRecurring}" />
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <label><g:message code="range.of.recurrence"/></label>
                            <div class="double-input-row">
                                <div class="form-row">
                                    <label><g:message code="start"/></label>
                                    <g:set var="startDateTime" value="${UUID.randomUUID().toString()}"/>
                                    <input type="text" id="${startDateTime}" class="timefield large" name="startDateTime" validation="skip@if{self::hidden} required" value="${event.startDateTime?.toDatePickerFormat(true, session.timezone)}"/>
                                </div><div class="form-row">
                                    <label><g:message code="end"/></label>
                                    <input type="radio" name="recurrenceEndType" value="no_end_date" ${event.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.NO_END_DATE? "checked = 'true'" : ""} /><span><g:message code="no.end.date" /></span>
                                    <input type="radio" toggle-target="number-of-recurrence" name="recurrenceEndType" value="end_after_repetition" ${event.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_AFTER_REPETITION? "checked = 'true'" : ""} /><span><g:message code="end.after.repetation" /></span>
                                    <input type="radio" toggle-target="end-by-date" name="recurrenceEndType" value="end_by_date" ${event.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_BY_DATE? "checked = 'true'" : ""} /><span><g:message code="end.by" /></span>
                                </div>
                            </div>
                            <div class="form-row mandatory number-of-recurrence">
                                <label><g:message code="number.of.repetation"/></label>
                                <input type="text" restrict="numeric" name="occurrencesOfRecurrence" class="large" validation="skip@if{self::hidden} required" value="${event.occurrencesOfRecurrence}" validation="maxlength[200]" maxlength="200" />
                            </div>
                            <div class="form-row mandatory end-by-date">
                                <label><g:message code="end.time"/></label>
                                <input type="text" class="timefield large" name="endDateTime" value="${event.endDateTime?.toDatePickerFormat(true, session.timezone)}" validation="skip@if{self::hidden} required compare[${startDateTime}, date, gt]" depends="#${startDateTime}">
                            </div>
                        </div>
                        <div class="form-row">
                            <input class="single" type="checkbox" name="isTicketPurchaseEnabled" toggle-target="form-row-ticket-purchase" value="true" uncheck-value="false" ${event.isTicketPurchaseEnabled? "checked = 'true'" : ""} />
                            <span><g:message code="enable.ticket.purchase" /></span>
                        </div>
                        <div class="form-row-ticket-purchase">
                            <div class="form-row show-tax-profile">
                                <label><g:message code="tax.profile"/></label>
                                <ui:domainSelect name="taxProfile" class="medium" domain="${TaxProfile}" prepend="${['': g.message(code: "none")]}" value="${event.taxProfile?.id}"/>
                            </div>
                            <div class="double-input-row">
                                <div class="form-row mandatory">
                                    <label><g:message code="maximum.ticket.number"/></label>
                                    <g:set var="maxTicket" value="${UUID.randomUUID().toString()}"/>
                                    <input name="maxTicket" restrict="numeric" id="${maxTicket}" type="text" validation="skip@if{self::hidden} required" class="medium" value="${event.maxTicket}" />
                                </div><div class="form-row mandatory">
                                    <label><g:message code="maximum.ticket.per.customer"/></label>
                                    <input name="maxTicketPerCustomer" restrict="numeric" type="text" class="medium" value="${event.maxTicketPerCustomer}" validation="skip@if{self::hidden} required compare[${maxTicket}, number, lt]" />
                                </div>
                            </div>
                            <div class="double-input-row">
                                <div class="form-row mandatory">
                                    <label><g:message code="ticket.price"/></label>
                                    <input type="text" class="large" name="ticketPrice" value="${event.ticketPrice?.toPrice() ?: ""}" validation="number max[99999999]" restrict="decimal" />
                                </div><div class="form-row">
                                    <label><g:message code="availability"/><span class="suggestion"> Who can see this event?</span></label>
                                    <input type="radio" name="ticketAvailability" value="everyone" ${event.ticketAvailability == DomainConstants.TICKET_AVAILABILITY.EVERYONE? "checked = 'true'" : ""} />
                                    <span><g:message code="everyone" /></span>
                                    <input type="radio" name="ticketAvailability" value="customer" ${event.ticketAvailability == DomainConstants.TICKET_AVAILABILITY.CUSTOMER? "checked = 'true'" : ""} />
                                    <span><g:message code="customer" /></span>
                                    <input type="radio" name="ticketAvailability" toggle-target="select-customer" value="selected" ${event.ticketAvailability == DomainConstants.TICKET_AVAILABILITY.SELECTED? "checked = 'true'" : ""} />
                                    <span class="selected-customer">
                                        <span class="value"><g:message code="selected.customer"/></span>
                                        <span class="select-customer">
                                            <span class="tool-icon choose choose-customer"></span>
                                        </span>
                                    </span>
                                    <g:each in="${event.availableToCustomers}" var="customer">
                                        <input type="hidden" name="customer" value="${customer.id}">
                                    </g:each>
                                    <g:each in="${event.availableToCustomerGroups}" var="customerGroup">
                                        <input type="hidden" name="customerGroup" value="${customerGroup.id}">
                                    </g:each>
                                </div>
                            </div>
                        </div>
                        <div class="form-row chosen-wrapper">
                            <label><g:message code="venue"/></label>
                            <g:select class="single" toggle-target="form-row-venue" name="isVenueEnabled" from="${['No', 'Yes']}" value="${event.isVenueEnabled ? 1 : 0}" keys="${[0,1]}" ></g:select>
                        </div>
                        <div class="form-row form-row-venue-1 row-inside">
                            <g:include view="/plugins/general_event/admin/event/addVenueView.gsp" model="[event: event, clazz: 'scrollable-equipment']" />
                        </div>
                        <div class="form-row-venue-0">
                            <div class="double-input-row">
                                <div class="form-row mandatory">
                                    <label><g:message code="address"/></label>
                                    <input type="text" class="large" type="text" validation="skip@if{self::hidden} required" value="${event.generalAddress}" name="generalAddress" />
                                </div><div class="form-row">
                                    <g:set var="googleMap" value="${UUID.randomUUID().toString()}"/>
                                    <label><g:message code="show.google.map"/></label>
                                    <input toggle-target="show-latitude-longitude" class="single" type="checkbox" id="${googleMap}" name="showGoogleMap" value="true" uncheck-value="false" ${event.showGoogleMap ? "checked" : ""} />
                                </div>
                            </div>
                            <div class="double-input-row show-latitude-longitude">
                                <div class="form-row mandatory">
                                    <label><g:message code="latitude"/></label>
                                    <input type="text" class="large" value="${event.latitude}" name="latitude" validation="skip@if{self::hidden} required@if{global:#${googleMap}:checked} number max[99999999]" />
                                </div><div class="form-row mandatory">
                                    <label><g:message code="longitude"/></label>
                                    <input type="text" class="large" value="${event.longitude}" name="longitude" validation="skip@if{self::hidden} required@if{global:#${googleMap}:min} number max[99999999]" />
                                </div>
                            </div>
                        </div>
                        <div class="form-row">
                            <label>&nbsp;</label>
                            <input class="single" toggle-target="form-row-add-equipment" type="checkbox" name="isEquipmentEnabled" value="true" uncheck-value="false" ${event.isEquipmentEnabled ? "checked='checked'" : ""}>
                            <g:message code="equipment"/>
                        </div>
                        <div class="form-row-add-equipment">
                            <g:include view="/plugins/general_event/admin/event/addEquipmentView.gsp" model="[event: event, clazz: 'scrollable-equipment']" />
                        </div>
                        <div class="form-row">
                            <label><g:message code="event.summary"/></label>
                            <textarea class="xx-larger" name="summary" validation="maxlength[500]" maxlength="500">${event.summary}</textarea>
                        </div>
                        <div class="form-row">
                            <label>&nbsp;</label>
                            <label><g:message code="image"/></label>
                            <input type="file" name="images" file-type="image" queue="event-image-queue" multiple="true" />
                            <div id="event-image-queue" class="multiple-image-queue">
                            </div>
                            <div class="event-image-container">
                                <div class="left-scroller scroll-navigator" style="display: none"></div><div class="right-scroller scroll-navigator" style="display: none"></div>
                                <div class="event-image-wrapper one-line-scroll-content">
                                    <g:each in="${event?.images}" var="image">
                                        <div image-id="${image.id}" image-name="${image.name}" class="image-thumb">
                                            <span class="tool-icon remove"></span>
                                            <input type="hidden" name="imageId" value="${image.id}" />
                                            <div class="image-container">
                                                <img src="${app.customResourceBaseUrl()}resources/general-event/event-${event?.id}/images/150-${image.name}" />
                                            </div>
                                        </div>
                                    </g:each>
                                </div>
                            </div>
                        </div>
                        <div class="form-row tinymce-container">
                            <label><g:message code="event.description"/></label>
                            <textarea class="wceditor no-auto-size xx-larger" style="height: 240px" toolbar-type="advanced" name="description" >${event.description}</textarea>
                        </div>
                    </div>
                </div>
            </div>
            <div id="bmui-tab-metatag">
                <div class="form-section">
                    <div class="form-section-info">
                        <h3><g:message code="meta.tag"/></h3>
                        <div class="info-content"><g:message code="form.section.text.event.meta.tag"/></div>
                    </div>
                    <div class="form-section-container">
                        <g:include view="/admin/metatag/metaTagEditor.gsp" model="[metaTags: event.metaTags?: [:]]"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="form-row wcui-horizontal-tab-button">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${event.id ? "update" : "save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>