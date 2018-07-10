package com.webcommander.plugin.wish_list

import com.webcommander.webcommerce.Product

class WishListItem {
    Long id
    Product product

    static belongsTo = [wishList: WishList]
}
