package com.constellio.app.modules.es.services.mapping;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.ListAssert;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.app.modules.es.services.mapping.ConnectorMappingServiceRuntimeException.ConnectorMappingServiceRuntimeException_InvalidArgument;
import com.constellio.app.modules.es.services.mapping.ConnectorMappingServiceRuntimeException.ConnectorMappingServiceRuntimeException_MetadataAlreadyExist;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class ConnectorMappingServiceAcceptanceTest extends ConstellioTest {

	private LocalDate date1 = new LocalDate();
	private LocalDate date2 = date1.plusDays(2);

	private LocalDateTime datetime1 = new LocalDateTime();
	private LocalDateTime datetime2 = datetime1.plusDays(2);

	ConnectorSmbInstance smbConnectorInstance;
	ConnectorHttpInstance httpConnectorInstance;
	ConnectorHttpInstance anotherHttpConnectorInstance;

	String httpConnectorDocumentSchema;
	String smbConnectorDocumentSchema;
	String smbConnectorFolderSchema;

	Users users = new Users();

	ConnectorManager connectorManager;
	ESSchemasRecordsServices es;
	ConnectorMappingService service;

	ConnectorLogger connectorLogger = new ConsoleConnectorLogger();

	String httpDocumentType = ConnectorHttpDocument.SCHEMA_TYPE;
	String smbDocumentType = ConnectorSmbDocument.SCHEMA_TYPE;
	String smbFolderType = ConnectorSmbFolder.SCHEMA_TYPE;

	ConnectorEventObserver eventObserver;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule().withAllTest(users));

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		service = new ConnectorMappingService(es);
		connectorManager = es.getConnectorManager();

		smbConnectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode("smb").setTitle("Smb connector").setEnabled(false)
				.setDomain("domain").setSeeds(asList("seeds")).setUsername("username").setPassword("password"));

		httpConnectorInstance = connectorManager.createConnector(es.newConnectorHttpInstance()
				.setCode("http").setTitle("Http connector").setEnabled(false)
				.setSeeds("seeds").setIncludePatterns("username"));

		anotherHttpConnectorInstance = connectorManager.createConnector(es.newConnectorHttpInstance()
				.setCode("http2").setTitle("Http connector").setEnabled(false)
				.setSeeds("seeds").setIncludePatterns("username"));

		httpConnectorDocumentSchema = ConnectorHttpDocument.SCHEMA_TYPE + "_" + httpConnectorInstance.getId();
		smbConnectorDocumentSchema = ConnectorSmbDocument.SCHEMA_TYPE + "_" + smbConnectorInstance.getId();
		smbConnectorFolderSchema = ConnectorSmbFolder.SCHEMA_TYPE + "_" + smbConnectorInstance.getId();
	}

	@After
	public void tearDown()
			throws Exception {

		if (eventObserver != null) {
			eventObserver.close();
		}
	}

	@Test
	public void givenNewUndeclaredConnectorFieldReturnedForADocumentThenAppearsHasAvailableFieldsForTheType()
			throws Exception {

		List<ConnectorField> initialHttpConnectorFields = service
				.getConnectorFields(httpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE);

		List<ConnectorField> initialSmbConnectorFolderFields = service
				.getConnectorFields(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE);

		List<ConnectorField> initialSmbConnectorDocumentFields = service
				.getConnectorFields(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE);

		addConnectorFieldsToConnectorInstances();

		getModelLayerFactory().newRecordServices().refresh(httpConnectorInstance, smbConnectorInstance);

		assertThatConnectorFields(httpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE)
				.containsAll(initialHttpConnectorFields)
				.contains(
						new ConnectorField(ConnectorHttpDocument.SCHEMA_TYPE + ":runtimeField1", "runtimeField1", STRING),
						new ConnectorField(ConnectorHttpDocument.SCHEMA_TYPE + ":runtimeField2", "runtimeField2", DATE),
						new ConnectorField(ConnectorHttpDocument.SCHEMA_TYPE + ":runtimeField3", "runtimeField3", DATE_TIME),
						new ConnectorField(ConnectorHttpDocument.SCHEMA_TYPE + ":runtimeField4", "Champ 4", STRING)
				)
				.hasSize(initialHttpConnectorFields.size() + 4);

		assertThatConnectorFields(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE)
				.containsAll(initialSmbConnectorFolderFields)
				.contains(
						new ConnectorField(ConnectorSmbFolder.SCHEMA_TYPE + ":runtimeField5", "runtimeField5", STRING),
						new ConnectorField(ConnectorSmbFolder.SCHEMA_TYPE + ":runtimeField6", "runtimeField6", BOOLEAN),
						new ConnectorField(ConnectorSmbFolder.SCHEMA_TYPE + ":runtimeField7", "Champ 7", STRING)
				)
				.hasSize(initialSmbConnectorFolderFields.size() + 3);

		assertThatConnectorFields(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE)
				.containsAll(initialSmbConnectorDocumentFields)
				.contains(
						new ConnectorField(ConnectorSmbDocument.SCHEMA_TYPE + ":runtimeField8", "runtimeField8", STRING),
						new ConnectorField(ConnectorSmbDocument.SCHEMA_TYPE + ":runtimeField9", "Champ 9", STRING)
				)
				.hasSize(initialSmbConnectorDocumentFields.size() + 2);

		assertThatConnectorFields(anotherHttpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE)
				.containsAll(initialHttpConnectorFields)
				.hasSize(initialHttpConnectorFields.size());

	}

	//FIXME TODO Thiago
	//TODO Thiago
	@Test
	public void whenCreateTargetMetadataThenHasCorrectInfos()
			throws Exception {

		assertThat(service.getTargetMetadata(httpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE)).isEmpty();
		assertThat(service.getTargetMetadata(anotherHttpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE)).isEmpty();
		assertThat(service.getTargetMetadata(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE)).isEmpty();
		assertThat(service.getTargetMetadata(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE)).isEmpty();

		service.createTargetMetadata(httpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE,
				new TargetParams("meta1", "My first metadata", BOOLEAN).withAdvancedSearch(true));
		service.createTargetMetadata(httpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE,
				new TargetParams("MAPmeta2", "My second metadata", STRING).withSearchable(true));

		service.createTargetMetadata(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE,
				new TargetParams("meta1", "My first metadata", DATE));
		service.createTargetMetadata(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE,
				new TargetParams("meta2", "My second metadata", STRING).withSearchable(true));

		service.createTargetMetadata(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE,
				new TargetParams("MAPmeta3", "My third metadata", STRING).withMultivalue(false).withSearchResults(true));
		service.createTargetMetadata(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE,
				new TargetParams("meta4", "My fourth metadata", DATE_TIME).withSearchable(true));

		assertThat(service.getTargetMetadata(httpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE))
				.extracting("code", "type", "label", "multivalue", "searchable").containsOnly(
				tuple(httpConnectorDocumentSchema + "_MAPmeta1", BOOLEAN, "My first metadata", true, false),
				tuple(httpConnectorDocumentSchema + "_MAPmeta2", STRING, "My second metadata", true, true)
		);
		assertThat(service.getTargetMetadata(anotherHttpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE)).isEmpty();
		assertThat(service.getTargetMetadata(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE))
				.extracting("code", "type", "label", "multivalue", "searchable").containsOnly(
				tuple(smbConnectorFolderSchema + "_MAPmeta1", DATE, "My first metadata", true, false),
				tuple(smbConnectorFolderSchema + "_MAPmeta2", STRING, "My second metadata", true, true)
		);
		assertThat(service.getTargetMetadata(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE))
				.extracting("code", "type", "label", "multivalue", "searchable").containsOnly(
				tuple(smbConnectorDocumentSchema + "_MAPmeta3", STRING, "My third metadata", false, false),
				tuple(smbConnectorDocumentSchema + "_MAPmeta4", DATE_TIME, "My fourth metadata", true, true)
		);

		SchemasDisplayManager manager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		assertThat(manager.getAdvancedSearchMetadatas(zeCollection, ConnectorHttpDocument.SCHEMA_TYPE)).extracting("metadataCode")
				.contains(httpConnectorDocumentSchema + "_MAPmeta1").doesNotContain(httpConnectorDocumentSchema + "_MAPmeta2");
		assertThat(manager.getSchema(zeCollection, smbConnectorDocumentSchema).getSearchResultsMetadataCodes())
				.contains(smbConnectorDocumentSchema + "_MAPmeta3").doesNotContain(smbConnectorDocumentSchema + "_MAPmeta4");
	}

	@Test(expected = ConnectorMappingServiceRuntimeException_MetadataAlreadyExist.class)
	public void whenCreateTwoTargetMetadatasWithTheSameCodeThenExceptionThrown() {
		service.createTargetMetadata(httpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE,
				new TargetParams("meta", "My first metadata", STRING));

		service.createTargetMetadata(httpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE,
				new TargetParams("meta", "My second metadata", STRING));
	}

	@Test(expected = ConnectorMappingServiceRuntimeException_InvalidArgument.class)
	public void whenCreateTargetMetadatasWithInvalidCodeThenExceptionThrown() {
		service.createTargetMetadata(smbConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE,
				new TargetParams("meta", "My metadata", STRING));
	}

	@Test(expected = ConnectorMappingServiceRuntimeException_InvalidArgument.class)
	public void whenCreateTargetMetadatasWithInvalidValueTypeThenExceptionThrown() {
		service.createTargetMetadata(httpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE,
				new TargetParams("meta", "My metadata", null));
	}

	@Test(expected = ConnectorMappingServiceRuntimeException_InvalidArgument.class)
	public void whenCreateTargetMetadatasWithInvalidDocumentSchemaTypeThenExceptionThrown() {
		service.createTargetMetadata(httpConnectorInstance, null,
				new TargetParams("meta", "My metadata", STRING));
	}

	@Test(expected = ConnectorMappingServiceRuntimeException_InvalidArgument.class)
	public void whenCreateTargetMetadatasWithInvalidConnectorInstanceThenExceptionThrown() {
		service.createTargetMetadata(null, ConnectorHttpDocument.SCHEMA_TYPE,
				new TargetParams("meta", "My metadata", STRING));
	}

	@Test(expected = ConnectorMappingServiceRuntimeException_InvalidArgument.class)
	public void whenCreateTargetMetadatasWithDocumentSchemaTypeInAnotherConnectorInstanceThenExceptionThrown() {
		service.createTargetMetadata(httpConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE,
				new TargetParams("meta", "My metadata", STRING));
	}

	@Test
	public void whenConfigureMappingThenPersisted()
			throws Exception {

		service.createTargetMetadata(httpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE,
				new TargetParams("meta1", "My first metadata", DATE));
		service.createTargetMetadata(httpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE,
				new TargetParams("meta2", "My second metadata", STRING));

		service.createTargetMetadata(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE,
				new TargetParams("meta1", "My first metadata", BOOLEAN));
		service.createTargetMetadata(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE,
				new TargetParams("meta2", "My second metadata", STRING));

		service.createTargetMetadata(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE,
				new TargetParams("meta3", "My third metadata", STRING));
		service.createTargetMetadata(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE,
				new TargetParams("meta4", "My fourth metadata", DATE_TIME));

		assertThat(service.getMapping(httpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE)).isEmpty();
		assertThat(service.getMapping(anotherHttpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE)).isEmpty();
		assertThat(service.getMapping(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE)).isEmpty();
		assertThat(service.getMapping(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE)).isEmpty();

		Map<String, List<String>> connectorHttpMapping = new HashMap<>();
		connectorHttpMapping.put("MAPmeta1", asList(httpDocumentType + ":runtimeField2"));
		connectorHttpMapping.put("MAPmeta2", asList(httpDocumentType + ":runtimeField1", httpDocumentType + ":runtimeField4"));
		service.setMapping(httpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE, connectorHttpMapping);

		Map<String, List<String>> connectorSmbFolderMapping = new HashMap<>();
		connectorSmbFolderMapping.put("MAPmeta1", asList(smbFolderType + ":runtimeField6"));
		connectorSmbFolderMapping.put("MAPmeta2", asList(smbFolderType + ":runtimeField7", smbFolderType + ":runtimeField5"));
		service.setMapping(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE, connectorSmbFolderMapping);

		Map<String, List<String>> connectorSmbDocumentMapping = new HashMap<>();
		connectorSmbDocumentMapping.put("MAPmeta3", asList(smbDocumentType + ":runtimeField9"));
		service.setMapping(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE, connectorSmbDocumentMapping);

		assertThat(service.getMapping(httpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE)).containsOnly(
				entry("MAPmeta1", asList(httpDocumentType + ":runtimeField2")),
				entry("MAPmeta2", asList(httpDocumentType + ":runtimeField1", httpDocumentType + ":runtimeField4"))
		);
		assertThat(service.getMapping(anotherHttpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE)).isEmpty();
		assertThat(service.getMapping(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE)).containsOnly(
				entry("MAPmeta1", asList(smbFolderType + ":runtimeField6")),
				entry("MAPmeta2", asList(smbFolderType + ":runtimeField7", smbFolderType + ":runtimeField5"))
		);
		assertThat(service.getMapping(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE)).containsOnly(
				entry("MAPmeta3", asList(smbDocumentType + ":runtimeField9"))
		);

		connectorSmbFolderMapping = new HashMap<>();
		connectorSmbFolderMapping.put("MAPmeta3", asList("runtimeField6"));
		connectorSmbFolderMapping.put("MAPmeta2", asList("runtimeField5", "runtimeField7"));
		service.setMapping(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE, connectorSmbFolderMapping);

		service.createTargetMetadata(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE,
				new TargetParams("meta3", "My third metadata", BOOLEAN));
		assertThat(service.getMapping(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE)).containsOnly(
				entry("MAPmeta3", asList("runtimeField6")),
				entry("MAPmeta2", asList("runtimeField5", "runtimeField7"))
		);
	}

	@Test
	public void givenPropertiesMappedWhenAddConnectorDocumentWithfieldsThenMappedToMetadatas()
			throws Exception {

		Metadata folderMeta1 = service.createTargetMetadata(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE,
				new TargetParams("meta1", "My first metadata", STRING));
		Metadata folderMeta2 = service.createTargetMetadata(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE,
				new TargetParams("meta2", "My second metadata", DATE));
		Metadata folderMeta3 = service.createTargetMetadata(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE,
				new TargetParams("meta3", "My third metadata", DATE_TIME));
		Metadata folderMeta4 = service.createTargetMetadata(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE,
				new TargetParams("meta4", "My fourth metadata", BOOLEAN));

		Metadata documentMeta1 = service.createTargetMetadata(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE,
				new TargetParams("meta1", "My first metadata", STRING));
		Metadata documentMeta2 = service.createTargetMetadata(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE,
				new TargetParams("meta2", "My second metadata", DATE));
		Metadata documentMeta3 = service.createTargetMetadata(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE,
				new TargetParams("meta3", "My third metadata", DATE_TIME));
		Metadata documentMeta4 = service.createTargetMetadata(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE,
				new TargetParams("meta4", "My fourth metadata", BOOLEAN));

		eventObserver = new DefaultConnectorEventObserver(es, connectorLogger, SDK_STREAM);

		ConnectorSmbFolder smbFolderAddedBeforeMapping = newValidSmbFolder()
				.addStringProperty("stringFieldA", "value1")
				.addStringProperty("stringFieldB", "value2")
				.addDateProperty("dateFieldA", date1)
				.addDateProperty("dateFieldB", date2)
				.addDateTimeProperty("dateTimeFieldA", datetime1)
				.addDateTimeProperty("dateTimeFieldB", datetime2)
				.addBooleanProperty("booleanFieldA", true)
				.addBooleanProperty("booleanFieldB", false);

		ConnectorSmbDocument smbDocumentAddedBeforeMapping = newValidSmbDocumentInFolder(smbFolderAddedBeforeMapping)
				.addStringProperty("stringFieldA", "value1")
				.addStringProperty("stringFieldB", "value2")
				.addDateProperty("dateFieldA", date1)
				.addDateProperty("dateFieldB", date2)
				.addDateTimeProperty("dateTimeFieldA", datetime1)
				.addDateTimeProperty("dateTimeFieldB", datetime2)
				.addBooleanProperty("booleanFieldA", true)
				.addBooleanProperty("booleanFieldB", false);

		eventObserver.addUpdateEvents(smbFolderAddedBeforeMapping, smbDocumentAddedBeforeMapping);

		Map<String, List<String>> connectorSmbFolderMapping = new HashMap<>();
		connectorSmbFolderMapping.put("MAPmeta1", asList(smbFolderType + ":stringFieldA", smbFolderType + ":stringFieldB"));
		connectorSmbFolderMapping.put("MAPmeta2", asList(smbFolderType + ":dateFieldA", smbFolderType + ":dateFieldB"));
		connectorSmbFolderMapping.put("MAPmeta3", asList(smbFolderType + ":dateTimeFieldA", smbFolderType + ":dateTimeFieldB"));
		connectorSmbFolderMapping.put("MAPmeta4", asList(smbFolderType + ":booleanFieldA", smbFolderType + ":booleanFieldB"));
		service.setMapping(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE, connectorSmbFolderMapping);

		Map<String, List<String>> connectorSmbDocMapping = new HashMap<>();
		connectorSmbDocMapping.put("MAPmeta1", asList(smbDocumentType + ":stringFieldB", smbDocumentType + ":stringFieldA"));
		connectorSmbDocMapping.put("MAPmeta2", asList(smbDocumentType + ":dateFieldB", smbDocumentType + ":dateFieldA"));
		connectorSmbDocMapping.put("MAPmeta3", asList(smbDocumentType + ":dateTimeFieldB", smbDocumentType + ":dateTimeFieldA"));
		connectorSmbDocMapping.put("MAPmeta4", asList(smbDocumentType + ":booleanFieldB", smbDocumentType + ":booleanFieldA"));
		service.setMapping(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE, connectorSmbDocMapping);

		ConnectorSmbFolder smbFolderAddedAfterMapping = newValidSmbFolder()
				.addStringProperty("stringFieldA", "value1")
				.addStringProperty("stringFieldB", "value2")
				.addDateProperty("dateFieldA", date1)
				.addDateProperty("dateFieldB", date2)
				.addDateTimeProperty("dateTimeFieldA", datetime1)
				.addDateTimeProperty("dateTimeFieldB", datetime2)
				.addBooleanProperty("booleanFieldA", true)
				.addBooleanProperty("booleanFieldB", false);

		ConnectorSmbDocument smbDocumentAddedAfterMapping = newValidSmbDocumentInFolder(smbFolderAddedBeforeMapping)
				.addStringProperty("stringFieldA", "value1")
				.addStringProperty("stringFieldB", "value2")
				.addDateProperty("dateFieldA", date1)
				.addDateProperty("dateFieldB", date2)
				.addDateTimeProperty("dateTimeFieldA", datetime1)
				.addDateTimeProperty("dateTimeFieldB", datetime2)
				.addBooleanProperty("booleanFieldA", true)
				.addBooleanProperty("booleanFieldB", false);
		eventObserver.addUpdateEvents(smbFolderAddedAfterMapping, smbDocumentAddedAfterMapping);
		eventObserver.flush();

		assertThat(smbDocumentAddedBeforeMapping.getList(documentMeta1)).isEmpty();
		assertThat(smbDocumentAddedBeforeMapping.getList(documentMeta2)).isEmpty();
		assertThat(smbDocumentAddedBeforeMapping.getList(documentMeta3)).isEmpty();
		assertThat(smbDocumentAddedBeforeMapping.getList(documentMeta4)).isEmpty();

		assertThat(smbFolderAddedBeforeMapping.getList(folderMeta1)).isEmpty();
		assertThat(smbFolderAddedBeforeMapping.getList(folderMeta2)).isEmpty();
		assertThat(smbFolderAddedBeforeMapping.getList(folderMeta3)).isEmpty();
		assertThat(smbFolderAddedBeforeMapping.getList(folderMeta4)).isEmpty();

		assertThat(smbDocumentAddedAfterMapping.getList(documentMeta1)).isEqualTo(asList("value2", "value1"));
		assertThat(smbDocumentAddedAfterMapping.getList(documentMeta2)).isEqualTo(asList(date2, date1));
		assertThat(smbDocumentAddedAfterMapping.getList(documentMeta3)).isEqualTo(asList(datetime2, datetime1));
		assertThat(smbDocumentAddedAfterMapping.getList(documentMeta4)).isEqualTo(asList(false, true));

		assertThat(smbFolderAddedAfterMapping.getList(folderMeta1)).isEqualTo(asList("value1", "value2"));
		assertThat(smbFolderAddedAfterMapping.getList(folderMeta2)).isEqualTo(asList(date1, date2));
		assertThat(smbFolderAddedAfterMapping.getList(folderMeta3)).isEqualTo(asList(datetime1, datetime2));
		assertThat(smbFolderAddedAfterMapping.getList(folderMeta4)).isEqualTo(asList(true, false));

		eventObserver.addUpdateEvents(smbFolderAddedAfterMapping, smbDocumentAddedAfterMapping);
		eventObserver.flush();
		assertThat(smbDocumentAddedAfterMapping.getList(documentMeta1)).isEmpty();
		assertThat(smbDocumentAddedAfterMapping.getList(documentMeta2)).isEmpty();
		assertThat(smbDocumentAddedAfterMapping.getList(documentMeta3)).isEmpty();
		assertThat(smbDocumentAddedAfterMapping.getList(documentMeta4)).isEmpty();

		assertThat(smbFolderAddedAfterMapping.getList(folderMeta1)).isEmpty();
		assertThat(smbFolderAddedAfterMapping.getList(folderMeta2)).isEmpty();
		assertThat(smbFolderAddedAfterMapping.getList(folderMeta3)).isEmpty();
		assertThat(smbFolderAddedAfterMapping.getList(folderMeta4)).isEmpty();

		smbFolderAddedAfterMapping
				.addStringProperty("stringFieldB", "value2")
				.addDateProperty("dateFieldB", date2)
				.addDateTimeProperty("dateTimeFieldB", datetime2)
				.addBooleanProperty("booleanFieldB", false);

		smbDocumentAddedAfterMapping
				.addStringProperty("stringFieldA", "value1")
				.addDateProperty("dateFieldA", date1)
				.addDateTimeProperty("dateTimeFieldA", datetime1)
				.addBooleanProperty("booleanFieldA", true);
		eventObserver.push(asList((ConnectorDocument) smbFolderAddedAfterMapping, smbDocumentAddedAfterMapping));
		eventObserver.flush();

		assertThat(smbDocumentAddedAfterMapping.getList(documentMeta1)).isEqualTo(asList("value1"));
		assertThat(smbDocumentAddedAfterMapping.getList(documentMeta2)).isEqualTo(asList(date1));
		assertThat(smbDocumentAddedAfterMapping.getList(documentMeta3)).isEqualTo(asList(datetime1));
		assertThat(smbDocumentAddedAfterMapping.getList(documentMeta4)).isEqualTo(asList(true));

		assertThat(smbFolderAddedAfterMapping.getList(folderMeta1)).isEqualTo(asList("value2"));
		assertThat(smbFolderAddedAfterMapping.getList(folderMeta2)).isEqualTo(asList(date2));
		assertThat(smbFolderAddedAfterMapping.getList(folderMeta3)).isEqualTo(asList(datetime2));
		assertThat(smbFolderAddedAfterMapping.getList(folderMeta4)).isEqualTo(asList(false));

	}

	private ConnectorSmbFolder newValidSmbFolder() {
		return es.newConnectorSmbFolder(smbConnectorInstance)
				.setUrl("/" + UUIDV1Generator.newRandomId())
				.setTraversalCode(UUIDV1Generator.newRandomId());
	}

	private ConnectorSmbDocument newValidSmbDocumentInFolder(ConnectorSmbFolder folder) {
		return es.newConnectorSmbDocument(smbConnectorInstance)
				.setParent(folder)
				.setUrl("/" + UUIDV1Generator.newRandomId())
				.setTraversalCode(UUIDV1Generator.newRandomId());
	}

	private ConnectorHttpDocument newValidHttpDocument() {
		return es.newConnectorHttpDocument(httpConnectorInstance)
				.setUrl("/" + UUIDV1Generator.newRandomId())
				.setTraversalCode(UUIDV1Generator.newRandomId());
	}

	private ListAssert<ConnectorField> assertThatConnectorFields(ConnectorInstance<?> connectorInstance,
			String connectorDocumentSchemaType) {
		return assertThat(service.getConnectorFields(connectorInstance, connectorDocumentSchemaType))
				.usingFieldByFieldElementComparator();
	}

	private void addConnectorFieldsToConnectorInstances() {
		eventObserver = new DefaultConnectorEventObserver(es, connectorLogger, SDK_STREAM);

		eventObserver.push(asList((ConnectorDocument) newValidHttpDocument()
				.addStringProperty("runtimeField1", "value")
				.addDateProperty("runtimeField2", new LocalDate())
				.addDateTimeProperty("runtimeField3", new LocalDateTime())
				.addStringProperty("runtimeField4", (String) null).withPropertyLabel("runtimeField4", "Champ 4")
		));

		ConnectorSmbFolder smbFolder = newValidSmbFolder()
				.addStringProperty("runtimeField5", "value")
				.addBooleanProperty("runtimeField6", true)
				.addStringProperty("runtimeField7", asList("value1", "value2")).withPropertyLabel("runtimeField7", "Champ 7");

		eventObserver.addUpdateEvents(smbFolder, newValidSmbDocumentInFolder(smbFolder)
				.addStringProperty("runtimeField8", "value")
				.addStringProperty("runtimeField9", (String) null).withPropertyLabel("runtimeField9", "Champ 9")
		);

		eventObserver.close();
		eventObserver = null;
	}
}
