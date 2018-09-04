import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.dropTable(
                    "customer_discount_condition_many_customer_many_product",
                "customer_discount_condition_many_customer_many_zone",
                "customer_discount_condition_customer_ids",
                "customer_discount_condition_customer_group_ids",
                "offer_coupon_discount_condition_product_ids",
                "offer_coupon_discount_condition_customer_ids",
                "offer_coupon_discount_condition_customer_group_ids",
                "offer_coupon_discount_condition_category_ids",
                "offer_incentive_and_sell_more_discount_condition_product_ids",
                "offer_incentive_and_sell_more_discount_condition_category_ids",
                "promote_product_discount_condition_product_ids",
                "promote_product_discount_condition_many_product_with_quantity",
                "promote_product_discount_condition_many_product_many_brand",
                "promote_product_discount_condition_many_customer_many_product",
                "promote_product_discount_condition_category_ids",
                "many_customer_many_product_product_ids",
                "many_customer_many_product_customer_ids",
                "many_customer_many_product_customer_group_ids",
                "many_customer_many_product_category_ids",
                "many_customer_many_zone_zone",
                "many_customer_many_zone_customer_ids",
                "many_customer_many_zone_customer_group_ids",
                "many_product_many_brand_product_ids",
                "many_product_many_brand_manufacturer_ids",
                "many_product_many_brand_category_ids",
                "many_product_many_brand_brand_ids",
                "many_product_with_quantity_product_ids",
                "many_product_with_quantity_category_ids",
                "many_product_with_quantity",
                "many_product_many_brand",
                "many_customer_many_zone",
                "many_customer_many_product",
                "promote_product_discount_condition",
                "offer_incentive_and_sell_more_discount_condition",
                "offer_coupon_discount_condition",
                "customer_discount_condition",
                "shipping_discount_details_discount_amount_tier",
                "product_discount_details_product",
                "product_discount_details_category",
                "product_discount_details_discount_qty_tier",
                "amount_discount_details_discount_amount_tier",
                "amount_discount_details",
                "product_discount_details",
                "shipping_discount_details",
                "discount_amount_tier",
                "discount_qty_tier",
                "discount_usage",
                "discount_details",
                "discount",

                "discount_assoc_product",
                "discount_assoc_customer_group",
                "discount_assoc_customer",
                "discount_assoc_category",
                "custom_discount_product",
                "custom_discount",
                "discount_assoc",
                "discount_coupon_usage",
                "discount_coupon",
                "discount_coupon_code",
                "discount_coupon_assoc"

            ).removePermission("discount")
        } catch(Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}