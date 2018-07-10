<g:applyLayout name="_widget">
    <div class="rss-feed-container">
        <g:if test="${config.show_title.toBoolean()}">
            <h2>${feed.channel.title.text()}</h2>
        </g:if>
        <g:set var="toDisplay" value="${config.item_to_display.toInteger()}"/>
        <g:set var="size" value="${feed.channel.item.size()}"/>
        <g:each in="${0..((toDisplay > size ? size : toDisplay) - 1)}" var="it">
            <g:set var="item" value="${feed.channel.item[it]}"/>
            <div class="item">
                <div class="title"><a href="${item.link.text()}">${item.title.text()}</a></div>
                <g:if test="${config.show_date.toBoolean()}">
                    <div class="date">${item.pubDate.text()}</div>
                </g:if>
                <g:if test="${config.show_content.toBoolean()}">
                    <div class="content">${item.description.text()}</div>
                </g:if>
                <g:if test="${config.show_author.toBoolean() && item.author.size()}">
                    <div class="author">${item.author.text()}</div>
                </g:if>
            </div>
        </g:each>
    </div>
</g:applyLayout>