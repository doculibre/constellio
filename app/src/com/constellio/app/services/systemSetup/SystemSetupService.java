package com.constellio.app.services.systemSetup;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.services.extensions.ConstellioModulesManagerRuntimeException.FailedToInstall;
import com.constellio.app.services.extensions.ConstellioModulesManagerRuntimeException.FailedToStart;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.systemSetup.SystemSetupServiceRuntimeException.SystemSetupServiceRuntimeException_InvalidSetupFile;
import com.constellio.app.services.systemSetup.SystemSetupServiceRuntimeException.SystemSetupServiceRuntimeException_InvalidSetupFileProperty;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.systemSetup.CollectionSetup;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.utils.InstanciationUtils;

public class SystemSetupService {

	private final ModelLayerFactory modelLayerFactory;
	private final AppLayerFactory appLayerFactory;
	private final AppLayerConfiguration appLayerConfiguration;

	public SystemSetupService(AppLayerFactory appLayerFactory, AppLayerConfiguration appLayerConfiguration) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.appLayerConfiguration = appLayerConfiguration;
	}

	public void setup() {
		Properties properties = readProperties();
		createAdminUser(properties);
		createCollection(properties);
	}

	private void createCollection(Properties properties) {
		final String collections = properties.getProperty("collections");
		if (StringUtils.isNotBlank(collections)) {
			for (String collectionItem : collections.split(",")) {
				String collectionCode = collectionItem.trim();
				addCollection(collectionCode, properties);
			}
		}
	}

	private void addCollection(String collectionCode, Properties properties) {
		String mainLanguage = appLayerFactory.getModelLayerFactory().getConfiguration().getMainDataLanguage();

		String collectionLanguageStr = properties.getProperty("collection." + collectionCode + ".languages");
		List<String> collectionLanguages;
		if (StringUtils.isNotBlank(collectionLanguageStr)) {
			collectionLanguages = Arrays.asList(collectionLanguageStr.split(","));
		} else {
			collectionLanguages = Arrays.asList(mainLanguage);
		}
		appLayerFactory.getCollectionsManager().createCollectionInCurrentVersion(collectionCode, collectionLanguages);

		CollectionsListManager collectionsListManager = modelLayerFactory.getCollectionsListManager();
		ConstellioModulesManager modulesManager = appLayerFactory.getModulesManager();
		String modules = properties.getProperty("collection." + collectionCode + ".modules");
		if (StringUtils.isNotBlank(modules)) {
			enableModules(collectionCode, collectionsListManager, modulesManager, modules);
		}

		String setupClassname = properties.getProperty("collection." + collectionCode + ".setup");
		if (setupClassname != null) {
			setup(collectionCode, setupClassname);
		}

		addAdminToTheNewCollection(collectionCode);
	}

	private void setup(String collectionCode, String setupClassname) {
		Class<?> setupClass = new InstanciationUtils().loadClassWithoutExpectableExceptions(setupClassname);
		CollectionSetup setup = (CollectionSetup) new InstanciationUtils()
				.instanciateWithoutExpectableExceptions(
						setupClass);
		setup.setup(collectionCode, appLayerFactory);
	}

	void addAdminToTheNewCollection(String collectionCode) {
		UserServices userServices = modelLayerFactory.newUserServices();
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		UserCredential admin = userServices.getUser(User.ADMIN);
		userServices.addUserToCollection(admin, collectionCode);
		User adminInCollection = userServices.getUserInCollection(User.ADMIN, collectionCode);
		adminInCollection.setCollectionReadAccess(true);
		adminInCollection.setCollectionWriteAccess(true);
		adminInCollection.setCollectionDeleteAccess(true);
		try {
			recordServices.update(adminInCollection.getWrappedRecord());
		} catch (RecordServicesException e) {
			throw new ImpossibleRuntimeException(e);
		}
	}

	private void enableModules(String collectionCode, CollectionsListManager collectionsListManager,
			ConstellioModulesManager modulesManager, String modules) {
		for (String moduleItem : modules.split(",")) {
			String moduleClass = moduleItem.trim();
			Module module = newInstanciationUtils().instanciate(moduleClass);

			if (!modulesManager.isInstalled(module)) {
				try {
					modulesManager.installValidModuleAndGetInvalidOnes(module, collectionsListManager);
				} catch (FailedToInstall failedToInstall) {
					throw new RuntimeException(failedToInstall);
				}
			}
			try {
				modulesManager.enableValidModuleAndGetInvalidOnes(collectionCode, module);
			} catch (FailedToStart failedToStart) {
				throw new RuntimeException(failedToStart);
			}

		}
	}

	private void createAdminUser(Properties properties) {
		String serviceKey = getRequiredProperty(properties, "admin.servicekey");
		String password = getRequiredProperty(properties, "admin.password");
		String username = "admin";
		String firstName = "System";
		String lastName = "Admin";
		String email = "admin@organization.com";
		UserCredentialStatus status = UserCredentialStatus.ACTIVE;
		String domain = "";
		List<String> globalGroups = new ArrayList<>();
		List<String> collections = new ArrayList<>();
		boolean isSystemAdmin = true;

		UserCredential adminCredentials = new UserCredential(username, firstName, lastName, email, serviceKey, isSystemAdmin,
				globalGroups, collections, new HashMap<String, LocalDateTime>(), status, domain, Arrays.asList(""), null);
		modelLayerFactory.newUserServices().addUpdateUserCredential(adminCredentials);
		AuthenticationService authenticationService = modelLayerFactory.newAuthenticationService();
		if (authenticationService.supportPasswordChange()) {
			authenticationService.changePassword("admin", password);
		}
	}

	private String getRequiredProperty(Properties properties, String property) {
		String value = properties.getProperty(property);
		if (StringUtils.isBlank(value)) {
			throw new SystemSetupServiceRuntimeException_InvalidSetupFileProperty(property);
		}
		return value;
	}

	Properties readProperties() {
		Reader reader = null;
		try {
			Properties properties = new Properties();
			reader = new FileReader(appLayerConfiguration.getSetupProperties());
			properties.load(reader);
			return properties;

		} catch (IOException e) {
			throw new SystemSetupServiceRuntimeException_InvalidSetupFile(e);

		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	public InstanciationUtils newInstanciationUtils() {
		return new InstanciationUtils();
	}
}
