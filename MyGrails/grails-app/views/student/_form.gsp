<g:if test="${flash.error}">
    <div class="alert alert-error" style="display: block">${flash.error}</div>
</g:if>
<g:if test="${flash.message}">
    <div class="message" style="display: block">${flash.message}</div>
</g:if>

<div class="form-group">
    <label>Name<span class="mandatory">*</span></label>
    <g:textField name="name" class="form-control" value="${student?.name}" placeholder="Please Enter Contact Name" required="required"/>
</div>
<div class="form-group">
    <label>Cell No<span class="mandatory">*</span></label>
    <g:textField name="cellNo" class="form-control" value="${student?.cellNo}" placeholder="Please Enter Cell No" required="required"/>
</div>

<div class="form-group">
    <label>Address</label>
    <g:textField name="address" class="form-control" value="${student?.address}" placeholder="Please Enter Address"/>
</div>
