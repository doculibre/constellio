/**
 * Copyright © 2010 DocuLibre inc.
 *
 * This file is part of Constellio.
 *
 * Constellio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Constellio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Constellio.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.constellio.app.modules.es.connectors.http.fetcher.config;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

/**
 * Fetcher configuration.
 *
 * @author Nicolas Bélisle (nicolas.belisle@doculibre.com)
 *
 */
public class FetcherConfig {

	private final String[] includePatterns;

	private final String[] excludePatterns;

	private final UrlNormalizer[] normalizers;

	private final int pauseTime;

	private final int requestTimeout;

	private final int depth;

	private final double delay;

	private final int threads;

	private final String[] startUrls;

	private final String[] onDemandUrls;

	private final String robotsUserAgent;

	FetcherConfig(String[] includePatterns, String[] excludePatterns, UrlNormalizer[] normalizers, int pauseTime,
			int requestTimeout, int depth, double delay, int threads, String[] startUrls, String[] onDemandUrls,
			String robotsUserAgent) {
		this.includePatterns = includePatterns;
		for (String includePattern : includePatterns) {
			Pattern.compile(includePattern);
		}
		this.excludePatterns = excludePatterns;
		if (excludePatterns != null) {
			for (String excludePattern : excludePatterns) {
				Pattern.compile(excludePattern);
			}
		}
		this.normalizers = normalizers;
		this.pauseTime = pauseTime;
		this.requestTimeout = requestTimeout;
		this.depth = depth;
		this.delay = delay;
		if (threads < 1) {
			throw new IllegalArgumentException("maxThread must be greather than one, actual value: " + threads);
		}
		this.threads = threads;
		this.startUrls = startUrls;
		this.onDemandUrls = onDemandUrls;
		this.robotsUserAgent = robotsUserAgent;
	}

	/**
	 * We first look at rejected/black URLs, then if nothing matches look for
	 * accepted/white URLs. If nothing matches, the URL is rejected.
	 *
	 * @param url
	 * @return
	 */
	public boolean isAccepted(String url) {
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			return false;
		}
		if (StringUtils.isBlank(url)) {
			return false;
		}
		if (excludePatterns != null) {
			for (String blackRegEx : excludePatterns) {
				Pattern pattern = Pattern.compile(blackRegEx);
				Matcher matcher = pattern.matcher(url);
				if (matcher.find()) {
					return false;
				}
			}
		}
		for (String whiteRegEx : includePatterns) {
			Pattern pattern = Pattern.compile(whiteRegEx);
			Matcher matcher = pattern.matcher(url);
			if (matcher.find()) {
				return true;
			}
		}
		return false;
	}

	public int getPauseTime() {
		return pauseTime;
	}

	public int getRequestTimeout() {
		return requestTimeout;
	}

	public String normalize(String url)
			throws MalformedURLException, URISyntaxException {
		for (UrlNormalizer normalizer : normalizers) {
			url = normalizer.normalize(url);
		}
		return url;
	}

	public String[] getIncludePatterns() {
		return this.includePatterns;
	}

	public String[] getExcludePatterns() {
		return this.excludePatterns;
	}

	public int getDepth() {
		return depth;
	}

	public Date getExpiration() {
		return new DateTime().minusDays((int) this.delay).toDate();
	}

	public String[] getStartUrls() {
		return startUrls;
	}

	public String[] getOnDemandUrls() {
		return onDemandUrls;
	}

	public String getRobotsUserAgent() {
		return robotsUserAgent;
	}

	public int getThreads() {
		return threads;
	}
}
