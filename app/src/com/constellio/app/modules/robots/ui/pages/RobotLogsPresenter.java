package com.constellio.app.modules.robots.ui.pages;

import com.constellio.app.modules.robots.constants.RobotsPermissionsTo;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.ui.navigation.RobotViews;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RobotLogsPresenter extends BaseRobotPresenter<RobotLogsView> {
	private String robotId;

	public RobotLogsPresenter(RobotLogsView view) {
		super(view, Robot.DEFAULT_SCHEMA);
	}

	public RobotLogsPresenter forParams(String parameters) {
		robotId = parameters;
		return this;
	}

	public RecordVO getRobot() {
		return presenterService().getRecordVO(robotId, VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public RecordVODataProvider getLogs() {
		MetadataSchemaVO schema = new MetadataSchemaToVOBuilder()
				.build(robotSchemas().robotLog.schema(), VIEW_MODE.TABLE, view.getSessionContext());
		final List<String> tree = robotsService().loadIdTreeOf(robotId);
		return new RecordVODataProvider(schema, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				LogicalSearchCondition condition = from(robotSchemas().robotLog.schemaType())
						.where(robotSchemas().robotLog.robot()).isIn(tree);
				return new LogicalSearchQuery(condition).sortDesc(Schemas.CREATED_ON);
			}
		};
	}

	public void backButtonClicked() {
		view.navigate().to(RobotViews.class).robotConfiguration(robotId);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RobotsPermissionsTo.MANAGE_ROBOTS).globally();
	}
}
