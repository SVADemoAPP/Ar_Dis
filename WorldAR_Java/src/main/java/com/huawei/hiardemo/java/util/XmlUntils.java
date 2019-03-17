package com.huawei.hiardemo.java.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXWriter;

import java.io.File;
import java.util.List;

public class XmlUntils {

    public static Document getDocument(String filePath){
        SAXReader reader = new SAXReader();
        try {
            return reader.read(new File(filePath));
        }catch (DocumentException e){
            return null;
        }
    }

    public static Element getRootElement(Document document){
            return document.getRootElement();
    }

    public static List<Element> getElementListByName(Element element, String elementName){
        return element.elements(elementName);
    }

    public static Element getElementByName(Element element, String elementName){
        return element.element(elementName);
    }

    public static String getAttributeValueByName(Element element, String attributeName){
        return element.attributeValue(attributeName);
    }

    public static void setAttributeValueByName(Element element, String attributeName, String attributeValue){
        element.attributeValue(attributeName,attributeValue);
    }

    public static void writeXmlFile(String filePath, Element rootElement){
        SAXWriter writer = new SAXWriter();
    }
}
