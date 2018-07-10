package com.webcommander.plugin.gallery_owl_carousel

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class GalleryOwlCarouselGrailsPlugin extends WebCommanderPluginBase {

    def title = "Owl Carousel Gallery"
    def author = "Md Sajedur Rahman"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''Carousel Gallery for Gallery widget'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/gallery-owl-carousel";
    {
        _plugin = new PluginMeta(identifier: "gallery-owl-carousel", name: title)
        hooks=[adminJss:[taglib:"owlCarousel",callable:"adminJSs"]]
    }


}
