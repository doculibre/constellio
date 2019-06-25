package com.constellio.model.services.configs;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.io.streamFactories.services.one.StreamOperation;
import com.constellio.data.utils.Delayed;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.batchprocess.RecordBatchProcess;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.entities.configs.SystemConfigurationScript;
import com.constellio.model.entities.configs.SystemConfigurationType;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.modules.PluginUtil;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.batch.actions.ReindexMetadatasBatchProcessAction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.configs.SystemConfigurationsManagerRuntimeException.SystemConfigurationsManagerRuntimeException_InvalidConfigValue;
import com.constellio.model.services.configs.SystemConfigurationsManagerRuntimeException.SystemConfigurationsManagerRuntimeException_UpdateScriptFailed;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.utils.InstanciationUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class SystemConfigurationsManager implements StatefulService, ConfigUpdatedEventListener {

	private static Logger LOGGER = LoggerFactory.getLogger(SystemConfigurationsManager.class);

	IOServices ioServices;

	ModelLayerFactory modelLayerFactory;

	static final String CONFIG_FILE_PATH = "/systemConfigs.properties";
	ConfigManager configManager;

	Delayed<ConstellioModulesManager> constellioModulesManagerDelayed;

	ConstellioCache cache2;

	//boolean readPropertiesFileRequired = true;

	public SystemConfigurationsManager(ModelLayerFactory modelLayerFactory, ConfigManager configManager,
									   Delayed<ConstellioModulesManager> constellioModulesManagerDelayed,
									   ConstellioCacheManager cacheManager) {
		this.modelLayerFactory = modelLayerFactory;
		this.configManager = configManager;
		this.constellioModulesManagerDelayed = constellioModulesManagerDelayed;
		this.configManager.registerListener(CONFIG_FILE_PATH, this);
		this.ioServices = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices();
		this.cache2 = cacheManager.getCache(getClass().getName());
	}

	@Override
	public void initialize() {
		clearCache();
		configManager.createPropertiesDocumentIfInexistent(CONFIG_FILE_PATH, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
			}
		});
	}

	private synchronized void clearCache() {
		cache2.clear();
		//		readPropertiesFileRequired = true;
	}

	@Override
	public void close() {
		clearCache();
	}

	public boolean signalDefaultValueModification(final SystemConfiguration config, final Object previousDefaultValue) {
		String propertyKey = config.getPropertyKey();
		Object currentValue = toObject(config, configManager.getProperties(CONFIG_FILE_PATH).getProperties().get(propertyKey));
		if (currentValue == null) {
			return reindex(config, config.getDefaultValue(), previousDefaultValue);
		}
		return false;
	}

	public boolean setValue(final SystemConfiguration config, final Object newValue) {
		try {
			if (config.getType() == SystemConfigurationType.BINARY) {
				StreamFactory<InputStream> streamFactory = (StreamFactory<InputStream>) newValue;
				final String configPath = "/systemConfigs/" + config.getCode();
				if (configManager.exist(configPath)) {
					if (streamFactory == null) {
						configManager.delete(configPath);
					} else {

						try {
							ioServices.execute(new StreamOperation<InputStream>() {
								@Override
								public void execute(InputStream stream) {
									String hash = configManager.getBinary(configPath).getHash();
									try {
										configManager.update(configPath, hash, stream);
									} catch (OptimisticLockingConfiguration e) {
										throw new ImpossibleRuntimeException(e);
									}
								}
							}, streamFactory);
						} catch (IOException e) {
							LOGGER.error("error when saving stream", e);
							throw new SystemConfigurationsManagerRuntimeException_InvalidConfigValue(config.getCode(), "");
						}

					}
				} else {
					if (streamFactory != null) {
						try {
							ioServices.execute(new StreamOperation<InputStream>() {
								@Override
								public void execute(InputStream stream) {
									configManager.add(configPath, stream);
								}
							}, streamFactory);
						} catch (IOException e) {
							LOGGER.error("error when saving stream", e);
							throw new SystemConfigurationsManagerRuntimeException_InvalidConfigValue(config.getCode(), "");
						}

					}
				}

			} else {

				final Object oldValue = getValue(config);

				ValidationErrors errors = new ValidationErrors();
				validate(config, newValue, errors);
				if (!errors.getValidationErrors().isEmpty()) {
					throw new SystemConfigurationsManagerRuntimeException_InvalidConfigValue(config.getCode(), newValue);
				}
				if (config.equals(ConstellioEIMConfigs.IN_UPDATE_PROCESS)) {
					configManager.updateProperties(CONFIG_FILE_PATH, updateConfigValueAlteration(config, newValue));
				} else {
					return reindex(config, newValue, oldValue);
				}
			}
		} finally {
			clearCache();
		}
		return false;
	}

	public boolean doesSetValueRequireReindexing(final SystemConfiguration config, final Object newValue) {
		if (config.getType() != SystemConfigurationType.BINARY) {
			final Object oldValue = getValue(config);

			ValidationErrors errors = new ValidationErrors();
			validate(config, newValue, errors);
			if (!errors.getValidationErrors().isEmpty()) {
				throw new SystemConfigurationsManagerRuntimeException_InvalidConfigValue(config.getCode(), newValue);
			}
			if (config.equals(ConstellioEIMConfigs.IN_UPDATE_PROCESS)) {
			} else {
				return doesReindexNeedToSetFlag(config, newValue, oldValue);
			}
		}
		return false;
	}

	private boolean doesReindexNeedToSetFlag(SystemConfiguration config, Object newValue, Object oldValue) {
		BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		SystemConfigurationScript<Object> listener = getInstanciatedScriptFor(config);
		List<String> collections = modelLayerFactory.getCollectionsListManager().getCollections();
		ConstellioModulesManager modulesManager = constellioModulesManagerDelayed.get();
		Module module = config.getModule() == null ? null : modulesManager.getInstalledModule(config.getModule());

		List<RecordBatchProcess> batchProcesses = startBatchProcessesToReindex(config);
		int totalRecordsToReindex = 0;
		for (RecordBatchProcess process : batchProcesses) {
			totalRecordsToReindex += process.getTotalRecordsCount();
		}
		return totalRecordsToReindex > 10000;
	}

	private boolean reindex(SystemConfiguration config, Object newValue, Object oldValue) {
		BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		SystemConfigurationScript<Object> listener = getInstanciatedScriptFor(config);
		List<String> collections = modelLayerFactory.getCollectionsListManager().getCollections();
		ConstellioModulesManager modulesManager = constellioModulesManagerDelayed.get();
		Module module = config.getModule() == null ? null : modulesManager.getInstalledModule(config.getModule());

		List<RecordBatchProcess> batchProcesses = startBatchProcessesToReindex(config);
		int totalRecordsToReindex = 0;
		for (RecordBatchProcess process : batchProcesses) {
			totalRecordsToReindex += process.getTotalRecordsCount();
		}
		try {

			if (listener != null) {
				listener.onValueChanged(oldValue, newValue, modelLayerFactory);

				for (String collection : collections) {
					if (module == null || modulesManager.isModuleEnabled(collection, module)) {
						listener.onValueChanged(oldValue, newValue, modelLayerFactory, collection);
					}
				}
			}

			configManager.updateProperties(CONFIG_FILE_PATH, updateConfigValueAlteration(config, newValue));

			boolean reindex = totalRecordsToReindex > 10000 || (config.isRequireReIndexing() && totalRecordsToReindex > 0);

			if (reindex) {
				for (BatchProcess batchProcess : batchProcesses) {
					batchProcessesManager.cancelStandByBatchProcess(batchProcess);
				}
			} else {
				for (BatchProcess batchProcess : batchProcesses) {
					batchProcessesManager.markAsPending(batchProcess);
				}
			}

			return reindex;
		} catch (RuntimeException e) {
			LOGGER.warn("Failed to execute script of system config '" + config.getCode() + "'", e);
			if (listener != null) {
				listener.onValueChanged(newValue, oldValue, modelLayerFactory);

				for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
					if (module == null || modulesManager.isModuleEnabled(collection, module)) {
						listener.onValueChanged(newValue, oldValue, modelLayerFactory, collection);
					}
				}
			}
			for (BatchProcess batchProcess : batchProcesses) {
				batchProcessesManager.cancelStandByBatchProcess(batchProcess);
			}

			throw new SystemConfigurationsManagerRuntimeException_UpdateScriptFailed(config.getCode(), newValue, e);
		}

	}

	private PropertiesAlteration updateConfigValueAlteration(final SystemConfiguration config, final Object newValue) {
		return new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				if (LangUtils.isEqual(newValue, config.getDefaultValue())) {
					properties.remove(config.getPropertyKey());
				} else {
					properties.put(config.getPropertyKey(), SystemConfigurationsManager.this.toString(config, newValue));
				}
			}
		};
	}

	List<RecordBatchProcess> startBatchProcessesToReindex(SystemConfiguration config) {
		List<RecordBatchProcess> batchProcesses = new ArrayList<>();
		for (String collection : modelLayerFactory.getCollectionsListManager().getCollectionsExcludingSystem()) {
			MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			for (String typeCode : types.getSchemaTypesSortedByDependency()) {
				MetadataSchemaType type = types.getSchemaType(typeCode);
				List<Metadata> metadatasToReindex = findMetadatasToReindex(type, config);
				if (!metadatasToReindex.isEmpty()) {
					batchProcesses.addAll(startBatchProcessesToReindex(metadatasToReindex, type, config));
				}
			}
		}
		return batchProcesses;
	}

	List<RecordBatchProcess> startBatchProcessesToReindex(List<Metadata> metadatasToReindex, MetadataSchemaType type,
														  SystemConfiguration config) {

		List<RecordBatchProcess> batchProcesses = new ArrayList<>();
		BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		List<String> schemaCodes = new SchemaUtils().toMetadataCodes(metadatasToReindex);
		LogicalSearchCondition condition = from(type).returnAll();
		if (searchServices.hasResults(condition)) {
			BatchProcessAction action = new ReindexMetadatasBatchProcessAction(schemaCodes);
			batchProcesses.add(batchProcessesManager
					.addBatchProcessInStandby(condition, action, "reindex.config " + config.getCode()));
		}
		return batchProcesses;
	}

	private List<Metadata> findMetadatasToReindex(MetadataSchemaType schemaType,
												  SystemConfiguration systemConfiguration) {
		Set<Metadata> reindexedMetadatas = new HashSet<>();
		for (Metadata metadata : schemaType.getCalculatedMetadatas()) {
			for (Dependency dependency : ((CalculatedDataEntry) metadata.getDataEntry()).getCalculator().getDependencies()) {
				if (dependency instanceof ConfigDependency) {
					ConfigDependency configDependency = (ConfigDependency) dependency;
					if (configDependency.getConfiguration().equals(systemConfiguration)) {
						reindexedMetadatas.add(metadata);
					}
				}
			}
		}
		return new ArrayList<>(reindexedMetadatas);
	}

	public void validate(SystemConfiguration config, Object newValue, ValidationErrors errors) {
		SystemConfigurationScript<Object> listener = getInstanciatedScriptFor(config);
		if (listener != null) {
			listener.validate(newValue, errors);
		}
	}

	public void reset(final SystemConfiguration config) {
		setValue(config, config.getDefaultValue());

		configManager.updateProperties(CONFIG_FILE_PATH, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.remove(config.getPropertyKey());
			}
		});

	}

	private String toString(SystemConfiguration config, Object value) {
		if (value == null) {
			return null;
		}
		switch (config.getType()) {

			case STRING:
				return value.toString();
			case BOOLEAN:
				return ((Boolean) value) ? "true" : "false";
			case INTEGER:
				return "" + value;
			case ENUM:
				return ((Enum<?>) value).name();
		}
		throw new ImpossibleRuntimeException("Unsupported config type : " + config.getType());
	}

	private Object toObject(SystemConfiguration config, String value) {
		if (value == null) {
			return null;
		}
		switch (config.getType()) {

			case STRING:
				return value;
			case BOOLEAN:
				return "true".equals(value);
			case INTEGER:
				return Integer.valueOf(value);
			case ENUM:
				return EnumUtils.getEnum((Class) config.getEnumClass(), value);
		}
		throw new ImpossibleRuntimeException("Unsupported config type : " + config.getType());
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(SystemConfiguration config) {
		T value;
		if (config.getType() == SystemConfigurationType.BINARY) {
			String configKey = "/systemConfigs/" + config.getCode();
			byte[] binaryContentFromCache = cache2.get(configKey);
			StreamFactory<InputStream> inputStreamFactory;
			if (binaryContentFromCache == null) {
				BinaryConfiguration binaryConfiguration = configManager.getBinary(configKey);
				if (binaryConfiguration != null) {
					inputStreamFactory = binaryConfiguration.getInputStreamFactory();
					try (InputStream in = inputStreamFactory.create(configKey + ".loadingInCache")) {
						byte[] binaryContent = IOUtils.toByteArray(in);
						cache2.put(configKey, binaryContent, InsertionReason.WAS_OBTAINED);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				} else {
					cache2.put(configKey, new byte[0], InsertionReason.WAS_OBTAINED);
					inputStreamFactory = null;
				}

			} else if (binaryContentFromCache.length == 0) {
				inputStreamFactory = null;

			} else {
				inputStreamFactory = ioServices
						.newByteArrayStreamFactory(binaryContentFromCache, getClass().getName() + "." + configKey);
			}

			value = (T) inputStreamFactory;

		} else {
			String propertyKey = config.getPropertyKey();
			String valueFromCache = getPropertiesUsingCache().get(propertyKey);
			if (valueFromCache != null) {
				value = (T) toObject(config, valueFromCache);
			} else {
				value = (T) config.getDefaultValue();
			}
		}
		return value;
	}

	Map<String, String> getPropertiesUsingCache() {
		Map<String, String> properties = cache2.get("properties");

		if (properties == null) {
			PropertiesConfiguration propertiesConfig = configManager.getProperties(CONFIG_FILE_PATH);
			if (propertiesConfig != null) {
				properties = propertiesConfig.getProperties();
				cache2.put("properties", (Serializable) properties, InsertionReason.WAS_OBTAINED);
			} else {
				cache2.put("properties", new HashMap<>(), InsertionReason.WAS_OBTAINED);
			}
		}

		return properties;
	}

	@Override
	public void onConfigUpdated(String configPath) {
		clearCache();
	}

	public SystemConfigurationScript<Object> getInstanciatedScriptFor(SystemConfiguration config) {
		if (config.getScriptClass() != null) {
			return new InstanciationUtils()
					.instanciateWithoutExpectableExceptions(config.getScriptClass());
		} else {
			return null;
		}
	}

	public List<SystemConfigurationGroup> getConfigurationGroups() {

		List<SystemConfigurationGroup> groups = new ArrayList<>();
		for (SystemConfiguration config : getAllConfigurations()) {
			SystemConfigurationGroup group = new SystemConfigurationGroup(config.getModule(), config.getConfigGroupCode());
			if (!groups.contains(group)) {
				groups.add(group);
			}
		}

		return groups;
	}

	public List<SystemConfiguration> getAllConfigurations() {
		List<SystemConfiguration> configurations = new ArrayList<>();
		configurations.addAll(ConstellioEIMConfigs.getCoreConfigs());
		for (Module module : constellioModulesManagerDelayed.get().getInstalledModules()) {
			configurations.addAll(PluginUtil.getConfigurations(module));
		}

		return configurations;
	}

	public SystemConfiguration getConfigurationWithCode(String code) {

		for (SystemConfiguration config : getAllConfigurations()) {
			if (config.getCode().equals(code)) {
				return config;
			}

		}

		return null;
	}

	public List<SystemConfiguration> getGroupConfigurations(SystemConfigurationGroup wantedGroup) {
		List<SystemConfiguration> configs = new ArrayList<>();
		for (SystemConfiguration config : getAllConfigurations()) {
			SystemConfigurationGroup group = new SystemConfigurationGroup(config.getModule(), config.getConfigGroupCode());
			if (group.equals(wantedGroup)) {
				configs.add(config);
			}
		}

		return configs;
	}

	public List<SystemConfiguration> getGroupConfigurationsWithCode(String code) {
		List<SystemConfiguration> configs = new ArrayList<>();
		for (SystemConfiguration config : getAllConfigurations()) {
			SystemConfigurationGroup group = new SystemConfigurationGroup(config.getModule(), config.getConfigGroupCode());
			if (group.getCode().equals(code)) {
				configs.add(config);
			}
		}

		Collections.sort(configs, new Comparator<SystemConfiguration>() {
			@Override
			public int compare(SystemConfiguration o1, SystemConfiguration o2) {
				return o1.getCode().compareTo(o2.getCode());
			}
		});

		return configs;
	}

	public List<SystemConfiguration> getNonHiddenGroupConfigurationsWithCodeOrderedByName(String groupCode,
																						  boolean showHidden) {
		List<SystemConfiguration> nonHidden = new ArrayList<>();
		for (SystemConfiguration config : getAllConfigurations()) {
			SystemConfigurationGroup group = new SystemConfigurationGroup(config.getModule(), config.getConfigGroupCode());
			if (group.getCode().equals(groupCode)) {
				if (showHidden || !config.isHidden()) {
					nonHidden.add(config);
				}
			}
		}

		Collections.sort(nonHidden, new Comparator<SystemConfiguration>() {
			@Override
			public int compare(SystemConfiguration o1, SystemConfiguration o2) {
				return o1.getCode().compareTo(o2.getCode());
			}
		});

		return nonHidden;
	}

	public File getFileFromValue(SystemConfiguration config, String fileNameToReturn) {
		StreamFactory<InputStream> streamFactory = this.getValue(config);
		InputStream returnStream = null;
		if (streamFactory != null) {
			try {
				returnStream = streamFactory.create(fileNameToReturn);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		if (returnStream == null) {
			return null;
		}

		File file = new File(fileNameToReturn);
		try {
			FileUtils.copyInputStreamToFile(returnStream, file);
			//TODO Francis file created by resource is not removed from file system
			modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices().closeQuietly(returnStream);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			IOUtils.closeQuietly(returnStream);
		}
		return file;
	}
}
