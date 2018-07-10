import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.dropTable('order_checkout_fields_title', 'order_checkout_field_options', 'order_custom_data', 'order_checkout_fields');
        }catch(Exception e) {
            e.printStackTrace()
        }
        finally {
            destroyUtil.closeConnection()
        }
    }
}