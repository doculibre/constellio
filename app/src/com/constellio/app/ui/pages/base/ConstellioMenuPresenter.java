package com.constellio.app.ui.pages.base;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserPhotosServicesRuntimeException.UserPhotosServicesRuntimeException_UserHasNoPhoto;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class ConstellioMenuPresenter implements Serializable {

	private static final String SHOW_PICTURE_STREAM = "ConstellioMenuPresenter-ShowPicture";

	private ConstellioMenu constellioMenu;

	private transient ConstellioFactories constellioFactories;

	private transient ModelLayerFactory modelLayerFactory;

	public ConstellioMenuPresenter(ConstellioMenu constellioMenu) {
		this.constellioMenu = constellioMenu;

		constellioFactories = constellioMenu.getConstellioFactories();
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		if (constellioFactories == null) {
			constellioFactories = ConstellioFactories.getInstance();
		}
		modelLayerFactory = constellioFactories.getModelLayerFactory();
	}

	public void homeButtonClicked() {
		constellioMenu.navigateTo().home();
	}

	//FIXME use service and remove redundant code in LoginPresenter
	Locale getSessionLanguage(User userInLastCollection) {
		String userPreferredLanguage = userInLastCollection.getLoginLanguageCode();
		String systemLanguage = modelLayerFactory.getConfiguration().getMainDataLanguage();
		if (StringUtils.isBlank(userPreferredLanguage)) {
			return getLocale(systemLanguage);
		} else {
			List<String> collectionLanguages = modelLayerFactory.getCollectionsListManager()
					.getCollectionLanguages(userInLastCollection.getCollection());
			if (collectionLanguages == null || collectionLanguages.isEmpty() || !collectionLanguages
					.contains(userPreferredLanguage)) {
				return getLocale(systemLanguage);
			} else {
				return getLocale(userPreferredLanguage);
			}
		}
	}

	private Locale getLocale(String languageCode) {
		for (Language language : Language.values()) {
			if (language.getCode().equals(languageCode)) {
				return new Locale(languageCode);
			}
		}
		throw new ImpossibleRuntimeException("Invalid language " + languageCode);
	}

	public void editProfileButtonClicked(String params) {
		constellioMenu.navigateTo().modifyProfil(params);
	}

	public void preferencesButtonClicked() {
	}

	public boolean isUserManagementButtonVisible() {
		return true;
	}

	public void userManagementButtonClicked() {

	}

	public boolean isAdminModuleButtonVisible() {
		return true;
	}

	public void adminModuleButtonClicked() {

	}

	public boolean isArchivesManagementButtonVisible() {
		return true;
	}

	public void archivesManagementButtonClicked() {

	}

	public boolean isLogsButtonVisible() {
		return true;
	}

	public void logsButtonClicked() {

	}

	public void signOutButtonClicked() {
		SessionContext sessionContext = constellioMenu.getSessionContext();
		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		User user = userServices.getUserInCollection(
				sessionContext.getCurrentUser().getUsername(), sessionContext.getCurrentCollection());
		modelLayerFactory.newLoggingServices().logout(user);

		sessionContext.setCurrentCollection(null);
		sessionContext.setCurrentUser(null);
		sessionContext.setForcedSignOut(true);
		sessionContext.clearSelectedRecordIds();
		constellioMenu.updateUIContent();
	}

	public InputStream newUserPhotoInputStream() {
		UserVO currentUser = constellioMenu.getSessionContext().getCurrentUser();
		UserPhotosServices photosServices = ConstellioFactories.getInstance().getModelLayerFactory().newUserPhotosServices();

		try {
			return photosServices.getPhotoInputStream(currentUser.getUsername()).create(SHOW_PICTURE_STREAM);
		} catch (UserPhotosServicesRuntimeException_UserHasNoPhoto u) {
			return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasCurrentUserPhoto() {
		UserVO currentUser = constellioMenu.getSessionContext().getCurrentUser();
		UserPhotosServices photosServices = ConstellioFactories.getInstance().getModelLayerFactory().newUserPhotosServices();
		return photosServices.hasPhoto(currentUser.getUsername());
	}

	private List<String> getCollectionLanguagesOrderedByCode(String collection) {
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		return appLayerFactory.getCollectionsManager().getCollectionLanguages(collection);
	}

	public void languageSelected(String languageText, String collection) {
		List<String> allLanguagesCodes = getCollectionLanguagesOrderedByCode(collection);
		for (String code : allLanguagesCodes) {
			if ($("Language." + code).equals(languageText)) {
				Locale locale = new Locale(code);
				i18n.setLocale(locale);
				constellioMenu.setLocale(locale);
				constellioMenu.updateUIContent();
			}
		}
	}

	public List<String> getCollectionLanguages(String collection) {
		List<String> returnList = new ArrayList<>();
		for (String code : getCollectionLanguagesOrderedByCode(collection)) {
			returnList.add($("Language." + code));
		}
		return returnList;
	}

	public boolean hasUserRightToViewSystemState() {
		SessionContext sessionContext = constellioMenu.getSessionContext();
		if (sessionContext != null) {
			ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
			UserServices userServices = modelLayerFactory.newUserServices();
			User user = userServices.getUserInCollection(
					sessionContext.getCurrentUser().getUsername(), sessionContext.getCurrentCollection());
			return user.has(CorePermissions.VIEW_SYSTEM_STATE).onSomething() || user.has(CorePermissions.VIEW_SYSTEM_STATE).globally();
		} else {
			return false;
		}
	}

	public String getCurrentVersion() {
		AppLayerFactory appLayerFactory = constellioMenu.getConstellioFactories().getAppLayerFactory();

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

	private String toPrintableVersion(String version) {
		String[] versionSplitted = version.split("\\.");

		if (versionSplitted.length == 5) {
			return versionSplitted[0] + "." + versionSplitted[1] + "." + versionSplitted[2] + "." + versionSplitted[3];
		}
		return version;
	}

	public String getSystemStateImportantMessage() {
		AppLayerFactory appLayerFactory = constellioMenu.getConstellioFactories().getAppLayerFactory();
		SystemGlobalConfigsManager manager = appLayerFactory.getSystemGlobalConfigsManager();
		if (manager.hasLastReindexingFailed()) {
			ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
			User user = new PresenterService(modelLayerFactory).getCurrentUser(ConstellioUI.getCurrentSessionContext());
			return user.has(CorePermissions.MANAGE_SYSTEM_UPDATES).globally() ? $("MainLayout.reindexingFailed") : null;
		}
		if (manager.isReindexingRequired()) {
			ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
			User user = new PresenterService(modelLayerFactory).getCurrentUser(ConstellioUI.getCurrentSessionContext());
			return user.has(CorePermissions.MANAGE_SYSTEM_UPDATES).globally() ? $("MainLayout.reindexingRequired") : null;

		} else if (manager.isCacheRebuildRequired()) {
			//A reindexing includes a cache rebuild, no need to add this message if a reindexing is required

			ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
			User user = new PresenterService(modelLayerFactory).getCurrentUser(ConstellioUI.getCurrentSessionContext());
			return user.has(CorePermissions.MANAGE_SYSTEM_UPDATES).globally() ? $("MainLayout.cacheRebuildRequired") : null;
		}

		return null;
	}

}
