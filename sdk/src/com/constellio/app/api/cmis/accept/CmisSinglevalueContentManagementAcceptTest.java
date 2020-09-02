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

public class CmisSinglevalueContentManagementAcceptTest extends ConstellioTest {

	private final String PDF_MIMETYPE = "application/pdf";
	private final String DOCX_MIMETYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	LocalDateTime firstDocumentModificationOClock = new LocalDateTime();
	LocalDateTime documentCreationOClock = new LocalDateTime().plusHours(1);
	LocalDateTime firstDocumentCheckOutOClock = new LocalDateTime().plusHours(2);
	LocalDateTime firstDocumentCheckOutModificationOClock = new LocalDateTime().plusHours(3);
	LocalDateTime firstDocumentCheckOutCheckInOClock = new LocalDateTime().plusHours(4);
	LocalDateTime secondDocumentCheckOutOClock = new LocalDateTime().plusHours(5);
	LocalDateTime secondDocumentCheckOutCheckInWithContentOClock = new LocalDateTime().plusHours(6);
	LocalDateTime replaceContentOClock = new LocalDateTime().plusHours(7);
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

		defineSchemasManager()
				.using(schemas.withAContentMetadata());
		CmisAcceptanceTestSetup.allSchemaTypesSupported(getAppLayerFactory());
		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();

