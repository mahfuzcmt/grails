<div class="header">
    <span class="item-group title entity-count">
        <g:message code="advanced.edit.css"/>
    </span>
    <div class="toolbar toolbar-right">
        <form class="search-form tool-group" action="#" onsubmit="return false">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item previous" title="<g:message code="previous"/>"><i></i></span>
            <span class="toolbar-item next" title="<g:message code="next"/>"><i></i></span>
        </div>
        <div class="tool-group">
            <span class="toolbar-item undo disabled" title="<g:message code="undo"/>"><i></i></span>
            <span class="toolbar-item redo disabled" title="<g:message code="redo"/>"><i></i></span>
        </div>
        %{--pause for custom css editor improvement--}%
        %{--<div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
        </div>--}%
        <div class="tool-group">
            <span class="toolbar-item save disabled" title="<g:message code="save"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="body cssEditorBody code-mirror-editor-body">
    <form><textarea class="code-area" name='code'></textarea></form>
</div>

<style type="text/css">
.CodeMirror-focused .cm-matchhighlight {
    background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAYAAABytg0kAAAAFklEQVQI12NgYGBgkKzc8x9CMDAwAAAmhwSbidEoSQAAAABJRU5ErkJggg==);
    background-position: bottom;
    background-repeat: repeat-x;
}
.CodeMirror-selected  {
    background-color: #1d87cf !important;
}
.CodeMirror-selectedtext {
    color: white;
}
</style>
