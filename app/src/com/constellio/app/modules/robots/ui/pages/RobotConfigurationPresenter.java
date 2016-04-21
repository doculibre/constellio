package com.constellio.app.modules.robots.ui.pages;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.reports.DryRunReportBuilderFactory;
import com.constellio.app.modules.robots.ui.navigation.RobotViews;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.vaadin.server.StreamResource.StreamSource;

public class RobotConfigurationPresenter extends BaseRobotPresenter<RobotConfigurationView> {
	private String rootRobotId;

	public RobotConfigurationPresenter(RobotConfigurationView view) {
		super(view, Robot.DEFAULT_SCHEMA);
	}

	public RobotConfigurationPresenter forParams(String parameters) {
		rootRobotId = parameters;
		return this;
	}

	public RecordVO getRootRobot() {
		return presenterService().getRecordVO(rootRobotId, VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public String getRootRobotId() {
		return rootRobotId;
	}

	public boolean canExecute(RecordVO robot) {
		return manager().canExecute(robot.getId());
	}

	public void executeButtonClicked(RecordVO robot) {
		view.showMessage($("RobotConfigurationView.executionStarting"));
		manager().startRobotExecution(robot.getId());
	}

	public void addButtonClicked(RecordVO robot) {
		view.navigate().to(RobotViews.class).addRobot(robot.getId());
	}

	public void editButtonClicked(RecordVO robot) {
		view.navigate().to(RobotViews.class).editRobot(robot.getId());
	}

	public void deleteButtonClicked(RecordVO robot) {
		robotsService().deleteRobotHierarchy(robot.getId());
	}

	public void robotNavigationRequested(String robotId) {
		view.navigate().to(RobotViews.class).robotConfiguration(robotId);
	}

	public void viewLogsButtonClicked() {
		view.navigate().to(RobotViews.class).displayLogs(rootRobotId);
	}

	public String getReportTitle() {
		return new DryRunReportBuilderFactory(
				manager().dryRun(robotSchemas().getRobot(rootRobotId)), view.getSessionContext()).getFilename();
	}

	public StreamSource getResource() {
		final DryRunReportBuilderFactory factory = new DryRunReportBuilderFactory(
				manager().dryRun(robotSchemas().getRobot(rootRobotId)), view.getSessionContext());
		return new StreamSource() {
			@Override
			public InputStream getStream() {
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				try {
					factory.getReportBuilder(modelLayerFactory).build(output);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return new ByteArrayInputStream(output.toByteArray());
			}
		};
	}
}
