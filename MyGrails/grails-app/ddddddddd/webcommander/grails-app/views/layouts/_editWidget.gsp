<g:form class="create-edit-form" controller="widget" action="save${widget.widgetType.capitalize()}Widget">
    <textarea name="params-cache" style="display: none !important;">${widget.params}</textarea>
    <g:layoutBody/>
</g:form>