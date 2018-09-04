<div class="content create-edit-form zone-panel-view">
    <div class="form-section">
        <div class="form-section-info-block">
            <h3><g:message code="zone"/></h3>
            <div class="info-content"><g:message code="section.text.create.zone"/></div>
        </div>
        <div class="form-section-container-block zone-table with-top-btn">
            <div class="btn-panel add-item">
                <button class="submit-button add-zone-btn" type="button">+&nbsp;<g:message code="add.zone"/></button>
            </div>
            <table class="content">
                <colgroup>
                    <col class="name">
                    <col class="country">
                    <col class="state">
                    <col class="post-code">
                    <col class="actions-column">
                </colgroup>
                <tr>
                    <th><g:message code="zone.name"/></th>
                    <th><g:message code="country"/></th>
                    <th><g:message code="state"/></th>
                    <th><g:message code="post.code"/></th>
                    <th class="actions-column"><g:message code="actions"/></th>
                </tr>
                <g:if test="${zones}">
                    <g:each in="${zones}" var="zone">
                        <tr>
                            <td>${zone.name.encodeAsBMHTML()}</td>
                            <td>
                                <g:set var="countries" value="${zone.countries}"/>
                                <span class="flag-icon ${countries.size() > 1 ? "" : countries[0].code.toLowerCase()}"></span>
                                <span>${countries?.name.join(", ")}</span>
                            </td>
                            <td>
                                <g:set var="states" value="${zone.states}"/>
                                ${states?.name.join(", ")}
                            </td>
                            <td>
                                <g:set var="postCodes" value="${zone.postCodes}"/>
                                ${postCodes?.join(", ")}
                            </td>
                            <td class="actions-column"><span class="action-navigator collapsed" entity-id="${zone.id}" entity-name="${zone.name.encodeAsBMHTML()}"></span></td>
                        </tr>
                    </g:each>
                </g:if>
                <g:else>
                    <tr class="table-no-entry-row">
                        <td colspan="6"><g:message code="no.zone.created"/></td>
                    </tr>
                </g:else>
            </table>
        </div>
    </div>
</div>