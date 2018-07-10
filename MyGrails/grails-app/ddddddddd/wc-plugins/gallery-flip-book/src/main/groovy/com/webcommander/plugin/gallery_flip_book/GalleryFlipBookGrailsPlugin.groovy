package com.webcommander.plugin.gallery_flip_book

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class GalleryFlipBookGrailsPlugin extends WebCommanderPluginBase {

    def title = "Flip Book Gallery"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Flip Book Gallery for Gallery widget'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/gallery-flip-book";
    {
        _plugin = new PluginMeta(identifier: "gallery-flip-book", name: title)
        hooks=[adminJss:[taglib:"flipBook",callable:"adminJSs"]]
    }


}
