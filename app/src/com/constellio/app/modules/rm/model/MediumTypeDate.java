package com.constellio.app.modules.rm.model;

import org.joda.time.LocalDate;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.schemas.ModifiableStructure;

public class MediumTypeDate implements ModifiableStructure {

	boolean dirty;

	private String mediumTypeId;

	private LocalDate date;

	public String getMediumTypeId() {
		return mediumTypeId;
	}

	public void setMediumTypeId(String mediumTypeId) {
		this.dirty |= !LangUtils.areNullableEqual(this.mediumTypeId, mediumTypeId);
		this.mediumTypeId = mediumTypeId;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.dirty |= !LangUtils.areNullableEqual(this.date, date);
		this.date = date;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}
}
