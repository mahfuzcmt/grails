import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil util = new PluginDestroyUtil()  
        try {
            List metaTags = util.executeQuery("SELECT meta_tag_id FROM evariation_details_meta_tag").collect { it.meta_tag_id};
            util.dropTable("evariation_details_meta_tag")
                    .dropTable("variation_product_video")
                    .dropTable("variation_product_image")
                    .dropTable("evariation_details_option")
                    .dropTable("evariation_description")
                    .dropTable("variation_inventory_history")
                    .dropTable("evariation_details")
                    .executeStatement("DELETE FROM product_variation_variation_option WHERE product_variation_options_id IN(SELECT id FROM product_variation WHERE details_id IN(SELECT id FROM variation_details WHERE model='enterprise'))")
                    .executeStatement("DELETE FROM product_variation WHERE details_id IN(SELECT id FROM variation_details WHERE model='enterprise')")
                    .executeStatement("DELETE FROM variation_details where model='enterprise'")
                    .deleteResourceFolders("variation/product");
            if(metaTags) {
                util.executeStatement("DELETE FROM meta_tag WHERE id IN(${metaTags.join(",")})")
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}