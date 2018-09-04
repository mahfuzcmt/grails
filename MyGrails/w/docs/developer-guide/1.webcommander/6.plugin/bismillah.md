#### WebCommander Plugin
---

WebCommander Plugin basically belongs to Grails Standard Plugin, but we did some customization in WebCommander, So i will
recommend, before start WebCommander plugin will be advantage if explore grails plugin first. 




1. Hook
2. Mixing




<br>

#### 1.0 Plugin creation.

**From IntelliJ IDEA press (Ctrl + Alt + G) from keyboard or Tools --> Grails --> Run Grails Command**

```bash
# Syntax 
create-wc-plugin plugin-name

# Example
create-wc-plugin bismillah

```

Now the plugin created under **Project root directory name** → **wc-plugins** → **plugin-name** 

![Bismillah Plugin](/resource/images/bismillah-plugin-structure.jpg "Bismillah Plugin")



<br>

#### 1.1 Plugin Installation in Development mode.

Add plugin in development mode by adding the plugin name into development-plugin.gradle file. This file is placed inside 
the **Project root directory**. Suppose, we want to added just created plugin **bismillah** in development mode. We could 
do it like this:

```gradle
ext.includePlugins = {
    return [
            "bismillah",
            "blog",
    ] as String[]
}
```






































<br><br>

**Article By:** H.M Touhid Mia, touhid@bitmascot.com
