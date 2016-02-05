package com.constellio.app.modules.es.connectors.http.fetcher;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.constellio.app.modules.es.connectors.http.utils.DigestUtil;

public class FailedFetchedDoc implements FetchedDoc {
	private final String url;
	private final byte[] content;
	private final String contentType;
	private final String digest;

	public FailedFetchedDoc(String url, Exception e)
			throws IOException, NoSuchAlgorithmException {
		this.url = url;
		this.content = e.getMessage().getBytes();
		this.contentType = "error";
		this.digest = DigestUtil.digest(this.content);
	}

	public String getUrl() {
		return url;
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
}
