package com.constellio.model.services.contents;

import com.constellio.data.dao.services.bigVault.solr.BigVaultException.CouldNotExecuteQuery;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserPermissionsChecker;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_CannotDeleteLastVersion;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_ContentMustBeCheckedOut;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_ContentMustNotBeCheckedOut;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_InvalidArgument;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_UserHasNoDeleteVersionPermission;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_VersionMustBeHigherThanPreviousVersion;
import com.constellio.model.services.contents.ContentManager.ParseOptions;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_ContentHasNoPreview;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_ContentHasNoThumbnail;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.assertj.core.api.Condition;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;
import static com.constellio.model.services.contents.ContentFactory.isCheckedOutBy;
import static com.constellio.model.services.migrations.ConstellioEIMConfigs.PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class ContentManagementAcceptTest extends ConstellioTest {

	private AtomicInteger threadCalls = new AtomicInteger();

	private final String OCTET_STREAM_MIMETYPE = "application/octet-stream";
	private final String PDF_MIMETYPE = "application/pdf";
	private final String DOCX_MIMETYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	LocalDateTime smashOClock = new LocalDateTime();
	LocalDateTime shishOClock = new LocalDateTime().plusHours(1);
	LocalDateTime teaOClock = new LocalDateTime().plusHours(2);
	ContentManager contentManager;
	RecordServices recordServices;
	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	TestsSchemasSetup anotherCollectionSchemas = new TestsSchemasSetup("anotherCollection");
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	ZeSchemaMetadatas anotherCollectionSchema = anotherCollectionSchemas.new ZeSchemaMetadatas();
	User bob, bobInAnotherCollection;
	User alice;
	String bobId;
	String aliceId;

	private long pdf1Length = 170039L;
	private long pdf2Length = 167347L;
	private long pdf3Length = 141667L;
	private long docx1Length = 27055L;
	private long docx2Length = 27325L;

	private String pdf1Hash = "KN8RjbrnBgq1EDDV2U71a6_6gd4=";
	private String pdf2Hash = "T-4zq4cGP_tXkdJp_qz1WVWYhoQ=";
	private String pdf3Hash = "2O9RyZlxNUL3asxk2yGDT6VIlbs=";
	private String docx1Hash = "Fss7pKBafi8ok5KaOwEpmNdeGCE=";
	private String docx2Hash = "TIKwSvHOXHOOtRd1K9t2fm4TQ4I=";

	@Mock UserPermissionsChecker userPermissionsChecker;

	@Before
	public void setUp()
			throws Exception {

		givenHashingEncodingIs(BASE64_URL_ENCODED);
		withSpiedServices(ContentManager.class);

		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryModelLayerConfiguration configuration) {
				configuration.setDelayBeforeDeletingUnreferencedContents(org.joda.time.Duration.standardMinutes(42));
				configuration.setUnreferencedContentsThreadDelayBetweenChecks(org.joda.time.Duration.standardHours(10));
			}
		});
		customSystemPreparation(new CustomSystemPreparation() {
			@Override
			public void prepare() {
				try {
					defineSchemasManager()
							.using(schemas.withAContentMetadata(whichIsSearchable).withAContentListMetadata()
									.withAParentReferenceFromZeSchemaToZeSchema());
					defineSchemasManager().using(anotherCollectionSchemas.withAContentMetadata());

					recordServices = getModelLayerFactory().newRecordServices();
					contentManager = getModelLayerFactory().getContentManager();

					MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
					TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();

					Map<Language, String> labelTitle1 = new HashMap<>();
					labelTitle1.put(Language.French, "taxo");

					Taxonomy taxonomy = Taxonomy.createPublic("taxo", labelTitle1, zeCollection, asList("zeSchemaType"));
					taxonomiesManager.addTaxonomy(taxonomy, metadataSchemasManager);
					taxonomiesManager.setPrincipalTaxonomy(taxonomy, metadataSchemasManager);

					UserServices userServices = getModelLayerFactory().newUserServices();
					userServices.addUpdateUserCredential(userServices.createUserCredential(
							"bob", "bob", "gratton", "bob@doculibre.com", new ArrayList<String>(),
							asList(zeCollection, "anotherCollection"), UserCredentialStatus.ACTIVE, "domain", Arrays.asList(""),
							null));

					userServices.addUpdateUserCredential(userServices.createUserCredential(
							"alice", "alice", "wonderland", "alice@doculibre.com", new ArrayList<String>(),
							asList(zeCollection), UserCredentialStatus.ACTIVE, "domain", Arrays.asList(""), null));

					bob = spy(userServices.getUserInCollection("bob", zeCollection));
					bob.setCollectionDeleteAccess(true);
					recordServices.update(bob.getWrappedRecord());

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void initializeFromCache() {
				recordServices = getModelLayerFactory().newRecordServices();
				contentManager = getModelLayerFactory().getContentManager();
				schemas.refresh(getModelLayerFactory().getMetadataSchemasManager());
				anotherCollectionSchemas.refresh(getModelLayerFactory().getMetadataSchemasManager());
			}
		});

		givenConfig(ConstellioEIMConfigs.VIEWER_CONTENTS_CONVERSION_SCHEDULE, null);
		givenConfig(ConstellioEIMConfigs.UNREFERENCED_CONTENTS_DELETE_SCHEDULE, null);
		givenConfig(ConstellioEIMConfigs.UNREFERENCED_CONTENTS_DELETE_SCHEDULE, "00-24");
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);
		givenTimeIs(smashOClock);

		alice = getModelLayerFactory().newUserServices().getUserInCollection("alice", zeCollection);
		bob = spy(getModelLayerFactory().newUserServices().getUserInCollection("bob", zeCollection));
		bobInAnotherCollection = getModelLayerFactory().newUserServices().getUserInCollection("bob", "anotherCollection");
		aliceId = alice.getId();
		bobId = bob.getId();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				Object value = invocation.callRealMethod();
				threadCalls.incrementAndGet();
				return value;
			}
		}).when(contentManager).deleteUnreferencedContents();
	}

	@Test
	public void whenAddingContentToSinglevalueContentMetadataThenContentRetreivable()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();

		assertThat(theRecordContent()).isNot(emptyVersion);
		assertThat(theRecordContent().getCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.is(version("0.1")).is(modifiedBy(bob)).has(modificationDatetime(smashOClock));
		assertThatVaultOnlyContains(pdf1Hash);
	}

	@Test
	public void whenAddingMinorEmptyContentThenContentRetreivable()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createEmptyMinor(bob, "ZePdf.pdf", uploadPdf1InputStream()))
				.isSaved();

		assertThat(theRecordContent()).is(emptyVersion);
		assertThat(theRecordContent().getCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.is(version("0.1")).is(modifiedBy(bob)).has(modificationDatetime(smashOClock));
		assertThatVaultOnlyContains(pdf1Hash);
	}

	@Test
	public void whenMarkingContentHashForCheckThenVeryFast()
			throws Exception {
		long before = new Date().getTime();

		for (int i = 0; i < 10000; i++) {
			contentManager.markForDeletionIfNotReferenced(UUIDV1Generator.newRandomId());
			if (i % 1000 == 0) {
				Record record = new TestRecord(zeSchema, UUIDV1Generator.newRandomId());
				new RecordPreparation(record).isSaved();
			}
			System.out.println(i);
		}

		long after = new Date().getTime();
		System.out.println((after - before));
	}

	@Test
	public void whenSearchingByBorrowedContentThenFindDocuments()
			throws Exception {

		Content minorContentAddedByBob = contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream()).checkOut(bob);
		Content contentAddedByAliceThenBorrowedByBob = contentManager.createMinor(alice, "ZePdf.pdf", uploadPdf1InputStream())
				.checkOut(bob);
		Content returnedContent = contentManager.createMajor(alice, "ZePdf.pdf", uploadPdf1InputStream());
		Content minorContentAddedByAlice = contentManager.createMinor(alice, "ZePdf.pdf", uploadPdf1InputStream())
				.checkOut(alice);
		Content minorContentAddedByBobThenBorrowedByAlice = contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())
				.checkOut(bob).checkIn().checkOut(alice);

		givenRecordWithId("bobDoc1").withSingleValueContent(minorContentAddedByBob).isSaved();
		givenRecordWithId("bobDoc2").withSingleValueContent(contentAddedByAliceThenBorrowedByBob).isSaved();
		givenRecordWithId("aliceDoc1").withSingleValueContent(minorContentAddedByAlice).isSaved();
		givenRecordWithId("aliceDoc2").withSingleValueContent(minorContentAddedByAlice).isSaved();
		givenRecordWithId("notBorrowedDoc").withSingleValueContent(returnedContent).isSaved();

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchQuery bobBorrowerdDocumentsQuery = new LogicalSearchQuery()
				.setCondition(from(zeSchema.instance()).where(zeSchema.contentMetadata()).is(isCheckedOutBy(bob)));

		LogicalSearchQuery aliceBorrowerdDocumentsQuery = new LogicalSearchQuery()
				.setCondition(from(zeSchema.instance()).where(zeSchema.contentMetadata()).is(isCheckedOutBy(alice)));

		assertThat(searchServices.searchRecordIds(bobBorrowerdDocumentsQuery)).containsOnly("bobDoc1", "bobDoc2");
		assertThat(searchServices.searchRecordIds(aliceBorrowerdDocumentsQuery)).containsOnly("aliceDoc1", "aliceDoc2");
	}

	@Test
	public void whenReplacingSingleValueContentThenNewContentRetreivableAndPreviousDeleted()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();

		when(theRecord()).withSingleValueContent(contentManager.createMinor(bob, "ZeDocx.docx", uploadDocx1InputStream()))
				.isSaved();

		assertThat(theRecordContent().getCurrentVersion()).has(docsMimetype()).has(filename("ZeDocx.docx")).has(
				docx1HashAndLength())
				.has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));
		assertThatVaultOnlyContains(docx1Hash);
	}

	@Test
	public void whenAddingContentToMultivalueContentMetadataThenNewContentRetreivable()
			throws Exception {

		givenRecord().addMultiValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();

		assertThat(theRecordFirstMultivalueContent().getCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf"))
				.has(pdf1HashAndLength()).has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));
		assertThatVaultOnlyContains(pdf1Hash);

	}

	@Test
	public void whenAddingAContentWithMultipleVersionsWithDifferentNamesThenNamesCorrectlySaved()
			throws Exception {

		Content content = contentManager.createMinor(alice, "ZePdf1.pdf", uploadPdf1InputStream());
		content.updateContentWithName(bob, uploadPdf2InputStream(), true, "ZePdf2.pdf");
		content.updateContentWithName(alice, uploadPdf3InputStream(), false, "ZePdf3.pdf");
		content.updateContentWithName(alice, uploadDocx1InputStream(), false, "ZeDocx1.docx");
		content.updateContentWithName(bob, uploadDocx2InputStream(), true, "ZeDocx2.docx");

		givenRecord().withSingleValueContent(content).isSaved();

		assertThat(theRecordContent().getVersions()).extracting("version", "filename", "lastModifiedBy").isEqualTo(asList(
				tuple("0.1", "ZePdf1.pdf", aliceId),
				tuple("1.0", "ZePdf2.pdf", bobId),
				tuple("1.1", "ZePdf3.pdf", aliceId),
				tuple("1.2", "ZeDocx1.docx", aliceId),
				tuple("2.0", "ZeDocx2.docx", bobId)
		));
	}

	@Test
	public void whenAddingAContentThenUpdateNameWithMultipleVersionsWithDifferentNamesThenNamesCorrectlySaved()
			throws Exception {

		Content content = contentManager.createMinor(alice, "ZePdf1.pdf", uploadPdf1InputStream());
		content.updateContent(bob, uploadPdf2InputStream(), true);
		content.renameCurrentVersion("ZePdf2.pdf");
		content.updateContentWithName(alice, uploadPdf3InputStream(), false, "test.txt");
		content.renameCurrentVersion("ZePdf3.pdf");
		content.updateContent(alice, uploadDocx1InputStream(), false);
		content.renameCurrentVersion("ZeDocx1.docx");
		content.updateContentWithName(bob, uploadDocx2InputStream(), true, "test.txt");
		content.renameCurrentVersion("ZeDocx2.docx");

		givenRecord().withSingleValueContent(content).isSaved();

		assertThat(theRecordContent().getVersions()).extracting("version", "filename", "lastModifiedBy").isEqualTo(asList(
				tuple("0.1", "ZePdf1.pdf", aliceId),
				tuple("1.0", "ZePdf2.pdf", bobId),
				tuple("1.1", "ZePdf3.pdf", aliceId),
				tuple("1.2", "ZeDocx1.docx", aliceId),
				tuple("2.0", "ZeDocx2.docx", bobId)
		));

	}

	@Test
	public void givenDefaultBehaviorIsParseAsyncWhenAddingContentThenFlaguedAndParsedLater()
			throws Exception {

		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.ASYNC_PARSING_FOR_ALL_CONTENTS);

		Content content = contentManager.createMinor(alice, "ZePdf1.pdf", uploadPdf1InputStream());

		Record record = givenRecord().withSingleValueContent(content).isSaved();
		assertThat(contentManager.isParsed(pdf1Hash)).isFalse();
		assertThat(record.<Boolean>get(Schemas.MARKED_FOR_PARSING)).isTrue();
		assertThat(contentMetadataParsedContentOf("zeRecord")).isNull();

		contentManager.handleRecordsMarkedForParsing();
		recordServices.refresh(record);

		assertThat(contentManager.isParsed(pdf1Hash)).isTrue();
		assertThat(record.<Boolean>get(Schemas.MARKED_FOR_PARSING)).isNull();
		assertThat(contentMetadataParsedContentOf("zeRecord")).contains("Forage de texte");

	}

	@Test
	public void givenDefaultBehaviorIsParseAsyncWhenAddingContentWithSyncParsingThenAlreadyParsed()
			throws Exception {

		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.ASYNC_PARSING_FOR_ALL_CONTENTS);

		UploadOptions options = new UploadOptions().setParseOptions(new ParseOptions());
		Content content = contentManager.createMinor(alice, "ZePdf1.pdf", uploadPdf1InputStream(options));

		Record record = givenRecord().withSingleValueContent(content).isSaved();
		assertThat(contentManager.isParsed(pdf1Hash)).isTrue();
		assertThat(record.<Boolean>get(Schemas.MARKED_FOR_PARSING)).isNull();
		assertThat(contentMetadataParsedContentOf("zeRecord")).contains("Forage de texte");

	}

	@Test
	public void givenMultipleContentVersionUploadedWithParseAsyncWhenParsedThenOnlyLastVersionIsParsed()
			throws Exception {

		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.ASYNC_PARSING_FOR_ALL_CONTENTS);

		Content content = contentManager.createMinor(alice, "ZePdf1.pdf", uploadPdf1InputStream());
		content.updateContent(bob, uploadPdf2InputStream(), false);

		Record record = givenRecord().withSingleValueContent(content).isSaved();
		assertThat(contentManager.isParsed(pdf1Hash)).isFalse();
		assertThat(contentManager.isParsed(pdf2Hash)).isFalse();
		assertThat(record.<Boolean>get(Schemas.MARKED_FOR_PARSING)).isTrue();
		assertThat(contentMetadataParsedContentOf("zeRecord")).isNull();

		contentManager.handleRecordsMarkedForParsing();
		recordServices.refresh(record);

		assertThat(contentManager.isParsed(pdf1Hash)).isFalse();
		assertThat(contentManager.isParsed(pdf2Hash)).isTrue();
		assertThat(record.<Boolean>get(Schemas.MARKED_FOR_PARSING)).isNull();
		assertThat(contentMetadataParsedContentOf("zeRecord")).contains("Château");

		record.<Content>get(zeSchema.contentMetadata()).deleteVersion("0.2");
		recordServices.update(record);
		contentManager.handleRecordsMarkedForParsing();
		recordServices.refresh(record);

		assertThat(contentManager.isParsed(pdf1Hash)).isTrue();
		assertThat(contentManager.isParsed(pdf2Hash)).isTrue();
		assertThat(record.<Boolean>get(Schemas.MARKED_FOR_PARSING)).isNull();
		assertThat(contentMetadataParsedContentOf("zeRecord")).contains("Forage de texte");

	}

	String contentMetadataParsedContentOf(String id) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.add("q", "id:" + id);
		SolrDocument document = null;
		try {
			document = getDataLayerFactory().newRecordDao().getBigVaultServer().query(params).getResults().get(0);
		} catch (CouldNotExecuteQuery couldNotExecuteQuery) {
			throw new RuntimeException(couldNotExecuteQuery);
		}
		return (String) document.getFirstValue("contentMetadata_txt_fr");
	}

	@Test
	public void whenAddingContentWithoutParsingHistoryVersionsThenMimetypeCorrectlySet()
			throws Exception {

		Content content = contentManager.createMinor(alice, "ZePdf.pdf", uploadPdf1InputStreamWithoutParsing());
		content.updateContent(bob, uploadPdf2InputStreamWithoutParsing(), true);
		content.updateContent(alice, uploadPdf3InputStreamWithoutParsing(), false);
		content.updateContentWithName(alice, uploadDocx1InputStreamWithoutParsing(), false, "ZeDocx.docx");
		content.updateContent(bob, uploadDocx2InputStreamWithoutParsing(), true);

		givenRecord().withSingleValueContent(content).isSaved();

		assertThat(theRecordContent().getVersions())
				.extracting("version", "filename", "lastModifiedBy", "mimetype", "hash", "length").isEqualTo(asList(
				tuple("0.1", "ZePdf.pdf", aliceId, PDF_MIMETYPE, pdf1Hash, pdf1Length),
				tuple("1.0", "ZePdf.pdf", bobId, PDF_MIMETYPE, pdf2Hash, pdf2Length),
				tuple("1.1", "ZePdf.pdf", aliceId, PDF_MIMETYPE, pdf3Hash, pdf3Length),
				tuple("1.2", "ZeDocx.docx", aliceId, DOCX_MIMETYPE, docx1Hash, docx1Length),
				tuple("2.0", "ZeDocx.docx", bobId, DOCX_MIMETYPE, docx2Hash, docx2Length)
		));

		assertThat(contentManager.isParsed(pdf1Hash)).isFalse();
		assertThat(contentManager.isParsed(pdf2Hash)).isFalse();
		assertThat(contentManager.isParsed(pdf3Hash)).isFalse();
		assertThat(contentManager.isParsed(docx1Hash)).isFalse();
		assertThat(contentManager.isParsed(docx2Hash)).isFalse();

		contentManager.handleRecordsMarkedForParsing();

		assertThat(contentManager.isParsed(pdf1Hash)).isFalse();
		assertThat(contentManager.isParsed(pdf2Hash)).isFalse();
		assertThat(contentManager.isParsed(pdf3Hash)).isFalse();
		assertThat(contentManager.isParsed(docx1Hash)).isFalse();
		assertThat(contentManager.isParsed(docx2Hash)).isTrue();
	}

	@Test
	public void givenHistoryVersionsAreNotParsedWhenRemovingCurrentVersionThenParseLastHistoryVersion()
			throws Exception {


		doReturn(true).when(userPermissionsChecker).globally();
		doReturn(userPermissionsChecker).when(bob).has(CorePermissions.DELETE_CONTENT_VERSION);

		Content content = contentManager.createMinor(alice, "ZePdf.pdf", uploadPdf1InputStreamWithoutParsing());
		content.updateContent(bob, uploadPdf2InputStreamWithoutParsing(), true);
		content.updateContent(alice, uploadPdf3InputStreamWithoutParsing(), false);
		content.updateContentWithName(alice, uploadDocx1InputStreamWithoutParsing(), false, "ZeDocx.docx");
		content.updateContent(bob, uploadDocx2InputStreamWithoutParsing(), true);

		givenRecord().withSingleValueContent(content).isSaved();

		when(theRecord()).deleteVersion("2.0", bob).and().isSaved();

		assertThat(theRecordContent().getVersions())
				.extracting("version", "filename", "lastModifiedBy", "mimetype", "hash", "length").isEqualTo(asList(
				tuple("0.1", "ZePdf.pdf", aliceId, PDF_MIMETYPE, pdf1Hash, pdf1Length),
				tuple("1.0", "ZePdf.pdf", bobId, PDF_MIMETYPE, pdf2Hash, pdf2Length),
				tuple("1.1", "ZePdf.pdf", aliceId, PDF_MIMETYPE, pdf3Hash, pdf3Length),
				tuple("1.2", "ZeDocx.docx", aliceId, DOCX_MIMETYPE, docx1Hash, docx1Length)
		));

		assertThat(contentManager.isParsed(pdf1Hash)).isFalse();
		assertThat(contentManager.isParsed(pdf2Hash)).isFalse();
		assertThat(contentManager.isParsed(pdf3Hash)).isFalse();
		assertThat(contentManager.isParsed(docx1Hash)).isFalse();

		contentManager.handleRecordsMarkedForParsing();

		assertThat(contentManager.isParsed(pdf1Hash)).isFalse();
		assertThat(contentManager.isParsed(pdf2Hash)).isFalse();
		assertThat(contentManager.isParsed(pdf3Hash)).isFalse();
		assertThat(contentManager.isParsed(docx1Hash)).isTrue();
	}

	@Test
	public void givenHistoryVersionsAreNotParsedWhenAddingAPreviousVersionHasANewVersionThenParsed()
			throws Exception {

		Content content = contentManager.createMinor(alice, "ZePdf.pdf", uploadPdf1InputStreamWithoutParsing());
		assertThat(contentManager.isParsed(pdf2Hash)).isFalse();
		content.updateContent(bob, uploadPdf2InputStreamWithoutParsing(), true);
		assertThat(contentManager.isParsed(pdf2Hash)).isFalse();
		content.updateContent(alice, uploadPdf3InputStream(), false);
		content.updateContentWithName(alice, uploadDocx1InputStreamWithoutParsing(), false, "ZeDocx.docx");
		content.updateContent(bob, uploadDocx2InputStream(), true);

		givenRecord().withSingleValueContent(content).isSaved();

		when(theRecord()).hasItsContentUpdated(alice, uploadPdf1InputStream()).and().isSaved();

		assertThat(theRecordContent().getVersions())
				.extracting("version", "filename", "lastModifiedBy", "mimetype", "hash", "length").isEqualTo(asList(
				tuple("0.1", "ZePdf.pdf", aliceId, PDF_MIMETYPE, pdf1Hash, pdf1Length),
				tuple("1.0", "ZePdf.pdf", bobId, PDF_MIMETYPE, pdf2Hash, pdf2Length),
				tuple("1.1", "ZePdf.pdf", aliceId, PDF_MIMETYPE, pdf3Hash, pdf3Length),
				tuple("1.2", "ZeDocx.docx", aliceId, DOCX_MIMETYPE, docx1Hash, docx1Length),
				tuple("2.0", "ZeDocx.docx", bobId, DOCX_MIMETYPE, docx2Hash, docx2Length),
				tuple("2.1", "ZeDocx.docx", aliceId, PDF_MIMETYPE, pdf1Hash, pdf1Length)
		));

		assertThat(contentManager.isParsed(pdf1Hash)).isTrue();
		assertThat(contentManager.isParsed(pdf2Hash)).isFalse();
		assertThat(contentManager.isParsed(pdf3Hash)).isTrue();
		assertThat(contentManager.isParsed(docx1Hash)).isFalse();
		assertThat(contentManager.isParsed(docx2Hash)).isTrue();
	}

	@Test
	public void whenAddingAContentThenUpdateNameAndCommentWithMultipleVersionsWithDifferentNamesThenNamesCorrectlySaved()
			throws Exception {

		Content content = contentManager.createMinor(alice, "zetest.pdf", uploadPdf1InputStream());
		content.setVersionComment("version comment 1");
		content.updateContent(bob, uploadPdf2InputStream(), true);
		content.setVersionComment("version comment 2");
		content.updateContent(alice, uploadPdf3InputStream(), false);
		content.setVersionComment("version comment 3");
		content.updateContentWithName(alice, uploadDocx1InputStream(), false, "zetest.docx");
		content.setVersionComment("version comment 4");
		content.updateContent(bob, uploadDocx2InputStream(), true);
		content.setVersionComment("version comment 5");

		givenRecord().withSingleValueContent(content).isSaved();

		assertThat(theRecordContent().getVersions()).extracting("version", "filename", "lastModifiedBy", "comment")
				.isEqualTo(asList(
						tuple("0.1", "zetest.pdf", aliceId, "version comment 1"),
						tuple("1.0", "zetest.pdf", bobId, "version comment 2"),
						tuple("1.1", "zetest.pdf", aliceId, "version comment 3"),
						tuple("1.2", "zetest.docx", aliceId, "version comment 4"),
						tuple("2.0", "zetest.docx", bobId, "version comment 5")
				));

	}

	@Test
	public void givenEmptyCommentThenSavedAsAnEmptyComment()
			throws Exception {

		Content content = contentManager.createMinor(alice, "zetest.pdf", uploadPdf1InputStream());
		content.setVersionComment("");
		givenRecord().withSingleValueContent(content).isSaved();

		assertThat(theRecordContent().getVersions()).extracting("version", "filename", "lastModifiedBy", "comment")
				.isEqualTo(asList(
						tuple("0.1", "zetest.pdf", aliceId, null)
				));

		when(theRecord()).hasItsContentCommentChangedTo("Ze comment").and().isSaved();
		assertThat(theRecordContent().getVersions()).extracting("version", "filename", "lastModifiedBy", "comment")
				.isEqualTo(asList(
						tuple("0.1", "zetest.pdf", aliceId, "Ze comment")
				));
	}

	@Test
	public void whenModifyingContentOfMultivalueContentMetadataThenNewContentRetreivableAndOldRemoved()
			throws Exception {

		givenRecord().addMultiValueContent(contentManager.createMinor(alice, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();

		givenTimeIs(shishOClock);
		given(theRecord()).clearAllMultiValueContents().addMultiValueContent(contentManager.createMinor(bob, "ZePdf2.pdf",
				uploadPdf2InputStream())).addMultiValueContent(
				contentManager.createMajor(bob, "ZeDocx.docx", uploadDocx1InputStream())).isSaved();

		assertThat(theRecordFirstMultivalueContent().getCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf2.pdf"))
				.has(pdf2HashAndLength()).has(version("0.1")).is(modifiedBy(bob)).has(modificationDatetime(shishOClock));

		assertThat(theRecordSecondAndLastMultivalueContent().getCurrentVersion()).has(docsMimetype()).has(filename("ZeDocx.docx"))
				.has(docx1HashAndLength()).has(version("1.0")).has(modifiedBy(bob)).has(modificationDatetime(shishOClock));
		assertThatVaultOnlyContains(docx1Hash, pdf2Hash);
	}

	@Test
	public void whenRenamingContentThenNameModified()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();

		when(theRecord()).hasItsContentRenamedTo("ZeUltimatePdf.pdf").and().isSaved();

		assertThat(theRecordContent().getCurrentVersion()).has(pdfMimetype()).has(filename("ZeUltimatePdf.pdf")).has(
				pdf1HashAndLength())
				.has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));
	}

	@Test
	public void whenCommentingAVersionThenCommentModified()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();

		when(theRecord()).hasItsContentCommentChangedTo("comment 1").and().isSaved();
		assertThat(theRecordContent().getCurrentVersion()).has(pdfMimetype()).has(comment("comment 1")).has(
				pdf1HashAndLength()).has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));

		when(theRecord()).hasItsContentCommentChangedTo("comment 2").and().isSaved();
		assertThat(theRecordContent().getCurrentVersion()).has(pdfMimetype()).has(comment("comment 2")).has(
				pdf1HashAndLength()).has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));
	}

	@Test
	public void givenaACheckedOutContentWhenCommentingAVersionThenCommentModified()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream()))
				.contentCheckedOutBy(alice).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsCheckedOutContentUpdatedWith(uploadPdf2InputStream()).hasItsContentCommentChangedTo("comment 1")
				.and().isSaved();

		assertThat(theRecordContent().getCurrentVersion()).has(pdfMimetype()).has(
				pdf1HashAndLength()).has(version("0.1")).has(modifiedBy(bob)).has(comment(null))
				.has(modificationDatetime(smashOClock));
		assertThat(theRecordContent().getCurrentCheckedOutVersion()).has(pdfMimetype()).has(comment("comment 1")).has(
				pdf2HashAndLength()).has(version("0.2")).has(modifiedBy(alice)).has(modificationDatetime(shishOClock));

		givenTimeIs(teaOClock);
		when(theRecord()).hasItsContentCommentChangedTo("comment 2").and().isSaved();
		assertThat(theRecordContent().getCurrentVersion()).has(pdfMimetype()).has(
				pdf1HashAndLength()).has(version("0.1")).has(modifiedBy(bob)).has(comment(null))
				.has(modificationDatetime(smashOClock));
		assertThat(theRecordContent().getCurrentCheckedOutVersion()).has(pdfMimetype()).has(comment("comment 2")).has(
				pdf2HashAndLength()).has(version("0.2")).has(modifiedBy(alice)).has(modificationDatetime(shishOClock));
	}

	@Test
	public void whenModifyingContentThenContentHashModifiedAndVersionIncremented()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();
		assertThat(theRecordContent().getCurrentVersion()).is(modifiedBy(bob)).has(modificationDatetime(smashOClock));

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsContentUpdated(alice, uploadPdf2InputStream()).and().isSaved();

		assertThat(theRecordContent().getCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf2HashAndLength())
				.has(
						version("0.2")).has(modifiedBy(alice)).has(modificationDatetime(shishOClock));

		assertThat(theRecordContent()).is(notCheckedOut());
		assertThatVaultOnlyContains(pdf1Hash, pdf2Hash);
	}

	@Test
	public void whenModifyingContentMultipleTimesThenContentHashModifiedAndVersionIncrementedEachTime()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();
		assertThat(theRecordContent().getCurrentVersion()).is(modifiedBy(bob)).has(modificationDatetime(smashOClock));

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsContentUpdated(alice, uploadPdf2InputStream()).and().isSaved();

		givenTimeIs(teaOClock);
		when(theRecord()).hasItsContentUpdated(bob, uploadPdf3InputStream()).and().isSaved();

		assertThat(theRecordContent().getCurrentVersion()).has(pdf3HashAndLength()).has(version("0.3")).has(modifiedBy(bob))
				.has(modificationDatetime(teaOClock));

		assertThat(theRecordContentHistory()).hasSize(2);
		assertThat(theRecordContentHistory().get(0)).has(pdf1HashAndLength()).has(version("0.1")).has(modifiedBy(bob))
				.has(modificationDatetime(smashOClock));
		assertThat(theRecordContentHistory().get(1)).has(pdf2HashAndLength()).has(version("0.2")).has(modifiedBy(alice))
				.has(modificationDatetime(shishOClock));
		assertThatVaultOnlyContains(pdf1Hash, pdf2Hash, pdf3Hash);
	}

	@Test(expected = ContentImplRuntimeException_UserHasNoDeleteVersionPermission.class)
	public void givenUserHasNoDeletePermissionThenExceptionWhenDeleteingVersion()
			throws Exception {
		doReturn(false).when(userPermissionsChecker).globally();
		doReturn(userPermissionsChecker).when(bob).has(CorePermissions.DELETE_CONTENT_VERSION);

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();
		assertThat(theRecordContent().getCurrentVersion()).is(modifiedBy(bob)).has(modificationDatetime(smashOClock));

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsContentUpdated(alice, uploadPdf2InputStream()).and().isSaved();

		givenTimeIs(teaOClock);
		when(theRecord()).hasItsContentUpdated(bob, uploadPdf3InputStream()).and().isSaved();

		when(theRecord()).deleteVersion("0.1", bob);
	}

	@Test
	public void whenDeletingTheOldestHistoryVersionThenDeleted()
			throws Exception {
		doReturn(true).when(userPermissionsChecker).globally();
		doReturn(userPermissionsChecker).when(bob).has(CorePermissions.DELETE_CONTENT_VERSION);

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();
		assertThat(theRecordContent().getCurrentVersion()).is(modifiedBy(bob)).has(modificationDatetime(smashOClock));

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsContentUpdated(alice, uploadPdf2InputStream()).and().isSaved();

		givenTimeIs(teaOClock);
		when(theRecord()).hasItsContentUpdated(bob, uploadPdf3InputStream()).and().isSaved();

		when(theRecord()).deleteVersion("0.1", bob).isSaved();

		assertThat(theRecordContent().getCurrentVersion()).has(pdf3HashAndLength()).has(version("0.3")).has(modifiedBy(bob))
				.has(modificationDatetime(teaOClock));

		assertThat(theRecordContentHistory()).hasSize(1);

		assertThat(theRecordContentHistory().get(0)).has(pdf2HashAndLength()).has(version("0.2")).has(modifiedBy(alice))
				.has(modificationDatetime(shishOClock));
		assertThatVaultOnlyContains(pdf2Hash, pdf3Hash);
	}

	@Test
	public void whenDeletingCurrentVersionThenPreviousVersionIsMovedFromHistoryToCurrentVersion()
			throws Exception {
		doReturn(true).when(userPermissionsChecker).globally();
		doReturn(userPermissionsChecker).when(bob).has(CorePermissions.DELETE_CONTENT_VERSION);

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();
		assertThat(theRecordContent().getCurrentVersion()).is(modifiedBy(bob)).has(modificationDatetime(smashOClock));

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsContentUpdated(alice, uploadPdf2InputStream()).and().isSaved();

		givenTimeIs(teaOClock);
		when(theRecord()).hasItsContentUpdated(bob, uploadPdf3InputStream()).and().isSaved();

		when(theRecord()).deleteVersion("0.3", bob).isSaved();

		assertThat(theRecordContent().getCurrentVersion()).has(pdf2HashAndLength()).has(version("0.2")).has(modifiedBy(alice))
				.has(modificationDatetime(shishOClock));

		assertThat(theRecordContentHistory()).hasSize(1);

		assertThat(theRecordContentHistory().get(0)).has(pdf1HashAndLength()).has(version("0.1")).has(modifiedBy(bob))
				.has(modificationDatetime(smashOClock));
		assertThatVaultOnlyContains(pdf1Hash, pdf2Hash);
	}

	@Test(expected = ContentImplRuntimeException_CannotDeleteLastVersion.class)
	public void whenDeletingTheOnlyVersionThenException()
			throws Exception {
		doReturn(true).when(userPermissionsChecker).globally();
		doReturn(userPermissionsChecker).when(bob).has(CorePermissions.DELETE_CONTENT_VERSION);

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();
		assertThat(theRecordContent().getCurrentVersion()).is(modifiedBy(bob)).has(modificationDatetime(smashOClock));

		when(theRecord()).deleteVersion("0.1", bob);
	}

	@Test
	public void whenDeletingTheNewestHistoryVersionThenDeleted()
			throws Exception {
		doReturn(true).when(userPermissionsChecker).globally();
		doReturn(userPermissionsChecker).when(bob).has(CorePermissions.DELETE_CONTENT_VERSION);

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();
		assertThat(theRecordContent().getCurrentVersion()).is(modifiedBy(bob)).has(modificationDatetime(smashOClock));

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsContentUpdated(alice, uploadPdf2InputStream()).and().isSaved();

		givenTimeIs(teaOClock);
		when(theRecord()).hasItsContentUpdated(bob, uploadPdf3InputStream()).and().isSaved();

		when(theRecord()).deleteVersion("0.2", bob).isSaved();

		assertThat(theRecordContent().getCurrentVersion()).has(pdf3HashAndLength()).has(version("0.3")).has(modifiedBy(bob))
				.has(modificationDatetime(teaOClock));

		assertThat(theRecordContentHistory()).hasSize(1);

		assertThat(theRecordContentHistory().get(0)).has(pdf1HashAndLength()).has(version("0.1")).has(modifiedBy(bob))
				.has(modificationDatetime(smashOClock));
		assertThatVaultOnlyContains(pdf1Hash, pdf3Hash);
	}

	@Test
	public void givenContentNonCheckedOutWhenFinalizingThenVersionLabelOfLastVersionModified()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();
		assertThat(theRecordContent().getCurrentVersion()).is(modifiedBy(bob)).has(modificationDatetime(smashOClock));

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsContentUpdated(alice, uploadPdf2InputStream()).and().isSaved();

		givenTimeIs(teaOClock);
		when(theRecord()).hasItsContentUpdated(bob, uploadPdf3InputStream()).and().isSaved();
		when(theRecord()).hasItsContentFinalized().and().isSaved();

		assertThat(theRecordContent().getCurrentVersion()).has(pdf3HashAndLength()).has(version("1.0")).has(modifiedBy(bob))
				.has(modificationDatetime(teaOClock));

		assertThat(theRecordContentHistory()).hasSize(2);
		assertThat(theRecordContentHistory().get(0)).has(pdf1HashAndLength()).has(version("0.1")).has(modifiedBy(bob))
				.has(modificationDatetime(smashOClock));
		assertThat(theRecordContentHistory().get(1)).has(pdf2HashAndLength()).has(version("0.2")).has(modifiedBy(alice))
				.has(modificationDatetime(shishOClock));
		assertThatVaultOnlyContains(pdf1Hash, pdf2Hash, pdf3Hash);
	}

	@Test
	public void givenFileNameAndDocumentTypeModifiedMultipleTimes()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "1.pdf", uploadPdf1InputStream())).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsContentUpdatedWithName(alice, uploadDocx1InputStream(), "2.docx").and().isSaved();

		givenTimeIs(teaOClock);
		when(theRecord()).hasItsContentUpdatedWithName(alice, uploadPdf2InputStream(), "3.pdf").and().isSaved();

		Content c = theRecordContent();
		c.getHistoryVersions();
		assertThat(c.getCurrentVersion()).has(pdf2HashAndLength()).has(filename("3.pdf")).has(pdfMimetype());
		assertThat(theRecordContentHistory()).hasSize(2);
		assertThat(theRecordContentHistory().get(0)).has(pdf1HashAndLength()).has(filename("1.pdf")).has(pdfMimetype());
		assertThat(theRecordContentHistory().get(1)).has(docx1HashAndLength()).has(filename("2.docx")).has(docsMimetype());

		assertThatVaultOnlyContains(pdf1Hash, docx1Hash, pdf2Hash);
	}

	@Test
	public void givenAContentIsReplacedWithAVersionConcentThenAllVersionsAreDeletedExceptTheGivenVersion()
			throws Exception {
		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "1.pdf", uploadPdf1InputStream())).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsContentRenamedTo("2.docx").hasItsContentUpdated(alice, uploadDocx1InputStream()).and().isSaved();

		givenTimeIs(teaOClock);
		when(theRecord()).hasItsContentRenamedTo("3.pdf").hasItsContentUpdated(alice, uploadPdf2InputStream()).and().isSaved();

		when(theRecord()).withSingleValueContent(contentManager.createMinor(bob, "ze.pdf", uploadPdf2InputStream())).isSaved();

		assertThatVaultOnlyContains(pdf2Hash);
	}

	@Test
	public void givenAContentIsDeletedThenAllVersionsDeletedExceptThoseInOtherRecords()
			throws Exception {

		givenAnotherRecord().withSingleValueContent(contentManager.createMinor(bob, "1.pdf", uploadPdf2InputStream())).isSaved();
		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "1.pdf", uploadPdf1InputStream())).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsContentRenamedTo("2.pdf").hasItsContentUpdated(alice, uploadPdf2InputStream()).and().isSaved();

		givenTimeIs(teaOClock);
		when(theRecord()).hasItsContentRenamedTo("3.docx").hasItsContentUpdated(alice, uploadDocx1InputStream()).and().isSaved();

		when(theRecord()).withSingleValueContent(null).isSaved();

		assertThatVaultOnlyContains(pdf2Hash);
	}

	@Test
	public void givenARecordIsLogicallyDeletedThenPhysicallyDeletedThenAllContentVersionsAreAvailableBeforePhysicalDelete()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "1.pdf", uploadPdf1InputStream())).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsContentRenamedTo("2.pdf").hasItsContentUpdated(alice, uploadPdf2InputStream()).and().isSaved();

		givenTimeIs(teaOClock);
		when(theRecord()).hasItsContentRenamedTo("3.docx").hasItsContentUpdated(alice, uploadDocx1InputStream()).and().isSaved();

		givenAnotherRecord().withSingleValueContent(contentManager.createMinor(bob, "2.docx", uploadDocx2InputStream()))
				.withParent(theRecord()).isSaved();

		recordServices.logicallyDelete(theRecord(), bob);
		assertThatVaultOnlyContains(pdf1Hash, pdf2Hash, docx1Hash, docx2Hash);
		givenTimeIs(shishOClock);

		recordServices.physicallyDelete(theRecord(), bob);
		assertThatVaultOnlyContains();
	}

	@Test
	public void whenModifyingContentAsFinalizedThenContentHashModifiedAndVersionFinalized()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsContentRenamedTo("ZeDoc.docx").and().
				hasItsContentUpdatedAndFinalized(alice, uploadDocx1InputStream()).and().isSaved();

		assertThat(theRecordContent().getCurrentVersion()).has(docsMimetype()).has(filename("ZeDoc.docx"))
				.has(docx1HashAndLength())
				.has(version("1.0")).has(modifiedBy(alice)).has(modificationDatetime(shishOClock));

		assertThat(theRecordContent()).is(notCheckedOut());
		assertThatVaultOnlyContains(docx1Hash, pdf1Hash);
	}

	@Test
	public void whenCheckoutContentThenUserIsCheckoutUser()
			throws RecordServicesException {

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).contentCheckedOutBy(alice).and().isSaved();

		assertThat(theRecordContent().getCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));
		assertThat(theRecordContent()).is(checkedOutBy(alice, shishOClock));

	}

	@Test
	public void givenCheckedOutContentWhenCheckinThenNothingHasChanged()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).and()
				.contentCheckedOutBy(alice).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).contentCheckedIn().and().isSaved();

		assertThat(theRecordContent().getCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.has(
						version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));
		assertThat(theRecordContent()).is(notCheckedOut());
		assertThatVaultOnlyContains(pdf1Hash);

	}

	@Test
	public void givenCheckedOutContentWhenCancelCheckinThenNothingHasChanged()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).and()
				.contentCheckedOutBy(alice).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).contentCheckedInCancelled().and().isSaved();

		assertThat(theRecordContent().getCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.has(
						version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));
		assertThat(theRecordContent()).is(notCheckedOut());
		assertThatVaultOnlyContains(pdf1Hash);

	}

	@Test
	public void givenMutlipleCollectionsWhenDeleteUnreferencedThenOnlyDeleteUnreferencedInBothCollections()
			throws Exception {
		givenTimeIs(shishOClock);
		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();
		givenAnotherCollectionRecord()
				.withSingleValueContent(contentManager.createMinor(bobInAnotherCollection, "ZePdf.pdf", uploadPdf2InputStream()))
				.isSaved();
		uploadPdf3InputStream();

		assertThatVaultOnlyContains(pdf1Hash, pdf2Hash);

	}

	@Test
	public void givenCheckedOutContentWhenCheckinInAsMinorVersionThenVersionIsNotFinalized()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).contentCheckedOutBy(alice).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).contentCheckedInAsMinor(uploadPdf2InputStream()).and().isSaved();

		assertThat(theRecordContentCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf2HashAndLength())
				.has(version("0.2")).has(modifiedBy(alice)).has(modificationDatetime(shishOClock));

		assertThat(theRecordContentHistory()).hasSize(1);
		assertThat(theRecordContentHistory().get(0)).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));
		assertThat(theRecordContent()).is(notCheckedOut());
		assertThatVaultOnlyContains(pdf1Hash, pdf2Hash);
	}

	@Test
	public void givenMinorEmptyVersionCheckedOutContentWhenCheckinInAsMinorVersionThenVersionHas0_1Version()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createEmptyMinor(alice, "ZePdf.pdf", uploadPdf1InputStream())).contentCheckedOutBy(alice)
				.isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).contentCheckedInAsMinor(uploadPdf2InputStream()).and().isSaved();

		assertThat(theRecordContentCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf2HashAndLength())
				.has(version("0.1")).has(modifiedBy(alice)).has(modificationDatetime(shishOClock));

		assertThat(theRecordContentHistory()).isEmpty();
		assertThat(theRecordContent()).is(notCheckedOut());
		assertThat(theRecordContent()).isNot(emptyVersion);
		assertThatVaultOnlyContains(pdf2Hash);
	}

	@Test
	public void givenMinorEmptyVersionCheckedOutContentWhenUpdatingTwiceThenCheckinInAsMinorVersionThenVersionHas0_1Version()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createEmptyMinor(alice, "ZePdf.pdf", uploadPdf1InputStream())).contentCheckedOutBy(alice)
				.isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsCheckedOutContentUpdatedWith(uploadPdf2InputStream()).and().isSaved();
		assertThat(theRecordContentHistory()).isEmpty();
		assertThat(theRecordContent()).is(checkedOutBy(alice, smashOClock));
		assertThat(theRecordContent()).is(emptyVersion);

		givenTimeIs(teaOClock);
		when(theRecord()).hasItsCheckedOutContentUpdatedWith(uploadPdf3InputStream()).and().isSaved();
		assertThat(theRecordContentHistory()).isEmpty();
		assertThat(theRecordContent()).is(checkedOutBy(alice, smashOClock));
		assertThat(theRecordContent()).is(emptyVersion);

		givenTimeIs(teaOClock.plusDays(1));
		when(theRecord()).contentCheckedIn().and().isSaved();

		assertThat(theRecordContentCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf3HashAndLength())
				.has(version("0.1")).has(modifiedBy(alice)).has(modificationDatetime(teaOClock));
		assertThat(theRecordContentHistory()).isEmpty();
		assertThat(theRecordContent()).is(notCheckedOut());
		assertThat(theRecordContent()).isNot(emptyVersion);

		assertThatVaultOnlyContains(pdf3Hash);
	}

	@Test
	public void givenMinorEmptyContentWhenUpdatingThen0_1Version()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createEmptyMinor(alice, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsContentUpdated(bob, uploadPdf2InputStream()).and().isSaved();

		assertThat(theRecordContentCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf2HashAndLength())
				.has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(shishOClock));
		assertThat(theRecordContentHistory()).isEmpty();
		assertThat(theRecordContent()).is(notCheckedOut());
		assertThat(theRecordContent()).isNot(emptyVersion);
	}

	@Test
	public void givenMinorEmptyVersionCheckedOutContentWhenCheckinInAsMajorVersionThenVersionHas1_0Version()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createEmptyMinor(alice, "ZePdf.pdf", uploadPdf1InputStream())).contentCheckedOutBy(alice)
				.isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).contentCheckedInAsMajor(uploadPdf2InputStream()).and().isSaved();

		assertThat(theRecordContentCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf2HashAndLength())
				.has(version("1.0")).has(modifiedBy(alice)).has(modificationDatetime(shishOClock));

		assertThat(theRecordContentHistory()).isEmpty();
		assertThat(theRecordContent()).is(notCheckedOut());
		assertThat(theRecordContent()).isNot(emptyVersion);
		assertThatVaultOnlyContains(pdf2Hash);
	}

	@Test
	public void givenMinorEmptyVersionCheckedOutContentWhenSavingModifiedVersionVersionThenHas0_1Version()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createEmptyMinor(alice, "ZePdf.pdf", uploadPdf1InputStream())).contentCheckedOutBy(alice)
				.isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsCheckedOutContentUpdatedWith(uploadPdf2InputStream()).and().isSaved();

		assertThat(theRecordContentCurrentCheckedOutVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf"))
				.has(pdf2HashAndLength()).has(version("0.1")).has(modifiedBy(alice)).has(modificationDatetime(shishOClock));

		assertThat(theRecordContentHistory()).isEmpty();
		assertThat(theRecordContent()).is(checkedOutBy(alice, smashOClock));
		assertThat(theRecordContent()).is(emptyVersion);
		assertThatVaultOnlyContains(pdf1Hash, pdf2Hash);

	}

	@Test
	public void givenCheckedOutContentWhenCheckinInAsMajorVersionThenVersionIsFinalized()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).contentCheckedOutBy(alice).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).contentCheckedInAsMajor(uploadPdf2InputStream()).and().isSaved();

		assertThat(theRecordContentCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf2HashAndLength())
				.has(version("1.0")).has(modifiedBy(alice)).has(modificationDatetime(shishOClock));

		assertThat(theRecordContentHistory()).hasSize(1);
		assertThat(theRecordContentHistory().get(0)).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));
		assertThat(theRecordContent()).is(notCheckedOut());
		assertThatVaultOnlyContains(pdf1Hash, pdf2Hash);
	}

	@Test
	public void givenCheckedOutContentWhenUpdatingContentThenVersionIsIncrementedAndStillCheckedOut()
			throws Exception {

		givenTimeIs(teaOClock);
		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).contentCheckedOutBy(alice).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsCheckedOutContentUpdatedWith(uploadPdf2InputStream()).and().isSaved();

		assertThat(theRecordContentCurrentCheckedOutVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(
				pdf2HashAndLength())
				.has(version("0.2")).has(modifiedBy(alice)).has(modificationDatetime(shishOClock));

		assertThat(theRecordContentCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(teaOClock));
		assertThat(theRecordContent()).is(checkedOutBy(alice, teaOClock));
		assertThatVaultOnlyContains(pdf1Hash, pdf2Hash);
		assertThat(theRecordContentHistory()).isEmpty();
	}

	@Test
	public void givenCheckedOutContentWhenUpdatingContentMultipleTimeThenVersionIsIncrementedOnlyOnceAndOnlyLastConcentIsConserved()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).contentCheckedOutBy(alice).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsCheckedOutContentUpdatedWith(uploadPdf2InputStream()).and().isSaved();
		assertThat(theRecordContentCurrentCheckedOutVersion()).has(pdf2HashAndLength()).has(version("0.2"))
				.has(modificationDatetime(shishOClock));

		givenTimeIs(teaOClock);
		when(theRecord()).hasItsCheckedOutContentUpdatedWith(uploadPdf3InputStream()).and().isSaved();
		assertThat(theRecordContentCurrentCheckedOutVersion()).has(pdf3HashAndLength()).has(version("0.2"))
				.has(modificationDatetime(teaOClock));

		assertThat(theRecordContentHistory()).isEmpty();
		assertThat(theRecordContentCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));

		assertThat(theRecordContent()).is(checkedOutBy(alice, smashOClock));
		assertThatVaultOnlyContains(pdf1Hash, pdf3Hash);
	}

	@Test
	public void givenCheckedOutContentWhenUpdatingContentAndCheckinThenUpdatedContentAndMinorVersion()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).contentCheckedOutBy(alice).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsCheckedOutContentUpdatedWith(uploadPdf2InputStream()).and().isSaved();
		when(theRecord()).contentCheckedIn().isSaved();

		assertThat(theRecordContentCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf2HashAndLength())
				.has(version("0.2")).has(modifiedBy(alice)).has(modificationDatetime(shishOClock));

		assertThat(theRecordContentHistory()).hasSize(1);
		assertThat(theRecordContentHistory().get(0)).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));
		assertThat(theRecordContent()).is(notCheckedOut());
		assertThatVaultOnlyContains(pdf1Hash, pdf2Hash);
	}

	@Test
	public void givenCheckedOutContentWhenUpdatingContentAndCancelCheckinThenNothingChanged()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).contentCheckedOutBy(alice).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsCheckedOutContentUpdatedWith(uploadPdf2InputStream()).and().isSaved();
		when(theRecord()).contentCheckedInCancelled().isSaved();

		assertThat(theRecordContentCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));
		assertThat(theRecordContentHistory()).isEmpty();
		assertThat(theRecordContent()).is(notCheckedOut());
		assertThatVaultOnlyContains(pdf1Hash);
	}

	@Test
	public void givenCheckedOutContentWhenUpdatingContentAndFinalisingThenUpdatedContentAndMinorVersion()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).contentCheckedOutBy(alice).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsCheckedOutContentUpdatedWith(uploadPdf2InputStream()).and().isSaved();
		when(theRecord()).finalizeVersion().isSaved();

		assertThat(theRecordContentCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf2HashAndLength())
				.has(version("1.0")).has(modifiedBy(alice)).has(modificationDatetime(shishOClock));

		assertThat(theRecordContentHistory()).hasSize(1);
		assertThat(theRecordContentHistory().get(0)).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));
		assertThat(theRecordContent()).is(notCheckedOut());
		assertThatVaultOnlyContains(pdf1Hash, pdf2Hash);
	}

	@Test
	public void givenCheckedOutContentWhenUpdatingContentAndCheckinWithNewContentAsMajorThenMajorAndFirstUpdatedVersionDeleted()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).contentCheckedOutBy(alice).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsCheckedOutContentUpdatedWith(uploadPdf2InputStream()).and().isSaved();
		when(theRecord()).contentCheckedInAsMajor(uploadPdf3InputStream()).isSaved();

		assertThat(theRecordContentCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf3HashAndLength())
				.has(version("1.0")).has(modifiedBy(alice)).has(modificationDatetime(shishOClock));

		assertThat(theRecordContentHistory()).hasSize(1);
		assertThat(theRecordContentHistory().get(0)).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));
		assertThat(theRecordContent()).is(notCheckedOut());
		assertThatVaultOnlyContains(pdf1Hash, pdf3Hash);
	}

	@Test
	public void givenCheckedOutContentWhenUpdatingContentAndCheckinWithNewContentAsMinorThenMajorAndFirstUpdatedVersionDeleted()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).contentCheckedOutBy(alice).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsCheckedOutContentUpdatedWith(uploadPdf2InputStream()).and().isSaved();
		when(theRecord()).contentCheckedInAsMinor(uploadPdf3InputStream()).isSaved();

		assertThat(theRecordContentCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf3HashAndLength())
				.has(version("0.2")).has(modifiedBy(alice)).has(modificationDatetime(shishOClock));

		assertThat(theRecordContentHistory()).hasSize(1);
		assertThat(theRecordContentHistory().get(0)).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.has(version("0.1")).has(modifiedBy(bob)).has(modificationDatetime(smashOClock));
		assertThat(theRecordContent()).is(notCheckedOut());
		assertThatVaultOnlyContains(pdf1Hash, pdf3Hash);
	}

	@Test
	public void givenCheckedOutContentWhenUpdatingContentWithIncorrectMethodThenExceptionAndNothingIsChanged()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).contentCheckedOutBy(alice).isSaved();

		givenTimeIs(shishOClock);
		when(theRecord()).hasItsCheckedOutContentUpdatedWith(uploadPdf2InputStream()).and().isSaved();
		assertThat(theRecordContentCurrentCheckedOutVersion()).has(pdf2HashAndLength()).has(version("0.2"))
				.has(modificationDatetime(shishOClock));

		givenTimeIs(teaOClock);
		RecordPreparation recordPreparation = when(theRecord());
		try {
			recordPreparation.hasItsContentUpdated(alice, uploadPdf3InputStream());
			fail("ContentImplRuntimeException_ContentMustNotBeCheckedOut expected");
		} catch (ContentImplRuntimeException_ContentMustNotBeCheckedOut e) {
			//OK
		}
		recordPreparation.isSaved();
		try {
			recordPreparation.hasItsContentUpdatedAndFinalized(alice, uploadPdf3InputStream());
			fail("ContentImplRuntimeException_ContentMustNotBeCheckedOut expected");
		} catch (ContentImplRuntimeException_ContentMustNotBeCheckedOut e) {
			//OK
		}
		recordPreparation.isSaved();

		assertThat(theRecordContentCurrentCheckedOutVersion()).has(pdf2HashAndLength()).has(version("0.2"))
				.has(modificationDatetime(shishOClock));
		assertThatVaultOnlyContains(pdf1Hash, pdf2Hash);
	}

	@Test
	public void whenUploadingNeverUsedContentWhenCleaningVaultThenContentRemoved()
			throws Exception {

		givenTimeIs(shishOClock);

		assertThat(getModelLayerFactory().getConfiguration().getDelayBeforeDeletingUnreferencedContents()).isEqualTo(
				org.joda.time.Duration.standardMinutes(42)
		);

		assertThat(getModelLayerFactory().getConfiguration().getUnreferencedContentsThreadDelayBetweenChecks()).isEqualTo(
				org.joda.time.Duration.standardHours(10)
		);

		uploadPdf1InputStream();
		uploadPdf2InputStream();
		uploadDocx1InputStream();

		givenTimeIs(shishOClock.plusMinutes(41));
		contentManager.deleteUnreferencedContents();
		assertThatVaultOnlyContainsWithoutAdvancingTime(pdf1Hash, pdf2Hash, docx1Hash);

		givenTimeIs(shishOClock.plusMinutes(42));
		assertThatVaultOnlyContainsWithoutAdvancingTime();
	}

	@Test
	public void givenAContentIsNotCheckedOutThenCannotCheckinAsMajor()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();

		givenTimeIs(shishOClock);
		RecordPreparation recordPreparation = when(theRecord());
		try {
			recordPreparation.contentCheckedInAsMajor(uploadPdf3InputStream());
			fail("ContentImplRuntimeException_ContentMustBeCheckedOut expected");
		} catch (ContentImplRuntimeException_ContentMustBeCheckedOut e) {
			//OK
		}
		recordPreparation.isSaved();
		assertThat(theRecordContentCurrentVersion()).has(pdf1HashAndLength()).has(modificationDatetime(smashOClock));
	}

	@Test
	public void givenAContentIsNotCheckedOutThenCannotCheckinAsMinor()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();

		givenTimeIs(shishOClock);
		RecordPreparation recordPreparation = when(theRecord());
		try {
			recordPreparation.contentCheckedInAsMinor(uploadPdf3InputStream());
			fail("ContentImplRuntimeException_ContentMustBeCheckedOut expected");
		} catch (ContentImplRuntimeException_ContentMustBeCheckedOut e) {
			//OK
		}
		recordPreparation.isSaved();
		assertThat(theRecordContentCurrentVersion()).has(pdf1HashAndLength()).has(modificationDatetime(smashOClock));
	}

	@Test
	public void givenAContentIsNotCheckedOutThenCannotCheckinAsMinorWithNewFilename()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();

		givenTimeIs(shishOClock);
		RecordPreparation recordPreparation = when(theRecord());
		try {
			recordPreparation.contentCheckedInAsMinorWithNewName(uploadPdf3InputStream(), "newFile.pdf");
			fail("ContentImplRuntimeException_ContentMustBeCheckedOut expected");
		} catch (ContentImplRuntimeException_ContentMustBeCheckedOut e) {
			//OK
		}
		recordPreparation.isSaved();
		assertThat(theRecordContentCurrentVersion()).has(pdf1HashAndLength()).has(modificationDatetime(smashOClock));
	}

	@Test
	public void givenAContentIsNotCheckedOutThenCannotCheckinAsSameVersionWithNewFilename()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();

		givenTimeIs(shishOClock);
		RecordPreparation recordPreparation = when(theRecord());
		try {
			recordPreparation.contentCheckedInAsSameVersionWithNewName(uploadPdf3InputStream(), "newFile.pdf");
			fail("ContentImplRuntimeException_ContentMustBeCheckedOut expected");
		} catch (ContentImplRuntimeException_ContentMustBeCheckedOut e) {
			//OK
		}
		recordPreparation.isSaved();
		assertThat(theRecordContentCurrentVersion()).has(pdf1HashAndLength()).has(modificationDatetime(smashOClock));
	}

	@Test
	public void givenAContentInMinorVersionIsCheckedOutWhenCheckinAsSameVersionWithNewFilenameThenUpdatedAndAtSameVersion()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream()).checkOut(alice)).isSaved();

		givenTimeIs(shishOClock);
		RecordPreparation recordPreparation = when(theRecord());
		recordPreparation.contentCheckedInAsSameVersionWithNewName(uploadPdf3InputStream(), "newFile.pdf");
		recordPreparation.isSaved();

		assertThat(theRecordContentCurrentVersion()).has(pdf3HashAndLength()).has(modificationDatetime(shishOClock))
				.has(filename("newFile.pdf")).has(version("0.1"));
	}

	@Test
	public void givenAContentInMajorVersionIsCheckedOutWhenCheckinAsSameVersionWithNewFilenameThenUpdatedAndAtSameVersion()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMajor(bob, "ZePdf.pdf", uploadPdf1InputStream()).checkOut(alice)).isSaved();

		givenTimeIs(shishOClock);
		RecordPreparation recordPreparation = when(theRecord());
		recordPreparation.contentCheckedInAsSameVersionWithNewName(uploadPdf3InputStream(), "newFile.pdf");
		recordPreparation.isSaved();

		assertThat(theRecordContentCurrentVersion()).has(pdf3HashAndLength()).has(modificationDatetime(shishOClock))
				.has(filename("newFile.pdf")).has(version("1.0"));
	}

	@Test
	public void givenAContentIsNotCheckedOutThenCannotCheckin()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();

		givenTimeIs(shishOClock);
		RecordPreparation recordPreparation = when(theRecord());
		try {
			recordPreparation.contentCheckedIn().isSaved();
			fail("ContentImplRuntimeException_ContentMustBeCheckedOut expected");
		} catch (ContentImplRuntimeException_ContentMustBeCheckedOut e) {
			//OK
		}
		recordPreparation.isSaved();
		assertThat(theRecordContentCurrentVersion()).has(pdf1HashAndLength()).has(modificationDatetime(smashOClock));
	}

	@Test
	public void givenAContentIsNotCheckedOutThenCannotCancelCheckin()
			throws Exception {

		Record record = givenRecord().withSingleValueContent(
				contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream())).isSaved();

		givenTimeIs(shishOClock);
		RecordPreparation recordPreparation = when(theRecord());
		try {
			recordPreparation.contentCheckedInCancelled();
			fail("ContentImplRuntimeException_ContentMustBeCheckedOut expected");
		} catch (ContentImplRuntimeException_ContentMustBeCheckedOut e) {
			//OK
		}
		recordPreparation.isSaved();
		assertThat(theRecordContentCurrentVersion()).has(pdf1HashAndLength()).has(modificationDatetime(smashOClock));
	}

	@Test
	public void givenAContentIsAlreadyCheckedOutThenCannotCheck()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createMinor(bob, "ZePdf.pdf", uploadPdf1InputStream()))
				.contentCheckedOutBy(alice).isSaved();

		givenTimeIs(shishOClock);
		RecordPreparation recordPreparation = when(theRecord());
		try {
			recordPreparation.contentCheckedOutBy(alice);
			fail("ContentImplRuntimeException_ContentMustNotBeCheckedOut expected");
		} catch (ContentImplRuntimeException_ContentMustNotBeCheckedOut e) {
			//OK
		}
		recordPreparation.isSaved();
		assertThat(theRecordContentCurrentVersion()).has(pdf1HashAndLength()).has(modificationDatetime(smashOClock));
	}

	@Test
	public void givenARecordHasAParsedContentHigherThanTheLimitThenTrimmed()
			throws Exception {
		givenConfig(PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS, 3000);
		SystemConfigurationsManager manager = getModelLayerFactory().getSystemConfigurationsManager();
		givenSingleValueContentMetadataIsSearchable();

		//default limit is 3mo
		ContentVersionDataSummary zeContent = contentManager.upload(getTestResourceInputStream("fileWith4MoOfParsedContent.txt"));
		ParsedContent parsedContent = contentManager.getParsedContent(zeContent.getHash());
		givenRecord().withSingleValueContent(contentManager.createMajor(alice, "file.txt", zeContent)).isSaved();

		assertThat(parsedContent.getParsedContent()).contains("Allo", "Hola", "Test").doesNotContain("Cafe");
		assertThatRecordCanBeObtainedWithKeywords("Allo", "Hola", "Test");
		assertThatRecordCannotBeObtainedWithKeywords("Cafe");

		manager.setValue(PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS, 4000);
		contentManager.reparse(zeContent.getHash());

		parsedContent = contentManager.getParsedContent(zeContent.getHash());
		assertThat(parsedContent.getParsedContent()).contains("Allo", "Hola", "Cafe", "Test");
		assertThatRecordCanBeObtainedWithKeywords("Allo", "Hola", "Cafe", "Test");

		manager.setValue(PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS, 2000);
		contentManager.reparse(zeContent.getHash());

		parsedContent = contentManager.getParsedContent(zeContent.getHash());
		assertThat(parsedContent.getParsedContent()).contains("Allo", "Test").doesNotContain("Cafe").doesNotContain("Hola");
		assertThatRecordCanBeObtainedWithKeywords("Allo", "Test");
		assertThatRecordCannotBeObtainedWithKeywords("Cafe", "Hola");
	}

	@Test
	public void givenTheRequireConversionFlagIsActivatedWhenCheckContentsToConvertThenConvertAndGenerateThumbnail()
			throws Exception {
		givenConfig(ConstellioEIMConfigs.ENABLE_THUMBNAIL_GENERATION, true);

		ContentVersionDataSummary zeContent = uploadDocx1InputStream();
		givenRecord().withSingleValueContent(contentManager.createMajor(alice, "file.docx", zeContent))
				.withRequireConversionFlag(true).isSaved();

		assertThat(theRecord().<Boolean>get(Schemas.MARKED_FOR_PREVIEW_CONVERSION)).isEqualTo(Boolean.TRUE);
		assertThat(contentManager.hasContentPreview(zeContent.getHash())).isFalse();
		try {
			contentManager.getContentPreviewInputStream(zeContent.getHash(), SDK_STREAM);
			fail("Exception expected");
		} catch (ContentManagerRuntimeException_ContentHasNoPreview e) {
			//OK
		}

		try {
			contentManager.getContentThumbnailInputStream(zeContent.getHash(), SDK_STREAM);
			fail("Exception expected");
		} catch (ContentManagerRuntimeException_ContentHasNoThumbnail e) {
			//OK
		}

		contentManager.convertPendingContentForPreview();

		assertThat(theRecord().<Boolean>get(Schemas.MARKED_FOR_PREVIEW_CONVERSION)).isNull();
		assertThat(contentManager.hasContentPreview(zeContent.getHash())).isTrue();
		assertThat(contentManager.hasContentThumbnail(zeContent.getHash())).isTrue();

		InputStream in = contentManager.getContentPreviewInputStream(zeContent.getHash(), SDK_STREAM);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);
		assertThat(out.toByteArray().length == 42);

	}

	@Test
	public void givenARecordCannotBeConvertedThenFlagRemovedAndStillNotAvailableForePreview()
			throws Exception {

		ContentVersionDataSummary zeContent = uploadACorruptedDocx();
		givenRecord().withSingleValueContent(contentManager.createMajor(alice, "file.docx", zeContent))
				.withRequireConversionFlag(true).isSaved();

		assertThat(theRecord().<Boolean>get(Schemas.MARKED_FOR_PREVIEW_CONVERSION)).isEqualTo(Boolean.TRUE);
		assertThat(contentManager.hasContentPreview(zeContent.getHash())).isFalse();
		try {
			contentManager.getContentPreviewInputStream(zeContent.getHash(), SDK_STREAM);
			fail("Exception expected");
		} catch (ContentManagerRuntimeException_ContentHasNoPreview e) {
			//OK
		}

		contentManager.convertPendingContentForPreview();

		assertThat(theRecord().<Boolean>get(Schemas.MARKED_FOR_PREVIEW_CONVERSION)).isNull();
		assertThat(contentManager.hasContentPreview(zeContent.getHash())).isFalse();
		try {
			contentManager.getContentPreviewInputStream(zeContent.getHash(), SDK_STREAM);
			fail("Exception expected");
		} catch (ContentManagerRuntimeException_ContentHasNoPreview e) {
			//OK
		}

	}

	@Test
	public void givenTheRequireConversionFlagIsActivatedAndThumbnailGenerationFlagIsDisabledThenNoThumbnail()
			throws Exception {
		givenConfig(ConstellioEIMConfigs.ENABLE_THUMBNAIL_GENERATION, false);

		ContentVersionDataSummary zeContent = uploadDocx1InputStream();
		givenRecord().withSingleValueContent(contentManager.createMajor(alice, "file.docx", zeContent))
				.withRequireConversionFlag(true).isSaved();

		assertThat(theRecord().<Boolean>get(Schemas.MARKED_FOR_PREVIEW_CONVERSION)).isEqualTo(Boolean.TRUE);
		try {
			contentManager.getContentThumbnailInputStream(zeContent.getHash(), SDK_STREAM);
			fail("Exception expected");
		} catch (ContentManagerRuntimeException_ContentHasNoThumbnail e) {
			//OK
		}

		contentManager.convertPendingContentForPreview();

		assertThat(theRecord().<Boolean>get(Schemas.MARKED_FOR_PREVIEW_CONVERSION)).isNull();
		assertThat(contentManager.hasContentThumbnail(zeContent.getHash())).isFalse();
	}

	@Test
	public void givenOnlyAMinorVersionWhenGetLatestMajorThenReturnNull()
			throws Exception {

		doReturn(true).when(userPermissionsChecker).globally();
		doReturn(userPermissionsChecker).when(bob).has(CorePermissions.DELETE_CONTENT_VERSION);
		Content content = contentManager.createMinor(alice, "ZePdf.pdf", uploadPdf1InputStreamWithoutParsing());
		givenRecord().withSingleValueContent(content).isSaved();

		assertThat(content.getLastMajorContentVersion()).isNull();
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo("0.1");

		when(theRecord()).hasItsContentUpdated(alice, uploadPdf2InputStream()).and().isSaved();
		assertThat(theRecordContent().getLastMajorContentVersion()).isNull();
		assertThat(theRecordContent().getCurrentVersion().getVersion()).isEqualTo("0.2");

		when(theRecord()).hasItsContentFinalized().isSaved();
		assertThat(theRecordContent().getLastMajorContentVersion().getVersion()).isEqualTo("1.0");
		assertThat(theRecordContent().getCurrentVersion().getVersion()).isEqualTo("1.0");

		when(theRecord()).hasItsContentUpdated(alice, uploadPdf3InputStream()).and().isSaved();
		assertThat(theRecordContent().getLastMajorContentVersion().getVersion()).isEqualTo("1.0");
		assertThat(theRecordContent().getCurrentVersion().getVersion()).isEqualTo("1.1");

		when(theRecord()).hasItsContentFinalized().isSaved();
		assertThat(theRecordContent().getLastMajorContentVersion().getVersion()).isEqualTo("2.0");
		assertThat(theRecordContent().getCurrentVersion().getVersion()).isEqualTo("2.0");

		when(theRecord()).hasItsContentUpdated(alice, uploadDocx1InputStream()).and().isSaved();
		assertThat(theRecordContent().getLastMajorContentVersion().getVersion()).isEqualTo("2.0");
		assertThat(theRecordContent().getCurrentVersion().getVersion()).isEqualTo("2.1");

		when(theRecord()).hasItsContentFinalized().isSaved();
		assertThat(theRecordContent().getLastMajorContentVersion().getVersion()).isEqualTo("3.0");
		assertThat(theRecordContent().getCurrentVersion().getVersion()).isEqualTo("3.0");

	}

	@Test
	public void whenCreateAndUpdateContentWithCustomVersionLabelThenUsed()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createWithVersion(bob, "ZePdf.pdf", uploadPdf1InputStream(), "3.5"))
				.isSaved();

		assertThat(theRecordContent()).isNot(emptyVersion);
		assertThat(theRecordContent().getCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.is(version("3.5")).is(modifiedBy(bob)).has(modificationDatetime(smashOClock));

		when(theRecord()).hasItsContentUpdatedWithVersionAndName(alice, uploadPdf2InputStream(), "42", "ZeNewPdf.pdf")
				.isSaved();

		when(theRecord()).hasItsContentUpdatedWithVersionAndName(alice, uploadPdf2InputStream(), "66.6", "ZeNewPdf.pdf")
				.isSaved();

		assertThat(theRecordContent().getCurrentVersion().getVersion()).isEqualTo("66.6");
		assertThat(theRecordContent().getHistoryVersions().get(1).getVersion()).isEqualTo("42.0");
		assertThat(theRecordContent().getHistoryVersions().get(0).getVersion()).isEqualTo("3.5");
	}

	@Test
	public void whenCreateAndUpdateContentWithBadCustomVersionThenException()
			throws Exception {

		try {
			givenRecord()
					.withSingleValueContent(contentManager.createWithVersion(bob, "ZePdf.pdf", uploadPdf1InputStream(), "3.5a"));

			fail("Invalid argument exception expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//oK
		}

		givenRecord().withSingleValueContent(contentManager.createWithVersion(bob, "ZePdf.pdf", uploadPdf1InputStream(), "3.5"))
				.isSaved();

		try {
			when(theRecord()).hasItsContentUpdatedWithVersionAndName(alice, uploadPdf2InputStream(), "42b", "ZeNewPdf.pdf");
			fail("Invalid argument exception expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//oK
		}

	}

	@Test
	public void whenCreateAndUpdateContentWithInferiorCustomVersionThenException()
			throws Exception {

		givenRecord().withSingleValueContent(contentManager.createWithVersion(bob, "ZePdf.pdf", uploadPdf1InputStream(), "3"))
				.isSaved();

		assertThat(theRecordContent().getCurrentVersion()).has(pdfMimetype()).has(filename("ZePdf.pdf")).has(pdf1HashAndLength())
				.is(version("3.0")).is(modifiedBy(bob)).has(modificationDatetime(smashOClock));

		try {
			when(theRecord()).hasItsContentUpdatedWithVersionAndName(alice, uploadPdf2InputStream(), "2.4", "ZeNewPdf.pdf");
			fail("Invalid argument exception expected");
		} catch (ContentImplRuntimeException_VersionMustBeHigherThanPreviousVersion e) {
			//oK
		}

	}

	//------------------------------------------------------------------

	private void assertThatRecordCanBeObtainedWithKeywords(String... keywords) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		for (String keyword : keywords) {
			assertThat(searchServices.search(new LogicalSearchQuery(fromAllSchemasIn(zeCollection).returnAll())
					.setFreeTextQuery(keyword))).isNotEmpty();
		}
	}

	private void assertThatRecordCannotBeObtainedWithKeywords(String... keywords) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		for (String keyword : keywords) {
			assertThat(searchServices.search(new LogicalSearchQuery(fromAllSchemasIn(zeCollection).returnAll())
					.setFreeTextQuery(keyword))).isEmpty();
		}
	}

	private void givenSingleValueContentMetadataIsSearchable() {
		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getMetadata(zeSchema.contentMetadata().getCode()).setSearchable(true);
			}
		});
		zeSchema = schemas.new ZeSchemaMetadatas();
	}

	private Condition<? super Content> notCheckedOut() {
		return new Condition<Content>() {
			@Override
			public boolean matches(Content value) {
				return value.getCheckoutUserId() == null && value.getCheckoutDateTime() == null
					   && value.getCurrentCheckedOutVersion() == null;
			}
		}.describedAs("notCheckedOut()");
	}

	private Condition<? super Content> checkedOutBy(final User user, final LocalDateTime localDateTime) {
		return new Condition<Content>() {
			@Override
			public boolean matches(Content value) {
				String userId = user == null ? null : user.getId();
				return localDateTime.equals(value.getCheckoutDateTime()) && LangUtils
						.areNullableEqual(userId, value.getCheckoutUserId());
			}
		}.describedAs("checkedOutBy(" + user + ", " + localDateTime + ")");
	}

	private Condition<? super ContentVersion> modifiedBy(final User user) {
		return new Condition<ContentVersion>() {
			@Override
			public boolean matches(ContentVersion value) {
				String userId = user == null ? null : user.getId();
				return LangUtils.areNullableEqual(userId, value.getModifiedBy());
			}
		}.describedAs("modifiedBy(" + user + ")");
	}

	private Condition<? super ContentVersion> modificationDatetime(final LocalDateTime dateTime) {
		return new Condition<ContentVersion>() {
			@Override
			public boolean matches(ContentVersion value) {
				return dateTime.equals(value.getLastModificationDateTime());
			}
		}.describedAs("modificationDatetime(" + dateTime + ")");
	}

	private Condition<? super ContentVersion> version(final String version) {
		return new Condition<ContentVersion>() {
			@Override
			public boolean matches(ContentVersion value) {
				return version.equals(value.getVersion());
			}
		}.describedAs("version(" + version + ")");
	}

	private Condition<? super ContentVersion> pdfMimetype() {
		return mimetype(PDF_MIMETYPE);
	}

	private Condition<? super ContentVersion> docsMimetype() {
		return mimetype(DOCX_MIMETYPE);
	}

	private Condition<? super ContentVersion> mimetype(final String mimetype) {
		return new Condition<ContentVersion>() {
			@Override
			public boolean matches(ContentVersion value) {
				return mimetype.equals(value.getMimetype());
			}
		}.describedAs("mimetype(" + mimetype + ")");
	}

	private Condition<? super ContentVersion> filename(final String filename) {
		return new Condition<ContentVersion>() {
			@Override
			public boolean matches(ContentVersion value) {
				return filename.equals(value.getFilename());
			}
		}.describedAs("filename(" + filename + ")");
	}

	private Condition<? super ContentVersion> comment(final String comment) {
		return new Condition<ContentVersion>() {
			@Override
			public boolean matches(ContentVersion value) {
				if (comment == null) {
					return value.getComment() == null || value.getComment().equals("");
				} else {
					return comment.equals(value.getComment());
				}
			}
		}.describedAs("comment(" + comment + ")");
	}

	private Condition<? super ContentVersion> pdf1HashAndLength() {
		return hash(pdf1Hash, pdf1Length);
	}

	private Condition<? super ContentVersion> pdf2HashAndLength() {
		return hash(pdf2Hash, pdf2Length);
	}

	private Condition<? super ContentVersion> pdf3HashAndLength() {
		return hash(pdf3Hash, pdf3Length);
	}

	private Condition<? super ContentVersion> docx1HashAndLength() {
		return hash(docx1Hash, docx1Length);
	}

	private Condition<? super ContentVersion> docx2HashAndLength() {
		return hash(docx2Hash, docx2Length);
	}

	private Condition<? super ContentVersion> hash(final String hash, final long length) {
		return new Condition<ContentVersion>() {
			@Override
			public boolean matches(ContentVersion value) {
				return hash.equals(value.getHash()) && length == value.getLength();
			}
		}.describedAs("hash(" + hash + ")");
	}

	private RecordPreparation givenRecord() {
		Record record = new TestRecord(zeSchema, "zeRecord");
		return new RecordPreparation(record);
	}

	private RecordPreparation givenRecordWithId(String id) {
		Record record = new TestRecord(zeSchema, id);
		return new RecordPreparation(record);
	}

	private RecordPreparation givenAnotherCollectionRecord() {
		Record record = new TestRecord(anotherCollectionSchema, "anotherCollectionRecord");
		return new RecordPreparation(record);
	}

	private Record theRecord() {
		return recordServices.getDocumentById("zeRecord");
	}

	private Content theRecordContent() {
		return theRecord().get(zeSchema.contentMetadata());
	}

	private List<ContentVersion> theRecordContentHistory() {
		return theRecordContent().getHistoryVersions();
	}

	private ContentVersion theRecordContentCurrentVersion() {
		return theRecordContent().getCurrentVersion();
	}

	private ContentVersion theRecordContentCurrentCheckedOutVersion() {
		return theRecordContent().getCurrentCheckedOutVersion();
	}

	private Content theRecordFirstMultivalueContent() {
		List<Content> contents = theRecord().getList(zeSchema.contentListMetadata());
		return contents.get(0);
	}

	private Content theRecordSecondAndLastMultivalueContent() {
		List<Content> contents = theRecord().getList(zeSchema.contentListMetadata());
		assertThat(contents).hasSize(2);
		return contents.get(1);
	}

	private RecordPreparation givenAnotherRecord() {
		Record record = new TestRecord(zeSchema, "anotherRecord");
		return new RecordPreparation(record);
	}

	private RecordPreparation given(Record record) {
		return new RecordPreparation(record);
	}

	private RecordPreparation when(Record record) {
		return new RecordPreparation(record);
	}

	private Record createZeRecord() {
		return new TestRecord(zeSchema, "zeRecord");
	}

	private ContentVersionDataSummary getPdf1InputStream() {
		return new ContentVersionDataSummary(pdf1Hash, "application/pdf", 170039);
	}

	private ContentVersionDataSummary getPdf2InputStream() {
		return new ContentVersionDataSummary(pdf2Hash, "application/pdf", 167347);
	}

	private ContentVersionDataSummary getPdf3InputStream() {
		return new ContentVersionDataSummary(pdf3Hash, "application/pdf", 141667);
	}

	private ContentVersionDataSummary getDocx1InputStream() {
		return new ContentVersionDataSummary(docx1Hash,
				"application/vnd.openxmlformats-officedocument.wordprocessingml.document", 27055);
	}

	private ContentVersionDataSummary getDocx2InputStream() {
		return new ContentVersionDataSummary(docx2Hash,
				"application/vnd.openxmlformats-officedocument.wordprocessingml.document", 27325);
	}

	private ContentVersionDataSummary uploadACorruptedDocx() {
		return contentManager.upload(getTestResourceInputStream("corrupted.docx"), new UploadOptions())
				.getContentVersionDataSummary();
	}

	private ContentVersionDataSummary uploadPdf1InputStream() {
		return contentManager.upload(getTestResourceInputStream("pdf1.pdf"), new UploadOptions())
				.getContentVersionDataSummary();
	}

	private ContentVersionDataSummary uploadPdf1InputStream(UploadOptions options) {
		return contentManager.upload(getTestResourceInputStream("pdf1.pdf"), options)
				.getContentVersionDataSummary();
	}

	private ContentVersionDataSummary uploadPdf2InputStream() {
		return contentManager.upload(getTestResourceInputStream("pdf2.pdf"), new UploadOptions())
				.getContentVersionDataSummary();
	}

	private ContentVersionDataSummary uploadPdf3InputStream() {
		return contentManager.upload(getTestResourceInputStream("pdf3.pdf"), new UploadOptions())
				.getContentVersionDataSummary();
	}

	private ContentVersionDataSummary uploadDocx1InputStream() {
		return contentManager.upload(getTestResourceInputStream("docx1.docx"), new UploadOptions())
				.getContentVersionDataSummary();
	}

	private ContentVersionDataSummary uploadDocx2InputStream() {
		return contentManager.upload(getTestResourceInputStream("docx2.docx"), new UploadOptions())
				.getContentVersionDataSummary();
	}

	private ContentVersionDataSummary uploadPdf1InputStreamWithoutParsing() {
		return contentManager.upload(getTestResourceInputStream("pdf1.pdf"), new UploadOptions("pdf1.pdf")
				.setParseOptions(ParseOptions.NO_PARSING)
				.setHandleDeletionOfUnreferencedHashes(false))
				.getContentVersionDataSummary();
	}

	private ContentVersionDataSummary uploadPdf2InputStreamWithoutParsing() {
		return contentManager.upload(getTestResourceInputStream("pdf2.pdf"), new UploadOptions("pd2.docx.pdf")
				.setParseOptions(ParseOptions.NO_PARSING)
				.setHandleDeletionOfUnreferencedHashes(false))
				.getContentVersionDataSummary();
	}

	private ContentVersionDataSummary uploadPdf3InputStreamWithoutParsing() {
		return contentManager.upload(getTestResourceInputStream("pdf3.pdf"), new UploadOptions("pd3.pdf.pdf")
				.setParseOptions(ParseOptions.NO_PARSING)
				.setHandleDeletionOfUnreferencedHashes(false))
				.getContentVersionDataSummary();
	}

	private ContentVersionDataSummary uploadDocx1InputStreamWithoutParsing() {
		return contentManager.upload(getTestResourceInputStream("docx1.docx"), new UploadOptions("doc1.docx")
				.setParseOptions(ParseOptions.NO_PARSING)
				.setHandleDeletionOfUnreferencedHashes(false))
				.getContentVersionDataSummary();
	}

	private ContentVersionDataSummary uploadDocx2InputStreamWithoutParsing() {
		return contentManager.upload(getTestResourceInputStream("docx2.docx"), new UploadOptions("doc2.doc.docx")
				.setParseOptions(ParseOptions.NO_PARSING)
				.setHandleDeletionOfUnreferencedHashes(false))
				.getContentVersionDataSummary();
	}

	private void assertThatVaultOnlyContains(String... hashes)
			throws Exception {
		RecordDao recordDao = getDataLayerFactory().newRecordDao();
		recordDao.flush();
		givenTimeIs(shishOClock.plusDays(1));
		assertThatVaultOnlyContainsWithoutAdvancingTime(hashes);
	}

	private void assertThatVaultOnlyContainsWithoutAdvancingTime(String... hashes)
			throws Exception {

		RecordDao recordDao = getDataLayerFactory().newRecordDao();
		recordDao.flush();

		getModelLayerFactory().getContentManager().deleteUnreferencedContents();

		List<String> hashList = asList(hashes);
		if (hashList.contains(pdf1Hash)) {
			assertThatContentIsAvailable(getPdf1InputStream());
		} else {
			assertThatContentIsNotAvailable(pdf1Hash);
		}
		if (hashList.contains(pdf2Hash)) {
			assertThatContentIsAvailable(getPdf2InputStream());
		} else {
			assertThatContentIsNotAvailable(pdf2Hash);
		}
		if (hashList.contains(pdf3Hash)) {
			assertThatContentIsAvailable(getPdf3InputStream());
		} else {
			assertThatContentIsNotAvailable(pdf3Hash);
		}
		if (hashList.contains(docx1Hash)) {
			assertThatContentIsAvailable(getDocx1InputStream());
		} else {
			assertThatContentIsNotAvailable(docx1Hash);
		}
		if (hashList.contains(docx2Hash)) {
			assertThatContentIsAvailable(getDocx2InputStream());
		} else {
			assertThatContentIsNotAvailable(docx2Hash);
		}
	}

	private void assertThatContentIsAvailable(ContentVersionDataSummary dataSummary)
			throws Exception {

		ContentVersionDataSummary retrievedDataSummary = contentManager.getContentVersionSummary(dataSummary.getHash())
				.getContentVersionDataSummary();
		assertThat(retrievedDataSummary).isEqualTo(dataSummary);

		InputStream contentStream = contentManager.getContentInputStream(dataSummary.getHash(), SDK_STREAM);
		if (dataSummary.getHash().equals(pdf1Hash)) {
			assertThat(contentStream).hasContentEqualTo(getTestResourceInputStream("pdf1.pdf"));

		} else if (dataSummary.getHash().equals(pdf2Hash)) {
			assertThat(contentStream).hasContentEqualTo(getTestResourceInputStream("pdf2.pdf"));

		} else if (dataSummary.getHash().equals(pdf3Hash)) {
			assertThat(contentStream).hasContentEqualTo(getTestResourceInputStream("pdf3.pdf"));

		} else if (dataSummary.getHash().equals(docx1Hash)) {
			assertThat(contentStream).hasContentEqualTo(getTestResourceInputStream("docx1.docx"));

		} else if (dataSummary.getHash().equals(docx2Hash)) {
			assertThat(contentStream).hasContentEqualTo(getTestResourceInputStream("docx2.docx"));

		}

	}

	private void assertThatContentIsNotAvailable(String hash) {
		try {
			contentManager.getContentInputStream(hash, SDK_STREAM);
			fail("Content with " + hash + " is available");
		} catch (ContentManagerRuntimeException_NoSuchContent noSuchContent) {
			//OK
		}

		try {
			contentManager.getContentVersionSummary(hash);
			fail("Content with " + hash + " is available");
		} catch (ContentManagerRuntimeException_NoSuchContent noSuchContent) {
			//OK
		}

	}

	private class RecordPreparation {

		private Record record;

		private RecordPreparation(Record record) {
			this.record = record;
		}

		public RecordPreparation withSingleValueContent(Content content) {
			record.set(zeSchema.contentMetadata(), content);
			return this;
		}

		public RecordPreparation addMultiValueContent(Content content) {
			List<Content> currentContends = record.getList(zeSchema.contentListMetadata());
			List<Content> contents = new ArrayList<>(currentContends);
			contents.add(content);
			record.set(zeSchema.contentListMetadata(), contents);
			return this;
		}

		public RecordPreparation clearAllMultiValueContents() {
			List<Content> contents = new ArrayList<>();
			record.set(zeSchema.contentListMetadata(), contents);
			return this;
		}

		public Record isSaved()
				throws RecordServicesException {
			String id = record.getId();
			if (record.isSaved()) {
				recordServices.update(record);
			} else {
				recordServices.add(record);
			}
			return recordServices.getDocumentById(id);
		}

		public RecordPreparation contentCheckedOutBy(User alice) {
			Content content = record.get(zeSchema.contentMetadata());
			content.checkOut(alice);
			return this;
		}

		public RecordPreparation and() {
			return this;
		}

		public RecordPreparation hasItsContentUpdatedAndFinalized(User alice,
																  ContentVersionDataSummary contentVersionDataSummary) {
			Content content = record.get(zeSchema.contentMetadata());
			content.updateContent(alice, contentVersionDataSummary, true);
			return this;
		}

		public RecordPreparation hasItsContentUpdated(User alice, ContentVersionDataSummary contentVersionDataSummary) {
			Content content = record.get(zeSchema.contentMetadata());
			content.updateContent(alice, contentVersionDataSummary, false);
			return this;
		}

		public RecordPreparation hasItsContentUpdatedWithName(User alice,
															  ContentVersionDataSummary contentVersionDataSummary,
															  String name) {
			Content content = record.get(zeSchema.contentMetadata());
			content.updateContentWithName(alice, contentVersionDataSummary, false, name);
			return this;
		}

		public RecordPreparation hasItsContentUpdatedWithVersionAndName(User alice,
																		ContentVersionDataSummary contentVersionDataSummary,
																		String version, String name) {
			Content content = record.get(zeSchema.contentMetadata());
			content.updateContentWithVersionAndName(alice, contentVersionDataSummary, version, name);
			return this;
		}

		public RecordPreparation hasItsCheckedOutContentUpdatedWith(
				ContentVersionDataSummary contentVersionDataSummary) {
			Content content = record.get(zeSchema.contentMetadata());
			content.updateCheckedOutContent(contentVersionDataSummary);
			return this;
		}

		public RecordPreparation hasItsContentRenamedTo(String newName) {
			Content content = record.get(zeSchema.contentMetadata());
			content.renameCurrentVersion(newName);
			return this;
		}

		public RecordPreparation hasItsContentCommentChangedTo(String newComment) {
			Content content = record.get(zeSchema.contentMetadata());
			content.setVersionComment(newComment);
			return this;
		}

		public RecordPreparation contentCheckedIn() {
			Content content = record.get(zeSchema.contentMetadata());
			content.checkIn();
			return this;
		}

		public RecordPreparation contentCheckedInCancelled() {
			Content content = record.get(zeSchema.contentMetadata());
			content.cancelCheckOut();
			return this;
		}

		public RecordPreparation finalizeVersion() {
			Content content = record.get(zeSchema.contentMetadata());
			content.finalizeVersion();
			return this;
		}

		public RecordPreparation contentCheckedInAsMinor(ContentVersionDataSummary contentVersionDataSummary) {
			Content content = record.get(zeSchema.contentMetadata());
			content.checkInWithModification(contentVersionDataSummary, false);
			return this;
		}

		public RecordPreparation contentCheckedInAsMinorWithNewName(ContentVersionDataSummary contentVersionDataSummary,
																	String newName) {
			Content content = record.get(zeSchema.contentMetadata());
			content.checkInWithModificationAndName(contentVersionDataSummary, false, newName);
			return this;
		}

		public RecordPreparation contentCheckedInAsSameVersionWithNewName(
				ContentVersionDataSummary contentVersionDataSummary,
				String newName) {
			Content content = record.get(zeSchema.contentMetadata());
			content.checkInWithModificationAndNameInSameVersion(contentVersionDataSummary, newName);
			return this;
		}

		public RecordPreparation contentCheckedInAsMajor(ContentVersionDataSummary contentVersionDataSummary) {
			Content content = record.get(zeSchema.contentMetadata());
			content.checkInWithModification(contentVersionDataSummary, true);
			return this;
		}

		public RecordPreparation withParent(Record parent) {
			record.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), parent);
			return this;
		}

		public RecordPreparation hasItsContentFinalized() {
			Content content = record.get(zeSchema.contentMetadata());
			content.finalizeVersion();
			return this;
		}

		public RecordPreparation deleteVersion(String versionLabel, User user) {
			Content content = record.get(zeSchema.contentMetadata());
			content.deleteVersion(versionLabel, user);
			return this;
		}

		public RecordPreparation withRequireConversionFlag(boolean value) {
			record.set(Schemas.MARKED_FOR_PREVIEW_CONVERSION, value);
			return this;
		}
	}

	Condition<? super Content> emptyVersion = new Condition<Content>() {
		@Override
		public boolean matches(Content value) {
			return value.isEmptyVersion();
		}
	};
}
