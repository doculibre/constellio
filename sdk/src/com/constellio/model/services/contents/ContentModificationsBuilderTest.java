package com.constellio.model.services.contents;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.TestUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class ContentModificationsBuilderTest extends ConstellioTest {

	String schemaCode = "type_default";
	String id1 = "1";
	String id2 = "2";
	String id3 = "3";

	long length1 = 1;
	long length2 = 2;

	String username;
	@Mock User user;
	@Mock InputStream inputStream;
	@Mock MetadataSchemaTypes types;
	@Mock MetadataSchema schema;
	RecordImpl record;

	Metadata singleValueContentMetadata;
	Metadata multiValueContentMetadata;

	ContentModificationsBuilder builder;

	String firstHash = "firstHash";
	String secondHash = "secondHash";
	String thirdHash = "thirdHash";
	String fourthHash = "fourthHash";
	String fifthHash = "fifthHash";

	CollectionInfo collectionInfo = new CollectionInfo((byte) 0, zeCollection, "fr", asList("fr"));

	@Before
	public void setUp()
			throws Exception {
		builder = new ContentModificationsBuilder(types);

		Metadata textMetadata = TestUtils.mockManualMetadata("type_default_text", MetadataValueType.STRING);
		singleValueContentMetadata = TestUtils.mockManualMetadata("type_default_singleValue", MetadataValueType.CONTENT);
		multiValueContentMetadata = TestUtils
				.mockManualMultivalueMetadata("type_default_multivalueValue", MetadataValueType.CONTENT);
		when(singleValueContentMetadata.getDataStoreCode()).thenReturn("singleValue_s");
		when(multiValueContentMetadata.getDataStoreCode()).thenReturn("multivalueValue_ss");
		when(types.getSchema(schemaCode)).thenReturn(schema);
		when(types.getSchemaOf(any(Record.class))).thenReturn(schema);
		MetadataList metadatas = new MetadataList(textMetadata, singleValueContentMetadata, multiValueContentMetadata);
		when(schema.getMetadatas()).thenReturn(
				metadatas);
		when(schema.getContentMetadatasForPopulate()).thenReturn(
				metadatas);
		when(types.getMetadata("type_default_singleValue")).thenReturn(singleValueContentMetadata);
		when(types.getMetadata("type_default_multivalueValue")).thenReturn(multiValueContentMetadata);
		when(user.getUsername()).thenReturn(username);
	}

	@Test
	public void givenARecordWithANewSingleValueContentWhenBuildingContentModificationThenInNewContentList() {

		Content newContent = createMajor(id1, user, "file.pdf", contentVersionDataSummary(firstHash));

		record = newRecord();
		record.set(singleValueContentMetadata, newContent);

		assertThatNewHashListOfBuildForModifiedRecords(record).containsOnly(firstHash);
		assertThatDeleteListOfBuildForModifiedRecords(record).isEmpty();
		assertThatBuildForDeletedRecords(record).containsOnly(firstHash);
	}

	@Test
	public void givenARecordWithANewSingleValueContentReplacingAPreviousWhenBuildingContentModificationThenInNewContentListAndPreviousVersionsOfRemovedContentInDeletedList() {
		ContentImpl oldContent
				= createMajor(id1, user, "file.pdf", new ContentVersionDataSummary(firstHash, "mime1", length1));
		oldContent.updateContent(user, new ContentVersionDataSummary(secondHash, "mime1", length1), true);

		Content newContent = createMajor(id2, user, "file.pdf", contentVersionDataSummary(thirdHash));

		record = newExistingRecordWith(singleValueContentMetadata, toStr(oldContent));
		record.set(singleValueContentMetadata, newContent);

		assertThatNewHashListOfBuildForModifiedRecords(record).containsOnly(thirdHash);
		assertThatDeleteListOfBuildForModifiedRecords(record).containsOnly(firstHash, secondHash);
		assertThatBuildForDeletedRecords(record).containsOnly(firstHash, secondHash, thirdHash);
	}

	@Test
	public void givenARecordWithARemovedSingleValueContentWhenBuildingContentModificationThenInInDeletedList() {
		ContentImpl oldContent
				= createMajor(id1, user, "file.pdf", new ContentVersionDataSummary(firstHash, "mime1", length1));
		oldContent.updateContent(user, new ContentVersionDataSummary(secondHash, "mime1", length1), true);

		record = newExistingRecordWith(singleValueContentMetadata, toStr(oldContent));
		record.set(singleValueContentMetadata, null);

		assertThatNewHashListOfBuildForModifiedRecords(record).isEmpty();
		assertThatDeleteListOfBuildForModifiedRecords(record).containsOnly(firstHash, secondHash);
		assertThatBuildForDeletedRecords(record).containsOnly(firstHash, secondHash);
	}

	@Test
	public void givenARecordWithANewMultiValueContentWhenBuildingContentModificationThenInNewContentList() {

		Content newContent1 = createMajor(id1, user, "file.pdf", contentVersionDataSummary(firstHash));
		Content newContent2 = createMajor(id2, user, "file.pdf", contentVersionDataSummary(secondHash));

		record = newRecord();
		record.set(multiValueContentMetadata, Arrays.asList(newContent1, newContent2));

		assertThatNewHashListOfBuildForModifiedRecords(record).containsOnly(firstHash, secondHash);
		assertThatDeleteListOfBuildForModifiedRecords(record).isEmpty();
		assertThatBuildForDeletedRecords(record).containsOnly(firstHash, secondHash);
	}

	@Test
	public void givenARecordWithANewMultiValueContentReplacingAPreviousWhenBuildingContentModificationThenInNewContentListAndPreviousVersionsOfRemovedContentInDeletedList() {

		ContentImpl oldContent1
				= createMajor(id1, user, "file.pdf", new ContentVersionDataSummary(firstHash, "mime1", length1));
		oldContent1.updateContent(user, new ContentVersionDataSummary(secondHash, "mime1", length1), true);
		ContentImpl oldContent2
				= createMajor(id2, user, "file.pdf", new ContentVersionDataSummary(thirdHash, "mime1", length1));

		ContentImpl newContent = createMajor(id3, user, "file.pdf", contentVersionDataSummary(fourthHash));

		record = newExistingRecordWith(multiValueContentMetadata, Arrays.asList(toStr(oldContent1), toStr(oldContent2)));
		oldContent2 = (ContentImpl) record.getList(multiValueContentMetadata).get(1);
		record.set(multiValueContentMetadata, Arrays.asList(newContent, oldContent2));

		assertThatNewHashListOfBuildForModifiedRecords(record).containsOnly(fourthHash);
		assertThatDeleteListOfBuildForModifiedRecords(record).containsOnly(firstHash, secondHash);
		assertThatBuildForDeletedRecords(record).containsOnly(firstHash, secondHash, thirdHash, fourthHash);
	}

	@Test
	public void givenARecordWithARemovedMultiValueContentWhenBuildingContentModificationThenInInDeletedList() {
		ContentImpl oldContent1
				= createMajor(id1, user, "file.pdf", new ContentVersionDataSummary(firstHash, "mime1", length1));
		oldContent1.updateContent(user, new ContentVersionDataSummary(secondHash, "mime1", length1), true);

		ContentImpl oldContent2
				= createMajor(id2, user, "file.pdf", new ContentVersionDataSummary(thirdHash, "mime1", length1));

		record = newExistingRecordWith(multiValueContentMetadata, Arrays.asList(toStr(oldContent1), toStr(oldContent2)));
		record.set(multiValueContentMetadata, null);

		assertThatNewHashListOfBuildForModifiedRecords(record).isEmpty();
		assertThatDeleteListOfBuildForModifiedRecords(record).containsOnly(firstHash, secondHash, thirdHash);
		assertThatBuildForDeletedRecords(record).containsOnly(firstHash, secondHash, thirdHash);
	}

	@Test
	public void givenAContentWithNewVersionWhenBuildingContentModificationThenInNewVersionList() {
		ContentImpl content
				= createMajor(id1, user, "file.pdf", new ContentVersionDataSummary(firstHash, "mime1", length1));
		content.updateContent(user, new ContentVersionDataSummary(secondHash, "mime1", length1), true);

		record = newExistingRecordWith(singleValueContentMetadata, toStr(content));
		content = record.get(singleValueContentMetadata);
		content.updateContent(user, contentVersionDataSummary(thirdHash), true);

		assertThatNewHashListOfBuildForModifiedRecords(record).containsOnly(thirdHash);
		assertThatDeleteListOfBuildForModifiedRecords(record).isEmpty();
		assertThatBuildForDeletedRecords(record).containsOnly(firstHash, secondHash, thirdHash);
	}

	@Test
	public void givenACheckedOutContentWithNewVersionWhenBuildingContentModificationThenInNewVersionList() {
		ContentImpl content
				= createMajor(id1, user, "file.pdf", new ContentVersionDataSummary(firstHash, "mime1", length1));
		content.updateContent(user, new ContentVersionDataSummary(secondHash, "mime1", length1), true);

		record = newExistingRecordWith(singleValueContentMetadata, toStr(content));
		content = record.get(singleValueContentMetadata);
		content.checkOut(user).updateCheckedOutContent(contentVersionDataSummary(thirdHash));

		assertThatNewHashListOfBuildForModifiedRecords(record).containsOnly(thirdHash);
		assertThatDeleteListOfBuildForModifiedRecords(record).isEmpty();
		assertThatBuildForDeletedRecords(record).containsOnly(firstHash, secondHash, thirdHash);
	}

	@Test
	public void givenACheckedOutContentWithNewVersionWhenBuildingContentModificationThenInNewVersionListAndPreviousWorkContentInDeleteList() {
		ContentImpl content
				= createMajor(id1, user, "file.pdf", new ContentVersionDataSummary(firstHash, "mime1", length1));
		content.updateContent(user, new ContentVersionDataSummary(secondHash, "mime1", length1), true);
		content.checkOut(user).updateCheckedOutContent(new ContentVersionDataSummary(thirdHash, "mime2", length2));

		record = newExistingRecordWith(singleValueContentMetadata, toStr(content));
		content = record.get(singleValueContentMetadata);
		content.updateCheckedOutContent(contentVersionDataSummary(fourthHash));

		assertThatNewHashListOfBuildForModifiedRecords(record).containsOnly(fourthHash);
		assertThatDeleteListOfBuildForModifiedRecords(record).containsOnly(thirdHash);
		assertThatBuildForDeletedRecords(record).containsOnly(firstHash, secondHash, thirdHash, fourthHash);
	}

	@Test
	public void givenACheckedOutRecordThenCurrentCheckedOutVersionCurrentVersionAndHistoryVersionAreInDeleteList() {
		ContentImpl content
				= createMajor(id1, user, "file.pdf", new ContentVersionDataSummary(firstHash, "mime1", length1));
		content.updateContent(user, new ContentVersionDataSummary(secondHash, "mime1", length1), true);
		content.checkOut(user).updateCheckedOutContent(new ContentVersionDataSummary(thirdHash, "mime2", length2));

		record = newExistingRecordWith(singleValueContentMetadata, toStr(content));

		assertThatNewHashListOfBuildForModifiedRecords(record).isEmpty();
		assertThatDeleteListOfBuildForModifiedRecords(record).isEmpty();
		assertThatBuildForDeletedRecords(record).containsOnly(firstHash, secondHash, thirdHash);
	}

	@Test
	public void givenMultivalueContentThenAllVersionInDeleteList() {
		ContentImpl content
				= createMajor(id1, user, "file.pdf", new ContentVersionDataSummary(firstHash, "mime1", length1));
		content.updateContent(user, new ContentVersionDataSummary(secondHash, "mime1", length1), true);
		content.checkOut(user).updateCheckedOutContent(new ContentVersionDataSummary(thirdHash, "mime2", length2));

		ContentImpl content2
				= createMajor(id1, user, "file2.pdf", new ContentVersionDataSummary(fourthHash, "mime1", length1));

		record = newExistingRecordWith(multiValueContentMetadata, Arrays.asList(toStr(content), toStr(content2)));

		assertThatNewHashListOfBuildForModifiedRecords(record).isEmpty();
		assertThatDeleteListOfBuildForModifiedRecords(record).isEmpty();
		assertThatBuildForDeletedRecords(record).containsOnly(firstHash, secondHash, thirdHash, fourthHash);

	}

	@Test
	public void givenRecordWithTwoContentHashesInMultivalueMetadataWhenReplacingOneContentHashByAnotherOneThenInDelteList() {
		ContentImpl content
				= createMajor(id1, user, "file.pdf", new ContentVersionDataSummary(firstHash, "mime1", length1));
		content.updateContent(user, new ContentVersionDataSummary(secondHash, "mime1", length1), true);
		content.checkOut(user).updateCheckedOutContent(new ContentVersionDataSummary(thirdHash, "mime2", length2));
		ContentImpl content2
				= createMajor(id1, user, "file2.pdf", new ContentVersionDataSummary(fourthHash, "mime1", length1));
		record = newExistingRecordWith(multiValueContentMetadata, Arrays.asList(toStr(content), toStr(content2)));

		ContentImpl content3
				= createMajor(id1, user, "file3.pdf", new ContentVersionDataSummary(fifthHash, "mime1", length1));
		record.set(multiValueContentMetadata, Arrays.asList(content2, content3));

		assertThatNewHashListOfBuildForModifiedRecords(record).containsOnly(fifthHash);
		assertThatDeleteListOfBuildForModifiedRecords(record).containsOnly(firstHash, secondHash, thirdHash);
		assertThatBuildForDeletedRecords(record).containsOnly(firstHash, secondHash, thirdHash, fourthHash, fifthHash);
	}

	@Test
	public void givenRecordWithTwoContentHashesInsingleValueMetadataWhenReplacingOneContentHashByAnotherOneThenInDelteList() {
		ContentImpl content = createMajor(id1, user, "file.pdf", new ContentVersionDataSummary(firstHash, "mime1", length1));
		content.updateContent(user, new ContentVersionDataSummary(secondHash, "mime1", length1), true);
		content.checkOut(user).updateCheckedOutContent(new ContentVersionDataSummary(thirdHash, "mime2", length2));
		ContentImpl content2 = createMajor(id1, user, "file2.pdf", new ContentVersionDataSummary(fourthHash, "mime1", length1));
		record = newExistingRecordWith(singleValueContentMetadata, toStr(content));

		record.set(singleValueContentMetadata, content2);

		assertThatNewHashListOfBuildForModifiedRecords(record).containsOnly(fourthHash);
		assertThatDeleteListOfBuildForModifiedRecords(record).containsOnly(firstHash, secondHash, thirdHash);
		assertThatBuildForDeletedRecords(record).containsOnly(firstHash, secondHash, thirdHash, fourthHash);
	}

	private org.assertj.core.api.ListAssert<String> assertThatBuildForDeletedRecords(Record record) {
		return assertThat(builder.buildForDeletedRecords(asList(record)));
	}

	private org.assertj.core.api.ListAssert<String> assertThatDeleteListOfBuildForModifiedRecords(Record record) {
		return assertThat(builder.buildForModifiedRecords(asList(record)).getDeletedContentsVersionsHashes());
	}

	private org.assertj.core.api.ListAssert<String> assertThatNewHashListOfBuildForModifiedRecords(Record record) {
		return assertThat(builder.buildForModifiedRecords(asList(record)).getContentsWithNewVersion());
	}

	private ContentVersion version(String hash) {
		return new ContentVersion(contentVersionDataSummary(hash), "test.pdf", "zeVersion", "bob", new LocalDateTime(), null);
	}

	private ContentVersionDataSummary contentVersionDataSummary(String hash) {
		return new ContentVersionDataSummary(hash, aString(), aLong());
	}

	private RecordImpl newRecord() {
		return new TestRecord(schemaCode, zeCollection);
	}

	private String toStr(Content content) {
		return new ContentFactory().toString(content);
	}

	private RecordImpl newExistingRecordWith(Metadata metadata, Object value) {
		Map<String, Object> params = new HashMap<>();
		params.put(metadata.getDataStoreCode(), value);
		params.put("schema_s", schemaCode);
		params.put("collection_s", zeCollection);
		RecordDTO recordDTO = new SolrRecordDTO("zeId", 3L, null, params, RecordDTOMode.FULLY_LOADED);

		return new TestRecord(recordDTO, collectionInfo, (short) 1);
	}

	private ContentImpl createMajor(String id, User user, String filename, ContentVersionDataSummary newVersion) {
		return ContentImpl.create(id, user, filename, newVersion, true, false);
	}

	private ContentImpl createMinor(String id, User user, String filename, ContentVersionDataSummary newVersion) {
		return ContentImpl.create(id, user, filename, newVersion, false, false);
	}

}