<div class="info-row">
    <label><g:message code="first.name"/></label>
    <span class="value">${customer.firstName.encodeAsBMHTML()}</span>
</div>
<g:if test="${customer.lastName}">
    <div class="info-row">
        <label><g:message code="last.name.surname"/></label>
        <span class="value">${customer.lastName?.encodeAsBMHTML()}</span>
    </div>
</g:if>
<div class="info-row">
    <label><g:message code="customer.type"/></label>
    <span class="value"><g:message code="${ customer.isCompany ? "company" : "individual"}"/></span>
</div>
<div class="info-row">
    <label><g:message code="sex"/></label>
    <span class="value">${customer.sex?.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="email"/></label>
    <span class="value">${customer.userName?.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="address.line.1"/></label>
    <span class="value">${customer.address?.addressLine1.encodeAsBMHTML()}</span>
</div>
<g:if test="${customer.address.addressLine2}">
    <div class="info-row">
        <label><g:message code="address.line.2"/></label>
        <span class="value">${customer.address?.addressLine2.encodeAsBMHTML()}</span>
    </div>
</g:if>
<g:if test="${customer.address.city}">
    <div class="info-row">
        <label><g:message code="suburb/city"/></label>
        <span class="value">${customer.address?.city.encodeAsBMHTML()}</span>
    </div>
</g:if>
<div class="info-row">
    <label><g:message code="country"/></label>
    <span class="value">${customer.address?.country?.name.encodeAsBMHTML()}</span>
</div>
<g:if test="${customer.address.state}">
    <div class="info-row">
        <label><g:message code="state"/></label>
        <span class="value">${customer.address?.state?.name.encodeAsBMHTML()}</span>
    </div>
</g:if>
<g:if test="${customer.address.postCode}">
    <div class="info-row">
        <label><g:message code="post.code"/></label>
        <span class="value">${customer.address?.postCode.encodeAsBMHTML()}</span>
    </div>
</g:if>
<g:if test="${customer.address.phone}">
    <div class="info-row">
        <label><g:message code="phone"/></label>
        <span class="value">${customer.address?.phone.encodeAsBMHTML()}</span>
    </div>
</g:if>
<g:if test="${customer.address.mobile}">
    <div class="info-row">
        <label><g:message code="mobile"/></label>
        <span class="value">${customer.address?.mobile.encodeAsBMHTML()}</span>
    </div>
</g:if>
<g:if test="${customer.address.fax}">
    <div class="info-row">
        <label><g:message code="fax"/></label>
        <span class="value">${customer.address?.fax.encodeAsBMHTML()}</span>
    </div>
</g:if>
<div class="info-row">
    <label><g:message code="status"/></label>
    <span class="value">${customer.status == "A" ? g.message(code: "active") : customer.status == "I" ? g.message(code: "inactive") : g.message(code: "awaiting")}</span>
</div>
<g:if test="${customer.abn}">
    <div class="info-row">
        <label><g:message code="abn"/></label>
        <span class="value">${customer.abn.encodeAsBMHTML()}</span>
    </div>
</g:if>
<g:if test="${customer.abnBranch}">
    <div class="info-row">
        <label><g:message code="abn.branch"/></label>
        <span class="value">${customer.abnBranch.encodeAsBMHTML()}</span>
    </div>
</g:if>
<div class="info-row">
    <label><g:message code="store.credit"/></label>
    <span class="value">${customer.storeCredit.toAdminPrice()}</span>
</div>
<div class="info-row">
    <label><g:message code="created"/></label>
    <span class="value">${customer.created.toAdminFormat(true, false, session.timezone)}</span>
</div>

<div class="info-row">
    <label><g:message code="updated"/></label>
    <span class="value">${customer.updated.toAdminFormat(true, false, session.timezone)}</span>
</div>