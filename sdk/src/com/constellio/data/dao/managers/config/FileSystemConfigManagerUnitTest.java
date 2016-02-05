package com.constellio.data.dao.managers.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.sdk.tests.ConstellioTest;

public class FileSystemConfigManagerUnitTest extends ConstellioTest {
	FileSystemConfigManager configManager;

	@Mock IOServices ioServices;
	@Mock HashingService hashService;
	@Mock SAXBuilder builder;

	@Mock FileOutputStream binaryOutputStream;

	PropertiesConfiguration propertiesConfiguration;
	XMLConfiguration xmlConfiguration;
	BinaryConfiguration binaryConfiguration;

	String propertiesConfigurationPath = "doc/config.properties";
	String xmlConfigurationPath = "doc/pom.xml";
	String binaryConfigurationPath = "doc/config.bin";

	String expectedContent = "Content";

	String hashOfAllFile = "hash";

	Map<String, String> properties;
	Map<String, String> propertiesAdded;
	Map<String, String> allPropertiesInNotAlphebeticalOrder;

	File configurationRoot = new File("ConfigurationRoot");

	File propertiesConfigurationFile = new File(configurationRoot, propertiesConfigurationPath);
	File xmlConfigurationFile = new File(configurationRoot, xmlConfigurationPath);
	File binaryConfigurationFile = new File(configurationRoot, binaryConfigurationPath);

	@Mock FileOutputStream fileOutputProperties;

	@Mock StreamFactory<InputStream> binStreamFactory;

	Document document;
	Element element;

	@Mock InputStream inputStream;

	@Mock DocumentAlteration alterDoc;

	@Mock Properties fileProperties;

	@Before
	public void setUp() {
		configManager = spy(new FileSystemConfigManager(configurationRoot, ioServices, hashService));

		properties = new HashMap<String, String>();
		propertiesAdded = new HashMap<String, String>();
		allPropertiesInNotAlphebeticalOrder = new HashMap<String, String>();

		propertiesConfiguration = new PropertiesConfiguration(hashOfAllFile, properties);

		properties.put("KeyOne", "ValueOne");

		propertiesAdded.put("KeyTwo", "ValueTwo");
		propertiesAdded.put("KeyThree", "ValueThree");

		allPropertiesInNotAlphebeticalOrder.put("KeyTwo", "ValueTwo");
		allPropertiesInNotAlphebeticalOrder.put("KeyThree", "ValueThree");
		allPropertiesInNotAlphebeticalOrder.put("KeyOne", "ValueOne");

		element = new Element("Element");
		document = new Document(element);

		xmlConfiguration = new XMLConfiguration(hashOfAllFile, hashOfAllFile, document);

		binaryConfiguration = new BinaryConfiguration(hashOfAllFile, binStreamFactory);
	}

	@Test
	public void whenGetPropertiesThenReturnRightPropertiesConfiguration()
			throws Exception {

		doReturn(true).when(configManager).exist(propertiesConfigurationPath);
		doReturn(fileProperties).when(configManager).newProperties();
		doReturn(properties).when(configManager).propertiesToMap(fileProperties);

		PropertiesConfiguration propertiesReturned = configManager.getProperties(propertiesConfigurationPath);

		assertThat(propertiesReturned.getProperties()).isEqualTo(properties);
	}

	@Test(expected = ConfigManagerException.OptimisticLockingConfiguration.class)
	public void whenUpdatePropertiesWithDifferentHashCodeThenException()
			throws Exception {

		doReturn(true).when(configManager).exist(propertiesConfigurationPath);
		doReturn(propertiesConfiguration).when(configManager).getProperties(propertiesConfigurationPath);

		configManager.update(propertiesConfigurationPath, "different hash", allPropertiesInNotAlphebeticalOrder);

	}

	@Test
	public void whenAddNewXMLThenNewXMLAdded()
			throws Exception {

		doReturn(false).when(configManager).exist(xmlConfigurationPath);
		doReturn(expectedContent).when(configManager).getContentOfDocument(document);

		configManager.add(xmlConfigurationPath, document);

		verify(ioServices).replaceFileContent(xmlConfigurationFile, expectedContent);
	}

