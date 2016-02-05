package com.constellio.app.modules.es.connectors.http.utils;

import static org.apache.tika.io.IOUtils.toByteArray;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.james.mime4j.io.LimitedInputStream;

import com.constellio.app.modules.es.connectors.http.ConnectorHttpDocumentFetchException;
import com.constellio.app.modules.es.connectors.http.ConnectorHttpDocumentFetchException.ConnectorHttpDocumentFetchException_CannotDownloadDocument;
import com.constellio.app.modules.es.connectors.http.ConnectorHttpDocumentFetchException.ConnectorHttpDocumentFetchException_CannotParseDocument;
import com.constellio.app.modules.es.connectors.http.ConnectorHttpDocumentFetchException.ConnectorHttpDocumentFetchException_DocumentHasNoParsedContent;
import com.constellio.app.modules.es.connectors.http.fetcher.UrlAcceptor;
import com.constellio.app.modules.es.connectors.http.fetcher.config.BasicUrlNormalizer;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.FileParserException;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HtmlPageParser {

	private static final long MAX_CONTENT_LENGTH = 20 * 1024 * 1024;

	HashingService hashingService;
	FileParser fileParser;
	BasicUrlNormalizer urlNormalizer;
	UrlAcceptor urlAcceptor;

	public HtmlPageParser(UrlAcceptor urlAcceptor, FileParser fileParser, HashingService hashingService) {
		this.urlAcceptor = urlAcceptor;
		this.urlNormalizer = new BasicUrlNormalizer();
		this.hashingService = hashingService;
		this.fileParser = fileParser;
	}

	public HtmlPageParserResults parse(String url, HtmlPage page)
			throws ConnectorHttpDocumentFetchException {

		ParsedContent parsedContent;
		Set<String> uniqueAnchors = getUniqueAnchors(page);
		byte[] content;
		String digest, title, parsedContentText;
		try {
			content = getContent(page);

		} catch (IOException e) {
			throw new ConnectorHttpDocumentFetchException_CannotDownloadDocument(url, e);
		}

		try {
			parsedContent = fileParser.parse(new ByteArrayInputStream(content), true);
			title = (String) parsedContent.getNormalizedProperty("title");
			parsedContentText = parsedContent.getParsedContent();
		} catch (FileParserException e) {
			throw new ConnectorHttpDocumentFetchException_CannotParseDocument(url, e);
		}

		if (parsedContentText.isEmpty()) {
			throw new ConnectorHttpDocumentFetchException_DocumentHasNoParsedContent(url);
		}

		try {
			digest = hashingService.getHashFromString(parsedContentText);
		} catch (HashingServiceException e) {
			throw new ImpossibleRuntimeException(e);
		}
		return new HtmlPageParserResults(digest, parsedContentText, title, uniqueAnchors,
				parsedContent.getMimetypeWithoutCharset(), parsedContent.getLanguage());
	}

	private byte[] getContent(HtmlPage page)
			throws IOException {
		WebResponse webResponse = page.getWebResponse();
		final String contentLenghtString = webResponse.getResponseHeaderValue("Content-Length");
		//		if (StringUtils.isNotBlank(contentLenghtString)) {
		//			final long contentLength = Long.parseLong(contentLenghtString);
		//			if (contentLength > MAX_CONTENT_LENGTH) {
		//				throw new IOException("Max content length exceeded: " + contentLength);
		//			}
		//		}
		InputStream contentStream = null;
		try {
			contentStream = new LimitedInputStream(webResponse.getContentAsStream(), MAX_CONTENT_LENGTH);
			return toByteArray(contentStream);
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}

	private String getTitle(HtmlPage page) {
		return null;
	}

	private Set<String> getUniqueAnchors(HtmlPage page) {
		Set<String> uniqueAnchorUrls = new HashSet<>();

		for (HtmlAnchor anchor : page.getAnchors()) {
			String anchorUrl = null;
			try {
				final String unNormalizedAnchorUrl = HtmlAnchorUtils.getUrl(anchor);
				anchorUrl = urlNormalizer.normalize(unNormalizedAnchorUrl);
			} catch (Exception e) {
				//Normal, just skipping this url
			}
			if (StringUtils.isNotBlank(anchorUrl) && !anchorUrl.equals(page.getUrl().toString()) && urlAcceptor
					.isAccepted(anchorUrl)) {
				uniqueAnchorUrls.add(anchorUrl);
			}
		}

		return uniqueAnchorUrls;
	}

	public static class HtmlPageParserResults {

		private String parsedContent;

		private String title;

		private Set<String> linkedUrls;

		private String language;

		private String digest;

		private String mimetype;

		public HtmlPageParserResults(String digest, String parsedContent, String title, Set<String> linkedUrls, String mimetype,
				String language) {
			this.digest = digest;
			this.parsedContent = parsedContent;
			this.title = title;
			this.linkedUrls = linkedUrls;
			this.mimetype = mimetype;
			this.language = language;
		}

		public String getDigest() {
			return digest;
		}

		public String getParsedContent() {
			return parsedContent;
		}

		public String getMimetype() {
			return mimetype;
		}

		public Set<String> getLinkedUrls() {
			return linkedUrls;
		}

		public String getTitle() {
			return title;
		}

		public String getLanguage() {
			return language;
		}
	}
}
