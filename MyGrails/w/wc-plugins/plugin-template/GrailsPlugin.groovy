package __PACKAGE_NAME__

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class __UNDER_2_CAMEL__GrailsPlugin extends WebCommanderPluginBase {

    def title = "__UNDER_2_CAMEL__"
    def author = "__AUTHOR_NAME__"
    def authorEmail = "developer@webcommander.com"
    def description = '''__UNDER_2_CAMEL__ Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/__PLUGIN_NAME__";
    {
        _plugin = new PluginMeta(identifier: "__PLUGIN_NAME__", name: title)
        hooks = [
                embeddableCss: [taglib: "", callable: ""],
        ]
    }


}
