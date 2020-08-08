package com.constellio.app.api.search;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class MultilingualFreeTextSearchBrokenAcceptTest extends ConstellioTest {

	String englishSearchField = "search_txt_en";
	String frenchSearchField = "search_txt_fr";

	String gangalfEnglishQuote = "All we have to decide is what to do with the time that is given us.";
	String gangalfFrenchQuote = "Vous ne passerez pas!";
	String vadorFirstEnglishQuote = "I find your lack of faith disturbing!";
	String vadorSecondEnglishQuote = "I am altering the deal, pray I do not alter it any further…";
	String vadorThirdEnglishQuote = "You underestimate the power of the dark side";

	SearchableQuoteWord vadorDocx = SearchableQuoteWord.english("vador.docx");
	SearchableQuoteWord vadorDocxFr = SearchableQuoteWord.french("vador.docx");
	SearchableQuoteWord darthDocx = SearchableQuoteWord.english("darth.docx");
	SearchableQuoteWord darthDocxFr = SearchableQuoteWord.french("darth.docx");
	SearchableQuoteWord gandalfDocxEn = SearchableQuoteWord.english("gandalf.docx");
	SearchableQuoteWord gandalfDocxFr = SearchableQuoteWord.french("gandalf.docx");
	List<SearchableQuoteWord> allFilenames = asList(vadorDocx, darthDocx, gandalfDocxEn, gandalfDocxFr, vadorDocxFr, darthDocxFr);

	SearchableQuoteWord wordInGandalfEnglishQuote = SearchableQuoteWord.english("decide");
	SearchableQuoteWord wordInGandalfFrenchQuote = SearchableQuoteWord.french("passerez");
	SearchableQuoteWord wordInVadorFirstEnglishQuote = SearchableQuoteWord.english("faith");
	SearchableQuoteWord wordInVadorSecondEnglishQuote = SearchableQuoteWord.english("deal");
	SearchableQuoteWord wordInVadorThirdEnglishQuote = SearchableQuoteWord.english("power");
	List<SearchableQuoteWord> allWords =
			asList(wordInGandalfEnglishQuote, wordInGandalfFrenchQuote, wordInVadorFirstEnglishQuote,
					wordInVadorSecondEnglishQuote, wordInVadorThirdEnglishQuote);

	String anotherCollection = "anotherCollection";
	TestsSchemasSetup anotherCollectionSetup = new TestsSchemasSetup(anotherCollection);
	ZeSchemaMetadatas anotherCollectionSchema = anotherCollectionSetup.new ZeSchemaMetadatas();
	TestsSchemasSetup zeCollectionSetup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeCollectionSchema = zeCollectionSetup.new ZeSchemaMetadatas();
	RecordServices recordServices;
	ContentManager contentManager;
	SolrClient solrServer;
	Users users = new Users();

	String zeCollectionRecordWithEnglishTitle = "zeCollectionRecord";
	String zeCollectionRecordWithFrenchTitle = "zeCollectionRecordWithFrenchTitle";
	String zeCollectionRecordWithEnglishContent = "zeCollectionRecordWithEnglishContent";
	String zeCollectionRecordWithFrenchContent = "zeCollectionRecordWithFrenchContent";
	String anotherCollectionRecordWithFrenchTitle = "anotherCollectionRecordWithFrenchTitle";
	String anotherCollectionRecordWithEnglishAndFrenchContent = "anotherCollectionRecordWithEnglishAndFrenchContent";

	String zeCollectionRecordWithFrenchAndEnglishValuesInCustomTextMetadata = "zeCollectionRecordWithValuesInCustomTextMetadata";
	String anotherCollectionRecordWithFrenchAndEnglishValuesInCustomTextMetadata = "anotherCollectionRecordWithValuesInCustomTextMetadata";

	String record1 = "record1";
	String record2 = "record2";

	User aliceInZeCollection;
	User aliceInAnotherCollection;

	@Before
	public void setUp()
			throws Exception {

		recordServices = getModelLayerFactory().newRecordServices();
		contentManager = getModelLayerFactory().getContentManager();

		solrServer = ((DataLayerFactory) getDataLayerFactory()).getRecordsVaultServer().getNestedSolrServer();

	}

	//@Test
	public void whenSearchingFrenchDocuments()
			throws Exception {
		givenZeCollectionMultilingualAndAnotherCollectionSingleLingualAndUnsearchableContentAndMultivalueStringMetadataMetadatas();

		//TODO Ajouter un accent dans Quebec afin de tester ce cas
		recordServices.add(new TestRecord(zeCollectionSchema)
				.set(Schemas.TITLE, "l'appartenance au Quebec chez I.B.M."));
		assertThat(resultsIdsOf(paramsWithQ("search_txt_fr:appartenances"))).hasSize(1);
		assertThat(resultsIdsOf(paramsWithQ("search_txt_fr:quebecc"))).hasSize(1);
		assertThat(resultsIdsOf(paramsWithQ("search_txt_fr:au"))).hasSize(0);

	}

	//@Test
	public void whenSearchingEnglishDocuments()
			throws Exception {
		givenZeCollectionMultilingualAndAnotherCollectionSingleLingualAndUnsearchableContentAndMultivalueStringMetadataMetadatas();

		recordServices.add(new TestRecord(zeCollectionSchema)
				.set(Schemas.TITLE, "The greatest search engines"));
		assertThat(resultsIdsOf(paramsWithQ("search_txt_en:greatest"))).hasSize(1);
		assertThat(resultsIdsOf(paramsWithQ("search_txt_en:great"))).hasSize(0);
		assertThat(resultsIdsOf(paramsWithQ("search_txt_en:engine"))).hasSize(1);
		assertThat(resultsIdsOf(paramsWithQ("search_txt_en:search"))).hasSize(1);
		assertThat(resultsIdsOf(paramsWithQ("search_txt_en:the"))).hasSize(0);

	}

	//@Test
	public void givenAMultilingualCollectionAndASinglelingualwithUnsearchableMetadatasThenNotSearchable()
			throws Exception {

		givenZeCollectionMultilingualAndAnotherCollectionSingleLingualAndUnsearchableContentAndMultivalueStringMetadataMetadatas();
		givenSomeRecords();

		whenSearchingInMetadataFieldUsingWildcardsThenFindRecords();

		whenSearchingInTitleMetadataAnalzedFieldUsingAWordInFrenchAndEnglishValuesThenOnlySearchInSpecifiedLanguage();
		whenSearchingInTitleMetadataAnalyzedFieldInMultipleCollectionsThenReturnRecordsOfAllCollections();

		whenSearchingInAllFieldsUsingAWordInFrenchAndEnglishValuesThenOnlySearchInSpecifiedLanguage();
		whenSearchingInAllFieldsInMultipleCollectionsThenReturnRecordsOfAllCollections();

		assertThat(resultsIdsOf(paramsWithQ("stringMetadata_t_en:*"))).isEmpty();
		assertThat(resultsIdsOf(paramsWithQ("stringMetadata_t_fr:*"))).isEmpty();
		assertThat(resultsIdsOf(paramsWithQ("stringMetadata_txt_en:*"))).isEmpty();
		assertThat(resultsIdsOf(paramsWithQ("stringMetadata_txt_fr:*"))).isEmpty();

		assertThat(resultsIdsOf(paramsWithQ("contentMetadata_txt_en:*"))).isEmpty();
		assertThat(resultsIdsOf(paramsWithQ("contentMetadata_txt_fr:*"))).isEmpty();
		assertThat(resultsIdsOf(paramsWithQ("contentListMetadata_txt_en:*"))).isEmpty();
		assertThat(resultsIdsOf(paramsWithQ("contentListMetadata_txt_fr:*"))).isEmpty();
	}

	//@Test
	public void givenAMultilingualCollectionAndASinglelingual_run1()
			throws Exception {

		givenAMultilingualCollectionAndASinglelingual();

	}

	//@Test
	public void givenAMultilingualCollectionAndASinglelingual_run2()
			throws Exception {

		givenAMultilingualCollectionAndASinglelingual();

	}

	//@Test
	public void givenAMultilingualCollectionAndASinglelingual_run3()
			throws Exception {

		givenAMultilingualCollectionAndASinglelingual();

	}

	//@Test
	public void givenAMultilingualCollectionAndASinglelingual_run4()
			throws Exception {

		givenAMultilingualCollectionAndASinglelingual();

	}

	//@Test
	public void givenAMultilingualCollectionAndASinglelingual_run5()
			throws Exception {

		givenAMultilingualCollectionAndASinglelingual();

	}

	private void givenAMultilingualCollectionAndASinglelingual()
			throws Exception {

		givenZeCollectionMultilingualAndAnotherCollectionSingleLingualAndSearchableContentAndMultivalueStringMetadataMetadatas();
		givenSomeRecords();

		whenSearchingInMetadataFieldUsingWildcardsThenFindRecords();

		whenSearchingInTitleMetadataAnalzedFieldUsingAWordInFrenchAndEnglishValuesThenOnlySearchInSpecifiedLanguage();
		whenSearchingInTitleMetadataAnalyzedFieldInMultipleCollectionsThenReturnRecordsOfAllCollections();

		whenSearchingInAllFieldsUsingAWordInFrenchAndEnglishValuesThenOnlySearchInSpecifiedLanguage();
		whenSearchingInAllFieldsInMultipleCollectionsThenReturnRecordsOfAllCollections();

		whenSearchingInContentMetadataAnalzedFieldUsingAWordInFrenchAndEnglishValuesThenOnlySearchInSpecifiedLanguage();
		whenSearchingInContentMetadataAnalyzedFieldInMultipleCollectionsThenReturnRecordsOfAllCollections();

		whenAMultivalueContainsValuesOfDifferentLanguagesThenValuesAreSeparated();
		whenLoadingRecordsThenCopyfieldsAreNotLoaded();

		whenMODIFYINGCopiedSingleValueMetadataThenPreviousValueIsNotInAnyField();
		whenMODIFYINGCopiedMultivalueMetadataThenPreviousValueIsNotInAnyField();

	}

	//@Test
	public void givenMultivalueContentListWhenUpdatingContentThenNewContentSearchableAndPreviousIsNot()
			throws Exception {

		givenBothCollectionBilingualAndSearchableContentAndMultivalueStringMetadataMetadatas();

		Content content1 = contentManager.createMajor(aliceInAnotherCollection, "gandalf.docx", textContent(gangalfFrenchQuote));
		Content content2 = contentManager
				.createMajor(aliceInAnotherCollection, "darth.docx", textContent(vadorFirstEnglishQuote));
		Record record = new TestRecord(anotherCollectionSchema).set(anotherCollectionSchema.contentListMetadata(),
				asList(content1, content2));
		recordServices.add(record);
		assertThatOnlyFoundQuoteWordsAre(wordInGandalfFrenchQuote, wordInVadorFirstEnglishQuote);
		assertThatOnlyFoundFilenamesAre(gandalfDocxFr, darthDocx);

		content1 = (Content) record.getList(anotherCollectionSchema.contentListMetadata()).get(0);
		content2 = (Content) record.getList(anotherCollectionSchema.contentListMetadata()).get(1);
		content1.updateContent(aliceInAnotherCollection, textContent(gangalfEnglishQuote), false);
		content2.updateContentWithName(aliceInAnotherCollection, textContent(vadorSecondEnglishQuote), false, "vador.docx");
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInGandalfEnglishQuote, wordInVadorSecondEnglishQuote);
		assertThatOnlyFoundFilenamesAre(gandalfDocxEn, vadorDocx);
	}

	//@Test
	public void givenMultivalueContentListWhenModifyingCheckedOutContentThenCheckedOutcontentNotSearchable()
			throws Exception {

		givenBothCollectionBilingualAndSearchableContentAndMultivalueStringMetadataMetadatas();

		Content content1 = contentManager.createMajor(aliceInAnotherCollection, "gandalf.docx", textContent(gangalfFrenchQuote));
		Content content2 = contentManager
				.createMajor(aliceInAnotherCollection, "darth.docx", textContent(vadorFirstEnglishQuote));
		Record record = new TestRecord(anotherCollectionSchema).set(anotherCollectionSchema.contentListMetadata(),
				asList(content1, content2));
		recordServices.add(record);
		assertThatOnlyFoundQuoteWordsAre(wordInGandalfFrenchQuote, wordInVadorFirstEnglishQuote);

		content1 = (Content) record.getList(anotherCollectionSchema.contentListMetadata()).get(0);
		content2 = (Content) record.getList(anotherCollectionSchema.contentListMetadata()).get(1);
		content1.checkOut(aliceInAnotherCollection).updateCheckedOutContent(textContent(gangalfEnglishQuote));
		content2.checkOut(aliceInAnotherCollection).updateCheckedOutContent(textContent(vadorSecondEnglishQuote));
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInGandalfFrenchQuote, wordInVadorFirstEnglishQuote);
	}

	//@Test
	public void givenMultivalueContentListWhenCheckinInModifiedContentThenSearchable()
			throws Exception {

		givenBothCollectionBilingualAndSearchableContentAndMultivalueStringMetadataMetadatas();

		Content content1 = contentManager.createMajor(aliceInAnotherCollection, "gandalf.docx", textContent(gangalfFrenchQuote));
		Content content2 = contentManager
				.createMajor(aliceInAnotherCollection, "darth.docx", textContent(vadorFirstEnglishQuote));
		Record record = new TestRecord(anotherCollectionSchema).set(anotherCollectionSchema.contentListMetadata(),
				asList(content1, content2));
		recordServices.add(record);
		assertThatOnlyFoundQuoteWordsAre(wordInGandalfFrenchQuote, wordInVadorFirstEnglishQuote);

		content1 = (Content) record.getList(anotherCollectionSchema.contentListMetadata()).get(0);
		content2 = (Content) record.getList(anotherCollectionSchema.contentListMetadata()).get(1);
		content1.checkOut(aliceInAnotherCollection);
		content2.checkOut(aliceInAnotherCollection);
		recordServices.update(record);

		content1 = (Content) record.getList(anotherCollectionSchema.contentListMetadata()).get(0);
		content2 = (Content) record.getList(anotherCollectionSchema.contentListMetadata()).get(1);
		content1.checkInWithModification(textContent(gangalfEnglishQuote), true);
		content2.checkInWithModification(textContent(vadorSecondEnglishQuote), false);
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInGandalfEnglishQuote, wordInVadorSecondEnglishQuote);
	}

	//@Test
	public void givenMultivalueContentListWhenReplacingContentsThenNewSearchableAndRemovedNotSearchable()
			throws Exception {

		givenBothCollectionBilingualAndSearchableContentAndMultivalueStringMetadataMetadatas();

		Content gandalfFr = contentManager
				.createMajor(aliceInAnotherCollection, "gandalf.docx", textContent(gangalfFrenchQuote));
		Content darth1 = contentManager.createMajor(aliceInAnotherCollection, "darth.docx", textContent(vadorFirstEnglishQuote));
		Content darth2 = contentManager
				.createMajor(aliceInAnotherCollection, "vador.docx", textContent(vadorSecondEnglishQuote));
		Record record = new TestRecord(anotherCollectionSchema).set(anotherCollectionSchema.contentListMetadata(),
				asList(gandalfFr, darth1));
		recordServices.add(record);
		assertThatOnlyFoundQuoteWordsAre(wordInGandalfFrenchQuote, wordInVadorFirstEnglishQuote);
		assertThatOnlyFoundFilenamesAre(gandalfDocxFr, darthDocx);

		gandalfFr = (Content) record.getList(anotherCollectionSchema.contentListMetadata()).get(0);
		record.set(anotherCollectionSchema.contentListMetadata(), asList(gandalfFr, darth2));
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInGandalfFrenchQuote, wordInVadorSecondEnglishQuote);
		assertThatOnlyFoundFilenamesAre(gandalfDocxFr, vadorDocx);
	}

	//@Test
	public void givenSinglevalueContentWhenUpdatingContentThenNewContentSearchableAndPreviousIsNot()
			throws Exception {

		givenBothCollectionBilingualAndSearchableContentAndMultivalueStringMetadataMetadatas();

		Content content = contentManager.createMajor(aliceInZeCollection, "gandalf.docx", textContent(gangalfFrenchQuote));
		Record record = new TestRecord(zeCollectionSchema).set(zeCollectionSchema.contentMetadata(), content);
		recordServices.add(record);
		assertThatOnlyFoundQuoteWordsAre(wordInGandalfFrenchQuote);
		assertThatOnlyFoundFilenamesAre(gandalfDocxFr);

		content = record.get(zeCollectionSchema.contentMetadata());
		content.updateContent(aliceInZeCollection, textContent(gangalfEnglishQuote), false);
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInGandalfEnglishQuote);
		assertThatOnlyFoundFilenamesAre(gandalfDocxEn);
	}

	//@Test
	public void givenSinglevalueContentWhenModifyingCheckedOutContentThenCheckedOutcontentNotSearchable()
			throws Exception {

		givenBothCollectionBilingualAndSearchableContentAndMultivalueStringMetadataMetadatas();

		Content content = contentManager.createMajor(aliceInZeCollection, "gandalf.docx", textContent(gangalfFrenchQuote));
		Record record = new TestRecord(zeCollectionSchema).set(zeCollectionSchema.contentMetadata(), content);
		recordServices.add(record);
		assertThatOnlyFoundQuoteWordsAre(wordInGandalfFrenchQuote);
		assertThatOnlyFoundFilenamesAre(gandalfDocxFr);

		content = record.get(zeCollectionSchema.contentMetadata());
		content.checkOut(aliceInZeCollection);
		recordServices.update(record);

		content = record.get(zeCollectionSchema.contentMetadata());
		content.updateCheckedOutContent(textContent(gangalfEnglishQuote));
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInGandalfFrenchQuote);
		assertThatOnlyFoundFilenamesAre(gandalfDocxFr);

		content = record.get(zeCollectionSchema.contentMetadata());
		content.checkIn();
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInGandalfEnglishQuote);
		assertThatOnlyFoundFilenamesAre(gandalfDocxEn);
	}

	//@Test
	public void givenSinglevalueContentWhenCheckinInModifiedContentThenSearchable()
			throws Exception {

		givenBothCollectionBilingualAndSearchableContentAndMultivalueStringMetadataMetadatas();

		Content content = contentManager.createMajor(aliceInZeCollection, "darth.docx", textContent(vadorFirstEnglishQuote));
		Record record = new TestRecord(zeCollectionSchema).set(zeCollectionSchema.contentMetadata(), content);
		recordServices.add(record);
		assertThatOnlyFoundQuoteWordsAre(wordInVadorFirstEnglishQuote);
		assertThatOnlyFoundFilenamesAre(darthDocx);

		content = record.get(zeCollectionSchema.contentMetadata());
		content.checkOut(aliceInZeCollection);
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInVadorFirstEnglishQuote);
		assertThatOnlyFoundFilenamesAre(darthDocx);

		content = record.get(zeCollectionSchema.contentMetadata());
		content.updateCheckedOutContent(textContent(vadorSecondEnglishQuote));
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInVadorFirstEnglishQuote);
		assertThatOnlyFoundFilenamesAre(darthDocx);

		content = record.get(zeCollectionSchema.contentMetadata());
		content.checkIn();
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInVadorSecondEnglishQuote);
		assertThatOnlyFoundFilenamesAre(darthDocx);
	}

	//@Test
	public void givenSinglevalueContentWhenReplacingContentsThenNewSearchableAndRemovedNotSearchable()
			throws Exception {

		givenBothCollectionBilingualAndSearchableContentAndMultivalueStringMetadataMetadatas();

		Content gandalfFr = contentManager.createMajor(aliceInZeCollection, "gandalf.docx", textContent(gangalfFrenchQuote));
		Content darth1 = contentManager.createMajor(aliceInZeCollection, "darth.docx", textContent(vadorFirstEnglishQuote));
		Record record = new TestRecord(zeCollectionSchema).set(zeCollectionSchema.contentMetadata(), gandalfFr);
		recordServices.add(record);
		assertThatOnlyFoundQuoteWordsAre(wordInGandalfFrenchQuote);
		assertThatOnlyFoundFilenamesAre(gandalfDocxFr);

		record.set(zeCollectionSchema.contentMetadata(), darth1);
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInVadorFirstEnglishQuote);
		assertThatOnlyFoundFilenamesAre(darthDocx);
	}

	private void whenMODIFYINGCopiedMultivalueMetadataThenPreviousValueIsNotInAnyField()
			throws SolrServerException, RecordServicesException, IOException {
		assertThat(resultsIdsOf(paramsWithQ("search_txt_fr:Roger"))).hasSize(1);
		assertThat(resultsIdsOf(paramsWithQ("search_txt_en:legend"))).hasSize(1);
		assertThat(resultsIdsOf(paramsWithQ("search_txt_fr:mysterious"))).hasSize(0);
		assertThat(resultsIdsOf(paramsWithQ("search_txt_en:mysterious"))).hasSize(0);

		Record record = recordServices.getDocumentById(zeCollectionRecordWithFrenchAndEnglishValuesInCustomTextMetadata);
		record.set(zeCollectionSchema.stringMetadata(), asList("The mysterious story of M. Lechat"));
		recordServices.add(record);

		assertThat(resultsIdsOf(paramsWithQ("search_txt_fr:Roger"))).hasSize(0);
		assertThat(resultsIdsOf(paramsWithQ("search_txt_en:legend"))).hasSize(0);
		assertThat(resultsIdsOf(paramsWithQ("search_txt_fr:mysterious"))).hasSize(0);
		assertThat(resultsIdsOf(paramsWithQ("search_txt_en:mysterious"))).hasSize(1);
	}

	private void whenMODIFYINGCopiedSingleValueMetadataThenPreviousValueIsNotInAnyField()
			throws SolrServerException, RecordServicesException, IOException {
		assertThat(resultsIdsOf(paramsWithQ("title_t_fr:sandy"))).hasSize(1);
		assertThat(resultsIdsOf(paramsWithQ("title_t_fr:Java"))).hasSize(0);
		assertThat(resultsIdsOf(paramsWithQ("title_t_en:Java"))).hasSize(0);

		Record record = recordServices.getDocumentById(zeCollectionRecordWithFrenchTitle);
		record.set(Schemas.TITLE, "Java is greatest language");
		recordServices.add(record);

		assertThat(resultsIdsOf(paramsWithQ("title_t_fr:sandy"))).hasSize(0);
		assertThat(resultsIdsOf(paramsWithQ("title_t_fr:Java"))).hasSize(0);
		assertThat(resultsIdsOf(paramsWithQ("title_t_en:Java"))).hasSize(1);
	}

	private void whenLoadingRecordsThenCopyfieldsAreNotLoaded() {
		RecordImpl record = (RecordImpl) recordServices
				.getDocumentById(zeCollectionRecordWithFrenchAndEnglishValuesInCustomTextMetadata);
		assertThat(record.getRecordDTO().getCopyFields()).isEmpty();
		assertThat(record.getRecordDTO().getFields()).doesNotContainKey("stringMetadata_txt_en")
				.doesNotContainKey("stringMetadata_txt_fr");
	}

	private void whenAMultivalueContainsValuesOfDifferentLanguagesThenValuesAreSeparated()
			throws SolrServerException, IOException {

		assertThat(resultsIdsOf(paramsWithQ("stringMetadata_txt_fr:Roger")))
				.containsOnly(zeCollectionRecordWithFrenchAndEnglishValuesInCustomTextMetadata);

		assertThat(resultsIdsOf(paramsWithQ("stringMetadata_txt_en:Roger"))).isEmpty();

		assertThat(resultsIdsOf(paramsWithQ("stringMetadata_txt_fr:legend"))).isEmpty();

		assertThat(resultsIdsOf(paramsWithQ("stringMetadata_txt_en:legend")))
				.containsOnly(zeCollectionRecordWithFrenchAndEnglishValuesInCustomTextMetadata);

		assertThat(resultsIdsOf(paramsWithQ("stringMetadata_txt_fr:blanc")))
				.containsOnly(anotherCollectionRecordWithFrenchAndEnglishValuesInCustomTextMetadata);

		assertThat(resultsIdsOf(paramsWithQ("stringMetadata_txt_en:blanc"))).isEmpty();

		assertThat(resultsIdsOf(paramsWithQ("stringMetadata_txt_fr:white")))
				.containsOnly(anotherCollectionRecordWithFrenchAndEnglishValuesInCustomTextMetadata);

		assertThat(resultsIdsOf(paramsWithQ("stringMetadata_txt_en:white"))).isEmpty();
	}

	private void whenSearchingInMetadataFieldUsingWildcardsThenFindRecords()
			throws Exception {

		assertThat(resultsIdsOf(paramsWithQ("title_s:*Sandy*")))
				.containsOnly(zeCollectionRecordWithEnglishTitle, zeCollectionRecordWithFrenchTitle);

		assertThat(resultsIdsOf(paramsWithQ("title_s:Sandy"))).isEmpty();
	}

	private void whenSearchingInTitleMetadataAnalzedFieldUsingAWordInFrenchAndEnglishValuesThenOnlySearchInSpecifiedLanguage()
			throws Exception {

		System.out.println("title_t_fr:sandy -> " + resultsIdsOf(paramsWithQ("title_t_fr:sandy")));
		System.out.println("title_t_en:sandy -> " + resultsIdsOf(paramsWithQ("title_t_en:sandy")));

		assertThat(resultsIdsOf(paramsWithQ("title_t_fr:sandy"))).containsOnly(zeCollectionRecordWithFrenchTitle);

		assertThat(resultsIdsOf(paramsWithQ("title_t_en:sandy"))).containsOnly(zeCollectionRecordWithEnglishTitle);

	}

	private void whenSearchingInTitleMetadataAnalyzedFieldInMultipleCollectionsThenReturnRecordsOfAllCollections()
			throws Exception {

		assertThat(resultsIdsOf(paramsWithQ("(title_t_en:kick OR bob_s:impossible)")))
				.containsOnly(zeCollectionRecordWithEnglishTitle);

		assertThat(resultsIdsOf(paramsWithQ("(title_t_fr:kick OR bob_s:impossible)")))
				.isEmpty();

		assertThat(resultsIdsOf(paramsWithQ("(title_t_fr:botte OR title_t_fr:intimidation)")))
				.containsOnly(zeCollectionRecordWithFrenchTitle, anotherCollectionRecordWithFrenchTitle);

		assertThat(resultsIdsOf(paramsWithQ("(title_t_en:botte OR title_t_en:intimidation)"))).isEmpty();
	}

	private void whenSearchingInAllFieldsUsingAWordInFrenchAndEnglishValuesThenOnlySearchInSpecifiedLanguage()
			throws Exception {

		assertThat(resultsIdsOf(paramsWithQ("search_txt_fr:sandy"))).containsOnly(zeCollectionRecordWithFrenchTitle);

		assertThat(resultsIdsOf(paramsWithQ("search_txt_en:sandy"))).containsOnly(zeCollectionRecordWithEnglishTitle);

	}

	private void whenSearchingInAllFieldsInMultipleCollectionsThenReturnRecordsOfAllCollections()
			throws Exception {

		assertThat(resultsIdsOf(paramsWithQ("(search_txt_en:kick OR bob_s:impossible)")))
				.containsOnly(zeCollectionRecordWithEnglishTitle);

		assertThat(resultsIdsOf(paramsWithQ("(search_txt_fr:kick OR bob_s:impossible)"))).isEmpty();

		assertThat(resultsIdsOf(paramsWithQ("(search_txt_fr:botte OR search_txt_fr:intimidation)")))
				.containsOnly(zeCollectionRecordWithFrenchTitle, anotherCollectionRecordWithFrenchTitle);

		assertThat(resultsIdsOf(paramsWithQ("(search_txt_en:botte OR search_txt_en:intimidation)"))).isEmpty();
	}

	private void whenSearchingInContentMetadataAnalzedFieldUsingAWordInFrenchAndEnglishValuesThenOnlySearchInSpecifiedLanguage()
			throws Exception {

		assertThat(resultsIdsOf(paramsWithQ("contentMetadata_txt_fr:oeufs"))).containsOnly(zeCollectionRecordWithFrenchContent);

		assertThat(resultsIdsOf(paramsWithQ("contentMetadata_txt_en:eggs"))).containsOnly(zeCollectionRecordWithEnglishContent);

	}

	private void whenSearchingInContentMetadataAnalyzedFieldInMultipleCollectionsThenReturnRecordsOfAllCollections()
			throws Exception {

		assertThat(resultsIdsOf(paramsWithQ("(contentMetadata_txt_en:eggs OR contentListMetadata_txt_en:eggs)")))
				.containsOnly(zeCollectionRecordWithEnglishContent);

		assertThat(resultsIdsOf(paramsWithQ("(contentMetadata_txt_fr:oeufs OR contentListMetadata_txt_fr:oeufs)")))
				.containsOnly(zeCollectionRecordWithFrenchContent, anotherCollectionRecordWithEnglishAndFrenchContent);

	}

	// ----------------------------------------------------------------------------------

	private void givenZeCollectionMultilingualAndAnotherCollectionSingleLingualAndUnsearchableContentAndMultivalueStringMetadataMetadatas()
			throws Exception {

		givenSpecialCollection(zeCollection, asList("fr", "en"));
		givenSpecialCollection(anotherCollection, asList("fr"));
		setupUsers();
		aliceInZeCollection = users.aliceIn(zeCollection);
		aliceInAnotherCollection = users.aliceIn(anotherCollection);

		defineSchemasManager().using(zeCollectionSetup.withAStringMetadata(whichIsMultivalue).withAContentMetadata());
		defineSchemasManager().using(anotherCollectionSetup.withAStringMetadata(whichIsMultivalue).withAContentListMetadata());
	}

	private void givenZeCollectionMultilingualAndAnotherCollectionSingleLingualAndSearchableContentAndMultivalueStringMetadataMetadatas()
			throws Exception {

		givenSpecialCollection(zeCollection, asList("fr", "en"));
		givenSpecialCollection(anotherCollection, asList("fr"));
		setupUsers();
		aliceInZeCollection = users.aliceIn(zeCollection);
		aliceInAnotherCollection = users.aliceIn(anotherCollection);

		defineSchemasManager().using(
				zeCollectionSetup.withAStringMetadata(whichIsMultivalue, whichIsSearchable)
						.withAContentMetadata(whichIsSearchable));
		defineSchemasManager().using(anotherCollectionSetup.withAStringMetadata(whichIsMultivalue, whichIsSearchable)
				.withAContentListMetadata(whichIsSearchable));
	}

	private void givenBothCollectionBilingualAndSearchableContentAndMultivalueStringMetadataMetadatas()
			throws Exception {

		prepareSystem(withZeCollection(), withCollection(anotherCollection));

		givenSpecialCollection(zeCollection, asList("fr", "en"));
		givenSpecialCollection(anotherCollection, asList("fr", "en"));
		setupUsers();
		aliceInZeCollection = users.aliceIn(zeCollection);
		aliceInAnotherCollection = users.aliceIn(anotherCollection);

		defineSchemasManager().using(
				zeCollectionSetup.withAStringMetadata(whichIsMultivalue, whichIsSearchable)
						.withAContentMetadata(whichIsSearchable));
		defineSchemasManager().using(anotherCollectionSetup.withALargeTextMetadata(whichIsMultivalue, whichIsSearchable)
				.withAContentListMetadata(whichIsSearchable));
	}

	private void givenSomeRecords()
			throws IOException, RecordServicesException {

		recordServices.add(new TestRecord(zeCollectionSchema, zeCollectionRecordWithEnglishTitle)
				.set(Schemas.TITLE, "Kick the ball Sandy. All right Tom."));

		recordServices.add(new TestRecord(zeCollectionSchema, zeCollectionRecordWithFrenchTitle)
				.set(Schemas.TITLE, "Botte la balle Sandy. Parfait Tom"));

		recordServices.add(new TestRecord(anotherCollectionSchema, anotherCollectionRecordWithFrenchTitle)
				.set(Schemas.TITLE, "Arr�tez ces insultes, ceci est de l'intimidation"));

		recordServices.add(new TestRecord(zeCollectionSchema, zeCollectionRecordWithEnglishContent)
				.set(zeCollectionSchema.contentMetadata(), newContent("Exercice.docx", "zeCollectionEnContent.docx")));

		recordServices.add(new TestRecord(zeCollectionSchema, zeCollectionRecordWithFrenchContent)
				.set(zeCollectionSchema.contentMetadata(), newContent("Exercice.docx", "zeCollectionFrContent.docx")));

		Content content1 = newContent("Travail.docx", "anotherCollectionEnContent.docx");
		Content content2 = newContent("Travail.docx", "anotherCollectionFrContent.docx");
		recordServices.add(new TestRecord(anotherCollectionSchema, anotherCollectionRecordWithEnglishAndFrenchContent)
				.set(anotherCollectionSchema.contentListMetadata(), asList(content1, content2)));

		recordServices.add(new TestRecord(zeCollectionSchema, zeCollectionRecordWithFrenchAndEnglishValuesInCustomTextMetadata)
				.set(zeCollectionSchema.stringMetadata(),
						asList("L'indien Roger est un grand parmi les grands", "The legend of Bob")));

		recordServices.add(new TestRecord(anotherCollectionSchema,
				anotherCollectionRecordWithFrenchAndEnglishValuesInCustomTextMetadata)
				.set(anotherCollectionSchema.stringMetadata(), asList("Je suis Gandalf le blanc", "Gandalf the white")));

	}

	private Content newContent(String fileName, String resource)
			throws IOException {
		StreamFactory<InputStream> factory = getTestResourceInputStreamFactory(resource);
		ContentVersionDataSummary dataSummary = contentManager.upload(factory.create(SDK_STREAM));
		return contentManager.createMajor(users.aliceIn(zeCollection), fileName, dataSummary);
	}

	private ContentVersionDataSummary textContent(String text) {
		Reader reader = new StringReader(text);
		InputStream inputStream = new ReaderInputStream(reader);
		ContentVersionDataSummary contentVersionDataSummary = contentManager.upload(inputStream);
		return contentVersionDataSummary;

	}

	private void setupUsers()
			throws RecordServicesException {
		UserServices userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices, zeCollection);
		userServices.execute(users.alice().getUsername(), (req) -> req.addToCollection(zeCollection));
		userServices.execute(users.alice().getUsername(), (req) -> req.addToCollection(zeCollection));
		userServices.execute(users.bob().getUsername(), (req) -> req.addToCollection(zeCollection));
		userServices.execute(users.bob().getUsername(), (req) -> req.addToCollection(zeCollection));
		userServices.execute(users.dakotaLIndien().getUsername(), (req) -> req.addToCollection(zeCollection));

		Transaction transaction = new Transaction();
		transaction.add(users.aliceIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
		transaction.add(users.bobIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
		transaction.add(users.dakotaLIndienIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
		recordServices.execute(transaction);
		transaction = new Transaction();
		transaction.add(users.aliceIn(anotherCollection).setCollectionReadAccess(true).getWrappedRecord());
		transaction.add(users.bobIn(anotherCollection).setCollectionReadAccess(true).getWrappedRecord());
		recordServices.execute(transaction);
	}

	private void assertThatOnlyFoundFilenamesAre(SearchableQuoteWord... filenames)
			throws SolrServerException, IOException {
		List<SearchableQuoteWord> expectedFilenamesFound = asList(filenames);
		for (SearchableQuoteWord aSearchableQuoteWord : allFilenames) {
			String word = aSearchableQuoteWord.word;
			int expectedSize = expectedFilenamesFound.contains(aSearchableQuoteWord) ? 1 : 0;
			if (aSearchableQuoteWord.english) {
				assertThat(resultsIdsOf(paramsWithQ(englishSearchField + ":" + word))).describedAs(word).hasSize(expectedSize);
			} else {
				assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + word))).describedAs(word).hasSize(expectedSize);
			}
		}
	}

	private void assertThatOnlyFoundQuoteWordsAre(SearchableQuoteWord... words)
			throws SolrServerException, IOException {
		List<SearchableQuoteWord> expectedFoundWords = asList(words);
		for (SearchableQuoteWord aSearchableQuoteWord : allWords) {
			String word = aSearchableQuoteWord.word;
			if (expectedFoundWords.contains(aSearchableQuoteWord)) {
				if (aSearchableQuoteWord.english) {
					assertThat(resultsIdsOf(paramsWithQ(englishSearchField + ":" + word))).describedAs(word).hasSize(1);
					assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + word))).describedAs(word).isEmpty();
				} else {
					assertThat(resultsIdsOf(paramsWithQ(englishSearchField + ":" + word))).describedAs(word).isEmpty();
					assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + word))).describedAs(word).hasSize(1);
				}
			} else {
				assertThat(resultsIdsOf(paramsWithQ(englishSearchField + ":" + word))).describedAs(word).isEmpty();
				assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + word))).describedAs(word).isEmpty();
			}
		}
	}

	private ModifiableSolrParams paramsWithQ(String q) {
		return new ModifiableSolrParams().set("q", q);
	}

	private List<String> resultsIdsOf(SolrParams params)
			throws SolrServerException, IOException {
		List<String> ids = new ArrayList<>();
		for (SolrDocument document : solrServer.query(params).getResults()) {
			ids.add((String) document.getFieldValue("id"));
		}
		return ids;
	}

	private static class SearchableQuoteWord {
		boolean english;
		String word;

		private static SearchableQuoteWord english(String word) {
			SearchableQuoteWord searchableQuoteWord = new SearchableQuoteWord();
			searchableQuoteWord.english = true;
			searchableQuoteWord.word = word;
			return searchableQuoteWord;
		}

		private static SearchableQuoteWord french(String word) {
			SearchableQuoteWord searchableQuoteWord = new SearchableQuoteWord();
			searchableQuoteWord.english = false;
			searchableQuoteWord.word = word;
			return searchableQuoteWord;
		}
	}
}
