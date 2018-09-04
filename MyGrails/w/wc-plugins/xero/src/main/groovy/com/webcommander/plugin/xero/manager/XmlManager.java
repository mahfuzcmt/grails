package com.webcommander.plugin.xero.manager;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlManager {
    public static <T> String getXml(T t) throws JAXBException {
        StringWriter xml = new StringWriter();
        JAXBContext context = JAXBContext.newInstance(t.getClass());
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(t, xml);
        return  xml.toString();
    }

    public static <T> T getObject(String xml, Class<T> type) throws JAXBException{
        StreamSource xmlSource = new StreamSource(new StringReader(xml));
        JAXBContext context = JAXBContext.newInstance(type);
        Unmarshaller um = context.createUnmarshaller();
        T t = type.cast(um.unmarshal(xmlSource));
        return  t;
    }
}
