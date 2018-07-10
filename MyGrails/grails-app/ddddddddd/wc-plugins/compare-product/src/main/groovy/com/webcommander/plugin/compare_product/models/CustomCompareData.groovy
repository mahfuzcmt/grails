package com.webcommander.plugin.compare_product.models

/**
 * Created by sadlil on 12/18/2014.
 */
class CustomCompareData {
    String label
    Long matched
    Long rank
    List<String> description

    CustomCompareData() {
        this.description = new ArrayList<String>();
    }

    CustomCompareData(String label, Long matched, Long rank, List<String> description) {
        this.label = label;
        this.matched = matched;
        this.rank = rank;
        this.description = description;
    }
}
