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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;

//import org.apache.james.mime4j.dom.datetime.DateTime;

public class CustomPluginsPackagingService {

	private static final String TEMP_FOLDER = "CustomPluginsPackagingService_TempFolder";

	public static final String CUSTOMER_CODE = "getCustomerCode()";
	public static final String CUSTOMER_NAME = "getCustomerName()";
	public static final String INSTALLATION_DATE = "getInstallationDateYYYYMMDD()";
	public static final String SUPPORT_PLAN_START = "getSupportPlanStartYYYYMMDD()";
	public static final String SUPPORT_PLAN_END = "getSupportPlanEndYYYYMMDD()";

	private final FileService fileService;
	private final ZipService zipService;

	public CustomPluginsPackagingService(FileService fileService, ZipService zipService) {
		this.fileService = fileService;
		this.zipService = zipService;
	}

	public void buildJars(File sourceFolder, File binFolder, File jarsDestinationFolder) {
		for (Customer customer : detectCustomers(sourceFolder)) {
			buildCustomerJar(customer, binFolder, jarsDestinationFolder);
		}
	}

	public List<Customer> detectCustomers(File sourceFolder) {
		List<Customer> customers = new ArrayList<Customer>();
		for (File licenseFile : fileService.listRecursiveFilesWithName(sourceFolder, "License.java")) {
			customers.add(buildCustomerFromLicense(licenseFile));
		}
		return customers;

	}

	public Customer buildCustomerFromLicense(File licenseFile) {
		String licenseContent = fileService.readFileToStringWithoutExpectableIOException(licenseFile);

		String code = extractLicenseAttribute(licenseFile, licenseContent, CUSTOMER_CODE);
		String name = extractLicenseAttribute(licenseFile, licenseContent, CUSTOMER_NAME);
		String thePackage = extractLicensePackage(licenseFile, licenseContent);
		LocalDateTime installationDate = extractLicenseDateAttribute(licenseFile, licenseContent, INSTALLATION_DATE);
		LocalDateTime supportPlanStart = extractLicenseDateAttribute(licenseFile, licenseContent, SUPPORT_PLAN_START);
		LocalDateTime supportPlanEnd = extractLicenseDateAttribute(licenseFile, licenseContent, SUPPORT_PLAN_END);

		Customer customer = new Customer();
		customer.setCode(code);
		customer.setName(name);
		customer.setLicensePackage(thePackage);
		customer.setInstallationDate(installationDate);
		customer.setSupportPlanStart(supportPlanStart);
		customer.setSupportPlanEnd(supportPlanEnd);
		return customer;
	}

	public void buildCustomerJar(Customer customer, File binFolder, File jarDestinationFolder) {

		File customerJar = new File(jarDestinationFolder, "constellio-" + customer.getCode() + ".jar");

		File tempFolder = null;
		try {
			String customerPackagePath = customer.getLicensePackage().replace(".", File.separator);
			tempFolder = fileService.newTemporaryFolder(TEMP_FOLDER);
			File classPackage = new File(binFolder, customerPackagePath);
			File clientWorkPackage = new File(tempFolder, customerPackagePath);
			clientWorkPackage.mkdirs();
			clientWorkPackage.mkdir();
			fileService.copyDirectoryWithoutExpectableIOException(classPackage, clientWorkPackage);
			try {
				zipService.zip(customerJar, Arrays.asList(tempFolder.listFiles()));
			} catch (ZipServiceException e) {
				throw new CustomPluginsPackagingServiceRuntimeException.CannotBuildCustumerJar(customer.getName(),
						binFolder.getPath(), jarDestinationFolder.getPath(), e);
			}
		} finally {
			fileService.deleteDirectoryWithoutExpectableIOException(tempFolder);
		}

	}

	public String extractLicenseAttribute(File licenseFile, String theLicenseContent, String method) {
		List<String> lines = Arrays.asList(theLicenseContent.split("\n"));
		int index = 0;
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.contains(method)) {
				index = i;
			}
		}
		String value = lines.get(index + 1).replace("\t", "").trim();
		if (!value.startsWith("return \"") || !value.endsWith("\";")) {
			throw new CustomPluginsPackagingServiceException.MethodCannotBeParsed(licenseFile, method);
		} else {
			return value.replace("return \"", "").replace("\";", "");
		}
	}

	public String extractLicensePackage(File licenseFile, String theLicenseContent) {
		List<String> lines = Arrays.asList(theLicenseContent.split("\n"));
		int index = 0;
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("package")) {
				index = i;
			}
		}
		return lines.get(index).replace("package ", "").replace(";", "").trim();
	}

	public LocalDateTime extractLicenseDateAttribute(File licenseFile, String theLicenseContent, String method) {
		String value = extractLicenseAttribute(licenseFile, theLicenseContent, method);
		try {
			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
			return LocalDateTime.parse(value.replace("-", ""), formatter);
		} catch (IllegalArgumentException e) {
			throw new CustomPluginsPackagingServiceException.InvalidDate(licenseFile, method, e);
		}

	}

	public void buildLicensesFiles(File src, File tempLicensesDir) {
		List<Customer> customers = detectCustomers(src);
		for (Customer customer : customers) {
			writeCustomerLicense(customer, tempLicensesDir);
		}
	}

	void writeCustomerLicense(Customer customer, File tempLicensesDir) {
		Writer writer = null;
		try {
			Element client = new Element("client");

			Document document = new Document(client);

			client.addContent(new Element("code").setText(customer.getCode()));
			client.addContent(new Element("name").setText(customer.getName()));
			client.addContent(new Element("plan").setText(customer.getPlan()));
			client.addContent(new Element("installationDate").setText(format(customer.getInstallationDate())));
			client.addContent(new Element("supportPlanStart").setText(format(customer.getSupportPlanStart())));
			client.addContent(new Element("supportPlanEnd").setText(format(customer.getSupportPlanEnd())));

			XMLOutputter xmlOutput = new XMLOutputter();

			xmlOutput.setFormat(Format.getPrettyFormat().setIndent("    "));
			writer = new FileWriter(new File(tempLicensesDir, customer.getCode() + ".xml"));
			xmlOutput.output(document, writer);

		} catch (IOException io) {
			throw new CustomPluginsPackagingServiceRuntimeException.CannotWriteCustumerLicense(customer.getName(),
					tempLicensesDir.getPath(), io);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	private String format(LocalDateTime localDateTime) {
		// return new SimpleDateFormat("yyyy-MM-dd").format(localDateTime);
		return localDateTime.toString("yyyy-MM-dd");

	}

}
