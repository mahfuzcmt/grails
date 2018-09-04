<div class="variation-mapping">
    %{--dom alignment should not change--}%
    <g:set var="length" value="${variations.size()}"/>
    <g:each in="${variations}" status="i" var="variation"><g:if test="${i%2 == 0 && i+1 != length}">
        <div class="double-input-row">
        </g:if><div class="form-row chosen-wrapper">
            <label>${variation.capitalize().encodeAsBMHTML()}</label>
            <g:set var="x" value="${[:]}"/>
            <g:select name="mapping.${variation}" from="${googleVariations.collect {g.message(code: it.value)}}" keys="${googleVariations.keySet()}" value="${mapping[variation]}"/>
        </div><g:if test="${i%2 != 0}"></div></g:if></g:each>
</div>