package com.webcommander.plugin.gallery_galleriffic

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class GalleryGallerifficGrailsPlugin extends WebCommanderPluginBase {

    def title = "Galleriffic Gallery"
    def author = "Md Sajedur Rahman"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''Galleriffic Gallery for Gallery widget'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/gallery-galleriffic";
    {
        _plugin = new PluginMeta(identifier: "gallery-galleriffic", name: title)
        hooks = [adminJss: [taglib: "galleryGalleriffic", callable: "adminJSs"]]
    }


}
