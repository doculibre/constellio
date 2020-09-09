package com.constellio.app.services.factories;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.conf.PropertiesAppLayerConfiguration;
import com.constellio.app.services.appManagement.GetWarVersionUtils;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.conf.PropertiesDataLayerConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.utils.Delayed;
import com.constellio.data.utils.Holder;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.data.utils.TenantUtils;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.conf.PropertiesModelLayerConfiguration;
import com.constellio.model.entities.enums.DecryptionVersion;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactoryImpl;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ConstellioFactories {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioFactories.class);

	public static ConstellioFactoriesInstanceProvider instanceProvider = new SingletonConstellioFactoriesInstanceProvider();

	private File propertyFile;
	private ConstellioFactoriesDecorator decorator;
	private DataLayerConfiguration dataLayerConfiguration;
	private ModelLayerConfiguration modelLayerConfiguration;
	private AppLayerConfiguration appLayerConfiguration;

	private FoldersLocator foldersLocator;
	private IOServicesFactory ioServicesFactory;
	private DataLayerFactory dataLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private AppLayerFactory appLayerFactory;

	private ConstellioFactories() {
	}

	public static ConstellioFactories getInstanceIfAlreadyStarted() {
		return instanceProvider.getInstance(TenantUtils.getTenantId(), null, false);
	}

	public static boolean isInitialized() {
		return instanceProvider.isInitialized(TenantUtils.getTenantId());
	}

	public static ConstellioFactories getInstance() {
		ConstellioFactoriesDecorator constellioFactoriesDecorator = new ConstellioFactoriesDecorator();
		return getInstance(() -> new FoldersLocator().getConstellioProperties(), constellioFactoriesDecorator);
	}

	public static ConstellioFactories getInstance(boolean acceptingFailedFactories) {
		ConstellioFactoriesDecorator constellioFactoriesDecorator = new ConstellioFactoriesDecorator();
		return getInstance(TenantUtils.getTenantId(), () -> new FoldersLocator().getConstellioProperties(), constellioFactoriesDecorator, acceptingFailedFactories);
	}

	public static ConstellioFactories getInstance(String tenantId) {
		ConstellioFactoriesDecorator constellioFactoriesDecorator = new ConstellioFactoriesDecorator();
		return getInstance(tenantId, () -> new FoldersLocator().getConstellioProperties(), constellioFactoriesDecorator, false);
	}

	public static ConstellioFactories getInstance(ConstellioFactoriesDecorator constellioFactoriesDecorator) {
		return getInstance(() -> new FoldersLocator().getConstellioProperties(), constellioFactoriesDecorator);
	}

	public static ConstellioFactories getInstance(final Supplier<File> propertyFileSupplier,
												  final ConstellioFactoriesDecorator decorator) {
		return getInstance(TenantUtils.getTenantId(), propertyFileSupplier, decorator, false);
	}

	private static ConstellioFactories getInstance(final String tenantId, final Supplier<File> propertyFileSupplier,
												   final ConstellioFactoriesDecorator decorator,
												   boolean acceptingFailedFactories) {
		String instanceName = tenantId != null ? "tenant" + tenantId : null;
		return instanceProvider.getInstance(tenantId,
				() -> buildFor(propertyFileSupplier.get(), decorator, instanceName, (short) 0), acceptingFailedFactories);
	}

	public static void start() {
	}

	public static void clear() {
		if (TenantUtils.isSupportingTenants()) {
			String tenantId = TenantUtils.getTenantId();
			instanceProvider.clear(tenantId);
		} else {
			instanceProvider.clear(TenantUtils.EMPTY_TENANT_ID);
		}
	}

	public static ConstellioFactories buildFor(File propertyFile, ConstellioFactoriesDecorator decorator,
											   String instanceName, short instanceId) {
		ConstellioFactories factories = new ConstellioFactories();

		if (instanceName == null) {
			instanceName = "default";
		}

		factories.propertyFile = propertyFile;
		Map<String, String> configs = PropertyFileUtils.loadKeyValues(propertyFile);

		factories.decorator = decorator;
		factories.foldersLocator = decorator.decorateFoldersLocator(new FoldersLocator());
		factories.buildConfiguration(propertyFile, configs);
		factories.buildLayers(instanceName, instanceId);
		return factories;
	}

	private void buildLayers(String instanceName, short instanceId) {
		Delayed<ConstellioModulesManager> modulesManager = new Delayed<>();
		ioServicesFactory = new IOServicesFactory(dataLayerConfiguration.getTempFolder());

		String warVersion = GetWarVersionUtils.getWarVersionUsingGradleAsFallback(null);

		dataLayerFactory = decorator.decorateDataLayerFactory(new DataLayerFactory(ioServicesFactory, dataLayerConfiguration,
				decorator.getStatefullServiceDecorator(), instanceName, instanceId, warVersion));

		Holder<AppLayerFactory> appLayerFactoryHolder = new Holder<>();
		Runnable markForReindexingRunnable = () -> {
			appLayerFactoryHolder.get().getSystemGlobalConfigsManager().setReindexingRequired(true);
		};

		Runnable markForCacheRebuildRunnable = () -> {
			appLayerFactoryHolder.get().getSystemGlobalConfigsManager().markLocalCachesAsRequiringRebuild();
		};
		modelLayerFactory = decorator.decorateModelServicesFactory(new ModelLayerFactoryImpl(dataLayerFactory, foldersLocator,
				modelLayerConfiguration, decorator.getStatefullServiceDecorator(), modulesManager, instanceName, instanceId,
				new ModelLayerFactoryFactory(), markForReindexingRunnable, markForCacheRebuildRunnable));

		appLayerFactory = decorator.decorateAppServicesFactory(new AppLayerFactoryImpl(appLayerConfiguration, modelLayerFactory,
				dataLayerFactory, decorator.getStatefullServiceDecorator(), instanceName, instanceId));

		appLayerFactoryHolder.set(appLayerFactory);
		modulesManager.set(appLayerFactory.getModulesManager());

	}

	private void buildConfiguration(File propertyFile, Map<String, String> configs) {
		File defaultTempFolder = foldersLocator.getDefaultTempFolder();
		File defaultFileSystemBaseFolder = new File(foldersLocator.getConfFolder(), "settings");

		this.dataLayerConfiguration = decorator
				.decorateDataLayerConfiguration(new PropertiesDataLayerConfiguration(configs, defaultTempFolder,
						defaultFileSystemBaseFolder, propertyFile));
		this.modelLayerConfiguration = decorator
				.decorateModelLayerConfiguration(
						new PropertiesModelLayerConfiguration(configs, dataLayerConfiguration, foldersLocator, propertyFile, new Supplier<DecryptionVersion>() {
							@Override
							public DecryptionVersion get() {

								return getModelLayerFactory().getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.DECRYPTION_VERSION);
							}
						}));
		this.appLayerConfiguration = decorator
				.decorateAppLayerConfiguration(
						new PropertiesAppLayerConfiguration(configs, modelLayerConfiguration, foldersLocator, propertyFile));
	}

	private static AtomicInteger factoryIdSeq = new AtomicInteger();

	public void onRequestStarted() {

		long factoryId = factoryIdSeq.incrementAndGet();

		//		AppLayerFactoryImpl appLayerFactory = (AppLayerFactoryImpl) getAppLayerFactory();
		//		AppLayerFactoryWithRequestCacheImpl requestCachedAppLayerFactory = new AppLayerFactoryWithRequestCacheImpl(
		//				appLayerFactory, "" + factoryId);

		//		requestCachedFactories.set(requestCachedAppLayerFactory);
		//		if (Toggle.LOG_REQUEST_CACHE.isEnabled()) {
		//			LOGGER.info("onRequestStarted() - " + requestCachedAppLayerFactory.toString());
		//		}
	}

	public void onRequestEnded() {

		//		AppLayerFactory appLayerFactory = requestCachedFactories.get();
		//		if (appLayerFactory != null && appLayerFactory instanceof AppLayerFactoryWithRequestCacheImpl) {
		//			((AppLayerFactoryWithRequestCacheImpl) appLayerFactory).disconnect();
		//			if (Toggle.LOG_REQUEST_CACHE.isEnabled()) {
		//				LOGGER.info("onRequestEnded() - " + appLayerFactory.toString());
		//			}
		//		}
		//		requestCachedFactories.set(null);
	}

	public IOServicesFactory getIoServicesFactory() {
		return ioServicesFactory;
	}

	public DataLayerFactory getDataLayerFactory() {
		return getModelLayerFactory().getDataLayerFactory();
	}

	public ModelLayerFactory getModelLayerFactory() {
		return getAppLayerFactory().getModelLayerFactory();
	}

	public DataLayerConfiguration getDataLayerConfiguration() {
		return dataLayerConfiguration;
	}

	public ModelLayerConfiguration getModelLayerConfiguration() {
		return modelLayerConfiguration;
	}

	public AppLayerConfiguration getAppLayerConfiguration() {
		return appLayerConfiguration;
	}

	public FoldersLocator getFoldersLocator() {
		return foldersLocator;
	}

	public AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}

	public AppLayerFactory getUncachedAppLayerFactory() {
		return appLayerFactory;
	}
}
