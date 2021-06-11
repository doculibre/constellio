package com.constellio.app.modules.rm.wrappers.structures;

import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

//AFTER : Move in core
public class CommentFactory implements CombinedStructureFactory {

	private static final String NULL = "~null~";
	public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, ":");

		Comment comment = new Comment();
		comment.userId = readString(stringTokenizer);
		comment.username = readString(stringTokenizer);
		comment.setCreationDateTime(readLocalDateTime(stringTokenizer));
		if (StringUtils.countMatches(string, ":") == 4) {
			comment.setModificationDateTime(readLocalDateTime(stringTokenizer));
		}
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
		if (comment.getCreationDateTime() != null) {
			writeString(stringBuilder, comment.getCreationDateTime().toString(DATE_PATTERN));
		} else {
			writeString(stringBuilder, NULL);
		}
		if (comment.getModificationDateTime() != null) {
			writeString(stringBuilder, comment.getModificationDateTime().toString(DATE_PATTERN));
		} else {
			writeString(stringBuilder, NULL);
		}
		writeString(stringBuilder, "" + comment.getMessage() == null ?
								   NULL :
								   comment.getMessage());
		return stringBuilder.toString();
	}

	private String readString(StringTokenizer stringTokenizer) {
		try {
			String value = stringTokenizer.nextToken();
			if (NULL.equals(value)) {
				return null;
			} else {
				return value.replace("~~~", ":");
			}
		} catch (NoSuchElementException e) {
			return "";
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
