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
package com.constellio.app.modules.rm.extensions;

import static com.constellio.app.modules.rm.model.CopyRetentionRuleFactory.variablePeriodCode;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionValidationEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;

public class RMSchemasLogicalDeleteExtension extends RecordExtension {

	String collection;

	ModelLayerFactory modelLayerFactory;

	RMSchemasRecordsServices rm;

	SearchServices searchServices;

	public RMSchemasLogicalDeleteExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collection = collection;
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.searchServices = modelLayerFactory.newSearchServices();
	}

	@Override
	public ExtensionBooleanResult isLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		if (event.isSchemaType(VariableRetentionPeriod.SCHEMA_TYPE)) {
			return isVariableRetentionPeriodLogicallyDeletable(event);

		} else if (event.isSchemaType(AdministrativeUnit.SCHEMA_TYPE)) {
			return isAdministrativeUnitLogicallyDeletable(event);

		} else if (event.isSchemaType(FilingSpace.SCHEMA_TYPE)) {
			return isFilingSpaceUnitLogicallyDeletable(event);

		} else if (event.isSchemaType(Category.SCHEMA_TYPE)) {
			return isCategoryLogicallyDeletable(event);

		} else if (event.isSchemaType(RetentionRule.SCHEMA_TYPE)) {
			return isRetentionRuleLogicallyDeletable(event);

		} else if (event.isSchemaType(UniformSubdivision.SCHEMA_TYPE)) {
			return isUniformSubdivisionDeletable(event);

		} else {
			return ExtensionBooleanResult.NOT_APPLICABLE;
		}

	}

	private ExtensionBooleanResult isAdministrativeUnitLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		return ExtensionBooleanResult.falseIf(event.isRecordReferenced());
	}

	private ExtensionBooleanResult isFilingSpaceUnitLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		return ExtensionBooleanResult.falseIf(event.isRecordReferenced());
	}

	private ExtensionBooleanResult isCategoryLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		return ExtensionBooleanResult.falseIf(event.isRecordReferenced());
	}

	private ExtensionBooleanResult isRetentionRuleLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		return ExtensionBooleanResult.falseIf(event.isRecordReferenced());
	}

	private ExtensionBooleanResult isUniformSubdivisionDeletable(RecordLogicalDeletionValidationEvent event) {
		return ExtensionBooleanResult.falseIf(event.isRecordReferenced());
	}

	private ExtensionBooleanResult isVariableRetentionPeriodLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		VariableRetentionPeriod variableRetentionPeriod = rm.wrapVariableRetentionPeriod(event.getRecord());
		String code = variableRetentionPeriod.getCode();
		if (code.equals("888") || code.equals("999")) {
			return ExtensionBooleanResult.FALSE;
		} else {
			long count = searchServices.getResultsCount(from(rm.retentionRuleSchemaType())
					.where(rm.retentionRuleCopyRetentionRules()).is(variablePeriodCode(code)));
			return ExtensionBooleanResult.trueIf(count == 0);
		}
	}
}
