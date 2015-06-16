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
package com.constellio.app.modules.rm.ui.pages.retentionRule;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.ui.builders.RetentionRuleToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class AddEditRetentionRulePresenter extends SingleSchemaBasePresenter<AddEditRetentionRuleView> {
	private boolean addView;
	private RetentionRuleVO retentionRuleVO;

	public AddEditRetentionRulePresenter(AddEditRetentionRuleView view) {
		super(view, RetentionRule.DEFAULT_SCHEMA);
	}

	public void forParams(String id) {
		Record record;
		if (StringUtils.isNotBlank(id)) {
			record = getRecord(id);
			addView = false;
		} else {
			record = newRecord();
			addView = true;
		}

		retentionRuleVO = new RetentionRuleToVOBuilder(appLayerFactory, schema(Category.DEFAULT_SCHEMA),
				schema(UniformSubdivision.DEFAULT_SCHEMA)).build(record, VIEW_MODE.FORM, view.getSessionContext());
		view.setRetentionRule(retentionRuleVO);

		boolean sortDisposal = false;
		if (!addView) {
			List<CopyRetentionRule> copyRetentionRules = retentionRuleVO.getCopyRetentionRules();
			for (CopyRetentionRule copyRetentionRule : copyRetentionRules) {
				if (copyRetentionRule.canSort()) {
					sortDisposal = true;
					break;
				}
			}
		}
		updateDisposalTypeForDocumentTypes(sortDisposal);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_RETENTIONRULE).globally();
	}

	public boolean isAddView() {
		return addView;
	}

	public void cancelButtonClicked() {
		if (addView) {
			view.navigateTo().listRetentionRules();
		} else {
			view.navigateTo().displayRetentionRule(retentionRuleVO.getId());
		}
	}

	public void saveButtonClicked() {
		Record record = toRecord(retentionRuleVO);
		try {
			addOrUpdate(record);
			saveCategories(record.getId(), retentionRuleVO.getCategories());
			saveUniformSubdivisions(record.getId(), retentionRuleVO.getUniformSubdivisions());
			view.navigateTo().listRetentionRules();
		} catch (ValidationRuntimeException e) {
			view.showErrorMessage($(e.getValidationErrors()));
		}
	}

	private void saveCategories(String id, List<String> categories) {
		MetadataSchema schema = schema(Category.DEFAULT_SCHEMA);
		Metadata ruleMetadata = schema.getMetadata(Category.RETENTION_RULES);
		saveInvertedRelation(id, categories, schema, ruleMetadata);
	}

	private void saveUniformSubdivisions(String id, List<String> subdivisions) {
		MetadataSchema schema = schema(UniformSubdivision.DEFAULT_SCHEMA);
		Metadata ruleMetadata = schema.getMetadata(UniformSubdivision.RETENTION_RULE);
		saveInvertedRelation(id, subdivisions, schema, ruleMetadata);
	}

	private void saveInvertedRelation(String id, List<String> records, MetadataSchema schema, Metadata ruleMetadata) {
		Transaction transaction = new Transaction().setUser(getCurrentUser());

		LogicalSearchCondition condition = from(schema).where(ruleMetadata).isEqualTo(id)
				.andWhere(Schemas.IDENTIFIER).isNotIn(records);
		List<Record> removed = searchServices().search(new LogicalSearchQuery(condition));
		for (Record record : removed) {
			List<Object> rules = new ArrayList<>(record.getList(ruleMetadata));
			rules.remove(id);
			record.set(ruleMetadata, rules);
			transaction.add(record);
		}

		condition = from(schema).where(Schemas.IDENTIFIER).isIn(records).andWhere(ruleMetadata).isNotEqual(id);
		List<Record> added = searchServices().search(new LogicalSearchQuery(condition));
		for (Record record : added) {
			List<Object> rules = new ArrayList<>(record.getList(ruleMetadata));
			rules.add(id);
			record.set(ruleMetadata, rules);
			transaction.add(record);
		}

		try {
			recordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	public void disposalTypeChanged(CopyRetentionRule copyRetentionRule) {
		boolean sortPossible;
		if (copyRetentionRule.canSort()) {
			sortPossible = true;
		} else {
			sortPossible = false;
			List<CopyRetentionRule> copyRetentionRules = retentionRuleVO.getCopyRetentionRules();
			for (CopyRetentionRule existingCopyRetentionRule : copyRetentionRules) {
				if (!existingCopyRetentionRule.equals(copyRetentionRule) && existingCopyRetentionRule.canSort()) {
					sortPossible = true;
					break;
				}
			}
		}
		updateDisposalTypeForDocumentTypes(sortPossible);
	}

	private void updateDisposalTypeForDocumentTypes(boolean sortDisposal) {
		view.setDisposalTypeVisibleForDocumentTypes(sortDisposal);
	}

}
