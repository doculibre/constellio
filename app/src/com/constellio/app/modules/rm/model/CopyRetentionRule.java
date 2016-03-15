package com.constellio.app.modules.rm.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.schemas.ModifiableStructure;

public class CopyRetentionRule implements ModifiableStructure {
	String code;
	CopyType copyType;
	List<String> mediumTypeIds = new ArrayList<>();
	String contentTypesComment;
	RetentionPeriod activeRetentionPeriod = RetentionPeriod.ZERO;
	String activeRetentionComment;
	RetentionPeriod semiActiveRetentionPeriod = RetentionPeriod.ZERO;
	String semiActiveRetentionComment;
	DisposalType inactiveDisposalType;
	String inactiveDisposalComment;
	String documentTypeId;
	String semiActiveDateMetadata;
	String activeDateMetadata;
	Integer openActiveRetentionPeriod;
	boolean dirty;
	private String id;

	public String getId() {
		return id;
	}

	CopyRetentionRule() {
	}

	public CopyRetentionRule setId(String id) {
		if (this.id != null) {
			throw new CopyRetentionRuleFactoryRuntimeException.CopyRetentionRuleFactoryRuntimeException_CannotModifyId(this.id);
		}
		dirty = true;
		this.id = id;
		return this;
	}

	public String getCode() {
		return code;
	}

	public CopyRetentionRule setCode(String code) {
		dirty = true;
		this.code = code;
		return this;
	}

	public Integer getOpenActiveRetentionPeriod() {
		return openActiveRetentionPeriod;
	}

	public CopyRetentionRule setOpenActiveRetentionPeriod(Integer openActiveRetentionPeriod) {
		dirty = true;
		this.openActiveRetentionPeriod = openActiveRetentionPeriod;
		return this;
	}

	public CopyType getCopyType() {
		return copyType;
	}

	public CopyRetentionRule setCopyType(CopyType copyType) {
		dirty = true;
		this.copyType = copyType;
		return this;
	}

	public List<String> getMediumTypeIds() {
		return mediumTypeIds;
	}

	public CopyRetentionRule setMediumTypeIds(List<String> mediumTypeIds) {
		dirty = true;
		this.mediumTypeIds = mediumTypeIds;
		return this;
	}

	public String getContentTypesComment() {
		return contentTypesComment;
	}

	public CopyRetentionRule setContentTypesComment(String contentTypesComment) {
		dirty = true;
		this.contentTypesComment = contentTypesComment;
		return this;
	}

	public RetentionPeriod getActiveRetentionPeriod() {
		return activeRetentionPeriod;
	}

	public CopyRetentionRule setActiveRetentionPeriod(RetentionPeriod activeRetentionPeriod) {
		dirty = true;
		if (activeRetentionPeriod == null || activeRetentionPeriod.getValue() == 0) {
			this.activeRetentionPeriod = RetentionPeriod.ZERO;
		} else {
			this.activeRetentionPeriod = activeRetentionPeriod;
		}
		return this;
	}

	public String getActiveRetentionComment() {
		return activeRetentionComment;
	}

	public CopyRetentionRule setActiveRetentionComment(String activeRetentionComment) {
		dirty = true;
		this.activeRetentionComment = activeRetentionComment;
		return this;
	}

	public RetentionPeriod getSemiActiveRetentionPeriod() {
		return semiActiveRetentionPeriod;
	}

	public CopyRetentionRule setSemiActiveRetentionPeriod(RetentionPeriod semiActiveRetentionPeriod) {
		dirty = true;
		if (semiActiveRetentionPeriod == null || semiActiveRetentionPeriod.getValue() == 0) {
			this.semiActiveRetentionPeriod = RetentionPeriod.ZERO;
		} else {
			this.semiActiveRetentionPeriod = semiActiveRetentionPeriod;
		}
		return this;
	}

	public String getSemiActiveRetentionComment() {
		return semiActiveRetentionComment;
	}

	public CopyRetentionRule setSemiActiveRetentionComment(String semiActiveRetentionComment) {
		dirty = true;
		this.semiActiveRetentionComment = semiActiveRetentionComment;
		return this;
	}

	public DisposalType getInactiveDisposalType() {
		return inactiveDisposalType;
	}

	public CopyRetentionRule setInactiveDisposalType(DisposalType inactiveDisposalType) {
		dirty = true;
		this.inactiveDisposalType = inactiveDisposalType;
		return this;
	}

	public String getInactiveDisposalComment() {
		return inactiveDisposalComment;
	}