		Map<Language, String> labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "taxo");

		Taxonomy taxonomy = Taxonomy.createPublic("taxo", labelTitle, zeCollection, asList("zeSchemaType"));
		taxonomiesManager.addTaxonomy(taxonomy, metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(taxonomy, metadataSchemasManager);

		UserServices userServices = getModelLayerFactory().newUserServices();
		userServices.execute(addUpdateUserCredential(
				"bob", "bob", "gratton", "bob@doculibre.com", new ArrayList<String>(), asList(zeCollection), ACTIVE).setServiceKey("bob-key").setSystemAdminEnabled());

		userServices.execute(addUpdateUserCredential(
				"alice", "alice", "wonderland", "alice@doculibre.com", new ArrayList<String>(), asList(zeCollection), ACTIVE).setServiceKey("alice-key").setSystemAdminEnabled());

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
	public void givenSingleValueContentThenCanManageItWithCmis()
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
		_2_firstDocumentModification();

		givenTimeIs(firstDocumentCheckOutOClock);
		givenCurrentUserIsAlice();
		_3_firstDocumentCheckOut();

		givenTimeIs(firstDocumentCheckOutModificationOClock);
		givenCurrentUserIsAlice();
		_4_firstDocumentCheckOutModification();

		givenTimeIs(firstDocumentCheckOutCheckInOClock);
		givenCurrentUserIsAlice();
		_5_firstDocumentCheckOutCheckIn();

		givenTimeIs(secondDocumentCheckOutOClock);
		givenCurrentUserIsBob();
		_6_secondDocumentCheckOut();

		givenTimeIs(secondDocumentCheckOutCheckInWithContentOClock);
		givenCurrentUserIsBob();
		_7_secondDocumentCheckOutCheckInWithContentOClock();

		givenTimeIs(replaceContentOClock);
		givenCurrentUserIsAlice();
		_8_replaceContentOClock(folder);

		givenTimeIs(deleteContentOClock);
		givenCurrentUserIsBob();
		_9_deleteContentOClock();

	}

	private void _1_documentCreation(Folder folder)
			throws IOException {
		Map<String, Object> properties = new HashMap<>();
		properties.put("metadata", "contentMetadata");
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		Document cmisContent = folder.createDocument(properties, pdf1ContentStream(), VersioningState.MAJOR);
		Content content = recordServices.getDocumentById(zeRecord).get(zeSchema.contentMetadata());
		assertThat(cmisContent.getId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + content.getId() + "_1.0");
		zeRecordContentId = content.getId();

		cmisContent = getSingleContentOf(zeRecord);
		assertThatContentStreamIsSameAs(cmisContent, pdf1ContentStream(), pdf1Hash);
		assertThatVersionCreationInfoAre(cmisContent, aliceId, documentCreationOClock);
		assertThat(cmisContent.getId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + content.getId() + "_1.0");
		assertThat(cmisContent.getProperty(PropertyIds.PARENT_ID).<String>getValue()).isEqualTo(zeRecord);
		assertThat(cmisContent.getVersionLabel()).isEqualTo("1.0");
		assertThat(cmisContent.getVersionSeriesCheckedOutBy()).isNull();
		//		assertThat(cmisContent.getVersionSeriesCheckedOutId()).isNull();
		assertThat(cmisContent.getVersionSeriesId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + content.getId());
		assertThat(cmisContent.isLatestMajorVersion()).isTrue();
		assertThat(cmisContent.isLatestVersion()).isTrue();
		assertThat(cmisContent.isMajorVersion()).isTrue();
		//		assertThat(cmisContent.isPrivateWorkingCopy()).isFalse();
		assertThat(cmisContent.isVersionSeriesCheckedOut()).isFalse();
		assertThat(cmisContent.<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");

	}

	private Document _2_firstDocumentModification()
			throws IOException {
		Document cmisContent = getContentsOf(zeRecord).get(0);
		cmisContent.setContentStream(docx1ContentStream(), true);
		recordServices.refresh(recordServices.getDocumentById(zeRecord));
		cmisContent = getContentsOf(zeRecord).get(0);
		assertThatContentStreamIsSameAs(cmisContent, docx1ContentStream(), docx1Hash);
		assertThatVersionCreationInfoAre(cmisContent, bobId, firstDocumentModificationOClock);
		assertThat(cmisContent.getId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_1.1");
		assertThat(cmisContent.getProperty(PropertyIds.PARENT_ID).<String>getValue()).isEqualTo(zeRecord);
		assertThat(cmisContent.getVersionLabel()).isEqualTo("1.1");
		assertThat(cmisContent.getVersionSeriesCheckedOutBy()).isNull();
		assertThat(cmisContent.getVersionSeriesCheckedOutId()).isNull();
		assertThat(cmisContent.getVersionSeriesId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(cmisContent.isLatestMajorVersion()).isFalse();
		assertThat(cmisContent.isLatestVersion()).isTrue();
		assertThat(cmisContent.isMajorVersion()).isFalse();
		//		assertThat(cmisContent.isPrivateWorkingCopy()).isFalse();
		assertThat(cmisContent.isVersionSeriesCheckedOut()).isFalse();
		assertThat(cmisContent.<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");
		return cmisContent;
	}

	private void _3_firstDocumentCheckOut()
			throws IOException {
		Document cmisContent = getSingleContentOf(zeRecord);
		String privateWorkingCopyId = cmisContent.checkOut().getId();
		assertThat(privateWorkingCopyId).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_co");

		//Therefore, until it is checked in (using the checkIn service), the PWC MUST NOT be considered
		// the latest or latest major version in the version series. That is, the values of the
		// cmis:isLatestVersion and cmis:isLatestMajorVersion properties MUST be FALSE.
		cmisContent = getSingleContentOf(zeRecord);
		assertThatContentStreamIsSameAs(cmisContent, docx1ContentStream(), docx1Hash);
		assertThatVersionCreationInfoAre(cmisContent, bobId, firstDocumentModificationOClock);
		assertThat(cmisContent.getId()).isEqualTo(privateWorkingCopyId);
		assertThat(cmisContent.getProperty(PropertyIds.PARENT_ID).<String>getValue()).isEqualTo(zeRecord);
		assertThat(cmisContent.getVersionLabel()).isEqualTo("1.1");
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
		assertThat(allVersions).hasSize(3);

		assertThatContentStreamIsSameAs(allVersions.get(0), docx1ContentStream(), docx1Hash);
		assertThatVersionCreationInfoAre(allVersions.get(0), bobId, firstDocumentModificationOClock);
		assertThat(allVersions.get(0).getId()).isEqualTo(privateWorkingCopyId);
		assertThat(allVersions.get(0).<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(allVersions.get(0).getVersionLabel()).isEqualTo("1.1");
		assertThat(allVersions.get(0).getVersionSeriesCheckedOutBy()).isEqualTo(aliceId);
		assertThat(allVersions.get(0).getVersionSeriesCheckedOutId()).isEqualTo(privateWorkingCopyId);
		assertThat(allVersions.get(0).getVersionSeriesId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(allVersions.get(0).isLatestMajorVersion()).isFalse();
		assertThat(allVersions.get(0).isLatestVersion()).isFalse();
		assertThat(allVersions.get(0).isMajorVersion()).isFalse();
		//assertThat(allVersions.get(0).isPrivateWorkingCopy()).isTrue();
		assertThat(allVersions.get(0).isVersionSeriesCheckedOut()).isTrue();
		assertThat(allVersions.get(0).<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");

		assertThatContentStreamIsSameAs(allVersions.get(1), docx1ContentStream(), docx1Hash);
		assertThatVersionCreationInfoAre(allVersions.get(1), bobId, firstDocumentModificationOClock);
		assertThat(allVersions.get(1).getId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_1.1");
		assertThat(allVersions.get(1).<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(allVersions.get(1).getVersionLabel()).isEqualTo("1.1");
		assertThat(allVersions.get(1).getVersionSeriesCheckedOutBy()).isEqualTo(aliceId);
		assertThat(allVersions.get(1).getVersionSeriesCheckedOutId()).isEqualTo(privateWorkingCopyId);
		assertThat(allVersions.get(1).getVersionSeriesId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(allVersions.get(1).isLatestMajorVersion()).isFalse();
		assertThat(allVersions.get(1).isLatestVersion()).isTrue();
		assertThat(allVersions.get(1).isMajorVersion()).isFalse();
		//assertThat(allVersions.get(1).isPrivateWorkingCopy()).isFalse();
		assertThat(allVersions.get(1).isVersionSeriesCheckedOut()).isTrue();
		assertThat(allVersions.get(1).<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");

		assertThat(allVersions.get(2).getVersionLabel()).isEqualTo("1.0");

		assertThat(allVersions.get(2).getId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_1.0");
		assertThat(allVersions.get(2).<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);

		assertThat(allVersions.get(2).getVersionSeriesCheckedOutBy()).isEqualTo(aliceId);
		assertThat(allVersions.get(2).getVersionSeriesCheckedOutId()).isEqualTo(privateWorkingCopyId);
		assertThat(allVersions.get(2).getVersionSeriesId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(allVersions.get(2).isLatestMajorVersion()).isTrue();
		assertThat(allVersions.get(2).isLatestVersion()).isFalse();
		assertThat(allVersions.get(2).isMajorVersion()).isTrue();
		//assertThat(allVersions.get(2).isPrivateWorkingCopy()).isFalse();
		assertThat(allVersions.get(2).isVersionSeriesCheckedOut()).isTrue();
		assertThat(allVersions.get(2).<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");
		assertThatVersionCreationInfoAre(allVersions.get(2), aliceId, documentCreationOClock);
		assertThatContentStreamIsSameAs(allVersions.get(2), pdf1ContentStream(), pdf1Hash);

		givenCurrentUserIsBob();
		//Bob does not see the pvc version and cannot checkout

		cmisContent = getSingleContentOf(zeRecord);
		allVersions = cmisContent.getAllVersions();
		assertThat(allVersions).hasSize(2);

		assertThatContentStreamIsSameAs(allVersions.get(0), docx1ContentStream(), docx1Hash);
		assertThatVersionCreationInfoAre(allVersions.get(0), bobId, firstDocumentModificationOClock);
		assertThat(allVersions.get(0).getId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_1.1");
		assertThat(allVersions.get(0).<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(allVersions.get(0).getVersionLabel()).isEqualTo("1.1");
		assertThat(allVersions.get(0).getVersionSeriesCheckedOutBy()).isEqualTo(aliceId);
		assertThat(allVersions.get(0).getVersionSeriesCheckedOutId()).isEqualTo(privateWorkingCopyId);
		assertThat(allVersions.get(0).getVersionSeriesId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(allVersions.get(0).isLatestMajorVersion()).isFalse();
		assertThat(allVersions.get(0).isLatestVersion()).isTrue();
		assertThat(allVersions.get(0).isMajorVersion()).isFalse();
		//assertThat(allVersions.get(0).isPrivateWorkingCopy()).isFalse();
		assertThat(allVersions.get(0).isVersionSeriesCheckedOut()).isTrue();
		assertThat(allVersions.get(0).<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");

		assertThatContentStreamIsSameAs(allVersions.get(1), pdf1ContentStream(), pdf1Hash);
		assertThatVersionCreationInfoAre(allVersions.get(1), aliceId, documentCreationOClock);
		assertThat(allVersions.get(1).getId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_1.0");
		assertThat(allVersions.get(1).<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(allVersions.get(1).getVersionLabel()).isEqualTo("1.0");
		assertThat(allVersions.get(1).getVersionSeriesCheckedOutBy()).isEqualTo(aliceId);
		assertThat(allVersions.get(1).getVersionSeriesCheckedOutId()).isEqualTo(privateWorkingCopyId);
		assertThat(allVersions.get(1).getVersionSeriesId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(allVersions.get(1).isLatestMajorVersion()).isTrue();
		assertThat(allVersions.get(1).isLatestVersion()).isFalse();
		assertThat(allVersions.get(1).isMajorVersion()).isTrue();
		//assertThat(allVersions.get(1).isPrivateWorkingCopy()).isFalse();
		assertThat(allVersions.get(1).isVersionSeriesCheckedOut()).isTrue();
		assertThat(allVersions.get(1).<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");

		try {
			cmisContent.checkOut();
			fail("Bob was able to check out a document which is already checked out by alice");
		} catch (Exception e) {
			//OK
		}
	}

	private void _4_firstDocumentCheckOutModification()
			throws IOException {
		Document cmisContent = getSingleContentOf(zeRecord);
		String privateWorkingCopyId = cmisContent.setContentStream(pdf2ContentStream(), true, true).getId();
		assertThat(privateWorkingCopyId).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_co");
		cmisContent = getSingleContentOf(zeRecord);
		assertThat(privateWorkingCopyId).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_co");

		assertThat(cmisContent.getVersionLabel()).isEqualTo("1.2");
		assertThatContentStreamIsSameAs(cmisContent, pdf2ContentStream(), pdf2Hash);
		assertThatVersionCreationInfoAre(cmisContent, aliceId, firstDocumentCheckOutModificationOClock);
		assertThat(cmisContent.getId()).isEqualTo(privateWorkingCopyId);
		assertThat(cmisContent.<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(cmisContent.getVersionLabel()).isEqualTo("1.2");
		assertThat(cmisContent.getVersionSeriesCheckedOutBy()).isEqualTo(aliceId);
		assertThat(cmisContent.getVersionSeriesCheckedOutId()).isEqualTo(privateWorkingCopyId);
		assertThat(cmisContent.getVersionSeriesId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(cmisContent.isLatestMajorVersion()).isFalse();
		assertThat(cmisContent.isLatestVersion()).isFalse();
		assertThat(cmisContent.isMajorVersion()).isFalse();
		//assertThat(cmisContent.isPrivateWorkingCopy()).isFalse();
		assertThat(cmisContent.isVersionSeriesCheckedOut()).isTrue();
		assertThat(cmisContent.<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");

		List<Document> allVersions = cmisContent.getAllVersions();
		assertThat(allVersions).hasSize(3);

		assertThatContentStreamIsSameAs(allVersions.get(0), pdf2ContentStream(), pdf2Hash);
		assertThatVersionCreationInfoAre(allVersions.get(0), aliceId, firstDocumentCheckOutModificationOClock);
		assertThat(allVersions.get(0).getId()).isEqualTo(privateWorkingCopyId);
		assertThat(allVersions.get(0).<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(allVersions.get(0).getVersionLabel()).isEqualTo("1.2");
		assertThat(allVersions.get(0).getVersionSeriesCheckedOutBy()).isEqualTo(aliceId);
		assertThat(allVersions.get(0).getVersionSeriesCheckedOutId()).isEqualTo(privateWorkingCopyId);
		assertThat(allVersions.get(0).getVersionSeriesId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(allVersions.get(0).isLatestMajorVersion()).isFalse();
		assertThat(allVersions.get(0).isLatestVersion()).isFalse();
		assertThat(allVersions.get(0).isMajorVersion()).isFalse();
		//assertThat(allVersions.get(0).isPrivateWorkingCopy()).isFalse();
		assertThat(allVersions.get(0).isVersionSeriesCheckedOut()).isTrue();
		assertThat(allVersions.get(0).<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");

		assertThatContentStreamIsSameAs(allVersions.get(1), docx1ContentStream(), docx1Hash);
		assertThatVersionCreationInfoAre(allVersions.get(1), bobId, firstDocumentModificationOClock);
		assertThat(allVersions.get(1).getId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_1.1");
		assertThat(allVersions.get(1).<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(allVersions.get(1).getVersionLabel()).isEqualTo("1.1");
		assertThat(allVersions.get(1).getVersionSeriesCheckedOutBy()).isEqualTo(aliceId);
		assertThat(allVersions.get(1).getVersionSeriesCheckedOutId()).isEqualTo(privateWorkingCopyId);
		assertThat(allVersions.get(1).getVersionSeriesId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(allVersions.get(1).isLatestMajorVersion()).isFalse();
		assertThat(allVersions.get(1).isLatestVersion()).isTrue();
		assertThat(allVersions.get(1).isMajorVersion()).isFalse();
		//assertThat(allVersions.get(1).isPrivateWorkingCopy()).isFalse();
		assertThat(allVersions.get(1).isVersionSeriesCheckedOut()).isTrue();
		assertThat(allVersions.get(1).<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");

		assertThatContentStreamIsSameAs(allVersions.get(2), pdf1ContentStream(), pdf1Hash);
		assertThatVersionCreationInfoAre(allVersions.get(2), aliceId, documentCreationOClock);
		assertThat(allVersions.get(2).getId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_1.0");
		assertThat(allVersions.get(2).<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(allVersions.get(2).getVersionLabel()).isEqualTo("1.0");
		assertThat(allVersions.get(2).getVersionSeriesCheckedOutBy()).isEqualTo(aliceId);
		assertThat(allVersions.get(2).getVersionSeriesCheckedOutId()).isEqualTo(privateWorkingCopyId);
		assertThat(allVersions.get(2).getVersionSeriesId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(allVersions.get(2).isLatestMajorVersion()).isTrue();
		assertThat(allVersions.get(2).isLatestVersion()).isFalse();
		assertThat(allVersions.get(2).isMajorVersion()).isTrue();
		//assertThat(allVersions.get(2).isPrivateWorkingCopy()).isFalse();
		assertThat(allVersions.get(2).isVersionSeriesCheckedOut()).isTrue();
		assertThat(allVersions.get(2).<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");
	}

	private void _5_firstDocumentCheckOutCheckIn()
			throws IOException {
		Document cmisContent = getSingleContentOf(zeRecord);
		String versionId = cmisContent.checkIn(true, new HashMap<String, Object>(), null, null).getId();
		assertThat(versionId).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_2.0");

		cmisContent = getSingleContentOf(zeRecord);
		assertThatContentStreamIsSameAs(cmisContent, pdf2ContentStream(), pdf2Hash);
		assertThatVersionCreationInfoAre(cmisContent, aliceId, firstDocumentCheckOutModificationOClock);
		assertThat(cmisContent.getId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_2.0");
		assertThat(cmisContent.<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(cmisContent.getVersionLabel()).isEqualTo("2.0");
		assertThat(cmisContent.getVersionSeriesCheckedOutBy()).isNull();
		assertThat(cmisContent.getVersionSeriesCheckedOutId()).isNull();
		assertThat(cmisContent.getVersionSeriesId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(cmisContent.isLatestMajorVersion()).isTrue();
		assertThat(cmisContent.isLatestVersion()).isTrue();
		assertThat(cmisContent.isMajorVersion()).isTrue();
		//assertThat(cmisContent.isPrivateWorkingCopy()).isFalse();
		assertThat(cmisContent.isVersionSeriesCheckedOut()).isFalse();
		assertThat(cmisContent.<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");

		List<Document> allVersions = cmisContent.getAllVersions();
		assertThat(allVersions).hasSize(3);

		assertThatContentStreamIsSameAs(allVersions.get(0), pdf2ContentStream(), pdf2Hash);
		assertThatVersionCreationInfoAre(allVersions.get(0), aliceId, firstDocumentCheckOutModificationOClock);
		assertThat(allVersions.get(0).getId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_2.0");
		assertThat(allVersions.get(0).<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(allVersions.get(0).getVersionLabel()).isEqualTo("2.0");
		assertThat(allVersions.get(0).getVersionSeriesCheckedOutBy()).isNull();
		assertThat(allVersions.get(0).getVersionSeriesCheckedOutId()).isNull();
		assertThat(allVersions.get(0).getVersionSeriesId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(allVersions.get(0).isLatestMajorVersion()).isTrue();
		assertThat(allVersions.get(0).isLatestVersion()).isTrue();
		assertThat(allVersions.get(0).isMajorVersion()).isTrue();
		//assertThat(allVersions.get(0).isPrivateWorkingCopy()).isFalse();
		assertThat(allVersions.get(0).isVersionSeriesCheckedOut()).isFalse();
		assertThat(allVersions.get(0).<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");
	}

	private void _6_secondDocumentCheckOut()
			throws IOException {
		Document cmisContent = getSingleContentOf(zeRecord);
		String privateWorkingCopyId = cmisContent.checkOut().getId();
		assertThat(privateWorkingCopyId).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_co");

		cmisContent = getSingleContentOf(zeRecord);
		assertThatContentStreamIsSameAs(cmisContent, pdf2ContentStream(), pdf2Hash);
		assertThatVersionCreationInfoAre(cmisContent, aliceId, firstDocumentCheckOutModificationOClock);
		assertThat(cmisContent.getId()).isEqualTo(privateWorkingCopyId);
		assertThat(cmisContent.<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(cmisContent.getVersionLabel()).isEqualTo("2.0");
		assertThat(cmisContent.getVersionSeriesCheckedOutBy()).isEqualTo(bobId);
		assertThat(cmisContent.getVersionSeriesCheckedOutId()).isEqualTo(privateWorkingCopyId);
		assertThat(cmisContent.getVersionSeriesId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(cmisContent.isLatestMajorVersion()).isFalse();
		assertThat(cmisContent.isLatestVersion()).isFalse();
		assertThat(cmisContent.isMajorVersion()).isFalse();
		//assertThat(cmisContent.isPrivateWorkingCopy()).isTrue();
		assertThat(cmisContent.isVersionSeriesCheckedOut()).isTrue();
		assertThat(cmisContent.<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");

	}

	private void _7_secondDocumentCheckOutCheckInWithContentOClock()
			throws IOException {
		Document cmisContent = getSingleContentOf(zeRecord);
		String versionId = cmisContent.checkIn(false, new HashMap<String, Object>(), docx2ContentStream(), null)
				.getId();
		assertThat(versionId).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_2.1");

		cmisContent = getSingleContentOf(zeRecord);

		assertThatVersionCreationInfoAre(cmisContent, bobId, secondDocumentCheckOutCheckInWithContentOClock);
		assertThat(cmisContent.getId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_2.1");
		assertThat(cmisContent.<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(cmisContent.getVersionLabel()).isEqualTo("2.1");
		assertThat(cmisContent.getVersionSeriesCheckedOutBy()).isNull();
		assertThat(cmisContent.getVersionSeriesCheckedOutId()).isNull();
		assertThat(cmisContent.getVersionSeriesId()).isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(cmisContent.isLatestMajorVersion()).isFalse();
		assertThat(cmisContent.isLatestVersion()).isTrue();
		assertThat(cmisContent.isMajorVersion()).isFalse();
		//assertThat(cmisContent.isPrivateWorkingCopy()).isFalse();
		assertThat(cmisContent.isVersionSeriesCheckedOut()).isFalse();
		assertThat(cmisContent.<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");
		assertThatContentStreamIsSameAs(cmisContent, docx2ContentStream(), docx2Hash);

		List<Document> allVersions = cmisContent.getAllVersions();
		assertThat(allVersions).hasSize(4);

		assertThatContentStreamIsSameAs(allVersions.get(0), docx2ContentStream(), docx2Hash);
		assertThatVersionCreationInfoAre(allVersions.get(0), bobId, secondDocumentCheckOutCheckInWithContentOClock);
		assertThat(allVersions.get(0).getId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_2.1");
		assertThat(allVersions.get(0).<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(allVersions.get(0).getVersionLabel()).isEqualTo("2.1");
		assertThat(allVersions.get(0).getVersionSeriesCheckedOutBy()).isNull();
		assertThat(allVersions.get(0).getVersionSeriesCheckedOutId()).isNull();
		assertThat(allVersions.get(0).getVersionSeriesId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(allVersions.get(0).isLatestMajorVersion()).isFalse();
		assertThat(allVersions.get(0).isLatestVersion()).isTrue();
		assertThat(allVersions.get(0).isMajorVersion()).isFalse();
		//assertThat(allVersions.get(0).isPrivateWorkingCopy()).isFalse();
		assertThat(allVersions.get(0).isVersionSeriesCheckedOut()).isFalse();
		assertThat(allVersions.get(0).<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");

		assertThatVersionCreationInfoAre(allVersions.get(1), aliceId, firstDocumentCheckOutModificationOClock);
		assertThat(allVersions.get(1).getId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_2.0");
		assertThat(allVersions.get(1).<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(allVersions.get(1).getVersionLabel()).isEqualTo("2.0");
		assertThat(allVersions.get(1).getVersionSeriesCheckedOutBy()).isNull();
		assertThat(allVersions.get(1).getVersionSeriesCheckedOutId()).isNull();
		assertThat(allVersions.get(1).getVersionSeriesId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId);
		assertThat(allVersions.get(1).isLatestMajorVersion()).isTrue();
		assertThat(allVersions.get(1).isLatestVersion()).isFalse();
		assertThat(allVersions.get(1).isMajorVersion()).isTrue();
		//assertThat(allVersions.get(1).isPrivateWorkingCopy()).isFalse();
		assertThat(allVersions.get(1).isVersionSeriesCheckedOut()).isFalse();
		assertThat(allVersions.get(1).<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");
		assertThatContentStreamIsSameAs(allVersions.get(1), pdf2ContentStream(), pdf2Hash);

		assertThat(allVersions.get(2).getId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_1.1");

		assertThat(allVersions.get(3).getId())
				.isEqualTo("content_" + zeRecord + "_contentMetadata_" + zeRecordContentId + "_1.0");
	}

	private void _8_replaceContentOClock(Folder folder)
			throws IOException {
		Map<String, Object> properties = new HashMap<>();
		properties.put("metadata", "contentMetadata");
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		folder.createDocument(properties, pdf2ContentStream(), VersioningState.MAJOR);

		Document cmisContent = getSingleContentOf(zeRecord);
		assertThatContentStreamIsSameAs(cmisContent, pdf2ContentStream(), pdf2Hash);
		assertThatVersionCreationInfoAre(cmisContent, aliceId, replaceContentOClock);
		assertThat(cmisContent.getId()).startsWith("content_" + zeRecord + "_contentMetadata_");
		assertThat(cmisContent.getId()).endsWith("_1.0");
		assertThat(cmisContent.getId()).doesNotContain(zeRecordContentId);
		assertThat(cmisContent.<String>getPropertyValue(PropertyIds.PARENT_ID)).isEqualTo(zeRecord);
		assertThat(cmisContent.getVersionLabel()).isEqualTo("1.0");
		assertThat(cmisContent.getVersionSeriesCheckedOutBy()).isNull();
		assertThat(cmisContent.getVersionSeriesCheckedOutId()).isNull();
		assertThat(cmisContent.getVersionSeriesId()).startsWith("content_" + zeRecord + "_contentMetadata_");
		assertThat(cmisContent.getVersionSeriesId()).doesNotContain(zeRecordContentId);
		assertThat(cmisContent.isLatestMajorVersion()).isTrue();
		assertThat(cmisContent.isLatestVersion()).isTrue();
		assertThat(cmisContent.isMajorVersion()).isTrue();
		//assertThat(cmisContent.isPrivateWorkingCopy()).isFalse();
		assertThat(cmisContent.isVersionSeriesCheckedOut()).isFalse();
		assertThat(cmisContent.<String>getPropertyValue("metadata")).isEqualTo("contentMetadata");

		assertThat(cmisContent.getAllVersions()).hasSize(1);

	}

	private void _9_deleteContentOClock() {
		Document cmisContent = getSingleContentOf(zeRecord);
		cmisContent.deleteAllVersions();

		assertThat(getContentsOf(zeRecord)).isEmpty();
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

	private ContentStream docx2ContentStream()
			throws IOException {
		String filename = "docx2.docx";
		BigInteger length = BigInteger.valueOf(docx2Length);
		String mimetype = DOCX_MIMETYPE;
		InputStream stream = getTestResourceInputStreamFactory("docx2.docx").create(SDK_STREAM);
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

	private Document getSingleContentOf(String id) {
		List<Document> contents = getContentsOf(id);
		assertThat(contents).hasSize(1);
		return contents.get(0);
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
