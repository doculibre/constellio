package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.modules.rm.wrappers.utils.DecomListUtil;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class DecommissioningListAcceptanceTest extends ConstellioTest {
	LocalDate november4 = new LocalDate(2009, 11, 4);
	LocalDate december12 = new LocalDate(2009, 12, 12);

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;

	ContainerRecord containerRecord;
	DecommissioningList decommissioningList;

	MediumType analogType, electronicType;

	@Before
	public void setUp()
			throws Exception {
		givenBackgroundThreadsEnabled();

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

		analogType = createMediumType("analog", true);
		electronicType = createMediumType("electronic", false);

		Transaction tr = new Transaction();
		tr.addAll(analogType, electronicType);
		recordServices.execute(tr);
	}

	@Test
	public void whenAddingFolderWithAnalogTypeToDecomListThenDecomListFolderMediaTypeContainsAnalog()
			throws Exception {

		String decomId = "decom";
		DecommissioningList decom = createDecomList(decomId);

		Folder folder = createFolder("folder");
		folder.setMediumTypes(analogType.getId());
		folder.setCurrentDecommissioningList(decomId);

		Transaction tr = new Transaction();
		tr.addAll(decom, folder);
		recordServices.execute(tr);

		waitForBatchProcess();

		assertThat(rm.getDecommissioningList(decomId).getFoldersMediaTypes()).containsOnly(FolderMediaType.ANALOG);
	}

	@Test
	public void whenAddingFolderWithElectronicTypeToDecomListThenDecomListFolderMediaTypeContainsElectronic()
			throws Exception {

		String decomId = "decom";
		DecommissioningList decom = createDecomList(decomId);

		Folder folder = createFolder("folder");
		folder.setMediumTypes(electronicType.getId());
		folder.setCurrentDecommissioningList(decomId);

		Transaction tr = new Transaction();
		tr.addAll(decom, folder);
		recordServices.execute(tr);

		waitForBatchProcess();

		assertThat(rm.getDecommissioningList(decomId).getFoldersMediaTypes()).containsOnly(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void whenAddingFolderWithHybridTypeToDecomListThenDecomListFolderMediaTypeContainsHybrid()
			throws Exception {

		String decomId = "decom";
		DecommissioningList decom = createDecomList(decomId);

		Folder folder = createFolder("folder");
		folder.setMediumTypes(analogType.getId(), electronicType.getId());
		folder.setCurrentDecommissioningList(decomId);

		Transaction tr = new Transaction();
		tr.addAll(decom, folder);
		recordServices.execute(tr);

		waitForBatchProcess();

		assertThat(rm.getDecommissioningList(decomId).getFoldersMediaTypes()).containsOnly(FolderMediaType.HYBRID);
	}

	@Test
	public void whenAddingFolderWithUnknownTypeToDecomListThenDecomListFolderMediaTypeContainsUnknown()
			throws Exception {

		String decomId = "decom";
		DecommissioningList decom = createDecomList(decomId);

		Folder folder = createFolder("folder");
		folder.setCurrentDecommissioningList(decomId);

		Transaction tr = new Transaction();
		tr.addAll(decom, folder);
		recordServices.execute(tr);

		waitForBatchProcess();

		assertThat(rm.getDecommissioningList(decomId).getFoldersMediaTypes()).containsOnly(FolderMediaType.UNKNOWN);
	}

	@Test
	public void whenAddingFolderWithAnalogTypeToProcessedDecomListThenDecomListFolderMediaTypeContainsAnalog()
			throws Exception {

		String decomId = "decom";
		DecommissioningList decom = createDecomList(decomId);

		Folder folder = createFolder("folder");
		folder.setMediumTypes(analogType.getId());
		folder.addPreviousDecommissioningList(decomId);

		Transaction tr = new Transaction();
		tr.addAll(decom, folder);
		recordServices.execute(tr);

		waitForBatchProcess();

		assertThat(rm.getDecommissioningList(decomId).getFoldersMediaTypes()).containsOnly(FolderMediaType.ANALOG);
	}

	@Test
	public void whenAddingFolderWithElectronicTypeToProcessedDecomListThenDecomListFolderMediaTypeContainsElectronic()
			throws Exception {

		String decomId = "decom";
		DecommissioningList decom = createDecomList(decomId);

		Folder folder = createFolder("folder");
		folder.setMediumTypes(electronicType.getId());
		folder.addPreviousDecommissioningList(decomId);

		Transaction tr = new Transaction();
		tr.addAll(decom, folder);
		recordServices.execute(tr);

		waitForBatchProcess();

		assertThat(rm.getDecommissioningList(decomId).getFoldersMediaTypes()).containsOnly(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void whenAddingFolderWithHybridTypeToProcessedDecomListThenDecomListFolderMediaTypeContainsHybrid()
			throws Exception {

		String decomId = "decom";
		DecommissioningList decom = createDecomList(decomId);

		Folder folder = createFolder("folder");
		folder.setMediumTypes(analogType.getId(), electronicType.getId());
		folder.addPreviousDecommissioningList(decomId);

		Transaction tr = new Transaction();
		tr.addAll(decom, folder);
		recordServices.execute(tr);

		waitForBatchProcess();

		assertThat(rm.getDecommissioningList(decomId).getFoldersMediaTypes()).containsOnly(FolderMediaType.HYBRID);
	}

	@Test
	public void whenAddingFolderWithUnknownTypeToProcessedDecomListThenDecomListFolderMediaTypeContainsUnknown()
			throws Exception {

		String decomId = "decom";
		DecommissioningList decom = createDecomList(decomId);

		Folder folder = createFolder("folder");
		folder.addPreviousDecommissioningList(decomId);

		Transaction tr = new Transaction();
		tr.addAll(decom, folder);
		recordServices.execute(tr);

		waitForBatchProcess();

		assertThat(rm.getDecommissioningList(decomId).getFoldersMediaTypes()).containsOnly(FolderMediaType.UNKNOWN);
	}

	@Test
	public void whenAddingFolderToDifferentDecomListThenAllDecomListFolderMediaTypeContainsFolderMediaType()
			throws Exception {

		String decomId = "decom";
		DecommissioningList decom = createDecomList(decomId);

		String anotherDecomId = "anotherDecom";
		DecommissioningList anotherDecom = createDecomList(anotherDecomId);

		Folder folder = createFolder("folder");
		folder.setMediumTypes(analogType.getId());
		folder.setCurrentDecommissioningList(decomId);
		folder.addPreviousDecommissioningList(anotherDecomId);

		Transaction tr = new Transaction();
		tr.addAll(decom, anotherDecom, folder);
		recordServices.execute(tr);

		waitForBatchProcess();

		assertThat(rm.getDecommissioningList(decomId).getFoldersMediaTypes()).containsOnly(FolderMediaType.ANALOG);
		assertThat(rm.getDecommissioningList(anotherDecomId).getFoldersMediaTypes()).containsOnly(FolderMediaType.ANALOG);
	}

	@Test
	public void whenAddingFolderToMultipleDecomListThenAllDecomListFolderMediaTypeContainsFolderMediaType()
			throws Exception {

		String decomId = "decom";
		DecommissioningList decom = createDecomList(decomId);

		String anotherDecomId = "anotherDecom";
		DecommissioningList anotherDecom = createDecomList(anotherDecomId);

		Folder folder = createFolder("folder");
		folder.setMediumTypes(analogType.getId());
		folder.addPreviousDecommissioningList(decomId);
		folder.addPreviousDecommissioningList(anotherDecomId);

		Transaction tr = new Transaction();
		tr.addAll(decom, anotherDecom, folder);
		recordServices.execute(tr);

		waitForBatchProcess();

		assertThat(rm.getDecommissioningList(decomId).getFoldersMediaTypes()).containsOnly(FolderMediaType.ANALOG);
		assertThat(rm.getDecommissioningList(anotherDecomId).getFoldersMediaTypes()).containsOnly(FolderMediaType.ANALOG);
	}

	@Test
	public void whenAddingMultipleFolderToDecomListThenDecomListFolderMediaTypeContainsAllFolderMediaTypes()
			throws Exception {

		String decomId = "decom";
		DecommissioningList decom = createDecomList(decomId);

		Folder folder = createFolder("folder");
		folder.setMediumTypes(analogType.getId());
		folder.addPreviousDecommissioningList(decomId);

		Folder anotherFolder = createFolder("anotherFolder");
		anotherFolder.setMediumTypes(electronicType.getId());
		anotherFolder.addPreviousDecommissioningList(decomId);

		Transaction tr = new Transaction();
		tr.addAll(decom, folder, anotherFolder);
		recordServices.execute(tr);

		waitForBatchProcess();

		assertThat(rm.getDecommissioningList(decomId).getFoldersMediaTypes()).containsOnly(FolderMediaType.ANALOG, FolderMediaType.ELECTRONIC);
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
		decommissioningList.setApprovalRequest(records.getUsers().dakotaLIndienIn(zeCollection).getId());
		decommissioningList.setApprovalUser(records.getUsers().dakotaLIndienIn(zeCollection));
		decommissioningList.setDecommissioningListType(DecommissioningListType.FOLDERS_TO_CLOSE);
		decommissioningList.setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE);

		decommissioningList.setContainerDetailsFor(aContainer.getId(), anotherContainer.getId());

		decommissioningList.setProcessingDate(december12);
		decommissioningList.setProcessingUser(records.getUsers().dakotaLIndienIn(zeCollection).getId());

		decommissioningList.setValidations(validations);

		decommissioningList = saveAndLoad(decommissioningList);

		DecomListUtil.setFolderDetailsInDecomList(zeCollection, getAppLayerFactory(), decommissioningList,
				asList(aFolder.getId(), anotherFolder.getId()), FolderDetailStatus.INCLUDED);

		decommissioningList = saveAndLoad(decommissioningList);

		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(decommissioningList.getApprovalDate()).isEqualTo(november4);
		assertThat(decommissioningList.getDescription()).isEqualTo("zeDescription");
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze list");
		assertThat(decommissioningList.getApprovalRequest()).isEqualTo(records.getUsers().dakotaLIndienIn(zeCollection).getId());
		assertThat(decommissioningList.getApprovalUser()).isEqualTo(records.getUsers().dakotaLIndienIn(zeCollection).getId());
		assertThat(DecomListUtil.getFoldersInDecomList(zeCollection, getAppLayerFactory(), decommissioningList)).isEqualTo(asList(aFolder.getId(), anotherFolder.getId()));
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
	public void whenSaveDocumentDecommissioningListThenMetadataValuesSaved()
			throws Exception {

		Document aDocument = newDocument();
		Document anotherDocument = newDocument();
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
		decommissioningList.setApprovalRequest(records.getUsers().dakotaLIndienIn(zeCollection).getId());
		decommissioningList.setApprovalUser(records.getUsers().dakotaLIndienIn(zeCollection));
		decommissioningList.setDecommissioningListType(DecommissioningListType.DOCUMENTS_TO_TRANSFER);
		decommissioningList.setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE);

		decommissioningList.setContainerDetailsFor(aContainer.getId(), anotherContainer.getId());

		decommissioningList.setProcessingDate(december12);
		decommissioningList.setProcessingUser(records.getUsers().dakotaLIndienIn(zeCollection).getId());

		decommissioningList.setValidations(validations);

		decommissioningList = saveAndLoad(decommissioningList);

		DecomListUtil.setDocumentsInDecomList(zeCollection, getAppLayerFactory(), decommissioningList,
				asList(aDocument.getId(), anotherDocument.getId()));

		decommissioningList = saveAndLoad(decommissioningList);

		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(decommissioningList.getApprovalDate()).isEqualTo(november4);
		assertThat(decommissioningList.getDescription()).isEqualTo("zeDescription");
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze list");
		assertThat(decommissioningList.getApprovalRequest()).isEqualTo(records.getUsers().dakotaLIndienIn(zeCollection).getId());
		assertThat(decommissioningList.getApprovalUser()).isEqualTo(records.getUsers().dakotaLIndienIn(zeCollection).getId());
		assertThat(DecomListUtil.getDocumentsInDecomList(zeCollection, getAppLayerFactory(), decommissioningList)).isEqualTo(asList(aDocument.getId(), anotherDocument.getId()));
		assertThat(decommissioningList.getContainers()).isEqualTo(asList(aContainer.getId(), anotherContainer.getId()));
		assertThat(decommissioningList.getContainerDetails()).isEqualTo(asList(
				new DecomListContainerDetail(aContainer.getId()),
				new DecomListContainerDetail(anotherContainer.getId())));
		assertThat(decommissioningList.getFoldersReportContent().getCurrentVersion().getFilename()).isEqualTo("folders.pdf");
		assertThat(decommissioningList.getDocumentsReportContent().getCurrentVersion().getFilename()).isEqualTo("documents.pdf");
		assertThat(decommissioningList.getProcessingDate()).isEqualTo(december12);
		assertThat(decommissioningList.getProcessingUser()).isEqualTo(records.getUsers().dakotaLIndienIn(zeCollection).getId());
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.DOCUMENTS_TO_TRANSFER);
		assertThat(decommissioningList.getOriginArchivisticStatus()).isEqualTo(OriginStatus.SEMI_ACTIVE);

		assertThat(decommissioningList.getValidations()).isEqualTo(validations);
	}

	@Test
	public void whenCreateRecordContainerInDecommissioningListThenCorrectlySaved()
			throws Exception {

		decommissioningList = saveAndLoad(newFilingSpaceAList().setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER));
		//TODO::JOLA.setFolderDetailsFor(rm.getFolders(records.folders("A04-A06")), FolderDetailStatus.INCLUDED));
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

	private Document newDocument() {
		Document document = rm.newDocument();
		document.setTitle("Ze document");
		document.setFolder(records.folder_A01);

		try {
			recordServices.add(document.getWrappedRecord(), User.GOD);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return rm.getDocument(document.getId());
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

	private MediumType createMediumType(String id, boolean isAnalog) {
		return rm.newMediumTypeWithId(id)
				.setCode("code_" + id)
				.setTitle("title_" + id)
				.setAnalogical(isAnalog);
	}

	private DecommissioningList createDecomList(String id) {
		return rm.newDecommissioningListWithId(id)
				.setTitle("title_" + id);
	}

	private Folder createFolder(String id) {
		return rm.newFolderWithId(id)
				.setTitle("title_" + id)
				.setOpenDate(november4)
				.setAdministrativeUnitEntered(records.unitId_11b)
				.setCategoryEntered(records.categoryId_X110)
				.setRetentionRuleEntered(records.ruleId_2)
				.setCopyStatusEntered(CopyType.PRINCIPAL);
	}
}
