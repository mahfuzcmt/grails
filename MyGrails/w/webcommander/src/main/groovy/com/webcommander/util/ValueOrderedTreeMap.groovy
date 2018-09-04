package com.webcommander.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Only put method is supported till now
 */
public class ValueOrderedTreeMap<K, V> extends TreeMap<K, V> {
    private Field rootF;
    private Field sizeF;
    private Field modCountF;
    private Field comparatorF;
    private Field leftF;
    private Field rightF;
    private Method fixAfterInsertionM;

    Constructor localEntry;

    private constructor() {
        Class entry = TreeMap.Entry;
        localEntry = entry.getDeclaredConstructor(Object.class, Object.class, entry);
        localEntry.setAccessible(true);
        rootF = getField("root");
        sizeF = getField("size");
        modCountF = getField("modCount");
        comparatorF = getField("comparator");
        leftF = entry.getDeclaredField("left");
        leftF.setAccessible(true);
        rightF = entry.getDeclaredField("right");
        rightF.setAccessible(true);
        fixAfterInsertionM = getMethod("fixAfterInsertion", entry);
    }

    public ValueOrderedTreeMap() throws Exception {
        constructor();
    }

    public ValueOrderedTreeMap(Comparator vComparator) throws Exception  {
        super(vComparator);
        constructor();
    }

    public Field getField(String field) throws NoSuchFieldException {
        Field fld = TreeMap.class.getDeclaredField(field);
        fld.setAccessible(true);
        return fld;
    }

    public Method getMethod(String name, Class... params) throws NoSuchMethodException {
        Method mthd = TreeMap.class.getDeclaredMethod(name, params);
        mthd.setAccessible(true);
        return mthd;
    }

    public V put(K key, V value) {
        try {
            Map.Entry<K,V> t = (Map.Entry)rootF.get(this);
            if (t == null) {
                rootF.set(this, localEntry.newInstance(key, value, null));
                sizeF.set(this, 1);
                modCountF.set(this, (Integer)modCountF.get(this) + 1);
                return null;
            }
            int cmp;
            Map.Entry<K,V> parent;
            // split comparator and comparable paths
            Comparator cpr = (Comparator)comparatorF.get(this);
            if (cpr != null) {
                while (t != null) {
                    parent = t;
                    cmp = cpr.compare(value, t.getValue());
                    if (cmp < 0)
                        t = (Map.Entry)leftF.get(t);
                    else if (cmp > 0)
                        t = (Map.Entry)rightF.get(t);
                    else
                        return t.setValue(value);
                }
            } else {
                if (key == null)
                    throw new NullPointerException();
                Comparable<? super V> k = (Comparable<? super V>) value;
                while (t != null) {
                    parent = t;
                    cmp = k.compareTo(t.getValue());
                    if (cmp < 0)
                        t = (Map.Entry)leftF.get(t);
                    else if (cmp > 0)
                        t = (Map.Entry)rightF.get(t);
                    else
                        return t.setValue(value);
                }
            }
            Map.Entry<K,V> e = (Map.Entry)localEntry.newInstance(key, value, parent);
            if (cmp < 0)
                leftF.set(parent, e);
            else
                rightF.set(parent, e);
            fixAfterInsertionM.invoke(this, e);
            sizeF.set(this, (Integer)sizeF.get(this) + 1);
            modCountF.set(this, (Integer)modCountF.get(this) + 1);
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
