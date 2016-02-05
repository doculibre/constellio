package com.constellio.app.modules.rm.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.ModifiableStructure;

public class CopyRetentionRuleInRule implements ModifiableStructure {

	CopyRetentionRule copyRetentionRule;

	String ruleId;

	String categoryId;

	int categoryLevel;

	boolean dirty;

	public CopyRetentionRuleInRule() {
	}

	public CopyRetentionRuleInRule(String ruleId, String categoryId, int categoryLevel, CopyRetentionRule copyRetentionRule) {
		if (ruleId == null) {
			throw new IllegalArgumentException("ruleId");
		}
		if (categoryId == null) {
			throw new IllegalArgumentException("categoryId");
		}
		if (copyRetentionRule == null) {
			throw new IllegalArgumentException("copyRetentionRule");
		}
		this.ruleId = ruleId;
		this.categoryId = categoryId;
		this.categoryLevel = categoryLevel;
		this.copyRetentionRule = copyRetentionRule;
		this.dirty = true;
	}

	public CopyRetentionRule getCopyRetentionRule() {
		return copyRetentionRule;
	}

	public String getRuleId() {
		return ruleId;
	}

	public int getCategoryLevel() {
		return categoryLevel;
	}

	public String getCategoryId() {
		return categoryId;
	}

	@Override
	public String toString() {
		return "Category '" + categoryId + "' on level '" + categoryLevel + "' with rule '" + ruleId + "' >" + copyRetentionRule;
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
		return dirty || (copyRetentionRule != null && copyRetentionRule.isDirty());
	}
}
