package com.constellio.app.modules.rm.ui.components.retentionRule;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class RetentionRuleReferenceDisplay extends ReferenceDisplay {

	public RetentionRuleReferenceDisplay(RecordVO recordVO) {
		super(recordVO);
	}

	public RetentionRuleReferenceDisplay(String recordId) {
		super(recordId);
	}

	@Override
	protected String getNiceTitle(Record record, MetadataSchemaTypes types) {

		ConstellioUI ui = ConstellioUI.getCurrent();
		String collection = ui.getSessionContext().getCurrentCollection();
		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, ui);

		RetentionRule retentionRule = new RetentionRule(record, types);
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

		StringBuffer sb = new StringBuffer();
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
			appendCopyRetentionRule(principalCopyRetentionRule, sb, rmSchemasRecordsServices);
		}
		for (CopyRetentionRule secondaryCopyRetentionRule : secondaryCopyRetentionRules) {
			sb.append("<br />");
			appendCopyRetentionRule(secondaryCopyRetentionRule, sb, rmSchemasRecordsServices);
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

		return sb.toString();
	}

	private void appendCopyRetentionRule(CopyRetentionRule copyRetentionRule, StringBuffer sb,
			RMSchemasRecordsServices rmSchemasRecordsServices) {
		CopyType copyType = copyRetentionRule.getCopyType();
		List<String> mediumTypeIds = copyRetentionRule.getMediumTypeIds();
		RetentionPeriod activeRetentionPeriod = copyRetentionRule.getActiveRetentionPeriod();
		RetentionPeriod semiActiveRetentionPeriod = copyRetentionRule.getSemiActiveRetentionPeriod();
		DisposalType inactiveDisposalType = copyRetentionRule.getInactiveDisposalType();

		String copyTypeLabel;
		if (CopyType.PRINCIPAL.equals(copyType)) {
			copyTypeLabel = $("RetentionRuleReferenceDisplay.copyType.principal");
		} else {
			copyTypeLabel = $("RetentionRuleReferenceDisplay.copyType.secondary");
		}

		sb.append(copyTypeLabel);
		sb.append(" - ");
		boolean firstMediumType = true;
		for (String mediumTypeId : mediumTypeIds) {
			MediumType mediumType = rmSchemasRecordsServices.getMediumType(mediumTypeId);
			String mediumTypeCode = mediumType.getCode();
			if (!firstMediumType) {
				sb.append(",");
			}
			firstMediumType = false;
			sb.append(mediumTypeCode);
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
		sb.append(copyRetentionRule.toString());
	}

}
