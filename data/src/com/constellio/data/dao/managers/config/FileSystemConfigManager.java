/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.data.dao.managers.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException.ConfigurationAlreadyExists;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException.NoSuchConfiguration;
import com.constellio.data.dao.managers.config.events.ConfigEventListener;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.managers.config.values.TextConfiguration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.KeyListMap;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;

public class FileSystemConfigManager implements StatefulService, ConfigManager {

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

	private final Map<String, Object> cache = new HashMap<>();

	private final KeyListMap<String, ConfigUpdatedEventListener> updatedConfigEventListeners = new KeyListMap<>();

	public FileSystemConfigManager(File configFolder, IOServices ioServices, HashingService hashService) {
		super();
		this.configFolder = configFolder;
		this.ioServices = ioServices;
		this.hashService = hashService;
	}

	@Override
	public void initialize() {

	}

	Map<String, Object> getCache() {
		return cache;
	}

	@Override
	public synchronized void add(String path, Document newDocument) {
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
		validateFileNonExistance(path);
		LOGGER.debug("add inputstream => " + path);
		File binFile = createFoldersAndFiles(path);

		OutputStream outputStream = null;
		try {
			outputStream = ioServices.newFileOutputStream(binFile, ADD_BINARY_FILE);
			ioServices.copy(newBinaryStream, outputStream);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("copy binary to file", e);
		} finally {
			ioServices.closeQuietly(outputStream);
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
		validateFileNonExistance(path);
		LOGGER.debug("add properties => " + path);

		File propertiesFile = createFoldersAndFiles(path);

		Properties properties = mapToProperties(newProperties);

		OutputStream outputStream = null;
		try {
			outputStream = ioServices.newFileOutputStream(propertiesFile, ADD_PROPERTIES_FILE);
			PropertyFileUtils.store(properties, outputStream);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("store properties", e);
		} finally {
			ioServices.closeQuietly(outputStream);
		}
	}

	Properties newProperties() {
		return new Properties();
	}

	@Override
	public synchronized void delete(String path) {
		LOGGER.debug("delete document => " + path);
		cache.remove(path);
		new File(configFolder, path).delete();
	}

	@Override
	public synchronized void delete(String path, String hash)
			throws OptimisticLockingConfiguration {
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

		cache.remove(path);
		new File(configFolder, path).delete();
	}

	@Override
	public synchronized boolean exist(String path) {
		File file = new File(configFolder, path);
		return file.exists() && file.isFile();
	}

	@Override
	public List<String> list(String path) {
		List<String> fileNames = new ArrayList<>();
		File file = new File(configFolder, path);
		if (file.exists()) {
			fileNames.addAll(Arrays.asList(file.list()));
		}
		return fileNames;
	}

	@Override
	public synchronized BinaryConfiguration getBinary(String path) {
		LOGGER.debug("get binary => " + path);
		if (!exist(path)) {
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
		XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
		return xmlOutput.outputString(doc);
	}

	Document getDocumentFromFile(File file) {
		SAXBuilder builder = newSAXBuilder();
		try {
			return builder.build(file);
		} catch (JDOMException e) {
			throw new ConfigManagerRuntimeException("JDOM2 Exception", e);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("build Document JDOM2 from file", e);
		}
	}

	@Override
	public synchronized PropertiesConfiguration getProperties(String path) {
		LOGGER.debug("get properties  => " + path);
		if (!exist(path)) {
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
		LOGGER.debug("get XML  => " + path);
		XMLConfiguration cachedConfiguration = null;// (XMLConfiguration) cache.get(path);
		if (cachedConfiguration == null) {
			try {
				cachedConfiguration = readXML(path);
			} catch (NoSuchConfiguration noSuchConfiguration) {
				return null;
			}
			cache.put(path, cachedConfiguration);
		}
		return cachedConfiguration;

	}

	private XMLConfiguration readXML(String path)
			throws NoSuchConfiguration {
		String fileContent = readFile(path);

		Document doc = getDocumentFromFile(new File(configFolder, path));
		String version = readVersion(doc);
		if (version.equals(NO_VERSION)) {
			try {
				version = hashService.getHashFromString(fileContent);
				XMLConfiguration config = new XMLConfiguration(version, doc);
				cache.put(path, config);
				return config;
			} catch (HashingServiceException e) {
				throw new ConfigManagerRuntimeException.CannotHashTheFile(path, e);
			}
		} else {
			return new XMLConfiguration(version, doc);
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
				cache.remove(path);
			} catch (IOException e) {
				throw new ConfigManagerRuntimeException.CannotCompleteOperation("replace File Content", e);
			}
		}

		for (ConfigUpdatedEventListener listener : updatedConfigEventListeners.get(path)) {
			listener.onConfigUpdated(path);
		}
	}

	@Override
	public synchronized void update(String path, String version, InputStream newBinaryStream)
			throws OptimisticLockingConfiguration {
		LOGGER.debug("update inputstream  => " + path + " to version => " + version);
		validateFileExistance(path);

		BinaryConfiguration binaryConfigurationOfPath = this.getBinary(path);
		String expectedVersion = binaryConfigurationOfPath.getHash();

		validateHash(path, version, expectedVersion);

		OutputStream outputStream = null;
		try {
			outputStream = ioServices.newFileOutputStream(new File(configFolder, path), UPDATE_BINARY_FILE);
			ioServices.copy(newBinaryStream, outputStream);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("copy binary stream to file", e);
		} finally {
			ioServices.closeQuietly(outputStream);
		}

		for (ConfigUpdatedEventListener listener : updatedConfigEventListeners.get(path)) {
			listener.onConfigUpdated(path);
		}
	}

	@Override
	public synchronized void update(String path, String version, Map<String, String> newProperties)
			throws OptimisticLockingConfiguration {
		LOGGER.debug("update properties  => " + path + " to version => " + version);
		validateFileExistance(path);

		File propertiesFile = new File(configFolder, path);

		Properties properties = mapToProperties(newProperties);

		PropertiesConfiguration propertiesConfigurationOfPath = this.getProperties(path);
		String expectedVersion = propertiesConfigurationOfPath.getHash();

		validateHash(path, version, expectedVersion);
		OutputStream outputStream = null;
		try {
			outputStream = ioServices.newFileOutputStream(propertiesFile, UPDATE_PROPERTIES_FILE);
			PropertyFileUtils.store(properties, outputStream);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("Write to properties file", e);
		} finally {
			ioServices.closeQuietly(outputStream);
		}

		for (ConfigUpdatedEventListener listener : updatedConfigEventListeners.get(path)) {
			listener.onConfigUpdated(path);
		}
	}

	@Override
	public void registerListener(String path, ConfigEventListener listener) {
		if (listener instanceof ConfigUpdatedEventListener) {
			this.updatedConfigEventListeners.add(path, (ConfigUpdatedEventListener) listener);
		}
	}

	private Properties mapToProperties(Map<String, String> newProperties) {
		TreeMap<String, String> newPropertiesSorted = new TreeMap<String, String>(newProperties);
		Properties properties = newProperties();
		properties.putAll(newPropertiesSorted);
		return properties;
	}

	@Override
	public synchronized void updateXML(String path, DocumentAlteration documentAlteration) {

		XMLConfiguration xmlConfiguration = this.getXML(path);

		Document doc = xmlConfiguration.getDocument();
		documentAlteration.alter(doc);

		try {
			update(path, xmlConfiguration.getHash(), doc);
		} catch (OptimisticLockingConfiguration e) {
			throw new ImpossibleRuntimeException(e);
		}

		cache.remove(path);
	}

	@Override
	public synchronized void updateProperties(String path, PropertiesAlteration propertiesAlteration) {

		PropertiesConfiguration propertiesConfiguration = this.getProperties(path);

		Map<String, String> doc = propertiesConfiguration.getProperties();
		propertiesAlteration.alter(doc);

		try {
			update(path, propertiesConfiguration.getHash(), doc);
		} catch (OptimisticLockingConfiguration e) {
			throw new ImpossibleRuntimeException(e);
		}

		cache.remove(path);
	}

	@Override
	public void createXMLDocumentIfInexistent(String path, DocumentAlteration documentAlteration) {
		if (!exist(path)) {
			Document document = new Document();
			documentAlteration.alter(document);
			try {
				add(path, document);
			} catch (ConfigurationAlreadyExists e) {
				LOGGER.info("Configuration was created by another instance", e);
			}
		}

	}

	@Override
	public void createPropertiesDocumentIfInexistent(String path, PropertiesAlteration propertiesAlteration) {
		if (!exist(path)) {
			Map<String, String> properties = new HashMap<>();
			propertiesAlteration.alter(properties);
			try {
				add(path, properties);
			} catch (ConfigurationAlreadyExists e) {
				LOGGER.info("Configuration was created by another instance", e);
			}
		}
	}

	@Override
	public void deleteAllConfigsIn(String collection) {
		List<String> toDelete = new ArrayList<>();
		for (Entry<String, Object> entry : cache.entrySet()) {
			if (entry.getKey().startsWith(collection)) {
				toDelete.add(entry.getKey());
			}
		}
		for (String collectionConfig : toDelete) {
			cache.remove(collectionConfig);
		}
		File folderCollection = new File(configFolder, collection);
		ioServices.deleteDirectoryWithoutExpectableIOException(folderCollection);
	}

	private void validateFileExistance(String path) {
		if (!exist(path)) {
			throw new NoSuchConfiguration(path);
		}
	}

	private void validateFileNonExistance(String path) {
		if (exist(path)) {
			throw new ConfigManagerRuntimeException.ConfigurationAlreadyExists(path);
		}
	}

	void validateHash(String path, String version, String expectedVersion)
			throws OptimisticLockingConfiguration {
		if (!version.equals(expectedVersion)) {
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
		LOGGER.debug("get Text  => " + path);
		TextConfiguration cachedConfiguration = null;// (XMLConfiguration) cache.get(path);
		if (cachedConfiguration == null) {
			try {
				String content = readFile(path);
				String version = hashService.getHashFromString(content);
				cachedConfiguration = new TextConfiguration(version, content);
			} catch (NoSuchConfiguration | HashingServiceException noSuchConfiguration) {
				return null;
			}
			cache.put(path, cachedConfiguration);
		}
		return cachedConfiguration;

	}
}
