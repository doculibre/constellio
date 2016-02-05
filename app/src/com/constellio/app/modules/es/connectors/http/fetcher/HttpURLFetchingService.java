package com.constellio.app.modules.es.connectors.http.fetcher;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.CredentialsProvider;

import com.constellio.app.modules.es.connectors.http.fetcher.URLFetchingServiceRuntimeException.URLFetchingServiceRuntimeException_HttpError;
import com.constellio.app.modules.es.connectors.http.fetcher.URLFetchingServiceRuntimeException.URLFetchingServiceRuntimeException_IOException;
import com.constellio.app.modules.es.connectors.http.fetcher.URLFetchingServiceRuntimeException.URLFetchingServiceRuntimeException_MalformedUrl;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.RefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;

public class HttpURLFetchingService implements AutoCloseable {

	private final WebClient webClient;

	public HttpURLFetchingService(int timeout) {
		this(timeout, null);
	}

	public HttpURLFetchingService(int timeout, CredentialsProvider credentialsProvider) {
		webClient = new WebClient();
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setTimeout(timeout);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		if (credentialsProvider != null) {
			webClient.setCredentialsProvider(credentialsProvider);
		}
	}

	public Page fetch(String url) {

		RefreshHandler emptyRefreshHandler = new RefreshHandler() {
			public void handleRefresh(final Page page, final URL url, final int seconds)
					throws IOException {

			}
		};

		//TODO Francis : Add a test for this (was added during a fetch of wikipedia)
		//webClient.setRefreshHandler(emptyRefreshHandler);
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.getOptions().setRedirectEnabled(true);
		try {
			return webClient.getPage(url);
		} catch (FailingHttpStatusCodeException e) {
			throw new URLFetchingServiceRuntimeException_HttpError(url, e);

		} catch (ConnectException e) {
			throw new URLFetchingServiceRuntimeException_IOException(url, e);

		} catch (MalformedURLException e) {
			throw new URLFetchingServiceRuntimeException_MalformedUrl(url, e);

		} catch (IOException e) {
			throw new URLFetchingServiceRuntimeException_IOException(url, e);

		}
	}

	@Override
	public void close() {
		webClient.closeAllWindows();
	}
}
