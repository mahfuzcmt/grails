<%@ page import="grails.converters.JSON; com.webcommander.util.StringUtil" %>
<g:set var="id" value="${StringUtil.uuid}"/>
<div id="${id}" class="gallery nivo-slider" style="${(config.width ? ('width:' + config.width + 'px;') : '') + (config.height ? ('height:' + config.height + 'px;') : '')}">
    <g:each in="${items}" var="im" status="i">
        <g:if test="${config.customLink == 'true' && im.linkTo != 0}">
            <a href="${links[i]}" target="${im.linkTarget}" ${i == 0 ? '' : 'style="display: none"'}>
                <img src="${appResource.getAlbumImageURL(image:im)}" alt="${im.altText}" data-thumb="${appResource.getAlbumImageURL(image:im, sizeOrPrefix: "thumb")}">
            </a>
        </g:if>
        <g:else>
            <img src="${appResource.getAlbumImageURL(image:im)}" alt="${im.altText}" ${i == 0 ? '' : 'style="display: none"'} data-thumb="${appResource.getAlbumImageURL(image:im, sizeOrPrefix: "thumb")}">
        </g:else>
    </g:each>
</div>
<g:if test="${request.page}">
    <%
        if(!request.is_nivo_loaded) {
            request.js_cache.push("galleries/nivoSlider/jquery.nivo.slider.pack.js")
            request.css_cache.push("galleries/nivoSlider/nivo-slider.css")
            request.is_nivo_loaded = true;
        }
    %>
    <script type="text/javascript">
        $(function() {
            bm.onReady($.prototype, "nivoSlider", function () {
                $("#${id}").nivoSlider({
                     manualAdvance: ${(config.directionNav.toBoolean() || config.controlNav.toBoolean()) ? config.manualAdvance.toBoolean() : false},
                     controlNav: ${config.controlNav.toBoolean()},
                     directionNav: ${config.directionNav.toBoolean()},
                     controlNavThumbs: ${config.controlNavThumbs.toBoolean()},
                     pauseOnHover: ${config.pauseOnHover.toBoolean()},
                     animSpeed: ${config.animSpeed},
                     effect: '${config.effect}',
                     prevText: '<site:message code="${config.prevText}"/>',
                     nextText: '<site:message code="${config.nextText}"/>',
                     slice: 20,
                     pauseTime: ${config.slideTime}
                });
            })
        });
    </script>
</g:if>
<g:else>
    <textarea style="display: none" class="config-value-cache">${(config as JSON).toString().encodeAsBMHTML()}</textarea>
</g:else>