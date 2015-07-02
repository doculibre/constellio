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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException.ConfigurationAlreadyExists;
import com.constellio.data.dao.managers.config.events.ConfigEventListener;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.ThreadList;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;

public class FileSystemConfigManagerAcceptanceTest extends ConstellioTest {

	FileSystemConfigManager configManager;

	String propertiesConfigurationPath = "resources/configs.properties";
	String propertiesConfigurationAddedPath = "resources/newConfigs.properties";

	String xmlConfigurationPath = "resources/test.xml";

	String binaryConfigurationPath = "resources/config.bin";
	String binaryConfigurationPathRecursiveFolders = "doc/docu/resources/configuration.bin";

	IOServices ioServices;

	HashingService hashService;

	Map<String, String> properties;
	Map<String, String> propertiesUpdated;
	InputStream binInputStream;
	InputStream binInputStreamUpdated;

	File root;

	Document docXML;
	Document docXMLWithVersion;
	Document docXMLWithVersionUpdated;

	File inputStream;
	File inputStreamUpdated;

	@Mock ConfigUpdatedEventListener firstListener;
	@Mock ConfigUpdatedEventListener secondListener;
	@Mock ConfigUpdatedEventListener otherPathListener;
	@Mock ConfigEventListener otherEventListener;

	@Before
	public void setUp()
			throws FileNotFoundException {

		hashService = getIOLayerFactory().newHashingService();

		root = newTempFolder();

		ioServices = getIOLayerFactory().newIOServices();

		configManager = spy(new FileSystemConfigManager(root, ioServices, hashService));

		this.loadProperties();

		this.loadInputStream();

		this.loadElementWithRoot();

	}

	private void loadElementWithRoot() {
		Element root = new Element("myRootElement");
		docXML = new Document(root);

		Element rootWithAttributeVersion = new Element("RootElement");
		rootWithAttributeVersion.setAttribute("version", "1");
		docXMLWithVersion = new Document(rootWithAttributeVersion);

		Element rootWithAttributeVersionUpdated = new Element("RootElementDifferent");
		rootWithAttributeVersionUpdated.setAttribute("version", "2");
		docXMLWithVersionUpdated = new Document(rootWithAttributeVersionUpdated);
	}

	@Test
	public void givenXMLDocWithVersionWhenUpdateThisDocumentThenDocumentUpdated()
			throws Exception {

		configManager.add(xmlConfigurationPath, docXMLWithVersion);

		configManager.update(xmlConfigurationPath, "1", docXMLWithVersionUpdated);

		XMLConfiguration expectedXMLConfiguration = new XMLConfiguration("2", docXMLWithVersionUpdated);

		assertEquals(expectedXMLConfiguration.getDocument(), configManager.getXML(xmlConfigurationPath).getDocument());
		assertThat(configManager.getXML(xmlConfigurationPath).getHash()).isEqualTo(expectedXMLConfiguration.getHash());
	}

	@Test(expected = ConfigManagerRuntimeException.WrongVersion.class)
	public void givenXMLDocWithVersionWhenUpdateThisDocumentWithSameVersionThenException()
			throws Exception {
		configManager.add(xmlConfigurationPath, docXMLWithVersion);

		configManager.update(xmlConfigurationPath, "1", docXMLWithVersion);
	}

	private void loadInputStream()
			throws FileNotFoundException {
		inputStream = newTempFileWithContent("BinaryConfigurationInputStream", "Contenu");
		inputStreamUpdated = newTempFileWithContent("BinaryConfigurationInputStreamUpdated", "anyUpdated");
		binInputStream = closeAfterTest(newFileInputStream(inputStream));
		binInputStreamUpdated = closeAfterTest(newFileInputStream(inputStreamUpdated));
	}

	private void loadProperties() {
		properties = new HashMap<String, String>();
		properties.put("KeyOne", "ValueOne");
		properties.put("KeyTwo", "ValueTwo");

		propertiesUpdated = new HashMap<String, String>();
		propertiesUpdated.put("KeyOneUpdated", "ValueOneUpdated");
		propertiesUpdated.put("KeyTwoUpdated", "ValueTwoUpdated");
	}

	@Test
	public void whenAddPropertiesThenPropertiesExists()
			throws Exception {
		configManager.add(propertiesConfigurationAddedPath, properties);
		assertThat(configManager.exist(propertiesConfigurationAddedPath)).isTrue();
	}

