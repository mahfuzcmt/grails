<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil" %>
<g:applyLayout name="_widget">
    <g:include view="widget/productListings.gsp" model="[productList: productList, config: config, totalCount: totalCount, offset: offset, max: max, url_prefix: 'prwd-' + widget.id]"/>
</g:applyLayout>