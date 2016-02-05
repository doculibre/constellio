package com.constellio.app.modules.es.connectors.http.fetcher;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;

public class ConnectorUrlAcceptor implements UrlAcceptor {

	private ConnectorHttpInstance connectorHttpInstance;

	public ConnectorUrlAcceptor(ConnectorHttpInstance connectorHttpInstance) {
		this.connectorHttpInstance = connectorHttpInstance;
	}

	@Override
	public boolean isAccepted(String url) {
		if (!isValidUrlPattern(url)) {
			return false;
		} else if (isSeed(url)) {
			return true;
		} else if (isExcluded(url)) {
			return false;
		} else if (isIncluded(url)) {
			return true;
		}		
		return isFromSeed(url);
	}

	private boolean isValidUrlPattern(String pattern) {
		if (StringUtils.isBlank(pattern)) {
			return false;
		}
		try {
			new URL(pattern);
		} catch (MalformedURLException e) {
			return false;
		}
		return true;
	}
	
	private boolean isFromSeed(String url) {	
		List<String> seeds = connectorHttpInstance.getSeedsList();
        for (String seed : seeds) {
        	if (StringUtils.isNotBlank(seed)) {
	        	if (StringUtils.startsWith(url, seed)) {
            		return true;
            	}
        	}
        }
        return false;
	}
	
	private boolean isSeed(String url) {	
		List<String> seeds = connectorHttpInstance.getSeedsList();
        for (String seed : seeds) {
        	if (StringUtils.equals(seed, url)) {
           		return true;
        	}
        }
        return false;
	}
	
	private boolean isExcluded(String url) {	
		String patterns = connectorHttpInstance.getExcludePatterns();
		String[] regexes = StringUtils.split(patterns, "\n");
		if (matches(url, regexes)) {
			return true;
		}
		return false;
	}
	
	private boolean isIncluded(String url) {
		String patterns = connectorHttpInstance.getIncludePatterns();
		String[] regexes = StringUtils.split(patterns, "\n");
		if (matches(url, regexes)) {
			return true;
		}
		return false;
	}
	
	private boolean matches(String url, String[] regexes) {
		if (regexes != null) {
			for (String excludedSite : regexes) {
				Pattern pattern = Pattern.compile(excludedSite);
				Matcher matcher = pattern.matcher(url);
				if (matcher.find()) {
					return true;
				}
			}
		}
		return false;
	}
	
}
