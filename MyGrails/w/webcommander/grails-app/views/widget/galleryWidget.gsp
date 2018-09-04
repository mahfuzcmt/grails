<%@ page import="com.webcommander.constants.Galleries" %>
<g:applyLayout name="_widget">
    <g:set var="galleryType" value="${Galleries.TYPES[config.gallery]}"/>
    <g:if test="${galleryType}">
        <g:include view="${galleryType.render}" model="${[widget: widget, config: config, items: items, links: links, totalCount: totalCount, offset: offset, max: max, url_prefix: url_prefix]}"/>
    </g:if>
</g:applyLayout>