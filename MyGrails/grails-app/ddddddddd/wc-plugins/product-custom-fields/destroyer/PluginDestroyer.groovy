import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeProductWidget('customField')
                    .dropTable('product_checkout_field_options', 'product_checkout_fields_title', 'product_checkout_field', 'category_checkout_field_options', 'category_checkout_fields_title', 'category_checkout_field')
        }catch (Exception e) {
            e.printStackTrace()
        }
        finally {
            destroyUtil.closeConnection()
        }
    }
}