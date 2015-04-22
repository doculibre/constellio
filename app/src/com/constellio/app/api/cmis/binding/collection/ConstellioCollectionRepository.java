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
