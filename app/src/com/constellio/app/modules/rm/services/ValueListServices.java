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
package com.constellio.app.modules.rm.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimistickLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class ValueListServices {
	MetadataSchemasManager schemasManager;
	SchemasDisplayManager schemasDisplayManager;
	TaxonomiesManager taxonomiesManager;
	UniqueIdGenerator uniqueIdGenerator;
	String collection;

	public ValueListServices(AppLayerFactory appLayerFactory, String collection) {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.uniqueIdGenerator = modelLayerFactory.getDataLayerFactory().getUniqueIdGenerator();
		this.collection = collection;
	}

	public List<MetadataSchemaType> getValueDomainTypes() {
		List<MetadataSchemaType> types = new ArrayList<>();
		for (MetadataSchemaType type : schemasManager.getSchemaTypes(collection).getSchemaTypes()) {
			if (type.getCode().startsWith("ddv")) {
				types.add(type);
			}
		}
		return Collections.unmodifiableList(types);
	}

	public MetadataSchemaType createValueDomain(String title) {
		String code = generateCode("ddv");

		MetadataSchemaTypesBuilder types = schemasManager.modify(collection);
		ValueListItemSchemaTypeBuilder builder = new ValueListItemSchemaTypeBuilder(types);

		builder.createValueListItemSchema(code, title, ValueListItemSchemaTypeCodeMode.REQUIRED_AND_UNIQUE);

		try {
			return schemasManager.saveUpdateSchemaTypes(types).getSchemaType(code);
		} catch (OptimistickLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}
	}

	public List<Taxonomy> getTaxonomies() {
		return taxonomiesManager.getEnabledTaxonomies(collection);
	}

	public Taxonomy createTaxonomy(String title) {
		String code = generateCode("");
		MetadataSchemaType type = createTaxonomyType("taxo" + code + "Type", title);
		Taxonomy taxonomy = Taxonomy.createPublic("taxo" + code, title, collection, Arrays.asList(type.getCode()));

		taxonomiesManager.addTaxonomy(taxonomy, schemasManager);

		return taxonomy;
	}

	public Taxonomy createTaxonomy(String title, List<String> userIds, List<String> groupIds, boolean isVisibleInHomePage) {
		String code = generateCode("");
		MetadataSchemaType type = createTaxonomyType("taxo" + code + "Type", title);
		Taxonomy taxonomy = Taxonomy
				.createPublic("taxo" + code, title, collection, userIds, groupIds, Arrays.asList(type.getCode()),
						isVisibleInHomePage);

		taxonomiesManager.addTaxonomy(taxonomy, schemasManager);

		return taxonomy;
	}

	public List<MetadataSchemaType> getClassifiedSchemaTypes(Taxonomy taxonomy) {

		List<MetadataSchemaType> classifiedTypes = new ArrayList<>();

		for (MetadataSchemaType type : schemasManager.getSchemaTypes(taxonomy.getCollection()).getSchemaTypes()) {
			List<Metadata> metadatas = type.getAllMetadatas().onlyTaxonomyReferences()
					.onlyReferencesToType(taxonomy.getSchemaTypes().get(0));

			if (!metadatas.isEmpty()) {
				classifiedTypes.add(type);
			}

		}

		return classifiedTypes;
	}

	public void createAMultivalueClassificationMetadataInGroup(Taxonomy taxonomy, String schemaType, String groupLabel) {

		MetadataSchemaTypesBuilder types = schemasManager.modify(taxonomy.getCollection());

		String localCode = taxonomy.getCode() + "Ref";
		MetadataSchemaTypeBuilder taxonomyType = types.getSchemaType(taxonomy.getSchemaTypes().get(0));
		types.getSchemaType(schemaType).getDefaultSchema().create(localCode).defineTaxonomyRelationshipToType(taxonomyType)
				.setMultivalue(true).setLabel(taxonomy.getTitle());
		try {
			schemasManager.saveUpdateSchemaTypes(types);
		} catch (OptimistickLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}

		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		SchemaTypeDisplayConfig typeDisplayConfig = schemasDisplayManager.getType(taxonomy.getCollection(), schemaType);
		if (!typeDisplayConfig.getMetadataGroup().contains(groupLabel)) {
			transaction.add(typeDisplayConfig.withNewMetadataGroup(groupLabel));
		}
		String metadataCode = schemaType + "_default_" + localCode;
		MetadataDisplayConfig metadataDisplayConfig = schemasDisplayManager.getMetadata(taxonomy.getCollection(), metadataCode);
		transaction.add(metadataDisplayConfig
				.withInputType(MetadataInputType.LOOKUP)
				.withMetadataGroup(groupLabel)
				.withVisibleInAdvancedSearchStatus(true));

		for (MetadataSchema schema : schemasManager.getSchemaTypes(taxonomy.getCollection()).getSchemaType(schemaType)
				.getAllSchemas()) {
			String comments = schema.getCode() + "_" + Folder.COMMENTS;
			SchemaDisplayConfig schemaDisplayConfig = schemasDisplayManager.getSchema(taxonomy.getCollection(), schema.getCode());
			transaction.add(schemaDisplayConfig
					.withNewDisplayMetadataBefore(metadataCode, comments)
					.withNewFormMetadata(metadataCode));
		}

		schemasDisplayManager.execute(transaction);
	}

	private MetadataSchemaType createTaxonomyType(String code, String title) {

		MetadataSchemaTypesBuilder types = schemasManager.modify(collection);
		ValueListItemSchemaTypeBuilder builder = new ValueListItemSchemaTypeBuilder(types);

		builder.createHierarchicalValueListItemSchema(code, title, ValueListItemSchemaTypeCodeMode.REQUIRED_AND_UNIQUE);

		try {
			return schemasManager.saveUpdateSchemaTypes(types).getSchemaType(code);
		} catch (OptimistickLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}
	}

	private String generateCode(String prefix) {
		String id = uniqueIdGenerator.next();
		id = id.replace("0", " ").trim().replace(" ", "0");
		return prefix + id;
	}
}
