<%@ page import="com.webcommander.content.Album" %>

<div class="gallery-content-config album">
    <div class="form-row chosen-wrapper">
        <label><g:message code="select.album"/></label>
        <ui:domainSelect name="album" class="medium" domain="${Album}" value="${albumId}"/>
    </div>
</div>