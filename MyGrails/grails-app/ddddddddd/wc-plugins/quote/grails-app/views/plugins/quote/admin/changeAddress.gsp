<%--
  Created by IntelliJ IDEA.
  User: sajedur
  Date: 6/16/2015
  Time: 11:51 AM
--%>
<form class="edit-popup-form">
    <g:include view="/admin/order/editAddress.gsp" params="${new HashMap(params)}"/>
    <div class="button-line">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="change"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>