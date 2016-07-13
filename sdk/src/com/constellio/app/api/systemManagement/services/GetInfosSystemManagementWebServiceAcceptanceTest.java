package com.constellio.app.api.systemManagement.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.conf.FoldersLocator;
import com.constellio.sdk.tests.ConstellioTest;

public class GetInfosSystemManagementWebServiceAcceptanceTest extends ConstellioTest {

	public static String VALID_LICENSE = "Jrx+t5qkn74ZHH26dq3gY+ZMDtNn+/7HhiWGKKSzq80TXbpVlOPaGbcqyb132kPysXMQxx98FDjfQhwJmyje0UbjVvCP9FkIlPzVo07P3f8tnd4aV3gqfWO4Qd9VKc2GBdx/Dg3hSCy2+a+WsOBFnizE0mVhtxZTyP9uAqpL/ZBoOr6hyiu0ZwBCApbyVWZeL80oZZvvyBvBTZodU5Mg/ATFircQvfeVTWlq/VhxlOvtzIONXLyCrfra/TsJXGRHz9Q62VCKXd5NhEGSX99xXa3FB07ckyLCa2sJORIMwQwqanfs8WjJBR9z1CCF8+w5jTqJl5bJoF9k0/jycHtDoQ==";

	String url;

	Map<String, String> arguments = new HashMap<>();

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection());

		url = startApplication();

		deleteUploadedLicense();
		getAppLayerFactory();
	}

	@Test
	public void givenUploadedLicenseThenAuthenticateIfSameLicense()
			throws Exception {
		getAppLayerFactory().newApplicationService().storeLicense(getTestResourceFile("license.xml"));

		Document document = new AdminSystemManagementCaller().call(url, "getInfos", VALID_LICENSE, arguments);

		assertThat(document.getRootElement().getChild("records").getText()).isEqualTo("7");

		try {
			new AdminSystemManagementCaller().call(url, "getInfos", VALID_LICENSE.replace("4", "3"), arguments);
			fail("Exception expected");
		} catch (AdminSystemManagementCaller.ServerUnauthorizedAccessRuntimeException e) {
			//OK
		}

		try {
			new AdminSystemManagementCaller().call(url, "getInfos", "", arguments);
			fail("Exception expected");
		} catch (AdminSystemManagementCaller.ServerUnauthorizedAccessRuntimeException e) {
			//OK
		}

		try {
			new AdminSystemManagementCaller().call(url, "getInfos", null, arguments);
			fail("Exception expected");
		} catch (AdminSystemManagementCaller.ServerUnauthorizedAccessRuntimeException e) {
			//OK
		}

	}

	@Test
	public void givenNoUploadedLicenseThenNeverAuthenticate()
			throws Exception {

		try {
			new AdminSystemManagementCaller().call(url, "getInfos", VALID_LICENSE, arguments);
			fail("Exception expected");
		} catch (AdminSystemManagementCaller.ServerUnauthorizedAccessRuntimeException e) {
			//OK
		}

		try {
			new AdminSystemManagementCaller().call(url, "getInfos", VALID_LICENSE.replace("4", "3"), arguments);
			fail("Exception expected");
		} catch (AdminSystemManagementCaller.ServerUnauthorizedAccessRuntimeException e) {
			//OK
		}

		try {
			new AdminSystemManagementCaller().call(url, "getInfos", "", arguments);
			fail("Exception expected");
		} catch (AdminSystemManagementCaller.ServerUnauthorizedAccessRuntimeException e) {
			//OK
		}

		try {
			new AdminSystemManagementCaller().call(url, "getInfos", null, arguments);
			fail("Exception expected");
		} catch (AdminSystemManagementCaller.ServerUnauthorizedAccessRuntimeException e) {
			//OK
		}

	}
	//---------------------------

	@After
	public void tearDown()
			throws Exception {
		deleteUploadedLicense();
		stopApplication();

	}

	private void deleteUploadedLicense() {
		new File(new FoldersLocator().getConstellioProject(), "conf" + File.separator + "license.xml").delete();
	}
}
