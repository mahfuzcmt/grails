<%@ page import="com.webcommander.content.Section" %>
<div class="fee-pu-content-header" style="${selectFormOnly ? 'display:none' : ''}">
    <div class="fee-pu-header-right fee-ignore">
        <div class="fee-section-box fee-search-panel">
            <ui:domainSelect name="sectionFilter" domain="${Section}" id="sectionFilter" prepend="${['': g.message(code: "section")]}" class="always-top" value="${params.sectionFilter ? params.sectionFilter.toLong() : 0}"/>
        </div>
        <div class="fee-search-box fee-search-panel">
            <input type="text" class="fee-search-input" name="searchText" value="${params.searchText}" placeholder="Search">
            <button type="button" class="fee-search-button"></button>
        </div>
        <button style="${createFormOnly ? 'display:none' : ''}" class="fee-pu-button fee-add-button" type="button"> + <g:message code="create.article"/></button>
    </div>
</div>
<div class="fee-pu-content-body article-list-container" style="${createFormOnly ? 'display:none' : ''}">
    <div class="fee-body">
        <table class="fee-table sortable-table" data-sortBy="${params.sortBy}">
            <tr>
                <th data-sortable="ALPHA"><g:message code="title"/></th>
                <th><g:message code="visibility"/></th>
                <th><g:message code="section"/></th>
                <th data-sortable="CREATED"><g:message code="date"/></th>
                <th></th>
            </tr>
            <g:set var="status" value="${[open: 'positive', hidden: 'negative', restricted: 'diplomatic']}"/>
            <g:each in="${articles}" var="article">
                <tr data-id="${article.id}">
                    <td>${article.name.encodeAsBMHTML()}</td>
                    <td class="status-column"><span class="status ${article.isPublished ? 'positive' : 'negative'}"></span></td>
                    <td>${article.section?.name}</td>
                    <td>${article.created.toAdminFormat(false, false, session.timezone)}</td>
                    <td><span class="fee-insert-btn">Insert</span></td>
                </tr>
            </g:each>
        </table>
    </div>
    <div class="fee-footer">
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>
<div class="fee-pu-content-body fee-add-panel" style="${createFormOnly ? '' : 'display:none'}">
    <div class="fee-form-row fee-multi-input-row">
        <div class="fee-form-element">
            <label for="articleName"><g:message code="article.name" default="Article Nameame"/></label>
            <input type="text" name="new.name" id="articleName" placeholder="${g.message(code: "article.name")}" maxlength="100" value="${params.articleName}">
        </div>
        <div class="fee-form-element">
            <label for="sectionId"><g:message code="article.section" default="Article Section"/></label>
            <ui:domainSelect name="new.section" domain="${Section}" id="sectionId" prepend="${['': g.message(code: "section")]}" class="always-top" value="${params.section}"/>
        </div>
    </div>
    <div class="fee-form-row">
        <div class="fee-form-element">
            <label for="articleContent"><g:message code="article.content" default="Article Content"/></label>
            <textarea name="new.content" id="articleContent" class="text-area" maxlength="65500">${params.newContent ?: ''}</textarea>
        </div>
    </div>
</div>