<%@ page import="com.webcommander.util.AppUtil" %>
<g:applyLayout name="_widget">
    <div class="content">
        <g:if test="${isAdmin}">
            <div class="widget-text">
                <span><g:message code="filter.widget"/></span>
            </div>
        </g:if>
        <g:else>
            <form action="${app.relativeBaseUrl()}filterPage/searchProduct" method="get">
                <input type="hidden" name="categoryId" value="${config["category_id"]}"/>
                <g:if test="${filterProfile}">
                    <div class="form-row filter-profile-select">
                        <g:each in="${filterProfile}" var="profile">
                            <div class="filter-profile">

                                <label class="name">${profile.name.encodeAsBMHTML()}</label>

                                <g:each in="${profile.filters}" var="filter">
                                    <g:if test="${filter.property.equals("priceRange")}">
                                        <span class="filter-name">${filter.name.encodeAsBMHTML()}</span>
                                        <div class="double-input-row">
                                            <div class="form-row">
                                                <label><site:message code="minimum.price"/></label>
                                                <input type="text" name="priceRange.minRange" validation="number maxlength[16]" restrict="decimal" maxlength="16"
                                                       value="${propertyValues?.priceRange?.minRange ? propertyValues.priceRange.minRange : 0}"/>
                                            </div>
                                            <div class="form-row">
                                                <label><site:message code="maximum.price"/></label>
                                                <input type="text" name="priceRange.maxRange" validation="number maxlength[16]" restrict="decimal" maxlength="16"
                                                       value="${propertyValues?.priceRange?.maxRange ? propertyValues.priceRange.maxRange : 0}"/>
                                            </div>
                                        </div>
                                    </g:if>
                                    <g:elseif test="${filter.property.matches("brand|manufacturer|productCondition")}">
                                        <g:if test="${config[filter.property]}">
                                            <div class="form-row">
                                            <span class="filter-name">${filter.name.encodeAsBMHTML()}</span>
                                            <g:select name="${filter.property}" from="${config[filter.property]}"
                                                      key="${config[filter.property]}" value="${propertyValues[filter.property]}"/>
                                            </div>
                                        </g:if>
                                    </g:elseif>
                                    <g:else>
                                        <div class="filter-check">
                                            <input type='checkbox' name='${filter.property}' class='single' uncheck-value="false" ${propertyValues[filter.property]?.matches("true|on") ? "checked='checked'" : ""}>
                                            <span class="filter-name">${filter.name.encodeAsBMHTML()}</span>
                                        </div>
                                    </g:else>
                                </g:each>

                            <div class="form-row-group">
                                <g:each in="${profile.filterGroups}" var="filterGroup">
                                    <g:if test="${filterGroup.isActive}">
                                    <div class="form-row">
                                        <label>${filterGroup.name.encodeAsBMHTML()}</label>
                                        <g:select name="filter-group-item" class="medium" from="${filterGroup.items}" optionValue="heading" optionKey="id" value="${filterGroupSelectedValues[filterGroup.id]}" noSelection="['': g.message(code: 'none')]" />
                                    </div>
                                    </g:if>
                                </g:each>
                            </div>

                            </div>
                        </g:each>
                    </div>
                </g:if>
            </form>
        </g:else>
    </div>
</g:applyLayout>
