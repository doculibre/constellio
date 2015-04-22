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
package com.constellio.model.packaging.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.sdk.tests.ConstellioTest;

public class CustomPluginsPackagingServiceAcceptanceTest extends ConstellioTest {

	int numbersOfCustomers = 3;
	FileService fileService;
	CustomPluginsPackagingService service;
	File workFolder;

	@Mock File srcFolder, tempLicenseDir;
	@Mock Customer customer1, customer2;

	private void assertCustomer1JarValid(File customer1Jar) {
		File customer1JarUnzipped = unzipInTempFolder(customer1Jar);
		File com = new File(customer1JarUnzipped, "com");
		File comCustomers = new File(com, "customers");
		File comCustomersCustomer1 = new File(comCustomers, "customer1");
		File licenseClass = new File(comCustomersCustomer1, "License.class");
		File plugin = new File(comCustomersCustomer1, "plugin");
		File somePlugin = new File(plugin, "somePlugin");
		File customer1PluginClass = new File(somePlugin, "Customer1Plugin.class");

		assertEquals(1, customer1JarUnzipped.listFiles().length);
		assertTrue(com.exists());
		assertEquals(1, com.listFiles().length);
		assertTrue(comCustomers.exists());
		assertEquals(1, comCustomers.listFiles().length);
		assertTrue(comCustomersCustomer1.exists());
		assertEquals(2, comCustomersCustomer1.listFiles().length);
		assertTrue(licenseClass.exists());
		assertTrue(plugin.exists());
		assertEquals(1, plugin.listFiles().length);
		assertTrue(somePlugin.exists());
		assertEquals(1, somePlugin.listFiles().length);
		assertTrue(customer1PluginClass.exists());
	}

	private void assertCustomer2JarValid(File customer2Jar) {
		File customer1JarUnzipped = unzipInTempFolder(customer2Jar);
		File com = new File(customer1JarUnzipped, "com");
		File comCustomers = new File(com, "customers");
		File comCustomersCustomer2 = new File(comCustomers, "customer2");
		File licenseClass = new File(comCustomersCustomer2, "License.class");

		assertEquals(1, customer1JarUnzipped.listFiles().length);
		assertTrue(com.exists());
		assertEquals(1, com.listFiles().length);
		assertTrue(comCustomers.exists());
		assertEquals(1, comCustomers.listFiles().length);
		assertTrue(comCustomersCustomer2.exists());
		assertEquals(1, comCustomersCustomer2.listFiles().length);
		assertTrue(licenseClass.exists());
	}

	private void assertCustomer3JarValid(File customer3Jar) {
		File customer3JarUnzipped = unzipInTempFolder(customer3Jar);
		File ca = new File(customer3JarUnzipped, "ca");
		File caCustomers = new File(ca, "customers");
		File caCustomersCustomer3 = new File(caCustomers, "customer3");
		File licenseClass = new File(caCustomersCustomer3, "License.class");
		File customer3PluginClass = new File(caCustomersCustomer3, "Customer3Plugin.class");

		assertEquals(1, customer3JarUnzipped.listFiles().length);
		assertTrue(ca.exists());
		assertEquals(1, ca.listFiles().length);
		assertTrue(caCustomers.exists());
		assertEquals(1, caCustomers.listFiles().length);
		assertTrue(caCustomersCustomer3.exists());
		assertEquals(2, caCustomersCustomer3.listFiles().length);
		assertTrue(licenseClass.exists());
		assertTrue(customer3PluginClass.exists());
	}

	@Before
	public void setup() {
		workFolder = newTempFolder();
		ZipService zipService = new ZipService(new IOServices(newTempFolder()));
		fileService = spy(new FileService(null) {
			@Override
			public File newTemporaryFolder(String resourceName) {
				return workFolder;
			}
		});
		service = spy(new CustomPluginsPackagingService(fileService, zipService));
	}

	@Test
	public void whenBuildingCustomerJarWithTempFolderThenDeleteFolderAfterZip() {
		assertTrue(workFolder.exists());
		File jarsDestinationFolder = newTempFolder();
		File binFolder = new File(getUnzippedResourceFile("valid_bin.zip"), "bin");

		Customer customer = mock(Customer.class);
		when(customer.getLicensePackage()).thenReturn("com.customers.customer1");
		when(customer.getCode()).thenReturn("customer1");

		String path = "com" + File.separator + "customers" + File.separator + "customer1";

		service.buildCustomerJar(customer, binFolder, jarsDestinationFolder);
		verify(fileService, times(1)).copyDirectoryWithoutExpectableIOException(new File(binFolder, path),
				new File(workFolder, path));

		assertFalse(workFolder.exists());

	}

