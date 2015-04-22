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
package com.constellio.app.ui.framework.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.app.services.collections.CollectionsManager;

public class CollectionVODataProvider implements DataProvider {
	protected transient CollectionsManager collectionManager;

	transient List<CollectionVO> collections;

	public CollectionVODataProvider() {
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	void init() {
		AppLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		collectionManager = modelLayerFactory.getCollectionsManager();
		collections = new ArrayList<>();

		List<String> codes = collectionManager.getCollectionCodes();
		for (String code : codes) {
			String name = collectionManager.getCollection(code).get(Schemas.TITLE.getLocalCode());
			CollectionVO collectionVO = new CollectionVO(code, name);
			collections.add(collectionVO);
		}
	}

	public CollectionVO getRecordVO(int index) {
		if (collections == null) {
			init();
		}
		return collections.get(index);
	}

	public int size() {
		if (collections == null) {
			init();
		}
		return collections.size();
	}

	public List<CollectionVO> getCollections() {
		if (collections == null) {
			init();
		}
		return collections;
	}

	public void delete(Integer index) {
		collections.remove(index);
	}

	public static class CollectionVO implements Serializable {
		private String code;
		private String name;

		public CollectionVO(String code, String name) {
			this.code = code;
			this.name = name;
		}

		public CollectionVO() {

		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
