package com.webcommander.constants

import com.webcommander.manager.PathManager
import groovy.io.FileType

class ResourceList {
    final static String[] fixedSiteCss = [
            "css/font-awesome/css/font-awesome.min.css",
            "css/widgets/jquery.slider.min.css",
            "css/widgets/jquery.fs.stepper.css",
            "css/widgets/chosen.css",
            "css/widgets/zebra_datepicker.css",
            "css/site/scrollbar.css",
            "css/common/app-base.css",
            "css/site/site-base.css",
            "css/front-end-editor/editor-common.css",
    ]

    final static String[] fixedAdminCss = [
            "css/widgets/zebra_datepicker.css",
            "css/widgets/jquery.slider.min.css",
            "css/widgets/jquery.fs.stepper.css",
            "css/widgets/ui.tree.css",
            "css/widgets/spectrum.css",
            "css/codemirror-editor/lib/codemirror.css",
            "css/codemirror-editor/addon/hint/show-hint.css",
            "css/codemirror-editor/addon/dialog/dialog.css",
            "css/codemirror-editor/theme/eclipse.css",
            "css/liveXml-editor/css/main.css",
            "css/tooltipster/tooltipster.css",
            "css/widgets/chosen.css",
            "css/common/custom-ui.css",
            "css/common/app-base.css",
            "css/admin/admin.css",
            "redactor/redactor.css",
            "redactor/plugins/css/clips.css",
            "css/front-end-editor/editor-common.css",
    ]

    final static String[] fixedSiteCoreJs = [
            "js/utility/constants.js",
            "js/jquery/jquery.min.js",
            "js/jquery/jquery-ui-pos-animate.min.js",
            "js/utility/browser.js",
            "js/utility/utility.js",
            "js/utility/view.js",
            "js/utility/prototype.js",
            "js/jquery/jquery.chosen.js",
            "js/jquery/jquery.chosen.extend.js",
            "js/ui-widgets/accordion.js",
            "js/ui-widgets/paginator.js",
            "js/jquery/jquery.i18n.properties-min.js",
            "js/utility/date.js",
            "js/ui-widgets/custom-ui.js",
            "js/site/base-config.js"
    ]

    final static String[] fixedSiteUIJs = [
            "js/ui-widgets/wcui-widget.js",
            "js/ui-widgets/tab.js",
            "js/jquery/jquery.form.js",
            "js/jquery/jquery.mousewheel.js",
            "js/jquery/jquery.hotkeys.js",
            "js/utility/validation.js",
            "js/ui-widgets/popup.js",
            "js/utility/form.js",
            "js/utility/custom-scrollbar.js",
            "js/ui-widgets/jquery.slider.min.js",
            "js/ui-widgets/jquery.fs.stepper.js",
            "js/ui-widgets/zebra_datepicker.js",
            "js/site/jquery.autocomplete.js",
            "js/site/site.js"
    ]

    final static String[] fixedFrontEndEditorCss = [
            "css/site/perfect-scrollbar.css",
            "redactor/redactor.css",
            "redactor/plugins/css/clips.css",
            "css/front-end-editor/animate.css",
            "css/front-end-editor/floating-menu.css",
            "css/front-end-editor/editor.css",
            "css/front-end-editor/editor-common.css",
            "css/front-end-editor/editor-widget.css",
            "js/colorpicker/css/colorpicker.css"
    ]

    private final static int resourceRootDirPathLength = PathManager.getSystemResourceRoot().length()

