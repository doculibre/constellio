package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.model.entities.schemas.ModifiableStructure;

import java.util.ArrayList;
import java.util.List;

public class CopyRetentionRule implements ModifiableStructure {
	String code;
	String title;
	String description;
	CopyType copyType;
	List<String> mediumTypeIds = new ArrayList<>();
	String contentTypesComment;
	RetentionPeriod activeRetentionPeriod = RetentionPeriod.ZERO;
	String activeRetentionComment;
	RetentionPeriod semiActiveRetentionPeriod = RetentionPeriod.ZERO;
	String semiActiveRetentionComment;
	DisposalType inactiveDisposalType;
	String inactiveDisposalComment;
	String typeId;
	String semiActiveDateMetadata;
	String activeDateMetadata;
	Integer openActiveRetentionPeriod;
	boolean essential;
	boolean ignoreActivePeriod;
	boolean dirty;
	String semiActiveYearTypeId;
	String inactiveYearTypeId;
	private String id;

	public String getId() {
		return id;
	}

	public CopyRetentionRule() {
	}

	public CopyRetentionRule setId(String id) {
		if (this.id != null) {
			throw new CopyRetentionRuleFactoryRuntimeException.CopyRetentionRuleFactoryRuntimeException_CannotModifyId(this.id);
		}
		markAsDirty();
		this.id = id;
		return this;
	}

	public String getCode() {
		return code;
	}

