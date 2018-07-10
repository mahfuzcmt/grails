package com.webcommander.throwables

import com.webcommander.models.CartObject

class CartManagerException extends RuntimeException {

    String message;
    CartObject product;
    List messageArgs

    CartManagerException() {}

    CartManagerException(CartObject product, String message) {
        this.product = product;
        this.message = message;
    }

    CartManagerException(CartObject product, String message, List messageArgs) {
        this.product = product;
        this.message = message;
        this.messageArgs = messageArgs
    }

}
