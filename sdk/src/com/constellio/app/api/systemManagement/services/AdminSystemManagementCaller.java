package com.constellio.app.api.systemManagement.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;

public class AdminSystemManagementCaller {

	public Document call(String url, String servlet, String certificat, Map<String, String> arguments) {

		StringBuilder urlBuilder = new StringBuilder(url);

		if (!url.endsWith("/")) {
			urlBuilder.append("/");
		}

		urlBuilder.append("systemManagement/").append(servlet);
		if (certificat != null) {
			urlBuilder.append("?certificate=").append(certificat.replace("/", "_").replace("+", "-"));
		}
		for (Map.Entry<String, String> entry : arguments.entrySet()) {
			urlBuilder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
		}

		WebClient webClient = new WebClient();
		WebRequest webRequest = null;
		try {
			System.out.println("**Query : \n" + urlBuilder.toString());
			webRequest = new WebRequest(new URL(urlBuilder.toString()), HttpMethod.GET);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		try {
			Page page = webClient.getPage(webRequest);
			String response = page.getWebResponse().getContentAsString();
			System.out.println("**Response : \n" + response);

			Document document = getDocumentFromString(response);

			Element errorElement = document.getRootElement().getChild("error");
			if (errorElement != null) {
				throw new ServerErrorRuntimeException(errorElement.getText());
			}

			Element unauthorizedAccessElement = document.getRootElement().getChild("unauthorizedAccess");
			if (unauthorizedAccessElement != null) {
				throw new ServerUnauthorizedAccessRuntimeException(url, unauthorizedAccessElement.getText());
			}

			Element exceptionElement = document.getRootElement().getChild("exception");
			if (exceptionElement != null) {
				throw new ServerErrorRuntimeException(exceptionElement.getText());
			}

			return document;

		} catch (IOException e) {
			throw new CommunicationFailedExceptionRuntimeException(urlBuilder.toString());
		}

	}

	org.jdom2.Document getDocumentFromString(String string) {
		SAXBuilder builder = new SAXBuilder();
		try {

			return builder.build(new StringReader(string));
		} catch (JDOMException | IOException e) {
			throw new RuntimeException("build Document JDOM2 from content '" + string + "' failed", e);
		}
	}

	public static class CommunicationFailedExceptionRuntimeException extends RuntimeException {

		String url;

		public CommunicationFailedExceptionRuntimeException(String url) {
			super("Communication error : " + url);
			this.url = url;
		}

		public String getUrl() {
			return url;
		}
	}

	public static class ServerErrorRuntimeException extends RuntimeException {

		String error;

		public ServerErrorRuntimeException(String error) {
			super("Server error : " + error);
			this.error = error;
		}
	}

	public static class ServerUnauthorizedAccessRuntimeException extends RuntimeException {

		String url;

		public ServerUnauthorizedAccessRuntimeException(String url, String problem) {
			super("Failed to connect to '" + url + "' : " + problem);
			this.url = url;
		}
	}

	public static class ServerExceptionRuntimeException extends RuntimeException {

		String stacktrace;

		public ServerExceptionRuntimeException(String stacktrace) {
			super("Server failure : " + stacktrace);
			this.stacktrace = stacktrace;
		}
	}
}
