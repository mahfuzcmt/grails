import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.dropTable("svariation_details")
                    .executeStatement("DELETE FROM product_variation_variation_option WHERE product_variation_options_id IN(SELECT id FROM product_variation WHERE details_id IN(SELECT id FROM variation_details WHERE model='standard'))")
                    .executeStatement("DELETE FROM product_variation WHERE details_id IN(SELECT id FROM variation_details WHERE model='standard')")
                    .executeStatement("DELETE FROM variation_details where model='standard'")
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}