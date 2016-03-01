package com.constellio.app.modules.complementary.esRmRobots.actions;

import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInParentFolderActionParameters;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.services.RobotsManager;

public class ClassifyConnectorFolderInParentFolderActionExecutor extends ClassifyConnectorFolderBaseActionExecutor {

	public final static String ID = ClassifyConnectorFolderInParentFolderActionParameters.SCHEMA_LOCAL_CODE;

	public final static String PARAMETER_SCHEMA = ClassifyConnectorFolderInParentFolderActionParameters.SCHEMA_LOCAL_CODE;

	public static void registerIn(RobotsManager robotsManager) {
		robotsManager.registerAction(
				ID, PARAMETER_SCHEMA, SUPPORTED_TYPES, new ClassifyConnectorFolderInParentFolderActionExecutor());
	}

	@Override
	protected ClassifyConnectorFolderActionParameters wrap(ActionParameters actionParameters) {
		return ClassifyConnectorFolderInParentFolderActionParameters.wrap(actionParameters);
	}
}
