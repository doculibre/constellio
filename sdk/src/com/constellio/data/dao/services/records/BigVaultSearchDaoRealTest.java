package com.constellio.data.dao.services.records;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.dao.dto.records.TransactionSearchDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils.MapBuilder;
import com.constellio.sdk.tests.annotations.LoadTest;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class BigVaultSearchDaoRealTest extends ConstellioTest {

	public static final String singleReferenceFieldCode = "refId_s";
	public static final String parentReferenceFieldCode = "parentRefPId_s";
	public static final String multipleReferencesFieldCode = "refId_ss";
	private static final String ID_FIELD = "id";
	private final String aContentId = "aContentId";
	private final String savedMetadataFieldName = "categoryId_s";
	private final String initialValue = "initialValue";
	private final String modifiedValue = "modifiedValue";
	LocalDateTime aDateTime = aDateTime();
	LocalDateTime anotherDateTime = aDateTime();
	LocalDate aDate = aDate();
	LocalDate anotherDate = aDate();
	private RecordDao recordDao;
	private RecordDTO child1;
	private RecordDTO child2;
	private RecordDTO child3;

	@Before
	public void setup() {
		recordDao = getDataLayerFactory().newSearchDao();

		Map<String, Object> fields = new HashMap<>();
		fields.put(savedMetadataFieldName, initialValue);

		child1 = new SolrRecordDTO("child1",
				buildParamMapWith("zeCollection", "zeType_default").andWith("parentPId_s", "parent")
						.andWith("otherRefId_s", "zeRef").andWith("archivisticStatus_s", "a").andWith("title_s", "child1")
						.andWith("categoryId_s", "categoryId_X110").andWith("tokens_ss", "v75h438929frjyeuirw==")
						.andWith("title_t_fr", "enfant1").andWith("title_t_en", "child1")
						.build(), RecordDTOMode.FULLY_LOADED);
		child2 = new SolrRecordDTO("child2",
				buildParamMapWith("zeCollection", "zeType_default").andWith("parentPId_s", "parent")
						.andWith("otherRefId_s", "zeRef").andWith("attachedPrincipalAncestorsIntIds_is", "54")
						.andWith("attachedAncestors_ss", "unitId_67").andWith("title_s", "child2")
						.andWith("title_t_fr", "recorden").andWith("title_t_en", "recorden").andWith("hidden_s", "__TRUE__")
						.build(), RecordDTOMode.FULLY_LOADED);

		child3 = new SolrRecordDTO("child3",
				buildParamMapWith("zeCollection", "zeType_default").andWith("parentPId_s", "parent")
						.andWith("otherRefId_s", "zeRef").andWith("title_s", "child3")
						.andWith("title_t_fr", "celuidetruit").andWith("title_t_en", "deletedone")
						.andWith("deleted_s", "__TRUE__")
						.build(), RecordDTOMode.FULLY_LOADED);
	}

	@Test
	public void __prepareTests__()
			throws Exception {

	}

	@Test
	@LoadTest
	public void whenAddingAndSearchingMultipleTimesWithDifferentDaoThenCanHandleLoad()
			throws Exception {

		for (int i = 0; i < 100; i++) {
			recordDao = getDataLayerFactory().newRecordDao();

			add(newRecordWithTitle("record #" + i));

			assertThat(recordDao.documentsCount()).isEqualTo(i + 1);
		}
	}

	private RecordDTO newRecordWithTitle(String title) {
		String id = UUID.randomUUID().toString();
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("title_s", title);
		fields.put("schema_s", "zeSchemaType_default");
		RecordDTO record = new SolrRecordDTO(nextID(), -1, null, fields, RecordDTOMode.FULLY_LOADED);
		return record;
	}

	private RecordDTO newRecordWithSingleReference(String referencedId) {
		String id = UUID.randomUUID().toString();
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put(singleReferenceFieldCode, referencedId);
		fields.put("schema_s", "zeSchemaType_default");

		RecordDTO record = new SolrRecordDTO(nextID(), -1, null, fields, RecordDTOMode.FULLY_LOADED);
		return record;
	}

	private RecordDTO newRecordWithSingleReferenceAndParentReference(String referencedId, String parentId) {
		String id = UUID.randomUUID().toString();
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put(singleReferenceFieldCode, referencedId);
		fields.put(parentReferenceFieldCode, parentId);
		fields.put("schema_s", "zeSchemaType_default");
		RecordDTO record = new SolrRecordDTO(nextID(), -1, null, fields, RecordDTOMode.FULLY_LOADED);
		return record;
	}

	private RecordDTO newRecordWithMultipleReferences(List<String> referencedId) {
		String id = UUID.randomUUID().toString();
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put(multipleReferencesFieldCode, referencedId);
		fields.put("schema_s", "zeSchemaType_default");
		RecordDTO record = new SolrRecordDTO(nextID(), -1, null, fields, RecordDTOMode.FULLY_LOADED);
		return record;
	}

	@Test
	@LoadTest
	public void whenAddingAndSearchingMultipleTimesWithSameDaoThenCanHandleLoad()
			throws Exception {
		for (int i = 0; i < 100; i++) {

			add(newRecordWithTitle("record #" + i));
			assertThat(recordDao.documentsCount()).isEqualTo(i + 1);
		}
	}

	@Test
	public void whenAddingRecordThenCanBeRetreivedWithHisNewId()
			throws Exception {

		RecordDTO record = newRecordWithTitle("test");

		add(record);

		assertEquals(record.getFields().get("title_s"), recordDao.get(record.getId()).getFields().get("title_s"));

	}

	@Test
	public void whenAddingRecordsToSearchThenNotAllFieldsAreSaved()
			throws Exception {

		add(child1);
		add(child2);
		add(child3);

		assertEquals(newArrayList(child1.getFields().get("tokens_ss")), recordDao.get(child1.getId()).getFields().get("tokens_ss"));
		assertEquals(child1.getFields().get("schema_s"), recordDao.get(child1.getId()).getFields().get("schema_s"));
		assertEquals(child1.getFields().get("archivisticStatus_s"), recordDao.get(child1.getId()).getFields().get("archivisticStatus_s"));
		assertEquals(child1.getFields().get("categoryId_s"), recordDao.get(child1.getId()).getFields().get("categoryId_s"));
		assertEquals(child2.getFields().get("collection_s"), recordDao.get(child2.getId()).getFields().get("collection_s"));
		assertEquals(newArrayList(Integer.parseInt((String) child2.getFields().get("attachedPrincipalAncestorsIntIds_is"))),
				recordDao.get(child2.getId()).getFields().get("attachedPrincipalAncestorsIntIds_is"));
		assertEquals(newArrayList(child2.getFields().get("attachedAncestors_ss")), recordDao.get(child2.getId()).getFields().get("attachedAncestors_ss"));
		assertEquals(child2.getFields().get("schema_s"), recordDao.get(child2.getId()).getFields().get("schema_s"));
		assertEquals(child2.getFields().get("hidden_s"), recordDao.get(child2.getId()).getFields().get("hidden_s"));
		assertEquals(child3.getFields().get("deleted_s"), recordDao.get(child3.getId()).getFields().get("deleted_s"));
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "title_t_fr:enfant2"))).hasSize(1);

		assertNotEquals(child1.getFields().get("title_s"), recordDao.get(child1.getId()).getFields().get("title_s"));
		assertNotEquals(child1.getFields().get("otherRefId_s"), recordDao.get(child1.getId()).getFields().get("otherRefId_s"));
		assertNotEquals(child1.getFields().get("parentPId_s"), recordDao.get(child1.getId()).getFields().get("parentPId_s"));
		assertNotEquals(child2.getFields().get("otherRefId_s"), recordDao.get(child2.getId()).getFields().get("otherRefId_s"));
		assertNotEquals(child2.getFields().get("title_s"), recordDao.get(child2.getId()).getFields().get("title_s"));
		assertNotEquals(child2.getFields().get("_version_"), recordDao.get(child2.getId()).getFields().get("_version_"));
	}

	@Test
	public void whenUpdatingRecordsToSearchThenOnlySearchFieldsAreUpdated()
			throws Exception {

		RecordDTO recordDTO = givenSavedRecordWithInitialValueInSavedMetadataFieldName();
		assertThat(recordDTO.getFields()).containsEntry(savedMetadataFieldName, initialValue);

		Map<String, Object> modifiedFields = new HashMap<>();
		modifiedFields.put("aField_s", "aValue");
		modifiedFields.put("aField_d", 42.0);
		modifiedFields.put("tokens_ss", newArrayList("jngidfngidufg==", "mfoisdnvlksdmvsfdsfvds=="));
		recordDTO = updateFieldsAndGetNewRecordDTO(recordDTO, modifiedFields);

		assertThat(recordDTO.getFields()).containsEntry(savedMetadataFieldName, initialValue)
				.doesNotContainEntry("aField_s", "aValue").doesNotContainEntry("aField_d", 42.0)
				.containsEntry("tokens_ss", newArrayList("jngidfngidufg==", "mfoisdnvlksdmvsfdsfvds=="));

	}

	@Test
	public void whenUpdatingRecordWithRemovedFieldsThenFieldsRemovedCorrectly()
			throws Exception {
		RecordDTO recordDTO = givenSavedRecordWithInitialValueInSavedMetadataFieldName();
		assertThat(recordDTO.getFields()).containsEntry(savedMetadataFieldName, initialValue);

		Map<String, Object> modifiedFields = new HashMap<>();
		modifiedFields.put(savedMetadataFieldName, null);
		modifiedFields.put("tokens_ss", newArrayList("jngidfngidufg==", "mfoisdnvlksdmvsfdsfvds=="));
		recordDTO = updateFieldsAndGetNewRecordDTO(recordDTO, modifiedFields);

		assertThat(recordDTO.getFields()).doesNotContainKey(savedMetadataFieldName)
				.containsEntry("tokens_ss", newArrayList("jngidfngidufg==", "mfoisdnvlksdmvsfdsfvds=="));
	}

	private void add(RecordDTO recordDTO) {
		recordDao.executeSimple(new TransactionSearchDTO(UUID.randomUUID().toString(), RecordsFlushing.NOW, Arrays.asList(recordDTO),
				new ArrayList<RecordDeltaDTO>()));
	}

	private RecordDTO updateFieldsAndGetNewRecordDTO(RecordDTO recordDTO, Map<String, Object> modifiedFields)
			throws Exception {
		update(new RecordDeltaDTO(recordDTO, modifiedFields, recordDTO.getFields()));
		return recordDao.get(recordDTO.getId());
	}

	private RecordDTO givenSavedRecordWithInitialValueInSavedMetadataFieldName() {
		return givenSavedRecordWithValueInSavedMetadataFieldName(initialValue);
	}

	private RecordDTO givenSavedRecordWithValueInSavedMetadataFieldName(String value) {
		Map<String, Object> fields = new HashMap<>();
		fields.put(savedMetadataFieldName, value);

		String id = UUID.randomUUID().toString();

		RecordDTO savedRecord = new SolrRecordDTO(id, -1, null, fields, RecordDTOMode.FULLY_LOADED);
		add(savedRecord);
		try {
			return recordDao.get(id);
		} catch (NoSuchRecordWithId e) {
			throw new RuntimeException(e);
		}

	}

	private void update(RecordDeltaDTO recordDeltaDTO) {
		recordDao.executeSimple(new TransactionSearchDTO(UUID.randomUUID().toString(), RecordsFlushing.NOW, new ArrayList<RecordDTO>(),
				Arrays.asList(recordDeltaDTO)));
	}

	private String nextID() {
		return UUID.randomUUID().toString();
	}

	private MapBuilder<String, Object> buildParamMapWith(String collection, String schema) {
		return MapBuilder.with("collection_s", (Object) collection).andWith("schema_s", schema);
	}

}
