import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance()
    public void destroy() {
        try {
            destroyUtil.removeSiteConfig('review_rating')
                        .removeWidget('productReview')
                        .removeSiteConfig('compare_product', 'is_rating_active')
                        .removeSiteConfig('category_page', 'is_rating_active')
                        .removeEmailTemplates('product-review-notification')
                        .removePermission('product_review')
                        .dropTable('product_review')
        }catch (Exception e) {
            e.printStackTrace()
        }
        finally {
            destroyUtil.closeConnection()
        }
    }
}