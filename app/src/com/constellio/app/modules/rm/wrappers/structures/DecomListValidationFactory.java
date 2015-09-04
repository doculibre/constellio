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
package com.constellio.app.modules.rm.wrappers.structures;

import java.util.StringTokenizer;

import org.joda.time.LocalDate;

import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;

public class DecomListValidationFactory implements StructureFactory {
	private static final String NULL = "~null~";
	private static final String ELEMENT_SEPARATOR = ":";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, ":");
		DecomListValidation decomListFolderDetail = new DecomListValidation();
		decomListFolderDetail.userId = readString(stringTokenizer);
		decomListFolderDetail.requestDate = readLocalDate(stringTokenizer);
		decomListFolderDetail.validationDate = readLocalDate(stringTokenizer);
		return decomListFolderDetail;
	}

	@Override
	public String toString(ModifiableStructure structure) {
		DecomListValidation validation = (DecomListValidation) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeString(stringBuilder, validation.getUserId());
		writeLocalDate(stringBuilder, validation.getRequestDate());
		writeLocalDate(stringBuilder, validation.getValidationDate());
		return stringBuilder.toString();
	}

	private LocalDate readLocalDate(StringTokenizer stringTokenizer) {
		String localDate = readString(stringTokenizer);
		return localDate == null ? null : LocalDate.parse(localDate);
	}

	private String readString(StringTokenizer stringTokenizer) {
		if (!stringTokenizer.hasMoreElements()) {
			return null;
		}
		String value = stringTokenizer.nextToken();
		if (NULL.equals(value)) {
			return null;
		} else {
			return value.replace("~~~", ELEMENT_SEPARATOR);
		}
	}

	private void writeLocalDate(StringBuilder stringBuilder, LocalDate value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(ELEMENT_SEPARATOR);
		}
		if (value == null) {
			stringBuilder.append(NULL);
		} else {
			stringBuilder.append(value.toString("yyyy-MM-dd"));
		}
	}

	private void writeString(StringBuilder stringBuilder, String value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(ELEMENT_SEPARATOR);
		}
		if (value == null) {
			stringBuilder.append(NULL);
		} else {
			stringBuilder.append(value.replace(ELEMENT_SEPARATOR, "~~~"));
		}
	}
}
