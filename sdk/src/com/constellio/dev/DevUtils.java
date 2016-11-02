package com.constellio.dev;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class DevUtils {

	public static void addMetadataListingReferencesInAllSchemaTypes(AppLayerFactory appLayerFactory) {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
			modelLayerFactory.getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
				@Override
				public void alter(MetadataSchemaTypesBuilder types) {
					for (MetadataSchemaTypeBuilder typeBuilder : types.getTypes()) {
						typeBuilder.getDefaultSchema().create("allReferences").setType(STRING).setMultivalue(true)
								.defineDataEntry().asCalculated(AllReferencesCalculator.class);
					}
				}
			});

		}

		ReindexingServices reindexingServices = modelLayerFactory.newReindexingServices();
		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

	}

	public static class AllReferencesCalculator implements MetadataValueCalculator<List<String>> {

		DynamicLocalDependency dependency = new DynamicLocalDependency() {
			@Override
			public boolean isDependentOf(Metadata metadata) {
				return metadata.getType() == REFERENCE;
			}
		};

		@Override
		public List<String> calculate(CalculatorParameters parameters) {
			DynamicDependencyValues values = parameters.get(dependency);
			Set<String> returnedValues = new HashSet<>();
			for (Metadata metadata : values.getAvailableMetadatas()) {
				Object o = values.getValue(metadata);
				if (o != null) {
					if (metadata.isMultivalue()) {
						returnedValues.addAll((List) o);
					} else {
						returnedValues.add((String) o);
					}
				}
			}

			return new ArrayList<>(returnedValues);
		}

		@Override
		public List<String> getDefaultValue() {
			return new ArrayList<>();
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRING;
		}

		@Override
		public boolean isMultiValue() {
			return true;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(dependency);
		}
	}

}
