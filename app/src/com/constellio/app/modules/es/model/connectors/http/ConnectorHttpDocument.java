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
package com.constellio.app.modules.es.model.connectors.http;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ConnectorHttpDocument extends ConnectorDocument<ConnectorHttpDocument> {

	public static final String SCHEMA_TYPE = "connectorHttpDocument";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CONNECTOR = ConnectorDocument.CONNECTOR;
	public static final String CONNECTOR_TYPE = ConnectorDocument.CONNECTOR_TYPE;
	public static final String FETCHED = ConnectorDocument.FETCHED;

	public static final String URL = "url";
	public static final String BASE_URI = "baseURI";
	public static final String PARSED_CONTENT = "parsedContent";

	public ConnectorHttpDocument(Record record, MetadataSchemaTypes types) {
		super(record, types, "connectorHttpDocument");
	}

	public ConnectorHttpDocument(Record record, MetadataSchemaTypes types, String typeRequirement) {
		super(record, types, typeRequirement);
	}

	@Override
	public List<String> getDefaultMetadata() {
		return Arrays.asList(CONNECTOR, CONNECTOR_TYPE, URL, BASE_URI, PARSED_CONTENT);
	}

	public String getURL() {
		return get(URL);
	}

	public ConnectorHttpDocument setURL(String url) {
		set(URL, url);
		return this;
	}

	public String getBaseURI() {
		return get(BASE_URI);
	}

	public ConnectorHttpDocument setBaseURI(String baseUri) {
		set(BASE_URI, baseUri);
		return this;
	}

	public String getParsedContent() {
		return get(PARSED_CONTENT);
	}

	public ConnectorHttpDocument setParsedContent(String parsedContent) {
		set(PARSED_CONTENT, parsedContent);
		return this;
	}
}