	@Test
	public void givenPropertiesFileWhenGetPropertiesThenReturnRightPropertiesConfiguration()
			throws Exception {
		configManager.add(propertiesConfigurationAddedPath, properties);

		String hash = configManager.getProperties(propertiesConfigurationAddedPath).getHash();

		PropertiesConfiguration expectedPropertiesConfiguration = new PropertiesConfiguration(hash, properties);
		assertThat(configManager.getProperties(propertiesConfigurationAddedPath).getProperties()).isEqualTo(
				expectedPropertiesConfiguration.getProperties());
	}

	@Test
	public void givenAddPropertyWhenUpdatePropertiesConfigurationThenPropertiesUpdated()
			throws Exception {

		configManager.add(propertiesConfigurationPath, properties);

		String hash = configManager.getProperties(propertiesConfigurationPath).getHash();

		configManager.update(propertiesConfigurationPath, hash, propertiesUpdated);

		PropertiesConfiguration expectedPropertiesConfiguration = new PropertiesConfiguration(hash, propertiesUpdated);
		PropertiesConfiguration currentProperties = configManager.getProperties(propertiesConfigurationPath);
		assertThat(currentProperties.getProperties()).isEqualTo(expectedPropertiesConfiguration.getProperties()).isEqualTo(
				propertiesUpdated);

	}

	@Test(expected = ConfigManagerException.OptimisticLockingConfiguration.class)
	public void whenUpdatePropertiesWithDifferentHashCodeThenException()
			throws Exception {

		configManager.add(propertiesConfigurationPath, properties);

		configManager.update(propertiesConfigurationPath, "DifferentHash", properties);
	}

	@Test
	public void whenAddXMLDocumentThenDocumentExists()
			throws Exception {
		configManager.add(xmlConfigurationPath, docXML);
		assertThat(configManager.exist(xmlConfigurationPath)).isTrue();
	}

	@Test
	public void givenXMLFileWhenGetXMLThenReturnRightXMLConfiguration()
			throws Exception {
		configManager.add(xmlConfigurationPath, docXML);

		String hash = configManager.getXML(xmlConfigurationPath).getHash();

		XMLConfiguration expectedXMLConfiguration = new XMLConfiguration(hash, docXML);
		assertEquals(expectedXMLConfiguration.getDocument(), configManager.getXML(xmlConfigurationPath).getDocument());
	}

