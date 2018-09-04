<div class="meta-tag-editor">
    <table class="meta-tag-table">
        <tr>
            <th><g:message code="name"/><span class="suggestion"> e.g: Meta Name for the page, for example “Web Commander”</span></th>
            <th><g:message code="value"/><span class="suggestion"> e.g: Value are like description of the page, for example “This page is a tool take command of your Website”</span></th>
            <th class="actions-column"><g:message code="action"/></th>
        </tr>
        <g:each in="${metaTags}" var="tag">
            <tr>
                <td class="editable name">${tag.name.encodeAsBMHTML()}</td>
                <td class="editable value">${tag.value.encodeAsBMHTML()}</td>
                <td class="actions-column">
                    <input name="tag_name" type="hidden" value="${tag.name.encodeAsBMHTML()}">
                    <input name="tag_content" type="hidden" value="${tag.value.encodeAsBMHTML()}">
                    <span class="tool-icon remove"></span>
                </td>
            </tr>
        </g:each>
        <tr class="last-row">
            <td><input maxlength="${ui.getSeoConfig(configKey:"meta_tag_name_limit")}" type="text" class="name td-full-width"></td>
            <td><input maxlength="${ui.getSeoConfig(configKey:"meta_tag_description_limit")}" type="text" class="value td-full-width"></td>
            <td class="actions-column"><span class="tool-icon add add-row"></span></td>
        </tr>
    </table>
</div>
