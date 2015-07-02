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
import com.constellio.model.entities.CollectionObject;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.ModelLayerSystemExtensions;

public class ModelLayerExtensions implements StatefulService {

	Map<String, ModelLayerCollectionExtensions> collectionExtensions = new HashMap<>();

	ModelLayerSystemExtensions systemWideExtensions = new ModelLayerSystemExtensions();

	@Override
	public void initialize() {
	}

	@Override
	public void close() {
	}

	public ModelLayerSystemExtensions getSystemWideExtensions() {
		return systemWideExtensions;
	}

	public final ModelLayerCollectionExtensions forCollectionOf(CollectionObject collectionObject) {
		return forCollection(collectionObject.getCollection());
	}

	public final ModelLayerCollectionExtensions forCollection(String collection) {
		if (!collectionExtensions.containsKey(collection)) {
			collectionExtensions.put(collection, new ModelLayerCollectionExtensions());
		}
		return collectionExtensions.get(collection);
	}

}