	private void assertEquals(Document expected, Document was) {
		StringWriter expectedwriter = new StringWriter();
		StringWriter waswriter = new StringWriter();

		try {
			new XMLOutputter().output(expected, expectedwriter);
			new XMLOutputter().output(was, waswriter);

			expectedwriter.close();
			waswriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		assertThat(waswriter.toString()).isEqualTo(expectedwriter.toString());
	}

	@Test(expected = ConfigManagerException.OptimisticLockingConfiguration.class)
	public void whenUpdateXMLWithDifferentHashCodeThenException()
			throws Exception {

		configManager.add(xmlConfigurationPath, docXML);

		configManager.update(xmlConfigurationPath, "differentHash", docXML);
	}

	@Test
	public void whenAddBinaryConfigurationThenExists()
			throws Exception {
		configManager.add(binaryConfigurationPath, binInputStream);
		assertThat(configManager.exist(binaryConfigurationPath)).isTrue();
	}

	@Test
	public void givenBinaryConfigurationFileWhenGetInputStreamFactoryThenReturnRightInputStream()
			throws Exception {

		StreamFactory<InputStream> binStreamFactory = getTestResourceInputStreamFactory(inputStream);
		configManager.add(binaryConfigurationPath, binInputStream);

		String hash = configManager.getBinary(binaryConfigurationPath).getHash();

		BinaryConfiguration expectedBinaryConfiguration = new BinaryConfiguration(hash, binStreamFactory);

		InputStream expectedContentInputStream = expectedBinaryConfiguration.getInputStreamFactory().create(SDK_STREAM);
		InputStream wasContentInputStream = closeAfterTest(configManager.getBinary(binaryConfigurationPath)
				.getInputStreamFactory().create(SDK_STREAM));

		byte[] expectedBytes = ioServices.readBytes(expectedContentInputStream);
		byte[] wasBytes = ioServices.readBytes(wasContentInputStream);

		assertThat(wasBytes).isEqualTo(expectedBytes);

		IOUtils.closeQuietly(expectedContentInputStream);
	}

	@Test
	public void givenBinaryConfigurationWhenUpdateThenUpdated()
			throws Exception {
		StreamFactory<InputStream> binStreamFactoryUpdated = getTestResourceInputStreamFactory(inputStreamUpdated);
		configManager.add(binaryConfigurationPath, binInputStream);
		configManager.registerListener(binaryConfigurationPath, firstListener);
		configManager.registerListener(binaryConfigurationPath, secondListener);
		configManager.registerListener(binaryConfigurationPath, otherEventListener);
		configManager.registerListener("otherPath", otherPathListener);
		verify(firstListener, never()).onConfigUpdated(anyString());
		verify(secondListener, never()).onConfigUpdated(anyString());

		String hash = configManager.getBinary(binaryConfigurationPath).getHash();
		configManager.update(binaryConfigurationPath, hash, binInputStreamUpdated);

		verify(firstListener).onConfigUpdated(binaryConfigurationPath);
		verify(secondListener).onConfigUpdated(binaryConfigurationPath);
		verify(otherPathListener, never()).onConfigUpdated(anyString());
		hash = configManager.getBinary(binaryConfigurationPath).getHash();
		BinaryConfiguration expectedBinaryConfiguration = new BinaryConfiguration(hash, binStreamFactoryUpdated);
		BinaryConfiguration currentBinary = configManager.getBinary(binaryConfigurationPath);
		InputStream expectedContentInputStream = expectedBinaryConfiguration.getInputStreamFactory().create(SDK_STREAM);
		InputStream wasContentInputStream = closeAfterTest(currentBinary.getInputStreamFactory().create(SDK_STREAM));
		byte[] wasBytes = ioServices.readBytes(wasContentInputStream);
		byte[] expectedBytes = ioServices.readBytes(expectedContentInputStream);
		assertThat(wasBytes).isEqualTo(expectedBytes);
		IOUtils.closeQuietly(expectedContentInputStream);
	}

	@Test(expected = ConfigManagerException.OptimisticLockingConfiguration.class)
	public void givenBinaryConfigurationWhenUpdateWithDifferentHashThenException()
			throws Exception {
		configManager.add(binaryConfigurationPath, binInputStream);

		configManager.update(binaryConfigurationPath, "DifferentHash", binInputStreamUpdated);

	}

	@Test
	public void whenDeleteThenDeleted()
			throws Exception {
		configManager.add(binaryConfigurationPath, binInputStream);

		configManager.delete(binaryConfigurationPath);

		assertThat(configManager.exist(binaryConfigurationPath)).isFalse();
	}

	@Test
	public void givenBinaryFileWhenDeleteWithHashThenDeleted()
			throws Exception {
		configManager.add(binaryConfigurationPath, binInputStream);

		String hash = configManager.getBinary(binaryConfigurationPath).getHash();
		configManager.delete(binaryConfigurationPath, hash);

		assertThat(configManager.exist(binaryConfigurationPath)).isFalse();
	}

	@Test(expected = ConfigManagerException.OptimisticLockingConfiguration.class)
	public void givenBinaryFileWhenDeleteWithDifferentHashThenException()
			throws Exception {
		configManager.add(binaryConfigurationPath, binInputStream);

		configManager.delete(binaryConfigurationPath, "DifferentHash");

	}

	@Test
	public void whenAddBinaryFileWithPathOfRecursiveFolderThenFileAdded()
			throws Exception {
		configManager.add(binaryConfigurationPathRecursiveFolders, binInputStream);
		assertThat(configManager.exist(binaryConfigurationPathRecursiveFolders)).isTrue();
	}

	@Test
	public void givenPropertyFileWhenGetPropertiesAndUpdateConfigurationThenWellInstancied()
			throws Exception {

		configManager.add(propertiesConfigurationPath, properties);
		PropertiesConfiguration propertiesConfig = configManager.getProperties(propertiesConfigurationPath);
		assertThat(propertiesConfig.getProperties()).isEqualTo(properties);
		configManager.registerListener(propertiesConfigurationPath, firstListener);
		configManager.registerListener(propertiesConfigurationPath, secondListener);
		configManager.registerListener(propertiesConfigurationPath, otherEventListener);
		configManager.registerListener("otherPath", otherPathListener);
		verify(firstListener, never()).onConfigUpdated(anyString());
		verify(secondListener, never()).onConfigUpdated(anyString());

		String hash = configManager.getProperties(propertiesConfigurationPath).getHash();
		configManager.update(propertiesConfigurationPath, hash, propertiesUpdated);

		verify(firstListener).onConfigUpdated(propertiesConfigurationPath);
		verify(secondListener).onConfigUpdated(propertiesConfigurationPath);
		verify(otherPathListener, never()).onConfigUpdated(anyString());
		propertiesConfig = configManager.getProperties(propertiesConfigurationPath);
		assertThat(propertiesConfig.getProperties()).isEqualTo(propertiesUpdated);
	}

	@Test
	public void whenUpdateXMLWithAlterDocThenUpdate()
			throws Exception {

		Document document = new Document();
		document.setRootElement(new Element("rootElement"));
		configManager.add(xmlConfigurationPath, document);

		configManager.registerListener(xmlConfigurationPath, firstListener);
		configManager.registerListener(xmlConfigurationPath, secondListener);
		configManager.registerListener(xmlConfigurationPath, otherEventListener);
		configManager.registerListener("otherPath", otherPathListener);

		verify(firstListener, never()).onConfigUpdated(anyString());
		verify(secondListener, never()).onConfigUpdated(anyString());

		configManager.updateXML(xmlConfigurationPath, new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				document.getRootElement().addContent(new Element("newContent"));
			}
		});