	public CopyRetentionRule setCode(String code) {
		markAsDirty();
		this.code = code;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public CopyRetentionRule setTitle(String title) {
		markAsDirty();
		this.title = title;
		return this;
	}

	public String getSemiActiveYearTypeId() {
		return semiActiveYearTypeId;
	}

	public CopyRetentionRule setSemiActiveYearTypeId(String dateTypeId) {
		markAsDirty();
		this.semiActiveYearTypeId = dateTypeId;
		return this;
	}

	public String getInactiveYearTypeId() {
		return inactiveYearTypeId;
	}

	public CopyRetentionRule setInactiveYearTypeId(String dateTypeId) {
		markAsDirty();
		this.inactiveYearTypeId = dateTypeId;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public CopyRetentionRule setDescription(String description) {
		markAsDirty();
		this.description = description;
		return this;
	}

	public Integer getOpenActiveRetentionPeriod() {
		return openActiveRetentionPeriod;
	}

	public CopyRetentionRule setOpenActiveRetentionPeriod(Integer openActiveRetentionPeriod) {
		markAsDirty();
		this.openActiveRetentionPeriod = openActiveRetentionPeriod;
		return this;
	}

	public CopyType getCopyType() {
		return copyType;
	}

	public CopyRetentionRule setCopyType(CopyType copyType) {
		markAsDirty();
		this.copyType = copyType;
		return this;
	}

	public List<String> getMediumTypeIds() {
		return mediumTypeIds;
	}

	public CopyRetentionRule setMediumTypeIds(List<String> mediumTypeIds) {
		markAsDirty();
		this.mediumTypeIds = mediumTypeIds;
		return this;
	}

	public String getContentTypesComment() {
		return contentTypesComment;
	}

	public CopyRetentionRule setContentTypesComment(String contentTypesComment) {
		markAsDirty();
		this.contentTypesComment = contentTypesComment;
		return this;
	}

	public RetentionPeriod getActiveRetentionPeriod() {
		return activeRetentionPeriod;
	}

	public CopyRetentionRule setActiveRetentionPeriod(RetentionPeriod activeRetentionPeriod) {
		markAsDirty();
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
		markAsDirty();
		this.activeRetentionComment = activeRetentionComment;
		return this;
	}

	public RetentionPeriod getSemiActiveRetentionPeriod() {
		return semiActiveRetentionPeriod;
	}

	public CopyRetentionRule setSemiActiveRetentionPeriod(RetentionPeriod semiActiveRetentionPeriod) {
		markAsDirty();
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
		markAsDirty();
		this.semiActiveRetentionComment = semiActiveRetentionComment;
		return this;
	}

	public DisposalType getInactiveDisposalType() {
		return inactiveDisposalType;
	}

	public CopyRetentionRule setInactiveDisposalType(DisposalType inactiveDisposalType) {
		markAsDirty();
		this.inactiveDisposalType = inactiveDisposalType;
		return this;
	}

	public String getInactiveDisposalComment() {
		return inactiveDisposalComment;
	}

	public CopyRetentionRule setInactiveDisposalComment(String inactiveDisposalComment) {
		markAsDirty();
		this.inactiveDisposalComment = inactiveDisposalComment;
		return this;
	}

	public String getTypeId() {
		return typeId;
	}

	public CopyRetentionRule setTypeId(String typeId) {
		markAsDirty();
		this.typeId = typeId;
		return this;
	}

	public CopyRetentionRule setTypeId(FolderType type) {
		return setTypeId(type == null ? null : type.getId());
	}

	public CopyRetentionRule setTypeId(DocumentType type) {
		return setTypeId(type == null ? null : type.getId());
	}

	public boolean isEssential() {
		return essential;
	}

	public CopyRetentionRule setEssential(boolean essential) {
		markAsDirty();
		this.essential = essential;
		return this;
	}

	public boolean isIgnoreActivePeriod() {
		return ignoreActivePeriod;
	}

	public CopyRetentionRule setIgnoreActivePeriod(boolean ignoreActivePeriod) {
		markAsDirty();
		this.ignoreActivePeriod = ignoreActivePeriod;
		return this;
	}

	public String getSemiActiveDateMetadata() {
		return semiActiveDateMetadata;
	}

	public CopyRetentionRule setSemiActiveDateMetadata(String semiActiveDateMetadata) {
		this.markAsDirty();
		this.semiActiveDateMetadata = semiActiveDateMetadata;
		return this;
	}

	public String getActiveDateMetadata() {
		return activeDateMetadata;
	}

	public CopyRetentionRule setActiveDateMetadata(String activeDateMetadata) {
		this.markAsDirty();
		this.activeDateMetadata = activeDateMetadata;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (code != null) {
			sb.append(code);
			sb.append("  ");
		}

		sb.append(activeRetentionPeriod == null ? "?" : activeRetentionPeriod.getValue());
		sb.append("-");
		sb.append(semiActiveRetentionPeriod == null ? "?" : semiActiveRetentionPeriod.getValue());
		sb.append("-");
		sb.append(inactiveDisposalType == null ? "?" : inactiveDisposalType.getCode());

		return sb.toString();
	}

	public String toPeriodsAndDisposalTypeString() {
		StringBuilder sb = new StringBuilder();

		sb.append(activeRetentionPeriod == null ? "?" : activeRetentionPeriod.getValue());
		sb.append("-");
		sb.append(semiActiveRetentionPeriod == null ? "?" : semiActiveRetentionPeriod.getValue());
		sb.append("-");
		sb.append(inactiveDisposalType == null ? "?" : inactiveDisposalType.getCode());

		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CopyRetentionRule)) {
			return false;
		}

		CopyRetentionRule that = (CopyRetentionRule) o;

		if (essential != that.essential) {
			return false;
		}
		if (ignoreActivePeriod != that.ignoreActivePeriod) {
			return false;
		}
		if (code != null ? !code.equals(that.code) : that.code != null) {
			return false;
		}
		if (title != null ? !title.equals(that.title) : that.title != null) {
			return false;
		}
		if (description != null ? !description.equals(that.description) : that.description != null) {
			return false;
		}
		if (copyType != that.copyType) {
			return false;
		}
		if (mediumTypeIds != null ? !mediumTypeIds.equals(that.mediumTypeIds) : that.mediumTypeIds != null) {
			return false;
		}
		if (contentTypesComment != null ?
			!contentTypesComment.equals(that.contentTypesComment) :
			that.contentTypesComment != null) {
			return false;
		}
		if (activeRetentionPeriod != null ?
			!activeRetentionPeriod.equals(that.activeRetentionPeriod) :
			that.activeRetentionPeriod != null) {
			return false;
		}
		if (activeRetentionComment != null ?
			!activeRetentionComment.equals(that.activeRetentionComment) :
			that.activeRetentionComment != null) {
			return false;
		}
		if (semiActiveRetentionPeriod != null ?
			!semiActiveRetentionPeriod.equals(that.semiActiveRetentionPeriod) :
			that.semiActiveRetentionPeriod != null) {
			return false;
		}
		if (semiActiveRetentionComment != null ?
			!semiActiveRetentionComment.equals(that.semiActiveRetentionComment) :
			that.semiActiveRetentionComment != null) {
			return false;
		}
		if (inactiveDisposalType != that.inactiveDisposalType) {
			return false;
		}
		if (inactiveDisposalComment != null ?
			!inactiveDisposalComment.equals(that.inactiveDisposalComment) :
			that.inactiveDisposalComment != null) {
			return false;
		}
		if (typeId != null ? !typeId.equals(that.typeId) : that.typeId != null) {
			return false;
		}
		if (semiActiveDateMetadata != null ?
			!semiActiveDateMetadata.equals(that.semiActiveDateMetadata) :
			that.semiActiveDateMetadata != null) {
			return false;
		}
		if (activeDateMetadata != null ? !activeDateMetadata.equals(that.activeDateMetadata) : that.activeDateMetadata != null) {
			return false;
		}
		if (openActiveRetentionPeriod != null ?
			!openActiveRetentionPeriod.equals(that.openActiveRetentionPeriod) :
			that.openActiveRetentionPeriod != null) {
			return false;
		}
		if (semiActiveYearTypeId != null ?
			!semiActiveYearTypeId.equals(that.semiActiveYearTypeId) :
			that.semiActiveYearTypeId != null) {
			return false;
		}
		if (inactiveYearTypeId != null ? !inactiveYearTypeId.equals(that.inactiveYearTypeId) : that.inactiveYearTypeId != null) {
			return false;
		}
		return id != null ? id.equals(that.id) : that.id == null;
	}

