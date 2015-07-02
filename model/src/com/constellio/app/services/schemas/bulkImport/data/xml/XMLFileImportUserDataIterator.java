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

import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.data.io.services.facades.IOServices;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class XMLFileImportUserDataIterator extends XMLFileImportDataIterator{
    public XMLFileImportUserDataIterator(Reader reader, IOServices ioServices) {
        super(reader, ioServices);
    }

    @Override
    protected ImportData parseRecord()
            throws XMLStreamException {
        //TODO
        /*String type;
        Object value;

        while (xmlReader.hasNext()) {
            int event = xmlReader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = xmlReader.getLocalName();
                   if(localName.equals(elementTag())){
                            fields = new HashMap<>();
                            previousSystemId = getElementId(xmlReader);
                            initElementFields(previousSystemId, fields);
                        }else if(localName.equals(mainElementTag())){
                            patterns = new HashMap<>();
                        }else{
                            type = getType();
                            value = isMultivalue() ? parseMultivalue(xmlReader.getLocalName(), type) : parseScalar(type);

                            if (value != "" && !value.equals("null")) {
                                fields.put(xmlReader.getLocalName(), value);
                            }
                        }
            } else if (event == XMLStreamConstants.END_ELEMENT && (xmlReader.getLocalName().equals(RECORD_TAG)
                    || xmlReader.getLocalName().equals(elementTag()))) {
                ++index;
                return new ImportData(index, schema, previousSystemId, fields);
            }
        }*/
        return null;
    }

    protected void initElementFields(String previousSystemId, Map<String, Object> fields) {
        fields.put(USERNAME, previousSystemId);
    }

    protected String getElementId(XMLStreamReader xmlReader) {
        return xmlReader.getAttributeValue("", USERNAME);
    }

    protected String mainElementTag() {
        return USERS_CREDENTIAL_TAG;
    }

    protected String elementTag() {
        return USER_CREDENTIAL_TAG;
    }
}
