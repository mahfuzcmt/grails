<div class="create-edit-form">
    <input type="hidden" name="pId" value="${product.id}">
    <div class="variation-selection">
        <g:set var="j" value="${1}"/>
        <g:each in="${types}" var="entry" status="i">
            <div class="variation-config">
                <g:if test="${i < 2}">
                    <span class="title"><g:message code="${i == 0 ? 'x.axis' : 'y.axis'}"/></span>
                    <g:each in="${types}" var="type" status="k">
                        <div class="form-row axis">
                            <g:set var="name" value="${i == 0 ? "xAxis" : "yAxis"}"/>
                            <input type="radio" radio="radio-${k+1}" name="config.${name}" value="${type.id}" ${config[name] == type.id ? 'checked' : ''}>
                            <span class="value"><g:message code="${type?.name.encodeAsBMHTML()}"/></span>
                        </div>
                    </g:each>
                </g:if>
                <g:else>
                    <span class="title"><g:message code="combobox"/> ${j}</span>
                    <g:each in="${types}" var="type" status="k">
                        <div class="form-row combo-box">
                            <g:set var="name" value="combobox${j}"/>
                            <input type="radio" radio="radio-${k+1}" name="config.${name}" value="${type.id}" ${config[name] == type.id ? 'checked' : ''}>
                            <span class="value"><g:message code="${type?.name.encodeAsBMHTML()}"/></span>
                        </div>
                    </g:each>
                    <%j++%>
                </g:else>
            </div>
        </g:each>
    </div>
    <div class="button-line">
        <button type="button" class="submit-button select-variation"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>
