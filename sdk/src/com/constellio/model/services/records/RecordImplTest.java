package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordRuntimeException;
import com.constellio.model.entities.records.RecordRuntimeException.CannotMerge;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.ManualDataEntry;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.records.RecordImplRuntimeException.RecordImplException_UnsupportedOperationOnUnsavedRecord;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.testimpl.ZeModifiableStructure;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.TestUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.sdk.tests.TestUtils.asMap;
import static com.constellio.sdk.tests.TestUtils.mockManualMetadata;
import static com.constellio.sdk.tests.TestUtils.mockMetadata;
import static com.constellio.sdk.tests.TestUtils.unmodifiableCollection;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RecordImplTest extends ConstellioTest {

	LocalDateTime shishOClock;

	String zeStringValue = "zeStringValue";
	String theSchemaCode = "a_b";

	String multipleTextMetadataCode = "multipleText";
	String multipleTextMetadataCodeAndType = multipleTextMetadataCode + "_ss";
	String numberMetadataCode = "number";
	String numberMetadataCodeAndType = numberMetadataCode + "_d";
	String otherNumberMetadataCode = "otherNumber";
	String otherNumberMetadataCodeAndType = otherNumberMetadataCode + "_d";
	String textMetadataCode = "text";
	String textMetadataCodeAndType = textMetadataCode + "_s";
	String dateMetadataCode = "date";
	String dateMetadataCodeAndType = dateMetadataCode + "_dt";
	String multipleBooleanMetadataCode = "multipleBoolean";
	String multipleBooleanMetadataCodeAndType = multipleBooleanMetadataCode + "_bs";
	String copyMetadataCode = "copy";
	String copyMetadataCodeAndType = copyMetadataCode + "_s";
	String calculatedMetadataCode = "calculated";
	String calculatedMetadataCodeAndType = calculatedMetadataCode + "_s";

	String factoredMetadataCode = "factoredMetadata";
	String factoredMetadataCodeAndType = factoredMetadataCode + "_s";

	String factoredListMetadataCode = "factoredListMetadata";
	String factoredListMetadataCodeAndType = factoredListMetadataCode + "_ss";

	@Mock Metadata textMetadata, numberMetadata, otherNumberMetadata, multipleTextMetadata, dateMetadata,
			multipleBooleanMetadata, copyMetadata, calculatedMetadata, factoredMetadata, factoredListMetadata;

	@Mock RecordDTO initialState, currentState;
	@Mock MetadataSchema zeSchema;

	String zeStructureInitialValue = "zeStructureInitialValue";
	String zeStructureModifiedValue = "zeStructureModifiedValue";
	String anotherStructureInitialValue = "anotherStructureInitialValue";
	String aThirdStructureInitialValue = "aThirdStructureInitialValue";
	@Mock ZeModifiableStructure zeStructure;
	@Mock ZeModifiableStructure anotherStructure;
	@Mock ZeModifiableStructure aThirdStructure;
	@Mock StructureFactory stringStructureFactory;

	@Mock FieldsPopulator copyfieldsPopulator, copyfieldsPopulator2;

	@Mock Factory<EncryptionServices> encryptionServicesFactory;

	CollectionInfo collectionInfo = new CollectionInfo(zeCollection, "fr", asList("fr"));

	@Before
	public void setUp()
			throws Exception {

		when(initialState.getFields()).thenReturn(newSchemaFields());
		when(currentState.getFields()).thenReturn(newSchemaFields());

		MetadataList zeSchemaMetadatas = new MetadataList(
				asList(multipleTextMetadata, dateMetadata, multipleBooleanMetadata, numberMetadata, otherNumberMetadata,
						textMetadata, copyMetadata, calculatedMetadata, factoredMetadata, factoredListMetadata));
		when(zeSchema.getMetadatas()).thenReturn(zeSchemaMetadatas);

		when(zeSchema.getMetadata(multipleTextMetadataCode)).thenReturn(multipleTextMetadata);
		when(multipleTextMetadata.getLocalCode()).thenReturn(multipleTextMetadataCode);
		when(multipleTextMetadata.getCode()).thenReturn(theSchemaCode + "_" + multipleTextMetadataCode);
		when(multipleTextMetadata.isMultivalue()).thenReturn(true);
		when(multipleTextMetadata.getDataStoreType()).thenReturn("ss");
		when(multipleTextMetadata.getDataStoreCode()).thenReturn(multipleTextMetadataCodeAndType);
		when(multipleTextMetadata.getDataEntry()).thenReturn(new ManualDataEntry());

		when(zeSchema.getMetadata(dateMetadataCode)).thenReturn(dateMetadata);
		when(dateMetadata.getLocalCode()).thenReturn(dateMetadataCode);
		when(dateMetadata.getCode()).thenReturn(theSchemaCode + "_" + dateMetadataCode);
		when(dateMetadata.getDataStoreType()).thenReturn("dt");
		when(dateMetadata.getDataStoreCode()).thenReturn(dateMetadataCodeAndType);
		when(dateMetadata.getDataEntry()).thenReturn(new ManualDataEntry());

		when(zeSchema.getMetadata(multipleBooleanMetadataCode)).thenReturn(multipleBooleanMetadata);
		when(multipleBooleanMetadata.getLocalCode()).thenReturn(multipleBooleanMetadataCode);
		when(multipleBooleanMetadata.getCode()).thenReturn(theSchemaCode + "_" + multipleBooleanMetadataCode);
		when(multipleBooleanMetadata.isMultivalue()).thenReturn(true);
		when(multipleBooleanMetadata.getDataStoreType()).thenReturn("bs");
		when(multipleBooleanMetadata.getDataStoreCode()).thenReturn(multipleBooleanMetadataCodeAndType);
		when(multipleBooleanMetadata.getDataEntry()).thenReturn(new ManualDataEntry());

		when(zeSchema.getMetadata(numberMetadataCode)).thenReturn(numberMetadata);
		when(numberMetadata.getLocalCode()).thenReturn(numberMetadataCode);
		when(numberMetadata.getCode()).thenReturn(theSchemaCode + "_" + numberMetadataCode);
		when(numberMetadata.getDataStoreType()).thenReturn("d");
		when(numberMetadata.getDataStoreCode()).thenReturn(numberMetadataCodeAndType);
		when(numberMetadata.getDataEntry()).thenReturn(new ManualDataEntry());

		when(zeSchema.getMetadata(otherNumberMetadataCode)).thenReturn(otherNumberMetadata);
		when(otherNumberMetadata.getLocalCode()).thenReturn(otherNumberMetadataCode);
		when(otherNumberMetadata.getCode()).thenReturn(theSchemaCode + "_" + otherNumberMetadataCode);
		when(otherNumberMetadata.getDataStoreType()).thenReturn("d");
		when(otherNumberMetadata.getDataStoreCode()).thenReturn(otherNumberMetadataCodeAndType);
		when(otherNumberMetadata.getDataEntry()).thenReturn(new ManualDataEntry());

		when(zeSchema.getMetadata(textMetadataCode)).thenReturn(textMetadata);
		when(textMetadata.getLocalCode()).thenReturn(textMetadataCode);
		when(textMetadata.getCode()).thenReturn(theSchemaCode + "_" + textMetadataCode);
		when(textMetadata.getDataStoreType()).thenReturn("s");
		when(textMetadata.getDataStoreCode()).thenReturn(textMetadataCodeAndType);
		when(textMetadata.getDataEntry()).thenReturn(new ManualDataEntry());

		when(zeSchema.getMetadata(copyMetadataCode)).thenReturn(copyMetadata);
		when(copyMetadata.getLocalCode()).thenReturn(copyMetadataCode);
		when(copyMetadata.getCode()).thenReturn(theSchemaCode + "_" + copyMetadataCode);
		when(copyMetadata.getDataStoreType()).thenReturn("s");
		when(copyMetadata.getDataStoreCode()).thenReturn(copyMetadataCodeAndType);
		when(copyMetadata.getDataEntry()).thenReturn(new CopiedDataEntry(aString(), aString()));

		when(zeSchema.getMetadata(calculatedMetadataCode)).thenReturn(calculatedMetadata);
		when(calculatedMetadata.getLocalCode()).thenReturn(calculatedMetadataCode);
		when(calculatedMetadata.getCode()).thenReturn(theSchemaCode + "_" + calculatedMetadataCode);
		when(calculatedMetadata.getDataStoreType()).thenReturn("s");
		when(calculatedMetadata.getDataStoreCode()).thenReturn(calculatedMetadataCodeAndType);
		when(calculatedMetadata.getDataEntry()).thenReturn(new CalculatedDataEntry(null));

		when(zeSchema.getMetadata(factoredMetadataCode)).thenReturn(factoredMetadata);
		when(factoredMetadata.getLocalCode()).thenReturn(factoredMetadataCode);
		when(factoredMetadata.getCode()).thenReturn(theSchemaCode + "_" + factoredMetadataCode);
		when(factoredMetadata.getDataStoreType()).thenReturn("s");
		when(factoredMetadata.getDataStoreCode()).thenReturn(factoredMetadataCodeAndType);
		when(factoredMetadata.getType()).thenReturn(MetadataValueType.STRING);
		when(factoredMetadata.getStructureFactory()).thenReturn((StructureFactory) stringStructureFactory);
		when(factoredMetadata.getDataEntry()).thenReturn(new ManualDataEntry());

		when(zeSchema.getMetadata(factoredListMetadataCode)).thenReturn(factoredListMetadata);
		when(factoredListMetadata.getLocalCode()).thenReturn(factoredListMetadataCode);
		when(factoredListMetadata.getCode()).thenReturn(theSchemaCode + "_" + factoredListMetadataCode);
		when(factoredListMetadata.getDataStoreType()).thenReturn("s");
		when(factoredListMetadata.getDataStoreCode()).thenReturn(factoredListMetadataCodeAndType);
		when(factoredListMetadata.getType()).thenReturn(MetadataValueType.STRING);
		when(factoredListMetadata.isMultivalue()).thenReturn(true);
		when(factoredListMetadata.getStructureFactory()).thenReturn((StructureFactory) stringStructureFactory);
		when(factoredListMetadata.getDataEntry()).thenReturn(new ManualDataEntry());


	}

	@Test(expected = UnsupportedOperationException.class)
	public void cannotAddRemoveFieldValuesUsingGetModifiedValues()
			throws Exception {
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");
		record.getModifiedValues().clear();
	}

	@Test(expected = RecordRuntimeException.CannotModifyADisconnectedRecord.class)
	public void givenDisconnectedRecordWhenSetMetadataValueThenException()
			throws Exception {
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");
		record.set(textMetadata, "theValue");
		record.markAsDisconnected();
		try {
			record.set(textMetadata, "thisValueCannotBeSet");
		} finally {
			assertThat(record.<String>get(textMetadata)).isEqualTo("theValue");
		}
	}

	@Test(expected = RecordRuntimeException.RecordDTORequired.class)
	public void cannotRefreshWithNullRecordDTO()
			throws Exception {
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");
		record.refresh(2, null);

	}

	@Test
	public void whenRefreshingThenVersionAndDTOSet()
			throws Exception {

		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");

		when(initialState.getId()).thenReturn(record.getId());
		when(currentState.getId()).thenReturn(record.getId());

		record.refresh(1, initialState);
		assertThat(record.getVersion()).isEqualTo(1);
		assertThat(record.getRecordDTO()).isEqualTo(initialState);

		record.refresh(2, currentState);
		assertThat(record.getVersion()).isEqualTo(2);
		assertThat(record.getRecordDTO()).isEqualTo(currentState);

	}

	@Test
	public void whenRefreshingThenClearChanges()
			throws Exception {
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");

		when(initialState.getId()).thenReturn(record.getId());

		record.set(textMetadata, "value");
		assertThat(record.<String>get(textMetadata)).isEqualTo("value");
		assertThat(record.isDirty()).isTrue();

		record.refresh(1, initialState);
		assertThat(record.<String>get(textMetadata)).isNull();
		assertThat(record.isDirty()).isFalse();
	}

	@Test
	public void givenNewRecordWhenProducingRecordDTOThenProducedWithFieldValues() {
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");
		record.set(multipleTextMetadata, asList("firstValue"));
		record.set(dateMetadata, "secondValue");

		when(copyfieldsPopulator.populateCopyfields(zeSchema, record))
				.thenReturn(TestUtils.asMap("copiedField1", (Object) "copiedValue1", "copiedField2", "copiedValue2"));

		when(copyfieldsPopulator2.populateCopyfields(zeSchema, record))
				.thenReturn(TestUtils.asMap("copiedField3", (Object) "copiedValue3"));

		MetadataList metadatas = new MetadataList(textMetadata, multipleTextMetadata, dateMetadata);
		when(zeSchema.getMetadatas()).thenReturn(metadatas);

		RecordDTO recordDTO = record
				.toNewDocumentDTO(zeSchema, asList(copyfieldsPopulator, copyfieldsPopulator2));
		assertThat(recordDTO.getId()).isNotNull();
		assertThat(recordDTO.getLoadedFields()).isNull();
		assertThat(recordDTO.getVersion()).isEqualTo(-1L);
		assertThat(recordDTO.getFields()).containsOnly(
				entry(multipleTextMetadataCodeAndType, asList("firstValue")),
				entry(dateMetadataCodeAndType, "secondValue"),
				entry("collection_s", "zeCollection"),
				entry("schema_s", "a_b"),
				entry("estimatedSize_i", 260));

		assertThat(recordDTO.getCopyFields()).containsOnly(
				entry("copiedField1", "copiedValue1"),
				entry("copiedField2", "copiedValue2"),
				entry("copiedField3", "copiedValue3"));

	}

	@Test
	public void givenRecordWithModifiedValuesWhenVerifyingMetadataModifiedThenReturnsTrue() {
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");
		record.set(dateMetadata, "secondValue");

		assertThat(record.isModified(dateMetadata));
	}

	@Test
	public void givenRecordWithoutModifiedValuesWhenVerifyingMetadataModifiedThenReturnsFalse() {
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");

		assertThat(record.isModified(dateMetadata)).isFalse();
	}

	@Test(expected = RecordRuntimeException.RecordIsAlreadySaved.class)
	public void givenSavedRecordWhenProducingRecordDTOThenProducedWithFieldValuesAndCurrentDTO() {
		Map<String, Object> fields = newSchemaFields();
		fields.put(multipleTextMetadataCodeAndType, asList("thisValueWillBeReplaced"));
		fields.put(multipleBooleanMetadataCodeAndType, asList("thirdValue"));
		RecordDTO savedRecordDTO = new SolrRecordDTO("zeOldId", 1L, null, fields);
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");
		record.refresh(1L, savedRecordDTO);

		record.set(multipleTextMetadata, asList("firstValue"));
		record.set(dateMetadata, "secondValue");

		MetadataList metadatas = new MetadataList(textMetadata, multipleTextMetadata, dateMetadata);
		when(zeSchema.getMetadatas()).thenReturn(metadatas);

		record.toNewDocumentDTO(zeSchema, asList(copyfieldsPopulator, copyfieldsPopulator2));

	}

	@Test
	public void givenValueEqualToRecordDTOWhenSettingMetadataThenNotAddedToModifiedValuesNotDirty()
			throws Exception {
		Map<String, Object> fields = newSchemaFields();
		fields.put(multipleTextMetadataCodeAndType, asList("firstValue"));
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");

		RecordDTO savedRecordDTO = new SolrRecordDTO(record.getId(), 1L, null, fields);
		record.refresh(1L, savedRecordDTO);

		record.set(multipleTextMetadata, asList("firstValue"));
		assertThat(record.isDirty()).isFalse();
		assertThat(record.getModifiedValues()).isEmpty();
		assertThat(record.<List<String>>get(multipleTextMetadata)).isEqualTo(asList("firstValue"));
	}

	@Test
	public void whenSettingDifferentValueThenAddedToModifiedValueAndRecordBecomeDirty()
			throws Exception {
		Map<String, Object> fields = newSchemaFields();
		fields.put(multipleTextMetadataCodeAndType, asList("firstValue"));

		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");

		RecordDTO savedRecordDTO = new SolrRecordDTO(record.getId(), 1L, null, fields);
		record.refresh(1L, savedRecordDTO);

		record.set(multipleTextMetadata, asList("otherValueValue"));
		assertThat(record.isDirty()).isTrue();
		assertThat(record.getModifiedValues()).hasSize(1);
		assertThat(record.<List<String>>get(multipleTextMetadata)).isEqualTo(asList("otherValueValue"));
	}

	@Test
	public void givenMetadataModifiedWhenSettingOriginalValueThenModifiedValueRemovedNotDirtyAnymore()
			throws Exception {
		Map<String, Object> fields = newSchemaFields();
		fields.put(multipleTextMetadataCodeAndType, asList("firstValue"));

		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");

		RecordDTO savedRecordDTO = new SolrRecordDTO(record.getId(), 1L, null, fields);
		record.refresh(1L, savedRecordDTO);

		record.set(multipleTextMetadata, asList("otherValueValue"));
		record.set(multipleTextMetadata, asList("firstValue"));
		assertThat(record.isDirty()).isFalse();
		assertThat(record.getModifiedValues()).isEmpty();
		assertThat(record.<List<String>>get(multipleTextMetadata)).isEqualTo(asList("firstValue"));
	}

	@Test
	public void whenProducingDeltaRecordDTOThenReturnModifications()
			throws Exception {
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");
		Map<String, Object> fields = newSchemaFields();
		fields.put(multipleTextMetadataCodeAndType, asList("firstValue"));
		fields.put(multipleBooleanMetadataCodeAndType, asList("thirdValue"));

		when(copyfieldsPopulator.populateCopyfields(zeSchema, record))
				.thenReturn(TestUtils.asMap("copiedField1", (Object) "copiedValue1", "copiedField2", "copiedValue2"));

		when(copyfieldsPopulator2.populateCopyfields(zeSchema, record))
				.thenReturn(TestUtils.asMap("copiedField3", (Object) "copiedValue3"));

		RecordDTO savedRecordDTO = new SolrRecordDTO(record.getId(), 1L, null, fields);
		record.refresh(1L, savedRecordDTO);

		record.set(multipleTextMetadata, asList("firstValue"));
		record.set(dateMetadata, "secondValue");
		record.set(multipleBooleanMetadata, null);

		RecordDeltaDTO deltaDTO = record
				.toRecordDeltaDTO(zeSchema, asList(copyfieldsPopulator, copyfieldsPopulator2));
		assertThat(deltaDTO.getId()).isEqualTo(record.getId());
		assertThat(deltaDTO.getFromVersion()).isEqualTo(1L);
		assertThat(deltaDTO.getModifiedFields()).containsEntry(dateMetadataCodeAndType, "secondValue")
				.containsEntry(multipleBooleanMetadataCodeAndType, null);
		assertThat(deltaDTO.getCopyfields()).hasSize(3).containsEntry("copiedField1",
				"copiedValue1").containsEntry("copiedField2", "copiedValue2").containsEntry("copiedField3", "copiedValue3");

	}

	@Test
	public void whenCreatingRecordWithSchemaCodeThenCanBeObtainedWithGetSchemaCode()
			throws Exception {
		Record record = new TestRecord("a_b", "zeCollection");

		assertThat(record.getSchemaCode()).isEqualTo("a_b");
	}

	@Test(expected = RecordRuntimeException.RequiredMetadataArgument.class)
	public void whenSettingWithNullMetadataThenException()
			throws Exception {
		Record record = new TestRecord("a_b", "zeCollection");
		record.set(null, "zeValue");
	}

	@Test(expected = RecordRuntimeException.CannotSetCollectionInSingleValueMetadata.class)
	public void whenSettingAListValueInSingleValueMetadataThenException()
			throws Exception {
		Record record = new TestRecord("a_b", "zeCollection");
		record.set(textMetadata, asList("a", "b"));
	}

	@Test(expected = RecordRuntimeException.CannotSetNonListValueInMultivalueMetadata.class)
	public void whenSettingASingleValueInMultivalueMetadataThenException()
			throws Exception {
		Record record = new TestRecord("a_b", "zeCollection");
		record.set(multipleTextMetadata, "aValue");
	}

	@Test(expected = RecordRuntimeException.RequiredMetadataArgument.class)
	public void whenGettingWithNullMetadataThenException()
			throws Exception {
		Record record = new TestRecord("a_b", "zeCollection");
		record.get(null);
	}

	@Test(expected = RecordRuntimeException.CannotSetManualValueInAutomaticField.class)
	public void whenSettingValueInCopiedMetadataThenException()
			throws Exception {
		Record record = new TestRecord("a_b", "zeCollection");
		record.set(copyMetadata, "manualValue");
	}

	@Test(expected = RecordRuntimeException.CannotSetManualValueInAutomaticField.class)
	public void whenSettingValueInCalculatedMetadataThenException()
			throws Exception {
		Record record = new TestRecord("a_b", "zeCollection");
		record.set(calculatedMetadata, "manualValue");
	}

	@Test
	public void givenNullWasSetToMultivalueMetadataWhenCallingGetterThenReturnUnmodifiableEmptyList()
			throws Exception {
		Record record = new TestRecord("a_b", "zeCollection");
		record.set(multipleTextMetadata, null);

		assertThat(record.<List<String>>get(multipleTextMetadata)).isEqualTo(new ArrayList<>());
		assertThat(record.<List<String>>get(multipleTextMetadata)).is(unmodifiableCollection());
		assertThat(record.getList(multipleTextMetadata)).isEqualTo(new ArrayList<>());
		assertThat(record.getList(multipleTextMetadata)).is(unmodifiableCollection());
	}

	@Test
	public void givenModifiableListWasSetToMultivalueMetadataWhenCallingGetterThenReturnUnmodifiableEmptyList()
			throws Exception {
		Record record = new TestRecord("a_b", "zeCollection");
		record.set(multipleTextMetadata, new ArrayList<>(asList("a", "b")));

		assertThat(record.<List<String>>get(multipleTextMetadata)).isEqualTo(asList("a", "b"));
		assertThat(record.<List<String>>get(multipleTextMetadata)).is(unmodifiableCollection());
		assertThat(record.getList(multipleTextMetadata)).isEqualTo(asList("a", "b"));
		assertThat(record.getList(multipleTextMetadata)).is(unmodifiableCollection());
	}

	@Test
	public void givenNullWasSetToMultivalueMetadataWhenCallingGetterThenReturnEmptyList()
			throws Exception {
		Record record = new TestRecord("a_b", "zeCollection");
		record.set(multipleTextMetadata, new ArrayList<>());

		assertThat(record.<List<String>>get(multipleTextMetadata));
		assertThat(record.getList(multipleTextMetadata)).isEqualTo(new ArrayList<>());
	}

	@Test
	public void givenRecordModifiedWhenMergingThenMergeSuccessful()
			throws Exception {
		Map<String, Object> initialStateFields = new HashMap<>();
		initialStateFields.put("schema_s", "zeSchemaType_default");
		initialStateFields.put("collection_s", "zeCollection");
		when(initialState.getFields()).thenReturn(initialStateFields);
		RecordImpl record = new TestRecord(initialState, collectionInfo);

		Map<String, Object> currentStateFields = new HashMap<>();
		currentStateFields.put(textMetadataCodeAndType, "differentValue");
		currentStateFields.put("schema_s", "zeSchemaType_default");
		currentStateFields.put("collection_s", "zeCollection");
		when(currentState.getFields()).thenReturn(currentStateFields);

		RecordImpl newRecord = new TestRecord(currentState, collectionInfo);

		record.merge(newRecord, zeSchema);

		assertThat(record.getVersion()).isEqualTo(newRecord.getVersion());
		assertThat(record.getRecordDTO()).isEqualTo(currentState);
		assertThat(record.isDirty()).isFalse();
		assertThat(record.getModifiedValues()).doesNotContainKey(textMetadataCodeAndType);
		assertThat(record.<String>get(textMetadata)).isEqualTo("differentValue");
	}

	@Test(expected = CannotMerge.class)
	public void givenRecordsConflictingWhenMergingThenExceptionThrown()
			throws Exception {
		Map<String, Object> initialStateFields = new HashMap<>();
		initialStateFields.put("collection_s", "zeCollection");
		initialStateFields.put(textMetadataCodeAndType, "aValue");
		initialStateFields.put("schema_s", theSchemaCode);
		when(initialState.getFields()).thenReturn(initialStateFields);
		RecordImpl record = new TestRecord(initialState, collectionInfo);

		Map<String, Object> currentStateFields = new HashMap<>();
		currentStateFields.put("collection_s", "zeCollection");
		currentStateFields.put(textMetadataCodeAndType, "differentValue");
		currentStateFields.put("schema_s", theSchemaCode);
		when(currentState.getFields()).thenReturn(currentStateFields);
		RecordImpl newRecord = new TestRecord(currentState, collectionInfo);

		record.set(textMetadata, "changedValue");

		record.merge(newRecord, zeSchema);
	}

	@Test
	public void whenMergingRecordWithTwoModifiedFieldsThenMerged()
			throws Exception {
		Map<String, Object> initialStateFields = new HashMap<>();
		initialStateFields.put("schema_s", "zeSchemaType_default");
		initialStateFields.put("collection_s", "zeCollection");
		when(initialState.getFields()).thenReturn(initialStateFields);
		RecordImpl record = new TestRecord(initialState, collectionInfo);

		Map<String, Object> currentStateFields = new HashMap<>();
		currentStateFields.put("schema_s", "zeSchemaType_default");
		currentStateFields.put("collection_s", "zeCollection");
		currentStateFields.put(textMetadataCodeAndType, "differentValue");
		currentStateFields.put(dateMetadataCodeAndType, new LocalDateTime(2014, 8, 15, 11, 2));
		when(currentState.getFields()).thenReturn(currentStateFields);
		RecordImpl newRecord = new TestRecord(currentState, collectionInfo);

		record.merge(newRecord, zeSchema);

		assertThat(record.getVersion()).isEqualTo(newRecord.getVersion());
		assertThat(record.getRecordDTO()).isEqualTo(currentState);
		assertThat(record.getModifiedValues()).doesNotContainKey(textMetadataCodeAndType);
		assertThat(record.getModifiedValues()).doesNotContainKey(dateMetadataCodeAndType);
		assertThat(record.isDirty()).isFalse();
		assertThat(record.<String>get(textMetadata)).isEqualTo("differentValue");
		assertThat(record.<LocalDateTime>get(dateMetadata)).isEqualTo(new LocalDateTime(2014, 8, 15, 11, 2));

	}

	@Test
	public void whenMergingRecordWithThenMergeFieldsCorrectly()
			throws Exception {
		Map<String, Object> initialStateFields = new HashMap<>();
		initialStateFields.put("schema_s", theSchemaCode);
		initialStateFields.put("collection_s", "zeCollection");
		when(initialState.getFields()).thenReturn(initialStateFields);
		RecordImpl record = new TestRecord(initialState, collectionInfo);

		Map<String, Object> currentStateFields = new HashMap<>();
		currentStateFields.put("schema_s", theSchemaCode);
		currentStateFields.put("collection_s", "zeCollection");
		currentStateFields.put(textMetadataCodeAndType, "modifiedValue");
		currentStateFields.put(dateMetadataCodeAndType, new LocalDateTime(2013, 8, 15, 11, 2));
		when(currentState.getFields()).thenReturn(currentStateFields);
		RecordImpl newRecord = new TestRecord(currentState, collectionInfo);

		record.set(textMetadata, "modifiedValue");
		record.set(numberMetadata, 42.0);
		record.merge(newRecord, zeSchema);

		assertThat(record.getVersion()).isEqualTo(newRecord.getVersion());
		assertThat(record.getRecordDTO()).isEqualTo(currentState);
		assertThat(record.getModifiedValues()).doesNotContainKey(textMetadataCodeAndType);
		assertThat(record.getModifiedValues()).doesNotContainKey(dateMetadataCodeAndType);
		assertThat(record.getModifiedValues()).containsEntry(numberMetadataCodeAndType, 42.0);
		assertThat(record.isDirty()).isTrue();
		assertThat(record.<String>get(textMetadata)).isEqualTo("modifiedValue");
		assertThat(record.<LocalDateTime>get(dateMetadata)).isEqualTo(new LocalDateTime(2013, 8, 15, 11, 2));

	}

	@Test(expected = CannotMerge.class)
	public void givenRecordsWithTwoFieldsConflictingOnOneWhenMergingThenExceptionThrown()
			throws Exception {
		Map<String, Object> initialStateFields = new HashMap<>();
		initialStateFields.put(textMetadataCodeAndType, "aValue");
		initialStateFields.put(dateMetadataCodeAndType, new LocalDateTime(2014, 8, 15, 11, 2));
		initialStateFields.put("schema_s", "a_b");
		initialStateFields.put("collection_s", "zeCollection");
		when(initialState.getFields()).thenReturn(initialStateFields);
		RecordImpl record = new TestRecord(initialState, collectionInfo);

		Map<String, Object> currentStateFields = new HashMap<>();
		currentStateFields.put(textMetadataCodeAndType, "differentValue");
		currentStateFields.put(dateMetadataCodeAndType, new LocalDateTime(2014, 8, 15, 11, 2));
		currentStateFields.put("schema_s", "a_b");
		currentStateFields.put("collection_s", "zeCollection");
		when(currentState.getFields()).thenReturn(currentStateFields);
		RecordImpl newRecord = new TestRecord(currentState, collectionInfo);

		record.set(textMetadata, "changedValue");

		record.merge(newRecord, zeSchema);
	}

	@Test(expected = RecordRuntimeException.InvalidMetadata.class)
	public void givenRecordOfSchemaTypeA_CustomWhenSetB_CustomMetadataThenException()
			throws Exception {

		Metadata metadata = mockMetadata("b_custom_m");

		RecordImpl record = new TestRecord("a_custom", "zeCollection");
		record.set(metadata, "1");

	}

	@Test(expected = RecordRuntimeException.InvalidMetadata.class)
	public void givenRecordOfSchemaTypeA_DefaultWhenSetA_CustomMetadataThenException()
			throws Exception {

		Metadata metadata = mockMetadata("b_custom_m");

		RecordImpl record = new TestRecord("a_default", "zeCollection");
		record.set(metadata, "1");

	}

	@Test(expected = RecordRuntimeException.InvalidMetadata.class)
	public void givenRecordOfSchemaTypeA_CustomWhenSetWithNullCodeThenException()
			throws Exception {

		Metadata metadata = mock(Metadata.class);
		when(metadata.getCode()).thenReturn(null);

		RecordImpl record = new TestRecord("a_custom", "zeCollection");
		record.set(metadata, "1");

	}

	@Test
	public void givenARecordsWithTwoEqualFieldsWhenMergingThenDoNottNeedToMerge()
			throws Exception {
		Map<String, Object> initialStateFields = new HashMap<>();
		initialStateFields.put("schema_s", "zeSchemaType_default");
		initialStateFields.put("collection_s", "zeCollection");
		initialStateFields.put(textMetadataCodeAndType, "aValue");
		initialStateFields.put(dateMetadataCodeAndType, new LocalDateTime(2014, 8, 15, 11, 2));
		when(initialState.getFields()).thenReturn(initialStateFields);
		RecordImpl record = new TestRecord(initialState, collectionInfo);

		assertThat(record.<String>get(textMetadata)).isEqualTo("aValue");

		Map<String, Object> currentStateFields = new HashMap<>();
		currentStateFields.put("schema_s", "zeSchemaType_default");
		currentStateFields.put("collection_s", "zeCollection");
		currentStateFields.put(textMetadataCodeAndType, "aValue");
		currentStateFields.put(dateMetadataCodeAndType, new LocalDateTime(2014, 8, 15, 11, 2));
		when(currentState.getFields()).thenReturn(currentStateFields);
		RecordImpl newRecord = new TestRecord(currentState, collectionInfo);

		record.merge(newRecord, zeSchema);

		assertThat(record.getRecordDTO()).isEqualTo(currentState);
		assertThat(record.<String>get(textMetadata)).isEqualTo("aValue");
		assertThat(record.isDirty()).isFalse();
		assertThat(record.getModifiedValues()).hasSize(0);
	}

	@Test
	public void givenRecordsWithSameValuesAndNumberTypesWhenMergingThenDoNotNeedToMerge()
			throws Exception {
		Map<String, Object> initialStateFields = new HashMap<>();
		initialStateFields.put("schema_s", "zeSchemaType_default");
		initialStateFields.put("collection_s", "zeCollection");
		initialStateFields.put(numberMetadataCodeAndType, 20);
		initialStateFields.put(otherNumberMetadataCodeAndType, 10.0);
		when(initialState.getFields()).thenReturn(initialStateFields);
		RecordImpl record = new TestRecord(initialState, collectionInfo);

		Map<String, Object> currentStateFields = new HashMap<>();
		currentStateFields.put("schema_s", "zeSchemaType_default");
		currentStateFields.put("collection_s", "zeCollection");
		currentStateFields.put(numberMetadataCodeAndType, 20);
		currentStateFields.put(otherNumberMetadataCodeAndType, 10.0);
		when(currentState.getFields()).thenReturn(currentStateFields);
		RecordImpl newRecord = new TestRecord(currentState, collectionInfo);

		record.merge(newRecord, zeSchema);

		assertThat(record.getRecordDTO()).isEqualTo(currentState);
		assertThat(record.<Integer>get(numberMetadata)).isEqualTo(20);
		assertThat(record.<Double>get(otherNumberMetadata)).isEqualTo(10.0);
		assertThat(record.isDirty()).isFalse();
		assertThat(record.getModifiedValues()).hasSize(0);
	}

	@Test
	public void givenRecordsWithSameValuesAndDifferentNumberTypesWhenMergingThenDoNotNeedToMerge()
			throws Exception {
		Map<String, Object> initialStateFields = new HashMap<>();
		initialStateFields.put("schema_s", "zeSchemaType_default");
		initialStateFields.put("collection_s", "zeCollection");
		initialStateFields.put(numberMetadataCodeAndType, 20);
		initialStateFields.put(otherNumberMetadataCodeAndType, 10.0);
		when(initialState.getFields()).thenReturn(initialStateFields);
		RecordImpl record = new TestRecord(initialState, collectionInfo);

		Map<String, Object> currentStateFields = new HashMap<>();
		currentStateFields.put("schema_s", "zeSchemaType_default");
		currentStateFields.put("collection_s", "zeCollection");
		currentStateFields.put(numberMetadataCodeAndType, 20.0);
		currentStateFields.put(otherNumberMetadataCodeAndType, 10.0);
		when(currentState.getFields()).thenReturn(currentStateFields);
		RecordImpl newRecord = new TestRecord(currentState, collectionInfo);

		record.merge(newRecord, zeSchema);

		assertThat(record.getRecordDTO()).isEqualTo(currentState);
		assertThat(record.<Double>get(numberMetadata)).isEqualTo(20.0);
		assertThat(record.<Double>get(otherNumberMetadata)).isEqualTo(10.0);
		assertThat(record.isDirty()).isFalse();
		assertThat(record.getModifiedValues()).hasSize(0);
	}

	@Test
	public void givenRecordsWithSameValuesAndDifferentNumberTypesWhenSetAndMergingThenDoNotNeedToMerge()
			throws Exception {
		Map<String, Object> initialStateFields = new HashMap<>();
		initialStateFields.put(numberMetadataCodeAndType, 20.0);
		initialStateFields.put("schema_s", "a_b");
		initialStateFields.put("collection_s", "zeCollection");
		when(initialState.getFields()).thenReturn(initialStateFields);
		RecordImpl record = new TestRecord(initialState, collectionInfo);

		record.set(numberMetadata, 20);

		Map<String, Object> currentStateFields = new HashMap<>();
		currentStateFields.put(numberMetadataCodeAndType, 20.0);
		currentStateFields.put("schema_s", "a_b");
		currentStateFields.put("collection_s", "zeCollection");
		when(currentState.getFields()).thenReturn(currentStateFields);
		RecordImpl newRecord = new TestRecord(currentState, collectionInfo);

		record.merge(newRecord, zeSchema);

		assertThat(record.getRecordDTO()).isEqualTo(currentState);
		assertThat(record.<Double>get(numberMetadata)).isEqualTo(20.0);
		assertThat(record.isDirty()).isFalse();
		assertThat(record.getModifiedValues()).hasSize(0);
	}

	@Test()
	public void givenOnlyOneNonNullValueInListWhenGettingNonNullValueThenRightValueReturned()
			throws Exception {
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");
		record.set(textMetadata, null);
		record.set(dateMetadata, null);
		record.set(numberMetadata, 5.0);

		assertThat(record.<Double>getNonNullValueIn(asList(textMetadata, dateMetadata, numberMetadata))).isEqualTo(5.0);
	}

	@Test(expected = RuntimeException.class)
	public void givenMultipleNonNullValuesInListWhenGettingNonNullValueThenRightValueReturned()
			throws Exception {
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");
		record.set(textMetadata, "aValue");
		record.set(dateMetadata, null);
		record.set(numberMetadata, 5.0);

		assertThat(record.<Double>getNonNullValueIn(asList(textMetadata, dateMetadata, numberMetadata))).isEqualTo(5.0);
	}

	@Test
	public void givenAllNullValuesInListWhenGettingNonNullValueThenNullReturned()
			throws Exception {
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");
		record.set(textMetadata, null);
		record.set(dateMetadata, null);
		record.set(numberMetadata, null);

		assertThat(record.<Double>getNonNullValueIn(asList(textMetadata, dateMetadata, numberMetadata))).isNull();
	}

	@Test
	public void whenGettingListForMetadataThenCorrectValueReturned() {
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");
		record.set(multipleTextMetadata, asList("value1", "value2", "value3"));

		assertThat(record.getList(multipleTextMetadata)).containsOnly("value1", "value2", "value3");
	}

	@Test
	public void whenGettingListForNullMetadataThenEmptyListReturned() {
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");
		record.set(multipleTextMetadata, null);

		assertThat(record.getList(multipleTextMetadata)).isEmpty();
	}

	@Test(expected = RecordImplRuntimeException.CannotGetListForSingleValue.class)
	public void whenGettingListForSingleValueMetadataThenWhat() {
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");
		record.set(textMetadata, "value");

		record.getList(textMetadata);
	}

	@Test
	public void givenNewRecordSavedWhenSetNewVersionThenSetNewDocumentDTOWithVersion() {
		RecordImpl record = new TestRecord(theSchemaCode, "zeCollection");
		record.set(multipleTextMetadata, asList("value1", "value2", "value3"));
		record.set(numberMetadata, 123);
		assertThat(record.isModified(multipleTextMetadata)).isTrue();
		assertThat(record.isModified(numberMetadata)).isTrue();

		record.markAsSaved(42, zeSchema);

		assertThat(record.isModified(multipleTextMetadata)).isFalse();
		assertThat(record.isModified(numberMetadata)).isFalse();
		assertThat(record.getVersion()).isEqualTo(42L);
		assertThat(record.<List<String>>get(multipleTextMetadata)).isEqualTo(asList("value1", "value2", "value3"));
		assertThat(record.<Double>get(numberMetadata)).isEqualTo(123.0);
		assertThat(record.getModifiedValues()).isEmpty();
	}

	@Test
	public void givenModifiedRecordSavedWhenSetNewVersionThenSetNewDocumentDTOWithVersion() {
		LocalDate date = new LocalDate();
		List<String> loadedFields = asList(multipleTextMetadataCodeAndType, numberMetadataCodeAndType);
		Map<String, Object> fieldValues = new HashMap<>();
		fieldValues.put(multipleTextMetadata.getDataStoreCode(), asList("value1", "value2", "value3"));
		fieldValues.put(numberMetadata.getDataStoreCode(), 123.0);
		fieldValues.put(dateMetadata.getDataStoreCode(), date);
		fieldValues.put("schema_s", theSchemaCode);
		fieldValues.put("collection_s", "zeCollection");
		RecordDTO recordDTO = new SolrRecordDTO("id", 4, loadedFields, fieldValues);
		RecordImpl record = new TestRecord(recordDTO, collectionInfo);

		record.set(multipleTextMetadata, asList("value4", "value5", "value6"));
		record.set(numberMetadata, 456);
		assertThat(record.isModified(multipleTextMetadata)).isTrue();
		assertThat(record.isModified(numberMetadata)).isTrue();

		record.markAsSaved(42, zeSchema);

		assertThat(record.isModified(multipleTextMetadata)).isFalse();
		assertThat(record.isModified(numberMetadata)).isFalse();
		assertThat(record.getVersion()).isEqualTo(42L);
		assertThat(record.<List<String>>get(multipleTextMetadata)).isEqualTo(asList("value4", "value5", "value6"));
		assertThat(record.<Double>get(numberMetadata)).isEqualTo(456.0);
		assertThat(record.<LocalDate>get(dateMetadata)).isEqualTo(date);
		assertThat(record.getModifiedValues()).isEmpty();
		assertThat(record.getRecordDTO().getLoadedFields()).isEqualTo(loadedFields);
	}

	@Test
	public void givenSingleValueMetadataWithStructureFactoryWhenGetValueThenBuildObjectUsingStructure()
			throws Exception {

		when(stringStructureFactory.build(zeStructureInitialValue)).thenReturn(zeStructure);

		Map<String, Object> fieldValues = newMap("zeCollection", theSchemaCode);
		fieldValues.put(factoredMetadataCodeAndType, zeStructureInitialValue);
		RecordImpl record = new TestRecord(new SolrRecordDTO("id", 4, null, fieldValues), collectionInfo);

		assertThat(record.isDirty()).isFalse();
		assertThat(record.getModifiedValues()).isEmpty();
		verify(stringStructureFactory, never()).build(zeStructureInitialValue);
		assertThat(record.<Object>get(factoredMetadata)).isSameAs(zeStructure);
		assertThat(record.<Object>get(factoredMetadata)).isSameAs(zeStructure);
		verify(stringStructureFactory, times(1)).build(zeStructureInitialValue);
	}

	@Test
	public void givenMultivalueMetadataWithStructureFactoryWhenGetValueThenBuildObjectUsingStructure()
			throws Exception {

		when(stringStructureFactory.build(zeStructureInitialValue)).thenReturn(zeStructure);
		when(stringStructureFactory.build(anotherStructureInitialValue)).thenReturn(anotherStructure);

		Map<String, Object> fieldValues = newMap("zeCollection", theSchemaCode);
		fieldValues.put(factoredMetadataCodeAndType, asList(zeStructureInitialValue, anotherStructureInitialValue));
		RecordImpl record = new TestRecord(new SolrRecordDTO("id", 4, null, fieldValues), collectionInfo);

		assertThat(record.isDirty()).isFalse();
		assertThat(record.getModifiedValues()).isEmpty();
		verify(stringStructureFactory, never()).build(zeStructureInitialValue);
		verify(stringStructureFactory, never()).build(anotherStructureInitialValue);
		assertThat(record.<List<Object>>get(factoredMetadata)).isSameAs(record.get(factoredMetadata))
				.isEqualTo(asList(zeStructure, anotherStructure));
		verify(stringStructureFactory, times(1)).build(zeStructureInitialValue);
		verify(stringStructureFactory, times(1)).build(anotherStructureInitialValue);
	}

	@Test
	public void givenFactoredSingleValueMetadataModifiedThenRecordIsDirtyAndValueInModifiedValues()
			throws Exception {

		when(stringStructureFactory.build(zeStructureInitialValue)).thenReturn(zeStructure);
		when(stringStructureFactory.toString(zeStructure)).thenReturn(zeStructureModifiedValue);
		Map<String, Object> fieldValues = newMap("zeCollection", theSchemaCode);
		fieldValues.put(factoredMetadataCodeAndType, zeStructureInitialValue);
		RecordImpl record = new TestRecord(new SolrRecordDTO("id", 4, null, fieldValues), collectionInfo);

		assertThat(record.<Object>get(factoredMetadata)).isSameAs(zeStructure);
		when(zeStructure.isDirty()).thenReturn(true);

		assertThat(record.isDirty()).isTrue();
		assertThat(record.getModifiedValues()).containsEntry(factoredMetadataCodeAndType, zeStructure).hasSize(1);

		assertThat(
				record.toRecordDeltaDTO(zeSchema, asList(copyfieldsPopulator)).getModifiedFields())
				.containsEntry(factoredMetadataCodeAndType, zeStructureModifiedValue);

		verify(copyfieldsPopulator).populateCopyfields(zeSchema, record);

	}

	@Test
	public void givenNewFactoredSingleValueMetadataThenRecordIsDirtyAndValueInModifiedValues()
			throws Exception {

		when(stringStructureFactory.build(zeStructureInitialValue)).thenReturn(zeStructure);
		when(stringStructureFactory.toString(zeStructure)).thenReturn(zeStructureModifiedValue);
		when(stringStructureFactory.toString(anotherStructure)).thenReturn(anotherStructureInitialValue);
		Map<String, Object> fieldValues = newMap("zeCollection", theSchemaCode);
		fieldValues.put(factoredMetadataCodeAndType, zeStructureInitialValue);
		RecordImpl record = new TestRecord(new SolrRecordDTO("id", 4, null, fieldValues), collectionInfo);

		when(anotherStructure.isDirty()).thenReturn(true);
		record.set(factoredMetadata, anotherStructure);

		assertThat(record.<Object>get(factoredMetadata)).isSameAs(anotherStructure);

		assertThat(record.isDirty()).isTrue();
		assertThat(record.getModifiedValues()).containsEntry(factoredMetadataCodeAndType, anotherStructure).hasSize(1);

		assertThat(record.toRecordDeltaDTO(zeSchema, asList(copyfieldsPopulator)).getModifiedFields())
				.containsEntry(factoredMetadataCodeAndType, anotherStructureInitialValue);

		verify(copyfieldsPopulator).populateCopyfields(zeSchema, record);
	}

	@Test
	public void givenFactoredSinglevalueInNewRecordWhenCreatingDtoThenStructureConvertedToString()
			throws Exception {

		when(stringStructureFactory.build(zeStructureInitialValue)).thenReturn(zeStructure);
		when(stringStructureFactory.toString(zeStructure)).thenReturn(zeStructureModifiedValue);
		when(stringStructureFactory.toString(anotherStructure)).thenReturn(anotherStructureInitialValue);
		RecordImpl record = new TestRecord("a_b", "zeCollection");

		when(anotherStructure.isDirty()).thenReturn(true);
		record.set(factoredMetadata, anotherStructure);

		assertThat(record.<Object>get(factoredMetadata)).isSameAs(anotherStructure);

		assertThat(record.isDirty()).isTrue();
		assertThat(record.getModifiedValues()).containsEntry(factoredMetadataCodeAndType, anotherStructure).hasSize(1);

		assertThat(record.toNewDocumentDTO(zeSchema, asList(copyfieldsPopulator))
				.getFields()).containsEntry(factoredMetadataCodeAndType, anotherStructureInitialValue);

		verify(copyfieldsPopulator).populateCopyfields(zeSchema, record);
	}

	@Test
	public void givenFactoredMultivalueMetadataModifiedThenRecordIsDirtyAndValueInModifiedValues()
			throws Exception {

		when(stringStructureFactory.build(zeStructureInitialValue)).thenReturn(zeStructure);
		when(stringStructureFactory.build(anotherStructureInitialValue)).thenReturn(anotherStructure);
		when(stringStructureFactory.toString(zeStructure)).thenReturn(zeStructureModifiedValue);
		when(stringStructureFactory.toString(anotherStructure)).thenReturn(anotherStructureInitialValue);
		when(stringStructureFactory.toString(aThirdStructure)).thenReturn(aThirdStructureInitialValue);
		Map<String, Object> fieldValues = newMap("zeCollection", theSchemaCode);
		fieldValues.put(factoredListMetadataCodeAndType, asList(zeStructureInitialValue, anotherStructureInitialValue));
		RecordImpl record = new TestRecord(new SolrRecordDTO("id", 4, null, fieldValues), collectionInfo);

		List<Object> values = record.get(factoredListMetadata);
		assertThat(values).isEqualTo(asList(zeStructure, anotherStructure));
		when(zeStructure.isDirty()).thenReturn(true);

		assertThat(record.isDirty()).isTrue();
		assertThat(record.getModifiedValues())
				.containsEntry(factoredListMetadataCodeAndType, asList(zeStructure, anotherStructure)).hasSize(1);

		assertThat(record.toRecordDeltaDTO(zeSchema, asList(copyfieldsPopulator)).getModifiedFields())
				.containsEntry(factoredListMetadataCodeAndType,
						asList(zeStructureModifiedValue, anotherStructureInitialValue));

		verify(copyfieldsPopulator).populateCopyfields(zeSchema, record);
	}

	@Test
	public void givenFactoredMultivalueInNewRecordWhenCreatingDtoThenStructureConvertedToString()
			throws Exception {

		when(stringStructureFactory.build(zeStructureInitialValue)).thenReturn(zeStructure);
		when(stringStructureFactory.build(anotherStructureInitialValue)).thenReturn(anotherStructure);
		when(stringStructureFactory.toString(zeStructure)).thenReturn(zeStructureInitialValue);
		when(stringStructureFactory.toString(anotherStructure)).thenReturn(anotherStructureInitialValue);
		when(stringStructureFactory.toString(aThirdStructure)).thenReturn(aThirdStructureInitialValue);
		RecordImpl record = new TestRecord("a_b", "zeCollection");

		record.set(factoredListMetadata, asList(zeStructure, anotherStructure));
		List<Object> values = record.get(factoredListMetadata);
		assertThat(values).isEqualTo(asList(zeStructure, anotherStructure));
		when(zeStructure.isDirty()).thenReturn(true);

		assertThat(record.toNewDocumentDTO(zeSchema, asList(copyfieldsPopulator))
				.getFields())
				.containsEntry(factoredListMetadataCodeAndType,
						asList(zeStructureInitialValue, anotherStructureInitialValue));

		verify(copyfieldsPopulator).populateCopyfields(zeSchema, record);
	}

	@Test
	public void givenFactoredMultivalueMetadataAddRemovedThenRecordIsDirtyAndValueInModifiedValues()
			throws Exception {

		when(stringStructureFactory.build(zeStructureInitialValue)).thenReturn(zeStructure);
		when(stringStructureFactory.build(anotherStructureInitialValue)).thenReturn(anotherStructure);
		when(stringStructureFactory.toString(zeStructure)).thenReturn(zeStructureInitialValue);
		when(stringStructureFactory.toString(anotherStructure)).thenReturn(anotherStructureInitialValue);
		when(stringStructureFactory.toString(aThirdStructure)).thenReturn(aThirdStructureInitialValue);
		Map<String, Object> fieldValues = newMap("zeCollection", theSchemaCode);
		fieldValues.put(factoredListMetadataCodeAndType, asList(zeStructureInitialValue, anotherStructureInitialValue));
		RecordImpl record = new TestRecord(new SolrRecordDTO("id", 4, null, fieldValues), collectionInfo);

		record.set(factoredListMetadata, asList(zeStructure, aThirdStructure));

		when(zeStructure.isDirty()).thenReturn(true);

		assertThat(record.isDirty()).isTrue();
		assertThat(record.getModifiedValues())
				.containsEntry(factoredListMetadataCodeAndType, asList(zeStructure, aThirdStructure)).hasSize(1);

		assertThat(record.toRecordDeltaDTO(zeSchema, asList(copyfieldsPopulator)).getModifiedFields())
				.containsEntry(factoredListMetadataCodeAndType,
						asList(zeStructureInitialValue, aThirdStructureInitialValue));

		verify(copyfieldsPopulator).populateCopyfields(zeSchema, record);
	}

	private Map<String, Object> newMap(String collection, String theSchemaCode) {

		Map<String, Object> fieldValues = new HashMap<>();
		fieldValues.put("schema_s", theSchemaCode);
		fieldValues.put("collection_s", "zeCollection");
		return fieldValues;
	}

	@Test
	public void givenNewRecordWithoutParentThenReturnNullParentId()
			throws Exception {

		RecordImpl record = new TestRecord("a_b", "zeCollection", "zeId");
		assertThat(record.getParentId()).isNull();
	}

	@Test
	public void givenNewRecordWithParentThenReturnParentId()
			throws Exception {
		RecordImpl record = new TestRecord("a_b", "zeCollection", "zeId");
		record.modifiedValues.put("zeParentPId_s", "modifiedParentId");
		assertThat(record.getParentId()).isEqualTo("modifiedParentId");
	}

	@Test
	public void givenSavedRecordWithoutParentThenReturnNullParentId()
			throws Exception {
		RecordDTO recordDTO = new SolrRecordDTO("id", 4, new ArrayList<String>(),
				asMap("refId_s", (Object) 123, "collection_s", "zeCollection"));
		RecordImpl record = new RecordImpl(recordDTO, collectionInfo);
		assertThat(record.getParentId()).isNull();
	}

	@Test
	public void givenSavedRecordWithParentThenReturnParentId()
			throws Exception {
		RecordDTO recordDTO = new SolrRecordDTO("id", 4, new ArrayList<String>(),
				asMap("refId_s", (Object) 123, "collection_s", "zeCollection", "zeParentPId_s", "initialParentId"));
		RecordImpl record = new RecordImpl(recordDTO, collectionInfo);
		assertThat(record.getParentId()).isEqualTo("initialParentId");
	}

	@Test
	public void givenSavedRecordWithModifiedParentThenReturnModifiedParentId()
			throws Exception {
		RecordDTO recordDTO = new SolrRecordDTO("id", 4, new ArrayList<String>(),
				asMap("refId_s", (Object) 123, "collection_s", "zeCollection", "zeParentPId_s", "initialParentId"));
		RecordImpl record = new RecordImpl(recordDTO, collectionInfo);
		record.modifiedValues.put("zeParentPId_s", "modifiedParentId");
		assertThat(record.getParentId()).isEqualTo("modifiedParentId");
	}

	@Test
	public void givenModifiedRecordWhenGetCopyOfOriginalRecordThenHasValuesFetchedFromServerAndNoModifications()
			throws Exception {
		RecordDTO recordDTO = new SolrRecordDTO("id", 4, new ArrayList<String>(),
				asMap("refId_s", (Object) 123, "collection_s", "zeCollection", "zeParentPId_s", "initialParentId"));
		RecordImpl record = new RecordImpl(recordDTO, collectionInfo);
		record.modifiedValues.put("zeParentPId_s", "modifiedParentId");
		assertThat(record.getParentId()).isEqualTo("modifiedParentId");
		assertThat(record.getCopyOfOriginalRecord().getParentId()).isEqualTo("initialParentId");
	}

	@Test(expected = RecordImplException_UnsupportedOperationOnUnsavedRecord.class)
	public void givenUnsavedRecordWhenGetCopyOfOriginalRecordThenException()
			throws Exception {
		RecordImpl record = new TestRecord("folder_default", "zeUltimateCollection", "42");
		record.getCopyOfOriginalRecord();
	}

	@Test
	public void whenSerializingNewRecordThenCorrectlySerializedAndUnserialized() {

		RecordImpl record = new TestRecord("folder_default", "zeUltimateCollection", "42");
		record.set(Schemas.TITLE, "zeTitle");
		record.set(Schemas.MODIFIED_BY, shishOClock);

		byte[] bytes = SerializationUtils.serialize(record);
		RecordImpl unserializedRecord = (RecordImpl) SerializationUtils.deserialize(bytes);

		assertThat(unserializedRecord.<String>get(Schemas.TITLE)).isEqualTo("zeTitle");
		assertThat(unserializedRecord.<LocalDateTime>get(Schemas.MODIFIED_BY)).isEqualTo(shishOClock);
		assertThat(unserializedRecord.getSchemaCode()).isEqualTo("folder_default");
		assertThat(unserializedRecord.getCollection()).isEqualTo("zeUltimateCollection");
		assertThat(unserializedRecord.getId()).isEqualTo("42");
		assertThat(unserializedRecord.getVersion()).isEqualTo(-1);
		assertThat(unserializedRecord.getRecordDTO()).isNull();
		assertThat(unserializedRecord.getLoadedStructuredValues()).isNull();
	}

	@Test
	public void whenSerializingModifiedRecordThenCorrectlySerializedAndUnserialized() {

		List<String> loadedFields = asList("1", "2");
		Map<String, Object> fields = new HashMap<>();
		fields.put("schema_s", "folder_default");
		fields.put("collection_s", "zeUltimateCollection");
		fields.put("key1", "value1");
		fields.put("key2", "value2");
		Map<String, Object> copyFields = asMap("key3", (Object) "value3", "key4", "value4");

		RecordDTO recordDTO = new SolrRecordDTO("id42", 42, loadedFields, fields, copyFields);
		RecordImpl record = new RecordImpl(recordDTO, collectionInfo);
		record.set(Schemas.TITLE, "zeTitle");
		record.set(Schemas.MODIFIED_BY, shishOClock);

		byte[] bytes = SerializationUtils.serialize(record);
		RecordImpl unserializedRecord = (RecordImpl) SerializationUtils.deserialize(bytes);

		assertThat(unserializedRecord.<String>get(Schemas.TITLE)).isEqualTo("zeTitle");
		assertThat(unserializedRecord.<LocalDateTime>get(Schemas.MODIFIED_BY)).isEqualTo(shishOClock);
		assertThat(unserializedRecord.getSchemaCode()).isEqualTo("folder_default");
		assertThat(unserializedRecord.getCollection()).isEqualTo("zeUltimateCollection");
		assertThat(unserializedRecord.getId()).isEqualTo("id42");
		assertThat(unserializedRecord.getVersion()).isEqualTo(42);
		assertThat(unserializedRecord.getRecordDTO().getId()).isEqualTo("id42");
		assertThat(unserializedRecord.getRecordDTO().getVersion()).isEqualTo(42);
		assertThat(unserializedRecord.getRecordDTO().getLoadedFields()).isEqualTo(loadedFields);
		assertThat(unserializedRecord.getRecordDTO().getFields()).isEqualTo(fields);
		assertThat(unserializedRecord.getRecordDTO().getCopyFields()).isEqualTo(copyFields);
		assertThat(unserializedRecord.getLoadedStructuredValues()).isNull();
	}

	//TODO @Test
	public void whenSettingCompatibleRawValueInMetadataWithInputMaskThenConvertIt()
			throws Exception {

		Metadata telephoneNumber = mockManualMetadata("zeType_default_telephoneNumber", MetadataValueType.STRING);
		Metadata postalCode = mockManualMetadata("zeType_default_postalCode", MetadataValueType.STRING);
		when(telephoneNumber.getDataStoreCode()).thenReturn("telephoneNumber_s");
		when(telephoneNumber.getInputMask()).thenReturn("(###) ###-####");
		when(postalCode.getDataStoreCode()).thenReturn("postalCode_s");
		when(postalCode.getInputMask()).thenReturn("U#U #U#");
		RecordImpl record = new TestRecord("zeType_default", "zeCollection", "zeId");

		record.set(telephoneNumber, "4183533390");
		record.set(postalCode, "G1N2C9");
		assertThat(record.<String>get(telephoneNumber)).isEqualTo("(418) 353-3390");
		assertThat(record.<String>get(postalCode)).isEqualTo("G1N 2C9");

		record.set(telephoneNumber, "4186664242");
		record.set(postalCode, "H0H0H0");
		assertThat(record.<String>get(telephoneNumber)).isEqualTo("(418) 666-4242");
		assertThat(record.<String>get(postalCode)).isEqualTo("H0H 0H0");

	}

	@Test
	public void whenSettingIncompatibleRawValueInMetadataWithInputMaskThenSetTheRawValue()
			throws Exception {

		Metadata telephoneNumber = mockManualMetadata("zeType_default_telephoneNumber", MetadataValueType.STRING);
		Metadata postalCode = mockManualMetadata("zeType_default_postalCode", MetadataValueType.STRING);
		when(telephoneNumber.getDataStoreCode()).thenReturn("telephoneNumber_s");
		when(telephoneNumber.getInputMask()).thenReturn("(###) ###-####");
		when(postalCode.getDataStoreCode()).thenReturn("postalCode_s");
		when(postalCode.getInputMask()).thenReturn("U#U #U#");
		RecordImpl record = new TestRecord("zeType_default", "zeCollection", "zeId");

		record.set(telephoneNumber, "418-353-3390");
		record.set(postalCode, "G1Nyyy2C9");
		assertThat(record.<String>get(telephoneNumber)).isEqualTo("418-353-3390");
		assertThat(record.<String>get(postalCode)).isEqualTo("G1Nyyy2C9");

		record.set(telephoneNumber, "(418y 666-4242");
		record.set(postalCode, "H0H:0H0");
		assertThat(record.<String>get(telephoneNumber)).isEqualTo("(418y 666-4242");
		assertThat(record.<String>get(postalCode)).isEqualTo("H0H:0H0");

	}

	@Test
	public void whenSettingFormattedValueInMetadataWithInputMaskThenSetTheValue()
			throws Exception {

		Metadata telephoneNumber = mockManualMetadata("zeType_default_telephoneNumber", MetadataValueType.STRING);
		Metadata postalCode = mockManualMetadata("zeType_default_postalCode", MetadataValueType.STRING);
		when(telephoneNumber.getDataStoreCode()).thenReturn("telephoneNumber_s");
		when(telephoneNumber.getInputMask()).thenReturn("(###) ###-####");
		when(postalCode.getDataStoreCode()).thenReturn("postalCode_s");
		when(postalCode.getInputMask()).thenReturn("U#U #U#");
		RecordImpl record = new TestRecord("zeType_default", "zeCollection", "zeId");

		record.set(telephoneNumber, "(418) 353-3390");
		record.set(postalCode, "G1N 2C9");
		assertThat(record.<String>get(telephoneNumber)).isEqualTo("(418) 353-3390");
		assertThat(record.<String>get(postalCode)).isEqualTo("G1N 2C9");

		record.set(telephoneNumber, "(418) 666-4242");
		record.set(postalCode, "H0H 0H0");
		assertThat(record.<String>get(telephoneNumber)).isEqualTo("(418) 666-4242");
		assertThat(record.<String>get(postalCode)).isEqualTo("H0H 0H0");

	}

	Map<String, Object> newSchemaFields() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("schema_s", theSchemaCode);
		fields.put("collection_s", zeCollection);
		return fields;
	}

}
