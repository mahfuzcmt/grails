<%@ page import="com.webcommander.webcommerce.Category" %>
<g:applyLayout name="_widget">
    <g:if test="${config.responsive_menu == "true"}"><div class="search-menu-button responsive-menu-btn"></div></g:if>
    <div class="search-form">
        <g:form controller="lookup" action="search" method="get" class="valid-verify-form" disable-on-invalid="false">
            <input type="text" id="elastic-search-text" class="search-text"  autocomplete="off"  name="name" placeholder="<site:message code="${config.placeholderText}"/>" error-position="none" validation="required">
            <button type="submit" class="icon-search et_ecommerce_search"><site:message code="${config.buttonText}"/></button>
        </g:form>
    </div>
    <g:if test="${config.responsive_menu == "true"}"><style type="text/css">
    <g:if test="${hasGlobal}">#wi-${widget.uuid} .search-menu-button {
        display: block;
    }
    #wi-${widget.uuid} .search-form {
        display: none;
    }
    #wi-${widget.uuid} .search-form.show {
        display: block;
    }</g:if>
    <g:each in="${resolutions}" var="resolution">@media ${resolution.min ? "(min-width: " + resolution.min + "px)" : ""}${resolution.max && resolution.min ? " and " : ""} ${resolution.max ? "(max-width: " + resolution.max + "px)" : ""}{
        #wi-${widget.uuid} .search-menu-button {
            display: block;
        }
        #wi-${widget.uuid} .search-form {
            display: none;
        }
        #wi-${widget.uuid} .search-form.show {
            display: block;
        }
    }</g:each>
    </style></g:if>
</g:applyLayout>