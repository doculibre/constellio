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
