package com.constellio.app.modules.rm.ui.pages.systemCheck;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.records.SystemCheckManager;
import com.constellio.app.services.records.SystemCheckReportBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.StreamResource;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class SystemCheckPresenter extends BasePresenter<SystemCheckView> {

	private boolean buttonsDisabled = false;

	public SystemCheckPresenter(SystemCheckView view) {
		super(view);
	}

	public SystemCheckPresenter(SystemCheckView view, ConstellioFactories constellioFactories,
			SessionContext sessionContext) {
		super(view, constellioFactories, sessionContext);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		UserServices userServices = userServices();
		return userServices.getUser(user.getUsername()).isSystemAdmin()
				|| userServices.has(user).allGlobalPermissionsInAnyCollection(
				CorePermissions.MANAGE_SYSTEM_COLLECTIONS, CorePermissions.MANAGE_SECURITY,
				CorePermissions.MANAGE_SYSTEM_SERVERS);
	}

	SystemCheckManager getSystemCheckManager() {
		return appLayerFactory.getSystemCheckManager();
	}

	void viewAssembled() {
		SystemCheckManager systemCheckManager = getSystemCheckManager();
		buttonsDisabled = systemCheckManager.isSystemCheckResultsRunning();
		view.setSystemCheckRunning(buttonsDisabled);
		if (systemCheckManager.getLastSystemCheckResults() != null) {
			String reportContent = new SystemCheckReportBuilder(systemCheckManager).build();
			view.setReportContent(reportContent);
		}
	}

	void startSystemCheckButtonClicked() {
		SystemCheckManager systemCheckManager = getSystemCheckManager();
		systemCheckManager.startSystemCheck(false);
		view.setSystemCheckRunning(true);
		buttonsDisabled = true;
	}

	void startSystemCheckAndRepairButtonClicked() {
		SystemCheckManager systemCheckManager = getSystemCheckManager();
		systemCheckManager.startSystemCheck(true);
		view.setSystemCheckRunning(true);
		buttonsDisabled = true;
	}

	void viewRefreshed() {
		SystemCheckManager systemCheckManager = getSystemCheckManager();
		boolean systemCheckRunning = systemCheckManager.isSystemCheckResultsRunning();
		if (buttonsDisabled != systemCheckRunning) {
			String reportContent = new SystemCheckReportBuilder(systemCheckManager).build();
			view.setReportContent(reportContent);
			view.setSystemCheckRunning(false);
			buttonsDisabled = false;
		}
	}

	File getReferencesFor(final String id) {
		File file = new File("referenceReport.txt");
		try {
			LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(view.getCollection()).where(Schemas.ALL_REFERENCES).isEqualTo(id);
			List<Record> recordLists = modelLayerFactory.newSearchServices().search(new LogicalSearchQuery(condition));
			for (Record record : recordLists) {
				FileUtils.write(file, record.getId() + " " + record.getSchemaCode() + " " + record.getTitle() + "\n", true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return file;
	}

	void backButtonClicked() {
		view.navigate().to().adminModule();
	}

}
