package com.constellio.app.modules.rm.services.borrowingServices;

import com.constellio.model.entities.EnumWithSmallCode;

public enum BorrowingType implements EnumWithSmallCode {

	BORROW("B"), CONSULTATION("C");

	private String code;

	BorrowingType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}

