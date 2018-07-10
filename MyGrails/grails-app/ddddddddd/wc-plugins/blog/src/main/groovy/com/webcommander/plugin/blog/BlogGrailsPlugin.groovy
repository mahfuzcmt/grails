package com.webcommander.plugin.blog

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class BlogGrailsPlugin extends WebCommanderPluginBase {

    def title = "Blog"
    def author = "WebCommander Developer"
    def authorEmail = "developer@webcommander.com"
    def description = '''Blog Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/blog";
    {
        _plugin = new PluginMeta(identifier: "blog", name: title)
        hooks = [
                embeddableCss: [taglib: "", callable: ""],
                siteSearchResult: [taglib: "blogApp", callable: "siteSearchResult"],
        ]
    }


}
