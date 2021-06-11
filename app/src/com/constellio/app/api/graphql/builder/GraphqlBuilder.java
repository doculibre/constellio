package com.constellio.app.api.graphql.builder;

import com.constellio.app.api.graphql.GraphqlDataFetchers;
import com.constellio.app.api.graphql.builder.SchemaInfo.SchemaInfoField;
import com.constellio.app.api.graphql.builder.SchemaInfo.SchemaInfoFieldParameter;
import com.constellio.app.api.graphql.builder.binding.StructureBindings;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import graphql.schema.idl.RuntimeWiring;
import org.apache.commons.lang.ClassUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.StringUtils.capitalize;

public class GraphqlBuilder {

	private final MetadataSchemaTypes metadataSchemaTypes;
	private final Set<String> schemaTypeCodes;
	private final List<SchemaInfo> schemaInfos = new ArrayList<>();
	private final Set<WiringInfo> wiringInfos = new HashSet<>();

	private final static String[] SUPPORTED_SCHEMA_TYPES = new String[]{RetentionRule.SCHEMA_TYPE,
																		/*Category.SCHEMA_TYPE,
																		AdministrativeUnit.SCHEMA_TYPE*/};

	public GraphqlBuilder(AppLayerFactory appLayerFactory, String collection) {
		metadataSchemaTypes = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		schemaTypeCodes = new HashSet<>(Arrays.asList(SUPPORTED_SCHEMA_TYPES));
		init();
	}

	private void init() {
		Stack<String> schemaTypeCodesStack = new Stack<>();
		schemaTypeCodesStack.addAll(Arrays.asList(SUPPORTED_SCHEMA_TYPES));

		// Structures
		StructureBindings.getSupportedStructures().forEach(structureClass -> {
			SchemaInfo structureInfo = SchemaInfo.builder()
					.keyword("type")
					.type(structureClass.getSimpleName())
					.fields(new ArrayList<>())
					.build();
			Arrays.stream(structureClass.getDeclaredFields()).forEach(field -> {
				structureInfo.getFields().add(SchemaInfoField.builder()
						.name(adaptStructureFieldName(field.getName()))
						.type(getTypeFromField(field))
						.multivalue(isMultivalue(field))
						.build());

				if (StructureBindings.isReference(field)) {
					String schemaType = StructureBindings.getFieldReferenceSchemaType(field);
					if (schemaTypeCodes.add(schemaType)) {
						schemaTypeCodesStack.push(schemaType);
					}

					addWiringInfo(wiringInfos, structureClass.getSimpleName(), adaptStructureFieldName(field.getName()),
							field.getName(), null, true);
				}
			});
			schemaInfos.add(structureInfo);
		});

		// Records
		while (!schemaTypeCodesStack.isEmpty()) {
			String schemaTypeCode = schemaTypeCodesStack.pop();

			SchemaInfo schemaInfo = SchemaInfo.builder()
					.keyword("type")
					.type(capitalize(schemaTypeCode))
					.fields(new ArrayList<>())
					.build();

			MetadataSchema schema = metadataSchemaTypes.getSchemaType(schemaTypeCode).getDefaultSchema();
			schema.getMetadatas().stream()
					.filter(metadata -> metadata.getType() != CONTENT &&
										(metadata.getType() != STRUCTURE || isStructureSupported(metadata)))
					.forEach(metadata -> {
						schemaInfo.getFields().add(SchemaInfoField.builder()
								.name(metadata.getLocalCode())
								.type(getTypeFromMetadata(metadata))
								.multivalue(metadata.isMultivalue())
								.build());

						if (metadata.getType() == REFERENCE) {
							String schemaType = metadata.getReferencedSchemaTypeCode();
							if (schemaTypeCodes.add(schemaType)) {
								schemaTypeCodesStack.push(schemaType);
							}

							addWiringInfo(wiringInfos, capitalize(schemaTypeCode), metadata.getLocalCode(),
									metadata.getLocalCode(), schemaTypeCode, true);
						}
					});
			schemaInfos.add(schemaInfo);
		}

		// Type QUERY
		SchemaInfo schemaInfo = SchemaInfo.builder().keyword("type").type("Query").fields(new ArrayList<>()).build();
		schemaTypeCodes.forEach(schemaTypeCode -> {
			// add ById for every schema type
			String nameById = schemaTypeCode + "ById";
			schemaInfo.getFields().add(SchemaInfoField.builder()
					.name(nameById)
					.type(capitalize(schemaTypeCode))
					.parameters(singletonList(SchemaInfoFieldParameter.builder().name("id").type("ID").required(true).build()))
					.build());
			addWiringInfo(wiringInfos, "Query", nameById, nameById, schemaTypeCode, false);

			// add ByCode if supported
			if (metadataSchemaTypes.getSchemaType(schemaTypeCode).hasMetadataWithCode(Schemas.CODE.getCode())) {
				String nameByCode = schemaTypeCode + "ByCode";
				schemaInfo.getFields().add(SchemaInfoField.builder()
						.name(nameByCode)
						.type(capitalize(schemaTypeCode))
						.parameters(singletonList(
								SchemaInfoFieldParameter.builder().name("code").type("String").required(true).build()))
						.build());
				addWiringInfo(wiringInfos, "Query", nameByCode, nameByCode, schemaTypeCode, false);
			}

			// add search for every schema type
			String nameSearch = "search" + capitalize(schemaTypeCode);
			schemaInfo.getFields().add(SchemaInfoField.builder()
					.name(nameSearch)
					.type(capitalize(schemaTypeCode))
					.multivalue(true)
					.parameters(singletonList(
							SchemaInfoFieldParameter.builder().name("expression").type("String").required(true).build()))
					.build());
			addWiringInfo(wiringInfos, "Query", nameSearch, nameSearch, schemaTypeCode, false);

			// TODO getAll ex: getAllRetentionRule
		});
		schemaInfos.add(schemaInfo);

		// TODO extension for plugins could be added here
	}

