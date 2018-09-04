<div class="macro-dropdown-panel">
    <form class="search-form search-block">
        <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
    </form>
    <div class="body macro-wrapper">
        <ul class="macro-dropdown-wrapper">
            <g:each in="${macrosList}" var="macro">
                <li class="each-macro">
                    <span class="macro-display-name searchable-text"><g:message code="${macro.replaceAll("_", ".")}"/></span>
                    <span class="macro-value">%${macro}%</span>
                </li>
            </g:each>
        </ul>
    </div>
</div>