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
package com.constellio.app.modules.rm.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.services.search.query.logical.criteria.IsContainingTextCriterion;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class CopyRetentionRuleFactory implements StructureFactory {

	private static final String NULL = "~null~";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, ":");
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setCode(readString(stringTokenizer));
		copyRetentionRule.setCopyType((CopyType) EnumWithSmallCodeUtils.toEnum(CopyType.class, readString(stringTokenizer)));
		copyRetentionRule.setContentTypesComment(readString(stringTokenizer));
		copyRetentionRule.setActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setActiveRetentionComment(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionComment(readString(stringTokenizer));
		copyRetentionRule.setInactiveDisposalType(readDisposalType(stringTokenizer));
		copyRetentionRule.setInactiveDisposalComment(readString(stringTokenizer));

		List<String> contentTypesCodes = new ArrayList<>();
		while (stringTokenizer.hasMoreTokens()) {
			contentTypesCodes.add(readString(stringTokenizer));
		}
		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
		copyRetentionRule.dirty = false;
		return copyRetentionRule;
	}

	private DisposalType readDisposalType(StringTokenizer stringTokenizer) {

		String value = readString(stringTokenizer);
		return value == null ? null : (DisposalType) EnumWithSmallCodeUtils.toEnum(DisposalType.class, value);
	}

	@Override
	public String toString(ModifiableStructure structure) {
		CopyRetentionRule rule = (CopyRetentionRule) structure;
		StringBuilder stringBuilder = new StringBuilder();

		writeString(stringBuilder, rule.getCode());
		writeString(stringBuilder, rule.getCopyType() == null ? "" : rule.getCopyType().getCode());
		writeString(stringBuilder, rule.getContentTypesComment());
		writeString(stringBuilder, write(rule.getActiveRetentionPeriod()));
		writeString(stringBuilder, rule.getActiveRetentionComment());
		writeString(stringBuilder, write(rule.getSemiActiveRetentionPeriod()));
		writeString(stringBuilder, rule.getSemiActiveRetentionComment());
		writeString(stringBuilder, rule.getInactiveDisposalType() == null ? NULL : rule.getInactiveDisposalType().getCode());
		writeString(stringBuilder, rule.getInactiveDisposalComment());

		for (String contentTypeCodes : rule.getMediumTypeIds()) {
			writeString(stringBuilder, contentTypeCodes);
		}

		return stringBuilder.toString();
	}

	private String write(RetentionPeriod activeRetentionPeriod) {
		if (activeRetentionPeriod == null) {
			return NULL;
		} else {
			String type = activeRetentionPeriod.isVariablePeriod() ? "V" : "F";
			return type + activeRetentionPeriod.getValue();
		}
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

	private RetentionPeriod readRetentionPeriod(StringTokenizer stringTokenizer) {
		String value = readString(stringTokenizer);
		if (value.startsWith("F")) {
			return RetentionPeriod.fixed(Integer.valueOf(value.substring(1)));

		} else if (value.startsWith("V")) {
			return RetentionPeriod.variable(value.substring(1));

		} else {
			return value == null ? null : new RetentionPeriod(Integer.valueOf(value));
		}
	}

	public static IsContainingTextCriterion variablePeriodCode(String code) {
		return new IsContainingTextCriterion(":V" + code + ":");
	}

}
