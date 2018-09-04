package com.webcommander.util

class SortAndSearchUtil {
    public static Collection sortInCustomOrder(Collection objs, String matchProperty, List order) {
        return objs.sort {
            order.indexOf(it[matchProperty])
        }
    }

    public static Boolean isSorted(List objs) {
        if(objs.size() <= 1) {
            return true
        }
        def compareElm = objs[0]
        for (int i = 1; i < objs.size(); i ++) {
            if(compareElm > objs[i]) {
                return false
            }
            compareElm = objs[i]
        }
        return true
    }

    public static int binarySearch(List objs, Object obj) {
        Integer size = objs.size()
        Integer low = 0, high = size - 1, mid  = high / 2
        Integer index = -1
        while (true) {
            if(low > high) {
                break
            }
            if(objs[mid] == obj) {
                index = mid
                break
            }
            if(obj < objs[mid]) {
                high = mid - 1
            } else {
                low = mid + 1
            }
            mid = (low + high) / 2
        }
        return index
    }

    public static void sortIfUnsorted(List objs) {
        if(!isSorted(objs)) {
            objs.sort()
        }
    }
}
