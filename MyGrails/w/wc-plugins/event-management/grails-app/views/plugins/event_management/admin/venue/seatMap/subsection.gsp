<div class="subsection">
    <g:each in="${subsection}" var="seat">
        <span class="${seat.cssClass}" data-id="${seat.id}">${seat.label}</span>
    </g:each>
</div>
