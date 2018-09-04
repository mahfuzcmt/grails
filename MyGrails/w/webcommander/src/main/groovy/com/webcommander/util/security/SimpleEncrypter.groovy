package com.webcommander.util.security;
/**
 * Created by sajedur on 01-03-2015.
 */
class SimpleEncrypter {
    long target;

    public SimpleEncrypter(long info) {
        this.target = info;
    }


    public void setTarget(long info) {
        this.target = info;
    }

    public long getCipher() {
        return ((target & 0xffff0000ffff0000l) >> 16) | ((target & 0x0000ffff0000ffffl) << 16);
    }

    public long getInfo() {
        return ((target & 0xffff0000ffff0000l) >> 16) | ((target & 0x0000ffff0000ffffl) << 16);
    }

    public static String getToken(){
        SimpleEncrypter simpleEncrypter = new SimpleEncrypter(System.currentTimeMillis())
        return simpleEncrypter.getCipher() + ""
    }
}
