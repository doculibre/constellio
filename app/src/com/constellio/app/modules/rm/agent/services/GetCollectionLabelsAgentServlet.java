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
package com.constellio.app.modules.rm.agent.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.wrappers.Collection;

public class GetCollectionLabelsAgentServlet extends SocketAgentServlet<Map<String, String>> {
	
	private static final Logger LOGGER = Logger.getLogger(GetCollectionLabelsAgentServlet.class);

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, String> respond(Map<String, Object> inParams) throws Exception {
		Map<String, String> collectionLabels = new HashMap<>();

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		CollectionsManager collectionsManager = appLayerFactory.getCollectionsManager();
		
		List<String> collectionNames = (List<String>) inParams.get("collectionNames");
		for (String collectionName : collectionNames) {
			String collectionLabel;
			try {
				Collection collection = collectionsManager.getCollection(collectionName);
				collectionLabel = collection.getTitle();
			} catch (Throwable t) {
				LOGGER.warn("Unable to retrieve label for collection " + collectionName, t);
				collectionLabel = collectionName;
			}
			collectionLabels.put(collectionName, collectionLabel);
		}
		return collectionLabels;
	}

}
