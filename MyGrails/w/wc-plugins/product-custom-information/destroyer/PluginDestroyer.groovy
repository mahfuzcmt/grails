import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.dropTable("product_custom_field_value", "product_custom_field")
        } catch(Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}