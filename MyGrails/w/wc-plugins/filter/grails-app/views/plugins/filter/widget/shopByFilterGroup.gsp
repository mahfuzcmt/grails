<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>

<g:applyLayout name="_widget">
    <g:if test="${request.page}">
        <%
            if(!request.shop_by_filter_group_script_loaded) {
        %>
        <script type="text/javascript">
            $(function() {
                $('.shop-by-selector').find('select').change(function() {
                    if(this.value) {
                        location.href = app.baseUrl + $(this).attr('name') + '/' + $(this).val()
                    }
                })
            })
        </script>
        <%
                request.shop_by_filter_group_script_loaded = true;
            }
        %>
    </g:if>

    <g:set var="itemType" value="filter"/>

    <g:if test="${config.style == "image"}">

        <g:each in="${shopData}" var="item">
            <g:set var="image" value="${appResource.getFilterGroupItemImageURL(filterGroupItem: item, imageSize: "400")}"/>
            <g:set var="url" value="${app.relativeBaseUrl() + itemType + "/" + item.url}"/>
            <div class="filter-group-item-block">
                <a class="image-container" href="${url}">
                    <img src="${image}" alt="${item.imageAlt}">
                </a>
                <a class="title-container" href="${url}">
                    <span class="title">${item.title.encodeAsBMHTML()}</span>
                </a>
            </div>
        </g:each>

    </g:if>
    <g:else>
        <div class="shop-by-selector">
            <label>${filterGroup ? filterGroup.name : ""}:</label>
            <g:select name="filter" optionKey="url" from="${shopData}" optionValue="title" value="${selected}" noSelection="${['':g.message([code: "select."])]}"/>
        </div>
    </g:else>
</g:applyLayout>