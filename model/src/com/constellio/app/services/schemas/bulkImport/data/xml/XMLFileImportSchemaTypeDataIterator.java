/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.services.schemas.bulkImport.data.xml;

import com.constellio.data.io.services.facades.IOServices;
import org.apache.commons.lang3.StringUtils;

import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.util.Map;

public class XMLFileImportSchemaTypeDataIterator extends XMLFileImportDataIterator{
    private static final String CODE = "code";
    private static final String SCHEMAS_TYPE_TAG = "types";
    private static final String SCHEMA_TYPE_TAG = "type";
    public static final String CODE_PREFIX = "codePrefix";
    private String codePrefix;

    public XMLFileImportSchemaTypeDataIterator(Reader reader, IOServices ioServices) {
        super(reader, ioServices);
    }

    protected void initElementFields(String previousSystemId, Map<String, Object> fields) {
    }

    protected String getElementId(XMLStreamReader xmlReader) {
        String code = xmlReader.getAttributeValue("", CODE);
        if(StringUtils.isNotBlank(codePrefix)){
            return codePrefix + "_" + code;
        }else{
            return code;
        }
    }

    protected String mainElementTag() {
        return SCHEMAS_TYPE_TAG;
    }

    protected String elementTag() {
        return SCHEMA_TYPE_TAG;
    }

    protected void initPatterns(XMLStreamReader xmlReader, Map<String, String> patterns) {
        this.codePrefix = xmlReader.getAttributeValue("", CODE_PREFIX);
        patterns.put(CODE_PREFIX, codePrefix);
    }

    public String getCodePrefix() {
        return codePrefix;
    }
}
