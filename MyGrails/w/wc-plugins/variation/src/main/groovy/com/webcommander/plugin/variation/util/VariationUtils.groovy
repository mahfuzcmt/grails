package com.webcommander.plugin.variation.util

/**
 * Created by shahin on 14/05/2015.
 */
class VariationUtils {
    public static Map getCombinationConfig(Integer index, Long typeId) {
        Map config = [:]
        switch(index) {
            case 0:
                config['xAxis'] = typeId
                break;
            case 1:
                config['yAxis'] = typeId
                break;
            default:
                config['combobox' + (index - 1)] = typeId
                break;
        }
        return config
    }

    public static List cartesianProduct(List _a, List _b) {
        List result = []
        if(!_a) {
            return _b
        }
        if(!_b) {
            return _a
        }
        _a.each{ a->
            result += _b.collect{ b->
                List temp = []
                if(a instanceof List) {
                    temp.addAll(a)
                } else {
                    temp.push(a)
                }
                if(b instanceof List) {
                    temp.addAll(b)
                } else {
                    temp.push(b)
                }
                temp
            }
        }
        return result
    }
}
