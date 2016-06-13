package com.constellio.app.modules.robots.services;

import com.constellio.app.modules.robots.ConstellioRobotsModule;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.services.factories.AppLayerFactory;

public class RobotSchemaRecordServices extends GeneratedRobotSchemaRecordServices {
	AppLayerFactory appLayerFactory;

	public RobotSchemaRecordServices(String collection, AppLayerFactory appLayerFactory) {
		super(collection, appLayerFactory);
		this.appLayerFactory = appLayerFactory;
	}

	public ActionParameters newActionParameters(String schema) {
		return wrapActionParameters(create(actionParameters.schemaType().getSchema(schema)));
	}

	public RobotsManager getRobotsManager() {
		return appLayerFactory.getRegisteredManager(getCollection(), ConstellioRobotsModule.ID, RobotsManager.ID);
	}

	public AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}

	public String getRobotCodesPath(Robot robot, String delimitter) {
		String parent = robot.getParent();
		if (parent == null) {
			return robot.getCode();
		} else {
			Robot parentRobot = getRobot(parent);
			return getRobotCodesPath(parentRobot, delimitter);
		}
	}

}
