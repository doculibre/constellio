package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDate;

import java.util.List;

public class ContainerRecord extends RecordWrapper {
	public static final String SCHEMA_TYPE = "containerRecord";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String LOCALIZATION = "localization";
	public static final String ADMINISTRATIVE_UNIT = "administrativeUnit";
	public static final String ADMINISTRATIVE_UNITS = "administrativeUnits";
	public static final String COMPLETION_DATE = "completionDate";
	public static final String DECOMMISSIONING_TYPE = "decommissioningType";
	public static final String DESCRIPTION = "description";
	public static final String FILING_SPACE = "filingSpace";
	public static final String FULL = "full";
	public static final String IDENTIFIER = "identifier";
	public static final String PLANIFIED_RETURN_DATE = "planifiedReturnDate";
	public static final String REAL_DEPOSIT_DATE = "realDepositDate";
	public static final String REAL_RETURN_DATE = "realReturnDate";
	public static final String REAL_TRANSFER_DATE = "realTransferDate";
	public static final String STORAGE_SPACE = "storageSpace";
	public static final String TEMPORARY_IDENTIFIER = "temporaryIdentifier";
	public static final String TYPE = "type";
	public static final String POSITION = "position";
	public static final String COMMENTS = "comments";
	public static final String BORROWED = "borrowed";
	public static final String BORROW_DATE = "borrowDate";
	public static final String BORROWER = "borrower";
	public static final String BORROW_RETURN_DATE = "borrowReturnDate";
	public static final String CAPACITY = "capacity";
	public static final String FILL_RATIO_ENTRED = "fillRatioEntered";
	public static final String LINEAR_SIZE_ENTERED = "linearSizeEntered";
	public static final String LINEAR_SIZE = "linearSize";
	public static final String LINEAR_SIZE_SUM = "linearSizeSum";
	public static final String AVAILABLE_SIZE = "availableSize";

