 <g:each in="${submissionDataList}" var="submissionData">
    <div class="info-row">
        <label>${submissionData.fieldName.encodeAsBMHTML()}</label>
        <span class="value">
            <g:if test="${submissionData.isFile}">
                <g:each in="${submissionData.fieldValue.split("&#")}" var="file">
                    <a target="_blank" title="<g:message code="download.file"/>" href="${app.baseUrl()}formAdmin/downloadSubmission?id=${submissionData.formSubmission.id}&fileName=${file}">${file}</a>
                </g:each>
            </g:if>
            <g:else>${submissionData.fieldValue.encodeAsBMHTML()}</g:else>
        </span>
    </div>
</g:each>
