package com.webcommander.throwables

class CartManagerExceptionWrap extends RuntimeException {
    List<CartManagerException> exceptions

    public CartManagerExceptionWrap(List<CartManagerException> exceptions) {
        super()
        this.exceptions = exceptions
    }
}
