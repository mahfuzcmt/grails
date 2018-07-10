<%--
  Created by IntelliJ IDEA.
  User: tariq
  Date: 03-Apr-17
  Time: 10:11 AM
--%>

<form method="post" class="edit-popup-form">
    <div class="form-row chosen-wrapper">
        <label><g:message code="submission.range"/></label>
        <ui:namedSelect class="large" toggle-target="submission-range" toggle-anim="slide" name="submission-range-selector" key="${searchCriteria}"
                        value=""/>
    </div>
    <div class="form-row mandatory datefield-between submission-range-date">
        <label><g:message code="date.range"/></label>
        <input type="text" class="datefield-from" name="dateFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to" name="dateTo" validation="skip@if{self::hidden} required"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>