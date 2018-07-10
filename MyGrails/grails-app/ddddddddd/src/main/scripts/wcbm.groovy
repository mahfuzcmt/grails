description("WebCommander Build Manager") {
    usage "grails wcbm action params"
    argument name: 'plugin', description: 'It Execute Action'
}

final String PLUGIN_DIRECTORY = "wc-plugins"
final String BINARY_COLLECTION_DIRECTORY = "binary-collection"
final String ROOT_BUILD_DIRECTORY = "build"
final String WC_BUILD_DIRECTORY = "webcommander/build"



int i = 1
def buildAllPlugin = {
    i = 1
    new File(PLUGIN_DIRECTORY).eachFile() { file ->
        println("------ ${i} Starting Build ${file.name} ")
        println("-------------------------------------")
        buildWcPlugin(file.name)
        println("")
        println("------ ${i} ${file.name} Build Done")
        println("")
        println("")
        println("")
        i++
    }
}

def cleanAll = {
    i = 1
    def buildFile = new File(ROOT_BUILD_DIRECTORY)
    if (buildFile.exists()) {
        println("------ ${i++} Cleaning Proxy")
        buildFile.deleteDir()
    }

    buildFile = new File(WC_BUILD_DIRECTORY)
    if (buildFile.exists()) {
        println("------ ${i++} Cleaning WebCommander Core")
        buildFile.deleteDir()
    }

    new File(PLUGIN_DIRECTORY).eachFile() { file ->
        println("------ ${i} Cleaning ${file.name}")
        def buildDir = new File(file, "build")
        if (buildDir.exists()) {
            buildDir.deleteDir()
        }
        def gradle = new File(file, ".gradle")
        if (gradle.exists()) {
            gradle.deleteDir()
        }
        i++
    }
}

def buildCore = {
    println("")
    println("------ Building Core Application")
    println("-------------------------------------")
    gradle.assemble()
    println("")
    println("------ Core Build Done")
    println("")
    println("")
}

def createCollectionDir = {
    def source = new File(BINARY_COLLECTION_DIRECTORY)
    if (source.exists()) {
        source.deleteDir()
    }
    source.mkdirs()
}

def collectAllPlugins = {
    i = 1
    createCollectionDir()
    new File(PLUGIN_DIRECTORY).eachFile() { file ->
        def zipFile = new File(file, "build/" + file.name + ".zip")
        if (zipFile.exists()) {
            println("------ ${i} Copping ${file.name}")
            new File(BINARY_COLLECTION_DIRECTORY + "/" + file.name + ".zip") << zipFile.bytes
            println("-------------------------------------")
            println("")
        } else {
            println("------ Not found ${file.name}")
            println("-------------------------------------")
            println("")
        }
    }
}

def collectWar = {
    gradle.copyWar()
}

if (args.size() == 0 && args.size() > 1) {
    println("Action Missing")
} else {
    def wcAction = args[0]
    if (wcAction.equals("build-core-and-collect")) {
        buildCore()
        collectWar()
    } else if (wcAction.equals("build-core")) {
        buildCore()
    } else if (wcAction.equals("build-all")) {
        buildCore()
        buildAllPlugin()
    } else if (wcAction.equals("clean-and-build-all")) {
        println("")
        cleanAll()
        println("")
        println("")

        buildCore()
        println("")
        println("")

        buildAllPlugin()
    } else if (wcAction.equals("collect-all")) {
        collectAllPlugins()
        collectWar()
    } else if (wcAction.equals("plugin")) {
        if (args.size() < 2) {
            println("args missing")
        } else if (args[1].equals("build-all")) {
            println("")
            println("------ Building Core Application")
            println("-------------------------------------")
            gradle.assemble()
            println("")
            println("------ Core Build Done")
            println("")
            println("")
            buildAllPlugin()
        } else if (args[1].equals("collect-all")) {
            collectAllPlugins()
        } else if (args[1].equals("clean-all")) {
            cleanAll()
        } else {
            if (new File(WC_BUILD_DIRECTORY, args[1])){
                println("Building Plugin " + args[1])
                buildWcPlugin(args[1])
            }else{
                println("Undefined Operation.")
            }
        }
    }
}
