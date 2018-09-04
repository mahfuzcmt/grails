### How to start?
---

1. Clone project from : git clone ssh://git@work.webmascot.com:7999/pdwc/webcommander.git OR you need to collect the URL from ur manager
2. Open IntelliJ IDEA (v2017.2.1 or later) then File → New → Project from existing source / Import → browse project location → import project from external model : Gradle → next → finish
3. From idea left panel, browse : extreme_v3/root_dir → grails-app → conf →  config → application.yml (if not exist the copy from webcommander/root_dir → docs → sample ), here adjust the database username and password
4. Copy webcommander/root_dir → docs → sample → development-plugin.gradle to extreme_v3/root_dir
5. Start the Project (Please select project extreme_v3 from run drop down, left of run/play button )


### How to add plugins in development mode?
---

Let's say we want to add blog plugin with the project
1. extreme_v3/root_dir → development-plugin.gradle if not the please copy from (extreme_v3/root_dir → docs → sample → development-plugin.gradle to extreme_v3/root_dir)
```
ext.includePlugins = {
    return [
            "blog",
    ] as String[]
}
```