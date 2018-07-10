import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.dropTable("product_variation_variation_option")
                    .dropTable("product_variation")
                    .dropTable("variation_details")
                    .dropTable("variation_option")
                    .dropTable("variation_type")
                    .dropTable("order_variation_item")
                    .removeSiteConfig("product", "enable_matrix_view")
                    .deleteResourceFolders("variation")
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}