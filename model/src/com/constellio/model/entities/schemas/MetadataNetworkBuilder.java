package com.constellio.model.entities.schemas;

import static com.constellio.model.entities.schemas.MetadataNetworkLinkType.AGGREGATION_INPUT;
import static com.constellio.model.entities.schemas.MetadataNetworkLinkType.AUTOMATIC_METADATA_INPUT;
import static com.constellio.model.entities.schemas.MetadataNetworkLinkType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataNetworkLinkType.SEQUENCE_INPUT;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.records.aggregations.MetadataAggregationHandlerFactory.getHandlerFor;
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
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.entries.SequenceDataEntry;
import com.constellio.model.services.records.aggregations.GetMetadatasUsedToCalculateParams;
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
			MetadataNetworkLink finalLink = new MetadataNetworkLink(link.fromMetadata, link.toMetadata, link.refMetadata,
					link.level, link.linkType);
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

	public void addNetworkLink(Metadata from, Metadata to, Metadata refMetadata, boolean increasingLevel,
			MetadataNetworkLinkType linkType) {

		List<Metadata> tosIncludingReference = new ArrayList<>();
		tosIncludingReference.add(to);
		tosIncludingReference.add(refMetadata);

		addNetworkLink(from, tosIncludingReference, refMetadata, increasingLevel, linkType);
	}

	public void addNetworkLink(Metadata from, List<Metadata> tos, Metadata refMetadata, boolean increasingLevel,
			MetadataNetworkLinkType linkType) {
		int level = 0;

		String fromSchemaType = schemaUtils.getSchemaTypeCode(from);

		for (Metadata to : tos) {

			if (linksFromMetadata != null && to != null && linksFromMetadata.contains(to.getCode())) {
				int metadataLevel = 0;
				for (ModifiableMetadataNetworkLink link : linksFromMetadata.get(to.getCode())) {
					metadataLevel = Math.max(metadataLevel, link.level);
				}
				level = Math.max(metadataLevel, level);
			}
		}

		if (refMetadata != null) {
			if (linksFromMetadata != null && refMetadata != null && linksFromMetadata.contains(refMetadata.getCode())) {
				int metadataLevel = 0;
				for (ModifiableMetadataNetworkLink link : linksFromMetadata.get(refMetadata.getCode())) {
					metadataLevel = Math.max(metadataLevel, link.level);
				}
				level = Math.max(metadataLevel, level);
			}
		}

		if (increasingLevel || from.isIncreasedDependencyLevel()) {
			level++;
		}

		for (Metadata to : tos) {
			if (to != null) {
				ModifiableMetadataNetworkLink link = new ModifiableMetadataNetworkLink(from, to, refMetadata, level, linkType);
				links.add(link);
				linksFromMetadata.add(from.getCode(), link);
				linksToMetadata.add(to.getCode(), link);
			}
		}

		if (refMetadata != null) {
			ModifiableMetadataNetworkLink link = new ModifiableMetadataNetworkLink(from, refMetadata, refMetadata, level,
					MetadataNetworkLinkType.REFERENCE);
			links.add(link);
			linksFromMetadata.add(from.getCode(), link);
			linksToMetadata.add(refMetadata.getCode(), link);
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
						build(builder, schema, metadata);
					}
				}
			}
		}
		return builder.build();
	}

	private static void build(final MetadataNetworkBuilder builder, MetadataSchema schema, Metadata metadata) {

		if (metadata.getLocalCode().equals("refText")) {
			System.out.println("todo");
		}

		if (metadata.getType() == MetadataValueType.REFERENCE) {

			Metadata toMetadata = builder.idMetadataOfType(metadata.getReferencedSchemaType());

			builder.addNetworkLink(metadata, asList(toMetadata), null, false, REFERENCE);

		} else if (DataEntryType.COPIED == metadata.getDataEntry().getType()) {
			CopiedDataEntry dataEntry = (CopiedDataEntry) metadata.getDataEntry();

			Metadata refMetadata = builder.metadata(dataEntry.getReferenceMetadata());

			builder.addNetworkLink(metadata, builder.metadata(dataEntry.getCopiedMetadata()), refMetadata, false,
					AUTOMATIC_METADATA_INPUT);

		} else if (DataEntryType.CALCULATED == metadata.getDataEntry().getType()) {
			CalculatedDataEntry dataEntry = (CalculatedDataEntry) metadata.getDataEntry();

			for (Dependency aDependency : dataEntry.getCalculator().getDependencies()) {

				if (aDependency instanceof LocalDependency) {
					LocalDependency dependency = (LocalDependency) aDependency;
					try {
						builder.addNetworkLink(metadata, schema.getMetadata(dependency.getLocalMetadataCode()), null, false,
								AUTOMATIC_METADATA_INPUT);
					} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
						//Metadata may be created later
					}

				} else if (aDependency instanceof ReferenceDependency) {
					try {
						ReferenceDependency dependency = (ReferenceDependency) aDependency;
						Metadata refMetadata = schema.getMetadata(dependency.getLocalMetadataCode());
						MetadataSchemaType referencedType = builder.type(refMetadata.getReferencedSchemaType());
						Metadata dependentMetadata = referencedType.getDefaultSchema()
								.getMetadata(dependency.getDependentMetadataCode());

						builder.addNetworkLink(metadata, dependentMetadata, refMetadata, false, AUTOMATIC_METADATA_INPUT);
					} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
						//Metadata may be created later
					}

				}
			}

		} else if (DataEntryType.SEQUENCE == metadata.getDataEntry().getType()) {
			SequenceDataEntry dataEntry = (SequenceDataEntry) metadata.getDataEntry();
			if (dataEntry.getMetadataProvidingSequenceCode() != null) {
				Metadata sequenceInputMetadata = schema.getMetadata(dataEntry.getMetadataProvidingSequenceCode());
				builder.addNetworkLink(metadata, sequenceInputMetadata, null, false, SEQUENCE_INPUT);
			}

		} else if (DataEntryType.AGGREGATED == metadata.getDataEntry().getType()) {
			AggregatedDataEntry dataEntry = (AggregatedDataEntry) metadata.getDataEntry();

			Metadata refMetadata = builder.metadata(dataEntry.getReferenceMetadata());

			GetMetadatasUsedToCalculateParams params = new GetMetadatasUsedToCalculateParams(metadata) {

				@Override
				public Metadata getMetadata(String metadataCode) {
					return builder.metadata(metadataCode);
				}
			};

			builder.addNetworkLink(metadata, getHandlerFor(metadata).getMetadatasUsedToCalculate(params), refMetadata, true,
					AGGREGATION_INPUT);

		}
	}

}
