package com.huawei.hiardemo.java.util;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.List;

public class XmlUntils {

    public static Element getRootElement(String filePath){
        SAXReader reader = new SAXReader();
        try {

            return reader.read(new File(filePath)).getRootElement();
        }catch (DocumentException e){
            return null;
        }
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
}