	public ContainerRecord(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public ContainerRecord setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public ContainerRecord setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public String getLocalization() {
		return get(LOCALIZATION);
	}

	public Double getCapacity() {
		return get(CAPACITY);
	}

	public ContainerRecord setCapacity(long capacity) {
		set(CAPACITY, capacity);
		return this;
	}

	public ContainerRecord setCapacity(Double capacity) {
		set(CAPACITY, capacity);
		return this;
	}

	public Double getFillRatioEntered() {
		return get(FILL_RATIO_ENTRED);
	}

	public ContainerRecord setFillRatioEntered(Double fillRatioEntered) {
		set(FILL_RATIO_ENTRED, fillRatioEntered);
		return this;
	}

	public String getTemporaryIdentifier() {
		return get(TEMPORARY_IDENTIFIER);
	}

	public ContainerRecord setTemporaryIdentifier(String temporaryIdentifier) {
		set(TEMPORARY_IDENTIFIER, temporaryIdentifier);
		return this;
	}

	public String getIdentifier() {
		return get(IDENTIFIER);
	}

	public ContainerRecord setIdentifier(String identifier) {
		set(IDENTIFIER, identifier);
		return this;
	}

	public String getType() {
		return get(TYPE);
	}

	public ContainerRecord setType(Record type) {
		set(TYPE, type);
		return this;
	}

	public ContainerRecord setType(String type) {
		set(TYPE, type);
		return this;
	}

	public ContainerRecord setType(ContainerRecordType type) {
		set(TYPE, type);
		return this;
	}

	public String getStorageSpace() {
		return get(STORAGE_SPACE);
	}

	public ContainerRecord setStorageSpace(Record storageSpace) {
		set(STORAGE_SPACE, storageSpace);
		return this;
	}

	public ContainerRecord setStorageSpace(String storageSpace) {
		set(STORAGE_SPACE, storageSpace);
		return this;
	}

	public ContainerRecord setStorageSpace(StorageSpace storageSpace) {
		set(STORAGE_SPACE, storageSpace);
		return this;
	}

	public String getBorrower() {
		return get(BORROWER);
	}

	public ContainerRecord setBorrower(Record borrower) {
		set(BORROWER, borrower);
		return this;
	}

	public ContainerRecord setBorrower(String borrower) {
		set(BORROWER, borrower);
		return this;
	}

	public ContainerRecord setBorrower(User borrower) {
		set(BORROWER, borrower);
		return this;
	}

	public DecommissioningType getDecommissioningType() {
		return get(DECOMMISSIONING_TYPE);
	}

	public ContainerRecord setDecommissioningType(DecommissioningType type) {
		set(DECOMMISSIONING_TYPE, type);
		return this;
	}

	public Boolean isFull() {
		return get(FULL);

	}

	public ContainerRecord setFull(Boolean full) {
		set(FULL, full);
		return this;
	}

	public String getAdministrativeUnit() {
		return get(ADMINISTRATIVE_UNIT);

	}

	public ContainerRecord setAdministrativeUnit(Record administrativeUnit) {
		set(ADMINISTRATIVE_UNIT, administrativeUnit);
		return this;
	}

	public ContainerRecord setAdministrativeUnit(String administrativeUnit) {
		set(ADMINISTRATIVE_UNIT, administrativeUnit);
		return this;
	}

	public ContainerRecord setAdministrativeUnit(AdministrativeUnit administrativeUnit) {
		set(ADMINISTRATIVE_UNIT, administrativeUnit);
		return this;
	}

	public List<String> getAdministrativeUnits() {
		return get(ADMINISTRATIVE_UNITS);

	}

	public ContainerRecord setAdministrativeUnits(List<?> administrativeUnits) {
		set(ADMINISTRATIVE_UNITS, administrativeUnits);
		return this;
	}

	public String getFilingSpace() {
		return get(FILING_SPACE);

	}

	public ContainerRecord setFilingSpace(Record filingSpace) {
		set(FILING_SPACE, filingSpace);
		return this;
	}

	public ContainerRecord setFilingSpace(String filingSpace) {
		set(FILING_SPACE, filingSpace);
		return this;
	}

	public ContainerRecord setFilingSpace(FilingSpace filingSpace) {
		set(FILING_SPACE, filingSpace);
		return this;
	}

	public LocalDate getCompletionDate() {
		return get(COMPLETION_DATE);
	}

	public ContainerRecord setCompletionDate(LocalDate completionDate) {
		set(COMPLETION_DATE, completionDate);
		return this;
	}

	public LocalDate getRealDepositDate() {
		return get(REAL_DEPOSIT_DATE);
	}

	public ContainerRecord setRealDepositDate(LocalDate realDepositDate) {
		set(REAL_DEPOSIT_DATE, realDepositDate);
		return this;
	}

	public LocalDate getRealTransferDate() {
		return get(REAL_TRANSFER_DATE);
	}

	public ContainerRecord setRealTransferDate(LocalDate realTransferDate) {
		set(REAL_TRANSFER_DATE, realTransferDate);
		return this;
	}

	public LocalDate getBorrowDate() {
		return get(BORROW_DATE);
	}

	public ContainerRecord setBorrowDate(LocalDate borrowDate) {
		set(BORROW_DATE, borrowDate);
		return this;
	}

	public LocalDate getRealReturnDate() {
		return get(REAL_RETURN_DATE);
	}

	public ContainerRecord setRealReturnDate(LocalDate realReturnDate) {
		set(REAL_RETURN_DATE, realReturnDate);
		return this;
	}

	public LocalDate getPlanifiedReturnDate() {
		return get(PLANIFIED_RETURN_DATE);
	}

	public ContainerRecord setPlanifiedReturnDate(LocalDate planifiedReturnDate) {
		set(PLANIFIED_RETURN_DATE, planifiedReturnDate);
		return this;
	}

	public String getPosition() {
		return get(POSITION);
	}

	public ContainerRecord setPosition(String position) {
		set(POSITION, position);
		return this;
	}

	public List<Comment> getComments() {
		return getList(COMMENTS);
	}

	public ContainerRecord setComments(List<Comment> comments) {
		set(COMMENTS, comments);
		return this;
	}

	public Boolean getBorrowed() {
		return get(BORROWED);
	}

	public ContainerRecord setBorrowed(Boolean borrowed) {
		set(BORROWED, borrowed);
		return this;
	}

	public Double getLinearSizeEntered() {
		return get(LINEAR_SIZE_ENTERED);
	}

	public ContainerRecord setLinearSizeEntered(double linearSizeEntered) {
		set(LINEAR_SIZE_ENTERED, linearSizeEntered);
		return this;
	}

	public Double getLinearSizeSum() {
		return get(LINEAR_SIZE_SUM);
	}

	public Double getLinearSize() {
		return get(LINEAR_SIZE);
	}

	public Double getAvailableSize() {
		return get(AVAILABLE_SIZE);
	}

	public static ContainerRecord wrap(Record record, MetadataSchemaTypes types) {
		return record == null ? null : new ContainerRecord(record, types);
	}
}
