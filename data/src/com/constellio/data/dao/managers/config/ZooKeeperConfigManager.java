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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
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

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.events.ConfigEventListener;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.managers.config.values.TextConfiguration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.KeyListMap;

public class ZooKeeperConfigManager implements StatefulService, ConfigManager, CuratorListener {
	private static final String GET_BINARY_CONTENT = "ZooKeeperConfigManager-getBinaryContent";
	private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperConfigManager.class);
	private final KeyListMap<String, ConfigUpdatedEventListener> updatedConfigEventListeners = new KeyListMap<>();
	private CuratorFramework client;
	private String CONFIG_FOLDER = "/constellio";
	private String address;
	private IOServices ioServices;

	public ZooKeeperConfigManager(String address, IOServices ioServices) {
		this.address = address;
		this.ioServices = ioServices;
	}

	@Override
	public void initialize() {
		try {
			RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
			client = CuratorFrameworkFactory.newClient(address, retryPolicy);
			client.start();

			client.getCuratorListenable().addListener(this);

		} catch (Exception e) {
			CloseableUtils.closeQuietly(client);
			throw new RuntimeException("Zookeeper exception");
		}
	}

	@Override
	public BinaryConfiguration getBinary(String path) {
		String tmpPath = processPath(CONFIG_FOLDER, path);
		byte[] ret;

		if (!exist(tmpPath)) {
			return null;
		}

		try {
			InterProcessReadWriteLock lock = new InterProcessReadWriteLock(client, tmpPath);
			InterProcessMutex readLock = lock.readLock();
			readLock.acquire();

			ret = client.getData().watched().forPath(tmpPath);

			readLock.release();

			return new BinaryConfiguration(getVersion(tmpPath), ioServices.newByteArrayStreamFactory(ret, GET_BINARY_CONTENT));
		} catch (NoNodeException e) {
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public XMLConfiguration getXML(String path) {
		String tmpPath = processPath(CONFIG_FOLDER, path);
		byte[] ret;

		if (!exist(tmpPath)) {
			return null;
		}

		try {
			InterProcessReadWriteLock lock = new InterProcessReadWriteLock(client, tmpPath);
			InterProcessMutex readLock = lock.readLock();
			readLock.acquire();

			ret = client.getData().watched().forPath(tmpPath);
			Document configuration = getDocumentFrom(ret);

			readLock.release();

			return new XMLConfiguration(getVersion(tmpPath), configuration);
		} catch (NoNodeException e) {
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public PropertiesConfiguration getProperties(String path) {
		String tmpPath = processPath(CONFIG_FOLDER, path);
		byte[] ret;

		try {
			client.sync().forPath(CONFIG_FOLDER);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (!exist(tmpPath)) {
			return null;
		}

		try {
			InterProcessReadWriteLock lock = new InterProcessReadWriteLock(client, tmpPath);
			InterProcessMutex readLock = lock.readLock();
			readLock.acquire();

			ret = client.getData().watched().forPath(tmpPath);
			Properties properties = new Properties();
			ByteArrayInputStream input = new ByteArrayInputStream(ret);
			properties.load(input);
			input.close();

			readLock.release();

			return new PropertiesConfiguration(getVersion(tmpPath), propertiesToMap(properties));
		} catch (NoNodeException e) {
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean exist(String path) {
		String tmpPath = processPath(CONFIG_FOLDER, path);
		try {
			Stat stat = client.checkExists().forPath(tmpPath);
			return stat != null;
		} catch (KeeperException.NoNodeException e) {
			return false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void createXMLDocumentIfInexistent(String path, DocumentAlteration documentAlteration) {
		String tmpPath = processPath(CONFIG_FOLDER, path);
		if (!exist(tmpPath)) {
			Document newDocument = new Document();
			documentAlteration.alter(newDocument);

			try {
				this.add(path, newDocument);
			} catch (ConfigManagerRuntimeException.ConfigurationAlreadyExists e) {
				LOGGER.info("Configuration was created by another instance", e);
			}
		}
	}

	@Override
	public void createPropertiesDocumentIfInexistent(String path, PropertiesAlteration propertiesAlteration) {
		String tmpPath = processPath(CONFIG_FOLDER, path);
		if (!exist(tmpPath)) {
			Map<String, String> mapProperties = new HashMap<>();
			propertiesAlteration.alter(mapProperties);

			try {
				this.add(path, mapProperties);
			} catch (ConfigManagerRuntimeException.ConfigurationAlreadyExists e) {
				LOGGER.info("Configuration was created by another instance", e);
			}
		}
	}

	@Override
	// the update must be atomic (no call to external method who access ZooKeeper) to keep lock integrity
	public void updateXML(String path, DocumentAlteration documentAlteration) {
		String tmpPath = processPath(CONFIG_FOLDER, path);

		InterProcessMutex lock = lockPath(tmpPath);
		try {
			// get current version
			byte[] ret = client.getData().forPath(tmpPath);
			Document document = getDocumentFrom(ret);
			documentAlteration.alter(document);

			// update the data and get the new bytes
			int version = Integer.parseInt(getVersion(tmpPath));
			byte[] bytes = getByteFromDocument(document);
			client.setData().withVersion(version).forPath(tmpPath, bytes);

			unlockPath(lock);
		} catch (OptimisticLockingConfiguration e) {
			unlockPath(lock);
			updateXML(path, documentAlteration);
		} catch (Exception e) {
			unlockPath(lock);
			throw new RuntimeException(e);
		}
	}

	@Override
	// the update must be atomic (no call to external method who access ZooKeeper) to keep lock integrity
	public void updateProperties(String path, PropertiesAlteration propertiesAlteration) {
		String tmpPath = processPath(CONFIG_FOLDER, path);

		InterProcessMutex lock = lockPath(tmpPath);
		try {
			// get current version
			byte[] ret = client.getData().forPath(tmpPath);
			Properties properties = new Properties();
			ByteArrayInputStream input = new ByteArrayInputStream(ret);
			properties.load(input);
			input.close();
			int version = Integer.parseInt(getVersion(tmpPath));

			// update the data and get the new bytes
			Map<String, String> mapProperties = propertiesToMap(properties);
			propertiesAlteration.alter(mapProperties);
			Properties prop = mapToProperties(mapProperties);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			prop.store(output, null);

			client.setData().withVersion(version).forPath(tmpPath, output.toByteArray());

			unlockPath(lock);
		} catch (OptimisticLockingConfiguration e) {
			unlockPath(lock);
			updateProperties(path, propertiesAlteration);
		} catch (Exception e) {
			unlockPath(lock);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void add(String path, InputStream newBinaryStream) {
		String tmpPath = processPath("/constellio", path);
		try {
			byte[] bytes = IOUtils.toByteArray(newBinaryStream);
			client.create().creatingParentsIfNeeded().forPath(tmpPath, bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (KeeperException.NodeExistsException e) {
			throw new ConfigManagerRuntimeException.ConfigurationAlreadyExists(path);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void add(String path, Document newDocument) {
		String tmpPath = processPath("/constellio", path);
		try {
			byte[] bytes = getByteFromDocument(newDocument);
			client.create().creatingParentsIfNeeded().forPath(tmpPath, bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (KeeperException.NodeExistsException e) {
			throw new ConfigManagerRuntimeException.ConfigurationAlreadyExists(path);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void add(String path, Map<String, String> newProperties) {
		String tmpPath = processPath("/constellio", path);
		try {
			Properties prop = mapToProperties(newProperties);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			prop.store(output, null);

			client.create().creatingParentsIfNeeded().forPath(tmpPath, output.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (KeeperException.NodeExistsException e) {
			throw new ConfigManagerRuntimeException.ConfigurationAlreadyExists(path);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void update(String path, String hash, InputStream newBinaryStream)
			throws OptimisticLockingConfiguration {
		String tmpPath = processPath("/constellio", path);
		try {
			byte[] bytes = IOUtils.toByteArray(newBinaryStream);
			client.setData().withVersion(Integer.parseInt(hash)).forPath(tmpPath, bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (KeeperException.BadVersionException e) {
			throw new OptimisticLockingConfiguration(path, hash, "");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void update(String path, String hash, Document newDocument)
			throws OptimisticLockingConfiguration {
		String tmpPath = processPath("/constellio", path);
		InterProcessMutex lock = lockPath(tmpPath);
		try {
			byte[] bytes = getByteFromDocument(newDocument);
			client.setData().withVersion(Integer.parseInt(hash)).forPath(tmpPath, bytes);

			unlockPath(lock);
		} catch (KeeperException.BadVersionException e) {
			unlockPath(lock);
			throw new OptimisticLockingConfiguration(path, hash, "");
		} catch (Exception e) {
			unlockPath(lock);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void update(String path, String hash, Map<String, String> newProperties)
			throws OptimisticLockingConfiguration {
		String tmpPath = processPath("/constellio", path);
		InterProcessMutex lock = lockPath(tmpPath);
		try {
			Properties prop = mapToProperties(newProperties);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			prop.store(output, null);
			client.setData().withVersion(Integer.parseInt(hash)).forPath(tmpPath, output.toByteArray());

			unlockPath(lock);
		} catch (IOException e) {
			unlockPath(lock);
			throw new RuntimeException(e);
		} catch (KeeperException.BadVersionException e) {
			unlockPath(lock);
			throw new OptimisticLockingConfiguration(path, hash, "");
		} catch (Exception e) {
			unlockPath(lock);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void delete(String path) {
		String tmpPath = processPath(CONFIG_FOLDER, path);

		InterProcessMutex lock = lockPath(tmpPath);
		try {
			client.delete().deletingChildrenIfNeeded().forPath(tmpPath);

			unlockPath(lock);
		} catch (Exception e) {
			unlockPath(lock);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void delete(String path, String hash)
			throws OptimisticLockingConfiguration {
		String tmpPath = processPath(CONFIG_FOLDER, path);

		InterProcessMutex lock = lockPath(tmpPath);
		try {
			client.delete().deletingChildrenIfNeeded().withVersion(Integer.parseInt(hash)).forPath(tmpPath);

			unlockPath(lock);
		} catch (BadVersionException e) {
			unlockPath(lock);
			throw new ConfigManagerException.OptimisticLockingConfiguration(path, hash, "");
		} catch (Exception e) {
			unlockPath(lock);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void deleteAllConfigsIn(String collection) {
		String tmpPath = processPath(CONFIG_FOLDER, "/" + collection);

		InterProcessMutex lock = lockPath(tmpPath);
		try {
			client.delete().deletingChildrenIfNeeded().forPath(tmpPath);

			unlockPath(lock);
		} catch (Exception e) {
			unlockPath(lock);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void registerListener(String path, ConfigEventListener listener) {
		String pathTmp = processPath("/constellio", path);

		if (listener instanceof ConfigUpdatedEventListener) {
			this.updatedConfigEventListeners.add(pathTmp, (ConfigUpdatedEventListener) listener);
		}
	}

	public Map<String, Object> getCache() {
		return null;
	}

	//////////////////////////////////////////////////////////////////////////////////////////

	private String getVersion(String tmpPath) {
		try {
			return "" + client.checkExists().forPath(tmpPath).getVersion();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private InterProcessMutex lockPath(String path) {
		InterProcessReadWriteLock lock = new InterProcessReadWriteLock(client, path);
		InterProcessMutex writeLock = lock.writeLock();
		try {
			writeLock.acquire();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return writeLock;
	}

	private void unlockPath(InterProcessMutex lock) {
		try {
			lock.release();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

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
		XMLOutputter outputter = new XMLOutputter();
		String doc = outputter.outputString(document);
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

	private String processPath(String startingPath, String path) {
		String[] partsTmp = path.split("/");

		List<String> parts = new ArrayList<>();
		for (String part : partsTmp) {
			if (!part.equals("")) {
				parts.add(part);
			}
		}

		if (parts.size() == 1) {
			startingPath += "/" + parts.get(0);
		} else if (parts.size() > 1) {
			int start = 0;
			if (parts.get(0).contains("constellio")) {
				start = 1;
			}

			for (int i = start; i < parts.size(); i++) {
				startingPath += "/" + parts.get(i);
			}
		}
		return startingPath;
	}

	@Override
	public void eventReceived(CuratorFramework curatorFramework, CuratorEvent event)
			throws Exception {
		switch (event.getType()) {
		case SET_DATA: {
			String path = event.getPath();
			for (ConfigUpdatedEventListener listener : updatedConfigEventListeners.get(path)) {
				String pathTmp = processPath("", path);
				listener.onConfigUpdated(pathTmp);
			}
			break;
		}
		}
	}

	@Override
	public void close() {

	}

	@Override
	public TextConfiguration getText(String path) {
		throw new UnsupportedOperationException("TODO");
	}
}
