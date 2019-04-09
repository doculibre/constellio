package com.constellio.app.modules.es.connectors.http.utils;

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
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.james.mime4j.io.LimitedInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;

import static org.apache.tika.io.IOUtils.toByteArray;

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
		HtmlPageParserResults htmlPageParserResults;

		if (!isNoIndexContent(page)) {
			ParsedContent parsedContent;
			Set<String> uniqueAnchors = getUniqueAnchors(page);
			byte[] content;
			try {
				content = getContent(page);

			} catch (IOException e) {
				throw new ConnectorHttpDocumentFetchException_CannotDownloadDocument(url, e);
			}

			try {
				parsedContent = fileParser.parse(new ByteArrayInputStream(content), true);
			} catch (FileParserException e) {
				throw new ConnectorHttpDocumentFetchException_CannotParseDocument(url, e);
			}

			htmlPageParserResults = finalizeHtmlPageParserResults(url, page, parsedContent, uniqueAnchors);
		} else {
			htmlPageParserResults = createNoIndexHtmlPageParserResults();
		}

		htmlPageParserResults.setNoFollow(isNoFollowLinks(page));

		return htmlPageParserResults;
	}

	private boolean isNoIndexContent(HtmlPage page) {
		return hasContentRestriction(page, "noindex");
	}

	private boolean isNoFollowLinks(HtmlPage page) {
		return hasContentRestriction(page, "nofollow");
	}

	private boolean hasContentRestriction(HtmlPage page, String typeOfRestriction) {
		DomNodeList<DomElement> metas = page.getElementsByTagName("meta");
		if (metas != null) {
			ListIterator<DomElement> listIterator = metas.listIterator();
			while (listIterator.hasNext()) {
				DomElement element = listIterator.next();
				String name = element.getAttribute("name");
				if ("robots".equalsIgnoreCase(name)) {
					String content = element.getAttribute("content");
					if (StringUtils.containsIgnoreCase(content, typeOfRestriction)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private HtmlPageParserResults createNoIndexHtmlPageParserResults() {
		try {
			String content = "NOINDEX";
			String hash = hashingService.getHashFromString(content);
			return new HtmlPageParserResults(hash, content, content, SetUtils.<String>emptySet(),
					"text/html", "fr", null);
		} catch (HashingServiceException e) {
			throw new ImpossibleRuntimeException(e);
		}
	}

	@NotNull
	private HtmlPageParserResults finalizeHtmlPageParserResults(String url, HtmlPage page, ParsedContent parsedContent,
																Set<String> uniqueAnchors)
			throws ConnectorHttpDocumentFetchException_DocumentHasNoParsedContent {
		String title;
		String parsedContentText;
		String digest;
		title = (String) parsedContent.getNormalizedProperty("title");
		parsedContentText = parsedContent.getParsedContent();

		//Try meta again
		if (StringUtils.isBlank( parsedContent.getDescription())) {
			List<DomElement> domElements = (List<DomElement>) page.getByXPath("//meta");
			for (DomElement domElement : domElements) {
				if (domElement.hasAttribute("name")) {
					String nameValue = domElement.getAttribute("name");
					if (StringUtils.containsIgnoreCase(nameValue, "description")) {
						String contentValue = domElement.getAttribute("content");
						if (StringUtils.isNotBlank(contentValue)) {
							parsedContent.setDescription(StringUtils.left(contentValue, 200));
							break;
						}
					}
				}
			}
		}

		//Extract after 1st h1
		if (StringUtils.isBlank(parsedContent.getDescription())) {
			String textAfterH1 = textAfterH1(page);
			parsedContent.setDescription(StringUtils.left(textAfterH1, 200));
		}

		String language = parsedContent.getLanguage();
		HtmlElement docElement = page.getDocumentElement();
		if (docElement != null) {
			String langAttribute = docElement.getLangAttribute();
			if (StringUtils.isNotBlank(langAttribute)) {
				Locale langLocale = new Locale(langAttribute);
				if (LocaleUtils.isAvailableLocale(langLocale)) {
					language = langLocale.getLanguage();
				}
			}
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
				parsedContent.getMimetypeWithoutCharset(), language, parsedContent.getDescription());
	}

	private String textAfterH1(HtmlPage page) {
		StringBuilder builder = new StringBuilder();
		DomNode h1Node = page.getBody().querySelector("h1");
		if (h1Node != null) {
			DomNodeList domNodes = page.getBody().querySelectorAll("*");
			ListIterator<DomNode> nodesIt = domNodes.listIterator();
			boolean afterH1 = false;
			while (nodesIt.hasNext()) {
				DomNode node = nodesIt.next();
				if (afterH1) {
					if (node.isDisplayed()) {
						String textContent = getShallowTextContent(node);
						builder.append(textContent + " ");
						if (builder.length() > 200) {
							break;
						}
					}
				} else if (node.getNodeType() == DomNode.ELEMENT_NODE && StringUtils.equalsIgnoreCase("h1", ((DomElement) node).getTagName())) {
					afterH1 = true;
				}
			}
		}
		return builder.toString();
	}

	private String getShallowTextContent(DomNode node) {
		StringBuilder builder = new StringBuilder();
		for (final DomNode child : node.getChildren()) {
			final short childType = child.getNodeType();
			if (childType ==  DomNode.TEXT_NODE) {
				builder.append(child.getNodeValue());
			}
		}
		return builder.toString().replace('\u0092','\'');
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

		private String description;

		private boolean noFollow;

		public HtmlPageParserResults(String digest, String parsedContent, String title, Set<String> linkedUrls,
									 String mimetype,
									 String language, String description) {
			this.digest = digest;
			this.parsedContent = parsedContent;
			this.title = title;
			this.linkedUrls = linkedUrls;
			this.mimetype = mimetype;
			this.language = language;
			this.description = description;
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

		public String getDescription() {
			return description;
		}

		public boolean isNoFollow() {
			return noFollow;
		}

		public void setNoFollow(boolean noFollow) {
			this.noFollow = noFollow;
		}
	}
}
