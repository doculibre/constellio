package com.constellio.app.services.extensions.plugins;

import com.constellio.app.services.extensions.plugins.ConstellioPluginConfigurationManagerRuntimeException.ConstellioPluginConfigurationManagerRuntimeException_CouldNotDisableInvalidPlugin;
import com.constellio.app.services.extensions.plugins.ConstellioPluginConfigurationManagerRuntimeException.ConstellioPluginConfigurationManagerRuntimeException_CouldNotDisableReadyToInstallPlugin;
import com.constellio.app.services.extensions.plugins.ConstellioPluginConfigurationManagerRuntimeException.ConstellioPluginConfigurationManagerRuntimeException_CouldNotEnableInvalidPlugin;
import com.constellio.app.services.extensions.plugins.ConstellioPluginConfigurationManagerRuntimeException.ConstellioPluginConfigurationManagerRuntimeException_InvalidPluginWithNoStatus;
import com.constellio.app.services.extensions.plugins.ConstellioPluginConfigurationManagerRuntimeException.ConstellioPluginConfigurationManagerRuntimeException_NoSuchPlugin;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginInfo;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.utils.TenantUtils;
import com.constellio.data.utils.TimeProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.DISABLED;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.ENABLED;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.INVALID;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.READY_TO_INSTALL;

public class ConstellioPluginConfigurationManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioPluginConfigurationManager.class);

	public static final String PLUGINS_CONFIG_PATH = "/plugins.xml";
	public static final String STATUS_ATTRIBUTE = "status";
	public static final String TITLE = "title";
	public static final String LAST_VERSION_ATTRIBUTE = "lastVersion";
	public static final String LAST_VERSION_INSTALLATION_DATE = "lastVersionInstallDate";
	public static final String REQUIRED_CONSTELLIO_VERSION = "requiredConstellioVersion";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	private static final String FAILURE_CAUSE = "failureCause";
	private static final String STACK_TRACE = "stackTrace";
	private final ConfigManager configManager;

	public ConstellioPluginConfigurationManager(DataLayerFactory dataLayerFactory) {
		this.configManager = dataLayerFactory.getConfigManager();
		this.configManager.keepInCache(PLUGINS_CONFIG_PATH);
	}

	public List<String> getActivePluginsIds() {
		List<String> activePluginIds = getPluginsWithStatus(ENABLED);
		LOGGER.warn("Active plugin ids of tenant '" + TenantUtils.getTenantId() + "' : " + activePluginIds, new Exception());
		return activePluginIds;
	}

	private List<String> getPluginsWithStatus(ConstellioPluginStatus status) {
		List<String> selectedPluginsIds = new ArrayList<>();
		if (status != null) {
			XMLConfiguration xmlConfig = readPlugins();
			for (Element pluginElement : xmlConfig.getDocument().getRootElement().getChildren()) {
				ConstellioPluginInfo pluginInfo = populateInfoFromElement(pluginElement);
				if (pluginInfo.getPluginStatus() != null && pluginInfo.getPluginStatus().equals(status)) {
					selectedPluginsIds.add(pluginInfo.getCode());
				}
			}
		}
		return selectedPluginsIds;
	}

	public void markPluginAsEnabled(String pluginId)
			throws ConstellioPluginConfigurationManagerRuntimeException {
		LOGGER.warn("marking plugin '" + pluginId + " ' as enabled for tenant " + TenantUtils.getTenantId(), new Exception());
		ConstellioPluginStatus status = prValidateModule(pluginId);
		switch (status) {
			case INVALID:
				throw new ConstellioPluginConfigurationManagerRuntimeException_CouldNotEnableInvalidPlugin(pluginId);
			case READY_TO_INSTALL:
			case DISABLED:
				break;
			case ENABLED:
				return;
			default:
				throw new RuntimeException("Unsupported status " + status);
		}
		setPluginAttributeValue(pluginId, STATUS_ATTRIBUTE, ENABLED.toString());
		setPluginAttributeValue(pluginId, STACK_TRACE, "");
	}

	private ConstellioPluginStatus prValidateModule(String pluginId)
			throws ConstellioPluginConfigurationManagerRuntimeException {
		ConstellioPluginInfo pluginInfo = getPluginInfo(pluginId);
		if (pluginInfo == null) {
			throw new ConstellioPluginConfigurationManagerRuntimeException_NoSuchPlugin(pluginId);
		}
		ConstellioPluginStatus status = pluginInfo.getPluginStatus();
		if (status == null) {
			throw new ConstellioPluginConfigurationManagerRuntimeException_InvalidPluginWithNoStatus(pluginId);
		}
		return status;
	}

	public void markPluginAsDisabled(String pluginId)
			throws ConstellioPluginConfigurationManagerRuntimeException {
		LOGGER.warn("marking plugin '" + pluginId + " ' as disabled for tenant " + TenantUtils.getTenantId(), new Exception());
		ConstellioPluginStatus status = prValidateModule(pluginId);
		switch (status) {
			case INVALID:
				throw new ConstellioPluginConfigurationManagerRuntimeException_CouldNotDisableInvalidPlugin(pluginId);
			case READY_TO_INSTALL:
				throw new ConstellioPluginConfigurationManagerRuntimeException_CouldNotDisableReadyToInstallPlugin(pluginId);
			case ENABLED:
				break;
			case DISABLED:
				return;
			default:
				throw new RuntimeException("Unsupported status " + status);
		}

		setPluginAttributeValue(pluginId, STATUS_ATTRIBUTE, DISABLED.toString());
	}

	private void setPluginAttributeValue(final String pluginId, final String attributeName,
										 final String attributeValue) {
		configManager.updateXML(PLUGINS_CONFIG_PATH, new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				Element pluginElement = document.getRootElement().getChild(pluginId);
				if (pluginElement == null) {
					throw new RuntimeException("Invalid plugin id " + pluginId);
				}
				pluginElement.setAttribute(attributeName, attributeValue);
			}
		});
	}

	public ConstellioPluginInfo getPluginInfo(String pluginId) {
		XMLConfiguration xmlConfig = readPlugins();
		Element pluginElement = xmlConfig.getDocument().getRootElement().getChild(pluginId);
		if (pluginElement == null) {
			return null;
		} else {
			return populateInfoFromElement(pluginElement);
		}
	}

	public void installPlugin(String pluginId, String pluginTitle, String version, String requiredConstellioVersion) {
		//LOGGER.info("Detected plugin : " + pluginId + "-" + version + " (" + pluginTitle + ")");
		LOGGER.warn("installing plugin '" + pluginId + " ' for tenant " + TenantUtils.getTenantId(), new Exception());
		final ConstellioPluginInfo pluginInfo = new ConstellioPluginInfo()
				.setLastInstallDate(TimeProvider.getLocalDate())
				.setPluginStatus(READY_TO_INSTALL)
				.setCode(pluginId)
				.setTitle(pluginTitle)
				.setRequiredConstellioVersion(requiredConstellioVersion)
				.setVersion(version);
		addOrUpdatePlugin(pluginInfo);
	}

	void addOrUpdatePlugin(final ConstellioPluginInfo pluginInfo) {
		if (pluginInfo != null && StringUtils.isNotBlank(pluginInfo.getCode())) {
			String id = pluginInfo.getCode();
			configManager.updateXML(PLUGINS_CONFIG_PATH, new DocumentAlteration() {
				@Override
				public void alter(Document document) {
					Element pluginInfoElement = document.getRootElement().getChild(pluginInfo.getCode());
					if (pluginInfoElement == null) {
						pluginInfoElement = new Element(pluginInfo.getCode());
					} else {
						document.getRootElement().removeChild(pluginInfo.getCode());
					}
					pluginInfoElement = populateElementFromInfo(pluginInfoElement, pluginInfo);
					document.getRootElement().addContent(pluginInfoElement);
				}
			});
		}
	}

	private Element populateElementFromInfo(Element pluginInfoElement, ConstellioPluginInfo pluginInfo) {
		pluginInfoElement = setAttributeValue(pluginInfoElement, STATUS_ATTRIBUTE, pluginInfo.getPluginStatus());
		pluginInfoElement = setAttributeValue(pluginInfoElement, TITLE, pluginInfo.getTitle());
		pluginInfoElement = setAttributeValue(pluginInfoElement, LAST_VERSION_ATTRIBUTE, pluginInfo.getVersion());
		pluginInfoElement = setAttributeValue(pluginInfoElement, REQUIRED_CONSTELLIO_VERSION,
				pluginInfo.getRequiredConstellioVersion());
		pluginInfoElement = setAttributeValue(pluginInfoElement, LAST_VERSION_INSTALLATION_DATE, pluginInfo.getLastInstallDate());
		pluginInfoElement = setAttributeValue(pluginInfoElement, FAILURE_CAUSE, pluginInfo.getPluginActivationFailureCause());
		pluginInfoElement = setAttributeValue(pluginInfoElement, STACK_TRACE, pluginInfo.getStackTrace());
		return pluginInfoElement;
	}

	private ConstellioPluginInfo populateInfoFromElement(Element pluginElement) {
		String statusAsString = pluginElement.getAttributeValue(STATUS_ATTRIBUTE);
		ConstellioPluginStatus pluginStatus = null;
		if (StringUtils.isNotBlank(statusAsString)) {
			pluginStatus = ConstellioPluginStatus.valueOf(statusAsString);
		}

		String version = pluginElement.getAttributeValue(LAST_VERSION_ATTRIBUTE);
		if (StringUtils.isBlank(version)) {
			version = null;
		}

		String constellioVersion = pluginElement.getAttributeValue(REQUIRED_CONSTELLIO_VERSION);
		if (StringUtils.isBlank(constellioVersion)) {
			constellioVersion = null;
		}

		String title = pluginElement.getAttributeValue(TITLE);

		String installationDateAsString = pluginElement.getAttributeValue(LAST_VERSION_INSTALLATION_DATE);
		LocalDate lastInstallDate = null;
		if (StringUtils.isNotBlank(installationDateAsString)) {
			lastInstallDate = LocalDate.parse(installationDateAsString);
		}

		PluginActivationFailureCause cause = null;
		String causeAsString = pluginElement.getAttributeValue(FAILURE_CAUSE);
		if (StringUtils.isNotBlank(causeAsString)) {
			cause = PluginActivationFailureCause.valueOf(causeAsString);
		}
		String stackTrace = pluginElement.getAttributeValue(STACK_TRACE);
		if (StringUtils.isBlank(stackTrace)) {
			stackTrace = null;
		}

		String code = pluginElement.getName();
		if (StringUtils.isBlank(title)) {
			title = code;
		}

		return new ConstellioPluginInfo().setCode(code).setTitle(title).setPluginStatus(pluginStatus).setVersion(version)
				.setRequiredConstellioVersion(constellioVersion).setLastInstallDate(lastInstallDate)
				.setPluginActivationFailureCause(cause).setStackTrace(stackTrace);
	}

	private Element setAttributeValue(Element element, String attributeName, Object value) {
		if (value != null && StringUtils.isNotBlank(value.toString())) {
			element.setAttribute(attributeName, value.toString());
		} else {
			element.setAttribute(attributeName, "");
		}
		return element;
	}

	public void createConfigFileIfNotExist() {
		if (!configManager.exist(PLUGINS_CONFIG_PATH)) {
			configManager.add(PLUGINS_CONFIG_PATH, new Document().setRootElement(new Element("plugins")));
		}
	}

	List<ConstellioPluginInfo> getPlugins(ConstellioPluginStatus status) {
		List<ConstellioPluginInfo> returnList = new ArrayList<>();
		if (status != null) {
			XMLConfiguration xmlConfig = readPlugins();
			for (Element pluginInfoElement : xmlConfig.getDocument().getRootElement().getChildren()) {
				ConstellioPluginInfo pluginInfo = populateInfoFromElement(pluginInfoElement);
				if (pluginInfo.getPluginStatus() != null && pluginInfo.getPluginStatus().equals(status)) {
					returnList.add(pluginInfo);
				}
			}
		}
		return returnList;
	}

	void invalidateModule(final String pluginId, final PluginActivationFailureCause cause, final Throwable throwable) {
		LOGGER.warn("invalidate module'" + pluginId + " ' for tenant " + TenantUtils.getTenantId(), new Exception());
		if (pluginId != null) {
			configManager.updateXML(PLUGINS_CONFIG_PATH, new DocumentAlteration() {
				@Override
				public void alter(Document document) {
					Element pluginElement = document.getRootElement().getChild(pluginId);
					if (pluginElement == null) {
						throw new RuntimeException("Could not enable plugin '" + pluginId + "'", throwable);
					}
					pluginElement.setAttribute(STATUS_ATTRIBUTE, INVALID.toString());
					if (cause != null) {
						String stackTrace = "";
						if (throwable != null) {
							stackTrace = stackTraceToString(throwable);
						}
						pluginElement.setAttribute(FAILURE_CAUSE, cause.toString());
						pluginElement.setAttribute(STACK_TRACE, stackTrace);
					} else {
						pluginElement.setAttribute(FAILURE_CAUSE, "");
						pluginElement.setAttribute(STACK_TRACE, "");
					}
				}
			});
		}
	}

	private String stackTraceToString(Throwable throwable) {
		return ExceptionUtils.getStackTrace(throwable);
		//		StringBuilder sb = new StringBuilder();
		//		for (StackTraceElement element : throwable.getStackTrace()) {
		//			sb.append(element.toString());
		//			sb.append("\n");
		//		}
		//		sb.append(throwable.getMessage());
		//		sb.append("\n");
		//		sb.append(throwable.getLocalizedMessage());
		//		sb.append("\n");
		//		return sb.toString();
	}

	public void removePlugin(final String pluginId) {
		if (pluginId != null) {
			configManager.updateXML(PLUGINS_CONFIG_PATH, new DocumentAlteration() {
				@Override
				public void alter(Document document) {
					Element rootElement = document.getRootElement();
					rootElement.removeChild(pluginId);
				}
			});
		}
	}

	public List<String> getAllPluginsCodes() {
		List<String> returnList = new ArrayList<>();
		XMLConfiguration xmlConfig = readPlugins();
		for (Element pluginInfoElement : xmlConfig.getDocument().getRootElement().getChildren()) {
			returnList.add(pluginInfoElement.getName());
		}
		return returnList;
	}

	private XMLConfiguration readPlugins() {
		XMLConfiguration configuration = configManager.getXML(PLUGINS_CONFIG_PATH);

		return configuration;
	}
}
