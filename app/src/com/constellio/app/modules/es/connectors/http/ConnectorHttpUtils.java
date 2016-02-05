package com.constellio.app.modules.es.connectors.http;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.constellio.app.modules.es.connectors.http.ConnectorHttpRuntimeException.ConnectorHttpRuntimeException_CannotGetAbsoluteHref;
import com.constellio.data.io.services.facades.IOServices;

public class ConnectorHttpUtils {

	private static final String READ_BINARY_CONTENT_RESOURCE = "ConnectorHttpUtils-ReadBinaryContent";

	public static String toAbsoluteHRef(String currentUrl, String href) {
		try {
			if (StringUtils.isBlank(href) || StringUtils.isBlank(currentUrl) || href.startsWith("http")) {
				return href;
			} else if (href.startsWith("/")) {
				int firstSlashIndex = currentUrl.indexOf("/", 7);
				if (firstSlashIndex == -1) {
					return href;
				} else {
					return currentUrl.substring(0, firstSlashIndex) + href;
				}
			} else {
				List<String> parts = new ArrayList<>(asList(currentUrl.split("/")));
				while (href.startsWith("../")) {
					parts.remove(parts.size() - 1);
					href = href.substring(3);
				}
				return StringUtils.join(parts, "/") + "/" + href;
			}

		} catch (Exception e) {
			throw new ConnectorHttpRuntimeException_CannotGetAbsoluteHref(currentUrl, href);
		}
	}

	public static FetchedDocumentContent fetch(String url)
			throws IOException {
		FetchedDocumentContent fetchedDocumentContent = new FetchedDocumentContent();
		Response response = Jsoup.connect(url).execute();
		//String contentType = response.contentType();
		fetchedDocumentContent.document = response.parse();
		fetchedDocumentContent.title = fetchedDocumentContent.document.title();
		String[] urlParts = url.split("/");
		fetchedDocumentContent.fileName = urlParts[urlParts.length - 1];

		return fetchedDocumentContent;
	}

	public static class FetchedDocumentContent {

		private Document document;
		private String title;
		private String fileName;

		public InputStream newInputStream(IOServices ioServices) {
			return ioServices.newByteInputStream(document.text().getBytes(), READ_BINARY_CONTENT_RESOURCE);
		}

		public long getContentLength() {
			return document.text().length();
		}

		public Document getDocument() {
			return document;
		}

		public String getTitle() {
			return title;
		}

		public String getFileName() {
			return fileName;
		}

		public String baseUri() {
			return document.baseUri();
		}
	}
}