    final static String[] fixedAdminJs = [
            "js/utility/constants.js",
            "js/jquery/jquery.min.js",
            "js/jquery/jquery-ui-pos-animate.min.js",
            "js/jquery/jquery.i18n.properties-min.js",
            "js/jquery/jquery.color.js",
            "js/jquery/jquery.hotkeys.js",
            "js/jquery/jquery.autosize-min.js",
            "js/jquery/jquery.form.js",
            "js/utility/browser.js",
            "js/utility/utility.js",
            "js/utility/prototype.js",
            "js/jquery/jquery.chosen.js",
            "js/jquery/jquery.chosen.extend.js",
            "js/jquery/jquery.mousewheel.js",
            "js/jquery/jquery.autoellipsis.min.js",
            "js/utility/webdav-communicator.js",
            "js/utility/progressbar.js",
            "js/ui-widgets/zebra_datepicker.js",
            "js/ui-widgets/jquery.slider.min.js",
            "js/ui-widgets/jquery.fs.stepper.js",
            "js/ui-widgets/spectrum.js",
            "js/utility/custom-scrollbar.js",
            "redactor/redactor.min.js"
    ] + new File(PathManager.getSystemResourceRoot("redactor/plugins/js")).list().collect { js ->
        "redactor/plugins/js/${js}"
    } + [
            "flow-player/flowplayer-3.2.13.min.js",
            "js/utility/undoredo.js",
            "js/utility/cssparser.js",
            "js/utility/swfobject.js",
            "js/aviary/aviary-feather.min.js",
            "js/utility/view.js",
            "js/utility/date.js",
            "js/utility/validation.js",
            "js/utility/form.js",
            "js/utility/drag.js",
            "js/jquery/stl.js",
            "js/jquery/jquery.fileupload.js",
            "js/admin/task-manager.js",
            "js/admin/admin-base.js",
            "js/admin/started-wizard.js",
            "js/admin/status-bar.js",
            "js/admin/widget-base.js",
            "js/ui-widgets/wcui-widget.js",
            "js/ui-widgets/custom-ui.js",
            "js/ui-widgets/paginator.js",
            "js/ui-widgets/accordion.js",
            "js/ui-widgets/feature-tabs.js",
            "js/ui-widgets/popup.js",
            "js/ui-widgets/tab.js",
            "js/ui-widgets/ui.tree.js",
            "js/codemirror-editor/lib/codemirror.js",
            "js/codemirror-editor/addon/hint/show-hint.js",
            "js/codemirror-editor/addon/hint/css-hint.js",
            "js/codemirror-editor/addon/hint/javascript-hint.js",
            "js/codemirror-editor/addon/dialog/dialog.js",
            "js/codemirror-editor/addon/search/search.js",
            "js/codemirror-editor/addon/search/searchcursor.js",
            "js/codemirror-editor/addon/search/match-highlighter.js",
            "js/codemirror-editor/addon/edit/closebrackets.js",
            "js/codemirror-editor/addon/edit/matchbrackets.js",
            "js/codemirror-editor/addon/mode/overlay.js",
            "js/codemirror-editor/mode/css/css.js",
            "js/codemirror-editor/mode/javascript/javascript.js",
            "js/codemirror-editor/mode/xml/xml.js",
            "js/codemirror-editor/mode/htmlmixed/htmlmixed.js",
            "js/tooltipster/jquery.tooltipster.min.js",
            "js/liveXml-editor/loc/xmlEditor.js",
            "js/liveXml-editor/ext/GLR/GLR.js",
            "js/utility/chart.js"
    ] + adminFeatureJss + adminEditorJss + adminWidgetJSs

    private static List<String> getAdminJsAllOfCategory(String category) {
        List jss = []
        new File(PathManager.getSystemResourceRoot("js/$category")).traverse([type: FileType.FILES]) { _file ->
            jss << _file.absolutePath.substring(resourceRootDirPathLength)
        }
        return jss
    }

    private static List<String> getAdminFeatureJss() {
        getAdminJsAllOfCategory("features")
    }

    private static List<String> getAdminEditorJss() {
        getAdminJsAllOfCategory("editors")
    }

    private static List<String> getAdminWidgetJSs() {
        getAdminJsAllOfCategory("app-widgets")
    }

    final static String[] fixedFrontEndEditorJs = [
            "redactor/redactor.min.js",
    ] + new File(PathManager.getSystemResourceRoot("redactor/plugins/js")).list().collect { js ->
        "redactor/plugins/js/${js}"
    } + [
            "js/utility/drag.js",
            "js/jquery/stl.js",
            "js/jquery/jquery.fileupload.js",
            "js/utility/cssparser.js",
            "js/utility/perfect-scrollbar.js",
            "js/colorpicker/js/colorpicker.js",
            "js/front-end-editor/src/fee.editor.js"
    ] + frontEndEditorCoreJSs + frontEndEditorWidgetJSs + frontEndEditorLayoutJSs

    private static List<String> getFrontEndEditorCoreJSs() {
        getAdminJsAllOfCategory("front-end-editor/src/core")
    }

    private static List<String> getFrontEndEditorWidgetJSs() {
        getAdminJsAllOfCategory("front-end-editor/src/widget")
    }

    private static List<String> getFrontEndEditorLayoutJSs() {
        getAdminJsAllOfCategory("front-end-editor/src/layout")
    }

}