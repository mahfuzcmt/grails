package com.webcommander.util;

import org.apache.commons.httpclient.methods.PostMethod;

public class HttpDeleteMethodUtil extends PostMethod {

    public HttpDeleteMethodUtil(String uri) {
        super(uri);
    }

    public String getName() {
        return "DELETE";
    }

}
