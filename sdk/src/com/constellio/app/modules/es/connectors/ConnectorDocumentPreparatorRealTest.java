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
package com.constellio.app.modules.es.connectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.ConnectorDocumentPreparator;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimistickLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;

public class ConnectorDocumentPreparatorRealTest extends ConstellioTest {

	RecordServices recordServices;
	ESSchemasRecordsServices es;

	ConnectorHttpDocument httpDocument;
	ConnectorManager connectorManager;
	ConnectorHttpInstance connectorInstance;
	String documentSchemaCode;
	MetadataSchema documentSchema;
	Map<String, List<String>> propertiesMapping;

	final String STRING_METADATA1 = "stringMetadata1";
	final String STRING_METADATA2 = "stringMetadata2";

	final String PROPERTY_CODE1 = "propertyCode1";
	final String PROPERTY_CODE2 = "propertyCode2";
	private String prop1value1 = "prop1value1";
	private String prop1value2 = "prop1value2";
	private String prop2value = "prop2value";
	private String exampleUrl = "http://example.url";

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioESModule();

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();

		propertiesMapping = new HashMap<>();

		connectorInstance = connectorManager.createConnector(es.newConnectorHttpInstance().setCode("zeConnector")
				.setSeeds(asList("http://constellio.com")));
		documentSchemaCode = connectorInstance.getDocumentsCustomSchemaCode();
		documentSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(ConnectorHttpDocument.SCHEMA_TYPE).getSchema(documentSchemaCode);

		addMetadataToDocumentSchema();
	}

	@Test
	public void givenHttpDocWithCustomPropertiesWhenApplyingMultiplePropertiesInOneMetaThenRecordIsPopulated()
			throws Exception {
		givenNewHttpDocument()
				.withProperty(PROPERTY_CODE1, prop1value1, prop1value2)
				.andProperty(PROPERTY_CODE2, prop2value);

		propertiesMapping.put(STRING_METADATA1, Arrays.asList(PROPERTY_CODE1, PROPERTY_CODE2));

		new ConnectorDocumentPreparator(propertiesMapping, documentSchema).applyProperties(httpDocument);

		assertThat(httpDocument.getList(STRING_METADATA1)).containsOnly(prop1value1, prop1value2, prop2value);
	}

	@Test
	public void givenHttpDocWithCustomPropertiesWhenApplyingPropertiesThenRecordIsPopulated()
			throws Exception {
		givenNewHttpDocument()
				.withProperty(PROPERTY_CODE1, prop1value1, prop1value2)
				.andProperty(PROPERTY_CODE2, prop2value);

		propertiesMapping.put(STRING_METADATA1, Arrays.asList(PROPERTY_CODE1));
		propertiesMapping.put(STRING_METADATA2, Arrays.asList(PROPERTY_CODE2));

		new ConnectorDocumentPreparator(propertiesMapping, documentSchema).applyProperties(httpDocument);

		assertThat(httpDocument.getList(STRING_METADATA1)).containsOnly(prop1value1, prop1value2);
		assertThat(httpDocument.getList(STRING_METADATA2)).containsOnly(prop2value);
	}

	@Test
	public void givenHttpDocWithDefaultMetadataInMapWhenApplyingPropertiesThenOnlyCustomPropertiesPopulated()
			throws Exception {
		givenNewHttpDocument().withURL(exampleUrl).andProperty(PROPERTY_CODE2, prop2value);

		propertiesMapping.put(ConnectorHttpDocument.URL, Arrays.asList("url"));
		propertiesMapping.put(STRING_METADATA2, Arrays.asList(PROPERTY_CODE2));

		new ConnectorDocumentPreparator(propertiesMapping, documentSchema).applyProperties(httpDocument);

		assertThat(httpDocument.getURL()).isEqualTo(exampleUrl);
		assertThat(httpDocument.getList(STRING_METADATA2)).containsOnly(prop2value);
	}

	@Test
	public void givenCustomMetadataMappedToDefaultPropertyThenProperlyPopulated()
			throws Exception {
		givenNewHttpDocument().withURL(exampleUrl).andProperty(PROPERTY_CODE2, prop2value);

		propertiesMapping.put(STRING_METADATA1, Arrays.asList(ConnectorHttpDocument.URL));
		propertiesMapping.put(STRING_METADATA2, Arrays.asList(PROPERTY_CODE2));

		new ConnectorDocumentPreparator(propertiesMapping, documentSchema).applyProperties(httpDocument);

		assertThat(httpDocument.getList(STRING_METADATA1)).containsOnly(exampleUrl);
		assertThat(httpDocument.getList(STRING_METADATA2)).containsOnly(prop2value);
	}

	private void addMetadataToDocumentSchema()
			throws OptimistickLocking {
		MetadataSchemasManager schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypesBuilder schemaTypesBuilder = schemasManager.modify(zeCollection);
		MetadataSchemaBuilder schemaBuilder = schemaTypesBuilder.getSchemaType(ConnectorHttpDocument.SCHEMA_TYPE)
				.getSchema(documentSchemaCode);

		schemaBuilder.createUndeletable(STRING_METADATA1).setType(MetadataValueType.STRING).setMultivalue(true);
		schemaBuilder.createUndeletable(STRING_METADATA2).setType(MetadataValueType.STRING).setMultivalue(true);

		schemasManager.saveUpdateSchemaTypes(schemaTypesBuilder);
	}


	private ConnectorHttpDocumentSetup givenNewHttpDocument() {
		return new ConnectorHttpDocumentSetup(httpDocument = es.newConnectorHttpDocument(connectorInstance));
	}

	private class ConnectorHttpDocumentSetup {
		private ConnectorHttpDocument document;

		public ConnectorHttpDocumentSetup(ConnectorHttpDocument connectorHttpDocument) {
			this.document = connectorHttpDocument;
		}

		public ConnectorHttpDocumentSetup withProperty(String key, String... values) {
			document.addProperty(key, values);
			return this;
		}

		public ConnectorHttpDocumentSetup andProperty(String key, String... values) {
			return withProperty(key, values);
		}

		public ConnectorHttpDocumentSetup withURL(String url) {
			document.setURL(url);
			return this;
		}

	}
}
