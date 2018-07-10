import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeSiteConfig('gift_card')
                    .removePaymentGateway('GCRD')
                    .removePaymentMeta('GCRD')
                    .removeEmailTemplates('gift-card-recipient')
                    .dropTable('gift_card_usage', 'gift_card');
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            destroyUtil.closeConnection();
        }
    }
}