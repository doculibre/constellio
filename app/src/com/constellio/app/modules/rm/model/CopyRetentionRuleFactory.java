package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LangUtils.StringReplacer;
import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.services.search.query.logical.criteria.IsContainingTextCriterion;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CopyRetentionRuleFactory implements CombinedStructureFactory {

	private static StringReplacer replacer = LangUtils.replacingLiteral("::", ":~null~:");
	private static StringReplacer replacerEncodingColon = LangUtils.replacingLiteral(":", "~~~");
	private static StringReplacer replacerDecodingColon = LangUtils.replacingLiteral("~~~", ":");

	private static final String NULL = "~null~";
	public static final String VERSION_2 = "version2";
	public static final String VERSION_3 = "version3";
	public static final String VERSION_4 = "version4";
	public static final String VERSION_5 = "v5";

	@Override
	public ModifiableStructure build(String string) {
		String stringWithNullReplaced = buildStringWithNullReplaced(string);
		StringTokenizer stringTokenizer = new StringTokenizer(stringWithNullReplaced, ":");
		String versionOrCode = readString(stringTokenizer);
		if (isVersion2(versionOrCode)) {
			return getModifiableStructureV2(stringTokenizer);

		} else if (isVersion3(versionOrCode)) {
			return getModifiableStructureV3(stringTokenizer);

		} else if (isVersion4(versionOrCode)) {
			return getModifiableStructureV4(stringTokenizer);

		} else if (isVersion5(versionOrCode)) {
			return getModifiableStructureV5(stringTokenizer);

		}
		stringTokenizer = new StringTokenizer(stringWithNullReplaced, ":");
		return getModifiableStructureV1(stringTokenizer);
		//		String newString = toString(copyRetentionRuleFactory);
		//return build(newString);

	}

	private String buildStringWithNullReplaced(String string) {
		String stringWithNullReplaced = string;

		while (stringWithNullReplaced.contains("::")) {
			stringWithNullReplaced = replacer.replaceOn(stringWithNullReplaced);
		}

		return stringWithNullReplaced;
	}

	private ModifiableStructure getModifiableStructureV2(StringTokenizer stringTokenizer) {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setCode(readString(stringTokenizer));
		copyRetentionRule.setCopyType((CopyType) EnumWithSmallCodeUtils.toEnum(CopyType.class, readString(stringTokenizer)));
		copyRetentionRule.setContentTypesComment(readString(stringTokenizer));
		copyRetentionRule.setActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setActiveRetentionComment(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionComment(readString(stringTokenizer));

		String disposalType = readString(stringTokenizer);
		if (disposalType != null && DisposalType.isValidCode(disposalType)) {
			copyRetentionRule.setInactiveDisposalType(readDisposalType(disposalType));
			copyRetentionRule.setInactiveDisposalComment(readString(stringTokenizer));
		} else {
			copyRetentionRule.setInactiveDisposalType(DisposalType.DESTRUCTION);
			copyRetentionRule.setInactiveDisposalComment(disposalType);
		}
		copyRetentionRule.setTypeId(readString(stringTokenizer));
		copyRetentionRule.setActiveDateMetadata(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveDateMetadata(readString(stringTokenizer));

		List<String> contentTypesCodes = new ArrayList<>();
		while (stringTokenizer.hasMoreTokens()) {
			contentTypesCodes.add(readString(stringTokenizer));
		}
		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
		copyRetentionRule.dirty = false;

		return copyRetentionRule;
	}

	private ModifiableStructure getModifiableStructureV3(StringTokenizer stringTokenizer) {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setId(readString(stringTokenizer));
		copyRetentionRule.setCode(readString(stringTokenizer));
		copyRetentionRule.setCopyType((CopyType) EnumWithSmallCodeUtils.toEnum(CopyType.class, readString(stringTokenizer)));
		copyRetentionRule.setEssential(readBoolean(stringTokenizer));
		copyRetentionRule.setContentTypesComment(readString(stringTokenizer));
		copyRetentionRule.setOpenActiveRetentionPeriod(readInteger(stringTokenizer));
		copyRetentionRule.setActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setActiveRetentionComment(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionComment(readString(stringTokenizer));

		String disposalType = readString(stringTokenizer);
		if (disposalType != null && DisposalType.isValidCode(disposalType)) {
			copyRetentionRule.setInactiveDisposalType(readDisposalType(disposalType));
			copyRetentionRule.setInactiveDisposalComment(readString(stringTokenizer));
		} else {
			copyRetentionRule.setInactiveDisposalType(DisposalType.DESTRUCTION);
			copyRetentionRule.setInactiveDisposalComment(disposalType);
		}
		copyRetentionRule.setTypeId(readString(stringTokenizer));
		copyRetentionRule.setActiveDateMetadata(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveDateMetadata(readString(stringTokenizer));

		List<String> contentTypesCodes = new ArrayList<>();
		while (stringTokenizer.hasMoreTokens()) {
			contentTypesCodes.add(readString(stringTokenizer));
		}
		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
		copyRetentionRule.dirty = false;

		return copyRetentionRule;
	}

	private ModifiableStructure getModifiableStructureV4(StringTokenizer stringTokenizer) {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setId(readString(stringTokenizer));
		copyRetentionRule.setCode(readString(stringTokenizer));
		copyRetentionRule.setTitle(readString(stringTokenizer));
		copyRetentionRule.setDescription(readString(stringTokenizer));
		copyRetentionRule.setCopyType((CopyType) EnumWithSmallCodeUtils.toEnum(CopyType.class, readString(stringTokenizer)));
		copyRetentionRule.setEssential(readBoolean(stringTokenizer));
		copyRetentionRule.setIgnoreActivePeriod(readBoolean(stringTokenizer));
		copyRetentionRule.setContentTypesComment(readString(stringTokenizer));
		copyRetentionRule.setOpenActiveRetentionPeriod(readInteger(stringTokenizer));
		copyRetentionRule.setActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setActiveRetentionComment(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionComment(readString(stringTokenizer));

		String disposalType = readString(stringTokenizer);
		if (disposalType != null && DisposalType.isValidCode(disposalType)) {
			copyRetentionRule.setInactiveDisposalType(readDisposalType(disposalType));
			copyRetentionRule.setInactiveDisposalComment(readString(stringTokenizer));
		} else {
			copyRetentionRule.setInactiveDisposalType(DisposalType.DESTRUCTION);
			copyRetentionRule.setInactiveDisposalComment(disposalType);
		}
		copyRetentionRule.setTypeId(readString(stringTokenizer));
		copyRetentionRule.setActiveDateMetadata(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveDateMetadata(readString(stringTokenizer));

		List<String> contentTypesCodes = new ArrayList<>();
		while (stringTokenizer.hasMoreTokens()) {
			contentTypesCodes.add(readString(stringTokenizer));
		}
		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
		copyRetentionRule.dirty = false;

		return copyRetentionRule;
	}

	private ModifiableStructure getModifiableStructureV5(StringTokenizer stringTokenizer) {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setId(readString(stringTokenizer));
		copyRetentionRule.setCode(readString(stringTokenizer));
		copyRetentionRule.setTitle(readString(stringTokenizer));
		copyRetentionRule.setDescription(readString(stringTokenizer));
		copyRetentionRule.setCopyType((CopyType) EnumWithSmallCodeUtils.toEnum(CopyType.class, readString(stringTokenizer)));
		copyRetentionRule.setEssential(readBoolean(stringTokenizer));
		copyRetentionRule.setIgnoreActivePeriod(readBoolean(stringTokenizer));
		copyRetentionRule.setContentTypesComment(readString(stringTokenizer));
		copyRetentionRule.setOpenActiveRetentionPeriod(readInteger(stringTokenizer));
		copyRetentionRule.setActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setActiveRetentionComment(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionComment(readString(stringTokenizer));

		String disposalType = readString(stringTokenizer);
		if (disposalType != null && DisposalType.isValidCode(disposalType)) {
			copyRetentionRule.setInactiveDisposalType(readDisposalType(disposalType));
			copyRetentionRule.setInactiveDisposalComment(readString(stringTokenizer));
		} else {
			copyRetentionRule.setInactiveDisposalType(DisposalType.DESTRUCTION);
			copyRetentionRule.setInactiveDisposalComment(disposalType);
		}
		copyRetentionRule.setTypeId(readString(stringTokenizer));
		copyRetentionRule.setActiveDateMetadata(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveDateMetadata(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveYearTypeId(readString(stringTokenizer));
		copyRetentionRule.setInactiveYearTypeId(readString(stringTokenizer));

		List<String> contentTypesCodes = new ArrayList<>();
		while (stringTokenizer.hasMoreTokens()) {
			contentTypesCodes.add(readString(stringTokenizer));
		}
		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
		copyRetentionRule.dirty = false;

		return copyRetentionRule;
	}

	private ModifiableStructure getModifiableStructureV1(StringTokenizer stringTokenizer) {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setCode(readString(stringTokenizer));
		copyRetentionRule.setCopyType((CopyType) EnumWithSmallCodeUtils.toEnum(CopyType.class, readString(stringTokenizer)));
		copyRetentionRule.setContentTypesComment(readString(stringTokenizer));
		copyRetentionRule.setActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setActiveRetentionComment(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionComment(readString(stringTokenizer));

		String disposalType = readString(stringTokenizer);
		if (disposalType != null && DisposalType.isValidCode(disposalType)) {
			copyRetentionRule.setInactiveDisposalType(readDisposalType(disposalType));
			copyRetentionRule.setInactiveDisposalComment(readString(stringTokenizer));
		} else {
			copyRetentionRule.setInactiveDisposalType(DisposalType.DESTRUCTION);
			copyRetentionRule.setInactiveDisposalComment(disposalType);
		}

		List<String> contentTypesCodes = new ArrayList<>();
		while (stringTokenizer.hasMoreTokens()) {
			contentTypesCodes.add(readString(stringTokenizer));
		}
		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
		copyRetentionRule.dirty = false;
		return copyRetentionRule;
	}

	private boolean isVersion2(String versionOrCode) {
		return VERSION_2.equals(versionOrCode);
	}

	private boolean isVersion3(String versionOrCode) {
		return VERSION_3.equals(versionOrCode);
	}

	private boolean isVersion4(String versionOrCode) {
		return VERSION_4.equals(versionOrCode);
	}

	private boolean isVersion5(String versionOrCode) {
		return VERSION_5.equals(versionOrCode);
	}

	private DisposalType readDisposalType(String value) {

		return value == null ? null : (DisposalType) EnumWithSmallCodeUtils.toEnum(DisposalType.class, value);
	}

	@Override
	public String toString(ModifiableStructure structure) {
		CopyRetentionRule rule = (CopyRetentionRule) structure;
		StringBuilder stringBuilder = new StringBuilder();

		writeString(stringBuilder, VERSION_5);
		writeString(stringBuilder, rule.getId());
		writeString(stringBuilder, rule.getCode());
		writeString(stringBuilder, rule.getTitle());
		writeString(stringBuilder, rule.getDescription());
		writeString(stringBuilder, rule.getCopyType() == null ? "" : rule.getCopyType().getCode());
		writeBoolean(stringBuilder, rule.isEssential());
		writeBoolean(stringBuilder, rule.isIgnoreActivePeriod());
		writeString(stringBuilder, rule.getContentTypesComment());
		writeString(stringBuilder, write(rule.getOpenActiveRetentionPeriod()));
		writeString(stringBuilder, write(rule.getActiveRetentionPeriod()));
		writeString(stringBuilder, rule.getActiveRetentionComment());
		writeString(stringBuilder, write(rule.getSemiActiveRetentionPeriod()));
		writeString(stringBuilder, rule.getSemiActiveRetentionComment());
		writeString(stringBuilder, rule.getInactiveDisposalType() == null ? NULL : rule.getInactiveDisposalType().getCode());
		writeString(stringBuilder, rule.getInactiveDisposalComment());

		writeString(stringBuilder, rule.getTypeId());
		writeString(stringBuilder, rule.getActiveDateMetadata());
		writeString(stringBuilder, rule.getSemiActiveDateMetadata());
		writeString(stringBuilder, rule.getSemiActiveYearTypeId());
		writeString(stringBuilder, rule.getInactiveYearTypeId());

		for (String contentTypeCodes : rule.getMediumTypeIds()) {
			writeString(stringBuilder, contentTypeCodes);
		}

		return stringBuilder.toString();
	}

	private boolean readBoolean(StringTokenizer stringTokenizer) {
		return "true".equals(readString(stringTokenizer));
	}

	private void writeBoolean(StringBuilder stringBuilder, boolean value) {
		writeString(stringBuilder, value ? "true" : "false");
	}

	private String write(Integer value) {
		if (value == null) {
			return NULL;
		} else {
			return "" + value;
		}
	}

	private String write(RetentionPeriod activeRetentionPeriod) {
		if (activeRetentionPeriod == null) {
			return NULL;
		} else {
			String type = activeRetentionPeriod.isVariablePeriod() ? "V" : "F";
			return type + activeRetentionPeriod.getValue();
		}
	}

	private Integer readInteger(StringTokenizer stringTokenizer) {
		String value = stringTokenizer.nextToken();

		if (NULL.equals(value)) {
			return null;
		} else {
			return Integer.valueOf(value);
		}
	}

	private String readString(StringTokenizer stringTokenizer) {
		String value = stringTokenizer.nextToken();

		if (NULL.equals(value)) {
			return null;
		} else {
			return replacerDecodingColon.replaceOn(value);
		}
	}

	private void writeString(StringBuilder stringBuilder, String value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(":");
		}
		if (value == null) {
			stringBuilder.append(NULL);
		} else {
			stringBuilder.append(replacerEncodingColon.replaceOn(value));
		}
	}

	private RetentionPeriod readRetentionPeriod(StringTokenizer stringTokenizer) {
		String value = readString(stringTokenizer);
		if (value == null) {
			return RetentionPeriod.ZERO;

		} else if (value.startsWith("F")) {
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

	private LocalDate readLocalDate(StringTokenizer stringTokenizer) {
		String localDate = readString(stringTokenizer);
		return localDate == null ? null : LocalDate.parse(localDate);
	}

}