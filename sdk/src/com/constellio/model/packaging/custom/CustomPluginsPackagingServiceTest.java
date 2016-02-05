package com.constellio.model.packaging.custom;

import static com.constellio.model.packaging.custom.CustomPluginsPackagingService.CUSTOMER_CODE;
import static com.constellio.model.packaging.custom.CustomPluginsPackagingService.CUSTOMER_NAME;
import static com.constellio.model.packaging.custom.CustomPluginsPackagingService.INSTALLATION_DATE;
import static com.constellio.model.packaging.custom.CustomPluginsPackagingService.SUPPORT_PLAN_END;
import static com.constellio.model.packaging.custom.CustomPluginsPackagingService.SUPPORT_PLAN_START;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.sdk.tests.ConstellioTest;

public class CustomPluginsPackagingServiceTest extends ConstellioTest {

	private static final int NUMBER_OF_CUSTOMERS = 3;
	private final File license1 = aFile();
	private final File license2 = aFile();
	private final File license3 = aFile();
	private final List<File> licenseFiles = new ArrayList<File>(Arrays.asList(license1, license2, license3));
	private final File sourceFolder = aFile();
	private final File binFolder = aFile();
	private final File jarDestinationFolder = aFile();
	private final File aLicense = aFile();
	private final String theLicenseContent = aString();
	private final String theClientCode = aString();
	private final String theClientName = aString();
	private final String theClientPackage = aString();
	private final LocalDateTime theClientInstallationDate = aDateTime();
	private final LocalDateTime theClientSupportPlanStart = aDateTime();
	private final LocalDateTime theClientSupportPlanEnd = aDateTime();
	private Customer customer1;
	private Customer customer2;
	private Customer customer3;
	private List<Customer> customers;
	private FileService fileService;
	private ZipService zipService;
	private CustomPluginsPackagingService service;

	@Before
	public void setup() {

		fileService = mock(FileService.class);
		zipService = mock(ZipService.class);
		customer1 = mock(Customer.class);
		customer2 = mock(Customer.class);
		customer3 = mock(Customer.class);
		customers = new ArrayList<Customer>(Arrays.asList(customer1, customer2, customer3));

		service = spy(new CustomPluginsPackagingService(fileService, zipService));

	}

	@Test
	public void whenBuildingJarsThenBuildForEveryClients() {
		doReturn(customers).when(service).detectCustomers(sourceFolder);
		doNothing().when(service).buildCustomerJar(any(Customer.class), eq(binFolder), eq(jarDestinationFolder));

		service.buildJars(sourceFolder, binFolder, jarDestinationFolder);

		verify(service, times(1)).buildCustomerJar(customer1, binFolder, jarDestinationFolder);
		verify(service, times(1)).buildCustomerJar(customer2, binFolder, jarDestinationFolder);
		verify(service, times(1)).buildCustomerJar(customer3, binFolder, jarDestinationFolder);
	}

	@Test
	public void whenCreatingCustomerFromLicenseFileThenReadMethods()
			throws IOException {
		when(fileService.readFileToStringWithoutExpectableIOException(aLicense)).thenReturn(theLicenseContent);
		doReturn(theClientCode).when(service).extractLicenseAttribute(aLicense, theLicenseContent, CUSTOMER_CODE);
		doReturn(theClientName).when(service).extractLicenseAttribute(aLicense, theLicenseContent, CUSTOMER_NAME);
		doReturn(theClientPackage).when(service).extractLicensePackage(aLicense, theLicenseContent);
		doReturn(theClientInstallationDate).when(service).extractLicenseDateAttribute(aLicense, theLicenseContent,
				INSTALLATION_DATE);
		doReturn(theClientSupportPlanStart).when(service).extractLicenseDateAttribute(aLicense, theLicenseContent,
				SUPPORT_PLAN_START);
		doReturn(theClientSupportPlanEnd).when(service)
				.extractLicenseDateAttribute(aLicense, theLicenseContent, SUPPORT_PLAN_END);

		Customer customer = service.buildCustomerFromLicense(aLicense);

		verify(fileService, times(1)).readFileToStringWithoutExpectableIOException(aLicense);
		assertEquals(theClientCode, customer.getCode());
		assertEquals(theClientName, customer.getName());
		assertEquals(theClientPackage, customer.getLicensePackage());
		assertEquals(theClientInstallationDate, customer.getInstallationDate());
		assertEquals(theClientSupportPlanStart, customer.getSupportPlanStart());
		assertEquals(theClientSupportPlanEnd, customer.getSupportPlanEnd());
	}

	@Test
	public void whenDetectingCustomersThenScanForLicenseFilesInSourceFolder() {
		when(fileService.listRecursiveFilesWithName(any(File.class), anyString())).thenReturn(licenseFiles);
		doReturn(customer1).when(service).buildCustomerFromLicense(license1);
		doReturn(customer2).when(service).buildCustomerFromLicense(license2);
		doReturn(customer3).when(service).buildCustomerFromLicense(license3);

		List<Customer> returnedCustomers = service.detectCustomers(sourceFolder);

		verify(fileService, times(1)).listRecursiveFilesWithName(sourceFolder, "License.java");
		verify(service, times(1)).buildCustomerFromLicense(license1);
		verify(service, times(1)).buildCustomerFromLicense(license2);
		verify(service, times(1)).buildCustomerFromLicense(license3);

		assertEquals(NUMBER_OF_CUSTOMERS, returnedCustomers.size());
		assertTrue(returnedCustomers.contains(customer1));
		assertTrue(returnedCustomers.contains(customer2));
		assertTrue(returnedCustomers.contains(customer3));
	}

}
