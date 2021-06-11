package com.constellio.app.modules.rm.wrappers.structures;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;

import static com.constellio.model.utils.EnumWithSmallCodeUtils.toEnumWithSmallCode;

public class RetentionRuleDocumentTypeFactory implements CombinedStructureFactory {
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
