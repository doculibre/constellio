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
