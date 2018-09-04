<g:applyLayout name="_widget">
    <g:each in="${articleList}" var="article">
        <g:include view="/site/common/singleArticleView.gsp" model="${[article: article, config: config]}"/>
    </g:each>
</g:applyLayout>
