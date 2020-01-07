package com.constellio.model.services.schemas.builders;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.calculators.UserTitleCalculator;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType;
import com.constellio.model.services.schemas.calculators.AllReferencesCalculator;
import com.constellio.model.services.schemas.calculators.AllRemovedAuthsCalculator;
import com.constellio.model.services.schemas.calculators.AttachedAncestorsCalculator2;
import com.constellio.model.services.schemas.calculators.AutocompleteFieldCalculator;
import com.constellio.model.services.schemas.calculators.DefaultTokensOfHierarchyCalculator;
import com.constellio.model.services.schemas.calculators.IntegerAttachedPrincipalConceptsAncestorsCalculator;
import com.constellio.model.services.schemas.calculators.IntegerDetachedPrincipalConceptsAncestorsCalculator;
import com.constellio.model.services.schemas.calculators.PathCalculator;
import com.constellio.model.services.schemas.calculators.PathPartsCalculator;
import com.constellio.model.services.schemas.calculators.PrincipalAncestorsCalculator;
import com.constellio.model.services.schemas.calculators.PrincipalConceptsIntIdsCalculator;
import com.constellio.model.services.schemas.calculators.PrincipalPathCalculator;
import com.constellio.model.services.schemas.calculators.SecondaryConceptsIntIdsCalculator;
import com.constellio.model.services.schemas.calculators.TokensCalculator2;
import com.constellio.model.services.schemas.calculators.TokensCalculator4;
import com.constellio.model.services.schemas.validators.ManualTokenValidator;

import java.util.HashMap;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;

public class CommonMetadataBuilder {
	public static final String ID = "id";
	public static final String SCHEMA = "schema";
	public static final String PATH = "path";
	public static final String REMOVED_AUTHORIZATIONS = "removedauthorizations";
	public static final String DETACHED_AUTHORIZATIONS = "detachedauthorizations";
	public static final String TOKENS = "tokens";
	public static final String TOKENS_OF_HIERARCHY = "tokensHierarchy";
	public static final String DENY_TOKENS = "denyTokens";
	public static final String SHARE_TOKENS = "shareTokens";
	public static final String SHARE_DENY_TOKENS = "shareDenyTokens";
	public static final String MANUAL_TOKENS = "manualTokens";
	public static final String LOGICALLY_DELETED = "deleted";
	public static final String PRINCIPAL_PATH = "principalpath";
	public static final String PATH_PARTS = "pathParts";
	public static final String CREATED_BY = "createdBy";
	public static final String MODIFIED_BY = "modifiedBy";
	public static final String CREATED_ON = "createdOn";
	public static final String MODIFIED_ON = "modifiedOn";
	public static final String TITLE = "title";
	public static final String SUMMARY = "summary";
	public static final String LEGACY_ID = "legacyIdentifier";
	public static final String VISIBLE_IN_TREES = "visibleInTrees";
	public static final String LOGICALLY_DELETED_ON = "logicallyDeletedOn";
	public static final String ERROR_ON_PHYSICAL_DELETION = "errorOnPhysicalDeletion";
	public static final String ALL_REFERENCES = "allReferences";
	public static final String MARKED_FOR_PREVIEW_CONVERSION = "markedForPreviewConversion";
	public static final String MARKED_FOR_REINDEXING = "markedForReindexing";
	public static final String MARKED_FOR_PARSING = "markedForParsing";
	public static final String ATTACHED_ANCESTORS = "attachedAncestors";
	public static final String ALL_REMOVED_AUTHS = "allRemovedAuths";
	public static final String SCHEMA_AUTOCOMPLETE_FIELD = "autocomplete";
	public static final String CAPTION = "caption";
	public static final String DATA_VERSION = "migrationDataVersion";
	public static final String ESTIMATED_SIZE = "estimatedSize";
	public static final String HIDDEN = "hidden";

	public static final String SECONDARY_CONCEPTS_INT_IDS = "secondaryConceptsIntIds";
	public static final String PRINCIPAL_CONCEPTS_INT_IDS = "principalConceptsIntIds";
	public static final String ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS = "attachedPrincipalAncestorsIntIds";
	public static final String DETACHED_PRINCIPALS_ANCESTORS_INT_IDS = "detachedPrincipalAncestorsIntIds";
	public static final String PRINCIPALS_ANCESTORS_INT_IDS = "principalAncestorsIntIds";


