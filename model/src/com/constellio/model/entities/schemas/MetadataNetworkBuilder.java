package com.constellio.model.entities.schemas;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.data.utils.KeyListMap;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.entries.AggregatedDataEntry;
import com.constellio.model.entities.schemas.entries.AggregationType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.entries.SequenceDataEntry;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.utils.DependencyUtils;

public class MetadataNetworkBuilder {

	List<MetadataSchemaType> types;
	Map<String, MetadataSchemaType> typesMap = new HashMap<>();
	List<ModifiableMetadataNetworkLink> links = new ArrayList<>();
	KeyListMap<String, ModifiableMetadataNetworkLink> linksFromMetadata = new KeyListMap<>();
	KeyListMap<String, ModifiableMetadataNetworkLink> linksToMetadata = new KeyListMap<>();
	Map<Integer, KeySetMap<String, String>> dependencies = new HashMap<>();
	SchemaUtils schemaUtils = new SchemaUtils();
	DependencyUtils dependencyUtils = new DependencyUtils();

	public MetadataNetworkBuilder(List<MetadataSchemaType> types) {
		this.types = types;
		typesMap = new HashMap<>();
		for (MetadataSchemaType type : types) {
			typesMap.put(type.getCode(), type);
		}
	}

	public MetadataNetwork build() {
		List<MetadataNetworkLink> links = new ArrayList<>();
		KeyListMap<String, MetadataNetworkLink> linksFromMetadata = new KeyListMap<>();
		KeyListMap<String, MetadataNetworkLink> linksToMetadata = new KeyListMap<>();
		for (ModifiableMetadataNetworkLink link : this.links) {
			MetadataNetworkLink finalLink = new MetadataNetworkLink(link.fromMetadata, link.toMetadata, link.level);
			links.add(finalLink);
			linksFromMetadata.add(finalLink.getFromMetadata().getCode(), finalLink);
			linksToMetadata.add(finalLink.getToMetadata().getCode(), finalLink);
		}

		return new MetadataNetwork(
				Collections.unmodifiableList(links),
				Collections.unmodifiableMap(linksFromMetadata.getNestedMap()),
				Collections.unmodifiableMap(linksToMetadata.getNestedMap())
		);
	}

	private Metadata idMetadataOfType(String code) {

		MetadataSchemaType type = type(code);

		return type == null ? null : type.getDefaultSchema().getMetadata(IDENTIFIER.getCode());
	}

	private MetadataSchemaType type(String code) {
		return typesMap.get(code);
	}

	private Metadata metadata(String code) {
		String schemaTypeCode = schemaUtils.getSchemaTypeCode(code);
		return type(schemaTypeCode).getMetadata(code);
	}

	public void addNetworkLink(Metadata from, List<Metadata> tos, boolean increasingLevel) {
		int level = 0;

		String fromSchemaType = schemaUtils.getSchemaTypeCode(from);

		for (Metadata to : tos) {
			int metadataLevel = 0;
			if (linksFromMetadata != null && to != null && linksFromMetadata.contains(to.getCode())) {
				for (ModifiableMetadataNetworkLink link : linksFromMetadata.get(to.getCode())) {
					metadataLevel = Math.max(metadataLevel, link.level);
				}

				String toSchemaType = schemaUtils.getSchemaTypeCode(to);

				level = Math.max(metadataLevel, level);
			}
		}

		if (increasingLevel || from.isIncreasedDependencyLevel()) {
			level++;
		}

		for (Metadata to : tos) {
			if (to != null) {
				ModifiableMetadataNetworkLink link = new ModifiableMetadataNetworkLink(from, to, level);
				//			String message = "Adding " + link.fromMetadata.getCode() + "->" + link.toMetadata.getCode() + " [" + level + "]";
				//			if (message.contains("ze") || message.contains("another") || message.contains("aThird")) {
				//				System.out.println(message);
				//			}
				links.add(link);
				linksFromMetadata.add(from.getCode(), link);
				linksToMetadata.add(to.getCode(), link);
			}
		}

		ajustingMetadatasLevel(from, level);
	}

