package com.constellio.app.ui.pages.base;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.utils.GradleFileVersionParser;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public class MainLayoutPresenter implements Serializable {

	private MainLayout mainLayout;

	public MainLayoutPresenter(MainLayout mainLayout) {
		this.mainLayout = mainLayout;
	}

	public ComponentState getStateFor(NavigationItem item) {
		return item.getStateFor(getUser(), mainLayout.getHeader().getConstellioFactories().getAppLayerFactory());
	}

	public List<NavigationItem> getNavigationItems() {
		List<NavigationItem> items = new ArrayList<>();
		ConstellioModulesManagerImpl manager = (ConstellioModulesManagerImpl) mainLayout.getHeader().getConstellioFactories()
				.getAppLayerFactory().getModulesManager();
		NavigationConfig config = manager.getNavigationConfig(mainLayout.getHeader().getCollection());
		items.addAll(config.getNavigation(MainLayout.MAIN_LAYOUT_NAVIGATION));

		Collections.sort(items);

		return items;
	}

	public User getUser() {
		String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		UserVO userVO = ConstellioUI.getCurrentSessionContext().getCurrentUser();
		ModelLayerFactory modelLayerFactory = mainLayout.getHeader().getConstellioFactories().getModelLayerFactory();
		AppLayerFactory appLayerFactory = mainLayout.getHeader().getConstellioFactories().getAppLayerFactory();
		RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(collection, appLayerFactory);
		return schemas.getUser(userVO.getId());

	}

	public String getCurrentVersion() {
		AppLayerFactory appLayerFactory = mainLayout.getHeader().getConstellioFactories().getAppLayerFactory();

		String version = appLayerFactory.newApplicationService().getWarVersion();

		if (version == null || version.equals("5.0.0")) {
			version = GradleFileVersionParser.getVersion();
		}

		if (version != null) {
			return toPrintableVersion(version);
		} else {
			return "";
		}
	}

	public String getMessage() {
		AppLayerFactory appLayerFactory = mainLayout.getHeader().getConstellioFactories().getAppLayerFactory();
		SystemGlobalConfigsManager manager = appLayerFactory.getSystemGlobalConfigsManager();
		if (manager.isReindexingRequired()) {
			ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
			User user = new PresenterService(modelLayerFactory).getCurrentUser(ConstellioUI.getCurrentSessionContext());
			return user.has(CorePermissions.MANAGE_SYSTEM_UPDATES).globally() ? $("MainLayout.reindexingRequired") : null;
		}
		return null;
	}

	private String toPrintableVersion(String version) {
		String[] versionSplitted = version.split("\\.");

		if (versionSplitted.length == 5) {
			return versionSplitted[0] + "." + versionSplitted[1] + "." + versionSplitted[2] + "." + versionSplitted[3];
		}
		return version;
	}
}
