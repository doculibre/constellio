package com.constellio.app.api.cmis.accept;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.constellio.data.conf.HashingEncoding.BASE64;
import static com.constellio.model.entities.security.global.UserCredentialStatus.ACTIVE;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CmisMultivalueContentManagementAcceptTest extends ConstellioTest {

	private final String PDF_MIMETYPE = "application/pdf";
	private final String DOCX_MIMETYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	LocalDateTime firstDocumentModificationOClock = new LocalDateTime();
	LocalDateTime documentCreationOClock = new LocalDateTime().plusHours(1);
	LocalDateTime firstDocumentCheckOutOClock = new LocalDateTime().plusHours(2);
	LocalDateTime firstDocumentCheckOutCheckInOClock = new LocalDateTime().plusHours(4);
	LocalDateTime deleteContentOClock = new LocalDateTime().plusHours(8);
	ContentManager contentManager;
	RecordServices recordServices;
	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	User bob;
	User alice;
	String bobId;
	String aliceId;
	String zeRecord = "zeRecord";
	Session session;
	String currentUserId;
	String zeRecordContentId;
	private long pdf1Length = 170039L;
	private long pdf2Length = 167347L;
	private long docx1Length = 27055L;
	private long docx2Length = 27325L;
	private String pdf1Hash = "KN8RjbrnBgq1EDDV2U71a6/6gd4=";
	private String pdf2Hash = "T+4zq4cGP/tXkdJp/qz1WVWYhoQ=";
	private String docx1Hash = "Fss7pKBafi8ok5KaOwEpmNdeGCE=";
	private String docx2Hash = "TIKwSvHOXHOOtRd1K9t2fm4TQ4I=";

	private String aliceToken, bobToken;

	@Before
	public void setUp()
			throws Exception {
		givenHashingEncodingIs(BASE64);
		givenTimeIs(firstDocumentModificationOClock);

		Map<Language, String> mapLangueTitle = new HashMap<>();
		mapLangueTitle.put(Language.French, "taxo");

		defineSchemasManager().using(schemas.withAMultivalueContentMetadata());
		CmisAcceptanceTestSetup.allSchemaTypesSupported(getAppLayerFactory());
		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		Taxonomy taxonomy = Taxonomy.createPublic("taxo", mapLangueTitle, zeCollection, asList("zeSchemaType"));
		taxonomiesManager.addTaxonomy(taxonomy, metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(taxonomy, metadataSchemasManager);

		UserServices userServices = getModelLayerFactory().newUserServices();
		userServices.execute(addUpdateUserCredential(
				"bob", "bob", "gratton", "bob@doculibre.com", new ArrayList<String>(), asList(zeCollection), ACTIVE).setServiceKey("bob_key").setSystemAdminEnabled());

		userServices.execute(addUpdateUserCredential(
				"alice", "alice", "wonderland", "alice@doculibre.com", new ArrayList<String>(), asList(zeCollection), ACTIVE).setServiceKey("alice_key").setSystemAdminEnabled());

		alice = userServices.getUserInCollection("alice", zeCollection);
		bob = userServices.getUserInCollection("bob", zeCollection);
		aliceToken = userServices.generateToken("alice");
		bobToken = userServices.generateToken("bob");

		aliceId = alice.getId();
		bobId = bob.getId();

		recordServices = getModelLayerFactory().newRecordServices();
		contentManager = getModelLayerFactory().getContentManager();

		alice.setCollectionDeleteAccess(true);
		bob.setCollectionDeleteAccess(true);
		alice.setCollectionWriteAccess(true);
		bob.setCollectionWriteAccess(true);
		recordServices.update(alice.getWrappedRecord());
		recordServices.update(bob.getWrappedRecord());

		CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());
	}

	@Test
	public void givenMultiValueContentThenCanManageItWithCmis()
			throws Exception {

		givenCurrentUserIsAlice();
		givenRecord();
		session = sessionFor(alice);

		Folder folder = (Folder) session.getObject(zeRecord);
		assertThat(getContentsOf(zeRecord)).isEmpty();

		givenTimeIs(documentCreationOClock);
		givenCurrentUserIsAlice();
		_1_documentCreation(folder);

		givenTimeIs(firstDocumentModificationOClock);
		givenCurrentUserIsBob();
		folder = (Folder) session.getObject(zeRecord);
		_2_addingAnotherDocument(folder);

		givenTimeIs(firstDocumentCheckOutOClock);
		givenCurrentUserIsAlice();
		_3_firstDocumentCheckOut();

		givenTimeIs(firstDocumentCheckOutCheckInOClock);
		givenCurrentUserIsAlice();
		_5_firstDocumentCheckOutCheckIn();

		givenTimeIs(deleteContentOClock);
		givenCurrentUserIsBob();
		_9_deleteOneContent();

	}

	private void _1_documentCreation(Folder folder)
			throws IOException {
		Map<String, Object> properties = new HashMap<>();
		properties.put("metadata", "contentMetadata");
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		Document cmisContent1 = folder.createDocument(properties, pdf1ContentStream(), VersioningState.MAJOR);
		Document cmisContent2 = folder.createDocument(properties, docx1ContentStream(), VersioningState.MAJOR);
		List<Content> contents = recordServices.getDocumentById(zeRecord).getList(zeSchema.contentMetadata());
		String firstContentId = contents.get(0).getId();
		String secondContentId = contents.get(1).getId();
		assertThat(cmisContent1.getId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + firstContentId + "_1.0");
		assertThat(cmisContent2.getId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + secondContentId + "_1.0");

		List<Document> cmisContents = getContentsOf(zeRecord);
		assertThatContentStreamIsSameAs(cmisContents.get(0), pdf1ContentStream(), pdf1Hash);
		assertThatVersionCreationInfoAre(cmisContents.get(0), aliceId, documentCreationOClock);
		assertThat(cmisContents.get(0).getId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + firstContentId + "_1.0");
		assertThat(cmisContents.get(0).getProperty(PropertyIds.PARENT_ID).<String>getValue()).isEqualTo(zeRecord);

		assertThatContentStreamIsSameAs(cmisContents.get(1), docx1ContentStream(), docx1Hash);
		assertThatVersionCreationInfoAre(cmisContents.get(1), aliceId, documentCreationOClock);
		assertThat(cmisContents.get(1).getId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + secondContentId + "_1.0");
		assertThat(cmisContents.get(1).getProperty(PropertyIds.PARENT_ID).<String>getValue()).isEqualTo(zeRecord);

	}

	private void _2_addingAnotherDocument(Folder folder)
			throws IOException {
		Map<String, Object> properties = new HashMap<>();
		properties.put("metadata", "contentMetadata");
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		Document cmisContent1 = folder.createDocument(properties, pdf2ContentStream(), VersioningState.MAJOR);
		List<Content> contents = recordServices.getDocumentById(zeRecord).getList(zeSchema.contentMetadata());
		zeRecordContentId = contents.get(2).getId();
		assertThat(cmisContent1.getId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_1.0");

		List<Document> cmisContents = getContentsOf(zeRecord);
		assertThat(cmisContents).hasSize(3);
		assertThat(cmisContent1.getId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_1.0");
		assertThatContentStreamIsSameAs(cmisContents.get(2), pdf2ContentStream(), pdf2Hash);
		assertThatVersionCreationInfoAre(cmisContents.get(2), bobId, firstDocumentModificationOClock);
		assertThat(cmisContents.get(2).getId()).isEqualTo(
				"content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_1.0");
		assertThat(cmisContents.get(2).getProperty(PropertyIds.PARENT_ID).<String>getValue()).isEqualTo(zeRecord);
	}

	private void _3_firstDocumentCheckOut()
			throws IOException {
		Document cmisContent = getContentsOf(zeRecord).get(2);
		String privateWorkingCopyId = cmisContent.checkOut().getId();
		assertThat(privateWorkingCopyId).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_co");

		//Therefore, until it is checked in (using the checkIn service), the PWC MUST NOT be considered
		// the latest or latest major version in the version series. That is, the values of the
		// cmis:isLatestVersion and cmis:isLatestMajorVersion properties MUST be FALSE.
		cmisContent = getContentsOf(zeRecord).get(2);
		assertThatContentStreamIsSameAs(cmisContent, pdf2ContentStream(), pdf2Hash);
		assertThatVersionCreationInfoAre(cmisContent, bobId, firstDocumentModificationOClock);
		assertThat(cmisContent.getId()).isEqualTo(privateWorkingCopyId);
		assertThat(cmisContent.getProperty(PropertyIds.PARENT_ID).<String>getValue()).isEqualTo(zeRecord);
		assertThat(cmisContent.getVersionLabel()).isEqualTo("1.0");
		assertThat(cmisContent.getVersionSeriesCheckedOutBy()).isEqualTo(aliceId);
		assertThat(cmisContent.getVersionSeriesCheckedOutId()).isEqualTo(privateWorkingCopyId);
		assertThat(cmisContent.getVersionSeriesId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(cmisContent.isLatestMajorVersion()).isFalse();
		assertThat(cmisContent.isLatestVersion()).isFalse();
		assertThat(cmisContent.isMajorVersion()).isFalse();
		//assertThat(cmisContent.isPrivateWorkingCopy()).isTrue();
		assertThat(cmisContent.isVersionSeriesCheckedOut()).isTrue();
		assertThat(cmisContent.<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");

		List<Document> allVersions = cmisContent.getAllVersions();
		assertThat(allVersions).hasSize(2);

		assertThatContentStreamIsSameAs(allVersions.get(0), pdf2ContentStream(), pdf2Hash);
		assertThatVersionCreationInfoAre(allVersions.get(0), bobId, firstDocumentModificationOClock);
		assertThat(allVersions.get(0).getId()).isEqualTo(privateWorkingCopyId);
		assertThat(allVersions.get(0).<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);

		try {
			cmisContent.checkOut();
			fail("Bob was able to check out a document which is already checked out by alice");
		} catch (Exception e) {
			//OK
		}
	}

	private void _5_firstDocumentCheckOutCheckIn()
			throws IOException {
		Document cmisContent = getContentsOf(zeRecord).get(2);
		String versionId = cmisContent.checkIn(true, new HashMap<String, Object>(), null, null).getId();
		assertThat(versionId).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_1.0");

		cmisContent = getContentsOf(zeRecord).get(2);
		assertThatContentStreamIsSameAs(cmisContent, pdf2ContentStream(), pdf2Hash);
		assertThatVersionCreationInfoAre(cmisContent, bobId, firstDocumentModificationOClock);
		assertThat(cmisContent.getId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_1.0");
		assertThat(cmisContent.<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(cmisContent.getVersionLabel()).isEqualTo("1.0");
		assertThat(cmisContent.getVersionSeriesCheckedOutBy()).isNull();
		assertThat(cmisContent.getVersionSeriesCheckedOutId()).isNull();
		assertThat(cmisContent.getVersionSeriesId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(cmisContent.isLatestMajorVersion()).isTrue();
		assertThat(cmisContent.isLatestVersion()).isTrue();
		assertThat(cmisContent.isMajorVersion()).isTrue();
		//assertThat(cmisContent.isPrivateWorkingCopy()).isFalse();
		assertThat(cmisContent.isVersionSeriesCheckedOut()).isFalse();
		assertThat(cmisContent.<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");
	}

	private void _9_deleteOneContent() {
		List<Document> cmisContents = getContentsOf(zeRecord);
		cmisContents.get(2).deleteAllVersions();

		assertThat(getContentsOf(zeRecord)).hasSize(2);
	}

	private ContentStream pdf1ContentStream()
			throws IOException {
		String filename = "pdf1.pdf";
		BigInteger length = BigInteger.valueOf(pdf1Length);
		String mimetype = PDF_MIMETYPE;
		InputStream stream = getTestResourceInputStreamFactory("pdf1.pdf").create(SDK_STREAM);
		return new ContentStreamImpl(filename, length, mimetype, stream);
	}

	private ContentStream pdf2ContentStream()
			throws IOException {
		String filename = "pdf2.pdf";
		BigInteger length = BigInteger.valueOf(pdf2Length);
		String mimetype = PDF_MIMETYPE;
		InputStream stream = getTestResourceInputStreamFactory("pdf2.pdf").create(SDK_STREAM);
		return new ContentStreamImpl(filename, length, mimetype, stream);
	}

	private ContentStream docx1ContentStream()
			throws IOException {
		String filename = "docx1.docx";
		BigInteger length = BigInteger.valueOf(docx1Length);
		String mimetype = DOCX_MIMETYPE;
		InputStream stream = getTestResourceInputStreamFactory("docx1.docx").create(SDK_STREAM);
		return new ContentStreamImpl(filename, length, mimetype, stream);
	}

	private void givenCurrentUserIsAlice() {
		session = sessionFor(alice);
	}

	private void givenCurrentUserIsBob() {
		session = sessionFor(bob);
	}

	private void assertThatVersionCreationInfoAre(Document document, String userId, LocalDateTime dateTime) {
		assertThat(document.getCreationDate().getTime()).isEqualToIgnoringMillis(dateTime.toDate());
		assertThat(document.getCreatedBy()).isEqualTo(userId);
		assertThat(document.getLastModificationDate().getTime()).isEqualToIgnoringMillis(dateTime.toDate());
		assertThat(document.getLastModifiedBy()).isEqualTo(userId);
	}

	private void assertThatContentStreamIsSameAs(Document document, ContentStream stream, String hash) {
		ContentStream contentStream = document.getContentStream();
		assertThat(contentStream.getMimeType()).isEqualTo(stream.getMimeType());  // OK
		assertThat(contentStream.getFileName()).isEqualTo(stream.getFileName()); // OK
		assertThat(document.getContentStreamLength()).isEqualTo(stream.getLength());
		// ContentStream lenghts are accessible in the document, not needed in the stream itself.
		assertThat(contentStream.getBigLength()).isNull();
		assertThat(contentStream.getLength()).isEqualTo(-1);

		assertThat(contentStream.getStream()).hasContentEqualTo(stream.getStream()); // OK

		// All of those are MISSING
		assertThat(document.getContentStreamId()).isEqualTo(hash);
		assertThat(document.getContentStreamFileName()).isEqualTo(stream.getFileName());
		assertThat(document.getContentStreamLength()).isEqualTo(stream.getLength());
		assertThat(document.getContentStreamMimeType()).isEqualTo(stream.getMimeType());
	}

	private List<Document> getContentsOf(String id) {
		List<Document> objectList = new ArrayList<>();
		Folder folder = (Folder) session.getObject(id);
		Iterator<CmisObject> iterator = folder.getChildren().iterator();
		while (iterator.hasNext()) {
			CmisObject object = iterator.next();
			if (object instanceof Document) {
				objectList.add((Document) object);
			}
		}
		return objectList;
	}

	private String givenRecord()
			throws RecordServicesException {
		Record record = new TestRecord(zeSchema, "zeRecord");
		recordServices.add(record);
		return record.getId();
	}

	private Session sessionFor(User user) {
		UserCredential userCredential = getModelLayerFactory().newUserServices().getUserCredential(user.getUsername());
		currentUserId = user.getUsername();
		String token = user.getUsername().equals("alice") ? aliceToken : bobToken;
		return newCmisSessionBuilder().authenticatedBy(userCredential.getServiceKey(), token).onCollection(zeCollection).build();
	}
}
