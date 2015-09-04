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
package com.constellio.sdk.load.script.preparators;

import static java.util.Arrays.asList;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
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

		CopyRetentionRule principal5_2_T = CopyRetentionRule.newPrincipal(asList(rm.PA(), rm.DM()), "5-2-T");
		CopyRetentionRule secondary2_0_D = CopyRetentionRule.newSecondary(asList(rm.PA(), rm.DM()), "2-0-D");
		RetentionRule rule = rm.newRetentionRule().setCode("2").setTitle("Rule #2")
				.setResponsibleAdministrativeUnits(true).setApproved(true)
				.setCopyRetentionRules(asList(principal5_2_T, secondary2_0_D));
		transaction.add(rule);
		ruleId = rule.getId();
	}
}
