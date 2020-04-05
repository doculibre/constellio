package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class DecommissioningList extends RecordWrapper {
	public static final String SCHEMA_TYPE = "decommissioningList";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String DESCRIPTION = "description";
	public static final String TYPE = "type";
	public static final String FILING_SPACE = "filingSpace";
	public static final String ADMINISTRATIVE_UNIT = "administrativeUnit";
	public static final String ANALOGICAL_MEDIUM = "analogicalMedium";
	public static final String ELECTRONIC_MEDIUM = "electronicMedium";
	public static final String APPROVAL_REQUEST_DATE = "approvalRequestDate";
	public static final String APPROVAL_REQUEST = "approvalRequest";
	public static final String APPROVAL_DATE = "approvalDate";
	public static final String APPROVAL_USER = "approvalUser";
	public static final String PROCESSING_DATE = "processingDate";
	public static final String PROCESSING_USER = "processingUser";
	public static final String FOLDER_DETAILS = "folderDetails";
	public static final String CONTAINER_DETAILS = "containerDetails";
	public static final String FOLDERS = "folders";
	public static final String CONTAINERS = "containers";
	public static final String DOCUMENTS = "documents";
	public static final String FOLDERS_MEDIA_TYPES = "foldersMediaTypes";
	public static final String STATUS = "status";
	public static final String UNIFORM_COPY_RULE = "uniformCopyRule";
	public static final String UNIFORM_COPY_TYPE = "uniformCopyType";
	public static final String UNIFORM_CATEGORY = "uniformCategory";
	public static final String UNIFORM_RULE = "uniformRule";
	public static final String UNIFORM = "uniform";
	public static final String ORIGIN_ARCHIVISTIC_STATUS = "originArchivisticStatus";
	public static final String VALIDATIONS = "validations";
	public static final String PENDING_VALIDATIONS = "pendingValidations";
	public static final String COMMENTS = "comments";
	public static final String DOCUMENTS_REPORT_CONTENT = "documentsReportContent";
	public static final String FOLDERS_REPORT_CONTENT = "foldersReportContent";
	public static final String CONTENTS = "contents";
	public static final String CURRENT_BATCH_PROCESS_ID = "currentBatchProcess";

	// Disabled fields
	public static final String VALIDATION_DATE = "validationDate";    // never used, disabled in 5.1.0
	public static final String VALIDATION_USER = "validationUser";    // never used, disabled in 5.1.0

	public DecommissioningList(Record record,
							   MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public DecommissioningList setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	//Description
	public String getDescription() {
		return get(DESCRIPTION);
	}

	public DecommissioningList setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	//Description
	public DecommissioningListType getDecommissioningListType() {
		return get(TYPE);
	}

	public DecommissioningList setDecommissioningListType(DecommissioningListType type) {
		set(TYPE, type);
		return this;
	}

	//FilingSpace
	public String getFilingSpace() {
		return get(FILING_SPACE);
	}

	public DecommissioningList setFilingSpace(String filingSpace) {
		set(FILING_SPACE, filingSpace);
		return this;
	}

	public DecommissioningList setFilingSpace(Record filingSpace) {
		set(FILING_SPACE, filingSpace);
		return this;
	}

	public DecommissioningList setFilingSpace(FilingSpace filingSpace) {
		set(FILING_SPACE, filingSpace);
		return this;
	}

	//AdministrativeUnit
	public String getAdministrativeUnit() {
		return get(ADMINISTRATIVE_UNIT);
	}

	public DecommissioningList setAdministrativeUnit(String administrativeUnit) {
		set(ADMINISTRATIVE_UNIT, administrativeUnit);
		return this;
	}

	public DecommissioningList setAdministrativeUnit(Record administrativeUnit) {
		set(ADMINISTRATIVE_UNIT, administrativeUnit);
		return this;
	}

	public DecommissioningList setAdministrativeUnit(AdministrativeUnit administrativeUnit) {
		set(ADMINISTRATIVE_UNIT, administrativeUnit);
		return this;
	}

	//Validations
	public List<DecomListValidation> getValidations() {
		return getList(VALIDATIONS);
	}

	public DecommissioningList setValidations(List<DecomListValidation> validations) {
		set(VALIDATIONS, validations);
		return this;
	}

	public DecommissioningList addValidationRequest(String userId, LocalDate requestDate) {
		List<DecomListValidation> validations = new ArrayList<>(getValidations());
		validations.add(new DecomListValidation(userId, requestDate));
		return setValidations(validations);
	}

	public DecommissioningList addValidationRequest(User user, LocalDate requestDate) {
		return addValidationRequest(user.getId(), requestDate);
	}

	public DecomListValidation getValidationFor(String userId) {
		for (DecomListValidation validation : getValidations()) {
			if (validation.getUserId().equals(userId)) {
				return validation;
			}
		}
		return null;
	}

	public DecommissioningList removeValidationRequest(DecomListValidation validation) {
		List<DecomListValidation> validations = new ArrayList<>(getValidations());
		validations.remove(validation);
		return setValidations(validations);
	}

	//ApprovalRequestDate
	public LocalDate getApprovalRequestDate() {
		return get(APPROVAL_REQUEST_DATE);
	}

	public DecommissioningList setApprovalRequestDate(LocalDate approvalRequestDate) {
		set(APPROVAL_REQUEST_DATE, approvalRequestDate);
		return this;
	}

	//ApprovalRequest
	public String getApprovalRequest() {
		return get(APPROVAL_REQUEST);
	}

	public DecommissioningList setApprovalRequest(String approvalRequest) {
		set(APPROVAL_REQUEST, approvalRequest);
		return this;
	}

	public DecommissioningList setApprovalRequest(Record approvalRequest) {
		set(APPROVAL_REQUEST, approvalRequest);
		return this;
	}

	public DecommissioningList setApprovalRequest(User approvalRequest) {
		set(APPROVAL_REQUEST, approvalRequest);
		return this;
	}

	//ApprovalDate
	public LocalDate getApprovalDate() {
		return get(APPROVAL_DATE);
	}

	public DecommissioningList setApprovalDate(LocalDate approvalDate) {
		set(APPROVAL_DATE, approvalDate);
		return this;
	}

	//ApprovalUser
	public String getApprovalUser() {
		return get(APPROVAL_USER);
	}

	public DecommissioningList setApprovalUser(String approvalUser) {
		set(APPROVAL_USER, approvalUser);
		return this;
	}

	public DecommissioningList setApprovalUser(Record approvalUser) {
		set(APPROVAL_USER, approvalUser);
		return this;
	}

	public DecommissioningList setApprovalUser(User approvalUser) {
		set(APPROVAL_USER, approvalUser);
		return this;
	}

	public String getCurrentBatchProcessId() {
		return get(CURRENT_BATCH_PROCESS_ID);
	}

	public DecommissioningList setCurrentBatchProcessId(String batchProcessId) {
		set(CURRENT_BATCH_PROCESS_ID, batchProcessId);
		return this;
	}

	//ProcessingDate
	public LocalDate getProcessingDate() {
		return get(PROCESSING_DATE);
	}

	public DecommissioningList setProcessingDate(LocalDate approvalDate) {
		set(PROCESSING_DATE, approvalDate);
		return this;
	}

	//ProcessingUser
	public String getProcessingUser() {
		return get(PROCESSING_USER);
	}

	public DecommissioningList setProcessingUser(String processingUser) {
		set(PROCESSING_USER, processingUser);
		return this;
	}

	public DecommissioningList setProcessingUser(Record processingUser) {
		set(PROCESSING_USER, processingUser);
		return this;
	}

	public DecommissioningList setProcessingUser(User processingUser) {
		set(PROCESSING_USER, processingUser);
		return this;
	}

	//OriginArchivisticStatus
	public OriginStatus getOriginArchivisticStatus() {
		return get(ORIGIN_ARCHIVISTIC_STATUS);
	}

	public DecommissioningList setOriginArchivisticStatus(OriginStatus originStatus) {
		set(ORIGIN_ARCHIVISTIC_STATUS, originStatus);
		return this;
	}

	public List<Content> getContent() {
		return get(CONTENTS);
	}

	public DecommissioningList setContent(List<Content> contents) {
		set(CONTENTS, contents);
		return this;
	}

	public List<DecomListFolderDetail> getFolderDetails() {
		return getList(FOLDER_DETAILS);
	}

	public DecomListFolderDetail getFolderDetail(String folderId) {
		for (DecomListFolderDetail detail : getFolderDetails()) {
			if (folderId.equals(detail.getFolderId())) {
				return detail;
			}
		}
		return null;
	}

	public FolderDetailWithType getFolderDetailWithType(String folderId) {
		for (FolderDetailWithType detail : getFolderDetailsWithType()) {
			if (folderId.equals(detail.getFolderId())) {
				return detail;
			}
		}
		return null;
	}

	//	public DecommissioningList setFolderDetailsFor(String... folders) {
	//		return setFolderDetailsFor(asList(folders));
	//	}
	//
	//	public DecommissioningList addFolderDetailsFor(String... folders) {
	//		List<DecomListFolderDetail> details = new ArrayList<>();
	//		details.addAll(getFolderDetails());
	//		List<String> existingDetails = getFolders();
	//		for (String folder : folders) {
	//			if(!existingDetails.contains(folder)) {
	//				details.add(new DecomListFolderDetail(folder));
	//			}
	//		}
	//		return setFolderDetails(details);
	//	}
	//
	//	public DecommissioningList setFolderDetailsFor(List<String> folders) {
	//		List<DecomListFolderDetail> details = new ArrayList<>();
	//		for (String folder : folders) {
	//			details.add(new DecomListFolderDetail(folder));
	//		}
	//		return setFolderDetails(details);
	//	}

	public DecommissioningList removeFolderDetail(String folderId) {
		List<DecomListFolderDetail> details = new ArrayList<>();
		for (DecomListFolderDetail detail : getFolderDetails()) {
			if (!folderId.equals(detail.getFolderId())) {
				details.add(detail);
			}
		}
		return setFolderDetails(details);
	}

	public DecommissioningList removeFolderDetails(List<String> folderIds) {
		List<DecomListFolderDetail> details = new ArrayList<>();
		for (DecomListFolderDetail detail : getFolderDetails()) {
			if (!folderIds.contains(detail.getFolderId())) {
				details.add(detail);
			}
		}
		return setFolderDetails(details);
	}

	public DecommissioningList addFolderDetailsFor(FolderDetailStatus folderDetailStatus, Folder... folders) {
		List<DecomListFolderDetail> details = new ArrayList<>();
		details.addAll(getFolderDetails());
		List<String> existingDetails = getFolders();
		for (Folder folder : folders) {
			if (!existingDetails.contains(folder.getId())) {
				details.add(new DecomListFolderDetail(folder, folderDetailStatus));
			}
		}
		return setFolderDetails(details);
	}

	public DecommissioningList setFolderDetailsFor(List<Folder> folders, FolderDetailStatus folderDetailStatus) {
		List<DecomListFolderDetail> details = new ArrayList<>();
		for (Folder folder : folders) {
			details.add(new DecomListFolderDetail(folder, folderDetailStatus).setFolderDetailStatus(folderDetailStatus));
		}
		setFolderDetails(details);
		return this;
	}

	public DecommissioningList setFolderDetailsForIds(List<String> folders, FolderDetailStatus folderDetailStatus) {
		List<DecomListFolderDetail> details = new ArrayList<>();
		for (String folder : folders) {
			details.add(new DecomListFolderDetail().setFolderId(folder).setFolderDetailStatus(folderDetailStatus));
		}
		setFolderDetails(details);
		return this;
	}

	public DecommissioningList setAlreadyIncludedFolderDetailsForIds(List<String> folders) {
		List<DecomListFolderDetail> details = new ArrayList<>();
		for (String folder : folders) {
			details.add(new DecomListFolderDetail().setFolderId(folder).setFolderDetailStatus(FolderDetailStatus.INCLUDED));
		}
		setFolderDetails(details);
		return this;
	}

	public DecommissioningList setFolderDetailsForIds(FolderDetailStatus folderDetailStatus, String... folders) {
		List<DecomListFolderDetail> details = new ArrayList<>();
		for (String folder : folders) {
			details.add(new DecomListFolderDetail().setFolderId(folder).setFolderDetailStatus(folderDetailStatus));
		}
		setFolderDetails(details);
		return this;
	}

	public DecommissioningList setAlreadyIncludedFolderDetailsForIds(String... folders) {
		List<DecomListFolderDetail> details = new ArrayList<>();
		for (String folder : folders) {
			details.add(new DecomListFolderDetail().setFolderId(folder).setFolderDetailStatus(FolderDetailStatus.INCLUDED));
		}
		setFolderDetails(details);
		return this;
	}

	public DecommissioningList setContainerDetails(List<DecomListContainerDetail> containerDetails) {
		set(CONTAINER_DETAILS, containerDetails);
		return this;
	}

	public List<DecomListContainerDetail> getContainerDetails() {
		return getList(CONTAINER_DETAILS);
	}

	public DecomListContainerDetail getContainerDetail(String containerId) {
		for (DecomListContainerDetail detail : getContainerDetails()) {
			if (containerId.equals(detail.getContainerRecordId())) {
				return detail;
			}
		}
		return null;
	}

	public DecommissioningList setContainerDetailsFor(String... containers) {
		return setContainerDetailsFor(asList(containers));
	}

	public DecommissioningList setContainerDetailsFor(List<String> containers) {
		List<DecomListContainerDetail> details = new ArrayList<>();
		for (String container : containers) {
			details.add(new DecomListContainerDetail(container));
		}
		return setContainerDetails(details);
	}

	public DecommissioningList removeContainerDetail(String containerId) {
		List<DecomListContainerDetail> details = new ArrayList<>();
		for (DecomListContainerDetail detail : getContainerDetails()) {
			if (!containerId.equals(detail.getContainerRecordId())) {
				details.add(detail);
			}
		}
		return setContainerDetails(details);
	}

	public DecommissioningList setContainerDetailsFrom(List<ContainerRecord> containers) {
		List<DecomListContainerDetail> details = new ArrayList<>();
		for (ContainerRecord container : containers) {
			details.add(new DecomListContainerDetail(container.getId()).setFull(container.isFull()));
		}
		return setContainerDetails(details);
	}

	public DecommissioningList addContainerDetailsFrom(List<ContainerRecord> containers) {
		List<String> previousContainers = getContainers();
		List<DecomListContainerDetail> details = new ArrayList<>(getContainerDetails());
		for (ContainerRecord container : containers) {
			if (!(previousContainers != null && previousContainers.contains(container.getId()))) {
				DecomListContainerDetail detail = new DecomListContainerDetail(container);
				details.add(detail);
			}
		}
		return setContainerDetails(details);
	}

	public DecommissioningList addContainerDetailsFromFolders(Folder... folders) {
		List<String> previousContainers = getContainers();
		List<DecomListContainerDetail> details = new ArrayList<>(getContainerDetails());
		for (Folder folder : folders) {
			String containerId = folder.getContainer();
			if (!(previousContainers != null && previousContainers.contains(containerId)) && containerId != null) {
				DecomListContainerDetail detail = new DecomListContainerDetail(containerId);
				details.add(detail);
			}
		}
		return setContainerDetails(details);
	}

	public DecommissioningList setFolderDetails(List<DecomListFolderDetail> folderDetails) {
		set(FOLDER_DETAILS, folderDetails);
		return this;
	}

	public List<Comment> getComments() {
		return getList(COMMENTS);
	}

	public DecommissioningList setComments(List<Comment> comments) {
		set(COMMENTS, comments);
		return this;
	}

	public List<String> getFolders() {
		return get(FOLDERS);
	}

	//Containers
	public List<String> getContainers() {
		return get(CONTAINERS);
	}

	public boolean hasAnalogicalMedium() {
		return get(ANALOGICAL_MEDIUM);
	}

	public boolean hasElectronicMedium() {
		return get(ELECTRONIC_MEDIUM);
	}

	public boolean isUniform() {
		return get(UNIFORM);
	}

	public String getUniformCategory() {
		return get(UNIFORM_CATEGORY);
	}

	public CopyRetentionRule getUniformCopyRule() {
		return get(UNIFORM_COPY_RULE);
	}

	public String getUniformRule() {
		return get(UNIFORM_RULE);
	}

	public CopyType getUniformCopyType() {
		return get(UNIFORM_COPY_TYPE);
	}

	public List<FolderMediaType> getFoldersMediaTypes() {
		return getList(FOLDERS_MEDIA_TYPES);
	}

	public DecomListStatus getStatus() {
		return get(STATUS);
	}

	public List<FolderDetailWithType> getFolderDetailsWithType() {
		List<DecomListFolderDetail> details = getFolderDetails();
		List<FolderMediaType> types = getFoldersMediaTypes();
		List<FolderDetailWithType> result = new ArrayList<>();
		for (int i = 0; i < details.size(); i++) {
			result.add(new FolderDetailWithType(details.get(i), types.get(i), getDecommissioningListType()));
		}
		return result;
	}

	public boolean isUnprocessed() {
		return getStatus() != DecomListStatus.PROCESSED;
	}

	public boolean isProcessed() {
		return getStatus() == DecomListStatus.PROCESSED;
	}

	public boolean isApproved() {
		return getApprovalDate() != null;
	}

	public boolean isValidated() {
		List<DecomListValidation> validations = getValidations();
		if (validations.isEmpty()) {
			return false;
		}
		for (DecomListValidation validation : validations) {
			if (!validation.isValidated()) {
				return false;
			}
		}
		return true;
	}

	public boolean isFromActive() {
		return getOriginArchivisticStatus() == OriginStatus.ACTIVE;
	}

	public boolean isFromSemiActive() {
		return getOriginArchivisticStatus() == OriginStatus.SEMI_ACTIVE;
	}

	public boolean isToInactive() {
		return getDecommissioningListType().isDepositOrDestroyal();
	}

	public Content getDocumentsReportContent() {
		return get(DOCUMENTS_REPORT_CONTENT);
	}

	public DecommissioningList setDocumentsReportContent(Content content) {
		set(DOCUMENTS_REPORT_CONTENT, content);
		return this;
	}

	public Content getFoldersReportContent() {
		return get(FOLDERS_REPORT_CONTENT);
	}

	public DecommissioningList setFoldersReportContent(Content content) {
		set(FOLDERS_REPORT_CONTENT, content);
		return this;
	}

	public DecommissioningList setDocuments(List<String> documents) {
		set(DOCUMENTS, documents);
		return this;
	}

	public DecommissioningList addDocuments(String... documents) {
		List<String> documentIDs = new ArrayList<>();
		documentIDs.addAll(getDocuments());
		List<String> existingDocuments = getDocuments();
		for (String document : documents) {
			if (!existingDocuments.contains(document)) {
				documentIDs.add(document);
			}
		}
		return setDocuments(documentIDs);
	}

	public List<String> getDocuments() {
		return getList(DOCUMENTS);
	}

	public DecommissioningList removeDocuments(String... idsToRemove) {
		ArrayList<String> ids = new ArrayList<>(getDocuments());
		for (int i = 0; i < idsToRemove.length; i++) {
			ids.remove(idsToRemove[i]);
		}
		return setDocuments(ids);
	}

	public DecommissioningList removeDocument(String id) {
		ArrayList<String> ids = new ArrayList<>(getDocuments());
		ids.remove(id);
		return setDocuments(ids);
	}

	public DecommissioningList removeReferences(Record... recordsToRemove) {
		List<String> documentsToRemove = new ArrayList<>();
		List<String> foldersToRemove = new ArrayList<>();

		for (Record record : recordsToRemove) {
			if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
				foldersToRemove.add(record.getId());
			} else if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
				documentsToRemove.add(record.getId());
			}
		}

		removeDocuments(documentsToRemove.toArray(new String[0]));
		removeFolderDetails(foldersToRemove);
		return this;
	}
}
