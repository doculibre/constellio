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
package com.constellio.model.services.extensions;

import java.util.HashMap;
import java.util.Map;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.extensions.ModelLayerCollectionEventsListeners;
import com.constellio.model.extensions.ModelLayerSystemEventsListeners;

public class ModelLayerExtensions implements StatefulService {

	Map<String, ModelLayerCollectionEventsListeners> collectionListeners = new HashMap<>();

	ModelLayerSystemEventsListeners systemListeners = new ModelLayerSystemEventsListeners();

	@Override
	public void initialize() {
	}

	@Override
	public void close() {
	}

	public ModelLayerSystemEventsListeners getSystemListeners() {
		return systemListeners;
	}

	public final ModelLayerCollectionEventsListeners getCollectionListeners(String collection) {
		if (!collectionListeners.containsKey(collection)) {
			collectionListeners.put(collection, new ModelLayerCollectionEventsListeners());
		}
		return collectionListeners.get(collection);
	}

}
