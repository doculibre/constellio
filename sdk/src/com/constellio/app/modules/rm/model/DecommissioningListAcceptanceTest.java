package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.modules.rm.model.enums.FolderMediaType.ANALOG;
import static com.constellio.app.modules.rm.model.enums.FolderMediaType.ELECTRONIC;
import static com.constellio.app.modules.rm.model.enums.FolderMediaType.HYBRID;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class DecommissioningListAcceptanceTest extends ConstellioTest {
	LocalDate november4 = new LocalDate(2009, 11, 4);
	LocalDate december12 = new LocalDate(2009, 12, 12);

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;

	ContainerRecord containerRecord;
	Folder folder;
	DecommissioningList decommissioningList;

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		decommissioningList = rm.newDecommissioningList();
		containerRecord = getContainerRecord();
	}

	@Test
	public void givenInitializedThenSchemaHasNoSecurity()
			throws Exception {

		assertThat(getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(DecommissioningList.SCHEMA_TYPE).hasSecurity()).isFalse();
	}

	@Test
	public void whenSaveDecommissioningListThenMetadataValuesSaved()
			throws Exception {

		Folder aFolder = newFolder();
		Folder anotherFolder = newFolder();
		ContainerRecord aContainer = newContainerRecord("A");
		ContainerRecord anotherContainer = newContainerRecord("B");

		List<DecomListValidation> validations = new ArrayList<>();
		validations.add(new DecomListValidation(records.getDakota_managerInA_userInB().getId(), november4));
		validations.add(new DecomListValidation(records.getBob_userInAC().getId(), november4));
		validations.add(new DecomListValidation(records.getCharles_userInA().getId(), november4));

		ContentManager contentManager = getModelLayerFactory().getContentManager();
		ContentVersionDataSummary newDocumentsVersions = contentManager.upload(getTestResourceInputStream("documents.pdf"));
		Content documentContent = contentManager.createMajor(records.getAdmin(), "documents.pdf", newDocumentsVersions);

		ContentVersionDataSummary newFoldersVersions = contentManager.upload(getTestResourceInputStream("folders.pdf"));
		Content folderContent = contentManager.createMajor(records.getAdmin(), "folders.pdf", newFoldersVersions);

		decommissioningList = rm.newDecommissioningList();
		decommissioningList.setTitle("Ze list");
		decommissioningList.setAdministrativeUnit(records.unitId_10a);
		decommissioningList.setDocumentsReportContent(documentContent);
		decommissioningList.setFoldersReportContent(folderContent);
		decommissioningList.setApprovalDate(november4);
		decommissioningList.setDescription("zeDescription");
		decommissioningList.setApprovalRequester(records.getUsers().dakotaLIndienIn(zeCollection).getId());
		decommissioningList.setApprovalUser(records.getUsers().dakotaLIndienIn(zeCollection));
		decommissioningList.setDecommissioningListType(DecommissioningListType.FOLDERS_TO_CLOSE);
		decommissioningList.setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE);

		decommissioningList.setFolderDetailsFor(asList(aFolder, anotherFolder), FolderDetailStatus.INCLUDED);
		decommissioningList.setContainerDetailsFor(aContainer.getId(), anotherContainer.getId());

		decommissioningList.setProcessingDate(december12);
		decommissioningList.setProcessingUser(records.getUsers().dakotaLIndienIn(zeCollection).getId());

		decommissioningList.setValidations(validations);

		decommissioningList = saveAndLoad(decommissioningList);

		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(decommissioningList.getApprovalDate()).isEqualTo(november4);
		assertThat(decommissioningList.getDescription()).isEqualTo("zeDescription");
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze list");
		assertThat(decommissioningList.getApprovalRequester()).isEqualTo(records.getUsers().dakotaLIndienIn(zeCollection).getId());
		assertThat(decommissioningList.getApprovalUser()).isEqualTo(records.getUsers().dakotaLIndienIn(zeCollection).getId());
		assertThat(decommissioningList.getFolders()).isEqualTo(asList(aFolder.getId(), anotherFolder.getId()));
		assertThat(decommissioningList.getFolderDetails())
				.isEqualTo(asList(new DecomListFolderDetail(aFolder, FolderDetailStatus.INCLUDED), new DecomListFolderDetail(anotherFolder, FolderDetailStatus.INCLUDED)));
		assertThat(decommissioningList.getContainers()).isEqualTo(asList(aContainer.getId(), anotherContainer.getId()));
		assertThat(decommissioningList.getContainerDetails()).isEqualTo(asList(
				new DecomListContainerDetail(aContainer.getId()),
				new DecomListContainerDetail(anotherContainer.getId())));
		assertThat(decommissioningList.getFoldersReportContent().getCurrentVersion().getFilename()).isEqualTo("folders.pdf");
		assertThat(decommissioningList.getDocumentsReportContent().getCurrentVersion().getFilename()).isEqualTo("documents.pdf");
		assertThat(decommissioningList.getProcessingDate()).isEqualTo(december12);
		assertThat(decommissioningList.getProcessingUser()).isEqualTo(records.getUsers().dakotaLIndienIn(zeCollection).getId());
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_CLOSE);
		assertThat(decommissioningList.getOriginArchivisticStatus()).isEqualTo(OriginStatus.SEMI_ACTIVE);

		assertThat(decommissioningList.getValidations()).isEqualTo(validations);
	}

	@Test
	public void givenFoldersWithUniformRuleAndNonUniformCopyAndCategoryThenNotUniform()
			throws Exception {

		decommissioningList = saveAndLoad(newFilingSpaceAList().setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER).setFolderDetailsFor(rm.getFolders(records.folders("A04-A06")), FolderDetailStatus.INCLUDED));
		assertThat(decommissioningList.hasAnalogicalMedium()).isEqualTo(true);
		assertThat(decommissioningList.hasElectronicMedium()).isEqualTo(true);
		assertThat(decommissioningList.getFoldersMediaTypes()).containsOnly(HYBRID, HYBRID, HYBRID);
		assertThat(decommissioningList.getStatus()).isEqualTo(DecomListStatus.GENERATED);
		assertThat(decommissioningList.getUniformCategory()).isEqualTo(records.categoryId_X110);
		assertThat(decommissioningList.getUniformCopyRule().toString())
				.isEqualTo(copyBuilder.newPrincipal(records.PA_MD, "42-5-C").toString());
		assertThat(decommissioningList.getUniformCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(decommissioningList.getUniformRule()).isEqualTo(records.ruleId_1);
		assertThat(decommissioningList.isUniform()).isEqualTo(true);

		decommissioningList = saveAndLoad(newFilingSpaceAList().setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER).setFolderDetailsFor(rm.getFolders(records.folders("A04-A06, A16-A18")), FolderDetailStatus.INCLUDED));
		assertThat(decommissioningList.hasAnalogicalMedium()).isEqualTo(true);
		assertThat(decommissioningList.hasElectronicMedium()).isEqualTo(true);
		assertThat(decommissioningList.getFoldersMediaTypes()).containsOnly(HYBRID, HYBRID, HYBRID, HYBRID, HYBRID, HYBRID);
		assertThat(decommissioningList.getStatus()).isEqualTo(DecomListStatus.GENERATED);
		assertThat(decommissioningList.getUniformCategory()).isNull();
		assertThat(decommissioningList.getUniformCopyRule().toString())
				.isEqualTo(copyBuilder.newPrincipal(records.PA_MD, "42-5-C").toString());
		assertThat(decommissioningList.getUniformCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(decommissioningList.getUniformRule()).isEqualTo(records.ruleId_1);
		assertThat(decommissioningList.isUniform()).isEqualTo(false);

		decommissioningList = saveAndLoad(newFilingSpaceAList().setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER).setFolderDetailsFor(rm.getFolders(records.folders("A22-A24")), FolderDetailStatus.INCLUDED));
		assertThat(decommissioningList.getFoldersMediaTypes()).containsOnly(ANALOG, ANALOG, ANALOG);
		assertThat(decommissioningList.hasAnalogicalMedium()).isEqualTo(true);
		assertThat(decommissioningList.hasElectronicMedium()).isEqualTo(false);

		decommissioningList = saveAndLoad(newFilingSpaceAList().setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER).setFolderDetailsFor(rm.getFolders(records.folders("A25-A27")), FolderDetailStatus.INCLUDED));
		assertThat(decommissioningList.getFoldersMediaTypes()).containsOnly(ELECTRONIC, ELECTRONIC, ELECTRONIC);
		assertThat(decommissioningList.hasAnalogicalMedium()).isEqualTo(false);
		assertThat(decommissioningList.hasElectronicMedium()).isEqualTo(true);

		decommissioningList = saveAndLoad(newFilingSpaceAList().setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER).setFolderDetailsFor(rm.getFolders(records.folders("A22-A27")), FolderDetailStatus.INCLUDED));
		assertThat(decommissioningList.hasAnalogicalMedium()).isEqualTo(true);
		assertThat(decommissioningList.hasElectronicMedium()).isEqualTo(true);
		assertThat(decommissioningList.getFoldersMediaTypes())
				.containsOnly(ANALOG, ANALOG, ANALOG, ELECTRONIC, ELECTRONIC, ELECTRONIC);
		assertThat(decommissioningList.getStatus()).isEqualTo(DecomListStatus.GENERATED);
		assertThat(decommissioningList.getUniformCategory()).isEqualTo(records.categoryId_X120);
		assertThat(decommissioningList.getUniformCopyRule()).isNull();
		assertThat(decommissioningList.getUniformCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(decommissioningList.getUniformRule()).isEqualTo(records.ruleId_4);
		assertThat(decommissioningList.isUniform()).isEqualTo(false);
	}

	@Test
	public void givenDecommissioningListwithUniformDocumentsThenUniformValues()
			throws Exception {

		decommissioningList = saveAndLoad((DecommissioningList) newFilingSpaceAList()
				.setDecommissioningListType(DecommissioningListType.DOCUMENTS_TO_TRANSFER).set(DecommissioningList.DOCUMENTS, documentIn(records.folders("A04-A06"))));
		assertThat(decommissioningList.getList(DecommissioningList.DOCUMENTS)).hasSize(9);
		assertThat(decommissioningList.getUniformCategory()).isEqualTo(records.categoryId_X110);
		assertThat(decommissioningList.getUniformCopyRule().toString())
				.isEqualTo(copyBuilder.newPrincipal(records.PA_MD, "42-5-C").toString());
		assertThat(decommissioningList.getUniformCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(decommissioningList.getUniformRule()).isEqualTo(records.ruleId_1);
		assertThat(decommissioningList.isUniform()).isEqualTo(true);

		decommissioningList = saveAndLoad((DecommissioningList) newFilingSpaceAList()
				.setDecommissioningListType(DecommissioningListType.DOCUMENTS_TO_TRANSFER).set(DecommissioningList.DOCUMENTS, documentIn(records.folders("A04-A06, A16-A18"))));
		assertThat(decommissioningList.getUniformCategory()).isNull();
		assertThat(decommissioningList.getUniformCopyRule().toString())
				.isEqualTo(copyBuilder.newPrincipal(records.PA_MD, "42-5-C").toString());
		assertThat(decommissioningList.getUniformCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(decommissioningList.getUniformRule()).isEqualTo(records.ruleId_1);
		assertThat(decommissioningList.isUniform()).isEqualTo(false);

		decommissioningList = saveAndLoad((DecommissioningList) newFilingSpaceAList()
				.setDecommissioningListType(DecommissioningListType.DOCUMENTS_TO_TRANSFER).set(DecommissioningList.DOCUMENTS, documentIn(records.folders("A04-A18, B52-B54"))));
		assertThat(decommissioningList.getUniformCategory()).isNull();
		assertThat(decommissioningList.getUniformCopyRule()).isNull();
		assertThat(decommissioningList.getUniformCopyType()).isNull();
		assertThat(decommissioningList.getUniformRule()).isNull();
		assertThat(decommissioningList.isUniform()).isEqualTo(false);
	}

	private List<String> documentIn(List<String> folders) {

		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.documentSchemaType()).where(rm.documentFolder()).isIn(folders));
		return getModelLayerFactory().newSearchServices().searchRecordIds(query);

	}

	@Test
	public void whenCreateRecordContainerInDecommissioningListThenCorrectlySaved()
			throws Exception {

		decommissioningList = saveAndLoad(newFilingSpaceAList().setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER).setFolderDetailsFor(rm.getFolders(records.folders("A04-A06")), FolderDetailStatus.INCLUDED));
		String containerRecordType = records.getContainerBac01().getType();

		ContainerRecord containerRecord = rm.newContainerRecord().setTitle("ze container").setTemporaryIdentifier("42")
				.setAdministrativeUnits(Arrays.asList(records.unitId_10a)).setDecommissioningType(DecommissioningType.DEPOSIT)
				.setType(containerRecordType);
		containerRecord.getWrappedRecord().set(Schemas.LOGICALLY_DELETED_STATUS, null);
		DecomListContainerDetail decomListContainerDetail = new DecomListContainerDetail(containerRecord.getId());
		decommissioningList.setContainerDetails(asList(decomListContainerDetail));
		Transaction transaction = new Transaction();
		transaction.addAll(containerRecord, decommissioningList);
		recordServices.execute(transaction);

	}

	private DecommissioningList newFilingSpaceAList() {
		DecommissioningList decommissioningList = rm.newDecommissioningList();
		decommissioningList.setTitle("Ze list");
		decommissioningList.setAdministrativeUnit(records.unitId_10a);
		return decommissioningList;
	}

	//

	private DecommissioningList saveAndLoad(DecommissioningList decommissioningList)
			throws RecordServicesException {
		recordServices.add(decommissioningList, records.getGandalf_managerInABC());
		return rm.getDecommissioningList(decommissioningList.getId());
	}

	private Folder getFolder() {
		if (folder != null && rm.getFolder(folder.getId()) != null) {
			return rm.getFolder(folder.getId());
		} else {
			return folder = newFolder();
		}
	}

	private Folder newFolder() {
		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_11b);
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setTitle("Ze folder");
		folder.setOpenDate(november4);
		folder.setCloseDateEntered(december12);
		try {
			recordServices.add(folder.getWrappedRecord(), User.GOD);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return rm.getFolder(folder.getId());
	}

	private ContainerRecord getContainerRecord() {
		if (containerRecord != null && containerRecord.getWrappedRecord() != null) {
			return containerRecord;
		} else {
			return containerRecord = newContainerRecord(aString());
		}
	}

	private ContainerRecord newContainerRecord(String token) {
		ContainerRecordType type = newContainerRecordType(token);
		ContainerRecord containerRecord = rm.newContainerRecord();
		containerRecord.setType(type);
		containerRecord.setTitle("zeContainerRecord Title " + token);
		containerRecord.setAdministrativeUnit(records.unitId_10a);
		containerRecord.setDecommissioningType(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		containerRecord.setFull(true);
		containerRecord.setStorageSpace(newStorageSpace());
		containerRecord.setTemporaryIdentifier("zeContainerRecord " + token);
		try {
			recordServices.add(containerRecord.getWrappedRecord(), User.GOD);
			return containerRecord;
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	StorageSpace newStorageSpace() {
		int token = anInteger();
		StorageSpace storageSpace = rm.newStorageSpace();
		storageSpace.setTitle("Storage space " + token);
		storageSpace.setCode("storageSpace" + token);
		try {
			recordServices.add(storageSpace.getWrappedRecord(), User.GOD);
			return storageSpace;
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	ContainerRecordType newContainerRecordType(String token) {
		ContainerRecordType type = rm.newContainerRecordType();
		type.setCode("zeContainerType." + token);
		type.setDescription("zeContainerType Description");
		type.setTitle("zeContainerType Title " + token);

		try {
			recordServices.add(type.getWrappedRecord(), User.GOD);
			return type;
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}
}
