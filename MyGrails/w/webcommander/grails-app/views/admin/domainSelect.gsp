<select>
    <option value="">${params.tip ?: g.message(code: "select")}</option>
    <g:each in="${domain}" var="dom">
        <option value="${dom.id}">${dom.name}</option>
    </g:each>
</select>