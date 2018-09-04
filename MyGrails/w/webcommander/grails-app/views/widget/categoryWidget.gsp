<g:applyLayout name="_widget">
    <g:include view="widget/categoryImageView.gsp" model="[categoryList: categoryList, config: config, totalCount: totalCount, offset: offset, max: max, url_prefix: 'crwd-' + widget.id]"/>
</g:applyLayout>