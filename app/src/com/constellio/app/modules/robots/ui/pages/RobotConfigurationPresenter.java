package com.constellio.app.modules.robots.ui.pages;

import com.constellio.app.modules.robots.model.DryRunRobotAction;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.reports.DryRunReportWriterFactory;
import com.constellio.app.modules.robots.ui.navigation.RobotViews;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.vaadin.server.StreamResource.StreamSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

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
		return new DryRunReportWriterFactory(new ArrayList<DryRunRobotAction>(), view.getSessionContext()).getFilename();
	}

	public StreamSource getResource() {
		return new StreamSource() {
			private static final String MUTEX = "mutex";
			private DryRunReportWriterFactory factory;

			@Override
			public InputStream getStream() {
				ByteArrayOutputStream output = new ByteArrayOutputStream();

				synchronized (MUTEX) {
					if (factory == null) {
						List<DryRunRobotAction> dryRun = manager().dryRun(robotSchemas().getRobot(rootRobotId));
						factory = new DryRunReportWriterFactory(dryRun, view.getSessionContext());
					}

					try {
						factory.getReportBuilder(modelLayerFactory).write(output);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

				return new ByteArrayInputStream(output.toByteArray());
			}
		};
	}

	public void backButtonClicked() {
		view.navigate().to(RobotViews.class).listRootRobots();
	}

	public void deleteRecordsButtonClicked() {
		view.navigate().to(RobotViews.class).deleteRobotRecords(rootRobotId);
	}

	public void deleteRecordsButtonClicked(RecordVO robot) {
		view.navigate().to(RobotViews.class).deleteRobotRecords(rootRobotId);
	}

}
