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
package com.constellio.model.services.emails;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.data.dao.managers.config.values.TextConfiguration;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.CloseableStreamFactory;
import com.constellio.model.services.collections.CollectionsListManager;

public class EmailTemplatesManager implements StatefulService {
	private static final String EMAIL_TEMPLATES_CONFIGS = "/emailTemplates/";
	public static final String LOGO_ID = "logo";
	public static final String BACKGROUND_ID = "background";

	private final ConfigManager configManager;
	private final CollectionsListManager collectionsListManager;
	private Map<String, CollectionTemplates> cache;
	private IOServices ioServices;

	public EmailTemplatesManager(ConfigManager configManager, CollectionsListManager collectionsListManager,
			IOServices ioServices) {
		this.configManager = configManager;
		this.ioServices = ioServices;
		this.collectionsListManager = collectionsListManager;
		initialize();
	}

	@Override
	public void initialize() {
		cache = new HashMap<>();
		for (String collection : collectionsListManager.getCollections()) {
			cache.put(collection, new CollectionTemplates());
		}
	}

	@Override
	public void close() {

	}

	public String getCollectionTemplate(String templateId, String collection) {
		loadCollectionTemplateIfRequired(collection, templateId);
		CollectionTemplates templates = cache.get(collection);
		return templates.getTemplate(templateId);
	}

	public void addCollectionTemplateIfInexistent(String templateId, String collection, InputStream templateStream)
			throws IOException, ConfigManagerException.OptimisticLockingConfiguration {

		String path = getConfigPath(collection, templateId);
		if (!configManager.exist(path)) {
			addCollectionTemplate(templateId, collection, templateStream);
		}
	}

	public void addCollectionTemplate(String templateId, String collection, InputStream templateStream)
			throws IOException, ConfigManagerException.OptimisticLockingConfiguration {

		CloseableStreamFactory<InputStream> streamFactory = ioServices.copyToReusableStreamFactory(templateStream);
		InputStream stream1 = null;
		InputStream stream2 = null;
		try {
			stream1 = streamFactory.create("stream1");
			stream2 = streamFactory.create("stream2");
			StringWriter writer = new StringWriter();
			IOUtils.copy(stream1, writer);
			String path = getConfigPath(collection, templateId);
			configManager.add(path, stream2);
			String templateText = writer.toString();
			initCacheWithCollectionIfRequired(collection);
			cache.get(collection).putTemplate(templateId, templateText);

		} finally {
			ioServices.closeQuietly(streamFactory);
			ioServices.closeQuietly(stream1);
			ioServices.closeQuietly(stream2);
		}
	}

	private synchronized void initCacheWithCollectionIfRequired(String collection) {
		if (cache.get(collection) == null) {
			cache.put(collection, new CollectionTemplates());
		}
	}

	private synchronized void loadCollectionTemplateIfRequired(String collection, String templateId) {
		initCacheWithCollectionIfRequired(collection);
		CollectionTemplates templates = cache.get(collection);
		if (templates.getTemplate(templateId) == null) {
			String collectionConfigPath = getConfigPath(collection, templateId);
			TextConfiguration templateText = configManager.getText(collectionConfigPath);
			templates.putTemplate(templateId, templateText.getText());
		}
	}

	private String getConfigPath(String collection, String templateId) {
		return "/" + collection + EMAIL_TEMPLATES_CONFIGS + templateId;
	}

	private class CollectionTemplates extends HashMap<String, String> {
		public String getTemplate(String templateId) {
			return get(templateId);
		}

		public void putTemplate(String templateId, String templateText) {
			put(templateId, templateText);
		}
	}
}
