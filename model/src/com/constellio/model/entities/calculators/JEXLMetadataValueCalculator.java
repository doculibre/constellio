package com.constellio.model.entities.calculators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlInfo;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.utils.Parametrized;

public class JEXLMetadataValueCalculator implements InitializedMetadataValueCalculator<Object>, Parametrized {

	MetadataValueType type;
	String metadataCode;
	JexlScript jexlScript;
	Set<List<String>> variables;

	private static final Logger LOGGER = LoggerFactory.getLogger(JEXLMetadataValueCalculator.class);

	String expression;
	List<Dependency> dependencies = new ArrayList<>();

	public JEXLMetadataValueCalculator(String expression) {
		this.expression = expression;
	}

	public JexlScript getJexlScript(){
		return jexlScript;
	}

	@Override
	public Object calculate(CalculatorParameters parameters) {

		JexlContext jc = prepareJexlContext(parameters);

		try {
			return jexlScript.execute(jc);

		} catch (JexlException e) {
			logJexlException(e);
			return null;
		} catch (Exception e) {

			Throwable t = e.getCause();

			e.printStackTrace();
			return null;
		}

	}

	private void logJexlException(JexlException e) {
		JexlInfo info = e.getInfo();
		String cause = StringUtils.substringAfterLast(e.getCause().getClass().getName(), ".");

		StringBuilder message = new StringBuilder().append("Jexl Script of metadata '").append(metadataCode)
				.append("' failed at column ").append(info.getColumn()).append(" of line ").append(info.getLine())
				.append(" : ").append(cause);

		if (e.getCause().getMessage() != null) {
			message.append(" ").append(e.getCause().getMessage());
		}

		LOGGER.error(message.toString());
	}

	private JexlContext prepareJexlContext(CalculatorParameters parameters) {
		JexlContext jc = new MapContext();

		for (Dependency dependency : dependencies) {
			if (dependency instanceof LocalDependency) {
				LocalDependency<?> localDependency = (LocalDependency<?>) dependency;
				String key = localDependency.getLocalMetadataCode();
				jc.set(key, parameters.get(localDependency));

			} else {
				ReferenceDependency<?> referenceDependency = (ReferenceDependency<?>) dependency;
				String key = referenceDependency.getLocalMetadataCode();

				Map<String, Object> map = new HashMap<>();
				map.put(referenceDependency.getDependentMetadataCode(), parameters.get(referenceDependency));

				jc.set(key, map);
			}
		}
		return jc;
	}

	@Override
	public Object getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return type;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return dependencies;
	}

	@Override
	public Object[] getInstanceParameters() {
		return new Object[] { expression };
	}

	@Override
	public void initialize(MetadataSchemaTypes types, MetadataSchema schema, Metadata calculatedMetadata) {
		metadataCode = calculatedMetadata.getCode();
		try {

			JexlEngine jexl = new JexlBuilder().create();
			jexlScript = jexl.createScript(expression);

			for (List<String> variable : jexlScript.getVariables()) {
				if (variable.size() == 2) {
					dependencies.add(toReferenceDependency(types, schema, variable));

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			dependencies.clear();
		}

	}

	@Override
	public void initialize(List<Metadata> schemaMetadatas, Metadata calculatedMetadata) {
		metadataCode = calculatedMetadata.getCode();
		type = calculatedMetadata.getType();
		dependencies.clear();
		try {

			JexlEngine jexl = new JexlBuilder().create();
			jexlScript = jexl.createScript(expression);
			variables = jexlScript.getVariables();
		} catch (Exception e) {
			e.printStackTrace();

		}

		if (variables != null) {
			for (List<String> variable : jexlScript.getVariables()) {
				if (variable.size() == 1) {
					dependencies.add(toLocalDependency(schemaMetadatas, variable));

				}
			}

		}

	}

	private LocalDependency toLocalDependency(MetadataSchema schema, List<String> variable) {
		boolean isRequired = false;
		Metadata metadata = schema.getMetadata(variable.get(0));
		return new LocalDependency<>(variable.get(0), isRequired, metadata.isMultivalue(),
				metadata.getType());
	}

	private LocalDependency toLocalDependency(List<Metadata> metadatas, List<String> variable) {
		boolean isRequired = false;
		Metadata metadata = null;

		for (Metadata aMetadata : metadatas) {
			if (aMetadata.getLocalCode().equals(variable.get(0))) {
				metadata = aMetadata;
				break;
			}
		}
		return new LocalDependency<>(variable.get(0), isRequired, metadata.isMultivalue(),
				metadata.getType());
	}

	private ReferenceDependency toReferenceDependency(MetadataSchemaTypes types, MetadataSchema schema, List<String> variable) {
		Metadata referenceMetadata = schema.getMetadata(variable.get(0));
		String referencedSchemaTypeCode = referenceMetadata.getAllowedReferences().getTypeWithAllowedSchemas();
		MetadataSchema referencedSchema = types.getDefaultSchema(referencedSchemaTypeCode);
		Metadata copiedMetadata = referencedSchema.getMetadata(variable.get(1));

		boolean isRequired = false;
		boolean isMultivalue = copiedMetadata.isMultivalue() || referenceMetadata.isMultivalue();
		boolean isGroupedByReferences = false;
		return new ReferenceDependency<>(variable.get(0), variable.get(1), isRequired,
				isMultivalue,
				copiedMetadata.getType(), isGroupedByReferences);
	}

}
