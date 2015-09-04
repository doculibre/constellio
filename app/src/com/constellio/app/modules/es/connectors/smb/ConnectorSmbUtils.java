/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.es.connectors.smb;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class ConnectorSmbUtils {
	private ESSchemasRecordsServices es;

	public ConnectorSmbUtils(ESSchemasRecordsServices es) {
		this.es = es;
	}

	public List<ConnectorDocument<?>> getExistingDocumentsOrFoldersWithUrl(String url, ConnectorInstance connectorInstance) {
		List<ConnectorDocument<?>> documentsOrFolders = es.searchConnectorDocuments(new LogicalSearchQuery(es.fromAllDocumentsOf(connectorInstance.getId())
				.andWhere(es.connectorSmbDocument.url())
				.isEqualTo(url)
				.orWhere(es.connectorSmbFolder.url())
				.isEqualTo(url)));
		return documentsOrFolders;
	}

	/**
	 * We first look at rejected/black URLs, then if nothing matches look for accepted/white URLs. If nothing matches, the URL is
	 * rejected.
	 */
	public boolean isAccepted(String url, ConnectorSmbInstance connectorInstance) {
		return isAccepted(url, connectorInstance.getSeeds(), connectorInstance.getInclusions(), connectorInstance.getExclusions());
	}

	public boolean isAccepted(String url, List<String> seeds, List<String> includePatterns, List<String> excludePatterns) {
		if (!url.startsWith("smb://")) {
			return false;
		}
		if (StringUtils.isBlank(url)) {
			return false;
		}

		if (excludePatterns != null && !excludePatterns.isEmpty()) {
			for (String blackRegEx : excludePatterns) {
				if (StringUtils.isNotBlank(blackRegEx)) {
					if (url.startsWith(blackRegEx)) {
						return false;
					}
					Pattern pattern = Pattern.compile(blackRegEx, Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(url);
					if (matcher.find()) {
						return false;
					}
				}
			}
		}

		if (includePatterns != null && !includePatterns.isEmpty()) {
			for (String whiteRegEx : includePatterns) {
				if (StringUtils.isNotBlank(whiteRegEx)) {
					if (url.startsWith(whiteRegEx)) {
						return true;
					}
					Pattern pattern = Pattern.compile(whiteRegEx, Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(url);
					if (matcher.find()) {
						return true;
					}
				}
			}
			return false;
		}
		// Fallback to seeds
		if (seeds != null) {
			for (String whiteRegEx : seeds) {
				if (StringUtils.isNotBlank(whiteRegEx)) {
					if (url.startsWith(whiteRegEx)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
