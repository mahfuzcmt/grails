package com.webcommander.plugin.form_editor

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class FormEditorGrailsPlugin extends WebCommanderPluginBase {

    def title = "Form Editor"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Displays forms in widget and manages them'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/form-editor";
    {
        _plugin = new PluginMeta(identifier: "form-editor", name: title)
        hooks=[adminJss:[taglib:"wcform",callable:"adminJSs"]]
    }


}
