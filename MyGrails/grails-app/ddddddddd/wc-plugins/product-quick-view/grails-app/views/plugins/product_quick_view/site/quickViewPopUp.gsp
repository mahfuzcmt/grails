<div class="product-quick-view-popup">
    <div class="header">
        <div class="title"><g:message code="product.quick.view"/></div>
        <span class="close-popup close-icon"></span>
        <div class="view-switcher scroller">
            <span class="view-left scroll-left"></span>
            <span class="view-right scroll-right"></span>
        </div>
    </div>
    <div class="body">
        <g:include view="site/product/productDetailsInfo.gsp" model="${pageScope.variables}"/>
        <style type="text/css">${pageContent.css}</style>
    </div>
</div>
