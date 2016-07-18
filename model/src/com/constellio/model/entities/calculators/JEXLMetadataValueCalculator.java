package com.constellio.model.entities.calculators;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
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

public class JEXLMetadataValueCalculator implements InitializedMetadataValueCalculator<String>, Parametrized {

	JexlScript jexlScript;

	private static final Logger LOGGER = LoggerFactory.getLogger(JEXLMetadataValueCalculator.class);

	String expression;
	List<Dependency> dependencies = new ArrayList<>();

	public JEXLMetadataValueCalculator(String expression) {
		this.expression = expression;
	}

	@Override
	public String calculate(CalculatorParameters parameters) {

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

		try {
			Object result = jexlScript.execute(jc);
			return (String) result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return STRING;
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
	public void initialize(MetadataSchemaTypes types, MetadataSchema schema) {

		System.out.println("initialized!!");
		try {

			JexlEngine jexl = new JexlBuilder().create();

			// Create an expression
			jexlScript = jexl.createScript(expression);

			for (List<String> variable : jexlScript.getVariables()) {
				if (variable.size() == 2) {

					Metadata referenceMetadata = schema.getMetadata(variable.get(0));
					String referencedSchemaTypeCode = referenceMetadata.getAllowedReferences().getTypeWithAllowedSchemas();
					MetadataSchema referencedSchema = types.getDefaultSchema(referencedSchemaTypeCode);
					Metadata copiedMetadata = referencedSchema.getMetadata(variable.get(1));

					boolean isRequired = false;
					boolean isMultivalue = copiedMetadata.isMultivalue() || referenceMetadata.isMultivalue();
					boolean isGroupedByReferences = false;
					dependencies.add(new ReferenceDependency<>(variable.get(0), variable.get(1), isRequired, isMultivalue,
							copiedMetadata.getType(), isGroupedByReferences));
				} else {
					boolean isRequired = false;
					Metadata metadata = schema.getMetadata(variable.get(0));
					dependencies.add(new LocalDependency<>(variable.get(0), isRequired, metadata.isMultivalue(),
							metadata.getType()));

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			dependencies.clear();
		}

	}

}
