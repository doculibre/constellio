package com.constellio.data.dao.services.records;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.solr.BigVaultRuntimeException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils.MapBuilder;
import com.constellio.sdk.tests.annotations.LoadTest;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.constellio.sdk.tests.TestUtils.asMap;
import static com.constellio.sdk.tests.TestUtils.asStringObjectMap;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.data.MapEntry.entry;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BigVaultRecordDaoRealTest extends ConstellioTest {

	public static final String singleReferenceFieldCode = "refId_s";
	public static final String parentReferenceFieldCode = "parentRefPId_s";
	public static final String multipleReferencesFieldCode = "refId_ss";
	private static final String ID_FIELD = "id";
	private final String aContentId = "aContentId";
	private final String savedMetadataFieldName = "savedField_s";
	private final String initialValue = "initialValue";
	private final String modifiedValue = "modifiedValue";
	LocalDateTime aDateTime = aDateTime();
	LocalDateTime anotherDateTime = aDateTime();
	LocalDate aDate = aDate();
	LocalDate anotherDate = aDate();
	private RecordDao recordDao;

	@Before
	public void setup() {
		recordDao = getDataLayerFactory().newRecordDao();

		Map<String, Object> fields = new HashMap<>();
		fields.put(savedMetadataFieldName, initialValue);

	}

	@Test
	public void __prepareTests__()
			throws Exception {

	}

	@Test(expected = OptimisticLocking.class)
	public void givenRecordAlreadyInStoreWhenAddingRecordThenExceptionThrown()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("title_s", "test");

		RecordDTO record = new SolrRecordDTO("1", 1, null, fields, false);

		add(record);
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
		RecordDTO record = new SolrRecordDTO(nextID(), -1, null, fields, false);
		return record;
	}

	private RecordDTO newRecordWithSingleReference(String referencedId) {
		String id = UUID.randomUUID().toString();
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put(singleReferenceFieldCode, referencedId);
		fields.put("schema_s", "zeSchemaType_default");

		RecordDTO record = new SolrRecordDTO(nextID(), -1, null, fields, false);
		return record;
	}

	private RecordDTO newRecordWithSingleReferenceAndParentReference(String referencedId, String parentId) {
		String id = UUID.randomUUID().toString();
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put(singleReferenceFieldCode, referencedId);
		fields.put(parentReferenceFieldCode, parentId);
		fields.put("schema_s", "zeSchemaType_default");
		RecordDTO record = new SolrRecordDTO(nextID(), -1, null, fields, false);
		return record;
	}

	private RecordDTO newRecordWithMultipleReferences(List<String> referencedId) {
		String id = UUID.randomUUID().toString();
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put(multipleReferencesFieldCode, referencedId);
		fields.put("schema_s", "zeSchemaType_default");
		RecordDTO record = new SolrRecordDTO(nextID(), -1, null, fields, false);
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
	public void whenAddingRecordWithReferenceToNonExistentIndexGivenValidationSkippedThenCanRetrieveRecord()
			throws Exception {
		RecordDTO record = newRecordWithSingleReference("idOfNonExistentIndex");
		recordDao.execute(new TransactionDTO(UUID.randomUUID().toString(), RecordsFlushing.NOW, Arrays.asList(record),
				new ArrayList<RecordDeltaDTO>(), new ArrayList<RecordDTO>(), new ArrayList<SolrParams>(), new HashSet<String>(),
				true, false));
		assertEquals(record.getFields().get("title_s"), recordDao.get(record.getId()).getFields().get("title_s"));
	}

	private RecordDTO newRecordWithParentPaths(List<String> parents) {
		String id = UUID.randomUUID().toString();
		List<String> paths = new ArrayList<>();
		for (String parentPath : parents) {
			paths.add(parentPath + "/" + id);
		}

		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("path_ss", paths);
		fields.put("schema_s", "zeSchemaType_default");
		RecordDTO record = new SolrRecordDTO(nextID(), -1, null, fields, false);
		return record;
	}

	@Test
	public void whenAddingRecordWithContentThenPersistContentId()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("content_s", aContentId);

		add(new SolrRecordDTO("zeId", -1, null, fields, false));

		RecordDTO savedRecord = recordDao.get("zeId");

		assertThat((String) savedRecord.getFields().get("content_s")).isEqualTo(aContentId);

	}

	@Test(expected = BigVaultRuntimeException.class)
	public void whenAddingRecordWithIncorrectFieldThenException()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("content_d", "thisIsNotANumber");

		add(new SolrRecordDTO(nextID(), -1, null, fields, false));

	}

	@Test
	public void whenStoringIntegersThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_i", 1);
		fields.put("field_is", Arrays.asList(1));
		fields.put("fields_is", Arrays.asList(1, 2));

		RecordDTO record = saveRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringStringsThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_s", "1");
		fields.put("field_ss", Arrays.asList("1"));
		fields.put("fields_ss", Arrays.asList("1", "1", "2", "3"));

		RecordDTO record = saveRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringStringsThenCanRetrieveThem2()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_s", "1");
		fields.put("field_ss", Arrays.asList("1"));
		fields.put("fields_ss", Arrays.asList("1", "2", "2", "3"));

		RecordDTO record = saveRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringLongsThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_l", 1l);
		fields.put("field_ls", Arrays.asList(1l));
		fields.put("fields_ls", Arrays.asList(1l, 2l, 2l, 3l));

		RecordDTO record = saveRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringTextsThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_t", "2");
		fields.put("field_txt", Arrays.asList("2"));
		fields.put("fields_txt", Arrays.asList("2", "3"));

		RecordDTO record = saveRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringFrenchTextsThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_t_fr", "banane");
		fields.put("field_txt_fr", Arrays.asList("banane"));
		fields.put("fields_txt_fr", Arrays.asList("banane", "pomme"));

		RecordDTO record = saveRecordWithFieldsAndLoadItFromStore(fields);

		assertThat(record.getFields()).doesNotContainKey("field_t_fr");
		assertThat(record.getFields()).doesNotContainKey("field_txt_fr");
		assertThat(record.getFields()).doesNotContainKey("fields_txt_fr");
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "field_t_fr:banane"))).hasSize(1);
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "field_txt_fr:banane"))).hasSize(1);
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "fields_txt_fr:banane"))).hasSize(1);
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "fields_txt_fr:pomme"))).hasSize(1);
	}

	@Test
	public void whenStoringEnglishTextsThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_t_en", "banana");
		fields.put("field_txt_en", Arrays.asList("banana"));
		fields.put("fields_txt_en", Arrays.asList("banana", "apple"));

		RecordDTO record = saveRecordWithFieldsAndLoadItFromStore(fields);

		assertThat(record.getFields()).doesNotContainKey("field_t_en");
		assertThat(record.getFields()).doesNotContainKey("field_txt_en");
		assertThat(record.getFields()).doesNotContainKey("fields_txt_en");
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "field_t_en:banana"))).hasSize(1);
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "field_txt_en:banana"))).hasSize(1);
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "fields_txt_en:banana"))).hasSize(1);
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "fields_txt_en:apple"))).hasSize(1);
	}

	@Test
	public void whenStoringBooleansThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_s", true);
		fields.put("field_ss", Arrays.asList(true));
		fields.put("fields_ss", Arrays.asList(true, false));

		RecordDTO record = saveRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringFloatsThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_f", 1.1f);
		fields.put("field_fs", Arrays.asList(2.2f));
		fields.put("fields_fs", Arrays.asList(2.2f, 3.3f));

		RecordDTO record = saveRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringDateTimesThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_dt", aDateTime);
		fields.put("field_dts", Arrays.asList(aDateTime, anotherDateTime));
		fields.put("fields_dts", Arrays.asList(aDateTime, anotherDateTime));
		fields.put("fields3_dts", null);
		fields.put("fields4_dts", Arrays.asList(null, null));
		fields.put("fields5_dts", Arrays.asList(null, aDateTime));

		RecordDTO record = saveRecordWithFieldsAndLoadItFromStore(fields);

		fields.put("fields4_dts", null);
		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringOldDateTimesThenCanRetrieveThem()
			throws Exception {

		LocalDateTime date1 = new LocalDateTime(500, 1, 2, 0, 0, 0, 0);
		LocalDateTime date2 = new LocalDateTime(1800, 1, 2, 0, 0, 0, 0);
		LocalDateTime date3 = new LocalDateTime(1899, 1, 2, 0, 0, 0, 0);
		LocalDateTime date4 = new LocalDateTime(1900, 1, 2, 3, 4, 5, 6);

		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_dt", date1);
		fields.put("field_dts", Arrays.asList(date1, date2, date3, date4));
		fields.put("fields_dts", Arrays.asList(date1, date2, date3, date4));

		RecordDTO record = saveRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringDatesThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_da", aDate);
		fields.put("field2_da", null);
		fields.put("field_das", Arrays.asList(aDate, anotherDate));
		fields.put("fields_das", Arrays.asList(aDate, anotherDate));
		fields.put("fields3_das", null);
		fields.put("fields4_das", Arrays.asList(null, null));
		fields.put("fields5_das", Arrays.asList(null, aDate));

		RecordDTO record = saveRecordWithFieldsAndLoadItFromStore(fields);

		fields.put("fields4_das", null);
		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringOldDatesThenCanRetrieveThem()
			throws Exception {

		LocalDate date1 = new LocalDate(500, 1, 2);
		LocalDate date2 = new LocalDate(1800, 1, 2);
		LocalDate date3 = new LocalDate(1899, 1, 2);
		LocalDate date4 = new LocalDate(1900, 1, 2);

		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_da", date1);
		fields.put("field2_da", null);
		fields.put("field_das", Arrays.asList(date1, date2, date3, date4));
		fields.put("fields_das", Arrays.asList(date1, date2, date3, date4));
		fields.put("fields3_das", null);

		RecordDTO record = saveRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringDoublesThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_d", 1.1);
		fields.put("field_ds", Arrays.asList(2.2));
		fields.put("fields_ds", Arrays.asList(2.2, 3.3));
		fields.put("field5_d", null);

		RecordDTO record = saveRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringIntegersWhileUpdatingThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_i", 1);
		fields.put("field_is", Arrays.asList(1));
		fields.put("fields_is", Arrays.asList(1, 2));

		RecordDTO record = updateRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringStringsWhileUpdatingThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_s", "1");
		fields.put("field_ss", Arrays.asList("1"));
		fields.put("fields_ss", Arrays.asList("1", "2"));

		RecordDTO record = updateRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringStringsWhileUpdatingThenCanRetrieveThem2()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_s", "1");
		fields.put("field_ss", Arrays.asList("1"));
		fields.put("fields_ss", Arrays.asList("1", "2"));

		RecordDTO record = updateRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringNullStringsWhileUpdatingThenNoValues()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_s", null);
		fields.put("fields_ss", null);
		fields.put("field2_ss", Arrays.asList((String) null));
		fields.put("field3_ss", Arrays.asList("value", null));

		RecordDTO record = updateRecordWithFieldsAndLoadItFromStore(fields);

		assertThat(record.getFields().get("field_s")).isNull();
		assertThat(record.getFields().get("fields_s")).isNull();
		assertThat(record.getFields().get("field2_ss")).isNull();
		assertThat(record.getFields().get("field3_ss")).isEqualTo(Arrays.asList("value", null));
	}

	@Test
	public void whenStoringLongsWhileUpdatingThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_l", 1l);
		fields.put("field_ls", Arrays.asList(1l));
		fields.put("fields_ls", Arrays.asList(1l, 2l));

		RecordDTO record = updateRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringTextsWhileUpdatingThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_t", "1");
		fields.put("field2_t", null);
		fields.put("field_txt", Arrays.asList("2"));
		fields.put("fields_txt", Arrays.asList("2", "3"));

		RecordDTO record = updateRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenLoadingARecordWith__NULL__InTextFieldsThenConsideredAsNull()
			throws Exception {
		givenDisabledAfterTestValidations();

		SolrInputDocument solrInputDocument = new SolrInputDocument();
		solrInputDocument.setField("id", "zeId");
		solrInputDocument.setField("field1_t", "__NULL__");
		solrInputDocument.setField("field2_t", asList("__NULL__"));
		solrInputDocument.setField("field3_txt", "__NULL__");
		solrInputDocument.setField("field4_txt", asList("__NULL__"));
		solrInputDocument.setField("field5_t", "value1");
		solrInputDocument.setField("field6_txt", asList("value2", "value3"));

		SolrClient solrClient = recordDao.getBigVaultServer().getNestedSolrServer();
		solrClient.add(solrInputDocument);
		solrClient.commit();

		RecordDTO record = recordDao.get("zeId");

		assertThat(record.getFields())
				.doesNotContainKey("field1_t")
				.doesNotContainKey("field2_t")
				.doesNotContainKey("field3_txt")
				.doesNotContainKey("field4_txt")
				.contains(
						entry("id", "zeId"),
						entry("field5_t", "value1"),
						entry("field6_txt", asList("value2", "value3"))
				);
	}

	@Test
	public void whenStoringFrenchTextsWhileUpdatingThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_t_fr", "banane");
		fields.put("field_txt_fr", Arrays.asList("banane"));
		fields.put("fields_txt_fr", Arrays.asList("banane", "pomme"));

		RecordDTO record = updateRecordWithFieldsAndLoadItFromStore(fields);

		assertThat(record.getFields()).doesNotContainKey("field_t_fr");
		assertThat(record.getFields()).doesNotContainKey("field_txt_fr");
		assertThat(record.getFields()).doesNotContainKey("fields_txt_fr");
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "field_t_fr:banane"))).hasSize(1);
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "field_txt_fr:banane"))).hasSize(1);
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "fields_txt_fr:banane"))).hasSize(1);
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "fields_txt_fr:pomme"))).hasSize(1);
	}

	@Test
	public void whenStoringEnglishTextsWhileUpdatingThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_t_en", "banana");
		fields.put("field_txt_en", Arrays.asList("banana"));
		fields.put("fields_txt_en", Arrays.asList("banana", "apple"));

		RecordDTO record = updateRecordWithFieldsAndLoadItFromStore(fields);

		assertThat(record.getFields()).doesNotContainKey("field_t_en");
		assertThat(record.getFields()).doesNotContainKey("field_txt_en");
		assertThat(record.getFields()).doesNotContainKey("fields_txt_en");
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "field_t_en:banana"))).hasSize(1);
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "field_txt_en:banana"))).hasSize(1);
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "fields_txt_en:banana"))).hasSize(1);
		assertThat(recordDao.searchQuery(new ModifiableSolrParams().set("q", "fields_txt_en:apple"))).hasSize(1);
	}

	@Test
	public void whenStoringBooleansWhileUpdatingThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_s", true);
		fields.put("field_ss", Arrays.asList(true));
		fields.put("fields_ss", Arrays.asList(true, false));

		RecordDTO record = updateRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringFloatsWhileUpdatingThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_f", 1.1f);
		fields.put("field_fs", Arrays.asList(2.2f));
		fields.put("fields_fs", Arrays.asList(2.2f, 3.3f));

		RecordDTO record = updateRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringDateTimesWhileUpdatingThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_dt", aDateTime);
		fields.put("field_dts", Arrays.asList(aDateTime, anotherDateTime));
		fields.put("fields_dts", Arrays.asList(aDateTime, anotherDateTime));

		RecordDTO record = updateRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringDatesWhileUpdatingThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_da", aDate);
		fields.put("field_das", Arrays.asList(aDate, anotherDate));
		fields.put("fields_das", Arrays.asList(aDate, anotherDate));

		RecordDTO record = updateRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	@Test
	public void whenStoringDoublesWhileUpdatingThenCanRetrieveThem()
			throws Exception {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("field_d", 1.1);
		fields.put("field_ds", Arrays.asList(2.2));
		fields.put("fields_ds", Arrays.asList(2.2, 3.3));

		RecordDTO record = updateRecordWithFieldsAndLoadItFromStore(fields);

		assertSameFields(record, fields);
	}

	private void assertSameFields(RecordDTO record, Map<String, Object> fields) {
		int size = 0;
		for (Map.Entry<String, Object> entry : fields.entrySet()) {
			Object expected = entry.getValue();
			Object was = record.getFields().get(entry.getKey());

			assertThat(was).describedAs("Field " + entry.getKey()).isEqualTo(expected);
			if (expected != null) {
				size++;
			}
		}
		Map<String, Object> recordFieldsWithoutSysS = new HashMap<>(record.getFields());
		recordFieldsWithoutSysS.remove("sys_s");
		recordFieldsWithoutSysS.remove("id");
		recordFieldsWithoutSysS.remove("_version_");
		recordFieldsWithoutSysS.remove("collection_s");
		assertThat(recordFieldsWithoutSysS.size()).isEqualTo(size);
	}

	private RecordDTO saveRecordWithFieldsAndLoadItFromStore(Map<String, Object> fields)
			throws OptimisticLocking {
		return saveRecordWithFieldsAndLoadItFromStore(fields, "zeId");
	}

	private RecordDTO saveRecordWithFieldsAndLoadItFromStore(Map<String, Object> fields, String id)
			throws OptimisticLocking {
		RecordDTO recordToSave = new SolrRecordDTO(id, -1, null, fields, false);
		add(recordToSave);

		try {
			return recordDao.get(id);
		} catch (NoSuchRecordWithId e) {
			throw new RuntimeException(e);
		}
	}

	private RecordDTO updateRecordWithFieldsAndLoadItFromStore(Map<String, Object> fields)
			throws Exception {
		Map<String, Object> initialValues = new HashMap<>();
		initialValues.put("collection_s", zeCollection);

		RecordDTO recordToSave = new SolrRecordDTO(nextID(), -1, null, initialValues, false);
		add(recordToSave);
		try {
			RecordDTO recordDTO = recordDao.get(recordToSave.getId());
			RecordDeltaDTO recordDeltaDTO = new RecordDeltaDTO(recordDTO, fields, recordDTO.getFields());
			update(recordDeltaDTO);
			return recordDao.get(recordDTO.getId());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void whenUpdatingRecordWithNewFieldsThenNewFieldsAddedCorrectly()
			throws Exception {
		RecordDTO recordDTO = givenSavedRecordWithInitialValueInSavedMetadataFieldName();
		assertThat(recordDTO.getFields()).containsEntry(savedMetadataFieldName, initialValue);

		Map<String, Object> modifiedFields = new HashMap<>();
		modifiedFields.put("aField_s", "aValue");
		modifiedFields.put("aField_d", 42.0);
		recordDTO = updateFieldsAndGetNewRecordDTO(recordDTO, modifiedFields);

		assertThat(recordDTO.getFields()).containsEntry(savedMetadataFieldName, initialValue).containsEntry("aField_s", "aValue")
				.containsEntry("aField_d", 42.0);
	}

	@Test
	public void whenUpdatingRecordWithRemovedFieldsThenFieldsRemovedCorrectly()
			throws Exception {
		RecordDTO recordDTO = givenSavedRecordWithInitialValueInSavedMetadataFieldName();
		assertThat(recordDTO.getFields()).containsEntry(savedMetadataFieldName, initialValue);

		Map<String, Object> modifiedFields = new HashMap<>();
		modifiedFields.put(savedMetadataFieldName, null);
		modifiedFields.put("aField_d", 42.0);
		recordDTO = updateFieldsAndGetNewRecordDTO(recordDTO, modifiedFields);

		assertThat(recordDTO.getFields()).doesNotContainKey(savedMetadataFieldName).containsEntry("aField_d", 42.0);
	}

	@Test
	public void whenUpdatingRecordWithModifiedFieldThenFieldModifiedCorrectly()
			throws Exception {
		RecordDTO recordDTO = givenSavedRecordWithInitialValueInSavedMetadataFieldName();
		assertThat(recordDTO.getFields()).containsEntry(savedMetadataFieldName, initialValue);

		Map<String, Object> modifiedFields = new HashMap<>();
		modifiedFields.put(savedMetadataFieldName, modifiedValue);
		modifiedFields.put("aField_d", 42.0);
		recordDTO = updateFieldsAndGetNewRecordDTO(recordDTO, modifiedFields);

		assertThat(recordDTO.getFields()).containsEntry(savedMetadataFieldName, modifiedValue).containsEntry("aField_d", 42.0);
	}

	@Test
	public void whenUpdatingRecordWithModifiedReferencesFieldRemainingValidThenNoProblem()
			throws Exception {
		RecordDTO recordDTO = givenSavedRecordWithInitialValidReferencesValue();

		Map<String, Object> modifiedFields = new HashMap<>();
		RecordDTO referencedRecord3 = newRecordWithTitle("referencedRecord3");
		add(referencedRecord3);
		referencedRecord3 = recordDao.get(referencedRecord3.getId());
		modifiedFields.put(multipleReferencesFieldCode, Arrays.asList(referencedRecord3.getId()));
		RecordDTO postChangeRecord = updateFieldsAndGetNewRecordDTO(recordDTO, modifiedFields);

		assertThat((List) postChangeRecord.getFields().get(multipleReferencesFieldCode)).contains(referencedRecord3.getId());
	}

	private RecordDTO givenSavedRecordWithInitialValidReferenceValue()
			throws OptimisticLocking, NoSuchRecordWithId {
		RecordDTO referencedRecord = newRecordWithTitle("referencedRecord");
		RecordDTO record = newRecordWithSingleReference(referencedRecord.getId());
		add(referencedRecord);
		add(record);
		return recordDao.get(record.getId());
	}

	private RecordDTO givenSavedRecordWithInitialValidReferencesValue()
			throws OptimisticLocking, NoSuchRecordWithId {
		RecordDTO referencedRecord1 = newRecordWithTitle("referencedRecord1");
		RecordDTO referencedRecord2 = newRecordWithTitle("referencedRecord2");
		RecordDTO record = newRecordWithMultipleReferences(Arrays.asList(referencedRecord1.getId(), referencedRecord2.getId()));
		add(referencedRecord1);
		add(referencedRecord2);
		add(record);
		return recordDao.get(record.getId());
	}

	@Test()
	public void whenUpdatingRecordWithOldVersionThenOptimisticLockingException()
			throws Exception {
		RecordDTO recordDTO = givenSavedRecordWithInitialValueInSavedMetadataFieldName();
		assertThat(recordDTO.getFields()).containsEntry(savedMetadataFieldName, initialValue);

		Map<String, Object> modifiedFields = new HashMap<>();
		modifiedFields.put(savedMetadataFieldName, modifiedValue);
		modifiedFields.put("aField_d", 42.0);
		updateFieldsAndGetNewRecordDTO(recordDTO, modifiedFields);

		try {
			recordDTO = updateFieldsAndGetNewRecordDTO(recordDTO, modifiedFields);
			fail("RecordDaoException.OptimisticLocking expected");
		} catch (RecordDaoException.OptimisticLocking e) {
			assertThat(e.getId()).isEqualTo(recordDTO.getId());
			assertThat(e.getVersion()).isEqualTo(recordDTO.getVersion());
		}
	}

	@Test(expected = BigVaultRuntimeException.class)
	public void whenUpdatingRecordWithInvalidFieldThenException()
			throws Exception {
		RecordDTO recordDTO = givenSavedRecordWithInitialValueInSavedMetadataFieldName();

		Map<String, Object> modifiedFields = new HashMap<>();
		modifiedFields.put(savedMetadataFieldName, modifiedValue);
		modifiedFields.put("aField_d", "thisIsNotANumber");
		updateFieldsAndGetNewRecordDTO(recordDTO, modifiedFields);
	}

	private RecordDTO updateFieldsAndGetNewRecordDTO(RecordDTO recordDTO, Map<String, Object> modifiedFields)
			throws Exception {
		update(new RecordDeltaDTO(recordDTO, modifiedFields, recordDTO.getFields()));
		return recordDao.get(recordDTO.getId());
	}

	private RecordDTO givenSavedRecordWithInitialValueInSavedMetadataFieldName()
			throws OptimisticLocking {
		return givenSavedRecordWithValueInSavedMetadataFieldName(initialValue);
	}

	private RecordDTO givenSavedRecordWithValueInSavedMetadataFieldName(String value)
			throws OptimisticLocking {
		Map<String, Object> fields = new HashMap<>();
		fields.put(savedMetadataFieldName, value);

		String id = UUID.randomUUID().toString();

		RecordDTO savedRecord = new SolrRecordDTO(id, -1, null, fields, false);
		add(savedRecord);
		try {
			return recordDao.get(id);
		} catch (NoSuchRecordWithId e) {
			throw new RuntimeException(e);
		}

	}

	@Test
	public void whenUpdatingWithRecordDTOWithoutModificationsThenDocumentNotModified()
			throws Exception {
		RecordDTO recordDTO = givenSavedRecordWithInitialValueInSavedMetadataFieldName();

		update(new RecordDeltaDTO(recordDTO, new HashMap<String, Object>(), recordDTO.getFields()));

		RecordDTO modifiedRecordDTO = recordDao.get(recordDTO.getId());
		assertThat(modifiedRecordDTO.getFields().get(savedMetadataFieldName)).isEqualTo(
				recordDTO.getFields().get(savedMetadataFieldName));
		assertThat(modifiedRecordDTO.getFields()).isEqualTo(recordDTO.getFields());
		assertThat(modifiedRecordDTO.getVersion()).isEqualTo(recordDTO.getVersion());
	}

	@Test
	public void whenUpdatingWithRecordDTOWithoutModificationsThenDocumentNewVersionAndSameValues()
			throws Exception {
		RecordDTO recordDTO = givenSavedRecordWithInitialValueInSavedMetadataFieldName();

		update(new RecordDeltaDTO(recordDTO, asStringObjectMap(savedMetadataFieldName, initialValue), recordDTO.getFields()));

		RecordDTO modifiedRecordDTO = recordDao.get(recordDTO.getId());
		assertThat(modifiedRecordDTO.getFields().get(savedMetadataFieldName)).isEqualTo(
				recordDTO.getFields().get(savedMetadataFieldName));
		assertThat(modifiedRecordDTO.getVersion()).isNotEqualTo(recordDTO.getVersion());
	}

	@Test
	public void givenOptimisticLockingWhenUpdatingRecordThenChangesNotConserved()
			throws Exception {
		RecordDTO recordDTO = givenSavedRecordWithInitialValueInSavedMetadataFieldName();
		RecordDTO dto = recordDao.get(recordDTO.getId());
		assertThat(dto.getFields().get(savedMetadataFieldName)).isEqualTo(initialValue);

		update(new RecordDeltaDTO(recordDTO, asStringObjectMap("anotherField_d", 1.1), recordDTO.getFields()));

		long secondVersion = recordDao.get(recordDTO.getId()).getVersion();

		dto = recordDao.get(recordDTO.getId());
		assertThat(dto.getFields().get(savedMetadataFieldName)).isEqualTo(initialValue);

		try {
			update(new RecordDeltaDTO(recordDTO, asStringObjectMap(savedMetadataFieldName, modifiedValue),
					recordDTO.getFields()));
			fail("Optimsitic locking expected");
		} catch (RecordDaoException.OptimisticLocking e) {
			dto = recordDao.get(recordDTO.getId());
			assertThat(dto.getFields().get(savedMetadataFieldName)).isEqualTo(initialValue);
			assertThat(dto.getVersion()).isEqualTo(secondVersion);
		}

	}

	@Test
	public void whenExecutingTransactionThenAllModificationsApplied()
			throws Exception {
		RecordDTO firstAlreadySavedRecord = givenSavedRecordWithInitialValueInSavedMetadataFieldName();
		RecordDTO secondAlreadySavedRecord = givenSavedRecordWithInitialValueInSavedMetadataFieldName();
		long initialDocumentsCount = recordDao.documentsCount();
		RecordDTO firstAddedRecord = new SolrRecordDTO(nextID(), asStringObjectMap(savedMetadataFieldName, "firstAdded"), false);
		RecordDTO secondAddedRecord = new SolrRecordDTO(nextID(), asStringObjectMap(savedMetadataFieldName, "secondAdded"), false);
		RecordDeltaDTO firstUpdatedRecord = new RecordDeltaDTO(firstAlreadySavedRecord, asStringObjectMap(savedMetadataFieldName,
				"firstModified"), firstAddedRecord.getFields());
		RecordDeltaDTO secondUpdatedRecord = new RecordDeltaDTO(secondAlreadySavedRecord, asStringObjectMap(
				savedMetadataFieldName, "secondModified"), secondAddedRecord.getFields());
		TransactionDTO transaction = new TransactionDTO(UUID.randomUUID().toString(), RecordsFlushing.NOW,
				asList(firstAddedRecord, secondAddedRecord), asList(firstUpdatedRecord, secondUpdatedRecord));

		recordDao.execute(transaction);
		RecordDTO firstModifiedRecord = recordDao.get(firstAlreadySavedRecord.getId());
		RecordDTO secondModifiedRecord = recordDao.get(secondAlreadySavedRecord.getId());

		assertThat(recordDao.documentsCount()).isEqualTo(initialDocumentsCount + 2);
		assertThat(firstAddedRecord.getFields().get(savedMetadataFieldName)).isEqualTo("firstAdded");
		assertThat(secondAddedRecord.getFields().get(savedMetadataFieldName)).isEqualTo("secondAdded");
		assertThat(firstModifiedRecord.getFields().get(savedMetadataFieldName)).isEqualTo("firstModified");
		assertThat(secondModifiedRecord.getFields().get(savedMetadataFieldName)).isEqualTo("secondModified");

	}

	private String nextID() {
		return UUID.randomUUID().toString();
	}

	private void updateRecordChangingItsVersion(RecordDTO recordDTO)
			throws Exception {
		RecordDTO zeRecord = recordDao.get(recordDTO.getId());
		update(new RecordDeltaDTO(zeRecord, asStringObjectMap("anotaTrivialField_s", 1.1), zeRecord.getFields()));
	}

	private void add(RecordDTO recordDTO)
			throws OptimisticLocking {
		recordDao.execute(new TransactionDTO(UUID.randomUUID().toString(), RecordsFlushing.NOW, Arrays.asList(recordDTO),
				new ArrayList<RecordDeltaDTO>()));
	}

	private void update(RecordDeltaDTO recordDeltaDTO)
			throws OptimisticLocking {
		recordDao.execute(new TransactionDTO(UUID.randomUUID().toString(), RecordsFlushing.NOW, new ArrayList<RecordDTO>(),
				Arrays.asList(recordDeltaDTO)));
	}

	@Test
	public void givenExceptionWhenExecutingTransactionThenNoModificationsApplied_run1()
			throws Exception {
		givenExceptionWhenExecutingTransactionThenNoModificationsApplied();
	}

	@Test
	public void givenExceptionWhenExecutingTransactionThenNoModificationsApplied_run2()
			throws Exception {
		givenExceptionWhenExecutingTransactionThenNoModificationsApplied();
	}

	@Test
	public void givenExceptionWhenExecutingTransactionThenNoModificationsApplied_run3()
			throws Exception {
		givenExceptionWhenExecutingTransactionThenNoModificationsApplied();
	}

	@Test
	public void givenExceptionWhenExecutingTransactionThenNoModificationsApplied_run4()
			throws Exception {
		givenExceptionWhenExecutingTransactionThenNoModificationsApplied();
	}

	@Test
	public void givenExceptionWhenExecutingTransactionThenNoModificationsApplied_run5()
			throws Exception {
		givenExceptionWhenExecutingTransactionThenNoModificationsApplied();
	}

	private void givenExceptionWhenExecutingTransactionThenNoModificationsApplied()
			throws Exception {
		List<SolrParams> deleteByQueries = asList((SolrParams) new ModifiableSolrParams().set("q", "savedField_s:666"));

		RecordDTO firstAlreadySavedRecord = givenSavedRecordWithInitialValueInSavedMetadataFieldName();
		RecordDTO secondAlreadySavedRecord = givenSavedRecordWithInitialValueInSavedMetadataFieldName();
		RecordDTO thirdAlreadySavedRecordWhichIsDeletedById = givenSavedRecordWithInitialValueInSavedMetadataFieldName();
		RecordDTO fourthAlreadySavedRecordWhichIsDeletedById = givenSavedRecordWithInitialValueInSavedMetadataFieldName();
		RecordDTO fifthAlreadySavedRecordWhichIsDeletedByQuery = givenSavedRecordWithValueInSavedMetadataFieldName("666");
		updateRecordChangingItsVersion(firstAlreadySavedRecord);
		printTimeElapsedSinceLastCall("1");
		long initialDocumentsCount = recordDao.documentsCount();

		RecordDTO firstAddedRecord = new SolrRecordDTO(nextID(), asStringObjectMap(savedMetadataFieldName, "firstAdded"), false);
		RecordDTO secondAddedRecord = new SolrRecordDTO(nextID(), asStringObjectMap(savedMetadataFieldName, "secondAdded"), false);
		RecordDeltaDTO firstUpdatedRecord = new RecordDeltaDTO(firstAlreadySavedRecord, asStringObjectMap(savedMetadataFieldName,
				"firstModified"), firstAddedRecord.getFields());
		RecordDeltaDTO secondUpdatedRecord = new RecordDeltaDTO(secondAlreadySavedRecord, asStringObjectMap(
				savedMetadataFieldName, "secondModified"), secondAddedRecord.getFields());
		TransactionDTO transaction = new TransactionDTO(UUID.randomUUID().toString(), RecordsFlushing.NOW,
				asList(firstAddedRecord, secondAddedRecord), asList(firstUpdatedRecord, secondUpdatedRecord),
				asList(thirdAlreadySavedRecordWhichIsDeletedById, fourthAlreadySavedRecordWhichIsDeletedById), deleteByQueries);

		printTimeElapsedSinceLastCall("2");
		try {
			recordDao.execute(transaction);
			fail("Optimistic locking expteced");
		} catch (RecordDaoException.OptimisticLocking e) {
			// OK
		}

		updateRecordChangingItsVersion(firstAlreadySavedRecord);
		printTimeElapsedSinceLastCall("3");

		secondAlreadySavedRecord = recordDao.get(secondAlreadySavedRecord.getId());
		thirdAlreadySavedRecordWhichIsDeletedById = recordDao.get(thirdAlreadySavedRecordWhichIsDeletedById.getId());
		fourthAlreadySavedRecordWhichIsDeletedById = recordDao.get(fourthAlreadySavedRecordWhichIsDeletedById.getId());
		secondUpdatedRecord = new RecordDeltaDTO(secondAlreadySavedRecord, asStringObjectMap(
				savedMetadataFieldName, "secondModified"), secondAddedRecord.getFields());

		assertThat(getFieldValue(secondAlreadySavedRecord.getId(), savedMetadataFieldName)).isEqualTo(initialValue);
		assertThat(getFieldValue(secondUpdatedRecord.getId(), savedMetadataFieldName)).isEqualTo(initialValue);
		assertThat(recordDao.documentsCount()).isEqualTo(initialDocumentsCount);
		assertThat(hasRecord(firstAddedRecord.getId())).isFalse();
		assertThat(hasRecord(secondAddedRecord.getId())).isFalse();
		assertThat(hasRecord(thirdAlreadySavedRecordWhichIsDeletedById.getId())).isTrue();
		assertThat(hasRecord(fourthAlreadySavedRecordWhichIsDeletedById.getId())).isTrue();
		assertThat(hasRecord(fifthAlreadySavedRecordWhichIsDeletedByQuery.getId())).isTrue();
		printTimeElapsedSinceLastCall("4");
		//Let's do the same transaction without the document with optimistic locking and verify that it is correct
		transaction = new TransactionDTO(UUID.randomUUID().toString(), RecordsFlushing.NOW,
				asList(firstAddedRecord, secondAddedRecord), asList(secondUpdatedRecord),
				asList(thirdAlreadySavedRecordWhichIsDeletedById, fourthAlreadySavedRecordWhichIsDeletedById), deleteByQueries);
		recordDao.execute(transaction);
		printTimeElapsedSinceLastCall("5");
		assertThat(getFieldValue(secondUpdatedRecord.getId(), savedMetadataFieldName)).isEqualTo("secondModified");
		printTimeElapsedSinceLastCall("5a");
		assertThat(recordDao.documentsCount()).isEqualTo(initialDocumentsCount - 1);
		printTimeElapsedSinceLastCall("5b");
		assertThat(hasRecord(firstAddedRecord.getId())).isTrue();
		printTimeElapsedSinceLastCall("5c");
		assertThat(hasRecord(secondAddedRecord.getId())).isTrue();
		printTimeElapsedSinceLastCall("5d");
		assertThat(hasRecord(thirdAlreadySavedRecordWhichIsDeletedById.getId())).isFalse();
		printTimeElapsedSinceLastCall("5e");
		assertThat(hasRecord(fourthAlreadySavedRecordWhichIsDeletedById.getId())).isFalse();
		printTimeElapsedSinceLastCall("5f");
		assertThat(hasRecord(fifthAlreadySavedRecordWhichIsDeletedByQuery.getId())).isFalse();
		printTimeElapsedSinceLastCall("6");
	}

	@Test
	public void givenDeleteByQueryCalledThenDeleteOnlyTargettedRecords()
			throws Exception {

		Map<String, Object> fieldWith42 = new HashMap();
		fieldWith42.put("afield_s", 42);
		Map<String, Object> fieldWith42AndChuckNorris = new HashMap();
		fieldWith42AndChuckNorris.put("afield_s", 42);
		fieldWith42AndChuckNorris.put("anotherfield_s", "ChuckNorris");
		Map<String, Object> fieldWith28AndChuckNorris = new HashMap();
		fieldWith28AndChuckNorris.put("afield_s", 28);
		fieldWith28AndChuckNorris.put("anotherfield_s", "ChuckNorris");

		RecordDTO record1 = saveRecordWithFieldsAndLoadItFromStore(fieldWith42, "1");
		RecordDTO record2 = saveRecordWithFieldsAndLoadItFromStore(fieldWith42AndChuckNorris, "2");
		RecordDTO record3 = saveRecordWithFieldsAndLoadItFromStore(fieldWith28AndChuckNorris, "3");
		assertThat(hasRecord(record1.getId())).isTrue();
		assertThat(hasRecord(record2.getId())).isTrue();
		assertThat(hasRecord(record3.getId())).isTrue();

		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.set("q", "afield_s:42");
		solrParams.set("fq", "anotherfield_s:ChuckNorris");
		recordDao.execute(new TransactionDTO(RecordsFlushing.NOW).withDeletedByQueries(solrParams));
		assertThat(hasRecord(record1.getId())).isTrue();
		assertThat(hasRecord(record2.getId())).isFalse();
		assertThat(hasRecord(record3.getId())).isTrue();

		solrParams = new ModifiableSolrParams();
		solrParams.set("q", "*:*");
		solrParams.set("fq", "anotherfield_s:ChuckNorris");
		recordDao.execute(new TransactionDTO(RecordsFlushing.NOW).withDeletedByQueries(solrParams));
		assertThat(hasRecord(record1.getId())).isTrue();
		assertThat(hasRecord(record2.getId())).isFalse();
		assertThat(hasRecord(record3.getId())).isFalse();

		solrParams = new ModifiableSolrParams();
		solrParams.set("q", "afield_s:42");
		recordDao.execute(new TransactionDTO(RecordsFlushing.NOW).withDeletedByQueries(solrParams));
		assertThat(hasRecord(record1.getId())).isFalse();
		assertThat(hasRecord(record2.getId())).isFalse();
		assertThat(hasRecord(record3.getId())).isFalse();
	}

	@Test
	public void givenDeleteByIdCalledThenDeleteOnlyTargettedRecords()
			throws Exception {

		Map<String, Object> fieldWith42 = new HashMap();
		fieldWith42.put("afield_s", 42);
		Map<String, Object> fieldWith42AndChuckNorris = new HashMap();
		fieldWith42AndChuckNorris.put("afield_s", 42);
		fieldWith42AndChuckNorris.put("anotherfield_s", "ChuckNorris");
		Map<String, Object> fieldWith28AndChuckNorris = new HashMap();
		fieldWith28AndChuckNorris.put("afield_s", 28);
		fieldWith28AndChuckNorris.put("anotherfield_s", "ChuckNorris");

		RecordDTO record1 = saveRecordWithFieldsAndLoadItFromStore(fieldWith42, "1");
		RecordDTO record2 = saveRecordWithFieldsAndLoadItFromStore(fieldWith42AndChuckNorris, "2");
		RecordDTO record3 = saveRecordWithFieldsAndLoadItFromStore(fieldWith28AndChuckNorris, "3");
		assertThat(hasRecord(record1.getId())).isTrue();
		assertThat(hasRecord(record2.getId())).isTrue();
		assertThat(hasRecord(record3.getId())).isTrue();

		List<RecordDTO> newRecords = new ArrayList<>();
		List<RecordDeltaDTO> modifiedRecords = new ArrayList<>();
		List<RecordDTO> deletedRecords = Arrays.asList(record1, record3);
		List<SolrParams> deletedRecordsByQuery = new ArrayList<>();

		TransactionDTO transactionDTO = new TransactionDTO(UUID.randomUUID().toString(), RecordsFlushing.NOW, newRecords,
				modifiedRecords, deletedRecords, deletedRecordsByQuery);

		recordDao.execute(transactionDTO);

		assertThat(hasRecord(record1.getId())).isFalse();
		assertThat(hasRecord(record2.getId())).isTrue();
		assertThat(hasRecord(record3.getId())).isFalse();
	}

	@Test
	public void whenAddingLaterThenAddedOnNextUpdateRequiringLockTransation()
			throws Exception {
		RecordDTO banana = new SolrRecordDTO("banana", -1, null, asStringObjectMap("field_t_fr", "banana"), false);
		RecordDTO apple = new SolrRecordDTO("apple", -1, null, asStringObjectMap("field_t_fr", "apple"), false);
		RecordDTO melon = new SolrRecordDTO("melon", -1, null, asStringObjectMap("field_t_fr", "melon"), false);
		RecordDTO orange = new SolrRecordDTO("orange", -1, null, asStringObjectMap("field_t_fr", "orange"), false);
		RecordDTO kiwi = new SolrRecordDTO("kiwi", -1, null, asStringObjectMap("field_t_fr", "kiwi"), false);
		recordDao.execute(new TransactionDTO(RecordsFlushing.ADD_LATER).withNewRecords(asList(banana, apple)));
		recordDao.execute(new TransactionDTO(RecordsFlushing.ADD_LATER).withNewRecords(asList(melon)));

		recordDao.getBigVaultServer().getNestedSolrServer().commit(true, true, true);

		assertThat(hasRecord("banana")).isFalse();
		assertThat(hasRecord("apple")).isFalse();
		assertThat(hasRecord("melon")).isFalse();
		assertThat(hasRecord("orange")).isFalse();
		assertThat(hasRecord("kiwi")).isFalse();

		recordDao.execute(new TransactionDTO(RecordsFlushing.NOW()).withNewRecords(asList(orange)));

		assertThat(hasRecord("banana")).isFalse();
		assertThat(hasRecord("apple")).isFalse();
		assertThat(hasRecord("melon")).isFalse();
		assertThat(hasRecord("orange")).isTrue();
		assertThat(hasRecord("kiwi")).isFalse();

		recordDao.execute(new TransactionDTO(RecordsFlushing.NOW()).withModifiedRecords(asList(
				new RecordDeltaDTO(recordDao.get("orange"), asStringObjectMap("field_t_fr", "orangina")))));

		assertThat(hasRecord("banana")).isFalse();
		assertThat(hasRecord("apple")).isFalse();
		assertThat(hasRecord("melon")).isFalse();
		assertThat(hasRecord("orange")).isTrue();
		assertThat(hasRecord("kiwi")).isFalse();

		recordDao.execute(new TransactionDTO(RecordsFlushing.NOW()).withNewRecords(asList(kiwi)).withModifiedRecords(asList(
				new RecordDeltaDTO(recordDao.get("orange"), asStringObjectMap("field_t_fr", "good old orange")))));

		assertThat(hasRecord("banana")).isTrue();
		assertThat(hasRecord("apple")).isTrue();
		assertThat(hasRecord("melon")).isTrue();
		assertThat(hasRecord("orange")).isTrue();
		assertThat(hasRecord("kiwi")).isTrue();
	}

	@Test
	public void whenAddingLaterThenAddedWhenFlushing()
			throws Exception {
		RecordDTO banana = new SolrRecordDTO("banana", -1, null, asStringObjectMap("field_t_fr", "banana"), false);
		RecordDTO apple = new SolrRecordDTO("apple", -1, null, asStringObjectMap("field_t_fr", "apple"), false);
		RecordDTO melon = new SolrRecordDTO("melon", -1, null, asStringObjectMap("field_t_fr", "melon"), false);
		recordDao.execute(new TransactionDTO(RecordsFlushing.ADD_LATER).withNewRecords(asList(banana, apple)));
		recordDao.execute(new TransactionDTO(RecordsFlushing.ADD_LATER).withNewRecords(asList(melon)));

		recordDao.getBigVaultServer().getNestedSolrServer().commit(true, true, true);

		assertThat(hasRecord("banana")).isFalse();
		assertThat(hasRecord("apple")).isFalse();
		assertThat(hasRecord("melon")).isFalse();

		recordDao.getBigVaultServer().flush();

		assertThat(hasRecord("banana")).isTrue();
		assertThat(hasRecord("apple")).isTrue();
		assertThat(hasRecord("melon")).isTrue();

	}

	@Test
	public void whenAddingNewRecordsWithOneOfThemAlreadyExistingThenNothingChanged()
			throws Exception {
		add(new SolrRecordDTO("record1", -1, null, asMap("title_s", (Object) "Title 1"), false));
		long versionAfterFirstTx = recordDao.get("record1").getVersion();

		try {
			recordDao.execute(new TransactionDTO(RecordsFlushing.NOW()).withNewRecords(asList(
					new SolrRecordDTO("record1", -1, null, asMap("title_s", (Object) "Title 1"), false),
					new SolrRecordDTO("record2", -1, null, asMap("title_s", (Object) "Title 2"), false),
					new SolrRecordDTO("record3", -1, null, asMap("title_s", (Object) "Title 3"), false)
			)));
			fail("Exception expected");
		} catch (RecordDaoException.OptimisticLocking e) {
			//OK
		}

		assertThat(hasRecord("record1")).isTrue();
		assertThat(hasRecord("record2")).isFalse();
		assertThat(hasRecord("record3")).isFalse();

		long versionAfterSecondTx = recordDao.get("record1").getVersion();
		assertThat(versionAfterSecondTx).isEqualTo(versionAfterFirstTx);

	}

	private String getFieldValue(String id, String field) {
		try {
			return (String) recordDao.get(id).getFields().get(field);
		} catch (NoSuchRecordWithId noSuchRecordWithId) {
			throw new RuntimeException(noSuchRecordWithId);
		}
	}

	private boolean hasRecord(String id) {

		try {
			recordDao.get(id);
			return true;
		} catch (NoSuchRecordWithId noSuchRecordWithId) {
			return false;
		}
	}

	private MapBuilder<String, Object> buildParamMapWith(String collection, String schema) {
		return MapBuilder.with("collection_s", (Object) collection).andWith("schema_s", schema);
	}
}