	@Test
	public void whenGetXMLThenReturnRightXMLConfiguration()
			throws Exception {

		doReturn(true).when(configManager).exist(xmlConfigurationPath);
		doReturn(builder).when(configManager).newSAXBuilder();
		when(builder.build(xmlConfigurationFile)).thenReturn(document);
		doReturn("-1").when(configManager).readVersion(any(Document.class));

		XMLConfiguration xmlConfigurationReturned = configManager.getXML(xmlConfigurationPath);

		assertEquals(document, xmlConfigurationReturned.getDocument());
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

	@Test
	public void whenUpdateXMLThenXMLUpdated()
			throws Exception {
		doReturn(true).when(configManager).exist(xmlConfigurationPath);
		doReturn(xmlConfiguration).when(configManager).getXML(xmlConfigurationPath);
		doReturn("-1").when(configManager).readVersion(any(Document.class));
		configManager.update(xmlConfigurationPath, hashOfAllFile, document);

		verify(ioServices).replaceFileContent(xmlConfigurationFile, configManager.getContentOfDocument(document));
	}

	@Test(expected = ConfigManagerException.OptimisticLockingConfiguration.class)
	public void whenUpdateXMLWithDifferentHashThenException()
			throws Exception {

		doReturn(true).when(configManager).exist(xmlConfigurationPath);
		doReturn(xmlConfiguration).when(configManager).getXML(xmlConfigurationPath);

		configManager.update(xmlConfigurationPath, "different hash", document);
	}

	@Test
	public void whenUpdateXMLWithAlterDocThenUpdate()
			throws Exception {
		doReturn(true).when(configManager).exist(xmlConfigurationPath);
		doReturn(xmlConfiguration).when(configManager).getXML(xmlConfigurationPath);
		doNothing().when(configManager).update(eq(xmlConfigurationPath), anyString(), any(Document.class));

		configManager.updateXML(xmlConfigurationPath, alterDoc);

		verify(configManager).update(eq(xmlConfigurationPath), anyString(), any(Document.class));

	}

	@Test
	public void whenAddNewBinaryThenBinaryAdded()
			throws Exception {
		doReturn(false).when(configManager).exist(binaryConfigurationPath);
		when(ioServices.newFileOutputStream(binaryConfigurationFile, FileSystemConfigManager.ADD_BINARY_FILE))
				.thenReturn(binaryOutputStream);

		configManager.add(binaryConfigurationPath, inputStream);

		verify(ioServices).replaceFileContent(binaryConfigurationFile, inputStream);
	}

	@Test
	public void whenGetBinaryThenReturnTheRightBinaryConfiguration()
			throws Exception {
		byte[] bytesReturned = { 1, 2, 3 };
		doReturn(true).when(configManager).exist(binaryConfigurationPath);
		when(ioServices.readBytes(inputStream)).thenReturn(bytesReturned);
		when(ioServices.newInputStreamFactory(binaryConfigurationFile, FileSystemConfigManager.READ_BINARY_FILE))
				.thenReturn(binStreamFactory);
		when(binStreamFactory.create(SDK_STREAM)).thenReturn(inputStream);

		BinaryConfiguration binaryConfigReturned = configManager.getBinary(binaryConfigurationPath);

		assertThat(binaryConfigReturned.getInputStreamFactory()).isEqualTo(binStreamFactory);
	}

	@Test
	public void whenUpdateBinaryThenBinaryUpdated()
			throws Exception {

		doReturn(true).when(configManager).exist(binaryConfigurationPath);
		doReturn(binaryConfiguration).when(configManager).getBinary(binaryConfigurationPath);

		configManager.update(binaryConfigurationPath, hashOfAllFile, inputStream);

		verify(ioServices).replaceFileContent(binaryConfigurationFile, inputStream);
	}

}
