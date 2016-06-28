package com.constellio.model.entities.calculators;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class StringPatternMetadataValueCalculator implements InitializedMetadataValueCalculator<String>, Parametrized {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringPatternMetadataValueCalculator.class);

	String pattern;
	List<Dependency> dependencies = new ArrayList<>();
	Map<String, Dependency> dependenciesByPlaceholderMap = new HashMap<>();
	List<String> placeHolders;

	public StringPatternMetadataValueCalculator(String pattern) {
		this.pattern = pattern;
		this.placeHolders = extractPlaceHoldersFromPattern(pattern);
	}

	@Override
	public String calculate(CalculatorParameters parameters) {
		return null;
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
		return new Object[] { pattern };
	}

	@Override
	public void initialize(MetadataSchemaTypes types, MetadataSchema schema) {

		try {
			for (String placeHolder : placeHolders) {
				String[] placeHolderParts = placeHolder.split("\\.");
				if (placeHolderParts.length == 2) {

					Metadata referenceMetadata = schema.getMetadata(placeHolderParts[0]);
					String referencedSchemaTypeCode = referenceMetadata.getAllowedReferences().getTypeWithAllowedSchemas();
					MetadataSchema referencedSchema = types.getDefaultSchema(referencedSchemaTypeCode);
					Metadata copiedMetadata = referencedSchema.getMetadata(placeHolderParts[1]);

					boolean isRequired = true;
					boolean isMultivalue = copiedMetadata.isMultivalue() || referenceMetadata.isMultivalue();
					boolean isGroupedByReferences = false;
					dependencies.add(new ReferenceDependency<>(placeHolderParts[0], placeHolderParts[1], isRequired, isMultivalue,
							copiedMetadata.getType(), isGroupedByReferences));

				} else {
					boolean isRequired = true;
					Metadata metadata = schema.getMetadata(placeHolder);
					dependencies.add(new LocalDependency<>(placeHolder, isRequired, metadata.isMultivalue(),
							metadata.getType()));
				}
			}

		} catch (Exception e) {
			LOGGER.warn("Pattern '" + pattern + "' is invalid", e);
			dependencies.clear();
		}

	}

	public static List<String> extractPlaceHoldersFromPattern(String pattern) {
		List<String> placeHolders = new ArrayList<>();

		int currentPlaceHoderStart = 0;

		for (int i = 0; i < pattern.length(); i++) {
			if (pattern.charAt(i) == '{') {
				currentPlaceHoderStart = i;
			} else if (pattern.charAt(i) == '}') {
				if (currentPlaceHoderStart != -1) {
					placeHolders.add(pattern.substring(currentPlaceHoderStart + 1, i));
				}
				currentPlaceHoderStart = -1;
			}
		}

		return placeHolders;
	}
}
