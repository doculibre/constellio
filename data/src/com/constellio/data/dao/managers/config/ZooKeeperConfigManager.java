package com.constellio.data.dao.managers.config;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.events.ConfigDeletedEventListener;
import com.constellio.data.dao.managers.config.events.ConfigEventListener;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.managers.config.values.TextConfiguration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.events.Event;
import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusListener;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.KeyListMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.BadVersionException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.data.Stat;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class ZooKeeperConfigManager implements StatefulService, ConfigManager, EventBusListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperConfigManager.class);
	private static final String ROOT_FOLDER = "/constellio";
	private static final String CONFIG_FOLDER = "/conf";
	private static final String GET_BINARY_CONTENT = "ZooKeeperConfigManager-getBinaryContent";
	private static final String CONFIG_UPDATED_EVENT_TYPE = "configUpdated";

	private static volatile CuratorFramework CLIENT;

	private final KeyListMap<String, ConfigUpdatedEventListener> updatedConfigEventListeners = new KeyListMap<>();
	private final KeyListMap<String, ConfigDeletedEventListener> deletedConfigEventListeners = new KeyListMap<>();

	private String address;
	private String rootFolder;
	private IOServices ioServices;
	private ConfigManagerHelper configManagerHelper;
	private EventBus eventBus;

	public ZooKeeperConfigManager(String address, String rootFolder, IOServices ioServices, EventBus eventBus) {
		this.address = address;
		this.rootFolder = StringUtils.removeEnd(rootFolder, "/");
		this.ioServices = ioServices;
		this.configManagerHelper = new ConfigManagerHelper(this);
		this.eventBus = eventBus;
		this.eventBus.register(this);
		getInstance(address);
	}

	public static synchronized CuratorFramework getInstance(String address) {
		if (CLIENT == null) {
			try {
				RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
				CLIENT = CuratorFrameworkFactory.newClient(address, retryPolicy);
				CLIENT.start();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return CLIENT;
	}

	@Override
	public void initialize() {
	}

	@Override
	public void close() {
	}

	@Override
	public BinaryConfiguration getBinary(String path) {
		if (!exist(path, false)) {
			return null;
		}
		try {
			Stat stat = new Stat();
			String clientPath = getClientPath(path);
			byte[] ret = CLIENT.getData().storingStatIn(stat).forPath(clientPath);
			return new BinaryConfiguration("" + stat.getVersion(), ioServices.newByteArrayStreamFactory(ret, GET_BINARY_CONTENT));
		} catch (NoNodeException e) {
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public XMLConfiguration getXML(String path) {
		if (!exist(path, false)) {
			return null;
		}
		try {
			Stat stat = new Stat();
			String clientPath = getClientPath(path);
			byte[] ret = CLIENT.getData().storingStatIn(stat).forPath(clientPath);
			Document configuration = getDocumentFrom(ret);
			return new XMLConfiguration("" + stat.getVersion(), "" + stat.getVersion(), configuration);
		} catch (NoNodeException e) {
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public PropertiesConfiguration getProperties(String path) {
		if (!exist(path, false)) {
			return null;
		}
		try {
			Stat stat = new Stat();
			String clientPath = getClientPath(path);
			byte[] ret = CLIENT.getData().storingStatIn(stat).forPath(clientPath);
			Properties properties = new Properties();
			properties.load(new ByteArrayInputStream(ret));

			return new PropertiesConfiguration("" + stat.getVersion(), propertiesToMap(properties));
		} catch (NoNodeException e) {
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public TextConfiguration getText(String path) {
		if (!exist(path, false)) {
			return null;
		}
		try {
			Stat stat = new Stat();
			String clientPath = getClientPath(path);
			byte[] ret = CLIENT.getData().storingStatIn(stat).forPath(clientPath);
			return new TextConfiguration("" + stat.getVersion(), new String(ret));
		} catch (NoNodeException e) {
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean exist(String path) {
		return exist(path, true);
	}

	public boolean exist(String path, boolean callExtensions) {
		String clientPath = getClientPath(path);
		try {
			Stat stat = CLIENT.checkExists().forPath(clientPath);
			return stat != null;
		} catch (NoNodeException e) {
			return false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean folderExist(String path) {
		return exist(path, false);
	}

	@Override
	public List<String> list(String path) {
		String clientPath = getClientPath(path);
		try {
			List<String> children = CLIENT.getChildren().forPath(clientPath);
			return children;
		} catch (NoNodeException e) {
			return Collections.emptyList();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void updateXML(String path, DocumentAlteration documentAlteration) {
		configManagerHelper.updateXML(path, documentAlteration);
	}

	@Override
	public synchronized void updateProperties(String path, PropertiesAlteration propertiesAlteration) {
		configManagerHelper.updateProperties(path, propertiesAlteration);
	}

	@Override
	public void createXMLDocumentIfInexistent(String path, DocumentAlteration documentAlteration) {
		configManagerHelper.createXMLDocumentIfInexistent(path, documentAlteration);
	}

	@Override
	public void createPropertiesDocumentIfInexistent(String path, PropertiesAlteration propertiesAlteration) {
		configManagerHelper.createPropertiesDocumentIfInexistent(path, propertiesAlteration);
	}

	@Override
	public void add(String path, InputStream newBinaryStream) {
		String clientPath = getClientPath(path);
		try {
			byte[] bytes = IOUtils.toByteArray(newBinaryStream);
			CLIENT.create().creatingParentsIfNeeded().forPath(clientPath, bytes);
		} catch (KeeperException.NodeExistsException e) {
			throw new ConfigManagerRuntimeException.ConfigurationAlreadyExists(path);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void add(String path, Document newDocument) {
		String clientPath = getClientPath(path);
		try {
			byte[] bytes = getByteFromDocument(newDocument);
			CLIENT.create().creatingParentsIfNeeded().forPath(clientPath, bytes);
		} catch (KeeperException.NodeExistsException e) {
			throw new ConfigManagerRuntimeException.ConfigurationAlreadyExists(path);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void add(String path, Map<String, String> newProperties) {
		String clientPath = getClientPath(path);
		try {
			Properties prop = mapToProperties(newProperties);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			prop.store(output, null);
			CLIENT.create().creatingParentsIfNeeded().forPath(clientPath, output.toByteArray());
		} catch (KeeperException.NodeExistsException e) {
			throw new ConfigManagerRuntimeException.ConfigurationAlreadyExists(path);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void update(String path, String hash, InputStream newBinaryStream)
			throws OptimisticLockingConfiguration {
		String clientPath = getClientPath(path);
		try {
			byte[] bytes = IOUtils.toByteArray(newBinaryStream);
			CLIENT.setData().withVersion(Integer.parseInt(hash)).forPath(clientPath, bytes);
		} catch (BadVersionException e) {
			cacheRemoveAndCallListeners(path);
			throw new OptimisticLockingConfiguration(path, hash, "");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (ConfigUpdatedEventListener listener : updatedConfigEventListeners.get(path)) {
			listener.onConfigUpdated(path);
		}
		eventBus.send(CONFIG_UPDATED_EVENT_TYPE, path);
	}

	@Override
	public synchronized void update(String path, String hash, Document newDocument)
			throws OptimisticLockingConfiguration {
		String clientPath = getClientPath(path);
		try {
			byte[] bytes = getByteFromDocument(newDocument);
			CLIENT.setData().withVersion(Integer.parseInt(hash)).forPath(clientPath, bytes);
		} catch (BadVersionException e) {
			cacheRemoveAndCallListeners(path);
			throw new OptimisticLockingConfiguration(path, hash, "");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (ConfigUpdatedEventListener listener : updatedConfigEventListeners.get(path)) {
			listener.onConfigUpdated(path);
		}
		eventBus.send(CONFIG_UPDATED_EVENT_TYPE, path);
	}

	private void cacheRemoveAndCallListeners(String path) {
		//removeFromCache(path);

		for (ConfigUpdatedEventListener listener : updatedConfigEventListeners.get(path)) {
			listener.onConfigUpdated(path);
		}
	}

	@Override
	public synchronized void update(String path, String hash, Map<String, String> newProperties)
			throws OptimisticLockingConfiguration {
		String clientPath = getClientPath(path);
		try {
			Properties prop = mapToProperties(newProperties);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			prop.store(output, null);
			CLIENT.setData().withVersion(Integer.parseInt(hash)).forPath(clientPath, output.toByteArray());
		} catch (BadVersionException e) {
			cacheRemoveAndCallListeners(path);
			throw new OptimisticLockingConfiguration(path, hash, "");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (ConfigUpdatedEventListener listener : updatedConfigEventListeners.get(path)) {
			listener.onConfigUpdated(path);
		}
		eventBus.send(CONFIG_UPDATED_EVENT_TYPE, path);
	}

	@Override
	public void delete(String path) {
		String clientPath = getClientPath(path);
		try {
			CLIENT.delete().deletingChildrenIfNeeded().forPath(clientPath);
		} catch (NoNodeException nodeNode) {
			//Ignore
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		for (ConfigDeletedEventListener listener : deletedConfigEventListeners.get(path)) {
			listener.onConfigDeleted(path);
		}
	}

	@Override
	public void deleteFolder(String path) {
		delete(path);
	}

	@Override
	public void delete(String path, String hash)
			throws OptimisticLockingConfiguration {
		String clientPath = getClientPath(path);
		try {
			CLIENT.delete().deletingChildrenIfNeeded().withVersion(Integer.parseInt(hash)).forPath(clientPath);
		} catch (BadVersionException e) {
			cacheRemoveAndCallListeners(path);
			throw new OptimisticLockingConfiguration(path, hash, "");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		for (ConfigDeletedEventListener listener : deletedConfigEventListeners.get(path)) {
			listener.onConfigDeleted(path);
		}
	}

	@Override
	public void deleteAllConfigsIn(String collection) {
		String clientPath = getClientPath(collection + "/");
		try {
			CLIENT.delete().deletingChildrenIfNeeded().forPath(clientPath);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void copySettingsFrom(File setting) {
		throw new RuntimeException("Not supported operation copySettingsFrom existing folder");
	}

	@Override
	public void move(String src, String dest) {
		throw new RuntimeException("Not supported operation move");
	}

	@Override
	public void importFrom(File settingsFolder) {
		try {
			if (this.exist("/", false)) {
				this.delete("/");
			}
			Iterator<File> files = FileUtils.iterateFiles(settingsFolder, null, true);
			String settingsFolderPath = settingsFolder.getPath();
			while (files.hasNext()) {
				File file = files.next();
				if (file.isFile()) {
					String filePath = file.getPath();
					String relativePath = StringUtils.removeStart(filePath, settingsFolderPath);
					relativePath = StringUtils.replace(relativePath, "\\", "/");
					relativePath = StringUtils.removeStart(relativePath, "/");
					byte[] content;
					if (file.getName().equals("schemas.xml")) {
						content = fixVersionNumber(file);
					} else {
						content = FileUtils.readFileToByteArray(file);
					}
					this.add(relativePath, new ByteArrayInputStream(content));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private byte[] fixVersionNumber(File schemasXml)
			throws IOException {
		Document document = getDocumentFrom(FileUtils.readFileToByteArray(schemasXml));
		document.getRootElement().setAttribute("version", "0");
		return getByteFromDocument(document);
	}

	@Override
	public void exportTo(File settingsFolder) {
		this.exportTo("/", settingsFolder);
	}

	public void exportTo(String path, File file) {
		try {
			List<String> subPaths = this.list(path);
			if (!subPaths.isEmpty()) {
				if (!file.exists()) {
					file.mkdirs();
				}
				for (String subPath : subPaths) {
					String completeSubPath = path + "/" + subPath;
					completeSubPath = StringUtils.removeStart(completeSubPath, "/");
					this.exportTo(completeSubPath, new File(file, subPath));
				}
			} else {
				BinaryConfiguration binary = this.getBinary(path);
				if (binary != null) {
					try (InputStream is = binary.getInputStreamFactory().create(file.getName())) {
						FileUtils.writeByteArrayToFile(file, IOUtils.toByteArray(is));
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void registerListener(String path, ConfigEventListener listener) {
		if (listener instanceof ConfigUpdatedEventListener) {
			this.updatedConfigEventListeners.add(path, (ConfigUpdatedEventListener) listener);
		}
		if (listener instanceof ConfigDeletedEventListener) {
			this.deletedConfigEventListeners.add(path, (ConfigDeletedEventListener) listener);
		}
	}

	@Override
	public void registerTopPriorityListener(String path, ConfigEventListener listener) {
		if (listener instanceof ConfigUpdatedEventListener) {
			this.updatedConfigEventListeners.addAtStart(path, (ConfigUpdatedEventListener) listener);
		}
		if (listener instanceof ConfigDeletedEventListener) {
			this.deletedConfigEventListeners.add(path, (ConfigDeletedEventListener) listener);
		}
	}

	/*
	@Override
	public void registerListener(final String path, final ConfigEventListener listener) {
		String clientPath = getClientPath(path);
		if (listener instanceof ConfigUpdatedEventListener) {
			try {
				TreeCache nodeCache = nodeCaches.get(clientPath);
				if (nodeCache == null) {
					nodeCache = new TreeCache(client, clientPath);
					nodeCache.start();
					nodeCaches.put(clientPath, nodeCache);
				}
				nodeCache.getListenable().addListener(new ListenerDecorator(clientPath, (ConfigUpdatedEventListener) listener));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	*/

	Document getDocumentFrom(byte[] bytes) {
		SAXBuilder builder = new SAXBuilder();
		try {
			return builder.build(new ByteArrayInputStream(bytes));
		} catch (JDOMException e) {
			throw new ConfigManagerRuntimeException("JDOM2 Exception", e);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("build Document JDOM2 from file", e);
		}
	}

	byte[] getByteFromDocument(Document document) {
		String doc = new XMLOutputter().outputString(document);
		return doc.getBytes();
	}

	Map<String, String> propertiesToMap(Properties properties) {
		Map<String, String> mapOfProperties = new HashMap<>();

		Enumeration<?> enumeration = properties.propertyNames();
		while (enumeration.hasMoreElements()) {
			String key = (String) enumeration.nextElement();
			String value = properties.getProperty(key);
			mapOfProperties.put(key, value);
		}

		return mapOfProperties;
	}

	private Properties mapToProperties(Map<String, String> newProperties) {
		TreeMap<String, String> newPropertiesSorted = new TreeMap<>(newProperties);
		Properties properties = new Properties();
		properties.putAll(newPropertiesSorted);
		return properties;
	}

	private String getClientPath(String path) {
		if (!StringUtils.startsWith(path, "/")) {
			path = "/" + path;
		}
		String clientPath = ROOT_FOLDER + this.rootFolder + CONFIG_FOLDER + path;
		return StringUtils.removeEnd(clientPath, "/");
	}

	private static class ListenerDecorator implements TreeCacheListener {

		private String path;
		private ConfigUpdatedEventListener innerListener;

		public ListenerDecorator(String path, ConfigUpdatedEventListener innerListener) {
			this.path = path;
			this.innerListener = innerListener;
		}

		@Override
		public void childEvent(CuratorFramework client, TreeCacheEvent event)
				throws Exception {
			if (event.getType() == TreeCacheEvent.Type.NODE_UPDATED && event.getData().getPath().equals(path)) {
				innerListener.onConfigUpdated(path);
			}
		}
	}

	@Override
	public void keepInCache(String path) {
		//This config manager has no cache
	}

	@Override
	public void onEventReceived(Event event) {
		switch (event.getType()) {
			case CONFIG_UPDATED_EVENT_TYPE:
				String path = event.getData();
				for (ConfigUpdatedEventListener listener : updatedConfigEventListeners.get(path)) {
					listener.onConfigUpdated(path);
				}
				break;

			default:
				throw new ImpossibleRuntimeException("Unsupported event " + event.getType());
		}
	}

	@Override
	public void notifyChanged(String path) {
		for (ConfigUpdatedEventListener listener : updatedConfigEventListeners.get(path)) {
			listener.onConfigUpdated(path);
		}
	}
}
