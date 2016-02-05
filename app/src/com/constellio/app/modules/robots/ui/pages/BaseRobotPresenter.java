package com.constellio.app.modules.robots.ui.pages;

import com.constellio.app.modules.robots.ConstellioRobotsModule;
import com.constellio.app.modules.robots.constants.RobotsPermissionsTo;
import com.constellio.app.modules.robots.model.services.RobotsService;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.modules.robots.services.RobotsManager;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.wrappers.User;

public abstract class BaseRobotPresenter<T extends BaseView> extends SingleSchemaBasePresenter<T> {
	private transient RobotsManager robotsManager;
	private transient RobotsService robotsService;
	private transient RobotSchemaRecordServices robotSchemaRecordServices;

	public BaseRobotPresenter(T view, String schemaCode) {
		super(view, schemaCode);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RobotsPermissionsTo.MANAGE_ROBOTS).globally();
	}

	protected RobotsManager manager() {
		if (robotsManager == null) {
			robotsManager = appLayerFactory.getRegisteredManager(collection, ConstellioRobotsModule.ID, RobotsManager.ID);
		}
		return robotsManager;
	}

	protected RobotsService robotsService() {
		if (robotsService == null) {
			robotsService = new RobotsService(collection, appLayerFactory);
		}
		return robotsService;
	}

	protected RobotSchemaRecordServices robotSchemas() {
		if (robotSchemaRecordServices == null) {
			robotSchemaRecordServices = new RobotSchemaRecordServices(collection, appLayerFactory);
		}
		return robotSchemaRecordServices;
	}
}
