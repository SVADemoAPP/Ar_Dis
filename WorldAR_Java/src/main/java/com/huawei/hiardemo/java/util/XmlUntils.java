package com.huawei.hiardemo.java.util;

import android.util.Log;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
        element.addAttribute(attributeName,attributeValue);
    }

    /**
     * 将修改后的Document存入指定文件
     * @param document
     * @param xmlFile
     * @throws IOException
     */
    public static void saveDocument(Document document, File xmlFile) {
        // 创建输出流
        XMLWriter writer = null;
        try {
            Writer osWrite = new OutputStreamWriter(new FileOutputStream(xmlFile));
            // 获取输出的指定格式
            OutputFormat format = OutputFormat.createPrettyPrint();
            // 设置编码 ，确保解析的xml为UTF-8格式
            format.setEncoding("UTF-8");
            // 指定输出文件以及格式
            writer = new XMLWriter(osWrite, format);// XMLWriter
            // 把document写入xmlFile指定的文件(可以为被解析的文件或者新创建的文件)
            writer.write(document);
            writer.flush();
        } catch (IOException e) {
            Log.e("XmlUtils","保存XML文件失败");
            e.printStackTrace();
        }finally {
            if(writer != null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
