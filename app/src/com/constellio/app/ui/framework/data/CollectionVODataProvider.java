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

		List<String> codes = collectionManager.getCollectionCodesExcludingSystem();
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
