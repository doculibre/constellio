package com.constellio.model.services.schemas.builders;

import java.util.HashMap;
import java.util.Map;

import com.constellio.model.entities.records.calculators.UserTitleCalculator;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType;
import com.constellio.model.services.schemas.calculators.AllAuthorizationsCalculator;
import com.constellio.model.services.schemas.calculators.InheritedAuthorizationsCalculator;
import com.constellio.model.services.schemas.calculators.ParentPathCalculator;
import com.constellio.model.services.schemas.calculators.PathCalculator;
import com.constellio.model.services.schemas.calculators.PathPartsCalculator;
import com.constellio.model.services.schemas.calculators.PrincipalPathCalculator;
import com.constellio.model.services.schemas.calculators.TokensCalculator2;
import com.constellio.model.services.schemas.validators.ManualTokenValidator;

public class CommonMetadataBuilder {
	public static final String ID = "id";
	public static final String SCHEMA = "schema";
	public static final String PATH = "path";
	public static final String PARENT_PATH = "parentpath";
	public static final String AUTHORIZATIONS = "authorizations";
	public static final String REMOVED_AUTHORIZATIONS = "removedauthorizations";
	public static final String INHERITED_AUTHORIZATIONS = "inheritedauthorizations";
	public static final String DETACHED_AUTHORIZATIONS = "detachedauthorizations";
	public static final String ALL_AUTHORIZATIONS = "allauthorizations";
	public static final String TOKENS = "tokens";
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
	public static final String FOLLOWERS = "followers";
	public static final String LEGACY_ID = "legacyIdentifier";
	public static final String SEARCHABLE = "searchable";
	public static final String VISIBLE_IN_TREES = "visibleInTrees";
	public static final String MARKED_FOR_PREVIEW_CONVERSION = "markedForPreviewConversion";

	private interface MetadataCreator {
		void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types);
	}

	private final Map<String, MetadataCreator> metadata = new HashMap<>();

	public CommonMetadataBuilder() {
		metadata.put(ID, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder builder, MetadataSchemaTypesBuilder types) {
				builder.createSystemReserved(ID).setType(MetadataValueType.STRING).setUnmodifiable(true)
						.setUniqueValue(true).setDefaultRequirement(true).setSearchable(true).setSortable(true);
			}
		});
		metadata.put(LEGACY_ID, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(LEGACY_ID).setType(MetadataValueType.STRING).setUnmodifiable(true)
						.setUniqueValue(true).setDefaultRequirement(true).setSearchable(true);
			}
		});

		metadata.put(SCHEMA, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(SCHEMA).setType(MetadataValueType.STRING).setDefaultRequirement(true);
			}
		});

		metadata.put(PATH, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(PATH).setType(MetadataValueType.STRING).setMultivalue(true)
						.defineDataEntry().asCalculated(PathCalculator.class);
			}
		});
		metadata.put(PATH_PARTS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(PATH_PARTS).setType(MetadataValueType.STRING).setMultivalue(true)
						.defineDataEntry().asCalculated(PathPartsCalculator.class);
			}
		});
		metadata.put(PRINCIPAL_PATH, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(PRINCIPAL_PATH).setType(MetadataValueType.STRING)
						.defineDataEntry().asCalculated(PrincipalPathCalculator.class);
			}
		});
		metadata.put(PARENT_PATH, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(PARENT_PATH).setType(MetadataValueType.STRING).setMultivalue(true)
						.defineDataEntry().asCalculated(ParentPathCalculator.class);
			}
		});

		metadata.put(AUTHORIZATIONS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(AUTHORIZATIONS).setType(MetadataValueType.STRING).setMultivalue(true);
			}
		});
		metadata.put(REMOVED_AUTHORIZATIONS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(REMOVED_AUTHORIZATIONS).setType(MetadataValueType.STRING).setMultivalue(true);
			}
		});
		metadata.put(INHERITED_AUTHORIZATIONS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadata = schema.createSystemReserved(INHERITED_AUTHORIZATIONS).setType(MetadataValueType.STRING)
						.setMultivalue(true);
				if (!schema.getCode().equals(Group.DEFAULT_SCHEMA)) {
					metadata.defineDataEntry().asCalculated(InheritedAuthorizationsCalculator.class);
				}
			}
		});
		metadata.put(DETACHED_AUTHORIZATIONS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(DETACHED_AUTHORIZATIONS).setType(MetadataValueType.BOOLEAN);
			}
		});
		metadata.put(ALL_AUTHORIZATIONS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(ALL_AUTHORIZATIONS).setType(MetadataValueType.STRING).setMultivalue(true)
						.defineDataEntry().asCalculated(AllAuthorizationsCalculator.class);
			}
		});

		metadata.put(TOKENS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(TOKENS).setType(MetadataValueType.STRING).setMultivalue(true)
						.defineDataEntry().asCalculated(TokensCalculator2.class);
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
				schema.createSystemReserved(LOGICALLY_DELETED).setType(MetadataValueType.BOOLEAN);
			}
		});

		metadata.put(SEARCHABLE, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(SEARCHABLE).setType(MetadataValueType.BOOLEAN);
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
					schema.createSystemReserved(CREATED_BY).defineReferencesTo(user);
				} catch (NoSuchSchemaType e) {
					// Do nothing
				}
			}
		});
		metadata.put(CREATED_ON, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(CREATED_ON).setType(MetadataValueType.DATE_TIME).setSortable(true);
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
					schema.createSystemReserved(MODIFIED_BY).defineReferencesTo(user);
				} catch (NoSuchSchemaType e) {
					// Do nothing
				}
			}
		});
		metadata.put(MODIFIED_ON, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(MODIFIED_ON).setType(MetadataValueType.DATE_TIME).setSortable(true);
			}
		});

		metadata.put(TITLE, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				MetadataBuilder title = schema.createUndeletable(TITLE).setType(MetadataValueType.STRING).setSearchable(true)
						.setSchemaAutocomplete(true);
				if (schema.getCode().equals(User.DEFAULT_SCHEMA)) {
					title.defineDataEntry().asCalculated(UserTitleCalculator.class);
				}
			}
		});

		metadata.put(FOLLOWERS, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(FOLLOWERS).setType(MetadataValueType.STRING).setMultivalue(true).setSearchable(true);
			}
		});

		metadata.put(VISIBLE_IN_TREES, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(VISIBLE_IN_TREES).setType(MetadataValueType.BOOLEAN);
			}
		});

		metadata.put(MARKED_FOR_PREVIEW_CONVERSION, new MetadataCreator() {
			@Override
			public void define(MetadataSchemaBuilder schema, MetadataSchemaTypesBuilder types) {
				schema.createSystemReserved(MARKED_FOR_PREVIEW_CONVERSION).setType(MetadataValueType.BOOLEAN);
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
		schema.createSystemReserved(code).setType(MetadataValueType.STRING).setMultivalue(true)
				.defineValidators().add(ManualTokenValidator.class);
	}

	private boolean isCollectionUserOrGroupSchema(MetadataSchemaBuilder schema) {
		String code = new SchemaUtils().getSchemaTypeCode(schema.getCode());
		return code.equals(Collection.SCHEMA_TYPE) || code.equals(User.SCHEMA_TYPE) || code.equals(Group.SCHEMA_TYPE);
	}
}