	@Test
	public void whenPackagingValidCustomPluginsThenBuildJarsForEarchClientsBasedOnLicenses() {
		File binFolder = new File(getUnzippedResourceFile("valid_bin.zip"), "bin");
		File sourceFolder = new File(getUnzippedResourceFile("valid_src.zip"), "src");
		File jarDestinationFolder = newTempFolder();

		service.buildJars(sourceFolder, binFolder, jarDestinationFolder);

		File customer1Jar = new File(jarDestinationFolder, "constellio-customer1.jar");
		File customer2Jar = new File(jarDestinationFolder, "constellio-customer2.jar");
		File customer3Jar = new File(jarDestinationFolder, "constellio-customer3.jar");

		TestCase.assertTrue(customer1Jar.exists());
		TestCase.assertTrue(customer2Jar.exists());
		TestCase.assertTrue(customer3Jar.exists());
		TestCase.assertEquals(numbersOfCustomers, jarDestinationFolder.listFiles().length);

		assertCustomer1JarValid(customer1Jar);
		assertCustomer2JarValid(customer2Jar);
		assertCustomer3JarValid(customer3Jar);
		assertTrue(!workFolder.exists());
	}

	@Test(expected = CustomPluginsPackagingServiceException.InvalidDate.class)
	public void whenParsingLicenseFileWithInvalidDateThenThrowException() {
		File invalidLicense = getTestResourceFile("invalidDate_License.java.txt");
		service.buildCustomerFromLicense(invalidLicense);
	}

	@Test(expected = CustomPluginsPackagingServiceException.MethodCannotBeParsed.class)
	public void whenParsingLicenseFileWithInvalidMethodThenThrowException() {
		File invalidLicense = getTestResourceFile("invalidMethod_License.java.txt");
		service.buildCustomerFromLicense(invalidLicense);
	}

	@Test
	public void whenParsingValidLicenseFileThenParseAttributesSuccessfully() {
		File validLicense = getTestResourceFile("valid_License.java.txt");
		Customer customer = service.buildCustomerFromLicense(validLicense);
		assertEquals("customer1", customer.getCode());
		assertEquals("Customer 1", customer.getName());
		assertEquals("com.customers.customer1", customer.getLicensePackage());
		assertEquals(date(1, 10, 2013), customer.getInstallationDate());
		assertEquals(date(2, 11, 2014), customer.getSupportPlanStart());
		assertEquals(date(3, 12, 2015), customer.getSupportPlanEnd());
	}

	@Test
	public void whenWriteCustomerLicenseThenXmlFileHasCorrectContentAndFormat() {
		Customer customer = new Customer();
		customer.setCode("zeCode");
		customer.setName("zeName");
		customer.setPlan("zePlan");
		customer.setInstallationDate(new LocalDateTime(2014, 11, 10, 0, 0));
		customer.setSupportPlanStart(new LocalDateTime(2014, 11, 11, 0, 0));
		customer.setSupportPlanEnd(new LocalDateTime(2015, 11, 11, 0, 0));
		File tempFolder = newTempFolder();
		File xmlFile = new File(tempFolder, "zeCode.xml");

		service.writeCustomerLicense(customer, tempFolder);

		assertThat(xmlFile).exists();
		assertThat(xmlFile).hasContentEqualTo(getTestResourceFile("expectedLicense.xml"));
	}

	@Test
	public void whenBuildingCustomerLicensesThenBuildLicenseForEachCustomer() {
		doReturn(Arrays.asList(customer1, customer2)).when(service).detectCustomers(srcFolder);
		doNothing().when(service).writeCustomerLicense(any(Customer.class), any(File.class));

		service.buildLicensesFiles(srcFolder, tempLicenseDir);

		verify(service).writeCustomerLicense(customer1, tempLicenseDir);
		verify(service).writeCustomerLicense(customer2, tempLicenseDir);
	}
}
