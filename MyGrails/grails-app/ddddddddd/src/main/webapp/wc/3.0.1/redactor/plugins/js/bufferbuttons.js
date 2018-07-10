(function($R)
{
    $R.add('plugin', 'bufferbuttons', {
        translations: {
            en: {
                "undo": "Undo",
                "redo": "Redo"
            }
        },

        init: function(app) {
            this.app = app;
            this.lang = app.lang;
            this.toolbar = app.toolbar;
        },
        start: function() {

            var undoButtonData = {
                title: this.lang.get('undo'),
                icon: '<i class="re-icon-undo"></i>',
                api: 'module.buffer.undo'
            };
            var redoButtonData = {
                title: this.lang.get('redo'),
                icon: '<i class="re-icon-redo"></i>',
                api: 'module.buffer.redo'
            };
            var $undoButton = this.toolbar.addButtonFirst('undo', undoButtonData);
            var $redoButton = this.toolbar.addButtonAfter('undo', 'redo', redoButtonData);
        }
    });
})(Redactor);