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
package com.constellio.app.modules.rm.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class RetentionRule extends RecordWrapper {
	public static final String SCHEMA_TYPE = "retentionRule";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String CODE = "code";
	public static final String ADMINISTRATIVE_UNITS = "administrativeUnits";
	public static final String COPY_RETENTION_RULES = "copyRetentionRules";
	public static final String RESPONSIBLE_ADMINISTRATIVE_UNITS = "responsibleAdministrativeUnits";
	public static final String APPROVED = "approved";
	public static final String APPROVAL_DATE = "approvalDate";
	public static final String DOCUMENT_TYPES = "documentTypes";
	public static final String DOCUMENT_TYPES_DETAILS = "documentTypesDetails";
	public static final String KEYWORDS = "keywords";
	public static final String CORPUS = "corpus";
	public static final String CORPUS_RULE_NUMBER = "corpusRuleNumber";
	public static final String COPY_RULES_COMMENT = "copyRulesComment";
	public static final String DESCRIPTION = "description";
	public static final String JURIDIC_REFERENCE = "juridicReference";
	public static final String GENERAL_COMMENT = "generalComment";
	public static final String HISTORY = "history";
	public static final String ESSENTIAL_DOCUMENTS = "essentialDocuments";
	public static final String CONFIDENTIAL_DOCUMENTS = "confidentialDocuments";

	public RetentionRule(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public RetentionRule setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public String getCode() {
		return get(CODE);
	}

	public RetentionRule setCode(String code) {
		set(CODE, code);
		return this;
	}

	public boolean isResponsibleAdministrativeUnits() {
		return getBooleanWithDefaultValue(RESPONSIBLE_ADMINISTRATIVE_UNITS, false);
	}

	public RetentionRule setResponsibleAdministrativeUnits(boolean responsibleAdministrativeUnits) {
		set(RESPONSIBLE_ADMINISTRATIVE_UNITS, responsibleAdministrativeUnits);
		return this;
	}

	public boolean isApproved() {
		return getBooleanWithDefaultValue(APPROVED, false);
	}

	public RetentionRule setApproved(boolean approved) {
		set(APPROVED, approved);
		return this;
	}

	public LocalDate getApprovalDate() {
		return get(APPROVAL_DATE);
	}

	public RetentionRule setApprovalDate(LocalDate approvalDate) {
		set(APPROVAL_DATE, approvalDate);
		return this;
	}

	public List<String> getDocumentTypes() {
		return getList(DOCUMENT_TYPES);
	}

	public List<RetentionRuleDocumentType> getDocumentTypesDetails() {
		return getList(DOCUMENT_TYPES_DETAILS);
	}

	public RetentionRule setDocumentTypesDetails(List<RetentionRuleDocumentType> documentTypesDetails) {
		set(DOCUMENT_TYPES_DETAILS, documentTypesDetails);
		return this;
	}

	public List<String> getKeywords() {
		return getList(KEYWORDS);
	}

	public RetentionRule setKeywords(List<String> keywords) {
		set(KEYWORDS, keywords);
		return this;
	}

	public String getCorpus() {
		return get(CORPUS);
	}

	public RetentionRule setCorpus(String corpus) {
		set(CORPUS, corpus);
		return this;
	}

	public String getCorpusRuleNumber() {
		return get(CORPUS_RULE_NUMBER);
	}

	public RetentionRule setCorpusRuleNumber(String corpusRuleNumber) {
		set(CORPUS_RULE_NUMBER, corpusRuleNumber);
		return this;
	}

	public List<String> getCopyRulesComment() {
		return get(COPY_RULES_COMMENT);
	}

	public RetentionRule setCopyRulesComment(List<String> copyRulesComments) {
		set(COPY_RULES_COMMENT, copyRulesComments);
		return this;
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public RetentionRule setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public String getJuridicReference() {
		return get(JURIDIC_REFERENCE);
	}

	public RetentionRule setJuridicReference(String juridicReference) {
		set(JURIDIC_REFERENCE, juridicReference);
		return this;
	}

	public String getGeneralComment() {
		return get(GENERAL_COMMENT);
	}

	public RetentionRule setGeneralComment(String generalComment) {
		set(GENERAL_COMMENT, generalComment);
		return this;
	}

	public List<String> getAdministrativeUnits() {
		return getList(ADMINISTRATIVE_UNITS);
	}

	public RetentionRule setAdministrativeUnits(List<?> administrativeUnits) {
		set(ADMINISTRATIVE_UNITS, administrativeUnits);
		return this;
	}

	public List<CopyRetentionRule> getCopyRetentionRules() {
		return getList(COPY_RETENTION_RULES);
	}

	public RetentionRule setCopyRetentionRules(CopyRetentionRule... copyRetentionRules) {
		return setCopyRetentionRules(Arrays.asList(copyRetentionRules));
	}

	public RetentionRule setCopyRetentionRules(List<CopyRetentionRule> copyRetentionRules) {
		set(COPY_RETENTION_RULES, copyRetentionRules);
		return this;
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

	public String getHistory() {
		return get(HISTORY);
	}

	public RetentionRule setHistory(String history) {
		set(HISTORY, history);
		return this;
	}

	public boolean isEssentialDocuments() {
		return getBooleanWithDefaultValue(ESSENTIAL_DOCUMENTS, false);
	}

	public RetentionRule setEssentialDocuments(boolean essential) {
		set(ESSENTIAL_DOCUMENTS, essential);
		return this;
	}

	public boolean isConfidentialDocuments() {
		return getBooleanWithDefaultValue(CONFIDENTIAL_DOCUMENTS, false);
	}

	public RetentionRule setConfidentialDocuments(boolean confidential) {
		set(CONFIDENTIAL_DOCUMENTS, confidential);
		return this;
	}

	private CopyRetentionRule getSecondaryCopyRetentionRule() {
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

	public static class RetentionRuleRuntimeException extends RuntimeException {
		public RetentionRuleRuntimeException(String message) {
			super(message);
		}
	}
}
