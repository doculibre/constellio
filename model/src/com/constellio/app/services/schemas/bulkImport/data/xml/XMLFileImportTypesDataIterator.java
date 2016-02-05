package com.constellio.app.services.schemas.bulkImport.data.xml;

import com.constellio.data.io.services.facades.IOServices;

import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.util.Map;

public class XMLFileImportTypesDataIterator extends XMLFileImportDataIterator{
    private static final String CODE = "code";
    private static final String TYPES_TAG = "types";
    private static final String TYPE_TAG = "type";
    public static final String CODE_PREFIX = "codePrefix";
    private String codePrefix;

    public XMLFileImportTypesDataIterator(Reader reader, IOServices ioServices) {
        super(reader, ioServices);
    }

    protected void initElementFields(String previousSystemId, Map<String, Object> fields) {
    }

    protected String getElementId(XMLStreamReader xmlReader) {
        String code = xmlReader.getAttributeValue("", CODE);
        return code;
    }

    protected String mainElementTag() {
        return TYPES_TAG;
    }

    protected String elementTag() {
        return TYPE_TAG;
    }

    protected void initPatterns(XMLStreamReader xmlReader, Map<String, String> patterns) {
        this.codePrefix = xmlReader.getAttributeValue("", CODE_PREFIX);
        patterns.put(CODE_PREFIX, codePrefix);
    }

}
