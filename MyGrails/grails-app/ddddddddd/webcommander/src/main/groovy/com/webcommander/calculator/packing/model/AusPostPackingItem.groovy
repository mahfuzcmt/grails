package com.webcommander.calculator.packing.model

class AusPostPackingItem implements Comparable<AusPostPackingItem> {
    //here the resolution is length > height > width
    Double weight
    Double length
    Double width
    Double height

    public AusPostPackingItem(Double wg) {
        weight = wg
    }

    public AusPostPackingItem(Double wg, Double l, Double w, Double h) {
        List lwh = [l, w, h]
        Collections.sort(lwh)
        //FYI: length > height > width
        length = lwh.get(2)
        width = lwh.get(0)
        height = lwh.get(1)
        weight = wg
    }

    public Double getWeight(boolean isDomestic = false) {
        if(isDomestic) {
            Double cubicWeight = 250 * length * width * height / (100*100*100) //cubic weight = 250 x length x width x height
            if(cubicWeight > weight) {
                return cubicWeight
            }
        }
        return weight
    }

    public Boolean isShippable(boolean isDomestic) {
        boolean shippable = false
        Double shippingWeight = getWeight(isDomestic)
        if(isDomestic) {
            //maxWeight && maxLength && maxDimension
            if(shippingWeight <= 22 && length <= 1.05 && length * width * height <= 0.25) {
                shippable = true
            }
        } else {
            //maxWeight && maxLength && maxDimension(girth); girth = 2 * (height + width)
            if(shippingWeight <= 20 && length <= 1.05 && 2 * (height * 100 + width * 100) <= 140) {
                shippable = true
            }
        }
        return shippable
    }

    public int compareTo(AusPostPackingItem o) { //using lexicographical sort(descending)
        int dif = o.length - length
        if(dif == 0) o.height - height
        if(dif == 0) o.width - width
        return dif
    }
}
