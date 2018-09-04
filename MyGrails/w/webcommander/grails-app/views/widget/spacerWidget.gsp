<g:applyLayout name="_widget">
    <div class="spacer">
        <g:if test="${!request.page || request.editMode}"><span><g:message code="spacer"/>-${config.height}</span></g:if>
    </div>
    <style type="text/css">
        #wi-${widget.uuid} .spacer {
            height: ${config.height}px;
        }
        @media (max-width: 1024px){
            #wi-${widget.uuid} .spacer {
                height: ${config.height_in_tab}px;
            }
        }
        @media (max-width: 767px){
            #wi-${widget.uuid} .spacer {
                height: ${config.height_in_mobile}px;
            }
        }
    </style>
</g:applyLayout>