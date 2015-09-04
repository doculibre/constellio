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
package com.constellio.app.modules.es.ui.pages;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.structures.MapStringListStringStructure;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;

public class EditSchemasConnectorInstancePresenterAcceptTest extends ConstellioTest {

	@Mock EditSchemasConnectorInstanceView view;
	@Mock ConstellioNavigator navigator;
	@Mock RecordVO recordVO;
	Users users = new Users();
	ConnectorManager connectorManager;
	RecordServices recordServices;
	UserServices userServices;
	ESSchemasRecordsServices es;
	String schemaCode;
	Map<Integer, String> properties;

	ConnectorType connectorType;
	ConnectorInstance connectorInstance, anotherConnectorInstace;

	EditSchemasConnectorInstancePresenter presenter;
	MetadataSchemasManager metadataSchemasManager;
	MetadataSchemaTypes types;

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection).withConstellioESModule().withAllTestUsers();

		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigateTo()).thenReturn(navigator);

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices);
		connectorManager = es.getConnectorManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		types = metadataSchemasManager.getSchemaTypes(zeCollection);
		configureConnectorsInstances();

		presenter = spy(new EditSchemasConnectorInstancePresenter(view));

		Map<String, String> properties = new HashMap<>();
		properties.put("aString", String.class.getName());
		properties.put("anotherString", String.class.getName());
		properties.put("aDate", LocalDate.class.getName());

		doReturn(properties).when(presenter).getDefaultAvailableProperties();
	}

	private void configureConnectorsInstances() {
		connectorInstance = connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("zeConnector")
						.setTitle("Ze Connector")
						.setTraversalCode("traversalCode")
						.setEnabled(true)
						.setSeeds(asList("http://constellio.com")));

		anotherConnectorInstace = connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("anotherConnector")
						.setTitle("Another Connector")
						.setTraversalCode("anotherTraversalCode")
						.setEnabled(true)
						.setSeeds(asList("http://constellio.com")));
	}

	@Test
	public void whenCreateMetadataVOThenOk()
			throws Exception {

		presenter.forParams(connectorInstance.getId());

		presenter.createMetadata(String.class.getName(), "zeCode", "Ze label", 0);

		assertThat(presenter.getMetadataVOs()).hasSize(1);

		MetadataVO metadataVO = presenter.getMetadataVOs().get(String.class.getName()).get(0);

		assertThat(metadataVO.getCode())
				.isEqualTo(ConnectorHttpDocument.SCHEMA_TYPE + "_" + connectorInstance.getId() + "_zeCode");
		assertThat(metadataVO.getType()).isEqualTo(MetadataValueType.STRING);
		assertThat(metadataVO.getLabel(view.getSessionContext().getCurrentLocale())).isEqualTo("Ze label");

	}

	@Test
	public void givenSameCodeAndSameTypeWhenCreateMetadataVOThenCreateOnce()
			throws Exception {

		presenter.forParams(connectorInstance.getId());
		presenter.createMetadata(String.class.getName(), "zeCode", "Ze label", 0);

		presenter.createMetadata(String.class.getName(), "zeCode", "Ze label", 0);

		assertThat(presenter.getMetadataVOs()).hasSize(1);

		MetadataVO metadataVO = presenter.getMetadataVOs().get(String.class.getName()).get(0);

		assertThat(metadataVO.getCode())
				.isEqualTo(ConnectorHttpDocument.SCHEMA_TYPE + "_" + connectorInstance.getId() + "_zeCode");
		assertThat(metadataVO.getType()).isEqualTo(MetadataValueType.STRING);
		assertThat(metadataVO.getLabel(view.getSessionContext().getCurrentLocale())).isEqualTo("Ze label");

	}

	@Test
	public void givenSameCodeAndDifferentTypeWhenCreateMetadataVOThenCreateOnce()
			throws Exception {

		presenter.forParams(connectorInstance.getId());
		presenter.createMetadata(String.class.getName(), "zeCode", "Ze label", 0);

		presenter.createMetadata(Double.class.getName(), "zeCode", "Ze label", 0);

		assertThat(presenter.getMetadataVOs()).hasSize(1);

		MetadataVO metadataVO = presenter.getMetadataVOs().get(String.class.getName()).get(0);

		assertThat(metadataVO.getCode())
				.isEqualTo(ConnectorHttpDocument.SCHEMA_TYPE + "_" + connectorInstance.getId() + "_zeCode");
		assertThat(metadataVO.getType()).isEqualTo(MetadataValueType.STRING);
		assertThat(metadataVO.getLabel(view.getSessionContext().getCurrentLocale())).isEqualTo("Ze label");

	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void givenInvalidClassNameWhenCreateMetadataVOThenDoNotCreate()
			throws Exception {

		presenter.forParams(connectorInstance.getId());

		presenter.createMetadata("invalidClassName", "zeCode", "Ze label", 0);

		assertThat(presenter.getMetadataVOs()).isEmpty();
	}

	@Test
	public void whenGetLocalCodeThenOk()
			throws Exception {

		assertThat(presenter.getLocalCode("type_default_code")).isEqualTo("code");
	}

	@Test
	public void whenEditSchemasThenSetteFieldCorrect() {
		schemaCode = ConnectorHttpDocument.SCHEMA_TYPE + "_" + connectorInstance.getId();

		givenMetadataCreatedAndMapping();

		Map<String, List<String>> map = new HashMap<>();
		String code = presenter.getMetadataVOs().get(String.class.getName()).get(0).getCode();
		String code2 = presenter.getMetadataVOs().get(LocalDate.class.getName()).get(0).getCode();

		map.put(code, asList("aString", "anotherString"));
		map.put(code2, asList("aDate"));

		MapStringListStringStructure structure = new MapStringListStringStructure(map);
		connectorInstance = es.getConnectorInstance(connectorInstance.getId());
		connectorInstance.setPropertiesMapping(structure);

		Map<Integer, MetadataVO> settedValues = presenter.getSettedField(properties);

		assertThat(settedValues.get(0).getCode()).isEqualTo(code);
		assertThat(settedValues.get(1).getCode()).isEqualTo(code);
		assertThat(settedValues.get(2).getCode()).isEqualTo(code2);
	}

	@Test
	public void whenSaveNewSchemasThenCreateSchemaMetadataAndMapProperties()
			throws Exception {
		schemaCode = ConnectorHttpDocument.SCHEMA_TYPE + "_" + connectorInstance.getId();

		givenMetadataCreatedAndMapping();
		assertThatMetadataIsCreated();

		connectorInstance = es.getConnectorInstance(connectorInstance.getId());
		assertThat(connectorInstance.getPropertiesMapping().get(schemaCode + "_zeCode"))
				.isEqualTo(asList("aString", "anotherString"));
		assertThat(connectorInstance.getPropertiesMapping().get(schemaCode + "_zeDate"))
				.isEqualTo(asList("aDate"));
	}

	@Test
	public void whenSaveNewSchemasThenEditThenMappingCorrect()
			throws Exception {
		schemaCode = ConnectorHttpDocument.SCHEMA_TYPE + "_" + connectorInstance.getId();

		givenMetadataCreatedAndMapping();
		assertThatMetadataIsCreated();

		presenter.forParams(connectorInstance.getId());

		Map<String, List<MetadataVO>> metadataVos = presenter.getMetadataVOs();

		assertThat(metadataVos).hasSize(2);
		assertThat(metadataVos.get(String.class.getName())).hasSize(2);
		assertThat(metadataVos.get(String.class.getName()).get(0).getCode()).isEqualTo(schemaCode + "_zeCode");
		assertThat(metadataVos.get(String.class.getName()).get(1).getCode()).isEqualTo(schemaCode + "_zeCode");
		assertThat(metadataVos.get(LocalDate.class.getName())).hasSize(1);
		assertThat(metadataVos.get(LocalDate.class.getName()).get(0).getCode()).isEqualTo(schemaCode + "_zeDate");
	}

	@Test
	public void whenEditSchemasThenSavedCorrectly()
			throws Exception {
		schemaCode = ConnectorHttpDocument.SCHEMA_TYPE + "_" + connectorInstance.getId();

		givenMetadataCreatedAndMapping();
		assertThatMetadataIsCreated();

		presenter.forParams(connectorInstance.getId());

		presenter.createMetadata(String.class.getName(), "zeNewCode", "Ze label", 0);
		properties = new HashMap<>();
		Map<Integer, MetadataVO> comboBoxes = new HashMap<>();
		MetadataVO metaZeNew = presenter.getMetadataVOs().get(String.class.getName()).get(0);
		MetadataVO metaZeCode = presenter.getMetadataVOs().get(String.class.getName()).get(1);
		MetadataVO metaDate = presenter.getMetadataVOs().get(LocalDate.class.getName()).get(0);

		comboBoxes.put(0, metaZeNew);
		comboBoxes.put(1, metaZeCode);
		comboBoxes.put(2, metaDate);

		properties.put(0, "aString");
		properties.put(1, "anotherString");
		properties.put(2, "aDate");

		presenter.saveButtonClicked(properties, comboBoxes);

		connectorInstance = es.getConnectorInstance(connectorInstance.getId());
		assertThat(connectorInstance.getPropertiesMapping().get(schemaCode + "_zeCode")).isEqualTo(asList("anotherString"));
		assertThat(connectorInstance.getPropertiesMapping().get(schemaCode + "_zeNewCode")).isEqualTo(asList("aString"));
		assertThat(connectorInstance.getPropertiesMapping().get(schemaCode + "_zeDate")).isEqualTo(asList("aDate"));
	}

	private void givenMetadataCreatedAndMapping() {
		presenter.forParams(connectorInstance.getId());
		presenter.createMetadata(String.class.getName(), "zeCode", "Ze label", 0);
		presenter.createMetadata(LocalDate.class.getName(), "zeDate", "Ze label", 0);

		properties = new HashMap<>();
		Map<Integer, MetadataVO> comboBoxes = new HashMap<>();
		MetadataVO metadataVO = presenter.getMetadataVOs().get(String.class.getName()).get(0);
		MetadataVO metadataVO2 = presenter.getMetadataVOs().get(LocalDate.class.getName()).get(0);

		comboBoxes.put(0, metadataVO);
		comboBoxes.put(1, metadataVO);
		comboBoxes.put(2, metadataVO2);

		properties.put(0, "aString");
		properties.put(1, "anotherString");
		properties.put(2, "aDate");

		presenter.saveButtonClicked(properties, comboBoxes);

	}

	private void assertThatMetadataIsCreated() {

		List<MetadataSchema> metadataSchemas = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(ConnectorHttpDocument.SCHEMA_TYPE).getCustomSchemas();

		boolean ok = false;
		for (MetadataSchema metadataSchema : metadataSchemas) {
			if (metadataSchema.getLocalCode().equals(connectorInstance.getId())) {
				ok = true;
				assertThat(metadataSchema.getMetadata("zeCode")).isNotNull();
			}
		}
		assertThat(ok).isTrue();
	}
}