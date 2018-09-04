description( "This command performs build of a WebCommander Plugin." ) {
    usage "grails build-wc-plugin <plugin1> <plugin2> <pluginN> "
    argument name:'WebCommander Plugin name', description:'The Plugin stored in ProjectRoot/wc-plugins/plugin-name'
}

String PLUGIN_GRADLE_FILE_LOCATION = "wc-plugins/%s/build.gradle"
String TEMP_BUILDABLE_PLUGIN_LIST = "build/tempBuildablePluginList"
File tempBuildablePluginListFile = new File(TEMP_BUILDABLE_PLUGIN_LIST)
tempBuildablePluginListFile.delete()
File file
args.each {
    if (!it.equals("plugin-template")){
        file = new File(String.format(PLUGIN_GRADLE_FILE_LOCATION, it))
        if (file.exists()){
            tempBuildablePluginListFile << it + "\n"
        }else{
            println("Plugin $it not exists.")
        }
    }else{
        println("${it} It's Not a plugin")
    }
}
if (args.size() != 0){
    gradle.buildWCPlugin()
}else{
    println("Missing parameter plugin-name")
}
