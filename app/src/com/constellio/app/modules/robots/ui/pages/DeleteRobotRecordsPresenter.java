package com.constellio.app.modules.robots.ui.pages;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.complementary.esRmRobots.services.ESRMRobotsServices;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.ui.navigation.RobotViews;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.pages.search.SearchPresenterService;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;

import static com.constellio.app.ui.i18n.i18n.$;

public class DeleteRobotRecordsPresenter extends BaseRobotPresenter<DeleteRobotRecordsView> {

	private String robotId;

	transient SchemasDisplayManager schemasDisplayManager;
	transient SearchPresenterService searchPresenterService;

	public DeleteRobotRecordsPresenter(DeleteRobotRecordsView view) {
		super(view, Robot.DEFAULT_SCHEMA);
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		User user = view.getConstellioFactories().getAppLayerFactory()
				.getModelLayerFactory().newUserServices()
				.getUserInCollection(view.getSessionContext().getCurrentUser().getUsername(), collection);
		searchPresenterService = new SearchPresenterService(collection, user, modelLayerFactory, null);
		schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
	}

	void forParams(String parameters) {
		robotId = parameters;

		Record robotRecord = getRecord(robotId);
		String robotTitle = robotRecord.getTitle();

		view.setTitle($("DeleteRobotRecordsView.viewTitle", robotTitle));

		final ProgressInfo progressInfo = new ProgressInfo();
		new Thread() {
			@Override
			public void run() {
				User currentUser = getCurrentUser();
				ESRMRobotsServices esRmRobotsServices = new ESRMRobotsServices(modelLayerFactory);
				try {
					esRmRobotsServices.deleteRobotFoldersAndDocuments(currentUser, robotId, progressInfo);
				} catch (Exception e) {
					String stackTrace = ExceptionUtils.getStackTrace(e);
					progressInfo.getErrorMessages().add(stackTrace);
					progressInfo.setTask($("DeleteRobotRecordsView.stoppedBecauseOfError"));
				}
			}
		}.start();

		view.setProgressInfo(progressInfo);
	}

	public void done() {
		view.navigate().to(RobotViews.class).robotConfiguration(robotId);
	}

	public void backButtonClicked() {
		view.navigate().to(RobotViews.class).robotConfiguration(robotId);
	}

}
