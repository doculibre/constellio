package com.constellio.app.modules.rm.ui.entities;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.data.utils.ImpossibleRuntimeException;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.modules.rm.wrappers.RetentionRule.ADMINISTRATIVE_UNITS;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.APPROVAL_DATE;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.APPROVED;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.CODE;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.COPY_RETENTION_RULES;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.COPY_RULES_COMMENT;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.CORPUS;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.DESCRIPTION;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.DOCUMENT_COPY_RETENTION_RULES;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.DOCUMENT_TYPES;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.DOCUMENT_TYPES_DETAILS;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.GENERAL_COMMENT;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.JURIDIC_REFERENCE;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.KEYWORDS;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.RESPONSIBLE_ADMINISTRATIVE_UNITS;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.SCOPE;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE;

public class RetentionRuleVO extends RecordVO {
	public static final String CATEGORIES = "categories";
	public static final String UNIFORM_SUBDIVISIONS = "subdivisions";
	public static final String RETENTION_RULE_DOCUMENT_TYPE = "retentionRuleDocumentType";

	String foldersNumber;

	public RetentionRuleVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
		super(id, metadataValues, viewMode);
	}

	public String getCode() {
		return get(CODE);
	}

	public void setCode(String code) {
		set(CODE, code);
	}

	public boolean isResponsibleAdministrativeUnits() {
		return getBooleanWithDefaultValue(RESPONSIBLE_ADMINISTRATIVE_UNITS, false);
	}

	public void setResponsibleAdministrativeUnits(boolean responsibleAdministrativeUnits) {
		set(RESPONSIBLE_ADMINISTRATIVE_UNITS, responsibleAdministrativeUnits);
	}

	public boolean isApproved() {
		return getBooleanWithDefaultValue(APPROVED, false);
	}

	public void setApproved(boolean approved) {
		set(APPROVED, approved);
	}

	public LocalDate getApprovalDate() {
		return get(APPROVAL_DATE);
	}

	public void setApprovalDate(LocalDate approvalDate) {
		set(APPROVAL_DATE, approvalDate);
	}

	public List<String> getDocumentTypes() {
		return getList(DOCUMENT_TYPES);
	}

	public List<RetentionRuleDocumentType> getDocumentTypesDetails() {
		return getList(DOCUMENT_TYPES_DETAILS);
	}

	public void setDocumentTypesDetails(List<RetentionRuleDocumentType> documentTypesDetails) {
		set(DOCUMENT_TYPES_DETAILS, documentTypesDetails);
	}

	public List<String> getKeywords() {
		return getList(KEYWORDS);
	}

	public void setKeywords(List<String> keywords) {
		set(KEYWORDS, keywords);
	}

	public String getCorpus() {
		return get(CORPUS);
	}

	public void setCorpus(String corpus) {
		set(CORPUS, corpus);
	}

	public List<String> getCopyRulesComment() {
		return get(COPY_RULES_COMMENT);
	}

	public void setCopyRulesComment(String copyRulesComment) {
		set(COPY_RULES_COMMENT, copyRulesComment);
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public void setDescription(String description) {
		set(DESCRIPTION, description);
	}

	public String getJuridicReference() {
		return get(JURIDIC_REFERENCE);
	}

	public void setJuridicReference(String juridicReference) {
		set(JURIDIC_REFERENCE, juridicReference);
	}

	public String getGeneralComment() {
		return get(GENERAL_COMMENT);
	}

	public void setGeneralComment(String generalComment) {
		set(GENERAL_COMMENT, generalComment);
	}

	public List<String> getAdministrativeUnits() {
		return getList(ADMINISTRATIVE_UNITS);
	}

	public void setAdministrativeUnits(List<?> administrativeUnits) {
		set(ADMINISTRATIVE_UNITS, administrativeUnits);
	}

	public List<CopyRetentionRule> getCopyRetentionRules() {
		return getList(COPY_RETENTION_RULES);
	}

	public String getFoldersNumber() {
		return foldersNumber;
	}

	public void setFoldersNumber(String foldersNumber) {
		this.foldersNumber = foldersNumber;
	}

	public void setCopyRetentionRules(CopyRetentionRule... copyRetentionRules) {
		setCopyRetentionRules(Arrays.asList(copyRetentionRules));
	}

	public void setCopyRetentionRules(List<CopyRetentionRule> copyRetentionRules) {
		set(COPY_RETENTION_RULES, copyRetentionRules);
	}

	public CopyRetentionRule getSecondaryCopy() {
		for (CopyRetentionRule copyRetentionRule : getCopyRetentionRules()) {
			if (copyRetentionRule.getCopyType() == CopyType.SECONDARY) {
				return copyRetentionRule;
			}
		}
		return null;
	}

	public List<CopyRetentionRule> getPrincipalCopies() {
		List<CopyRetentionRule> principalCopies = new ArrayList<>();
		for (CopyRetentionRule copyRetentionRule : getCopyRetentionRules()) {
			if (copyRetentionRule.getCopyType() == CopyType.PRINCIPAL) {
				principalCopies.add(copyRetentionRule);
			}
		}
		return principalCopies;
	}

	public CopyRetentionRule getSecondaryCopyRetentionRule() {
		for (CopyRetentionRule copyRetentionRule : getCopyRetentionRules()) {
			if (copyRetentionRule.getCopyType() == CopyType.SECONDARY) {
				return copyRetentionRule;
			}
		}
		throw new ImpossibleRuntimeException("Retention rule has no secondary copy retention rule");
	}

	public final boolean hasCopyRetentionRuleWithSortDispositionType() {
		if(this.getMetadataOrNull(COPY_RETENTION_RULES) == null) {
			return false;
		}

		for (CopyRetentionRule copyRetentionRule : getCopyRetentionRules()) {
			if (DisposalType.SORT == copyRetentionRule.getInactiveDisposalType()) {
				return true;
			}
		}
		return false;
	}

	public List<String> getCategories() {
		return getList(CATEGORIES);
	}

	public List<String> getRetentionRuleDocumentTypes() {
		return getList(RETENTION_RULE_DOCUMENT_TYPE);
	}

	public List<String> getUniformSubdivisions() {
		return getList(UNIFORM_SUBDIVISIONS);
	}

	public static class RetentionRuleRuntimeException extends RuntimeException {
		public RetentionRuleRuntimeException(String message) {
			super(message);
		}
	}

	public List<CopyRetentionRule> getDocumentCopyRetentionRules() {
		return getList(DOCUMENT_COPY_RETENTION_RULES);
	}

	public void setDocumentCopyRetentionRules(CopyRetentionRule... documentCopyRetentionRules) {
		setDocumentCopyRetentionRules(Arrays.asList(documentCopyRetentionRules));
	}

	public void setDocumentCopyRetentionRules(List<CopyRetentionRule> copyRetentionRules) {
		set(DOCUMENT_COPY_RETENTION_RULES, copyRetentionRules);
	}

	public CopyRetentionRule getPrincipalDefaultDocumentCopyRetentionRule() {
		return get(PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE);
	}

	public void setPrincipalDefaultDocumentCopyRetentionRule(
			CopyRetentionRule principalDefaultDocumentCopyRetentionRule) {
		set(PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE, principalDefaultDocumentCopyRetentionRule);
	}

	public CopyRetentionRule getSecondaryDefaultDocumentCopyRetentionRule() {
		return get(SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE);
	}

	public void setSecondaryDefaultDocumentCopyRetentionRule(
			CopyRetentionRule secondaryDefaultDocumentCopyRetentionRule) {
		set(SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE, secondaryDefaultDocumentCopyRetentionRule);
	}

	public RetentionRuleScope getScope() {
		return get(SCOPE);
	}

	public void setScope(RetentionRuleScope scope) {
		set(SCOPE, scope);
	}

	public void setDecommissioningType(RetentionRuleScope retentionRuleScope) {
		set(SCOPE, retentionRuleScope);
	}
}
