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
package com.constellio.app.api.cmis.builders.objectType;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.MutableDocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableFolderTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutablePropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.server.support.TypeDefinitionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class CollectionRepositoryTypesDefinitionBuilder {

	private static final String COLLECTION = "collection";

	private static final String TAXONOMY = "taxonomy";

	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionRepositoryTypesDefinitionBuilder.class);

	MetadataSchemaTypes types;
	TypeDefinitionFactory typeDefinitionFactory;

	public CollectionRepositoryTypesDefinitionBuilder(MetadataSchemaTypes types, TypeDefinitionFactory typeDefinitionFactory) {
		this.types = types;
		this.typeDefinitionFactory = typeDefinitionFactory;
	}

	public Map<String, TypeDefinition> build() {
		Map<String, TypeDefinition> typeDefinitions = new HashMap<String, TypeDefinition>();

		// add base folder type
		MutableFolderTypeDefinition baseFolderType = typeDefinitionFactory.createBaseFolderTypeDefinition(CmisVersion.CMIS_1_1);
		removeQueryableAndOrderableFlags(baseFolderType);

		typeDefinitions.put(baseFolderType.getId(), baseFolderType);

		// add base document type
		MutableDocumentTypeDefinition documentType = typeDefinitionFactory.createBaseDocumentTypeDefinition(CmisVersion.CMIS_1_1);
		removeQueryableAndOrderableFlags(documentType);
		PropertyDefinition<?> documentContentMetadataProperty = PropertyBuilderFactory
				.newStringProperty("metadata").setName("metadata")
				.setMultivalue(false).setRequired(true).setUpdatability(true).build();
		PropertyDefinition<?> documentContentParentProperty = PropertyBuilderFactory
				.newStringProperty(PropertyIds.PARENT_ID).setName(PropertyIds.PARENT_ID)
				.setMultivalue(false).setRequired(false).setUpdatability(true).build();
		documentType.addPropertyDefinition(documentContentMetadataProperty);
		documentType.addPropertyDefinition(documentContentParentProperty);
		documentType.setIsVersionable(true);

		typeDefinitions.put(documentType.getId(), documentType);

		MutableFolderTypeDefinition taxoType = typeDefinitionFactory.createFolderTypeDefinition(CmisVersion.CMIS_1_1,
				baseFolderType.getId());
		taxoType.setId(TAXONOMY);
		taxoType.setLocalName(TAXONOMY);
		taxoType.setDisplayName(TAXONOMY);
		taxoType.setDescription(TAXONOMY);
		typeDefinitions.put(taxoType.getId(), taxoType);

		for (MetadataSchemaType schemaType : types.getSchemaTypes()) {

			for (MetadataSchema metadataSchema : schemaType.getAllSchemas()) {
				MutableFolderTypeDefinition folder = typeDefinitionFactory.createFolderTypeDefinition(CmisVersion.CMIS_1_1,
						baseFolderType.getId());
				folder.setId(metadataSchema.getCode());
				folder.setLocalName(metadataSchema.getCode());
				folder.setDisplayName(metadataSchema.getCode());
				folder.setQueryName(metadataSchema.getCode());
				folder.setDescription(metadataSchema.getCode());
				// folder.setParentTypeId(schemaType.getLocalCode());

				PropertyDefinition<?> propertiesSchema = PropertyBuilderFactory
						.newStringProperty(metadataSchema.getCode()).setName(metadataSchema.getCode())
						.setMultivalue(false).setRequired(true).setUpdatability(true).build();
				folder.addPropertyDefinition(propertiesSchema);

				for (Metadata metadata : metadataSchema.getMetadatas()) {

					PropertyDefinition<?> propertiesMetadata = PropertyBuilderFactory.getPropertyFor(metadata);
					folder.addPropertyDefinition(propertiesMetadata);
				}
				typeDefinitions.put(folder.getId(), folder);
			}
		}
		return typeDefinitions;
	}

	/**
	 * Removes the queryable and orderable flags from the property definitions of a type definition because this implementations
	 * does neither support queries nor can order objects.
	 */
	private void removeQueryableAndOrderableFlags(MutableTypeDefinition type) {
		for (PropertyDefinition<?> propDef : type.getPropertyDefinitions().values()) {
			MutablePropertyDefinition<?> mutablePropDef = (MutablePropertyDefinition<?>) propDef;
			mutablePropDef.setIsQueryable(false);
			mutablePropDef.setIsOrderable(false);
		}
	}

}
