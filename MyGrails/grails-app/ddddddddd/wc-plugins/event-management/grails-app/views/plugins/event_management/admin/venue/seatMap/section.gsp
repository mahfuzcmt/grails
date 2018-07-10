<div class="section-wrap">
    <g:each in="${section.value}" var="subsections">
        <g:each in="${subsections}" var="subsection">
            <g:include view="/plugins/event_management/admin/venue/seatMap/subsection.gsp" model="[subsection: subsection]"/>
        </g:each>
    </g:each>
</div>