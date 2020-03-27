package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.model.entities.records.Record;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class RetentionRuleInfoBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(RetentionRuleInfoBuilder.class);

	private String info;

	public RetentionRuleInfoBuilder(Record record) {
		ConstellioUI ui = ConstellioUI.getCurrent();
		String collection = ui.getSessionContext().getCurrentCollection();
		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, ui);

		//Wrap using current locale
		RetentionRule retentionRule = rmSchemasRecordsServices.wrapRetentionRule(record);

		String code = retentionRule.getCode();
		String title = retentionRule.getTitle();
		String description = retentionRule.getDescription();
		List<String> copyRulesComments = retentionRule.getCopyRulesComment();
		List<CopyRetentionRule> copyRetentionRules = retentionRule.getCopyRetentionRules();

		List<CopyRetentionRule> principalCopyRetentionRules = new ArrayList<>();
		List<CopyRetentionRule> secondaryCopyRetentionRules = new ArrayList<>();
		for (CopyRetentionRule copyRetentionRule : copyRetentionRules) {
			CopyType copyType = copyRetentionRule.getCopyType();
			if (CopyType.PRINCIPAL.equals(copyType)) {
				principalCopyRetentionRules.add(copyRetentionRule);
			} else {
				secondaryCopyRetentionRules.add(copyRetentionRule);
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append(code);
		sb.append("-");
		sb.append(title);
		sb.append(" : ");
		if (StringUtils.isNotBlank(description)) {
			//Add br
			String descriptionWithBr = StringUtils.replace(description, System.getProperty("line.separator"), "<br />");
			sb.append(descriptionWithBr);
		}

		for (CopyRetentionRule principalCopyRetentionRule : principalCopyRetentionRules) {
			sb.append("<br />");
			appendCopyRetentionRule(principalCopyRetentionRule, retentionRule, sb, true, rmSchemasRecordsServices);
		}
		for (CopyRetentionRule secondaryCopyRetentionRule : secondaryCopyRetentionRules) {
			sb.append("<br />");
			appendCopyRetentionRule(secondaryCopyRetentionRule, retentionRule, sb, true, rmSchemasRecordsServices);
		}
		if (secondaryCopyRetentionRules.isEmpty()) {
			sb.append($("RetentionRuleReferenceDisplay.copyType.secondary"));
			sb.append(" - ");
			sb.append($("RetentionRuleReferenceDisplay.notApplicable"));
		}
		for (String copyRulesComment : copyRulesComments) {
			sb.append("<br />");
			String copyRulesCommentWithBr = StringUtils.replace(copyRulesComment, System.getProperty("line.separator"), "<br />");
			sb.append(copyRulesCommentWithBr);
		}
		info = sb.toString();
	}

	private void appendCopyRetentionRule(CopyRetentionRule copyRetentionRule, RetentionRule retentionRule,
										 StringBuilder sb, boolean addLabelAndCode,
										 RMSchemasRecordsServices rmSchemasRecordsServices) {
		CopyType copyType = copyRetentionRule.getCopyType();
		List<String> mediumTypeIds = copyRetentionRule.getMediumTypeIds();
		RetentionPeriod activeRetentionPeriod = copyRetentionRule.getActiveRetentionPeriod();
		RetentionPeriod semiActiveRetentionPeriod = copyRetentionRule.getSemiActiveRetentionPeriod();
		DisposalType inactiveDisposalType = copyRetentionRule.getInactiveDisposalType();

		if (addLabelAndCode) {
			String copyTypeLabel;
			if (CopyType.PRINCIPAL.equals(copyType)) {
				copyTypeLabel = $("RetentionRuleReferenceDisplay.copyType.principal");
			} else {
				copyTypeLabel = $("RetentionRuleReferenceDisplay.copyType.secondary");
			}

			sb.append(copyTypeLabel);
			sb.append(" - ");
		}

		boolean firstMediumType = true;
		for (String mediumTypeId : mediumTypeIds) {
			try {
				MediumType mediumType = rmSchemasRecordsServices.getMediumType(mediumTypeId);
				String mediumTypeCode = mediumType.getCode();
				if (!firstMediumType) {
					sb.append(",");
				}
				firstMediumType = false;
				sb.append(mediumTypeCode);
			} catch (Throwable t) {
				LOGGER.warn("Error while getting medium type " + mediumTypeId + " for retention rule " + retentionRule + ", copy retention rule " + copyRetentionRule);
			}
		}
		sb.append(" : ");

		//		sb.append(activeRetentionPeriod.getRetentionType().getCode());
		//		sb.append("-");
		//		if (semiActiveRetentionPeriod != null) {
		//			sb.append(semiActiveRetentionPeriod.getRetentionType().getCode());
		//		} else {
		//			sb.append("0");
		//		}
		//		sb.append("-");
		//		sb.append(inactiveDisposalType.getCode());
		if (addLabelAndCode) {
			sb.append(copyRetentionRule.toString());
		} else {
			sb.append(activeRetentionPeriod == null ? "?" : activeRetentionPeriod.getValue());
			sb.append("-");
			sb.append(semiActiveRetentionPeriod == null ? "?" : semiActiveRetentionPeriod.getValue());
			sb.append("-");
			sb.append(inactiveDisposalType == null ? "?" : inactiveDisposalType.getCode());
		}
	}

	public String getInfo() {
		return info;
	}

}
