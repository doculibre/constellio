package com.constellio.sdk;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.factories.ConstellioFactoriesDecorator;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.utils.Factory;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.security.authentification.AuthenticationService;

import java.io.File;

/**
 * Created by dakota on 11/9/15.
 */
public class SDKScriptUtils {

	public static AppLayerFactory startApplicationWithoutBackgroundProcessesAndAuthentication() {

		ConstellioFactoriesDecorator constellioFactoriesDecorator = new ConstellioFactoriesDecorator() {
			@Override
			public AppLayerConfiguration decorateAppLayerConfiguration(AppLayerConfiguration appLayerConfiguration) {
				return super.decorateAppLayerConfiguration(appLayerConfiguration);
			}

			@Override
			public ModelLayerConfiguration decorateModelLayerConfiguration(
					ModelLayerConfiguration modelLayerConfiguration) {

				File keyTxt = new File(modelLayerConfiguration.getDataLayerConfiguration().getSettingsFileSystemBaseFolder(),
						"key.txt");

				//modelLayerConfiguration = spy(modelLayerConfiguration);
				modelLayerConfiguration.setBatchProcessesEnabled(false);
				Factory<EncryptionServices> encryptionServicesFactory = new Factory<EncryptionServices>() {
					@Override
					public EncryptionServices get() {
						return new FakeEncryptionServices();
					}
				};
				//				when(modelLayerConfiguration.getEncryptionServicesFactory()).thenReturn(encryptionServicesFactory);
				//				when(modelLayerConfiguration.getConstellioEncryptionFile()).thenReturn(keyTxt);


				return modelLayerConfiguration;
			}

			@Override
			public DataLayerConfiguration decorateDataLayerConfiguration(
					DataLayerConfiguration dataLayerConfiguration) {

				dataLayerConfiguration.setBackgroundThreadsEnabled(false);
				return super.decorateDataLayerConfiguration(dataLayerConfiguration);
			}

			@Override
			public ModelLayerFactory decorateModelServicesFactory(final ModelLayerFactory modelLayerFactory) {
				modelLayerFactory.setAuthenticationService(new AuthenticationService() {
					@Override
					public boolean authenticate(String username, String password) {
						return modelLayerFactory.newUserServices().getUserCredential(username) != null;
					}

					@Override
					public boolean supportPasswordChange() {
						return false;
					}

					@Override
					public void changePassword(String username, String oldPassword, String newPassword) {

					}

					@Override
					public void changePassword(String username, String newPassword) {

					}

					@Override
					public void reloadServiceConfiguration() {

					}
				});
				return modelLayerFactory;
			}
		};

		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance(constellioFactoriesDecorator).getAppLayerFactory();
		return appLayerFactory;
	}

	public static AppLayerFactory startApplicationWithBatchProcesses() {

		ConstellioFactoriesDecorator constellioFactoriesDecorator = new ConstellioFactoriesDecorator() {
			@Override
			public AppLayerConfiguration decorateAppLayerConfiguration(AppLayerConfiguration appLayerConfiguration) {
				return super.decorateAppLayerConfiguration(appLayerConfiguration);
			}

			@Override
			public ModelLayerConfiguration decorateModelLayerConfiguration(
					ModelLayerConfiguration modelLayerConfiguration) {
				modelLayerConfiguration.setBatchProcessesEnabled(true);
				return super.decorateModelLayerConfiguration(modelLayerConfiguration);
			}

			@Override
			public DataLayerConfiguration decorateDataLayerConfiguration(
					DataLayerConfiguration dataLayerConfiguration) {
				dataLayerConfiguration.setBackgroundThreadsEnabled(false);
				return super.decorateDataLayerConfiguration(dataLayerConfiguration);
			}

			@Override
			public ModelLayerFactory decorateModelServicesFactory(final ModelLayerFactory modelLayerFactory) {
				modelLayerFactory.setAuthenticationService(new AuthenticationService() {
					@Override
					public boolean authenticate(String username, String password) {
						return modelLayerFactory.newUserServices().getUserCredential(username) != null;
					}

					@Override
					public boolean supportPasswordChange() {
						return false;
					}

					@Override
					public void changePassword(String username, String oldPassword, String newPassword) {

					}

					@Override
					public void changePassword(String username, String newPassword) {

					}

					@Override
					public void reloadServiceConfiguration() {

					}
				});
				return modelLayerFactory;
			}
		};

		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance(constellioFactoriesDecorator).getAppLayerFactory();
		return appLayerFactory;
	}

	public static AppLayerFactory startApplicationWithBatchProcessesAndBackgroundThreads() {

		ConstellioFactoriesDecorator constellioFactoriesDecorator = new ConstellioFactoriesDecorator() {
			@Override
			public AppLayerConfiguration decorateAppLayerConfiguration(AppLayerConfiguration appLayerConfiguration) {
				return super.decorateAppLayerConfiguration(appLayerConfiguration);
			}

			@Override
			public ModelLayerConfiguration decorateModelLayerConfiguration(
					ModelLayerConfiguration modelLayerConfiguration) {
				modelLayerConfiguration.setBatchProcessesEnabled(true);
				return super.decorateModelLayerConfiguration(modelLayerConfiguration);
			}

			@Override
			public DataLayerConfiguration decorateDataLayerConfiguration(
					DataLayerConfiguration dataLayerConfiguration) {
				dataLayerConfiguration.setBackgroundThreadsEnabled(true);
				return super.decorateDataLayerConfiguration(dataLayerConfiguration);
			}

			@Override
			public ModelLayerFactory decorateModelServicesFactory(final ModelLayerFactory modelLayerFactory) {
				modelLayerFactory.setAuthenticationService(new AuthenticationService() {
					@Override
					public boolean authenticate(String username, String password) {
						return modelLayerFactory.newUserServices().getUserCredential(username) != null;
					}

					@Override
					public boolean supportPasswordChange() {
						return false;
					}

					@Override
					public void changePassword(String username, String oldPassword, String newPassword) {

					}

					@Override
					public void changePassword(String username, String newPassword) {

					}

					@Override
					public void reloadServiceConfiguration() {

					}
				});
				return modelLayerFactory;
			}
		};

		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance(constellioFactoriesDecorator).getAppLayerFactory();
		return appLayerFactory;
	}
}