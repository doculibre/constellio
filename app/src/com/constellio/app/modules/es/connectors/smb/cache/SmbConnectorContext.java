package com.constellio.app.modules.es.connectors.smb.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.es.connectors.smb.service.SmbModificationIndicator;

public class SmbConnectorContext implements Serializable {

	final String connectorId;

	SmbConnectorContext(String connectorId) {
		this.connectorId = connectorId;
	}

	Map<String, SmbModificationIndicator> recordUrls = new HashMap<>();

	public synchronized SmbModificationIndicator getModificationIndicator(String url) {
		return recordUrls.get(url);
	}

	public synchronized void traverseModified(String url, SmbModificationIndicator modificationIndicator, String parentId, String traversalCode) {
		modificationIndicator.setParentId(parentId);
		modificationIndicator.setTraversalCode(traversalCode);
		recordUrls.put(url, modificationIndicator);
	}

	public synchronized void traverseUnchanged(String url, String traversalCode) {
		SmbModificationIndicator indicator = recordUrls.get(url);
		if (indicator != null) {
			indicator.setTraversalCode(traversalCode);
		}
	}

	public synchronized String getParentId(String url) {
		SmbModificationIndicator indicator = recordUrls.get(url);
		if (indicator != null) {
			return indicator.getParentId();
		}
		return null;
	}

	public synchronized void delete(String url) {
		recordUrls.remove(url);
	}

	public synchronized List<String> staleUrls(String traversalCode) {
		List<String> urls = new ArrayList<>();
		for (Map.Entry<String, SmbModificationIndicator> entry : this.recordUrls.entrySet()) {
			SmbModificationIndicator indicator = entry.getValue();
			if (indicator == null || !StringUtils.equals(indicator.getTraversalCode(), traversalCode)) {
				urls.add(entry.getKey());
			}
		}

		return urls;
	}

	public String getConnectorId() {
		return connectorId;
	}
}
