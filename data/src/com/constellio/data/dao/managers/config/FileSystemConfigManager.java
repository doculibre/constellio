package com.constellio.data.dao.managers.config;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException.ConfigurationAlreadyExists;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException.NoSuchConfiguration;
import com.constellio.data.dao.managers.config.events.ConfigDeletedEventListener;
import com.constellio.data.dao.managers.config.events.ConfigEventListener;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.managers.config.values.TextConfiguration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.events.Event;
import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusListener;
import com.constellio.data.extensions.DataLayerExtensions;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.KeyListMap;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.data.utils.TenantUtils;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

public class FileSystemConfigManager implements StatefulService, EventBusListener, ConfigManager {

	static final String ADD_BINARY_FILE = "FileSystemConfigManager-AddBinaryFile";
	static final String UPDATE_BINARY_FILE = "FileSystemConfigManager-UpdateBinaryFile";
	static final String READ_BINARY_FILE = "FileSystemConfigManager-ReadBinaryFile";

	static final String ADD_PROPERTIES_FILE = "FileSystemConfigManager-AddPropertiesFile";
	static final String UPDATE_PROPERTIES_FILE = "FileSystemConfigManager-UpdatePropertiesFile";
	static final String READ_PROPERTIES_FILE = "FileSystemConfigManager-ReadPropertiesFile";
	final String READ_PROPERTIES_FILE_2 = "FileSystemConfigManager-ReadPropertiesFile2";

	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemConfigManager.class);

	private static final String NO_VERSION = "-1";
	private final File configFolder;
	private final HashingService hashService;
	private final IOServices ioServices;
	private final ConstellioCache cache;
	private DataLayerExtensions extensions;
	ConfigManagerHelper configManagerHelper;

	private static final String CONFIG_UPDATED_EVENT_TYPE = "configUpdated";
	EventBus eventBus;

	//	private final Map<String, Object> cache = new HashMap<>();

	private final KeyListMap<String, ConfigUpdatedEventListener> updatedConfigEventListeners = new KeyListMap<>();
	private final KeyListMap<String, ConfigDeletedEventListener> deletedConfigEventListeners = new KeyListMap<>();

	public FileSystemConfigManager(File configFolder, IOServices ioServices, HashingService hashService,
								   ConstellioCache cache,
								   DataLayerExtensions extensions, EventBus eventBus) {
		super();
		this.configFolder = configFolder;
		this.ioServices = ioServices;
		this.hashService = hashService;
		this.cache = cache;
		this.extensions = extensions;
		this.configManagerHelper = new ConfigManagerHelper(this);
		this.eventBus = eventBus;
		this.eventBus.register(this);
	}

	@Override
	public void initialize() {
	}

	Object getFromCache(String key) {
		return cache.get(key);
	}

	void putInCache(String key, Object value) {
		//cache.put(key, (Serializable) value);
	}

	void removeFromCache(String key) {
		cache.remove(key);
	}

	void clearCache() {
		cache.clear();
	}

	Set<String> getCacheKeys() {
		Set<String> cacheKeys = new HashSet<>();
		for (Iterator<String> it = cache.keySet(); it.hasNext(); ) {
			String cacheKey = (String) it.next();
			cacheKeys.add(cacheKey);
		}
		return cacheKeys;
	}

	@Override
	public synchronized void add(String path, Document newDocument) {
		extensions.getSystemWideExtensions().onAddUpdateConfig(path);
		validateFileNonExistance(path);
		LOGGER.debug("add document => " + path);
		String content = getContentOfDocument(newDocument);

		try {
			ioServices.replaceFileContent(new File(configFolder, path), content);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("replace file content", e);
		}
	}

	@Override
	public synchronized void add(String path, InputStream newBinaryStream) {
		extensions.getSystemWideExtensions().onAddUpdateConfig(path);
		validateFileNonExistance(path);
		LOGGER.debug("add inputstream => " + path);

		File binFile = createFoldersAndFiles(path);

		try {
			ioServices.replaceFileContent(binFile, newBinaryStream);

		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("copy binary to file", e);
		}
	}

	File createFoldersAndFiles(String path) {
		LOGGER.debug("create folder => " + path);
		File file = new File(configFolder, path);
		file.getParentFile().mkdirs();
		return file;
	}

	@Override
	public synchronized void add(String path, Map<String, String> newProperties) {
		extensions.getSystemWideExtensions().onAddUpdateConfig(path);
		validateFileNonExistance(path);
		LOGGER.debug("add properties => " + path);

		File propertiesFile = createFoldersAndFiles(path);
		File tempPropertiesFile = ioServices.getAtomicWriteTempFileFor(propertiesFile);

		Properties properties = mapToProperties(newProperties);

		OutputStream outputStream = null;
		try {
			outputStream = ioServices.newFileOutputStream(tempPropertiesFile, ADD_PROPERTIES_FILE);
			PropertyFileUtils.store(properties, outputStream);
		} catch (IOException e) {
			ioServices.deleteQuietly(tempPropertiesFile);
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("store properties", e);
		} finally {
			ioServices.closeQuietly(outputStream);
		}
		ioServices.moveFile(tempPropertiesFile, propertiesFile);
	}

	Properties newProperties() {
		return new Properties();
	}

	@Override
	public synchronized void delete(String path) {
		extensions.getSystemWideExtensions().onDeleteConfig(path);
		LOGGER.debug("delete document => " + path);
		removeFromCache(path);
		new File(configFolder, path).delete();

		for (ConfigDeletedEventListener listener : deletedConfigEventListeners.get(path)) {
			listener.onConfigDeleted(path);
		}
	}

	@Override
	public synchronized void deleteFolder(String path) {
		extensions.getSystemWideExtensions().onDeleteConfig(path);
		LOGGER.debug("delete folder => " + path);
		// TODO Remove from cache? Are folders in cache??
		File folderToDelete = new File(configFolder, path);
		try {
			FileUtils.deleteDirectory(folderToDelete);
		} catch (IOException e) {
			// TODO Proper exception
			throw new RuntimeException(e);
		}

		for (ConfigDeletedEventListener listener : deletedConfigEventListeners.get(path)) {
			listener.onConfigDeleted(path);
		}
	}

	@Override
	public synchronized void delete(String path, String hash)
			throws OptimisticLockingConfiguration {
		extensions.getSystemWideExtensions().onDeleteConfig(path);
		LOGGER.debug("delete document  => " + path);
		String content;
		try {
			content = readFile(path);
		} catch (ConfigManagerRuntimeException.NoSuchConfiguration e) {
			LOGGER.debug("Nothing to delete", e);
			return;
		}
		String expectedVersion = "";
		try {
			expectedVersion = hashService.getHashFromString(content);
		} catch (HashingServiceException e) {
			throw new ConfigManagerRuntimeException.CannotHashTheFile(path, e);
		}

		validateHash(path, hash, expectedVersion);

		removeFromCache(path);
		new File(configFolder, path).delete();

		for (ConfigDeletedEventListener listener : deletedConfigEventListeners.get(path)) {
			listener.onConfigDeleted(path);
		}
	}

	@Override
	public synchronized boolean exist(String path) {
		return exist(path, true);
	}

	public synchronized boolean exist(String path, boolean callExtensions) {
		if (callExtensions) {
			extensions.getSystemWideExtensions().onReadConfig(path);
		}
		File file = new File(configFolder, path);
		return file.exists() && file.isFile();
	}

	@Override
	public synchronized boolean folderExist(String path) {
		extensions.getSystemWideExtensions().onReadConfig(path);
		File file = new File(configFolder, path);
		return file.exists() && file.isDirectory();
	}

	@Override
	public List<String> list(String path) {
		extensions.getSystemWideExtensions().onReadConfig(path);
		List<String> fileNames = new ArrayList<>();
		File file = new File(configFolder, path);
		if (file.exists()) {
			fileNames.addAll(Arrays.asList(file.list()));
		}
		return fileNames;
	}

	@Override
	public synchronized BinaryConfiguration getBinary(String path) {
		extensions.getSystemWideExtensions().onReadConfig(path);
		LOGGER.debug("get binary => " + path);
		if (!exist(path, false)) {
			return null;
		}

		StreamFactory<InputStream> binaryStreamFactory = ioServices
				.newInputStreamFactory(new File(configFolder, path), READ_BINARY_FILE);

		InputStream inputStream = null;
		try {
			inputStream = binaryStreamFactory.create(getClass().getName() + ".getBinary");
			return new BinaryConfiguration(hashService.getHashFromBytes(ioServices.readBytes(inputStream)), binaryStreamFactory);
		} catch (HashingServiceException e) {
			throw new ConfigManagerRuntimeException.CannotHashTheFile(path, e);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("read binary file", e);
		} finally {
			ioServices.closeQuietly(inputStream);
		}

	}

	String getContentOfDocument(Document doc) {
		if (doc.getRootElement() == null) {
			throw new ImpossibleRuntimeException("Document must have at least one element");
		}

		XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
		return xmlOutput.outputString(doc);
	}

	Document getDocumentFromFile(File file) {
		SAXBuilder builder = newSAXBuilder();
		try {
			return builder.build(file);
		} catch (JDOMException e) {
			throw new ConfigManagerRuntimeException("JDOM2 Exception reading '" + file.getAbsolutePath() + "'", e);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("build Document JDOM2 from file", e);
		}
	}

	@Override
	public synchronized PropertiesConfiguration getProperties(String path) {
		extensions.getSystemWideExtensions().onReadConfig(path);
		LOGGER.debug("get properties  => " + path);
		if (!exist(path, false)) {
			return null;
		}

		File propertiesFile = new File(configFolder, path);

		Properties properties = newProperties();
		InputStream inputStream = null;
		try {
			inputStream = ioServices.newFileInputStream(propertiesFile, READ_PROPERTIES_FILE);
			properties.load(inputStream);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("load the properties file", e);
		} finally {
			ioServices.closeQuietly(inputStream);
		}

		Map<String, String> mapProperties = propertiesToMap(properties);

		inputStream = null;
		try {
			inputStream = ioServices.newFileInputStream(propertiesFile, READ_PROPERTIES_FILE_2);
			return new PropertiesConfiguration(hashService.getHashFromBytes(ioServices.readBytes(inputStream)), mapProperties);
		} catch (HashingServiceException e) {
			throw new ConfigManagerRuntimeException.CannotHashTheFile(path, e);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("load the properties file", e);
		} finally {
			ioServices.closeQuietly(inputStream);
		}

	}

	@Override
	public synchronized XMLConfiguration getXML(String path) {
		extensions.getSystemWideExtensions().onReadConfig(path);
		XMLConfiguration cachedConfiguration = (XMLConfiguration) getFromCache(path);
		if (cachedConfiguration == null) {
			try {
				cachedConfiguration = readXML(path);
			} catch (NoSuchConfiguration noSuchConfiguration) {
				return null;
			}
			putInCache(path, cachedConfiguration);
		}
		return cachedConfiguration;

	}

	private XMLConfiguration readXML(String path)
			throws NoSuchConfiguration {

		String fileContent = readFile(path);
		String hash;
		try {
			hash = hashService.getHashFromString(fileContent);
		} catch (HashingServiceException e) {
			throw new ConfigManagerRuntimeException.CannotHashTheFile(path, e);
		}
		Document doc = getDocumentFromFile(new File(configFolder, path));
		String version = readVersion(doc);
		if (version.equals(NO_VERSION)) {
			XMLConfiguration config = new XMLConfiguration(hash, hash, doc);
			return config;
		} else {
			return new XMLConfiguration(version, hash, doc);
		}
	}

	String readVersion(Document doc) {
		Element root = doc.getRootElement();
		String version = root.getAttributeValue("version");
		if (version == null) {
			return NO_VERSION;
		} else {
			return version;
		}
	}

	SAXBuilder newSAXBuilder() {
		return new SAXBuilder();
	}

	private String readFile(String path)
			throws NoSuchConfiguration {

		validateFileExistance(path);
		String content = "";
		try {
			if (TenantUtils.getTenantId() == null ||
				!configFolder.getAbsolutePath().contains(TenantUtils.getTenantId())) {
				LOGGER.error("read file  => " + new File(configFolder, path).getAbsolutePath() + " for " + TenantUtils.getTenantId(), new RuntimeException(""));
			}
			content = ioServices.readFileToString(new File(configFolder, path));
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("read file content", e);
		}
		return content;
	}

	Map<String, String> propertiesToMap(Properties properties) {
		Map<String, String> mapOfProperties = new HashMap<String, String>();

		Enumeration<?> enumeration = properties.propertyNames();
		while (enumeration.hasMoreElements()) {
			String key = (String) enumeration.nextElement();
			String value = properties.getProperty(key);
			mapOfProperties.put(key, value);
		}

		return mapOfProperties;
	}

	@Override
	public synchronized void update(String path, String version, Document newDocument)
			throws OptimisticLockingConfiguration {
		extensions.getSystemWideExtensions().onAddUpdateConfig(path);
		LOGGER.debug("update document  => " + path + " to version => " + version);
		File xmlFile = new File(configFolder, path);
		validateFileExistance(path);

		XMLConfiguration xmlConfigurationOfPath = this.getXML(path);
		String expectedVersion = xmlConfigurationOfPath.getHash();
		validateHash(path, version, expectedVersion);

		if (expectedVersion.equals(readVersion(newDocument)) && newDocument != xmlConfigurationOfPath.getDocument()) {
			throw new ConfigManagerRuntimeException.WrongVersion(expectedVersion);
		} else {
			try {
				String content = getContentOfDocument(newDocument);
				ioServices.replaceFileContent(xmlFile, content);
				removeFromCache(path);
			} catch (IOException e) {
				throw new ConfigManagerRuntimeException.CannotCompleteOperation("replace File Content", e);
			}
		}

		for (ConfigUpdatedEventListener listener : updatedConfigEventListeners.get(path)) {
			listener.onConfigUpdated(path);
		}
		eventBus.send(CONFIG_UPDATED_EVENT_TYPE, path);
	}

	private void cacheRemoveAndCallListeners(String path) {
		cache.remove(path);

		for (ConfigUpdatedEventListener listener : updatedConfigEventListeners.get(path)) {
			listener.onConfigUpdated(path);
		}
	}

	@Override
	public synchronized void update(String path, String version, InputStream newBinaryStream)
			throws OptimisticLockingConfiguration {
		extensions.getSystemWideExtensions().onAddUpdateConfig(path);
		LOGGER.debug("update inputstream  => " + path + " to version => " + version);
		validateFileExistance(path);

		BinaryConfiguration binaryConfigurationOfPath = this.getBinary(path);
		String expectedVersion = binaryConfigurationOfPath.getHash();

		validateHash(path, version, expectedVersion);

		try {
			ioServices.replaceFileContent(new File(configFolder, path), newBinaryStream);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("copy binary stream to file", e);
		}

		for (ConfigUpdatedEventListener listener : updatedConfigEventListeners.get(path)) {
			listener.onConfigUpdated(path);
		}
		eventBus.send(CONFIG_UPDATED_EVENT_TYPE, path);
	}

	@Override
	public synchronized void update(String path, String version, Map<String, String> newProperties)
			throws OptimisticLockingConfiguration {
		extensions.getSystemWideExtensions().onAddUpdateConfig(path);
		LOGGER.debug("update properties  => " + path + " to version => " + version);
		validateFileExistance(path);

		File propertiesFile = new File(configFolder, path);
		File tempPropertiesFile = ioServices.getAtomicWriteTempFileFor(propertiesFile);

		Properties properties = mapToProperties(newProperties);

		PropertiesConfiguration propertiesConfigurationOfPath = this.getProperties(path);
		String expectedVersion = propertiesConfigurationOfPath.getHash();

		validateHash(path, version, expectedVersion);
		OutputStream outputStream = null;
		try {
			outputStream = ioServices.newFileOutputStream(tempPropertiesFile, UPDATE_PROPERTIES_FILE);
			PropertyFileUtils.store(properties, outputStream);
		} catch (IOException e) {
			ioServices.deleteQuietly(tempPropertiesFile);
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("Write to properties file", e);
		} finally {
			ioServices.closeQuietly(outputStream);
		}

		ioServices.moveFile(tempPropertiesFile, propertiesFile);

		for (ConfigUpdatedEventListener listener : updatedConfigEventListeners.get(path)) {
			listener.onConfigUpdated(path);
		}
		eventBus.send(CONFIG_UPDATED_EVENT_TYPE, path);
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
			this.deletedConfigEventListeners.addAtStart(path, (ConfigDeletedEventListener) listener);
		}
	}

	private Properties mapToProperties(Map<String, String> newProperties) {
		TreeMap<String, String> newPropertiesSorted = new TreeMap<String, String>();
		Properties properties = newProperties();
		for (Map.Entry<String, String> entry : newProperties.entrySet()) {
			if (entry.getValue() != null) {
				newPropertiesSorted.put(entry.getKey(), entry.getValue());
			} else {
				properties.remove(entry.getKey());
			}
		}

		properties.putAll(newPropertiesSorted);
		return properties;
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
	public void deleteAllConfigsIn(String collection) {
		List<String> toDelete = new ArrayList<>();
		for (String cacheKey : getCacheKeys()) {
			if (cacheKey.startsWith(collection)) {
				toDelete.add(cacheKey);
			}
		}
		for (String collectionConfig : toDelete) {
			removeFromCache(collectionConfig);
		}
		File folderCollection = new File(configFolder, collection);
		ioServices.deleteDirectoryWithoutExpectableIOException(folderCollection);
	}

	@Override
	public synchronized void copySettingsFrom(File setting) {
		try {
			this.ioServices.copyDirectory(setting, this.configFolder);
			clearCache();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void move(String src, String dest) {
		extensions.getSystemWideExtensions().onAddUpdateConfig(dest);
		if (!exist(src, false)) {
			throw new NoSuchConfiguration(src);
		}
		if (exist(dest, false)) {
			throw new ConfigurationAlreadyExists(dest);
		}
		File srcFile = new File(configFolder, src);
		File destFile = new File(configFolder, dest);
		if (!srcFile.renameTo(destFile)) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("move '" + src + "' to '" + dest + "'", null);
		}
	}

	@Override
	public void importFrom(File settingsFolder) {
		try {
			File bckFile = new File(this.configFolder.getParentFile(), this.configFolder.getName() + ".bck");
			this.configFolder.renameTo(bckFile);
			FileUtils.copyDirectory(settingsFolder, this.configFolder);
			FileUtils.deleteDirectory(bckFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void exportTo(File settingsFolder) {
		try {
			FileUtils.copyDirectory(this.configFolder, settingsFolder);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void keepInCache(String path) {
		//This config manager has no cache
	}

	private void validateFileExistance(String path) {
		if (!exist(path, false)) {
			throw new NoSuchConfiguration(path);
		}
	}

	private void validateFileNonExistance(String path) {
		if (exist(path, false)) {
			throw new ConfigManagerRuntimeException.ConfigurationAlreadyExists(path);
		}
	}

	void validateHash(String path, String version, String expectedVersion)
			throws OptimisticLockingConfiguration {
		if (!version.equals(expectedVersion)) {
			cacheRemoveAndCallListeners(path);
			throw new ConfigManagerException.OptimisticLockingConfiguration(path, expectedVersion, version);
		}
	}

	@Override
	public void close() {

	}

	public File getConfigFolder() {
		return configFolder;
	}

	@Override
	public TextConfiguration getText(String path) {
		extensions.getSystemWideExtensions().onAddUpdateConfig(path);
		LOGGER.debug("get Text  => " + path);
		TextConfiguration cachedConfiguration = null;// (XMLConfiguration) getFromCache(path);
		if (cachedConfiguration == null) {
			try {
				String content = readFile(path);
				String version = hashService.getHashFromString(content);
				cachedConfiguration = new TextConfiguration(version, content);
			} catch (NoSuchConfiguration | HashingServiceException noSuchConfiguration) {
				return null;
			}
			putInCache(path, cachedConfiguration);
		}
		return cachedConfiguration;

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
