package com.webcommander

class JSONSerializableList<T extends JSONSerializable> extends ArrayList<T> {
    static {
        JSONSerializableList.metaClass.mixin(T)
    }
}
