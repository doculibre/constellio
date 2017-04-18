package com.constellio.app.modules.rm.ui.entities;

import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.model.enums.RetentionType;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.Arrays;
import java.util.List;

import static com.constellio.app.modules.rm.wrappers.Folder.*;

public class FolderVO extends RecordVO {

	public FolderVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
		super(id, metadataValues, viewMode);
	}

	public FolderVO(RecordVO recordVO) {
		super(recordVO.getId(), recordVO.getMetadataValues(), recordVO.getViewMode());
	}

	public String getParentFolder() {
		return get(PARENT_FOLDER);
	}

	public void setParentFolder(String folder) {
		set(PARENT_FOLDER, folder);
	}

	public void setParentFolder(FolderVO folder) {
		set(PARENT_FOLDER, folder);
	}

	public void setParentFolder(RecordVO folder) {
		set(PARENT_FOLDER, folder);
	}

	public String getContainer() {
		return get(CONTAINER);
	}

	public void setContainer(String container) {
		set(CONTAINER, container);
	}

	public void setContainer(RecordVO container) {
		set(CONTAINER, container);
	}

	public String getUniformSubdivision() {
		return get(UNIFORM_SUBDIVISION_ENTERED);
	}

	public void setUniformSubdivision(String uniformSubdivision) {
		set(UNIFORM_SUBDIVISION_ENTERED, uniformSubdivision);
	}

	public void setUniformSubdivision(RecordVO uniformSubdivision) {
		set(UNIFORM_SUBDIVISION_ENTERED, uniformSubdivision);
	}

	public String getAdministrativeUnit() {
		return get(ADMINISTRATIVE_UNIT_ENTERED);
	}

	public void setAdministrativeUnit(String administrativeUnit) {
		set(ADMINISTRATIVE_UNIT_ENTERED, administrativeUnit);
	}

	public void setAdministrativeUnit(RecordVO administrativeUnit) {
		set(ADMINISTRATIVE_UNIT_ENTERED, administrativeUnit);
	}

	public String getFilingSpace() {
		return get(FILING_SPACE_ENTERED);
	}

	public String getFilingSpaceCode() {
		return get(FILING_SPACE_CODE);
	}

	public void setFilingSpace(String filingSpace) {
		set(FILING_SPACE_ENTERED, filingSpace);
	}

	public void setFilingSpace(RecordVO filingSpace) {
		set(FILING_SPACE_ENTERED, filingSpace);
	}

	public String getCategory() {
		return get(CATEGORY_ENTERED);
	}

	public void setCategory(String category) {
		set(CATEGORY_ENTERED, category);
	}

	public void setCategory(RecordVO category) {
		set(CATEGORY_ENTERED, category);
	}

	public String getCategoryCode() {
		return get(CATEGORY_CODE);
	}

	public List<String> getMediumTypes() {
		return get(MEDIUM_TYPES);
	}

	public void setMediumTypes(String... mediumTypes) {
		setMediumTypes(Arrays.asList(mediumTypes));
	}

	public void setMediumTypes(List<?> mediumTypes) {
		set(MEDIUM_TYPES, mediumTypes);
	}

	public RetentionType getActiveRetentionType() {
		return get(ACTIVE_RETENTION_TYPE);
	}

	public RetentionType getSemiActiveRetentionType() {
		return get(SEMIACTIVE_RETENTION_TYPE);
	}

	public DisposalType getInactiveDisposalType() {
		return get(INACTIVE_DISPOSAL_TYPE);
	}

	public String getRetentionRule() {
		return get(RETENTION_RULE_ENTERED);
	}

	public void setRetentionRule(String retentionRule) {
		set(RETENTION_RULE_ENTERED, retentionRule);
	}

	public void setRetentionRule(RecordVO retentionRule) {
		set(RETENTION_RULE_ENTERED, retentionRule);
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public void setDescription(String description) {
		set(DESCRIPTION, description);
	}

	public String getKeywords() {
		return get(KEYWORDS);
	}

	public void setKeywords(String keywords) {
		set(KEYWORDS, keywords);
	}

	public LocalDate getCloseDate() {
		return get(CLOSING_DATE);
	}

	public LocalDate getCloseDateEntered() {
		return get(ENTERED_CLOSING_DATE);
	}

	public void setCloseDateEntered(LocalDate closeDate) {
		set(ENTERED_CLOSING_DATE, closeDate);
	}

	public LocalDate getOpenDate() {
		return get(OPENING_DATE);
	}

	public void setOpenDate(LocalDate openDate) {
		set(OPENING_DATE, openDate);
	}

	public List<LocalDate> getCopyRulesExpectedTransferDates() {
		return getList(COPY_RULES_EXPECTED_TRANSFER_DATES);
	}

	public LocalDate getExpectedTransferDate() {
		return get(EXPECTED_TRANSFER_DATE);
	}

	public LocalDate getActualTransferDate() {
		return get(ACTUAL_TRANSFER_DATE);
	}

	public void setActualTransferDate(LocalDate transferDate) {
		set(ACTUAL_TRANSFER_DATE, transferDate);
	}

	public List<LocalDate> getCopyRulesExpectedDepositDates() {
		return getList(COPY_RULES_EXPECTED_DEPOSIT_DATES);
	}

	public LocalDate getExpectedDepositDate() {
		return get(EXPECTED_DEPOSIT_DATE);
	}

	public LocalDate getActualDepositDate() {
		return get(ACTUAL_DEPOSIT_DATE);
	}

	public void setActualDepositDate(LocalDate depositDate) {
		set(ACTUAL_DEPOSIT_DATE, depositDate);
	}

	public List<LocalDate> getCopyRulesExpectedDestructionDates() {
		return getList(COPY_RULES_EXPECTED_DESTRUCTION_DATES);
	}

	public LocalDate getExpectedDestructionDate() {
		return get(EXPECTED_DESTRUCTION_DATE);
	}

	public LocalDate getActualDestructionDate() {
		return get(ACTUAL_DESTRUCTION_DATE);
	}

	public void setActualDestructionDate(LocalDate destructionDate) {
		set(ACTUAL_DESTRUCTION_DATE, destructionDate);
	}

	public String getType() {
		return get(TYPE);
	}

	public void setType(RecordVO type) {
		set(TYPE, type);
	}

	public void setType(String type) {
		set(TYPE, type);
	}

	public void setCopyStatusEntered(CopyType type) {
		set(COPY_STATUS_ENTERED, type);
	}

	public CopyType getCopyStatusEntered() {
		return get(COPY_STATUS_ENTERED);
	}

	public FolderStatus getArchivisticStatus() {
		return get(ARCHIVISTIC_STATUS);
	}

	public CopyType getCopyStatus() {
		return get(COPY_STATUS);
	}

	public LocalDate getDecommissioningDate() {
		return get(DECOMMISSIONING_DATE);
	}

	public void setBorrowed(Boolean borrowed) {
		set(BORROWED, borrowed);
	}

	public Boolean getBorrowed() {
		return get(BORROWED);
	}

	public void setBorrowDate(LocalDateTime borrowDate) {
		set(BORROW_DATE, borrowDate);
	}

	public LocalDateTime getBorrowDate() {
		return get(BORROW_DATE);
	}

	public void setPreviewReturnDate(LocalDateTime previewReturnDate) {
		set(BORROW_PREVIEW_RETURN_DATE, previewReturnDate);
	}

	public LocalDate getPreviewReturnDate() {
		return get(BORROW_PREVIEW_RETURN_DATE);
	}

	public void setBorrowUserId(String borrowUserId) {
		set(BORROW_USER, borrowUserId);
	}

	public String getBorrowUserId() {
		return get(BORROW_USER);
	}

	public void setLinearSize(Double linearSize) {
		set(LINEAR_SIZE, linearSize);
	}

}
