package com.constellio.sdk.load.script.preparators;

import static java.util.Arrays.asList;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.sdk.load.script.utils.LinkableIdsList;

public class CategoriesTaxonomyPreparator extends BaseTaxonomyPreparator {

	String ruleId;

	LinkableIdsList ids = new LinkableIdsList();

	@Override
	public void init(RMSchemasRecordsServices rm, Transaction transaction) {
		initRules(rm, transaction);
	}

	@Override
	protected RecordWrapper newConceptWithCodeAndParent(RMSchemasRecordsServices rm, String code, RecordWrapper parent) {

		String title = "Category '" + code + "'";
		return ids.attach(rm.newCategory().setCode(code).setTitle(title).setParent((Category) parent))
				.setRetentionRules(asList(ruleId));
	}

	@Override
	public void attach(RMSchemasRecordsServices rm, Record record) {
		if (record.getSchemaCode().startsWith(Folder.SCHEMA_TYPE)) {
			rm.wrapFolder(record).setCategoryEntered(ids.next());
		}
	}

	private void initRules(RMSchemasRecordsServices rm, Transaction transaction) {
		CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();
		CopyRetentionRule principal5_2_T = copyBuilder.newPrincipal(asList(rm.PA(), rm.DM()), "5-2-T");
		CopyRetentionRule secondary2_0_D = copyBuilder.newSecondary(asList(rm.PA(), rm.DM()), "2-0-D");
		RetentionRule rule = rm.newRetentionRule().setCode("2").setTitle("Rule #2")
				.setResponsibleAdministrativeUnits(true).setApproved(true)
				.setCopyRetentionRules(asList(principal5_2_T, secondary2_0_D));
		transaction.add(rule);
		ruleId = rule.getId();
	}
}
