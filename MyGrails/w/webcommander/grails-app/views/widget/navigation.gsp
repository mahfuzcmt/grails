<g:applyLayout name="_widget">
    <g:if test="${config.responsive_menu == "true"}"><div class="menu-title">
        <div class="menu-button responsive-menu-btn" transition-type="${config.transition_type}"><em>&nbsp;</em> <em>&nbsp;</em> <em>&nbsp;</em></div>
        <span>${site.message(code: widget.title).encodeAsBMHTML()}</span>
    </div></g:if>
    ${dom}
    <g:if test="${config.responsive_menu == "true"}"><style type="text/css">
        <g:if test="${hasGlobal}"> #wi-${widget.uuid} .menu-title {
            display: block;
        }
        #wi-${widget.uuid} .nav-wrapper {
            display: none;
        }
        #wi-${widget.uuid} .nav-wrapper.show {
            display: block;
        }
        #wi-${widget.uuid} .nav-wrapper a{
            display: block;
        }</g:if>
        <g:each in="${resolutions}" var="resolution">@media ${resolution.min ? "(min-width: " + resolution.min + "px)" : ""}${resolution.max && resolution.min ? " and " : ""} ${resolution.max ? "(max-width: " + resolution.max + "px)" : ""}{
            #wi-${widget.uuid} .menu-title {
                display: block;
            }
            #wi-${widget.uuid} .nav-wrapper {
                display: none;
            }
            #wi-${widget.uuid} .nav-wrapper.show {
                display: block;
            }
            #wi-${widget.uuid} .nav-wrapper a{
                display: block;
            }
        }</g:each>
    </style></g:if>
</g:applyLayout>