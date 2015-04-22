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

import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;

public class DecomListContainerDetailFactory implements StructureFactory {

	private static final String NULL = "~null~";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, ":");

		DecomListContainerDetail decomListContainerDetail = new DecomListContainerDetail();
		decomListContainerDetail.setContainerRecordId(readString(stringTokenizer));
		decomListContainerDetail.setFull(Boolean.valueOf(readString(stringTokenizer)));
		decomListContainerDetail.dirty = false;
		return decomListContainerDetail;
	}

	@Override
	public String toString(ModifiableStructure structure) {

		DecomListContainerDetail decomListContainerDetail = (DecomListContainerDetail) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeString(stringBuilder, "" + (decomListContainerDetail.getContainerRecordId() == null ?
				NULL :
				decomListContainerDetail.getContainerRecordId()));
		writeString(stringBuilder, "" + decomListContainerDetail.isFull() == null ?
				String.valueOf(false) :
				String.valueOf(decomListContainerDetail.isFull()));
		return stringBuilder.toString();
	}

	private String readString(StringTokenizer stringTokenizer) {
		String value = stringTokenizer.nextToken();
		if (NULL.equals(value)) {
			return null;
		} else {
			return value.replace("~~~", ":");
		}
	}

	private void writeString(StringBuilder stringBuilder, String value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(":");
		}
		if (value == null) {
			stringBuilder.append(NULL);
		} else {
			stringBuilder.append(value.replace(":", "~~~"));
		}
	}
}
