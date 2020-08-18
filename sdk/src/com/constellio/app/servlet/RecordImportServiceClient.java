package com.constellio.app.servlet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_OK;

public class RecordImportServiceClient {
	public static final String COLLECTION = "collection";
	public static final String USERNAME = "username";
	public static final String TOKEN_OR_PASSWORD = "token";
	public static final String SERVICEKEY = "serviceKey";

	private HttpConfigurations config;

	public RecordImportServiceClient(HttpConfigurations config) {
		this.config = config;
	}


	public String callService(String servletPath, Map<String, Object> inParams, boolean timeout) {
		HttpURLConnection outConnection = setupConnection(servletPath, false);

		try {
			outConnection.setRequestMethod("POST");
			outConnection.setRequestProperty("content-type", "multipart/form-data");
			outConnection.setDoInput(true);
			outConnection.setDoOutput(true);
			outConnection.setUseCaches(false);
			outConnection.setDefaultUseCaches(false);
			outConnection.addRequestProperty("dataType", (String) inParams.get("dataType"));
			outConnection.addRequestProperty(USERNAME, config.getUsername());
			outConnection.addRequestProperty(TOKEN_OR_PASSWORD, config.getToken());
			outConnection.addRequestProperty(COLLECTION, config.getCollection());
			outConnection.addRequestProperty(SERVICEKEY, config.getServiceKey());
			outConnection.setDoOutput(true);

			File fileToSend = new File((String) inParams.get("importFile"));
			if (fileToSend != null) {
				byte[] file = FileUtils.readFileToByteArray(fileToSend);
				outConnection.getOutputStream().write(file);
			}

			outConnection.connect();
			int responseCode = outConnection.getResponseCode();
			String response = "";
			if (responseCode == SC_OK) {

			} else {

				byte[] responseBytes = IOUtils.toByteArray(outConnection.getErrorStream());
				response = new String(responseBytes, "ISO-8859-1");
			}
			return response;
		} catch (IOException e) {
			return null;
		}
	}


	private String readResponse(InputStream inputStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String output;
		while ((output = br.readLine()) != null) {
			sb.append(output);
		}
		return sb.toString();
	}

	private HttpURLConnection setupConnection(String servletPath, boolean timeout) {

		String baseURL = config.getAdress();
		if (baseURL == null) {
			throw new RuntimeException();
		}
		HttpURLConnection outConnection = null;
		try {

			if (!baseURL.endsWith("/")) {
				baseURL += "/";
			}
			baseURL += "rm";
			URL serviceURL = new URL(baseURL + servletPath);
			try {
				outConnection = (HttpURLConnection) serviceURL.openConnection();
				if (timeout) {
					outConnection.setConnectTimeout(2000);
				}
				return outConnection;
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public String uploadXLSXFile(String file) {
		Map<String, Object> inParams = new HashMap<>();
		inParams.put("importFile", file);
		inParams.put("dataType", "xlsx");
		return callService("/uploadRecords", inParams, true);
	}

}
