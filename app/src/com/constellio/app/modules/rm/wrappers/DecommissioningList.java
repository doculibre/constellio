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
package com.constellio.app.modules.rm.wrappers;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class DecommissioningList extends RecordWrapper {
	public static final String SCHEMA_TYPE = "decommissioningList";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String DESCRIPTION = "description";
	public static final String TYPE = "type";
	public static final String FILING_SPACE = "filingSpace";
	public static final String ADMINISTRATIVE_UNIT = "administrativeUnit";
	public static final String ANALOGICAL_MEDIUM = "analogicalMedium";
	public static final String ELECTRONIC_MEDIUM = "electronicMedium";
	public static final String VALIDATION_DATE = "validationDate";
	public static final String VALIDATION_USER = "validationUser";
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
	public static final String FOLDERS_MEDIA_TYPES = "foldersMediaTypes";
	public static final String STATUS = "status";
	public static final String UNIFORM_COPY_RULE = "uniformCopyRule";
	public static final String UNIFORM_COPY_TYPE = "uniformCopyType";
	public static final String UNIFORM_CATEGORY = "uniformCategory";
	public static final String UNIFORM_RULE = "uniformRule";
	public static final String UNIFORM = "uniform";
	public static final String ORIGIN_ARCHIVISTIC_STATUS = "originArchivisticStatus";

	public static final String COMMENTS = "comments";

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

	//ValidationDate
	public LocalDate getValidationDate() {
		return get(VALIDATION_DATE);
	}

	public DecommissioningList setValidationDate(String validationDate) {
		set(VALIDATION_DATE, validationDate);
		return this;
	}

	public DecommissioningList setValidationDate(LocalDate validationDate) {
		set(VALIDATION_DATE, validationDate);
		return this;
	}

	//validationUser
	public String getValidationUser() {
		return get(VALIDATION_USER);
	}

	public DecommissioningList setValidationUser(String validationUser) {
		set(VALIDATION_USER, validationUser);
		return this;
	}

	public DecommissioningList setValidationUser(Record validationUser) {
		set(VALIDATION_USER, validationUser);
		return this;
	}

	public DecommissioningList setValidationUser(User validationUser) {
		set(VALIDATION_USER, validationUser);
		return this;
	}

	//ApprovalRequestDate
	public LocalDate getApprovalRequestDate() {
		return get(APPROVAL_REQUEST_DATE);
	}

	public DecommissioningList setApprovalRequestDate(String approvalRequestDate) {
		set(APPROVAL_REQUEST_DATE, approvalRequestDate);
		return this;
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

	public DecommissioningList setApprovalDate(String approvalDate) {
		set(APPROVAL_DATE, approvalDate);
		return this;
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

	//ProcessingDate
	public LocalDate getProcessingDate() {
		return get(PROCESSING_DATE);
	}

	public DecommissioningList setProcessingDate(String processingDate) {
		set(PROCESSING_DATE, processingDate);
		return this;
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

	public DecommissioningList setFolderDetailsFor(String... folders) {
		return setFolderDetailsFor(asList(folders));
	}

	public DecommissioningList setFolderDetailsFor(List<String> folders) {
		List<DecomListFolderDetail> details = new ArrayList<>();
		for (String folder : folders) {
			details.add(new DecomListFolderDetail(folder));
		}
		setFolderDetails(details);
		return this;
	}

	public DecommissioningList removeFolderDetail(String folderId) {
		List<DecomListFolderDetail> details = new ArrayList<>();
		for (DecomListFolderDetail detail : getFolderDetails()) {
			if (!folderId.equals(detail.getFolderId())) {
				details.add(detail);
			}
		}
		setFolderDetails(details);
		return this;
	}

	public DecommissioningList setFolderDetailsFrom(List<Folder> folders) {
		List<DecomListFolderDetail> details = new ArrayList<>();
		for (Folder folder : folders) {
			details.add(new DecomListFolderDetail(folder.getId()).setContainerRecordId(folder.getContainer()));
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

	public DecommissioningList setContainerDetailsFor(String... containers) {
		return setContainerDetailsFor(asList(containers));
	}

	public DecommissioningList setContainerDetailsFor(List<String> containers) {
		List<DecomListContainerDetail> details = new ArrayList<>();
		for (String container : containers) {
			details.add(new DecomListContainerDetail(container));
		}
		setContainerDetails(details);
		return this;
	}

	public DecommissioningList setContainerDetailsFrom(List<ContainerRecord> containers) {
		List<DecomListContainerDetail> details = new ArrayList<>();
		for (ContainerRecord container : containers) {
			details.add(new DecomListContainerDetail(container.getId()).setFull(container.isFull()));
		}
		setContainerDetails(details);
		return this;
	}

	public DecommissioningList addContainerDetailsFrom(List<ContainerRecord> containers) {
		List<DecomListContainerDetail> details = new ArrayList<>(getContainerDetails());
		for (ContainerRecord container : containers) {
			DecomListContainerDetail detail = new DecomListContainerDetail(container.getId())
					.setFull(container.isFull() != null && container.isFull());
			details.add(detail);
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

	public boolean isFromActive() {
		return getOriginArchivisticStatus() == OriginStatus.ACTIVE;
	}

	public boolean isFromSemiActive() {
		return getOriginArchivisticStatus() == OriginStatus.SEMI_ACTIVE;
	}

	public boolean isToInactive() {
		return getDecommissioningListType().isDepositOrDestroyal();
	}
}
