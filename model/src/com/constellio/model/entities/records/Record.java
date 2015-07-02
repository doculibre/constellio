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
package com.constellio.model.entities.records;

import java.io.Serializable;
import java.util.List;

import com.constellio.model.entities.CollectionObject;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataList;

public interface Record extends Serializable, CollectionObject {

	public String getId();

	public long getVersion();

	public String getSchemaCode();

	public boolean isDirty();

	boolean isModified(Metadata metadata);

	Record set(Metadata metadata, Object value);

	<T> T get(Metadata metadata);

	<T> T getNonNullValueIn(List<Metadata> metadatas);

	<T> List<T> getList(Metadata metadata);

	MetadataList getModifiedMetadatas(MetadataSchemaTypes schemaTypes);

	//MetadataList getMetadatasWithValue(MetadataSchemaTypes schemaTypes);

	boolean isSaved();

	String getCollection();

	String getParentId();

	boolean isActive();

	List<String> getFollowers();

	Record getCopyOfOriginalRecord();

	String getIdTitle();

	void removeAllFieldsStartingWith(String field);

	void markAsModified(Metadata metadata);
}
