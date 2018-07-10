import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil util = PluginDestroyUtil.getInstance()
    public void destroy() {
        try {
            util.dropTable("point_history", "loyalty_point",
                    "special_point_rule_customer_group", "special_point_rule_customer", "special_point_rule",
                    "loyalty_point_on_share_history", "order_referral")
                    .removePaymentGateway("LPP")
                    .removeSiteConfig('loyalty-point')
                    .removeEmailTemplates('loyalty-point-reward-notification')
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}