import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeAutoPage('gift.registry')
                    .removeSiteConfig('gift_registry')
                    .removeSiteConfig('product', 'add_to_gift_registry');
            destroyUtil.dropTable('gift_registry_email', 'gift_registry_item_combination', 'gift_registry_item_variations', 'order_and_registry_mapping', 'gift_registry_item', 'gift_registry');
            destroyUtil.removeSiteMessage('select.only.gift.product.or.remove.all.gift.from.gift')
                    .removeSiteMessage('select.product.from.current.registry')
                    .removeSiteMessage('remove.non.gift.product.from.cart')
                    .removeSiteMessage('address.line')
            destroyUtil.removeEmailTemplates('gift-registry-share')
        } catch(Exception e) {
            e.printStackTrace()
        } finally {
            destroyUtil.closeConnection()
        }
    }
}