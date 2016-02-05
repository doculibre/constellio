package com.constellio.app.modules.es.connectors.http.fetcher;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.james.mime4j.io.LimitedInputStream;

import com.constellio.app.modules.es.connectors.http.fetcher.config.FetcherConfig;
import com.constellio.app.modules.es.connectors.http.utils.DigestUtil;
import com.constellio.app.modules.es.connectors.http.utils.HtmlAnchorUtils;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class FetchedHttpDoc implements FetchedDoc {

	private static final long MAX_CONTENT_LENGTH = 5 * 1024 * 1024;

	private static final Logger LOG = Logger.getLogger(FetchedHttpDoc.class.getName());

	private final FetcherConfig fetcherHttpConfig;
	private final String url;
	private final Page fetchedPage;
	private final Set<String> uniqueAnchorUrls;
	private byte[] content;
	private String contentType;
	private String digest;

	public FetchedHttpDoc(FetcherConfig fetcherHttpConfig, String url, Page fetchedPage)
			throws IOException, NoSuchAlgorithmException {
		this.url = url;
		this.fetcherHttpConfig = fetcherHttpConfig;
		this.fetchedPage = fetchedPage;
		this.uniqueAnchorUrls = getUniqueAnchors(fetchedPage);
		WebResponse webResponse = fetchedPage.getWebResponse();
		this.content = getContent(webResponse);
		this.contentType = webResponse.getContentType();
		if (fetchedPage instanceof HtmlPage) {
			this.digest = DigestUtil.digest(((HtmlPage) fetchedPage).asText().getBytes());
		} else {
			this.digest = DigestUtil.digest(this.content);
		}
	}

	private Set<String> getUniqueAnchors(final Page fetchedPage) {
		final Set<String> uniqueAnchorUrls = new HashSet<String>();
		if (fetchedPage instanceof HtmlPage) {
			HtmlPage htmlPage = (HtmlPage) fetchedPage;
			for (HtmlAnchor anchor : htmlPage.getAnchors()) {
				if (HtmlAnchorUtils.isMailto(anchor)) {
					// Ignore
				} else if (HtmlAnchorUtils.isJavascript(anchor)) {
					// FIXME Deal with Javascript links
					// fetchedPage = this.link.getHtmlAnchor().click();
				} else {
					String anchorUrl = null;
					try {
						final String unNormalizedAnchorUrl = HtmlAnchorUtils.getUrl(anchor);
						anchorUrl = this.fetcherHttpConfig.normalize(unNormalizedAnchorUrl);
					} catch (Exception e) {
						LOG.fine("Rejected anchor url :" + anchorUrl + " from " + this.url);
						continue;
					}
					if (StringUtils.isNotBlank(anchorUrl) && fetcherHttpConfig.isAccepted(anchorUrl)) {
						uniqueAnchorUrls.add(anchorUrl);
					}
				}
			}

		}
		return uniqueAnchorUrls;
	}

	private byte[] getContent(WebResponse response)
			throws IOException {
		final String contentLenghtString = response.getResponseHeaderValue("Content-Length");
		if (StringUtils.isNotBlank(contentLenghtString)) {
			final long contentLength = Long.parseLong(contentLenghtString);
			if (contentLength > MAX_CONTENT_LENGTH) {
				throw new IOException("Max content length exceeded: " + contentLength);
			}
		}
		InputStream contentStream = null;
		try {
			contentStream = new LimitedInputStream(response.getContentAsStream(), MAX_CONTENT_LENGTH);
			return IOUtils.toByteArray(contentStream);
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}

	public Page getFetchedPage() {
		return fetchedPage;
	}

	public Set<String> getUniqueAnchorUrls() {
		return uniqueAnchorUrls;
	}

	public byte[] getContent() {
		return content;
	}

	public String getContentType() {
		return contentType;
	}

	public String getDigest() {
		return digest;
	}

	public String getUrl() {
		return url;
	}
}
