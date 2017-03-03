package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

import static com.constellio.app.ui.i18n.i18n.$;

public enum DecommissioningType implements EnumWithSmallCode {

	TRANSFERT_TO_SEMI_ACTIVE("T"), DEPOSIT("C"), DESTRUCTION("D");

	private String code;

	DecommissioningType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return $("DecommissioningType." + this.getCode());
	}

}
