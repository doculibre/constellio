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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.wrappers.Collection;

public class CollectionVODataProvider implements DataProvider {
	protected transient CollectionsManager collectionManager;

	transient List<CollectionVO> collections;

	public CollectionVODataProvider(AppLayerFactory appLayerFactory) {
		init(appLayerFactory);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init(getAppLayerFactory());
	}

	private AppLayerFactory getAppLayerFactory() {
		return ConstellioFactories.getInstance().getAppLayerFactory();
	}

	void init(AppLayerFactory appLayerFactory) {
		collectionManager = appLayerFactory.getCollectionsManager();
		collections = new ArrayList<>();

		List<String> codes = collectionManager.getCollectionCodes();
		for (String code : codes) {
			Collection collection = collectionManager.getCollection(code);
			CollectionVO collectionVO = new CollectionVO(code, collection.getName(), collection.getLanguages().get(0));
			collections.add(collectionVO);
		}
	}

	public CollectionVO getRecordVO(int index) {
		if (collections == null) {
			init(getAppLayerFactory());
		}
		return collections.get(index);
	}

	public int size() {
		if (collections == null) {
			init(getAppLayerFactory());
		}
		return collections.size();
	}

	public List<CollectionVO> getCollections() {
		if (collections == null) {
			init(getAppLayerFactory());
		}
		return collections;
	}

	public List<CollectionVO> getCollections(int startIndex, int count) {
		int toIndex = startIndex + count;
		List<CollectionVO> newCollectionVOs = getCollections();
		List subList = new ArrayList();
		if (startIndex > newCollectionVOs.size()) {
			return subList;
		} else if (toIndex > newCollectionVOs.size()) {
			toIndex = newCollectionVOs.size();
		}
		return newCollectionVOs.subList(startIndex, toIndex);
	}

	public void delete(Integer index) {
		collections.remove(index);
	}

	public static class CollectionVO implements Serializable {
		private String code;
		private String name;
		private String language;
		private Set<String> modules;

		public CollectionVO(String code, String name, String language, List<String> installedModules) {
			this.code = code;
			this.name = name;
			this.language = language;
			modules = new HashSet<>(installedModules);
		}

		public CollectionVO(String code, String name, String language) {
			this(code, name, language, new ArrayList<String>());
		}

		public CollectionVO() {
			this(null, null, null, new ArrayList<String>());
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

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public Set<String> getModules() {
			return modules;
		}

		public void setModules(Set<String> modules) {
			this.modules = modules;
		}
	}
}
