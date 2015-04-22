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

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;

public class CommentFactory implements StructureFactory {

	private static final String NULL = "~null~";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, ":");

		Comment comment = new Comment();
		comment.userId = readString(stringTokenizer);
		comment.username = readString(stringTokenizer);
		comment.setDateTime(readLocalDateTime(stringTokenizer));
		comment.setMessage(readString(stringTokenizer));
		comment.dirty = false;
		return comment;
	}

	@Override
	public String toString(ModifiableStructure structure) {

		Comment comment = (Comment) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeString(stringBuilder, "" + comment.getUserId() == null ?
				NULL :
				comment.getUserId());
		writeString(stringBuilder, "" + comment.getUsername() == null ?
				"" :
				comment.getUsername());
		if (comment.getDateTime() != null) {
			writeString(stringBuilder, comment.getDateTime().toString("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		} else {
			writeString(stringBuilder, NULL);
		}
		writeString(stringBuilder, "" + comment.getMessage() == null ?
				NULL :
				comment.getMessage());
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

	private LocalDateTime readLocalDateTime(StringTokenizer stringTokenizer) {
		String localDateTime = readString(stringTokenizer);
		return localDateTime == null ? null : LocalDateTime.parse(localDateTime);
	}

}
