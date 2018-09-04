<%
    Map articleConfig = [
            article_title: "show",
            display_option: "full"
    ]
%>
<g:each in="${items}" status="i" var="article">
    <div class="slide slide-${i+1}">
        <g:include view="/site/common/singleArticleView.gsp" model="${[article: article, config: articleConfig]}"/>

    </div>
</g:each>