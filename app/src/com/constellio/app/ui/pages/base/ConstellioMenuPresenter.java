package com.constellio.app.ui.pages.base;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserPhotosServicesRuntimeException.UserPhotosServicesRuntimeException_UserHasNoPhoto;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.lang3.StringUtils;

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
}
