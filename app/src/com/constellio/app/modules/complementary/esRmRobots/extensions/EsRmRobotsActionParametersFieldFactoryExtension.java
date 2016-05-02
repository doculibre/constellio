package com.constellio.app.modules.complementary.esRmRobots.extensions;

import com.constellio.app.api.extensions.RecordFieldFactoryExtension;
import com.constellio.app.api.extensions.params.RecordFieldFactoryExtensionParams;
import com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.EsRmRobotActionParametersFieldFactory;
import com.constellio.app.modules.robots.ui.components.actionParameters.DynamicParametersField;
import com.constellio.app.ui.framework.components.RecordFieldFactory;

public class EsRmRobotsActionParametersFieldFactoryExtension extends RecordFieldFactoryExtension {
	@Override
	public RecordFieldFactory newRecordFieldFactory(RecordFieldFactoryExtensionParams params) {
		RecordFieldFactory recordFieldFactory;
		String key = params.getKey();
		if (DynamicParametersField.RECORD_FIELD_FACTORY_KEY.equals(key)) {
			recordFieldFactory = new EsRmRobotActionParametersFieldFactory();
		} else {
			recordFieldFactory = super.newRecordFieldFactory(params); 
		}
		return recordFieldFactory;
	}

	

}
