<table>
    <colgroup>
        <col style="width: 5%" />
        <col style="width: 10%" />
        <col style="width: 35%" />
        <col style="width: 50%" />
    </colgroup>
    <thead>
    <tr>
        <th>SL</th>
        <th>Status</th>
        <th>Name/SKU [Match By]</th>
        <th>Remarks</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${logs}" var="log" status="i">
        <tr>
            <td>${i + 1}</td>
            <td>${log.type}</td>
            <td>${log.logFor}</td>
            <td><g:message code="${log.msg}" args="${log.args}"/></td>
        </tr>
    </g:each>
    <g:if test="${!logs}">
        <tr>
            <td colspan="4">No results found.</td>
            <td></td>
            <td></td>
            <td></td>
        </tr>
    </g:if>
    </tbody>
</table>