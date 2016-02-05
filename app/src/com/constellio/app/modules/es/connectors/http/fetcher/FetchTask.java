package com.constellio.app.modules.es.connectors.http.fetcher;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.constellio.app.modules.es.connectors.http.fetcher.config.FetcherConfig;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

public class FetchTask implements Callable<FetchedDoc> {

	private static final Logger LOG = Logger.getLogger(FetchTask.class.getName());

	private final FetchTaskCompletedHandler handler;

	private final WebClient webClient = new WebClient();
	private final FetcherConfig config;
	private final String url;

	FetchTask(FetcherConfig config, String url, FetchTaskCompletedHandler handler) {
		this.config = config;
		initWebClient(config);
		this.url = url;
		this.handler = handler;
	}

	private void initWebClient(FetcherConfig config) {
		// FIXME TEMPORARY
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setTimeout(config.getRequestTimeout());
		webClient.getOptions().setThrowExceptionOnScriptError(false);
	}

	@Override
	public FetchedDoc call()
			throws Exception {
		//Return empty doc in worse case
		FetchedDoc fetchedDoc = new FetchedDoc() {
		};
		try {
			Page fetchedPage = this.webClient.getPage(url);
			LOG.finer("Successfully fetched: " + url);
			fetchedDoc = new FetchedHttpDoc(config, url, fetchedPage);
		} catch (FailingHttpStatusCodeException e) {
			LOG.info("Http error " + e.getStatusCode() + " for: " + url);
			fetchedDoc = new FailedFetchedDoc(url, e);
		} catch (MalformedURLException e) {
			LOG.warning("Malformed url : " + this.url);
			fetchedDoc = new FailedFetchedDoc(url, e);
		} catch (IOException e) {
			fetchedDoc = new FailedFetchedDoc(url, e);
		} catch (NoSuchAlgorithmException e) {
			LOG.severe("Invalid Algorith : " + e.getMessage());
		} finally {
			if (this.handler != null) {
				this.handler.taskCompleted(fetchedDoc);
			}
		}
		return fetchedDoc;
	}
}
