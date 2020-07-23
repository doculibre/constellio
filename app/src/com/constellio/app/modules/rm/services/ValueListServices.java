package com.constellio.app.modules.rm.services;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeBuilderOptions;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ValueListServices {
	AppLayerFactory appLayerFactory;
	MetadataSchemasManager schemasManager;
	SchemasDisplayManager schemasDisplayManager;
	TaxonomiesManager taxonomiesManager;
	UniqueIdGenerator uniqueIdGenerator;
	SearchServices searchServices;
	String collection;

	public ValueListServices(AppLayerFactory appLayerFactory, String collection) {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.uniqueIdGenerator = modelLayerFactory.getDataLayerFactory().getUniqueIdGenerator();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
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

	public MetadataSchemaType createValueDomain(Map<Language, String> titleMap, boolean isMultiLingual) {
		String code = generateCode("ddv");
		return createValueDomain(code, titleMap, isMultiLingual);
	}

	public MetadataSchemaType createValueDomain(String code, Map<Language, String> titleMap, boolean isMultiLingual) {
		CreateValueListOptions createValueListOptions = new CreateValueListOptions();
		createValueListOptions.setMultilingual(isMultiLingual);
		return createValueDomain(code, titleMap, createValueListOptions);
	}

	public static class CreateValueListOptions {

		ValueListItemSchemaTypeBuilderOptions codeMode = ValueListItemSchemaTypeBuilderOptions.codeMetadataRequiredAndUnique();

		boolean createMetadatasAsMultivalued = true;

		boolean multilingual = true;

		List<String> typesWithReferenceMetadata = new ArrayList<>();

		public boolean isMultilingual() {
			return multilingual;
		}

		public CreateValueListOptions setMultilingual(boolean multilingual) {
			this.multilingual = multilingual;
			return this;
		}

		public ValueListItemSchemaTypeBuilderOptions getCodeMode() {
			return codeMode;
		}

		public CreateValueListOptions setCodeMode(
				ValueListItemSchemaTypeBuilderOptions codeMode) {
			this.codeMode = codeMode;
			return this;
		}

		public boolean isCreateMetadatasAsMultivalued() {
			return createMetadatasAsMultivalued;
		}

		public CreateValueListOptions setCreateMetadatasAsMultivalued(boolean createMetadatasAsMultivalued) {
			this.createMetadatasAsMultivalued = createMetadatasAsMultivalued;
			return this;
		}

		public List<String> getTypesWithReferenceMetadata() {
			return typesWithReferenceMetadata;
		}

		public CreateValueListOptions setTypesWithReferenceMetadata(List<String> typesWithReferenceMetadata) {
			this.typesWithReferenceMetadata = typesWithReferenceMetadata;
			return this;
		}
	}

	public MetadataSchemaType createValueDomain(String code, Map<Language, String> title,
												CreateValueListOptions options) {
		if (!code.startsWith("ddv")) {
			throw new RuntimeException("Code must start with ddv");
		}

		MetadataSchemaTypesBuilder types = schemasManager.modify(collection);

		createValueDomain(code, title, options, types);

		try {
			return schemasManager.saveUpdateSchemaTypes(types).getSchemaType(code);
		} catch (OptimisticLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}
	}

	public MetadataSchemaTypeBuilder createValueDomain(String code, Map<Language, String> title,
													   CreateValueListOptions options,
													   MetadataSchemaTypesBuilder types) {

		if (!code.startsWith("ddv")) {
			throw new RuntimeException("Code must start with ddv");
		}

		ValueListItemSchemaTypeBuilder builder = new ValueListItemSchemaTypeBuilder(types);

		MetadataSchemaTypeBuilder valueListSchemaType = builder
				.createValueListItemSchema(code, title, options.codeMode.setMultilingual(options.isMultilingual()));

		if (options.getTypesWithReferenceMetadata() != null) {
			for (String schemaType : options.getTypesWithReferenceMetadata()) {
				String metadataCode = code.replace("ddv", "");
				types.getSchemaType(schemaType).getDefaultSchema().create(metadataCode).setType(REFERENCE)
						.setMultivalue(options.isCreateMetadatasAsMultivalued())
						.setLabels(valueListSchemaType.getLabels())
						.defineReferencesTo(valueListSchemaType);
			}
		}

		return valueListSchemaType;
	}

	public List<Taxonomy> getTaxonomies() {
		return taxonomiesManager.getEnabledTaxonomies(collection);
	}

	public Taxonomy createTaxonomy(String code, Map<Language, String> title, boolean isMultiLingual) {
		MetadataSchemaType type = createTaxonomyType("taxo" + code + "Type", title, isMultiLingual);
		Taxonomy taxonomy = Taxonomy.createPublic("taxo" + code, title, collection, Arrays.asList(type.getCode()));

		taxonomiesManager.addTaxonomy(taxonomy, schemasManager);

		return taxonomy;
	}

	public Taxonomy lazyCreateTaxonomy(MetadataSchemaTypesBuilder typeBuilder, String code, Map<Language, String> title,
									   boolean isMultiLingual) {
		String typeCode = "taxo" + code + "Type";
		ValueListItemSchemaTypeBuilder builder = new ValueListItemSchemaTypeBuilder(typeBuilder);

		builder.createHierarchicalValueListItemSchema(typeCode, title,
				ValueListItemSchemaTypeBuilderOptions.codeMetadataRequiredAndUnique().titleUnique(false)
						.setMultilingual(isMultiLingual));

		return Taxonomy.createPublic("taxo" + code, title, collection, Arrays.asList(typeCode));
	}

	public Taxonomy createTaxonomy(Map<Language, String> title, boolean isMultiLingual) {
		String code = generateCode("");
		return createTaxonomy(code, title, isMultiLingual);
	}

	public Taxonomy createTaxonomy(Map<Language, String> title, Map<Language, String> abbreviation,
								   List<String> userIds, List<String> groupIds,
								   boolean isVisibleInHomePage, boolean isMultiLingual) {
		String code = generateCode("");
		return createTaxonomy(code, title, abbreviation, userIds, groupIds, isVisibleInHomePage, isMultiLingual);
	}

	public Taxonomy createTaxonomy(String code, Map<Language, String> title, Map<Language, String> abbreviation,
								   List<String> userIds, List<String> groupIds,
								   boolean isVisibleInHomePage, boolean isMultiLingual) {
		MetadataSchemaType type = createTaxonomyType("taxo" + code + "Type", title, isMultiLingual);
		Taxonomy taxonomy = Taxonomy
				.createPublic("taxo" + code, title, abbreviation, collection, userIds, groupIds,
						Arrays.asList(type.getCode()), isVisibleInHomePage);

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

	//FIXME label multilingual
	//TODO Patrick
	public Metadata createAMultivalueClassificationMetadataInGroup(Taxonomy taxonomy, String schemaTypeCode,
																   String groupCode,
																   String groupLabel) {

		MetadataSchemaTypesBuilder types = schemasManager.modify(taxonomy.getCollection());

		String localCode = taxonomy.getCode() + "Ref";
		MetadataSchemaTypeBuilder taxonomyType = types.getSchemaType(taxonomy.getSchemaTypes().get(0));
		MetadataBuilder metadataBuilder = types.getSchemaType(schemaTypeCode).getDefaultSchema().create(localCode)
				.defineTaxonomyRelationshipToType(taxonomyType)
				.setMultivalue(true);

		for (Language language : schemasManager.getSchemaTypes(collection).getLanguages()) {
			if (taxonomy.getTitle(language) == null) {
				continue;
			}
			metadataBuilder.addLabel(language, taxonomy.getTitle(language));
		}

		try {
			schemasManager.saveUpdateSchemaTypes(types);
		} catch (OptimisticLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}

		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		SchemaTypeDisplayConfig typeDisplayConfig = schemasDisplayManager.getType(taxonomy.getCollection(), schemaTypeCode);
		if (!typeDisplayConfig.getMetadataGroup().keySet().contains(groupCode)) {
			Map<String, Map<Language, String>> groups = new HashMap<>();
			for (Language language : schemasManager.getSchemaTypes(collection).getLanguages()) {
				Map<Language, String> labels = new HashMap<>();
				labels.put(language, groupLabel);
				groups.put(groupCode, labels);

			}
			transaction.add(typeDisplayConfig.withNewMetadataGroup(groups));
		}

		MetadataSchemaType schemaType = schemasManager.getSchemaTypes(taxonomy.getCollection()).getSchemaType(schemaTypeCode);
		for (MetadataSchema schema : schemaType.getAllSchemas()) {

			String metadataCode = schema.getCode() + "_" + localCode;
			MetadataDisplayConfig metadataDisplayConfig = schemasDisplayManager.getMetadata(taxonomy.getCollection(),
					metadataCode);
			transaction.add(metadataDisplayConfig
					.withInputType(MetadataInputType.LOOKUP)
					.withMetadataGroup(groupCode)
					.withVisibleInAdvancedSearchStatus(true));

			String comments = schema.getCode() + "_" + Folder.COMMENTS;
			SchemaDisplayConfig schemaDisplayConfig = schemasDisplayManager.getSchema(taxonomy.getCollection(), schema.getCode());
			transaction.add(schemaDisplayConfig
					.withNewDisplayMetadataBefore(metadataCode, comments)
					.withNewFormMetadata(metadataCode));
		}

		schemasDisplayManager.execute(transaction);

		return schemaType.getDefaultSchema().get(localCode);
	}

	private MetadataSchemaType createTaxonomyType(String code, Map<Language, String> title, boolean isMultiLingual) {

		MetadataSchemaTypesBuilder types = schemasManager.modify(collection);
		ValueListItemSchemaTypeBuilder builder = new ValueListItemSchemaTypeBuilder(types);

		builder.createHierarchicalValueListItemSchema(code, title,
				ValueListItemSchemaTypeBuilderOptions.codeMetadataRequiredAndUnique().titleUnique(false).setMultilingual(isMultiLingual));

		try {
			return schemasManager.saveUpdateSchemaTypes(types).getSchemaType(code);
		} catch (OptimisticLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}
	}

	private String generateCode(String prefix) {
		String id = uniqueIdGenerator.next();
		id = id.replace("0", " ").trim().replace(" ", "0");
		return prefix + id;
	}

	public void deleteValueListOrTaxonomy(final String schemaTypeCode)
			throws ValidationException {

		final MetadataSchemaType schemaType = schemasManager.getSchemaTypes(collection).getSchemaType(schemaTypeCode);
		if (searchServices.hasResults(query(from(schemaType).returnAll()))) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("code", schemaTypeCode);
			parameters.put("label", schemaType.getLabels());

			ValidationErrors errors = new ValidationErrors();
			errors.add(ValueListServices.class, "valueListHasRecords", parameters);

			throw new ValidationException(errors);
		}

		final List<String> metadatasToRemove = new ArrayList<>();
		for (MetadataSchemaType aSchemaType : schemasManager.getSchemaTypes(collection).getSchemaTypes()) {
			for (MetadataSchema aSchema : aSchemaType.getAllSchemas()) {
				for (Metadata aMetadata : aSchema.getMetadatas().onlyReferencesToType(schemaTypeCode).onlyWithoutInheritance()) {
					metadatasToRemove.add(aMetadata.getCode());
				}
			}
		}

		Taxonomy taxonomy = taxonomiesManager.getTaxonomyFor(collection, schemaTypeCode);
		if (taxonomy != null) {
			taxonomiesManager.deleteWithoutValidations(taxonomy);
		}

		schemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				for (String metadataToRemove : metadatasToRemove) {
					String schemaCode = new SchemaUtils().getSchemaCode(metadataToRemove);
					types.getSchema(schemaCode).deleteMetadataWithoutValidation(metadataToRemove);
				}

				types.deleteSchemaType(schemaType, searchServices);
			}
		});
	}
}
