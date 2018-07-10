package com.webcommander.plugin.snippet

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class SnippetGrailsPlugin extends WebCommanderPluginBase {

    def title = "Snippet"
    def author = "Md Sajedur Rahman"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''Create & manage snippets'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/snippet";
    {
        _plugin = new PluginMeta(identifier: "snippet", name: title)
        hooks=[adminJss:[taglib:'snippet',callable:'adminJss'],popupCreateForm:[taglib:'snippet',callable:'popupCreateForm'],sitePopup:[taglib:'snippet',callable:'sitePopup']]
    }


}
