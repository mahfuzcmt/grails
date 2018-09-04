(function() {
    var _def_config = {
        shopping_cart_page_message_display_time: 15000,
        server_message_display_time: 8000,
        newsletter_subscription_message_display_time: 6000,
        checkout_step_message_display_time: 6000,
        validation_newsletter_subscription_error_position: "inline",
        customer_profile_message_display_time: 6000,
        product_details_page_status_time: 5000,
        product_zoom_box_dimension: "1x", //1x,1.25x,1.5x,1.75x,2x
        number_block_in_paginator_count: 7,
        number_block_in_paginator_count_800: 3,
        site_popup_animation_clazz: "anim-fade-zoom"
    };
    app.config = $.extend(_def_config, app.config);
})();