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
package com.constellio.app.modules.rm.ui.entities;

import static com.constellio.app.modules.rm.wrappers.RetentionRule.ADMINISTRATIVE_UNITS;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.APPROVAL_DATE;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.APPROVED;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.CODE;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.COPY_RETENTION_RULES;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.COPY_RULES_COMMENT;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.CORPUS;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.DESCRIPTION;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.DOCUMENT_TYPES;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.DOCUMENT_TYPES_DETAILS;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.GENERAL_COMMENT;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.JURIDIC_REFERENCE;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.KEYWORDS;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.RESPONSIBLE_ADMINISTRATIVE_UNITS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.data.utils.ImpossibleRuntimeException;

public class RetentionRuleVO extends RecordVO {

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

	public String getCopyRulesComment() {
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
		for (CopyRetentionRule copyRetentionRule : getCopyRetentionRules()) {
			if (DisposalType.SORT == copyRetentionRule.getInactiveDisposalType()) {
				return true;
			}
		}
		return false;
	}

	public List<String> getCategories() {
		return getList("categories");
	}

	public List<String> getUniformSubdivisions() {
		return getList("subdivisions");
	}

	public static class RetentionRuleRuntimeException extends RuntimeException {
		public RetentionRuleRuntimeException(String message) {
			super(message);
		}

	}
}
