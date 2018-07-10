<page:convertAutoPage name="${name}" macros="${macros}"/>
<g:applyLayout name="sitepage">
    %{--
            parameter becoming duplicate in grails 3
            <g:include view="${view}" params="${new LinkedHashMap(params)}"/>
    --}%
    <g:include view="${view}"/>
</g:applyLayout>