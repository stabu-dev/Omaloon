import groovy.lang.Closure

pluginManagement{
    repositories{
        gradlePluginPortal()
        maven("https://raw.githubusercontent.com/GlennFolker/EntityAnnoMaven/main")
    }

    plugins{
        val entVersion: String by settings
        id("com.github.GlennFolker.EntityAnno") version(entVersion)
    }
}

if(JavaVersion.current().ordinal < JavaVersion.VERSION_17.ordinal){
    throw IllegalStateException("JDK 17 is a required minimum version. Yours: ${System.getProperty("java.version")}")
}

include("annotations")

val localprop = java.util.Properties()
if(file("local.properties").exists()) localprop.load(file("local.properties").reader())
val asmLibPath = localprop["asm_lib_path"]

if(asmLibPath != null){
    println("Loading local AsmLib")
    val root = file(asmLibPath)
    val includeSelfPath = File(root, "includeSelf.gradle").canonicalPath
    apply{
        from(includeSelfPath)
    }
    (extra["includeSelf"] as Closure<*>).call(root)
    //added in includeSelf, then autodeleted
}


val modName: String by settings
rootProject.name = modName