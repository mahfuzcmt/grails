<div class="file-selector">
        <g:if test="${params.isFile == 'true'}">
            <div class="remote_folders">
            </div>
        </g:if>
        <g:else>
            <div class="left-right-selector-panel">
                <div class="multi-column two-column">
                    <div class="columns first-column">
                        <div class="remote_folders">
                        </div>
                    </div><div class="columns last-column">
                        <div class="asset-library-image-preview"></div>
                    </div>
                </div>
            </div>
        </g:else>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="select"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>