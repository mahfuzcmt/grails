
description( "Create a WebCommander Plugin." ) {
    usage "grails create-wc-plugin <plugin1-name>"
    argument name:'WebCommander Plugin name', description:'The Plugin stored in ProjectRoot/wc-plugins/plugin-name'
}

final String PLUGIN_DIRECTORY = "wc-plugins"
final String PLUGIN_TEMPLATE = PLUGIN_DIRECTORY + File.separator + "plugin-template"

if (args.size() == 0 && args.size() > 1){
    println("Missing parameter plugin-name")
}else{
    String pluginName = args[0].replaceAll(/\B[A-Z]/) { '-' + it }.toLowerCase()
    String packageName = pluginName.replaceAll("-","_")
    String pluginCamelCaseName = packageName.replaceAll( "(_)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() } )
    pluginCamelCaseName = pluginCamelCaseName.capitalize()

    String pluginDirectory = PLUGIN_DIRECTORY + File.separator + pluginName
    File pluginDirectoryFile = new File(pluginDirectory)
    if (pluginDirectoryFile.exists()){
        println("The Plugin Already Exists.")
    }else{
       pluginDirectoryFile.mkdir()
        String author = "WebCommander Developer"
        String and = File.separator
        String pluginRoot = PLUGIN_DIRECTORY + File.separator + pluginName
        String grailsApp = pluginRoot + and + "grails-app"
        String src = pluginRoot + and + "src"
        String mainPackageLocation = "com" + and + "webcommander" + and + "plugin" + and + packageName
        String mainGroovy = src + and + "main" + and + "groovy" + and + mainPackageLocation
        String mainWebapp = src + and + "main" + and + "webapp"
        String integrationTest = src + and + "integration-test" + and + "groovy" + and + mainPackageLocation
        String test = src + and + "test" + and + "groovy"+ and + mainPackageLocation
        String i18n = grailsApp + and + "i18n"
        String init = grailsApp + and + "init" + and + mainPackageLocation
        String conf = grailsApp + and + "conf"
        String controllerRoot = grailsApp + and + "controllers" + and + mainPackageLocation
        String destroyer =  pluginRoot + and + "destroyer"
        String groovyPackageName = "com.webcommander.plugin." + packageName
        String domain = grailsApp + and + "domain" + and + mainPackageLocation
        [
                conf,
                destroyer,
                controllerRoot + and + "controllers",
                domain,
                i18n,
                grailsApp + and + "services" + and + mainPackageLocation,
                grailsApp + and + "taglib" + and + mainPackageLocation,
                grailsApp + and + "utils" + and + mainPackageLocation,
                grailsApp + and + "views" + and + "plugins" + and + packageName,
                mainWebapp + and + "css",
                mainWebapp + and + "images",
                mainWebapp + and + "WEB-INF",
                mainWebapp + and + "js",
                integrationTest,
                mainGroovy,
                init,
                test
        ].each {
            new File(it).mkdirs()
        }
        def locationMapping = [
                "Application_groovy"    : [destination:init,namePrefix:""],
                "application_yml"       : [destination:conf,namePrefix:""],
                "BootStrap_groovy"      : [destination:init,namePrefix:""],
                "build_gradle"          : [destination:pluginRoot,namePrefix:""],
                "GrailsPlugin_groovy"   : [destination:mainGroovy,namePrefix:pluginCamelCaseName],
                "logback_groovy"        : [destination:conf,namePrefix:""],
                "messages_properties"   : [destination:i18n,namePrefix:""],
                "PluginDestroyer_groovy": [destination:destroyer,namePrefix:""],
                "settings_gradle"       : [destination:pluginRoot,namePrefix:""],
                "UrlMappings_groovy"    : [destination:controllerRoot,namePrefix:""],

        ]
        new File(PLUGIN_TEMPLATE).eachFile() { file ->
            String mapKey = file.getName().replaceAll(/[.-]/,"_")
            String content = file.text
            content = content.replaceAll("__PACKAGE_NAME__",groovyPackageName)
            content = content.replaceAll("__PLUGIN_NAME__",pluginName)
            content = content.replaceAll("__AUTHOR_NAME__",author)
            content = content.replaceAll("__UNDER_2_CAMEL__",pluginCamelCaseName)
            new File(locationMapping[mapKey].destination + and + locationMapping[mapKey].namePrefix + file.getName()) << content
        }
    }




    println(packageName + " " + pluginName)



}