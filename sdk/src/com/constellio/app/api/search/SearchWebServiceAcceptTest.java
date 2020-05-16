package com.constellio.app.api.search;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.thesaurus.ThesaurusService;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

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

// Confirm @SlowTest
public class SearchWebServiceAcceptTest extends ConstellioTest {

	String englishSearchField = "search_txt_en";
	String frenchSearchField = "search_txt_fr";

	String quote1 = "Vous ne passerez pas!";
	String quote2 = "Votre manque de foi me consterne";
	String quote3 = "Le S'Quatre novembre au soir";
	String quote4 = "Je voudrais voudrais vous dire";

	SearchableQuoteWord darthDocx = SearchableQuoteWord.french("darth");
	SearchableQuoteWord vadorDocx = SearchableQuoteWord.french("vador");
	SearchableQuoteWord gandalfDocx = SearchableQuoteWord.french("gandalf");
	List<SearchableQuoteWord> allFilenames = asList(darthDocx, gandalfDocx);

	SearchableQuoteWord wordInQuote1 = SearchableQuoteWord.french("passerez");
	SearchableQuoteWord wordInQuote2 = SearchableQuoteWord.french("consterne");
	SearchableQuoteWord wordInQuote3 = SearchableQuoteWord.french("novembre");
	SearchableQuoteWord wordInQuote4 = SearchableQuoteWord.french("voudrais");

	List<SearchableQuoteWord> allWords = asList(wordInQuote1, wordInQuote2, wordInQuote3, wordInQuote4);

	TestsSchemasSetup zeCollectionSetup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeCollectionSchema = zeCollectionSetup.new ZeSchemaMetadatas();
	RecordServices recordServices;
	ContentManager contentManager;
	Users users = new Users();
	SolrClient solrServer;

	User aliceInZeCollection;

	public static final String SKOS_XML_FILE_PATH = "SKOS destination 21 juillet 2017.xml";

	@Before
	public void setUp()
			throws Exception {

	}

	@Test
	public void givenAMultilingualCollectionAndASinglelingualwithUnsearchableMetadatasThenNotSearchable()
			throws Exception {

		givenFrenchCollectionWithUnSearchableMetadatas();

		Content content1 = contentManager.createMinor(aliceInZeCollection, "gandalf.docx", textContent(quote1));
		Content content2 = contentManager.createMajor(aliceInZeCollection, "darth.docx", textContent(quote2));
		Record record = new TestRecord(zeCollectionSchema)
				.set(zeCollectionSchema.contentListMetadata(), asList(content1))
				.set(zeCollectionSchema.contentMetadata(), content2)
				.set(Schemas.TITLE, quote3)
				.set(zeCollectionSchema.stringMetadata(), asList(quote3))
				.set(zeCollectionSchema.largeTextMetadata(), quote3)
				.set(zeCollectionSchema.multivaluedLargeTextMetadata(), asList(quote3));

		recordServices.add(record);

		assertThatNoQuotesWordsCanBeFound();
		assertThatNoFileNamesCanBeFound();

		recordServices.update(record.set(Schemas.TITLE, quote1));
		assertThatNoQuotesWordsCanBeFound();
		assertThatNoFileNamesCanBeFound();
	}

	@Test
	public void givenContentListWhenUpdatingContentThenNewContentSearchableAndPreviousIsNot()
			throws Exception {

		givenFrenchCollectionWithSearchableMetadatas();

		Content content1 = contentManager.createMinor(aliceInZeCollection, "gandalf.docx", textContent(quote1));
		Content content2 = contentManager.createMajor(aliceInZeCollection, "darth.docx", textContent(quote2));
		Record record = new TestRecord(zeCollectionSchema).set(zeCollectionSchema.contentListMetadata(),
				asList(content1, content2));
		recordServices.add(record);
		assertThatOnlyFoundQuoteWordsAre(wordInQuote1, wordInQuote2);
		assertThatOnlyFoundFilenamesAre(gandalfDocx, darthDocx);

		content1 = (Content) record.getList(zeCollectionSchema.contentListMetadata()).get(0);
		content2 = (Content) record.getList(zeCollectionSchema.contentListMetadata()).get(1);
		content1.updateContent(aliceInZeCollection, textContent(quote3), false);
		content2.updateContentWithName(aliceInZeCollection, textContent(quote1), false, "vador.docx");
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInQuote1, wordInQuote3);
		assertThatOnlyFoundFilenamesAre(gandalfDocx, vadorDocx);