	private boolean isStructureSupported(Metadata metadata) {
		return getStructureName(metadata) != null;
	}

	public String buildSchemas() {
		return new GraphqlSchemaBuilder().build(schemaInfos);
	}

	public RuntimeWiring buildDataFetchers(GraphqlDataFetchers graphqlDataFetchers) {
		return new GraphqlProviderBuilder().build(graphqlDataFetchers, wiringInfos, schemaTypeCodes);
	}

	private String getTypeFromMetadata(Metadata metadata) {
		if (metadata.getLocalCode().equalsIgnoreCase("id")) {
			return "ID";
		} else {
			switch (metadata.getType()) {
				case STRING:
				case TEXT:
				case DATE:
				case DATE_TIME:
				case ENUM:
					return "String";
				case NUMBER:
					return "Float";
				case INTEGER:
					return "Int";
				case BOOLEAN:
					return "Boolean";
				case REFERENCE:
					String schemaType = metadata.getReferencedSchemaTypeCode();
					return capitalize(schemaType);
				case STRUCTURE:
					return getStructureSimpleName(metadata);
				case CONTENT:
					return "Content";
				default:
					throw new RuntimeException("Unsupported MetadataValueType " + metadata.getType());
			}
		}
	}

	private String getTypeFromField(Field field) {
		Map<String, String> structureReferenceFields = StructureBindings.getReferenceFields(field.getDeclaringClass());
		if (structureReferenceFields.containsKey(field.getName())) {
			return capitalize(structureReferenceFields.get(field.getName()));
		}

		if (ClassUtils.isAssignable(field.getType(), Boolean.class, true)) {
			return "Boolean";
		} else if (ClassUtils.isAssignable(field.getType(), Integer.class, true)) {
			return "Int";
		} else if (ClassUtils.isAssignable(field.getType(), Number.class, true)) {
			return "Float";
		} else {
			return "String";
		}
	}

	private String adaptStructureFieldName(String fieldName) {
		return fieldName.replace("Id", "");
	}

	private boolean isMultivalue(Field field) {
		return field.getType().isArray() || Collection.class.isAssignableFrom(field.getType());
	}

	private String getStructureName(Metadata metadata) {
		Class<?> structureClass = StructureBindings.getStructure(metadata.getStructureFactory());
		return structureClass != null ? structureClass.getName() : null;
	}

	private String getStructureSimpleName(Metadata metadata) {
		Class<?> structureClass = StructureBindings.getStructure(metadata.getStructureFactory());
		return structureClass != null ? structureClass.getSimpleName() : null;
	}

	private void addWiringInfo(Set<WiringInfo> wiringInfos, String type, String name, String constellioName,
							   String schemaType, boolean reference) {
		wiringInfos.add(WiringInfo.builder().type(type).name(name).constellioName(constellioName)
				.schemaType(schemaType).reference(reference).build());
	}
}
