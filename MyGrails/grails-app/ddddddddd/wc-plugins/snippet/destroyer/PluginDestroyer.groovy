import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {
    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    void destroy() {
        try {
            destroyUtil.removeWidget('snippet')
                        .removePermission('snippet')
                        .removePermission('snippet_template')
                        .deleteResourceFolders("snippet")
                        .removeFoldersFromModifiableResource("snippet-templates")
            //TODO: if plugin removed from all tenant then below code may work
                        /*.removeFoldersFromSysResource("snippet-templates")*/
                        .dropTable('snippet')
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            destroyUtil.closeConnection()
        }
    }
}