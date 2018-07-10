import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.dropTable(
         "product_with_variation_variation_ids", "variation_discount_details_product_with_variation",
                "product_with_variation", "variation_discount_details"
            )
        } catch(Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}