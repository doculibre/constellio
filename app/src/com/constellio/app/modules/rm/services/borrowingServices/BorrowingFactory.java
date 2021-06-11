package com.constellio.app.modules.rm.services.borrowingServices;

import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;
import org.joda.time.LocalDateTime;

import java.util.StringTokenizer;

public class BorrowingFactory implements CombinedStructureFactory {

	private static final String NULL = "~null~";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, ":");

		Borrowing borrowing = new Borrowing();
		BorrowingType borrowingType = null;
		String borrowTypeString = readString(stringTokenizer);
		if (borrowTypeString != null) {
			borrowingType = BorrowingType.valueOf(borrowTypeString);
		}
		borrowing.borrowingType = borrowingType;
		borrowing.borrowerId = readString(stringTokenizer);
		borrowing.borrowerUsername = readString(stringTokenizer);
		borrowing.setBorrowDateTime(readLocalDateTime(stringTokenizer));
		borrowing.setReturnDateTime(readLocalDateTime(stringTokenizer));
		borrowing.setPreviewReturnDateTime(readLocalDateTime(stringTokenizer));
		borrowing.dirty = false;
		return borrowing;
	}

	@Override
	public String toString(ModifiableStructure structure) {

		Borrowing borrowing = (Borrowing) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeString(stringBuilder, borrowing.getBorrowingType() == null ?
								   NULL :
								   "" + borrowing.getBorrowingType());
		writeString(stringBuilder, "" + borrowing.getBorrowerId() == null ?
								   NULL :
								   borrowing.getBorrowerId());
		writeString(stringBuilder, "" + borrowing.getBorrowerUsername() == null ?
								   "" :
								   borrowing.getBorrowerUsername());
		if (borrowing.getBorrowDateTime() != null) {
			writeString(stringBuilder, borrowing.getBorrowDateTime().toString("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		} else {
			writeString(stringBuilder, NULL);
		}
		if (borrowing.getReturnDateTime() != null) {
			writeString(stringBuilder, borrowing.getReturnDateTime().toString("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		} else {
			writeString(stringBuilder, NULL);
		}
		if (borrowing.getPreviewReturnDateTime() != null) {
			writeString(stringBuilder, borrowing.getPreviewReturnDateTime().toString("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		} else {
			writeString(stringBuilder, NULL);
		}
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
