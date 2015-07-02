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
package com.constellio.model.services.contents;

import static com.constellio.model.services.contents.ContentFactory.isCheckedOutBy;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Condition;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserPermissionsChecker;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_CannotDeleteLastVersion;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_ContentMustBeCheckedOut;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_ContentMustNotBeCheckedOut;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_UserHasNoDeleteVersionPermission;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class ContentManagementAcceptTest extends ConstellioTest {

	private AtomicInteger threadCalls = new AtomicInteger();

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

	private String pdf1Hash = "KN8RjbrnBgq1EDDV2U71a6/6gd4=";
	private String pdf2Hash = "T+4zq4cGP/tXkdJp/qz1WVWYhoQ=";
	private String pdf3Hash = "2O9RyZlxNUL3asxk2yGDT6VIlbs=";
	private String docx1Hash = "Fss7pKBafi8ok5KaOwEpmNdeGCE=";
	private String docx2Hash = "TIKwSvHOXHOOtRd1K9t2fm4TQ4I=";

	@Mock UserPermissionsChecker userPermissionsChecker;

	@Before
	public void setUp()
			throws Exception {

		withSpiedServices(ContentManager.class);

		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(ModelLayerConfiguration configuration) {
				Mockito.when(configuration.getDelayBeforeDeletingUnreferencedContents()).thenReturn(
						org.joda.time.Duration.standardMinutes(42));

				Mockito.when(configuration.getUnreferencedContentsThreadDelayBetweenChecks()).thenReturn(
						org.joda.time.Duration.standardHours(10));
			}
		});

		givenTimeIs(smashOClock);
		defineSchemasManager()
				.using(schemas.withAContentMetadata().withAContentListMetadata().withAParentReferenceFromZeSchemaToZeSchema());
		defineSchemasManager().using(anotherCollectionSchemas.withAContentMetadata());

		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		Taxonomy taxonomy = Taxonomy.createPublic("taxo", "taxo", zeCollection, Arrays.asList("zeSchemaType"));
		taxonomiesManager.addTaxonomy(taxonomy, metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(taxonomy, metadataSchemasManager);

		getModelLayerFactory().newUserServices().addUpdateUserCredential(
				new UserCredential("bob", "bob", "gratton", "bob@doculibre.com", new ArrayList<String>(),
						Arrays.asList(zeCollection, "anotherCollection"), UserCredentialStatus.ACTIVE, "domain"));

		getModelLayerFactory().newUserServices().addUpdateUserCredential(
				new UserCredential("alice", "alice", "wonderland", "alice@doculibre.com", new ArrayList<String>(),
						Arrays.asList(zeCollection), UserCredentialStatus.ACTIVE, "domain"));

		alice = getModelLayerFactory().newUserServices().getUserInCollection("alice", zeCollection);
		bob = spy(getModelLayerFactory().newUserServices().getUserInCollection("bob", zeCollection));
		bobInAnotherCollection = getModelLayerFactory().newUserServices().getUserInCollection("bob", "anotherCollection");
		aliceId = alice.getId();
		bobId = bob.getId();

		recordServices = getModelLayerFactory().newRecordServices();
		contentManager = getModelLayerFactory().getContentManager();

		bob.setCollectionDeleteAccess(true);
		recordServices.update(bob.getWrappedRecord());

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

		assertThat(theRecordContent().getCurrentVersion()).has(docsMimetype()).has(filename("ZeDoc.docx")).has(
				docx1HashAndLength()).has(
				version("1.0")).has(modifiedBy(alice)).has(modificationDatetime(shishOClock));

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

	//------------------------------------------------------------------

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
		return new ContentVersionDataSummary("KN8RjbrnBgq1EDDV2U71a6/6gd4=", "application/pdf", 170039);
	}

	private ContentVersionDataSummary getPdf2InputStream() {
		return new ContentVersionDataSummary("T+4zq4cGP/tXkdJp/qz1WVWYhoQ=", "application/pdf", 167347);
	}

	private ContentVersionDataSummary getPdf3InputStream() {
		return new ContentVersionDataSummary("2O9RyZlxNUL3asxk2yGDT6VIlbs=", "application/pdf", 141667);
	}

	private ContentVersionDataSummary getDocx1InputStream() {
		return new ContentVersionDataSummary("Fss7pKBafi8ok5KaOwEpmNdeGCE=",
				"application/vnd.openxmlformats-officedocument.wordprocessingml.document", 27055);
	}

	private ContentVersionDataSummary getDocx2InputStream() {
		return new ContentVersionDataSummary("TIKwSvHOXHOOtRd1K9t2fm4TQ4I=",
				"application/vnd.openxmlformats-officedocument.wordprocessingml.document", 27325);
	}

	private ContentVersionDataSummary uploadPdf1InputStream() {
		return contentManager.upload(getTestResourceInputStream("pdf1.pdf"));
	}

	private ContentVersionDataSummary uploadPdf2InputStream() {
		return contentManager.upload(getTestResourceInputStream("pdf2.pdf"));
	}

	private ContentVersionDataSummary uploadPdf3InputStream() {
		return contentManager.upload(getTestResourceInputStream("pdf3.pdf"));
	}

	private ContentVersionDataSummary uploadDocx1InputStream() {
		return contentManager.upload(getTestResourceInputStream("docx1.docx"));
	}

	private ContentVersionDataSummary uploadDocx2InputStream() {
		return contentManager.upload(getTestResourceInputStream("docx2.docx"));
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

		List<String> hashList = Arrays.asList(hashes);
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

		ContentVersionDataSummary retrievedDataSummary = contentManager.getContentVersionSummary(dataSummary.getHash());
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

		public RecordPreparation hasItsContentUpdatedWithName(User alice, ContentVersionDataSummary contentVersionDataSummary,
				String name) {
			Content content = record.get(zeSchema.contentMetadata());
			content.updateContentWithName(alice, contentVersionDataSummary, false, name);
			return this;
		}

		public RecordPreparation hasItsCheckedOutContentUpdatedWith(ContentVersionDataSummary contentVersionDataSummary) {
			Content content = record.get(zeSchema.contentMetadata());
			content.updateCheckedOutContent(contentVersionDataSummary);
			return this;
		}

		public RecordPreparation hasItsContentRenamedTo(String newName) {
			Content content = record.get(zeSchema.contentMetadata());
			content.renameCurrentVersion(newName);
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
	}

	Condition<? super Content> emptyVersion = new Condition<Content>() {
		@Override
		public boolean matches(Content value) {
			return value.isEmptyVersion();
		}
	};
}