		recordServices.update(record.set(Schemas.TITLE, "a new title"));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote1, wordInQuote3);
		assertThatOnlyFoundFilenamesAre(gandalfDocx, vadorDocx);

		recordServices.update(record.set(zeCollectionSchema.contentListMetadata(), new ArrayList<>()));
		assertThatNoQuotesWordsCanBeFound();
		assertThatNoFileNamesCanBeFound();
	}

	@Test
	public void givenContentWhenUpdatingThenNewContentSearchableAndPreviousIsNot()
			throws Exception {

		givenFrenchCollectionWithSearchableMetadatas();

		Content content = contentManager.createMajor(aliceInZeCollection, "gandalf.docx", textContent(quote1));
		Record record = new TestRecord(zeCollectionSchema).set(zeCollectionSchema.contentMetadata(), content);
		recordServices.add(record);
		assertThatOnlyFoundQuoteWordsAre(wordInQuote1);
		assertThatOnlyFoundFilenamesAre(gandalfDocx);

		content = record.get(zeCollectionSchema.contentMetadata());
		content.updateContentWithName(aliceInZeCollection, textContent(quote3), false, "vador.docx");
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInQuote3);
		assertThatOnlyFoundFilenamesAre(vadorDocx);

		Content anotherContent = contentManager.createMajor(aliceInZeCollection, "darth.docx", textContent(quote2));
		record.set(zeCollectionSchema.contentMetadata(), anotherContent);
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInQuote2);
		assertThatOnlyFoundFilenamesAre(darthDocx);

		recordServices.update(record.set(Schemas.TITLE, "a new title"));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote2);
		assertThatOnlyFoundFilenamesAre(darthDocx);

		recordServices.update(record.set(zeCollectionSchema.contentMetadata(), null));

		assertThatNoQuotesWordsCanBeFound();
		assertThatNoFileNamesCanBeFound();
	}

	@Test
	public void givenThesaurusValueIsResponseOk()
			throws Exception {
		assumePluginsSDK();

		Users users = new Users();
		RMTestRecords records = new RMTestRecords(zeCollection);

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
		);

		UserServices userServices = getModelLayerFactory().newUserServices();
		users.using(userServices);

		User systemAdmin = users.adminIn(zeCollection);

		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();
		authenticationService.changePassword(systemAdmin.getUsername(), "youshallnotpass");

		getModelLayerFactory().getThesaurusManager().set(getTestResourceInputStream(SKOS_XML_FILE_PATH), zeCollection);

		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.add("q", "*:*");
		solrParams.add("fq", "collection_s:zeCollection");

		SolrClient solrServer = newSearchClient();

		String serviceKey = userServices.giveNewServiceToken(userServices.getUserCredential(systemAdmin.getUsername()));
		String token = userServices.getToken(serviceKey, systemAdmin.getUsername(), "youshallnotpass");
		solrParams.set("serviceKey", serviceKey);
		solrParams.set("token", token);
		solrParams.set(SearchWebService.THESAURUS_VALUE, "abeile");
		solrServer.query(solrParams);

		getModelLayerFactory().getDataLayerFactory().getEventsVaultServer().flush();

		QueryResponse solrDocumentsList = solrServer.query(solrParams);
		solrDocumentsList.getResponse().getAll(SearchWebService.SKOS_CONCEPTS);
		solrDocumentsList.getResponse().getAll(ThesaurusService.DISAMBIGUATIONS);

	}

	@Test
	public void givenContentListWhenUpdatingCheckedOutContentThenCheckedOutContentSearchableAndPreviousIsNot()
			throws Exception {

		givenFrenchCollectionWithSearchableMetadatas();

		Content content1 = contentManager.createMinor(aliceInZeCollection, "gandalf.docx", textContent(quote1));
		Content content2 = contentManager.createMajor(aliceInZeCollection, "darth.docx", textContent(quote2));
		Record record = new TestRecord(zeCollectionSchema).set(zeCollectionSchema.contentListMetadata(),
				asList(content1, content2));
		recordServices.add(record);
		assertThatOnlyFoundQuoteWordsAre(wordInQuote1, wordInQuote2);
		assertThatOnlyFoundFilenamesAre(gandalfDocx, darthDocx);

		content1 = (Content) record.getList(zeCollectionSchema.contentListMetadata()).get(0);
		content2 = (Content) record.getList(zeCollectionSchema.contentListMetadata()).get(1);
		content1.checkOut(aliceInZeCollection);
		content2.checkOut(aliceInZeCollection).updateCheckedOutContent(textContent(quote3));
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInQuote1, wordInQuote2);
		assertThatOnlyFoundFilenamesAre(gandalfDocx, darthDocx);

		recordServices.update(record.set(Schemas.TITLE, "a new title"));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote1, wordInQuote2);
		assertThatOnlyFoundFilenamesAre(gandalfDocx, darthDocx);

		content1 = (Content) record.getList(zeCollectionSchema.contentListMetadata()).get(0);
		content2 = (Content) record.getList(zeCollectionSchema.contentListMetadata()).get(1);
		content1.checkInWithModification(textContent(quote4), false);
		content2.checkIn();
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInQuote3, wordInQuote4);
		assertThatOnlyFoundFilenamesAre(gandalfDocx, darthDocx);

	}

	@Test
	public void givenContentWhenUpdatingCheckedOutContentThenCheckedOutContentSearchableAndPreviousIsNot()
			throws Exception {

		givenFrenchCollectionWithSearchableMetadatas();

		Content content = contentManager.createMinor(aliceInZeCollection, "gandalf.docx", textContent(quote1));
		Record record = new TestRecord(zeCollectionSchema).set(zeCollectionSchema.contentMetadata(), content);
		recordServices.add(record);
		assertThatOnlyFoundQuoteWordsAre(wordInQuote1);
		assertThatOnlyFoundFilenamesAre(gandalfDocx);

		content = record.get(zeCollectionSchema.contentMetadata());
		content.checkOut(aliceInZeCollection).updateCheckedOutContent(textContent(quote3));
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInQuote1);
		assertThatOnlyFoundFilenamesAre(gandalfDocx);

		recordServices.update(record.set(Schemas.TITLE, "a new title"));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote1);
		assertThatOnlyFoundFilenamesAre(gandalfDocx);

		content = record.get(zeCollectionSchema.contentMetadata());
		content.checkInWithModification(textContent(quote4), false);
		recordServices.update(record);
		assertThatOnlyFoundQuoteWordsAre(wordInQuote4);
		assertThatOnlyFoundFilenamesAre(gandalfDocx);

	}

	@Test
	public void givenStringListWhenAddUpdatingThenSearchable()
			throws Exception {

		givenFrenchCollectionWithSearchableMetadatas();

		Record record = new TestRecord(zeCollectionSchema);

		recordServices.add(record.set(zeCollectionSchema.stringMetadata(), asList(quote1, quote2)));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote1, wordInQuote2);

		recordServices.update(record.set(zeCollectionSchema.stringMetadata(), asList(quote2, quote3)));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote2, wordInQuote3);

		recordServices.update(record.set(Schemas.TITLE, "a new title"));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote2, wordInQuote3);

		recordServices.update(record.set(zeCollectionSchema.stringMetadata(), new ArrayList<>()));
		assertThatNoQuotesWordsCanBeFound();

	}

	@Test
	public void givenStringWhenAddUpdatingThenSearchable()
			throws Exception {

		givenFrenchCollectionWithSearchableMetadatas();

		Record record = new TestRecord(zeCollectionSchema);

		recordServices.add(record.set(Schemas.TITLE, quote1));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote1);

		recordServices.update(record.set(Schemas.TITLE, quote3));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote3);

		recordServices.update(record.set(zeCollectionSchema.stringMetadata(), asList("a new value")));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote3);

		recordServices.update(record.set(Schemas.TITLE, null));
		assertThatNoQuotesWordsCanBeFound();

	}

	@Test
	public void givenTextListWhenAddUpdatingThenSearchable()
			throws Exception {

		givenFrenchCollectionWithSearchableMetadatas();

		Record record = new TestRecord(zeCollectionSchema);

		recordServices.add(record.set(zeCollectionSchema.multivaluedLargeTextMetadata(), asList(quote1, quote2)));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote1, wordInQuote2);

		recordServices.update(record.set(zeCollectionSchema.multivaluedLargeTextMetadata(), asList(quote2, quote3)));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote2, wordInQuote3);

		recordServices.update(record.set(Schemas.TITLE, "a new title"));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote2, wordInQuote3);

		recordServices.update(record.set(zeCollectionSchema.multivaluedLargeTextMetadata(), new ArrayList<>()));
		assertThatNoQuotesWordsCanBeFound();

	}

	@Test
	public void givenTextWhenAddUpdatingThenSearchable()
			throws Exception {

		givenFrenchCollectionWithSearchableMetadatas();

		Record record = new TestRecord(zeCollectionSchema);

		recordServices.add(record.set(zeCollectionSchema.largeTextMetadata(), quote1));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote1);

		recordServices.update(record.set(zeCollectionSchema.largeTextMetadata(), quote3));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote3);

		recordServices.update(record.set(Schemas.TITLE, "a new title"));
		assertThatOnlyFoundQuoteWordsAre(wordInQuote3);

		recordServices.update(record.set(zeCollectionSchema.largeTextMetadata(), null));
		assertThatNoQuotesWordsCanBeFound();

	}

	@Test
	public void givenIdsAreNotSearchableWhenSearchingUsingIdsThenFindRecords()
			throws Exception {

		givenFrenchCollectionWithSearchableMetadatas();

		zeCollectionSetup.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeCollectionSchema.code()).get(Schemas.IDENTIFIER).setSearchable(false);
			}
		});

		Record record = new TestRecord(zeCollectionSchema, "00000042666");
		recordServices.add(record.set(zeCollectionSchema.largeTextMetadata(), quote1));

		Record anotherRecord = new TestRecord(zeCollectionSchema, "00000142666");
		recordServices.add(anotherRecord.set(zeCollectionSchema.largeTextMetadata(), quote1));

		Record thirdRecord = new TestRecord(zeCollectionSchema, "00000banane");
		recordServices.add(thirdRecord.set(zeCollectionSchema.largeTextMetadata(), quote1));

		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "00000042666"))).isEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "42666"))).isEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "00000142666"))).isEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "142666"))).isEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "00000banane"))).isEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "banane"))).isEmpty();

	}

	@Test
	public void givenSearchableNumberThenFindRecordsWithNumber()
			throws Exception {

		givenFrenchCollectionWithSearchableMetadatas();

		zeCollectionSetup.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeCollectionSchema.code()).get(Schemas.IDENTIFIER).setSearchable(false);
			}
		});

		Record record = new TestRecord(zeCollectionSchema, "00000042666");
		recordServices
				.add(record.set(zeCollectionSchema.largeTextMetadata(), quote1).set(zeCollectionSchema.numberMetadata(), 475.0));

		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + 475.0d))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + 475))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "\"475.0\""))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "\"475,0\""))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "\"475\""))).isNotEmpty();
	}

	@Test
	public void givenSearchableMultivalueNumberThenFindRecordsWithNumber()
			throws Exception {

		givenFrenchCollectionWithMultivalueSearchableMetadatas();

		zeCollectionSetup.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeCollectionSchema.code()).get(Schemas.IDENTIFIER).setSearchable(false);
			}
		});

		Record record = new TestRecord(zeCollectionSchema, "00000042666");
		recordServices.add(record.set(zeCollectionSchema.largeTextMetadata(), quote1)
				.set(zeCollectionSchema.numberMetadata(), asList(475.0, 333)));

		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + 475.0d))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + 475))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "\"475.0\""))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "\"475,0\""))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "\"475\""))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + 333.0d))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + 333))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "\"333.0\""))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "\"333,0\""))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "\"333\""))).isNotEmpty();
	}

	@Test
	public void givenSearchableDateThenFindRecordsWithDate()
			throws Exception {

		frenchSearchField = "dateMetadata.search_ss";
		givenFrenchCollectionWithSearchableMetadatas();

		zeCollectionSetup.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeCollectionSchema.code()).get(Schemas.IDENTIFIER).setSearchable(false);
			}
		});

		LocalDate date = new LocalDate(2001, 01, 01);

		Record record = new TestRecord(zeCollectionSchema, "00000042666");
		recordServices
				.add(record.set(zeCollectionSchema.largeTextMetadata(), quote1).set(zeCollectionSchema.dateMetadata(), date));

		//assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "2001/01/01"))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "2001-01-01"))).isNotEmpty();
	}

	@Test
	public void givenSearchableMultivalueDateThenFindRecordsWithDate()
			throws Exception {

		frenchSearchField = "dateMetadata.search_ss";
		givenFrenchCollectionWithMultivalueSearchableMetadatas();

		zeCollectionSetup.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeCollectionSchema.code()).get(Schemas.IDENTIFIER).setSearchable(false);
			}
		});

		givenConfig(ConstellioEIMConfigs.DATE_FORMAT, "MM-dd-yyyy");

		LocalDate date = new LocalDate(2001, 01, 01);
		LocalDate date2 = new LocalDate(2001, 01, 01).plusWeeks(1);

		Record record = new TestRecord(zeCollectionSchema, "00000042666");
		recordServices.add(record.set(zeCollectionSchema.largeTextMetadata(), quote1)
				.set(zeCollectionSchema.dateMetadata(), asList(date, date2)));

		reindex();

		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "01-01-2001"))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "01-08-2001"))).isNotEmpty();

		givenConfig(ConstellioEIMConfigs.DATE_FORMAT, "dd/MM/yyyy");
		reindex();

		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "01/01/2001"))).isNotEmpty();
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "08/01/2001"))).isNotEmpty();

	}

	@Test
	public void givenIdsAreSearchableWhenSearchingUsingIdsThenFindRecords()
			throws Exception {

		givenFrenchCollectionWithSearchableMetadatas();

		Record record = new TestRecord(zeCollectionSchema, "00000042666");
		recordServices.add(record.set(zeCollectionSchema.largeTextMetadata(), quote1));

		Record anotherRecord = new TestRecord(zeCollectionSchema, "00000142666");
		recordServices.add(anotherRecord.set(zeCollectionSchema.largeTextMetadata(), quote1));

		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "00000042666"))).containsOnly("00000042666");
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "42666"))).containsOnly("00000042666");
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "00000142666"))).containsOnly("00000142666");
		assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + "142666"))).containsOnly("00000142666");

	}

	// ----------------------------------------------------------------------------------

	private void givenFrenchCollectionWithUnSearchableMetadatas()
			throws Exception {

		prepareSystem(withZeCollection());
		recordServices = getModelLayerFactory().newRecordServices();
		contentManager = getModelLayerFactory().getContentManager();

		solrServer = ((DataLayerFactory) getDataLayerFactory()).getRecordsVaultServer().getNestedSolrServer();
		setupUsers();
		aliceInZeCollection = users.aliceIn(zeCollection);

		defineSchemasManager().using(zeCollectionSetup
				.withAStringMetadata(whichIsMultivalue)
				.withALargeTextMetadata()
				.withAMultivaluedLargeTextMetadata()
				.withAContentMetadata()
				.withAContentListMetadata());

	}

	private void givenFrenchCollectionWithSearchableMetadatas()
			throws Exception {

		prepareSystem(withZeCollection());
		recordServices = getModelLayerFactory().newRecordServices();
		contentManager = getModelLayerFactory().getContentManager();

		solrServer = ((DataLayerFactory) getDataLayerFactory()).getRecordsVaultServer().getNestedSolrServer();
		setupUsers();
		aliceInZeCollection = users.aliceIn(zeCollection);

		defineSchemasManager().using(zeCollectionSetup
				.withAStringMetadata(whichIsMultivalue, whichIsSearchable)
				.withANumberMetadata(whichIsSearchable)
				.withADateMetadata(whichIsSearchable)
				.withALargeTextMetadata(whichIsSearchable)
				.withAMultivaluedLargeTextMetadata(whichIsSearchable)
				.withAContentMetadata(whichIsSearchable)
				.withAContentListMetadata(whichIsSearchable));
	}

	private void givenFrenchCollectionWithMultivalueSearchableMetadatas()
			throws Exception {

		prepareSystem(withZeCollection());
		recordServices = getModelLayerFactory().newRecordServices();
		contentManager = getModelLayerFactory().getContentManager();

		solrServer = ((DataLayerFactory) getDataLayerFactory()).getRecordsVaultServer().getNestedSolrServer();
		setupUsers();
		aliceInZeCollection = users.aliceIn(zeCollection);

		defineSchemasManager().using(zeCollectionSetup
				.withAStringMetadata(whichIsMultivalue, whichIsSearchable)
				.withANumberMetadata(whichIsMultivalue, whichIsSearchable)
				.withADateMetadata(whichIsMultivalue, whichIsSearchable)
				.withALargeTextMetadata(whichIsSearchable)
				.withAMultivaluedLargeTextMetadata(whichIsSearchable)
				.withAContentMetadata(whichIsSearchable)
				.withAContentListMetadata(whichIsSearchable));
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
		users.setUp(userServices);
		userServices.addUserToCollection(users.alice(), zeCollection);
		userServices.addUserToCollection(users.bob(), zeCollection);
		userServices.addUserToCollection(users.dakotaLIndien(), zeCollection);

		Transaction transaction = new Transaction();
		transaction.add(users.aliceIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
		transaction.add(users.bobIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
		transaction.add(users.dakotaLIndienIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
		recordServices.execute(transaction);
	}

	private void assertThatNoFileNamesCanBeFound()
			throws SolrServerException, IOException {
		assertThatOnlyFoundFilenamesAre();
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

	private void assertThatNoQuotesWordsCanBeFound()
			throws SolrServerException, IOException {
		assertThatOnlyFoundFilenamesAre();
	}

	private void assertThatOnlyFoundQuoteWordsAre(SearchableQuoteWord... words)
			throws SolrServerException, IOException {
		List<SearchableQuoteWord> expectedFoundWords = asList(words);
		for (SearchableQuoteWord aSearchableQuoteWord : allWords) {
			String word = aSearchableQuoteWord.word;
			String field = aSearchableQuoteWord.english ? "english" : "french";
			String wordsFoundDescription = "Word '" + word + "' was found in " + field + " field";
			String wordsNotFoundDescription = "Word '" + word + "' was not found in " + field + " field";
			if (expectedFoundWords.contains(aSearchableQuoteWord)) {
				if (aSearchableQuoteWord.english) {
					assertThat(resultsIdsOf(paramsWithQ(englishSearchField + ":" + word))).describedAs(wordsNotFoundDescription)
							.hasSize(1);
					assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + word))).describedAs(wordsFoundDescription)
							.isEmpty();
				} else {
					assertThat(resultsIdsOf(paramsWithQ(englishSearchField + ":" + word))).describedAs(wordsFoundDescription)
							.isEmpty();
					assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + word))).describedAs(wordsNotFoundDescription)
							.hasSize(1);
				}
			} else {
				assertThat(resultsIdsOf(paramsWithQ(englishSearchField + ":" + word))).describedAs(wordsFoundDescription)
						.isEmpty();
				assertThat(resultsIdsOf(paramsWithQ(frenchSearchField + ":" + word))).describedAs(wordsFoundDescription)
						.isEmpty();
			}
		}
	}

	private ModifiableSolrParams paramsWithQ(String q) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", q);
		params.set("fq", "schema_s:zeSchemaType_*");
		return params;
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
