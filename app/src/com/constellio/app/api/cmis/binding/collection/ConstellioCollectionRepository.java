package com.constellio.app.api.cmis.binding.collection;

import java.io.File;

public class ConstellioCollectionRepository {

	private String collection;

	private File root;

	private ConstellioCollectionTypeDefinitionsManager typeDefinitionsManager;

	private ConstellioCollectionRepositoryInfoManager repositoryInfoManager;

	public ConstellioCollectionRepository(String collection, File root,
			ConstellioCollectionTypeDefinitionsManager typeDefinitionsManager,
			ConstellioCollectionRepositoryInfoManager repositoryInfoManager) {
		this.collection = collection;
		this.root = root;
		this.typeDefinitionsManager = typeDefinitionsManager;
		this.repositoryInfoManager = repositoryInfoManager;
	}

	public String getCollection() {
		return collection;
	}

	public File getRoot() {
		return root;
	}

	public ConstellioCollectionTypeDefinitionsManager getTypeDefinitionsManager() {
		return typeDefinitionsManager;
	}

	public ConstellioCollectionRepositoryInfoManager getRepositoryInfoManager() {
		return repositoryInfoManager;
	}

	@Override
	public String toString() {
		return collection;
	}
}