		verify(firstListener).onConfigUpdated(xmlConfigurationPath);
		verify(secondListener).onConfigUpdated(xmlConfigurationPath);
		verify(otherPathListener, never()).onConfigUpdated(anyString());
		assertNotNull(configManager.getXML(xmlConfigurationPath).getDocument().getRootElement().getChild("newContent"));

	}

	@Test
	public void givenNoXMLFileWhenCreatingXMLDocumentIfInexistentThenCreateIt()
			throws Exception {

		configManager.createXMLDocumentIfInexistent("/zePath.xml", new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				document.addContent(new Element("anOtherTag"));
			}
		});

		assertThat(configManager.getXML("/zePath.xml").getDocument().getRootElement().getName()).isEqualTo("anOtherTag");
	}

	@Test
	// Another manager (in an other constellio instance) created the file
	public void givenConfigurationAlreadyExistsExceptionWhenAddingXMLFileWhenCreatingXMLDocumentThenReturnSilently()
			throws Exception {

		doThrow(ConfigurationAlreadyExists.class).when(configManager).add(eq("/zePath.xml"), any(Document.class));

		configManager.createXMLDocumentIfInexistent("/zePath.xml", new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				document.addContent(new Element("anOtherTag"));
			}
		});

	}

	@Test
	public void givenXMLFileWhenCreatingXMLDocumentIfInexistentThenDoNotCreateIt()
			throws Exception {

		Document document = new Document();
		document.addContent(new Element("aTag"));
		configManager.add("/zePath.xml", document);

		configManager.createXMLDocumentIfInexistent("/zePath.xml", new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				document.addContent(new Element("anOtherTag"));
			}
		});

		assertThat(configManager.getXML("/zePath.xml").getDocument().getRootElement().getName()).isEqualTo("aTag");
	}

	@Test
	public void givenNoPropertiesFileWhenCreatingPropertiesDocumentIfInexistentThenCreateIt()
			throws Exception {

		configManager.createPropertiesDocumentIfInexistent("/zePath.properties", new PropertiesAlteration() {

			@Override
			public void alter(Map<String, String> properties) {
				properties.put("anotherKey", "anotherValue");
			}
		});

		assertThat(configManager.getProperties("/zePath.properties").getProperties()).containsEntry("anotherKey", "anotherValue");
	}

	@Test
	// Another manager (in an other constellio instance) created the file
	public void givenConfigurationAlreadyExistsExceptionWhenAddingPropertiesFileWhenCreatingPropertiesDocumentThenReturnSilently()
			throws Exception {

		doThrow(ConfigurationAlreadyExists.class).when(configManager).add(eq("/zePath.properties"), any(Map.class));

		configManager.createPropertiesDocumentIfInexistent("/zePath.properties", new PropertiesAlteration() {

			@Override
			public void alter(Map<String, String> properties) {
				properties.put("anotherKey", "anotherValue");
			}
		});

	}

	@Test
	public void givenPropertyFileWhenCreatingPropertiesDocumentIfInexistentThenDoNotCreateIt()
			throws Exception {

		Map<String, String> properties = new HashMap<>();
		properties.put("aKey", "aValue");
		configManager.add("/zePath.properties", properties);

		configManager.createPropertiesDocumentIfInexistent("/zePath.properties", new PropertiesAlteration() {

			@Override
			public void alter(Map<String, String> properties) {
				properties.put("anotherKey", "anotherValue");
			}
		});

		assertThat(configManager.getProperties("/zePath.properties").getProperties()).containsEntry("aKey", "aValue");
	}

	@Test
	public void givenPropertiesWhenUpdatePropertiesThenItIsUpdated()
			throws Exception {
		String propertiesPath = "/properties.properties";

		Map<String, String> propertiesMap = new HashMap<>();
		propertiesMap.put("property1", "value1");
		propertiesMap.put("property2", "value2");
		configManager.add(propertiesPath, propertiesMap);

		Map<String, String> newPropertiesMap = new HashMap<>();
		newPropertiesMap.put("property1", "value1 updated");
		newPropertiesMap.put("property3", "value3 added");

		configManager.updateProperties(propertiesPath, newPropertiesAlteration(newPropertiesMap));

		PropertiesConfiguration propertiesConfiguration = configManager.getProperties(propertiesPath);
		assertThat(propertiesConfiguration.getProperties().get("property1")).isEqualTo("value1 updated");
		assertThat(propertiesConfiguration.getProperties().get("property2")).isEqualTo("value2");
		assertThat(propertiesConfiguration.getProperties().get("property3")).isEqualTo("value3 added");
	}

	@Test
	public void givenSomeConfigsWhenDeleteAllConfigsInCollectionThenAllConfigsAreDeleted()
			throws Exception {
		String newConfigs1Xml = "collection/newConfigs1.xml";
		String newConfigs2Xml = "collection/newConfigs2.xml";
		configManager.add(newConfigs1Xml, docXMLWithVersion);
		configManager.add(newConfigs2Xml, docXMLWithVersion);
		configManager.getXML(newConfigs1Xml);
		configManager.getXML(newConfigs2Xml);

		assertThat(configManager.getCache().containsKey(newConfigs1Xml)).isTrue();
		assertThat(configManager.getCache().containsKey(newConfigs2Xml)).isTrue();
		assertThat(configManager.exist(newConfigs1Xml)).isTrue();
		assertThat(configManager.exist(newConfigs2Xml)).isTrue();

		configManager.deleteAllConfigsIn("collection");

		assertThat(configManager.getCache().containsKey(newConfigs1Xml)).isFalse();
		assertThat(configManager.getCache().containsKey(newConfigs2Xml)).isFalse();
		assertThat(configManager.exist(newConfigs1Xml)).isFalse();
		assertThat(configManager.exist(newConfigs2Xml)).isFalse();
	}

	@SlowTest
	@Test
	public void givenXMLDocumentModifiedConcurrentlyByMultipleThreadsThenNoUpdateLost()
			throws InterruptedException {
		Document document = new Document();
		Element root = new Element("root");
		root.setAttribute("a", "0");
		root.setAttribute("b", "0");
		document.addContent(root);

		configManager.add("/zePath.xml", document);

		ThreadList<Thread> threadList = new ThreadList<>();
		for (int i = 0; i < 5; i++) {
			threadList.add(new Thread() {
				@Override
				public void run() {
					for (int j = 0; j < 500; j++) {
						configManager.updateXML("/zePath.xml", newIncrementDecrementAlteration("a", "b"));
					}
				}
			});
		}
		for (int i = 0; i < 5; i++) {
			threadList.add(new Thread() {
				@Override
				public void run() {
					for (int j = 0; j < 507; j++) {
						configManager.updateXML("/zePath.xml", newIncrementDecrementAlteration("b", "a"));
					}
				}
			});
		}

		threadList.startAll();
		threadList.joinAll();

		document = configManager.getXML("/zePath.xml").getDocument();
		assertThat(document.getRootElement().getAttributeValue("a")).isEqualTo("-35");
		assertThat(document.getRootElement().getAttributeValue("b")).isEqualTo("35");

	}

	@Test
	public void givenNoFilesInFolderWhenListThenOk()
			throws Exception {

		assertThat(configManager.list("/listFiles")).isEmpty();
	}

	@Test
	public void givenFilesInFolderWhenListThenOk()
			throws Exception {

		givenTwoFilesInFolder();

		assertThat(configManager.list("/listFiles")).hasSize(2);

	}

	private void givenTwoFilesInFolder() {
		Document document = new Document();
		Element root = new Element("root");
		root.setAttribute("a", "0");
		root.setAttribute("b", "0");
		document.addContent(root);

		configManager.add("listFiles/zePath.xml", document);
		configManager.add("listFiles/ze1Path.xml", document);
	}

	private DocumentAlteration newIncrementDecrementAlteration(final String increment, final String decrement) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				int value = Integer.valueOf(document.getRootElement().getAttributeValue(increment));
				document.getRootElement().setAttribute(increment, String.valueOf(value + 1));

				value = Integer.valueOf(document.getRootElement().getAttributeValue(decrement));
				document.getRootElement().setAttribute(decrement, String.valueOf(value - 1));
			}
		};
	}

	private PropertiesAlteration newPropertiesAlteration(final Map<String, String> newProperties) {
		return new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				for (Map.Entry<String, String> entry : newProperties.entrySet()) {
					properties.put(entry.getKey(), entry.getValue());
				}
			}
		};
	}
}
