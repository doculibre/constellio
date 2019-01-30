package com.constellio.app.modules.complementary.esRmRobots.extensions;

import com.constellio.app.api.extensions.RecordFieldFactoryExtension;
import com.constellio.app.api.extensions.params.RecordFieldFactoryExtensionParams;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorDocumentInFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderDirectlyInThePlanActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInParentFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifySmbFolderInFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.EsRmRobotActionParametersFieldFactory;
import com.constellio.app.modules.robots.ui.components.actionParameters.DynamicParametersField;
import com.constellio.app.ui.framework.components.RecordFieldFactory;

public class EsRmRobotsActionParametersFieldFactoryExtension extends RecordFieldFactoryExtension {

	@Override
	public RecordFieldFactory newRecordFieldFactory(RecordFieldFactoryExtensionParams params) {
		String key = params.getKey();
		String schemaCode = params.getRecordVO().getSchema().getLocalCode();
		if (DynamicParametersField.RECORD_FIELD_FACTORY_KEY.equals(key) &&
			(schemaCode.equals(ClassifyConnectorDocumentInFolderActionParameters.SCHEMA_LOCAL_CODE) ||
			 schemaCode.equals(ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA_LOCAL_CODE) ||
			 schemaCode.equals(ClassifyConnectorFolderInParentFolderActionParameters.SCHEMA_LOCAL_CODE) ||
			 schemaCode.equals(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE) ||
			 schemaCode.equals(ClassifySmbFolderInFolderActionParameters.SCHEMA_LOCAL_CODE))) {
			return new EsRmRobotActionParametersFieldFactory();
		}
		return null;
	}

}
