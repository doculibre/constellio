package com.constellio.app.api.graphql.builder;

import com.constellio.app.api.graphql.builder.SchemaInfo.SchemaInfoField;
import com.constellio.data.conf.FoldersLocator;
import com.google.common.base.Charsets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.api.graphql.utils.GraphqlUtils.SCHEMAS_FILENAME;
import static java.lang.System.lineSeparator;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static org.apache.commons.lang.StringUtils.capitalize;

public class GraphqlSchemaBuilder {

	public String build(final List<SchemaInfo> schemaInfos) {
		try {
			String data = buildSchemasData(schemaInfos);
			//writeToSchemaFile(data);
			return data;
		} catch (Exception e) {
			throw new RuntimeException("Failed to update file " + SCHEMAS_FILENAME);
		}
	}

	public void writeToSchemaFile(String data) throws IOException {
		File schemasFile = new File(new FoldersLocator().getResourcesFolder(), SCHEMAS_FILENAME);
		Files.deleteIfExists(schemasFile.toPath());

		write(schemasFile, data);
	}

	private String buildSchemasData(final List<SchemaInfo> schemaInfos) {
		StringBuilder builder = new StringBuilder();
		builder.append("# Generated using ").append(getClass().getSimpleName()).append(".java").append(lineSeparator());

		schemaInfos.stream()
				.filter(schemaInfo -> schemaInfo.getKeyword().equals("type") && schemaInfo.getType().equals("Query"))
				.findFirst()
				.ifPresent(schemaInfo -> {
					builder.append("type Query {").append(lineSeparator());
					schemaInfo.getFields().forEach(field -> {
						builder.append("\t").append(field.getName()).append(getParameters(field)).append(": ")
								.append(getType(field.getType(), field.isMultivalue())).append(lineSeparator());
					});
					builder.append("}");
				});

		schemaInfos.stream()
				.filter(schemaInfo -> schemaInfo.getKeyword().equals("type") && !schemaInfo.getType().equals("Query"))
				.forEach(schemaInfo -> {
					builder.append(lineSeparator()).append(lineSeparator())
							.append("type ").append(capitalize(schemaInfo.getType())).append(" {").append(lineSeparator());
					schemaInfo.getFields().forEach(field -> {
						builder.append("\t").append(field.getName()).append(": ")
								.append(getType(field.getType(), field.isMultivalue())).append(lineSeparator());
					});
					builder.append("}");
				});

		return builder.toString();
	}

	private String getParameters(SchemaInfoField field) {
		String parameters = field.getParameters().stream().map(parameter -> {
			String parameterString = parameter.getName().concat(":").concat(parameter.getType());
			if (parameter.isMultivalue()) {
				parameterString += "!";
			}
			return parameterString;
		}).collect(Collectors.joining(", "));
		return "(" + parameters + ")";
	}

	private String getType(String type, boolean multivalue) {
		return multivalue ? "[" + type + "]" : type;
	}

	private void write(File file, String value) throws IOException {
		Files.write(file.toPath(), value.getBytes(Charsets.UTF_8), CREATE, APPEND);
	}

}
