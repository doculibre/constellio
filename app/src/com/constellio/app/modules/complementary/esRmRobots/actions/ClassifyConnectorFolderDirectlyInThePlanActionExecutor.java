package com.constellio.app.modules.complementary.esRmRobots.actions;

import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderDirectlyInThePlanActionParameters;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.services.RobotsManager;

public class ClassifyConnectorFolderDirectlyInThePlanActionExecutor extends ClassifyConnectorFolderBaseActionExecutor {

	public final static String ID = ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA_LOCAL_CODE;

	public final static String PARAMETER_SCHEMA = ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA_LOCAL_CODE;

	public static void registerIn(RobotsManager robotsManager) {
		robotsManager.registerAction(
				ID, PARAMETER_SCHEMA, SUPPORTED_TYPES, new ClassifyConnectorFolderDirectlyInThePlanActionExecutor());
	}

	@Override
	protected ClassifyConnectorFolderActionParameters wrap(ActionParameters actionParameters) {
		return ClassifyConnectorFolderDirectlyInThePlanActionParameters.wrap(actionParameters);
	}
}
