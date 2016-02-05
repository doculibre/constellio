package com.constellio.app.modules.rm.wrappers.structures;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.schemas.ModifiableStructure;

public class PendingAlert implements ModifiableStructure {

	Double reminderCount;
	AlertCode alertCode;
	LocalDateTime on;
	boolean dirty;

	public PendingAlert() {
	}

	public PendingAlert(LocalDateTime on, AlertCode alertCode, Double reminderCount, boolean dirty) {
		this.reminderCount = reminderCount;
		this.alertCode = alertCode;
		this.on = on;
		this.dirty = dirty;
	}

	public Double getReminderCount() {
		return reminderCount;
	}

	public void setReminderCount(Double reminderCount) {
		dirty = true;
		this.reminderCount = reminderCount;
	}

	public AlertCode getAlertCode() {
		return alertCode;
	}

	public void setAlertCode(AlertCode alertCode) {
		dirty = true;
		this.alertCode = alertCode;
	}

	public LocalDateTime getOn() {
		return on;
	}

	public void setOn(LocalDateTime on) {
		dirty = true;
		this.on = on;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return "PendingAlertHandlerImpl{" +
				"reminderCount=" + reminderCount +
				", alertCode=" + alertCode +
				", on=" + on +
				", dirty=" + dirty +
				'}';
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "dirty");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "dirty");
	}
}
