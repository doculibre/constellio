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

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.support.TypeDefinitionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_ObjectNotFound;
import com.constellio.app.api.cmis.builders.objectType.CollectionRepositoryTypesDefinitionBuilder;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerListener;

/**
 * Manages the type definitions for all FileShare repositories.
 */
public class ConstellioCollectionTypeDefinitionsManager implements MetadataSchemasManagerListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioCollectionTypeDefinitionsManager.class);

	private static final String NAMESPACE = "http://chemistry.apache.org/opencmis/fileshare";

	private final TypeDefinitionFactory typeDefinitionFactory;
	private final MetadataSchemasManager metadataSchemasManager;

	private String collection;
	private Map<String, TypeDefinition> typeDefinitions;

	public ConstellioCollectionTypeDefinitionsManager(ModelLayerFactory modelLayerFactory, String collection) {
		this.collection = collection;
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.metadataSchemasManager.registerListener(this);

		this.typeDefinitionFactory = setupTypeDefinitionFactory();

		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
		typeDefinitions = newCollectionRepositoryTypesDefinitionBuilder(types);
	}

	private static TypeDefinitionFactory setupTypeDefinitionFactory() {
		TypeDefinitionFactory typeDefinitionFactory = TypeDefinitionFactory.newInstance();
		typeDefinitionFactory.setDefaultNamespace(NAMESPACE);
		typeDefinitionFactory.setDefaultControllableAcl(false);
		typeDefinitionFactory.setDefaultControllablePolicy(false);
		typeDefinitionFactory.setDefaultQueryable(false);
		typeDefinitionFactory.setDefaultFulltextIndexed(false);
		typeDefinitionFactory.setDefaultTypeMutability(typeDefinitionFactory.createTypeMutability(false, false, false));
		return typeDefinitionFactory;
	}

	public Map<String, TypeDefinition> newCollectionRepositoryTypesDefinitionBuilder(MetadataSchemaTypes types) {
		return new CollectionRepositoryTypesDefinitionBuilder(types, typeDefinitionFactory).build();
	}

	public synchronized TypeDefinition getInternalTypeDefinition(String typeId) {
		return typeDefinitions.get(typeId);
	}

	public synchronized Collection<TypeDefinition> getInternalTypeDefinitions() {
		return typeDefinitions.values();
	}

	public TypeDefinition getTypeDefinition(CallContext context, String typeId) {
		TypeDefinition type = typeDefinitions.get(typeId);
		if (type == null) {
			throw new CmisExceptions_ObjectNotFound("type", typeId);
		}

		return typeDefinitionFactory.copy(type, true, context.getCmisVersion());
	}

	public TypeDefinitionList getTypeChildren(CallContext context, String typeId, Boolean includePropertyDefinitions,
			BigInteger maxItems, BigInteger skipCount) {
		return typeDefinitionFactory.createTypeDefinitionList(typeDefinitions, typeId, includePropertyDefinitions, maxItems,
				skipCount, context.getCmisVersion());
	}

	public List<TypeDefinitionContainer> getTypeDescendants(CallContext context, String typeId, BigInteger depth,
			Boolean includePropertyDefinitions) {
		return typeDefinitionFactory.createTypeDescendants(typeDefinitions, typeId, depth, includePropertyDefinitions,
				context.getCmisVersion());
	}

	@Override
	public void onCollectionSchemasModified(String collection) {
		if (collection.equals(this.collection)) {
			MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
			typeDefinitions = newCollectionRepositoryTypesDefinitionBuilder(types);
		}
	}
}
