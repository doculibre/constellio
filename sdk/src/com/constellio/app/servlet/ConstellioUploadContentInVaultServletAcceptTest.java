package com.constellio.app.servlet;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ConstellioUploadContentInVaultServletAcceptTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	UserServices userServices;
	String bobToken;
	String bobServiceKey = "bobServiceKey";
	Users users = new Users();
	private static int PORT_NUMBER = 7070;
	private static String SERVLET_URL = "uploadContentInVault";

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records));
		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		bobToken = userServices.generateToken(bobGratton);
		userServices.addUpdateUserCredential(users.bob().setServiceKey(bobServiceKey).setSystemAdminEnabled());

		startApplication();
	}


	@Test
	public void givenWebServiceIsEnabledAndValidArgumentsWhenExcuteThenFileUploadedToVault()
			throws Exception {
		File fileToSend = getTestResourceFile("fileToImport.txt");

		Map<String, Object> result = callUploadContent(bobServiceKey, users.bob().getUsername(), bobToken, zeCollection, fileToSend);
		assertThat(result.containsKey("result"));
		assertThat(result.get("result")).isNotNull();
		assertThat(result.get("result") instanceof HashMap)
		;

		Map<String, String> resultsMap = (Map<String, String>) result.get("result");
		assertThat(resultsMap.size() == 1);
		assertThat(resultsMap.containsKey("hash"));

		String uploadedFileHash = resultsMap.get("hash");
		ContentManager contentManager = getModelLayerFactory().getContentManager();
		assertThat(contentManager.doesFileExist(uploadedFileHash));
	}


	private Map<String, Object> callUploadContent(String serviceKey, String username, String token, String collection,
												  File file)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		String url = sb.append("http://localhost:")
				.append(PORT_NUMBER)
				.append("/constellio")
				.append("/rm/")
				.append(SERVLET_URL)
				.toString();
		URL serviceURL = new URL(url);

		try {
			HttpURLConnection outConnection = (HttpURLConnection) serviceURL.openConnection();

			outConnection.setRequestMethod("POST");
			outConnection.setDoInput(true);
			outConnection.setDoOutput(true);
			outConnection.setUseCaches(false);
			outConnection.setDefaultUseCaches(false);
			outConnection.addRequestProperty("fileName", file.getName());
			outConnection.addRequestProperty("username", username);
			outConnection.addRequestProperty("token", token);
			outConnection.addRequestProperty("collection", collection);
			outConnection.addRequestProperty("serviceKey", serviceKey);
			outConnection.setDoOutput(true);
			final int bufferSize = 1024 * 1024;
			outConnection.setChunkedStreamingMode(bufferSize);
			outConnection.connect();
			OutputStream output = outConnection.getOutputStream();

			FileInputStream fileInputStream = new FileInputStream(file);
			IOUtils.copy(fileInputStream, output);

			fileInputStream.close();

			InputStream input = outConnection.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(input);
			return (Map<String, Object>) ois.readObject();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;

	}


}