	private void ajustingMetadatasLevel(Metadata from, int level) {

		for (ModifiableMetadataNetworkLink link : linksToMetadata.get(from.getCode())) {
			if (link.level < level) {
				link.setLevel(level);
				ajustingMetadatasLevel(link.getFromMetadata(), level);
			}
		}

	}

	private KeySetMap<String, String> getDependencies(int level) {
		KeySetMap<String, String> levelDependencies = this.dependencies.get(level);
		if (levelDependencies == null) {
			levelDependencies = new KeySetMap<>();
			this.dependencies.put(level, levelDependencies);
		}
		return levelDependencies;
	}

	public static MetadataNetwork buildFrom(List<MetadataSchemaType> types) {

		MetadataNetworkBuilder builder = new MetadataNetworkBuilder(types);

		for (MetadataSchemaType type : types) {
			for (MetadataSchema schema : type.getAllSchemas()) {
				for (Metadata metadata : schema.getMetadatas()) {

					if (metadata.getInheritance() == null) {
						build(builder, type, schema, metadata);
					}
				}
			}
		}
		return builder.build();
	}

	private static void build(MetadataNetworkBuilder builder, MetadataSchemaType type, MetadataSchema schema,
			Metadata metadata) {

		if (metadata.getLocalCode().equals("refText")) {
			System.out.println("todo");
		}

		if (metadata.getType() == MetadataValueType.REFERENCE) {

			Metadata toMetadata = builder.idMetadataOfType(metadata.getReferencedSchemaType());

			if (metadata != null) {
				builder.addNetworkLink(metadata, asList(toMetadata), false);
			}

		} else if (DataEntryType.COPIED == metadata.getDataEntry().getType()) {
			CopiedDataEntry dataEntry = (CopiedDataEntry) metadata.getDataEntry();
			List<Metadata> metadatas = asList(builder.metadata(dataEntry.getCopiedMetadata()),
					builder.metadata(dataEntry.getReferenceMetadata()));
			builder.addNetworkLink(metadata, metadatas, false);

		} else if (DataEntryType.CALCULATED == metadata.getDataEntry().getType()) {
			CalculatedDataEntry dataEntry = (CalculatedDataEntry) metadata.getDataEntry();

			List<Metadata> metadatas = new ArrayList<>();
			for (Dependency aDependency : dataEntry.getCalculator().getDependencies()) {

				if (aDependency instanceof LocalDependency) {
					LocalDependency dependency = (LocalDependency) aDependency;
					metadatas.add(schema.getMetadata(dependency.getLocalMetadataCode()));

				} else if (aDependency instanceof ReferenceDependency) {
					ReferenceDependency dependency = (ReferenceDependency) aDependency;
					Metadata refMetadata = schema.getMetadata(dependency.getLocalMetadataCode());
					MetadataSchemaType referencedType = builder.type(refMetadata.getReferencedSchemaType());
					Metadata dependentMetadata = referencedType.getDefaultSchema()
							.getMetadata(dependency.getDependentMetadataCode());

					metadatas.add(refMetadata);
					metadatas.add(dependentMetadata);

				}
			}
			builder.addNetworkLink(metadata, metadatas, false);

		} else if (DataEntryType.SEQUENCE == metadata.getDataEntry().getType()) {
			SequenceDataEntry dataEntry = (SequenceDataEntry) metadata.getDataEntry();
			if (dataEntry.getMetadataProvidingSequenceCode() != null) {
				Metadata sequenceInputMetadata = schema.getMetadata(dataEntry.getMetadataProvidingSequenceCode());
				builder.addNetworkLink(metadata, asList(sequenceInputMetadata), false);
			}

		} else if (DataEntryType.AGGREGATED == metadata.getDataEntry().getType()) {
			AggregatedDataEntry dataEntry = (AggregatedDataEntry) metadata.getDataEntry();

			List<Metadata> metadatas = new ArrayList<>();
			metadatas.add(builder.metadata(dataEntry.getReferenceMetadata()));
			if (dataEntry.getAgregationType() == AggregationType.SUM) {
				metadatas.add(builder.metadata(dataEntry.getInputMetadata()));
			}
			builder.addNetworkLink(metadata, metadatas, true);

		}
	}

}