	public CopyRetentionRule setInactiveDisposalComment(String inactiveDisposalComment) {
		dirty = true;
		this.inactiveDisposalComment = inactiveDisposalComment;
		return this;
	}

	public String getDocumentTypeId() {
		return documentTypeId;
	}

	public CopyRetentionRule setDocumentTypeId(String documentTypeId) {
		dirty = true;
		this.documentTypeId = documentTypeId;
		return this;
	}

	public String getSemiActiveDateMetadata() {
		return semiActiveDateMetadata;
	}

	public CopyRetentionRule setSemiActiveDateMetadata(String semiActiveDateMetadata) {
		this.dirty = true;
		this.semiActiveDateMetadata = semiActiveDateMetadata;
		return this;
	}

	public String getActiveDateMetadata() {
		return activeDateMetadata;
	}

	public CopyRetentionRule setActiveDateMetadata(String activeDateMetadata) {
		this.dirty = true;
		this.activeDateMetadata = activeDateMetadata;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		//		sb.append(copyType == null ? "?" : copyType.getCode());
		//		sb.append(mediumTypeIds.toString());
		//		sb.append(" ");
		sb.append(activeRetentionPeriod == null ? "?" : activeRetentionPeriod.getValue());
		sb.append("-");
		sb.append(semiActiveRetentionPeriod == null ? "?" : semiActiveRetentionPeriod.getValue());
		sb.append("-");
		sb.append(inactiveDisposalType == null ? "?" : inactiveDisposalType.getCode());

		return sb.toString();
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "dirty");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "dirty");
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	//	public static CopyRetentionRule newPrincipal(List<String> contentTypesCodes, String value) {
	//		return copyBuilder.newRetentionRule(CopyType.PRINCIPAL, contentTypesCodes, value);
	//	}
	//
	//	public static CopyRetentionRule newSecondary(List<String> contentTypesCodes, String value) {
	//		return copyBuilder.newRetentionRule(CopyType.SECONDARY, contentTypesCodes, value);
	//	}
	//
	//	public static CopyRetentionRule newPrincipal(List<String> contentTypesCodes) {
	//		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
	//		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
	//		copyRetentionRule.setCopyType(CopyType.PRINCIPAL);
	//		return copyRetentionRule;
	//	}
	//
	//	public static CopyRetentionRule newSecondary(List<String> contentTypesCodes) {
	//		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
	//		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
	//		copyRetentionRule.setCopyType(CopyType.SECONDARY);
	//		return copyRetentionRule;
	//	}
	//
	//	public static CopyRetentionRule copyBuilder.newRetentionRule(CopyType copyType, List<String> contentTypesCodes, String value) {
	//		String[] parts = (" " + value + " ").split("-");
	//		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
	//		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
	//		copyRetentionRule.setCopyType(copyType);
	//
	//		String part0 = parts[0].trim();
	//		String part1 = parts[1].trim();
	//		String part2 = parts[2].trim();
	//
	//		if (!part0.isEmpty() && !part0.equals("0")) {
	//			copyRetentionRule.setActiveRetentionPeriod(new RetentionPeriod(Integer.valueOf(part0)));
	//		}
	//		if (!part1.isEmpty() && !part1.equals("0")) {
	//			copyRetentionRule.setSemiActiveRetentionPeriod(new RetentionPeriod(Integer.valueOf(part1)));
	//		}
	//		if (!part2.isEmpty()) {
	//			copyRetentionRule.setInactiveDisposalType((DisposalType) EnumWithSmallCodeUtils.toEnum(DisposalType.class, part2));
	//		}
	//		return copyRetentionRule;
	//	}

	public boolean canTransferToSemiActive() {
		return semiActiveRetentionPeriod != RetentionPeriod.ZERO;
	}

	public boolean canDeposit() {
		return inactiveDisposalType != null && inactiveDisposalType.isDepositOrSort();
	}

	public boolean canDestroy() {
		return inactiveDisposalType != null && inactiveDisposalType.isDestructionOrSort();
	}

	public boolean canSort() {
		return inactiveDisposalType != null && inactiveDisposalType == DisposalType.SORT;
	}

	public CopyRetentionRuleInRule in(RetentionRule rule, Category category) {
		return new CopyRetentionRuleInRule(rule.getId(), category.getId(), category.getLevel(), this);
	}

	public CopyRetentionRuleInRule in(String ruleId, String category, int categoryLevel) {
		return new CopyRetentionRuleInRule(ruleId, category, categoryLevel, this);
	}

}
