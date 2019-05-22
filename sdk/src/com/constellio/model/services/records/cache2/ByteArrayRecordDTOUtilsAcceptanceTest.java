package com.constellio.model.services.records.cache2;

import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.data.utils.Holder;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichAllowsZeSchemaType;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ByteArrayRecordDTOUtilsAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	TestsSchemasSetup.ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	TestsSchemasSetup.AnotherSchemaMetadatas anotherSchema = setup.new AnotherSchemaMetadatas();

	MetadataSchemasManager schemasManager;
	RecordServices recordServices;
	ReindexingServices reindexingServices;

	@Before
	public void setup() throws Exception {
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();
		reindexingServices = getModelLayerFactory().newReindexingServices();

		SummaryCacheSingletons.dataStore = new FileSystemRecordsValuesCacheDataStore(new File(newTempFolder(), "test.db"));
	}


	@After
	public void tearDown() throws Exception {
		SummaryCacheSingletons.dataStore.close();
	}

	@Test
	public void whenStoringSingleValueMetadatasInAByteArrayRecordDTOThenStoredAndRetrieved() throws Exception {
		defineSchemasManager().using(setup
				.withABooleanMetadata()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema());

		setup.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(anotherSchema.code())
						.create("booleanMetadata")
						.setType(MetadataValueType.BOOLEAN);
			}
		});

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.booleanMetadata(), true)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), null);

		RecordImpl record2 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), record1.getId())
				.set(zeSchema.booleanMetadata(), false);

		RecordImpl record3 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.booleanMetadata(), null)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), record2.getId());

		RecordImpl record4 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "Gargamel")
				.set(zeSchema.booleanMetadata(), null)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), record3.getId());

		RecordImpl record5 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "Azrael")
				.set(zeSchema.booleanMetadata(), true)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), "GrandSchtroumpfs");

		RecordImpl record6 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "GrandSchtroumpfs")
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), "Gargamel")
				.set(zeSchema.booleanMetadata(), false);

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());

		RecordImpl record7 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
				.set(anotherSchemaType.getMetadata("booleanMetadata"), true)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), null);

		RecordImpl record8 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "Albator")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), record6.getId())
				.set(anotherSchemaType.getMetadata("booleanMetadata"), false);

		RecordImpl record9 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "Goldorak")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), record1.getId());

		recordServices.execute(new Transaction(record1, record2, record3, record4, record5, record6, record7, record8, record9));

		Holder<MetadataSchema> zeschemaHolder = new Holder<>(setup.zeDefaultSchema());
		Holder<MetadataSchema> anotherSchemaHolder = new Holder<>(setup.anotherDefaultSchema());

		ByteArrayRecordDTO dto1 = ByteArrayRecordDTO.create(getModelLayerFactory(), record1.getRecordDTO());
		ByteArrayRecordDTO dto2 = ByteArrayRecordDTO.create(getModelLayerFactory(), record2.getRecordDTO());
		ByteArrayRecordDTO dto3 = ByteArrayRecordDTO.create(getModelLayerFactory(), record3.getRecordDTO());
		ByteArrayRecordDTO dto4 = ByteArrayRecordDTO.create(getModelLayerFactory(), record4.getRecordDTO());
		ByteArrayRecordDTO dto5 = ByteArrayRecordDTO.create(getModelLayerFactory(), record5.getRecordDTO());
		ByteArrayRecordDTO dto6 = ByteArrayRecordDTO.create(getModelLayerFactory(), record6.getRecordDTO());
		ByteArrayRecordDTO dto7 = ByteArrayRecordDTO.create(getModelLayerFactory(), record7.getRecordDTO());
		ByteArrayRecordDTO dto8 = ByteArrayRecordDTO.create(getModelLayerFactory(), record8.getRecordDTO());
		ByteArrayRecordDTO dto9 = ByteArrayRecordDTO.create(getModelLayerFactory(), record9.getRecordDTO());

		assertThat(dto1.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(true);
		assertThat(dto1.get(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isEqualTo(null);
		assertThat(dto2.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(false);
		assertThat(dto2.get(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isEqualTo(record1.getId());
		assertThat(dto3.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(null);
		assertThat(dto3.get(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isEqualTo(record2.getId());
		assertThat(dto4.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(null);
		assertThat(dto4.get(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isEqualTo(record3.getId());
		assertThat(dto5.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(true);
		assertThat(dto5.get(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isEqualTo("GrandSchtroumpfs");
		assertThat(dto6.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(false);
		assertThat(dto6.get(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isEqualTo("Gargamel");
		assertThat(dto7.get(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isEqualTo(true);
		assertThat(dto7.get(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isEqualTo(null);
		assertThat(dto8.get(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isEqualTo(false);
		assertThat(dto8.get(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isEqualTo(record6.getId());
		assertThat(dto9.get(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isEqualTo(null);
		assertThat(dto9.get(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isEqualTo(record1.getId());
	}

	@Test
	public void whenStoringMultivalueMetadatasInAByteArrayRecordDTOThenStoredAndRetrieved() throws Exception {
		defineSchemasManager().using(setup
				.withABooleanMetadata()
				.withATitle()
				.withAStringMetadata(whichIsMultivalue)
				.withAnIntegerMetadata(whichIsMultivalue)
				.withANumberMetadata(whichIsMultivalue)
				.withADateMetadata(whichIsMultivalue)
				.withADateTimeMetadata(whichIsMultivalue)
				.withAReferenceMetadata(whichAllowsZeSchemaType, whichIsMultivalue)
				.withAnEnumMetadata(CopyType.class, whichIsMultivalue)
				.withAReferenceFromAnotherSchemaToZeSchema(whichIsMultivalue));

		LocalDate date = new LocalDate();
		LocalDateTime dateTime = new LocalDateTime();

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.booleanMetadata(), false)
				.set(zeSchema.integerMetadata(), asList(619, 33, -244))
				.set(zeSchema.numberMetadata(), asList(420.69d, -40d, 0.0d))
				.set(Schemas.TITLE, "Johnny B Good")
				.set(zeSchema.stringMetadata(), asList("!", "1", "a"))
				.set(zeSchema.dateMetadata(), asList(date, date.plusDays(3), date.plusDays(-100)))
				//				.set(zeSchema.dateTimeMetadata(), asList(dateTime, dateTime.plusHours(5), dateTime.minusYears(33)))
				.set(zeSchema.referenceMetadata(), null)
				.set(zeSchema.enumMetadata(), asList(CopyType.PRINCIPAL, CopyType.PRINCIPAL, CopyType.SECONDARY));

		/*RecordImpl record2 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "Popeye")
				.set(zeSchema.referenceMetadata(), null*//*asList(record1.getId(), null)*//*);

		RecordImpl record3 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "Olive");
//				.set(zeSchema.referenceMetadata(), asList("Popeye", record1.getId()));

		RecordImpl record4 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "Gontran")
				.set(zeSchema.booleanMetadata(), false);
//				.set(zeSchema.referenceMetadata(), asList("Olive", "Popeye"));

		RecordImpl record5 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "Tintin")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList("Olive", "Gontran"));

		RecordImpl record6 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "Milou")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList(null, null));

		RecordImpl record7 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "CapitaineHaddock")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList(null, "Popeye"));

		RecordImpl record8 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "ProfesseurTournesol")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList(record1.getId(), record1.getId()));*/

		recordServices.execute(new Transaction(record1/*, record2, record3, record4, record5, record6, record7, record8*/));

		Holder<MetadataSchema> zeschemaHolder = new Holder<>(setup.zeDefaultSchema());
		Holder<MetadataSchema> anotherSchemaHolder = new Holder<>(setup.anotherDefaultSchema());

		ByteArrayRecordDTO dto1 = ByteArrayRecordDTO.create(getModelLayerFactory(), record1.getRecordDTO());
		/*ByteArrayRecordDTO dto2 = new ByteArrayRecordDTO(zeschemaHolder, record2.getRecordDTO());
		ByteArrayRecordDTO dto3 = new ByteArrayRecordDTO(zeschemaHolder, record3.getRecordDTO());
		ByteArrayRecordDTO dto4 = new ByteArrayRecordDTO(zeschemaHolder, record4.getRecordDTO());
		ByteArrayRecordDTO dto5 = new ByteArrayRecordDTO(anotherSchemaHolder, record5.getRecordDTO());
		ByteArrayRecordDTO dto6 = new ByteArrayRecordDTO(anotherSchemaHolder, record6.getRecordDTO());
		ByteArrayRecordDTO dto7 = new ByteArrayRecordDTO(anotherSchemaHolder, record7.getRecordDTO());
		ByteArrayRecordDTO dto8 = new ByteArrayRecordDTO(anotherSchemaHolder, record8.getRecordDTO());*/

		assertThat(dto1.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(false);
		assertThat(dto1.get(zeSchema.referenceMetadata().getDataStoreCode())).isEqualTo(asList());
		assertThat(dto1.get(zeSchema.integerMetadata().getDataStoreCode())).isEqualTo(asList(619, 33, -244));
		assertThat(dto1.get(zeSchema.numberMetadata().getDataStoreCode())).isEqualTo(asList(420.69d, -40d, 0.0d));
		assertThat(dto1.get(zeSchema.stringMetadata().getDataStoreCode())).isEqualTo(asList("!", "1", "a"));
		assertThat(dto1.get(Schemas.TITLE.getDataStoreCode())).isEqualTo("Johnny B Good");
		assertThat(dto1.get(zeSchema.dateMetadata().getDataStoreCode())).isEqualTo(asList(date, date.plusDays(3), date.plusDays(-100)));
		//		assertThat(dto1.get(zeSchema.dateTimeMetadata().getDataStoreCode())).isEqualTo(asList(dateTime, dateTime.plusHours(5), dateTime.minusYears(33)));
		assertThat(dto1.get(zeSchema.enumMetadata().getDataStoreCode())).isEqualTo(asList(CopyType.PRINCIPAL.getCode(), CopyType.PRINCIPAL.getCode(), CopyType.SECONDARY.getCode()));
		//		assertThat(dto2.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(null);
		//		assertThat(dto2.get(zeSchema.referenceMetadata().getDataStoreCode())).isEqualTo(asList(record1.getId(), null));
		//		assertThat(dto3.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(null);
		//		assertThat(dto3.get(zeSchema.referenceMetadata().getDataStoreCode())).isEqualTo(asList("Popeye", record1.getId()));
		//		assertThat(dto4.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(false);
		//		assertThat(dto4.get(zeSchema.referenceMetadata().getDataStoreCode())).isEqualTo(asList("Olive", "Popeye"));
		//		assertThat(dto5.get(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isEqualTo(asList("Olive", "Gontran"));
		//		assertThat(dto6.get(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isEqualTo(asList(null, null));
		//		assertThat(dto7.get(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isEqualTo(asList(null, "Popeye"));
		//		assertThat(dto8.get(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isEqualTo(asList(record1.getId(), record1.getId()));

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		//		assertThatRecord(recordServices.getDocumentById(record1.getId())).hasMetadataValue(zeSchema.stringMetadata(), asList("", "1", "a"));
	}

	@Test
	public void whenStoringMetadatasInAByteArrayRecordDTOThenVerifyingTheStoreMetadatas() throws Exception {
		defineSchemasManager().using(setup
				.withABooleanMetadata()
				.withAStringMetadata()
				.withANumberMetadata()
				.withAReferenceMetadata(whichAllowsZeSchemaType, whichIsMultivalue)
				.withAReferenceFromAnotherSchemaToZeSchema(whichIsMultivalue)
				.withAParentReferenceFromZeSchemaToZeSchema());

		setup.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(anotherSchema.code())
						.create("stringMetadata")
						.setType(MetadataValueType.STRING);

				types.getSchema(anotherSchema.code())
						.create("booleanMetadata")
						.setType(MetadataValueType.BOOLEAN);
			}
		});

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.booleanMetadata(), true)
				.set(zeSchema.stringMetadata(), "Howd'y Cowboy ?")
				.set(zeSchema.numberMetadata(), 777)
				.set(zeSchema.referenceMetadata(), null)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), null);

		RecordImpl record2 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "LuckyLuke")
				.set(zeSchema.booleanMetadata(), false)
				.set(zeSchema.stringMetadata(), "Le moment est venu de nous dire au revoir, mon vieux Jolly Jumper.")
				.set(zeSchema.numberMetadata(), 1)
				.set(zeSchema.referenceMetadata(), null)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), record1.getId());

		RecordImpl record3 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "JollyJumper")
				.set(zeSchema.booleanMetadata(), null)
				.set(zeSchema.stringMetadata(), "Je n'aime pas le voir partir seul. Sans moi, il est démonté.")
				.set(zeSchema.numberMetadata(), 8)
				.set(zeSchema.referenceMetadata(), asList("LuckyLuke", record1.getId()))
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), null);

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());

		RecordImpl record4 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "Rantanplan")
				.set(anotherSchema.stringMetadata(), "Woof woof")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList("JollyJumper", "LuckyLuke"));

		RecordImpl record5 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "JoeDalton")
				.set(anotherSchema.stringMetadata(), "Damn you Lucky Luke")
				.set(anotherSchemaType.getMetadata("booleanMetadata"), false)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList(null, null));

		RecordImpl record6 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "JackDalton")
				.set(anotherSchema.stringMetadata(), "Hey Joe !")
				.set(anotherSchemaType.getMetadata("booleanMetadata"), true)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList(null, "LuckyLuke"));

		recordServices.execute(new Transaction(record1, record2, record3, record4, record5, record6));

		Holder<MetadataSchema> zeschemaHolder = new Holder<>(setup.zeDefaultSchema());
		Holder<MetadataSchema> anotherSchemaHolder = new Holder<>(setup.anotherDefaultSchema());

		ByteArrayRecordDTO dto1 = ByteArrayRecordDTO.create(getModelLayerFactory(), record1.getRecordDTO());
		ByteArrayRecordDTO dto2 = ByteArrayRecordDTO.create(getModelLayerFactory(), record2.getRecordDTO());
		ByteArrayRecordDTO dto3 = ByteArrayRecordDTO.create(getModelLayerFactory(), record3.getRecordDTO());
		ByteArrayRecordDTO dto4 = ByteArrayRecordDTO.create(getModelLayerFactory(), record4.getRecordDTO());
		ByteArrayRecordDTO dto5 = ByteArrayRecordDTO.create(getModelLayerFactory(), record5.getRecordDTO());
		ByteArrayRecordDTO dto6 = ByteArrayRecordDTO.create(getModelLayerFactory(), record6.getRecordDTO());

		assertThat(dto1.keySet()).containsOnly(zeSchema.booleanMetadata().getDataStoreCode());
		assertThat(dto2.keySet()).containsOnly(zeSchema.booleanMetadata().getDataStoreCode(), zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode());
		assertThat(dto3.keySet()).containsOnly(zeSchema.referenceMetadata().getDataStoreCode());
		assertThat(dto4.keySet()).containsOnly(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode());
		assertThat(dto5.keySet()).containsOnly(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode(), anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode());
		assertThat(dto5.get(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isEqualTo(false);
		assertThat(dto6.keySet()).containsOnly(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode(), anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode());
		assertThat(dto6.get(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isEqualTo(true);
	}

	@Test
	public void whenStoringMetadatasInAByteArrayRecordDTOThenVerifyingTheStoredValues() throws Exception {
		defineSchemasManager().using(setup
				.withABooleanMetadata()
				.withAStringMetadata()
				.withANumberMetadata()
				.withAReferenceMetadata(whichAllowsZeSchemaType, whichIsMultivalue)
				.withAReferenceFromAnotherSchemaToZeSchema(whichIsMultivalue)
				.withAParentReferenceFromZeSchemaToZeSchema());

		setup.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(anotherSchema.code())
						.create("stringMetadata")
						.setType(MetadataValueType.STRING);

				types.getSchema(anotherSchema.code())
						.create("booleanMetadata")
						.setType(MetadataValueType.BOOLEAN);
			}
		});

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.booleanMetadata(), true)
				.set(zeSchema.stringMetadata(), "Howd'y Cowboy ?")
				.set(zeSchema.numberMetadata(), 777)
				.set(zeSchema.referenceMetadata(), null)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), null);

		RecordImpl record2 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "LuckyLuke")
				.set(zeSchema.booleanMetadata(), false)
				.set(zeSchema.stringMetadata(), "Le moment est venu de nous dire au revoir, mon vieux Jolly Jumper.")
				.set(zeSchema.numberMetadata(), 1)
				.set(zeSchema.referenceMetadata(), null)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), record1.getId());

		RecordImpl record3 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "JollyJumper")
				.set(zeSchema.booleanMetadata(), null)
				.set(zeSchema.stringMetadata(), "Je n'aime pas le voir partir seul. Sans moi, il est démonté.")
				.set(zeSchema.numberMetadata(), 8)
				.set(zeSchema.referenceMetadata(), asList("LuckyLuke", record1.getId()))
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), null);

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());

		RecordImpl record4 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "Rantanplan")
				.set(anotherSchema.stringMetadata(), "Woof woof")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList("JollyJumper", "LuckyLuke"));

		RecordImpl record5 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "JoeDalton")
				.set(anotherSchema.stringMetadata(), "Damn you Lucky Luke")
				.set(anotherSchemaType.getMetadata("booleanMetadata"), false)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList(null, null));

		RecordImpl record6 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "JackDalton")
				.set(anotherSchema.stringMetadata(), "Hey Joe !")
				.set(anotherSchemaType.getMetadata("booleanMetadata"), true)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList(null, "LuckyLuke"));

		recordServices.execute(new Transaction(record1, record2, record3, record4, record5, record6));

		Holder<MetadataSchema> zeschemaHolder = new Holder<>(setup.zeDefaultSchema());
		Holder<MetadataSchema> anotherSchemaHolder = new Holder<>(setup.anotherDefaultSchema());

		ByteArrayRecordDTO dto1 = ByteArrayRecordDTO.create(getModelLayerFactory(), record1.getRecordDTO());
		ByteArrayRecordDTO dto2 = ByteArrayRecordDTO.create(getModelLayerFactory(), record2.getRecordDTO());
		ByteArrayRecordDTO dto3 = ByteArrayRecordDTO.create(getModelLayerFactory(), record3.getRecordDTO());
		ByteArrayRecordDTO dto4 = ByteArrayRecordDTO.create(getModelLayerFactory(), record4.getRecordDTO());
		ByteArrayRecordDTO dto5 = ByteArrayRecordDTO.create(getModelLayerFactory(), record5.getRecordDTO());
		ByteArrayRecordDTO dto6 = ByteArrayRecordDTO.create(getModelLayerFactory(), record6.getRecordDTO());

		assertThat(dto1.getFields().values()).containsOnly(true);
		assertThat(dto2.getFields().values()).containsOnly(false, record1.getId());
		assertThat(dto3.getFields().values()).containsOnly(asList("LuckyLuke", record1.getId()));
		assertThat(dto4.getFields().values()).containsOnly(asList("JollyJumper", "LuckyLuke"));
		assertThat(dto5.getFields().values()).containsOnly(asList(null, null), false);
		assertThat(dto6.getFields().values()).containsOnly(asList(null, "LuckyLuke"), true);
	}

	@Test
	public void whenStoringMetadatasInAByteArrayRecordDTOThenVerifyingTheEntries() throws Exception {
		defineSchemasManager().using(setup
				.withABooleanMetadata()
				.withAStringMetadata()
				.withANumberMetadata()
				.withATitle()
				.withAnIntegerMetadata()
				.withAReferenceMetadata(whichAllowsZeSchemaType, whichIsMultivalue)
				.withAReferenceFromAnotherSchemaToZeSchema(whichIsMultivalue)
				.withAParentReferenceFromZeSchemaToZeSchema());

		setup.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(anotherSchema.code())
						.create("stringMetadata").setMultivalue(true)
						.setType(MetadataValueType.STRING);

				types.getSchema(anotherSchema.code())
						.create("booleanMetadata").setMultivalue(true)
						.setType(MetadataValueType.BOOLEAN);
			}
		});

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.booleanMetadata(), true)
				.set(zeSchema.stringMetadata(), "Howd'y Cowboy ?")
				.set(zeSchema.numberMetadata(), 777d)
				.set(zeSchema.integerMetadata(), 666)
				.set(zeSchema.referenceMetadata(), null)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), null);

		RecordImpl record2 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "LuckyLuke")
				.set(zeSchema.booleanMetadata(), false)
				.set(zeSchema.stringMetadata(), "Le moment est venu de nous dire au revoir, mon vieux Jolly Jumper.")
				.set(zeSchema.numberMetadata(), 0d)
				.set(Schemas.TITLE, "Les aventures de Lucky Luke")
				.set(zeSchema.referenceMetadata(), null)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), record1.getId());

		RecordImpl record3 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "JollyJumper")
				.set(zeSchema.booleanMetadata(), null)
				.set(zeSchema.stringMetadata(), "Je n'aime pas le voir partir seul. Sans moi, il est démonté.")
				.set(zeSchema.numberMetadata(), -8d)
				.set(zeSchema.referenceMetadata(), asList("LuckyLuke", record1.getId()))
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), null);

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());

		RecordImpl record4 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "Rantanplan")
				//				.set(anotherSchema.stringMetadata(), "Woof woof")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList("JollyJumper", "LuckyLuke"));

		RecordImpl record5 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "JoeDalton")
				.set(anotherSchema.stringMetadata(), null/*"Damn you Lucky Luke"*/)
				.set(anotherSchemaType.getMetadata("booleanMetadata"), asList(null, null, false))
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList(null, null));

		RecordImpl record6 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "JackDalton")
				//				.set(anotherSchema.stringMetadata(), "Hey Joe !")
				//				.set(anotherSchemaType.getMetadata("booleanMetadata"), true)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList(null, "LuckyLuke"));

		recordServices.execute(new Transaction(record1, record2, record3, record4, record5, record6));

		Holder<MetadataSchema> zeschemaHolder = new Holder<>(setup.zeDefaultSchema());
		Holder<MetadataSchema> anotherSchemaHolder = new Holder<>(setup.anotherDefaultSchema());

		ByteArrayRecordDTO dto1 = ByteArrayRecordDTO.create(getModelLayerFactory(), record1.getRecordDTO());
		ByteArrayRecordDTO dto2 = ByteArrayRecordDTO.create(getModelLayerFactory(), record2.getRecordDTO());
		ByteArrayRecordDTO dto3 = ByteArrayRecordDTO.create(getModelLayerFactory(), record3.getRecordDTO());
		//		ByteArrayRecordDTO dto4 = new ByteArrayRecordDTO(anotherSchemaHolder, record4.getRecordDTO());
		ByteArrayRecordDTO dto5 = ByteArrayRecordDTO.create(getModelLayerFactory(), record5.getRecordDTO());
		ByteArrayRecordDTO dto6 = ByteArrayRecordDTO.create(getModelLayerFactory(), record6.getRecordDTO());

		// Not using containsOnly since it's contains basic metadatas like estimatedSize_i that cannot really be tested
		// consistently
		//		assertThat(toMap(dto1.entrySet())).contains(entry(zeSchema.booleanMetadata().getDataStoreCode(), true),
		//				entry(zeSchema.numberMetadata().getDataStoreCode(), 777d),
		//				entry(zeSchema.integerMetadata().getDataStoreCode(), 666));
		//
		//		assertThat(toMap(dto2.entrySet())).contains(entry(zeSchema.booleanMetadata().getDataStoreCode(), false),
		//				entry(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode(), record1.getId()),
		//				entry(zeSchema.numberMetadata().getDataStoreCode(), 0d),
		//				entry(Schemas.TITLE.getDataStoreCode(), "Les aventures de Lucky Luke"));
		//
		//		assertThat(toMap(dto3.entrySet())).contains(entry(zeSchema.referenceMetadata().getDataStoreCode(), asList("LuckyLuke", record1.getId())),
		//				entry(zeSchema.numberMetadata().getDataStoreCode(), -8d));

		//		assertThat(toMap(dto4.entrySet())).contains(entry(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode(), asList("JollyJumper", "LuckyLuke")));

		//		assertThat(toMap(dto5.entrySet())).contains(entry(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode(), asList()),
		//				entry(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode(), asList(null, null)));

		assertThat(dto5.get(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isEqualTo(asList(true, false, true));

		//		assertThat(toMap(dto6.entrySet())).contains(entry(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode(), true),
		//				entry(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode(), asList(null, "LuckyLuke")));

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		//		assertThatRecord(recordServices.getDocumentById(record5.getId())).hasMetadataValue(anotherSchemaType.getMetadata("booleanMetadata"), null/*asList("!", "1", "a")*/);
	}

	private Map<String, Object> toMap(Set<Entry<String, Object>> dtoEntrySet) {
		Map<String, Object> mapFromSet = new HashMap<>();

		for (Entry<String, Object> entry : dtoEntrySet) {
			mapFromSet.put(entry.getKey(), entry.getValue());
		}

		return mapFromSet;
	}
}
