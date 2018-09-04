<div id="owl-carousel-${widget.uuid}" class="owl-carousel">
    <%
        Map articleConfig = [
            article_title: "show",
            display_option: "full"
        ]
    %>
    <g:each in="${items}" var="article">
        <div class="item">
            <g:include view="/site/common/singleArticleView.gsp" model="${[article: article, config: articleConfig]}"/>
        </div>
    </g:each>
</div>