	@Override
	public int hashCode() {
		int result = code != null ? code.hashCode() : 0;
		result = 31 * result + (title != null ? title.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (copyType != null ? copyType.hashCode() : 0);
		result = 31 * result + (mediumTypeIds != null ? mediumTypeIds.hashCode() : 0);
		result = 31 * result + (contentTypesComment != null ? contentTypesComment.hashCode() : 0);
		result = 31 * result + (activeRetentionPeriod != null ? activeRetentionPeriod.hashCode() : 0);
		result = 31 * result + (activeRetentionComment != null ? activeRetentionComment.hashCode() : 0);
		result = 31 * result + (semiActiveRetentionPeriod != null ? semiActiveRetentionPeriod.hashCode() : 0);
		result = 31 * result + (semiActiveRetentionComment != null ? semiActiveRetentionComment.hashCode() : 0);
		result = 31 * result + (inactiveDisposalType != null ? inactiveDisposalType.hashCode() : 0);
		result = 31 * result + (inactiveDisposalComment != null ? inactiveDisposalComment.hashCode() : 0);
		result = 31 * result + (typeId != null ? typeId.hashCode() : 0);
		result = 31 * result + (semiActiveDateMetadata != null ? semiActiveDateMetadata.hashCode() : 0);
		result = 31 * result + (activeDateMetadata != null ? activeDateMetadata.hashCode() : 0);
		result = 31 * result + (openActiveRetentionPeriod != null ? openActiveRetentionPeriod.hashCode() : 0);
		result = 31 * result + (essential ? 1 : 0);
		result = 31 * result + (ignoreActivePeriod ? 1 : 0);
		result = 31 * result + (semiActiveYearTypeId != null ? semiActiveYearTypeId.hashCode() : 0);
		result = 31 * result + (inactiveYearTypeId != null ? inactiveYearTypeId.hashCode() : 0);
		result = 31 * result + (id != null ? id.hashCode() : 0);
		return result;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

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

	private void markAsDirty() {
		dirty = true;
	}

}
