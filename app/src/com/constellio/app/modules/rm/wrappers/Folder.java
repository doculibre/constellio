package com.constellio.app.modules.rm.wrappers;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.model.enums.RetentionType;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.PendingAlert;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class Folder extends RMObject {
	public static final String SCHEMA_TYPE = "folder";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String ADMINISTRATIVE_UNIT_ENTERED = "administrativeUnitEntered";
	public static final String ADMINISTRATIVE_UNIT = "administrativeUnit";
	public static final String ADMINISTRATIVE_UNIT_ANCESTORS = "administrativeUnitAncestors";
	public static final String ARCHIVISTIC_STATUS = "archivisticStatus";
	public static final String PERMISSION_STATUS = "permissionStatus";
	public static final String UNIFORM_SUBDIVISION = "uniformSubdivision";
	public static final String UNIFORM_SUBDIVISION_ENTERED = "uniformSubdivisionEntered";
	public static final String CATEGORY_ENTERED = "categoryEntered";
	public static final String CATEGORY = "category";
	public static final String CATEGORY_CODE = "categoryCode";
	public static final String MAIN_COPY_RULE = "mainCopyRule";
	public static final String MAIN_COPY_RULE_ID_ENTERED = "mainCopyRuleIdEntered";
	public static final String APPLICABLE_COPY_RULES = "applicableCopyRule";
	public static final String COPY_STATUS = "copyStatus";
	public static final String COPY_STATUS_ENTERED = "copyStatusEntered";
	public static final String CLOSING_DATE = "closingDate";
	public static final String ENTERED_CLOSING_DATE = "enteredClosingDate";
	public static final String DESCRIPTION = "description";
	public static final String FILING_SPACE_ENTERED = "filingSpaceEntered";
	public static final String FILING_SPACE = "filingSpace";
	public static final String FILING_SPACE_CODE = "filingSpaceCode";
	public static final String KEYWORDS = "keywords";
	public static final String MEDIUM_TYPES = "mediumTypes";
	public static final String OPENING_DATE = "openingDate";
	public static final String PARENT_FOLDER = "parentFolder";
	public static final String ACTUAL_TRANSFER_DATE = "actualTransferDate";
	public static final String ACTUAL_DEPOSIT_DATE = "actualDepositDate";
	public static final String ACTUAL_DESTRUCTION_DATE = "actualDestructionDate";
	public static final String COPY_RULES_EXPECTED_TRANSFER_DATES = "copyRulesExpectedTransferDates";
	public static final String EXPECTED_TRANSFER_DATE = "expectedTransferDate";
	public static final String COPY_RULES_EXPECTED_DEPOSIT_DATES = "copyRulesExpectedDepositDates";
	public static final String EXPECTED_DEPOSIT_DATE = "expectedDepositDate";
	public static final String COPY_RULES_EXPECTED_DESTRUCTION_DATES = "copyRulesExpectedDestructionDates";
	public static final String EXPECTED_DESTRUCTION_DATE = "expectedDestructionDate";
	public static final String RETENTION_RULE = "retentionRule";
	public static final String RETENTION_RULE_ENTERED = "retentionRuleEntered";

	public static final String DECOMMISSIONING_DATE = "decommissioningDate";
	public static final String ACTIVE_RETENTION_TYPE = "activeRetentionType";
	public static final String ACTIVE_RETENTION_CODE = "activeRetentionPeriodCode";
	public static final String SEMIACTIVE_RETENTION_TYPE = "semiactiveRetentionType";
	public static final String SEMIACTIVE_RETENTION_CODE = "semiactiveRetentionPeriodCode";
	public static final String INACTIVE_DISPOSAL_TYPE = "inactiveDisposalType";
	public static final String MEDIA_TYPE = "mediaType";
	public static final String CONTAINER = "container";
	public static final String TYPE = "type";
	public static final String FOLDER_TYPE = "folderType";
	public static final String COMMENTS = "comments";
	public static final String BORROWED = "borrowed";
	public static final String BORROW_DATE = "borrowDate";
	public static final String BORROW_RETURN_DATE = "borrowReturnDate";
	public static final String BORROW_PREVIEW_RETURN_DATE = "borrowPreviewReturnDate";
	public static final String RETURN_BORROW_USER_ENTERED = "returnBorrowUserEntered";
	public static final String BORROW_USER = "borrowUser";
	public static final String BORROW_USER_ENTERED = "borrowUserEntered";
	public static final String BORROWING_TYPE = "borrowingType";
	public static final String LINEAR_SIZE = "linearSize";
	public static final String ALERT_USERS_WHEN_AVAILABLE = "alertUsersWhenAvailable";
	public static final String PENDING_ALERTS = "pendingAlerts";
	public static final String NEXT_ALERT_ON = "nextAlertOn";
	public static final String CREATED_BY_ROBOT = "createdByRobot";

	public static final String CALENDAR_YEAR_ENTERED = "calendarYearEntered";
	public static final String CALENDAR_YEAR = "calendarYear";

	public static final String MANUAL_EXPECTED_TRANSFER_DATE = "manualExpectedTransferDate";
	public static final String MANUAL_EXPECTED_DEPOSIT_DATE = "manualExpectedDepositDate";
	public static final String MANUAL_EXPECTED_DESTRUCTION_DATE = "manualExpectedDesctructionDate";
	public static final String MANUAL_ARCHIVISTIC_STATUS = "manualArchivisticStatus";

	public static final String TIME_RANGE = "timerange";

	//public static final String CALENDAR_YEAR_ENTERED = "calendarYearEntered";
	//public static final String CALENDAR_YEAR = "calendarYear";
	//TO DELETE
	public static final String RETENTION_RULE_ADMINISTRATIVE_UNITS = "ruleAdminUnit";

	public Folder(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public Folder setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public Double getLinearSize() {
		return get(LINEAR_SIZE);
	}

	public Folder setLinearSize(Double linearSize) {
		set(LINEAR_SIZE, linearSize);
		return this;
	}

	public String getParentFolder() {
		return get(PARENT_FOLDER);
	}

	public Folder setParentFolder(String folder) {
		set(PARENT_FOLDER, folder);
		return this;
	}

	public Folder setParentFolder(Folder folder) {
		set(PARENT_FOLDER, folder);
		return this;
	}

	public Folder setParentFolder(Record folder) {
		set(PARENT_FOLDER, folder);
		return this;
	}

	public String getContainer() {
		return get(CONTAINER);
	}

	public Folder setContainer(String container) {
		set(CONTAINER, container);
		return this;
	}

	public Folder setContainer(ContainerRecord container) {
		set(CONTAINER, container);
		return this;
	}

	public Folder setContainer(Record container) {
		set(CONTAINER, container);
		return this;
	}

	public String getUniformSubdivision() {
		return get(UNIFORM_SUBDIVISION);
	}

	public String getUniformSubdivisionEntered() {
		return get(UNIFORM_SUBDIVISION_ENTERED);
	}

	public Folder setUniformSubdivisionEntered(String uniformSubdivision) {
		set(UNIFORM_SUBDIVISION_ENTERED, uniformSubdivision);
		return this;
	}

	public Folder setUniformSubdivisionEntered(Record uniformSubdivision) {
		set(UNIFORM_SUBDIVISION_ENTERED, uniformSubdivision);
		return this;
	}

	public Folder setUniformSubdivisionEntered(UniformSubdivision uniformSubdivision) {
		set(UNIFORM_SUBDIVISION_ENTERED, uniformSubdivision);
		return this;
	}

	public String getApplicableAdministrative() {
		return get(ADMINISTRATIVE_UNIT);
	}

	public String getAdministrativeUnit() {
		return get(ADMINISTRATIVE_UNIT);
	}

	public String getAdministrativeUnitEntered() {
		return get(ADMINISTRATIVE_UNIT_ENTERED);
	}

	public Folder setAdministrativeUnitEntered(String administrativeUnit) {
		set(ADMINISTRATIVE_UNIT_ENTERED, administrativeUnit);
		return this;
	}

	public Folder setAdministrativeUnitEntered(Record administrativeUnit) {
		set(ADMINISTRATIVE_UNIT_ENTERED, administrativeUnit);
		return this;
	}

	public Folder setAdministrativeUnitEntered(AdministrativeUnit administrativeUnit) {
		set(ADMINISTRATIVE_UNIT_ENTERED, administrativeUnit);
		return this;
	}

	public String getFilingSpace() {
		return get(FILING_SPACE);
	}

	public String getFilingSpaceEntered() {
		return get(FILING_SPACE_ENTERED);
	}

	public String getFilingSpaceCode() {
		return get(FILING_SPACE_CODE);
	}

	public Folder setFilingSpaceEntered(String filingSpace) {
		set(FILING_SPACE_ENTERED, filingSpace);
		return this;
	}

	public Folder setFilingSpaceEntered(Record filingSpace) {
		set(FILING_SPACE_ENTERED, filingSpace);
		return this;
	}

	public Folder setFilingSpaceEntered(FilingSpace filingSpace) {
		set(FILING_SPACE_ENTERED, filingSpace);
		return this;
	}

	public String getCategory() {
		return get(CATEGORY);
	}

	public String getCategoryEntered() {
		return get(CATEGORY_ENTERED);
	}

	public Folder setCategoryEntered(String category) {
		set(CATEGORY_ENTERED, category);
		return this;
	}

	public Folder setCategoryEntered(Record category) {
		set(CATEGORY_ENTERED, category);
		return this;
	}

	public Folder setCategoryEntered(Category category) {
		set(CATEGORY_ENTERED, category);
		return this;
	}

	public String getCategoryCode() {
		return get(CATEGORY_CODE);
	}

	public List<String> getMediumTypes() {
		return get(MEDIUM_TYPES);
	}

	public Folder setMediumTypes(String... mediumTypes) {
		return setMediumTypes(Arrays.asList(mediumTypes));
	}

	public Folder setMediumTypes(List<?> mediumTypes) {
		set(MEDIUM_TYPES, mediumTypes);
		return this;
	}

	public RetentionType getActiveRetentionType() {
		return get(ACTIVE_RETENTION_TYPE);
	}

	public String getActiveRetentionCode() {
		return get(ACTIVE_RETENTION_CODE);
	}

	public RetentionType getSemiActiveRetentionType() {
		return get(SEMIACTIVE_RETENTION_TYPE);
	}

	public String getSemiActiveRetentionCode() {
		return get(SEMIACTIVE_RETENTION_CODE);
	}

	public DisposalType getInactiveDisposalType() {
		return get(INACTIVE_DISPOSAL_TYPE);
	}

	public String getRetentionRule() {
		return get(RETENTION_RULE);
	}

	public String getRetentionRuleEntered() {
		return get(RETENTION_RULE_ENTERED);
	}

	public Folder setRetentionRuleEntered(String retentionRule) {
		set(RETENTION_RULE_ENTERED, retentionRule);
		return this;
	}

	public Folder setRetentionRuleEntered(Record retentionRule) {
		set(RETENTION_RULE_ENTERED, retentionRule);
		return this;
	}

	public Folder setRetentionRuleEntered(RetentionRule retentionRule) {
		set(RETENTION_RULE_ENTERED, retentionRule);
		return this;
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public Folder setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public List<String> getKeywords() {
		return getList(KEYWORDS);
	}

	public Folder setKeywords(List<String> keywords) {
		set(KEYWORDS, keywords);
		return this;
	}

	public String getCreatedByRobot() {
		return get(CREATED_BY_ROBOT);
	}

	public Folder setCreatedByRobot(String robotId) {
		set(CREATED_BY_ROBOT, robotId);
		return this;
	}

	public LocalDate getCloseDate() {
		return get(CLOSING_DATE);
	}

	public LocalDate getCloseDateEntered() {
		return get(ENTERED_CLOSING_DATE);
	}

	public Folder setCloseDateEntered(LocalDate closeDate) {
		set(ENTERED_CLOSING_DATE, closeDate);
		return this;
	}

	public LocalDate getOpeningDate() {
		return get(OPENING_DATE);
	}

	public LocalDate getOpenDate() {
		return get(OPENING_DATE);
	}

	public Folder setOpenDate(LocalDate openDate) {
		set(OPENING_DATE, openDate);
		return this;
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

	public Folder setActualTransferDate(LocalDate transferDate) {
		set(ACTUAL_TRANSFER_DATE, transferDate);
		return this;
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

	public Folder setActualDepositDate(LocalDate depositDate) {
		set(ACTUAL_DEPOSIT_DATE, depositDate);
		return this;
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

	public Folder setActualDestructionDate(LocalDate destructionDate) {
		set(ACTUAL_DESTRUCTION_DATE, destructionDate);
		return this;
	}

	public String getType() {
		return get(TYPE);
	}

	public Folder setType(FolderType type) {
		set(TYPE, type);
		return this;
	}

	public Folder setType(Record type) {
		set(TYPE, type);
		return this;
	}

	public Folder setType(String type) {
		set(TYPE, type);
		return this;
	}

	public Folder setCopyStatusEntered(CopyType type) {
		set(COPY_STATUS_ENTERED, type);
		return this;
	}

	public List<Comment> getComments() {
		return getList(COMMENTS);
	}

	public Folder setComments(List<Comment> comments) {
		set(COMMENTS, comments);
		return this;
	}

	public CopyType getCopyStatusEntered() {
		return get(COPY_STATUS_ENTERED);
	}

	public List<String> getAdministrativeUnitAncestors() {
		return getList(ADMINISTRATIVE_UNIT_ANCESTORS);
	}

	public FolderStatus getArchivisticStatus() {
		return get(ARCHIVISTIC_STATUS);
	}

	public FolderStatus getPermissionStatus() {
		FolderStatus status = get(PERMISSION_STATUS);
		return status != null ? status : getArchivisticStatus();
	}

	public Folder setPermissionStatus(FolderStatus status) {
		set(PERMISSION_STATUS, status);
		return this;
	}

	public CopyType getCopyStatus() {
		return get(COPY_STATUS);
	}

	public CopyRetentionRule getMainCopyRule() {
		return get(MAIN_COPY_RULE);
	}

	public String getMainCopyRuleIdEntered() {
		return get(MAIN_COPY_RULE_ID_ENTERED);
	}

	public Folder setMainCopyRuleEntered(String mainCopyRuleIdEntered) {
		set(MAIN_COPY_RULE_ID_ENTERED, mainCopyRuleIdEntered);
		return this;
	}

	public List<CopyRetentionRule> getApplicableCopyRules() {
		return getList(APPLICABLE_COPY_RULES);
	}

	public LocalDate getDecommissioningDate() {
		return get(DECOMMISSIONING_DATE);
	}

	public FolderMediaType getMediaType() {
		return get(MEDIA_TYPE);
	}

	public Boolean getBorrowed() {
		return get(BORROWED);
	}

	public Folder setBorrowed(Boolean borrowed) {
		set(BORROWED, borrowed);
		return this;
	}

	public LocalDateTime getBorrowDate() {
		return get(BORROW_DATE);
	}

	public Folder setBorrowDate(LocalDateTime borrowDate) {
		set(BORROW_DATE, borrowDate);
		return this;
	}

	public LocalDateTime getBorrowReturnDate() {
		return get(BORROW_RETURN_DATE);
	}

	public Folder setBorrowReturnDate(LocalDateTime borrowReturnDate) {
		set(BORROW_RETURN_DATE, borrowReturnDate);
		return this;
	}

	public LocalDate getBorrowPreviewReturnDate() {
		return get(BORROW_PREVIEW_RETURN_DATE);
	}

	public Folder setBorrowPreviewReturnDate(LocalDate borrowPreviewReturnDate) {
		set(BORROW_PREVIEW_RETURN_DATE, borrowPreviewReturnDate);
		return this;
	}

	public String getReturnBorrowUserEntered() {
		return get(RETURN_BORROW_USER_ENTERED);
	}

	public Folder setReturnBorrowUserEntered(String returnBorrowUserEntered) {
		set(RETURN_BORROW_USER_ENTERED, returnBorrowUserEntered);
		return this;
	}

	public String getBorrowUser() {
		return get(BORROW_USER);
	}

	public Folder setBorrowUser(String borrowUser) {
		set(BORROW_USER, borrowUser);
		return this;
	}

	public String getBorrowUserEntered() {
		return get(BORROW_USER_ENTERED);
	}

	public Folder setBorrowUserEntered(String borrowUserEntered) {
		set(BORROW_USER_ENTERED, borrowUserEntered);
		return this;
	}

	public BorrowingType getBorrowType() {
		return get(BORROWING_TYPE);
	}

	public Folder setBorrowType(BorrowingType borrowType) {
		set(BORROWING_TYPE, borrowType);
		return this;
	}

	public boolean hasAnalogicalMedium() {
		FolderMediaType mediaType = getMediaType();
		return mediaType != null && mediaType.potentiallyHasAnalogMedium();
	}

	public boolean hasElectronicMedium() {
		FolderMediaType mediaType = getMediaType();
		return mediaType != null && mediaType.potentiallyHasElectronicMedium();
	}

	public List<String> getAlertUsersWhenAvailable() {
		return getList(ALERT_USERS_WHEN_AVAILABLE);
	}

	public Folder setAlertUsersWhenAvailable(List<String> users) {
		set(ALERT_USERS_WHEN_AVAILABLE, users);
		return this;
	}

	public List<String> getPendingAlerts() {
		return getList(PENDING_ALERTS);
	}

	public Folder setPendingAlerts(List<PendingAlert> pendingAlerts) {
		set(PENDING_ALERTS, pendingAlerts);
		return this;
	}

	public LocalDateTime getNextAlertOn() {
		return get(NEXT_ALERT_ON);
	}

	public Folder setNextAlertOn(LocalDateTime nextAlertOn) {
		set(PENDING_ALERTS, nextAlertOn);
		return this;
	}

	public Folder setFormCreatedBy(String userId) {
		set(FORM_CREATED_BY, userId);
		return this;
	}

	public Folder setFormCreatedBy(Record user) {
		set(FORM_CREATED_BY, user);
		return this;
	}

	public Folder setFormCreatedBy(User user) {
		set(FORM_CREATED_BY, user);
		return this;
	}

	public Folder setFormCreatedOn(LocalDateTime dateTime) {
		set(FORM_CREATED_ON, dateTime);
		return this;
	}

	public Folder setFormModifiedBy(String userId) {
		set(FORM_MODIFIED_BY, userId);
		return this;
	}

	public Folder setFormModifiedBy(Record user) {
		set(FORM_MODIFIED_BY, user);
		return this;
	}

	public Folder setFormModifiedBy(User user) {
		set(FORM_MODIFIED_BY, user);
		return this;
	}

	public Folder setFormModifiedOn(LocalDateTime dateTime) {
		set(FORM_MODIFIED_ON, dateTime);
		return this;
	}

	public LocalDate getManualExpecteTransferdDate() {
		return get(MANUAL_EXPECTED_TRANSFER_DATE);
	}

	public Folder setManualExpectedTransferDate(LocalDate date) {
		set(MANUAL_EXPECTED_TRANSFER_DATE, date);
		return this;
	}

	public LocalDate getManualExpectedDepositDate() {
		return get(MANUAL_EXPECTED_DEPOSIT_DATE);
	}

	public Folder setManualExpectedDepositDate(LocalDate date) {
		set(MANUAL_EXPECTED_DEPOSIT_DATE, date);
		return this;
	}

	public LocalDate getManualExpectedDestructionDate() {
		return get(MANUAL_EXPECTED_DESTRUCTION_DATE);
	}

	public Folder setManualExpectedDestructionDate(LocalDate date) {
		set(MANUAL_EXPECTED_DESTRUCTION_DATE, date);
		return this;
	}

	public FolderStatus getManualArchivisticStatus() {
		return get(MANUAL_ARCHIVISTIC_STATUS);
	}

	public Folder setManualArchivisticStatus(FolderStatus status) {
		set(MANUAL_ARCHIVISTIC_STATUS, status);
		return this;
	}

}

