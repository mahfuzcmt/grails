package com.webcommander

import grails.converters.JSON
import grails.util.Holders
import org.grails.core.artefact.DomainClassArtefactHandler

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.text.SimpleDateFormat

class JSONSerializable {

    private Long serialId = Random.newInstance().nextLong();

    private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static isGrailsField(String fieldName) {
        return fieldName.startsWith("org_grails_")
    }

    private def serializeContent(def val, List serialized, StringBuilder builder) {
        if(val == null) {
            builder.append("null");
        } else if (Collection.isAssignableFrom(val.getClass())) {
            builder.append("[");
            val.eachWithIndex { entry, ind ->
                if(ind) {
                    builder.append(",");
                }
                serializeContent(entry, serialized, builder);
            }
            builder.append("]");
        } else if (val instanceof JSONSerializable) {
            serialized.add(serialId);
            val.serializeP(serialized, builder)
        } else if (val instanceof Date) {
            builder.append('"');
            builder.append(dateFormatter.format(val));
            builder.append('"');
        } else if (val instanceof Boolean) {
            builder.append(val.toBoolean());
        } else {
            builder.append('"');
            builder.append(val.toString().encodeAsJavaScript());
            builder.append('"');
        }
    }

    private String serializeP(List<Long> serialized, StringBuilder builder = null) {
        builder = builder ?: new StringBuilder();
        builder.append("{\"sid\":")
        builder.append(serialId)
        if(serialized.contains(serialId)) {
            builder.append('}')
        } else {
            List transients = [];
            if(this.class.metaClass.getMetaProperty("transients")) {
                transients = this.class.transients
            }
            this.class.declaredFields.each {
                def key = it.name;
                if(!transients.contains(key) && !it.synthetic && !Modifier.isStatic(it.getModifiers()) && !Modifier.isFinal(it.getModifiers())) {
                    def val = delegate[key];
                    builder.append(',"')
                    builder.append(key)
                    builder.append('":')
                    serializeContent(val, serialized, builder);
                }
            }
            builder.append("}")
        }
        return builder.toString();
    }

    public String serialize() {
        if(Collection.isAssignableFrom(this.class)) {
            StringBuilder builder = new StringBuilder()
            serializeContent(this, [], builder);
            return builder.toString()
        } else {
            serializeP([]);
        }
    }

    private def deSerializeField(Field field, def val, Map deSerialized) {
        Class cls = field.type;
        if(cls.primitive) {
            if(cls.equals(int.class))
                return Integer.parseInt(val).intValue();
            else if(cls.equals(long.class))
                return Long.parseLong(val).longValue();
            else if(cls.equals(float.class))
                return Float.parseFloat(val).floatValue();
            else if(cls.equals(double.class))
                return Double.parseDouble(val).doubleValue();
            else if(cls.equals(boolean.class))
                return Boolean.parseBoolean("" + val).booleanValue();
        } else if(cls.getPackage().getName().equals("java.lang")) {
            if(val == "null") {
                return null;
            }
            Class[] params = [String.class];
            return cls.getConstructor(params).newInstance(["" + val] as Object[]);
        } else if(cls.equals(Date.class)) {
            return dateFormatter.parse(val);
        } else if(cls.getSuperclass().equals(JSONSerializable)) {
            if(deSerialized[val.sid]) {
                return deSerialized[val.sid];
            }
            def obj;
            if(Holders.grailsApplication.isArtefactOfType(DomainClassArtefactHandler.TYPE, cls) && val.id) {
                obj = cls.get(val.id);
            } else {
                obj = cls.newInstance();
            }
            obj.deSerializeMap(val, deSerialized)
            return obj;
        } else if(cls.equals(Collection.class) || Arrays.asList(cls.getInterfaces()).contains(Collection.class)) {
            Collection obj = cls.interface ? [] : cls.newInstance();
            cls = (Class)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
            val.each { map ->
                if(deSerialized[val.sid]) {
                    obj.add(deSerialized[val.sid]);
                } else {
                    def _obj;
                    if(Holders.grailsApplication.isArtefactOfType(DomainClassArtefactHandler.TYPE, cls) && map.id) {
                        _obj = cls.get(map.id);
                    } else {
                        _obj = cls.newInstance();
                    }
                    _obj.deSerializeMap(map, deSerialized)
                    obj.add(_obj)
                }
            }
            return obj;
        }
    }

    public static JSONSerializable deSerialize(String json, Class<JSONSerializable> serializable) {
        Map map = JSON.parse(json);
        def obj;
        if(Holders.grailsApplication.isArtefactOfType(DomainClassArtefactHandler.TYPE, serializable) && map.id) {
            obj = serializable.get(map.id);
        } else {
            obj = serializable.newInstance();
        }
        obj.deSerializeMap(map, [:])
        return obj;
    }

    public void deSerialize(String json) {
        Map map = JSON.parse(json);
        deSerializeMap(map, [:]);
    }

    private void deSerializeMap(Map map, Map deSerialized) {
        deSerialized[map.sid] = this;
        this.class.declaredFields.each {
            Integer modifier = it.getModifiers()
            if(!it.synthetic && !Modifier.isStatic(modifier) && !Modifier.isFinal(modifier) && !isGrailsField(it.name)) {
                def key = it.name;
                def val = map[key];
                if(null.equals(val)) {
                    this[key] = null;
                } else {
                    this[key] = deSerializeField(it, val, deSerialized);
                }
            }
        }
    }
}
