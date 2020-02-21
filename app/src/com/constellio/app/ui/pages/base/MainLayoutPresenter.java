package com.constellio.app.ui.pages.base;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.guide.GuideManager;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

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
		if (userVO == null) {
			return null;
		} else {
			AppLayerFactory appLayerFactory = mainLayout.getHeader().getConstellioFactories().getAppLayerFactory();
			RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(collection, appLayerFactory);
			return schemas.getUser(userVO.getId());
		}

	}

	public String getCurrentVersion() {
		AppLayerFactory appLayerFactory = mainLayout.getHeader().getConstellioFactories().getAppLayerFactory();

		String version = appLayerFactory.newApplicationService().getWarVersion();

		if (version == null || version.equals("5.0.0")) {
			File versionFile = new File(new FoldersLocator().getConstellioProject(), "version");
			if (versionFile.exists()) {
				try {
					version = FileUtils.readFileToString(versionFile);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				version = "no version file";
			}
		}

		if (version != null) {
			return toPrintableVersion(version);
		} else {
			return "";
		}
	}

	protected String getGuideUrl() {
		BaseView currentView = mainLayout.getHeader().getCurrentView();
		if (currentView == null) {
			return null;
		}
		AppLayerFactory appLayerFactory = mainLayout.getHeader().getConstellioFactories().getAppLayerFactory();
		GuideManager manager = new GuideManager(appLayerFactory.getModelLayerFactory().getDataLayerFactory());
		String language = ConstellioUI.getCurrentSessionContext().getCurrentLocale().getLanguage();
		String field = "guide." + currentView.getClass().getSimpleName();
		String url = manager.getPropertyValue(language, field);
		if (url == null || url.isEmpty()) {
			return $(field);
		}
		return url;
	}

	private String toPrintableVersion(String version) {
		String[] versionSplitted = version.split("\\.");

		if (versionSplitted.length == 5) {
			return versionSplitted[0] + "." + versionSplitted[1] + "." + versionSplitted[2] + "." + versionSplitted[3];
		}
		return version;
	}

	public String getBadge(NavigationItem item) {
		User user = getUser();
		AppLayerFactory appLayerFactory = mainLayout.getHeader().getConstellioFactories().getAppLayerFactory();
		return item.getBadge(user, appLayerFactory);
	}

	public boolean hasGuideConfigurationPermission() {
		return getUser().has(CorePermissions.SYSTEM_MANAGEMENT).globally();
	}
}
