package com.constellio.app.modules.rm.wrappers.structures;

import static com.constellio.model.utils.EnumWithSmallCodeUtils.toEnumWithSmallCode;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;

public class RetentionRuleDocumentTypeFactory implements StructureFactory {
	@Override
	public ModifiableStructure build(String strValue) {
		RetentionRuleDocumentType type = new RetentionRuleDocumentType();
		if (strValue.contains(":")) {
			String[] splitted = strValue.split(":");
			type.documentTypeId = splitted[0];
			type.disposalType = (DisposalType) toEnumWithSmallCode(DisposalType.class, splitted[1]);
		} else {
			type.documentTypeId = strValue;
			type.disposalType = null;
		}
		return type;
	}

	@Override
	public String toString(ModifiableStructure structure) {
		RetentionRuleDocumentType type = (RetentionRuleDocumentType) structure;
		if (type.getDisposalType() == null) {
			return type.getDocumentTypeId();
		} else {
			return type.getDocumentTypeId() + ":" + type.getDisposalType().getCode();
		}
	}
}