	private interface MetadataCreator {
		void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types);
	}

	private final Map<String, MetadataCreator> metadata = new HashMap<>();

	public CommonMetadataBuilder() {
		metadata.put(ID, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder builder, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = builder.createSystemReserved(ID).setType(STRING)
						.setUnmodifiable(true)
						.setUniqueValue(true).setDefaultRequirement(true).setSearchable(true).setSortable(true);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});
		metadata.put(LEGACY_ID, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(LEGACY_ID).setType(STRING)
						.setUnmodifiable(true)
						.setUniqueValue(true).setDefaultRequirement(true).setSearchable(true);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(SCHEMA, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(SCHEMA).setType(STRING)
						.setDefaultRequirement(true);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(PATH, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(PATH).setType(STRING)
						.setMultivalue(true)
						.defineDataEntry().asCalculated(PathCalculator.class);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});
		metadata.put(PATH_PARTS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(PATH_PARTS).setType(STRING)
						.setMultivalue(true)
						.defineDataEntry().asCalculated(PathPartsCalculator.class);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});
		metadata.put(PRINCIPAL_PATH, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(PRINCIPAL_PATH).setType(STRING)
						.defineDataEntry().asCalculated(PrincipalPathCalculator.class);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(REMOVED_AUTHORIZATIONS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(REMOVED_AUTHORIZATIONS)
						.setType(STRING).setMultivalue(true);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});
		metadata.put(DETACHED_AUTHORIZATIONS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(DETACHED_AUTHORIZATIONS)
						.setType(BOOLEAN);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(TOKENS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(TOKENS).setType(STRING)
						.setMultivalue(true);
				if (!asList(Collection.SCHEMA_TYPE, User.SCHEMA_TYPE, Group.SCHEMA_TYPE).contains(schema.getTypeCode())
					&& types.hasSchemaType(Authorization.SCHEMA_TYPE)) {
					metadataBuilder.defineDataEntry().asCalculated(TokensCalculator4.class);

				} else {
					metadataBuilder.defineDataEntry().asCalculated(TokensCalculator2.class);

				}

				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(TOKENS_OF_HIERARCHY, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(TOKENS_OF_HIERARCHY).setType(STRING)
						.setMultivalue(true).defineDataEntry().asCalculated(DefaultTokensOfHierarchyCalculator.class);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(DENY_TOKENS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				defineTokenMetadata(schema, DENY_TOKENS);
			}
		});
		metadata.put(SHARE_TOKENS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				defineTokenMetadata(schema, SHARE_TOKENS);
			}
		});
		metadata.put(SHARE_DENY_TOKENS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				defineTokenMetadata(schema, SHARE_DENY_TOKENS);
			}
		});
		metadata.put(MANUAL_TOKENS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				defineTokenMetadata(schema, MANUAL_TOKENS);
			}
		});

		metadata.put(LOGICALLY_DELETED, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(LOGICALLY_DELETED)
						.setType(BOOLEAN);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});


		metadata.put(CREATED_BY, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				if (isCollectionUserOrGroupSchema(schema)) {
					return;
				}
				try {
					MetadataSchemaTypeBuilder user = types.getSchemaType(User.SCHEMA_TYPE);
					MetadataBuilder metadataBuilder = schema.createSystemReserved(CREATED_BY).defineReferencesTo(user);
					for (Language language : types.getLanguages()) {
						metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
					}
				} catch (NoSuchSchemaType e) {
					// Do nothing
				}
			}
		});
		metadata.put(CREATED_ON, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(CREATED_ON).setType(DATE_TIME)
						.setSortable(true);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(MODIFIED_BY, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				if (isCollectionUserOrGroupSchema(schema)) {
					return;
				}
				try {
					MetadataSchemaTypeBuilder user = types.getSchemaType(User.SCHEMA_TYPE);
					MetadataBuilder metadataBuilder = schema.createSystemReserved(MODIFIED_BY).defineReferencesTo(user);
					for (Language language : types.getLanguages()) {
						metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
					}
				} catch (NoSuchSchemaType e) {
					// Do nothing
				}
			}
		});
		metadata.put(MODIFIED_ON, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(MODIFIED_ON).setType(DATE_TIME).setSortable(true);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(TITLE, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder title = schema.createUndeletable(TITLE).setType(STRING).setSearchable(true)
						.setSchemaAutocomplete(true);
				if (schema.getCode().equals(User.DEFAULT_SCHEMA)) {
					title.defineDataEntry().asCalculated(UserTitleCalculator.class);
				}
				for (Language language : types.getLanguages()) {
					title.addLabel(language, title.getLocalCode());
				}
			}
		});


		metadata.put(VISIBLE_IN_TREES, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(VISIBLE_IN_TREES).setType(BOOLEAN);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(MARKED_FOR_PREVIEW_CONVERSION, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(MARKED_FOR_PREVIEW_CONVERSION).setType(BOOLEAN);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(LOGICALLY_DELETED_ON, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(LOGICALLY_DELETED_ON).setType(DATE_TIME);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(ERROR_ON_PHYSICAL_DELETION, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(ERROR_ON_PHYSICAL_DELETION).setType(BOOLEAN);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(ALL_REFERENCES, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(ALL_REFERENCES).setType(STRING).setMultivalue(true)
						.defineDataEntry().asCalculated(AllReferencesCalculator.class);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(MARKED_FOR_REINDEXING, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(MARKED_FOR_REINDEXING).setType(BOOLEAN);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(ATTACHED_ANCESTORS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(ATTACHED_ANCESTORS).setType(STRING)
						.setMultivalue(true).setEssential(true)
						.defineDataEntry().asCalculated(AttachedAncestorsCalculator2.class);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(ALL_REMOVED_AUTHS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(ALL_REMOVED_AUTHS).setType(STRING)
						.setMultivalue(true).setEssential(true)
						.defineDataEntry().asCalculated(AllRemovedAuthsCalculator.class);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(SCHEMA_AUTOCOMPLETE_FIELD, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(SCHEMA_AUTOCOMPLETE_FIELD).setType(STRING)
						.setMultivalue(true).setEssential(true).setMultiLingual(true)
						.defineDataEntry().asCalculated(AutocompleteFieldCalculator.class);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(DATA_VERSION, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(DATA_VERSION).setType(NUMBER)
						.setEssentialInSummary(true);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(MARKED_FOR_PARSING, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(MARKED_FOR_PARSING).setType(BOOLEAN);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(CAPTION, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = schema.createSystemReserved(CAPTION).setType(STRING)
						.setSortable(true);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(ESTIMATED_SIZE, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder builder, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = builder.createSystemReserved(ESTIMATED_SIZE).setType(INTEGER);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(HIDDEN, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder builder, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = builder.createSystemReserved(HIDDEN).setType(BOOLEAN);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder builder, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = builder.createSystemReserved(ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS)
						.setType(INTEGER).setMultivalue(true).setEssentialInSummary(true)
						.defineDataEntry().asCalculated(IntegerAttachedPrincipalConceptsAncestorsCalculator.class);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(DETACHED_PRINCIPALS_ANCESTORS_INT_IDS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder builder, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = builder.createSystemReserved(DETACHED_PRINCIPALS_ANCESTORS_INT_IDS)
						.setType(INTEGER).setMultivalue(true).setEssentialInSummary(true)
						.defineDataEntry().asCalculated(IntegerDetachedPrincipalConceptsAncestorsCalculator.class);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(SECONDARY_CONCEPTS_INT_IDS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder builder, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = builder.createSystemReserved(SECONDARY_CONCEPTS_INT_IDS)
						.setType(INTEGER).setMultivalue(true).setEssentialInSummary(true)
						.defineDataEntry().asCalculated(SecondaryConceptsIntIdsCalculator.class);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(PRINCIPAL_CONCEPTS_INT_IDS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder builder, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = builder.createSystemReserved(PRINCIPAL_CONCEPTS_INT_IDS)
						.setType(INTEGER).setMultivalue(true).setEssentialInSummary(true)
						.defineDataEntry().asCalculated(PrincipalConceptsIntIdsCalculator.class);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});

		metadata.put(PRINCIPALS_ANCESTORS_INT_IDS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder builder, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = builder.createSystemReserved(PRINCIPALS_ANCESTORS_INT_IDS)
						.setType(INTEGER).setMultivalue(true).setEssentialInSummary(true)
						.defineDataEntry().asCalculated(PrincipalAncestorsCalculator.class);
				for (Language language : types.getLanguages()) {
					metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
				}
			}
		});


	}

	public void addCommonMetadataToAllExistingSchemas(MetadataSchemaTypesBuilder types) {
		for (MetadataSchemaTypeBuilder type : types.getTypes()) {
			addCommonMetadataToExistingSchema(type.getDefaultSchema(), types);
		}
	}

	public void addCommonMetadataToExistingSchema(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
		for (Map.Entry<String, MetadataCreator> each : metadata.entrySet()) {
			if (!schema.hasMetadata(each.getKey())) {
				each.getValue().define(schema, types);
			}
		}
	}

	public void addCommonMetadataToNewSchema(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
		for (MetadataCreator creator : metadata.values()) {
			creator.define(schema, types);
		}
	}

	private void defineTokenMetadata(MetadataSchemaBuilder schema, String code) {
		MetadataBuilder metadataBuilder = schema.createSystemReserved(code).setType(STRING).setMultivalue(true);
		metadataBuilder.defineValidators().add(ManualTokenValidator.class);
	}

	private boolean isCollectionUserOrGroupSchema(MetadataSchemaBuilder schema) {
		String code = new SchemaUtils().getSchemaTypeCode(schema.getCode());
		return code.equals(Collection.SCHEMA_TYPE) || code.equals(User.SCHEMA_TYPE) || code.equals(Group.SCHEMA_TYPE);
	}
}
