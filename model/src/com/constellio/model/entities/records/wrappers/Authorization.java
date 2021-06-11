package com.constellio.model.entities.records.wrappers;

import com.constellio.data.dao.dto.records.RecordId;
import org.joda.time.LocalDate;

import java.util.List;

public interface Authorization {

	String getId();

	String getCollection();

	List<String> getPrincipals();

	List<RecordId> getPrincipalsIds();

	String getSharedBy();

	List<String> getRoles();

	LocalDate getStartDate();

	LocalDate getLastTokenRecalculate();

	LocalDate getEndDate();

	String getTargetSchemaType();

	boolean isOverrideInherited();

	String getTarget();

	int getTargetRecordIntId();

	RecordId getTargetRecordId();

	boolean isSynced();

	boolean isFutureAuthorization();

	boolean isNegative();

	boolean isActiveAuthorization();

	RecordId getSource();

	boolean isCascading();

	default String getAsString() {
		return "Giving " + (isNegative() ? "negative " : "") + getRoles() + " to " + getPrincipals() + " from " + getSharedBy() + " on " + getTarget() + " (" + getTargetSchemaType() + ")";
	}

	default boolean isActiveAuthorizationAtDate(LocalDate date) {
		LocalDate startDate = getStartDate();
		LocalDate endDate = getEndDate();
		if (startDate != null && endDate == null) {
			return !startDate.isAfter(date);
		} else if (startDate == null && endDate != null) {
			return !endDate.isBefore(date);
		} else if (startDate != null && endDate != null) {
			return !startDate.isAfter(date) && !endDate.isBefore(date);
		} else {
			return true;
		}
	}

	default boolean hasModifiedStatusSinceLastTokenRecalculate() {
		if (getStartDate() != null || getEndDate() != null) {

			if (getLastTokenRecalculate() == null) {
				return true;

			} else {

				boolean wasActiveDuringLastRecalculate = isActiveAuthorizationAtDate(getLastTokenRecalculate());
				boolean isCurrentlyActive = isActiveAuthorization();

				return wasActiveDuringLastRecalculate != isCurrentlyActive;

			}

		} else {
			return false;
		}
	}

}
