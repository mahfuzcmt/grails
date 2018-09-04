package com.webcommander.plugin.ebay_listing.model

import com.webcommander.JSONSerializable
import com.webcommander.JSONSerializableList
import com.ebay.soap.eBLBaseComponents.CategoryType

class EbayCategoryData extends JSONSerializable implements Comparable<EbayCategoryData> {
    String id
    String name
    String parentId
    List<EbayCategoryData> children = new JSONSerializableList<EbayCategoryData>()

    public EbayCategoryData(CategoryType categoryType, int parentIdx) {
        id = categoryType.categoryID
        name = categoryType.categoryName
        parentId = categoryType.categoryParentID[parentIdx] == categoryType.categoryID ? "0" : categoryType.categoryParentID[parentIdx]
    }

    @Override
    int compareTo(EbayCategoryData o) {
        if(children) {
            Collections.sort(children)
        }
        return name.compareTo(o.name)
    }
}
