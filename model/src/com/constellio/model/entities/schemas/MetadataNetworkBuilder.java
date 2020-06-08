package com.constellio.model.entities.schemas;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataNetworkLinkType.AGGREGATION_INPUT;
import static com.constellio.model.entities.schemas.MetadataNetworkLinkType.AUTOMATIC_METADATA_INPUT;
import static com.constellio.model.entities.schemas.MetadataNetworkLinkType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataNetworkLinkType.SEQUENCE_INPUT;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.records.aggregations.MetadataAggregationHandlerFactory.getHandlerFor;
import static java.util.Arrays.asList;

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

		setLinkLevels();

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

	private void setLinkLevels() {

		for (ModifiableMetadataNetworkLink link : links) {
			if (link.getLinkType() == MetadataNetworkLinkType.AGGREGATION_INPUT) {
				link.setLevel(1);
			}
		}

		boolean noLinksModified = false;
		while (!noLinksModified) {
			noLinksModified = true;

			for (ModifiableMetadataNetworkLink link : links) {
				List<ModifiableMetadataNetworkLink> linksToTo = linksFromMetadata.get(link.getToMetadata().getCode());
				if (linksToTo.size() != 0) {
					int level = 0;
					for (ModifiableMetadataNetworkLink linkToTo : linksToTo) {
						level = Math.max(level, linkToTo.level);
					}

					if (link.mustBeEven && level % 2 == 1) {
						level++;
					}
					if (link.mustBeOdd && level % 2 == 0) {
						level++;
					}

					if (link.level != level) {
						noLinksModified = false;
					}

					link.setLevel(level);
				}
			}

		}

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

	public void addNetworkLink(Metadata from, Metadata to, Metadata refMetadata, int level,
							   MetadataNetworkLinkType linkType, boolean mustBeOdd, boolean mustBeEven) {

		List<Metadata> tosIncludingReference = new ArrayList<>();
		tosIncludingReference.add(to);
		tosIncludingReference.add(refMetadata);

		addNetworkLink(from, tosIncludingReference, refMetadata, level, linkType, mustBeOdd, mustBeEven);
	}

	int getDependencyLevelRequiredFor(Metadata from, List<Metadata> tos, boolean mustBeOdd, boolean mustBeEven) {
		int level = 0;

		for (Metadata to : tos) {

			if (linksFromMetadata != null && to != null) {
				if (linksFromMetadata.contains(to.getCode())) {
					int metadataLevel = 0;
					for (ModifiableMetadataNetworkLink link : linksFromMetadata.get(to.getCode())) {
						metadataLevel = Math.max(metadataLevel, link.level);
					}
					level = Math.max(metadataLevel, level);
				} else {
					level = 3;
				}
			}
		}

		if (mustBeOdd && level % 2 != 1) {
			level++;
		}

		if (!mustBeEven && level % 2 != 0) {
			level++;
		}

		return level;
	}

	public void addNetworkLink(Metadata from, List<Metadata> tos, Metadata refMetadata, int level,
							   MetadataNetworkLinkType linkType, boolean mustBeOdd, boolean mustBeEven) {

		for (Metadata to : tos) {
			if (to != null) {
				ModifiableMetadataNetworkLink link = new ModifiableMetadataNetworkLink(from, to, refMetadata, linkType, mustBeOdd,
						mustBeEven);
				links.add(link);
				linksFromMetadata.add(from.getCode(), link);
				linksToMetadata.add(to.getCode(), link);
			}
		}

		if (refMetadata != null) {
			ModifiableMetadataNetworkLink link = new ModifiableMetadataNetworkLink(from, refMetadata, refMetadata,
					MetadataNetworkLinkType.REFERENCE, mustBeOdd, mustBeEven);
			links.add(link);
			linksFromMetadata.add(from.getCode(), link);
			linksToMetadata.add(refMetadata.getCode(), link);
		}

		//ajustingMetadatasLevel(from, level);
	}

	//	private void ajustingMetadatasLevel(Metadata from, int level) {
	//
	//		for (ModifiableMetadataNetworkLink link : linksToMetadata.get(from.getCode())) {
	//			if (link.level < level) {
	//				link.setLevel(level);
	//				ajustingMetadatasLevel(link.getFromMetadata(), level);
	//			}
	//		}
	//
	//	}

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

		if (metadata.getType() == MetadataValueType.REFERENCE) {

			Metadata toMetadata = builder.idMetadataOfType(metadata.getReferencedSchemaTypeCode());

			int level = builder.getDependencyLevelRequiredFor(metadata, asList(toMetadata), false, false);
			builder.addNetworkLink(metadata, asList(toMetadata), null, level, REFERENCE, false, false);

		}

		if (DataEntryType.COPIED == metadata.getDataEntry().getType()) {
			CopiedDataEntry dataEntry = (CopiedDataEntry) metadata.getDataEntry();

			Metadata refMetadata = builder.metadata(dataEntry.getReferenceMetadata());
			Metadata copiedMetadata = builder.metadata(dataEntry.getCopiedMetadata());

			int level = builder.getDependencyLevelRequiredFor(metadata, asList(refMetadata, copiedMetadata), false, true);
			builder.addNetworkLink(metadata, copiedMetadata, refMetadata, level, AUTOMATIC_METADATA_INPUT, false, true);

		} else if (DataEntryType.CALCULATED == metadata.getDataEntry().getType()) {

			CalculatedDataEntry dataEntry = (CalculatedDataEntry) metadata.getDataEntry();
			boolean hasRefDependency = false;
			List<Metadata> metadatas = new ArrayList<>();
			for (Dependency aDependency : dataEntry.getCalculator().getDependencies()) {

				if (aDependency instanceof LocalDependency) {
					LocalDependency dependency = (LocalDependency) aDependency;
					try {
						metadatas.add(schema.getMetadata(dependency.getLocalMetadataCode()));
					} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
						//Metadata may be created later
					}

				} else if (aDependency instanceof ReferenceDependency) {
					hasRefDependency = true;
					try {
						ReferenceDependency dependency = (ReferenceDependency) aDependency;
						Metadata refMetadata = schema.getMetadata(dependency.getLocalMetadataCode());
						MetadataSchemaType referencedType = builder.type(refMetadata.getReferencedSchemaTypeCode());
						Metadata dependentMetadata = referencedType.getDefaultSchema()
								.getMetadata(dependency.getDependentMetadataCode());

						metadatas.add(refMetadata);
						metadatas.add(dependentMetadata);
					} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
						//Metadata may be created later
					}

				}
			}

			int level = builder.getDependencyLevelRequiredFor(metadata, metadatas, false, hasRefDependency);
			for (Dependency aDependency : dataEntry.getCalculator().getDependencies()) {

				if (aDependency instanceof LocalDependency) {
					LocalDependency dependency = (LocalDependency) aDependency;
					try {
						builder.addNetworkLink(metadata, schema.getMetadata(dependency.getLocalMetadataCode()), null, level,
								AUTOMATIC_METADATA_INPUT, false, hasRefDependency);
					} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
						//Metadata may be created later
					}

				} else if (aDependency instanceof ReferenceDependency) {
					try {
						ReferenceDependency dependency = (ReferenceDependency) aDependency;
						Metadata refMetadata = schema.getMetadata(dependency.getLocalMetadataCode());
						MetadataSchemaType referencedType = builder.type(refMetadata.getReferencedSchemaTypeCode());
						Metadata dependentMetadata = referencedType.getDefaultSchema()
								.getMetadata(dependency.getDependentMetadataCode());

						builder.addNetworkLink(metadata, dependentMetadata, refMetadata, level, AUTOMATIC_METADATA_INPUT, false,
								hasRefDependency);

					} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
						//Metadata may be created later
					}

				}
			}

			//			CalculatedDataEntry dataEntry = (CalculatedDataEntry) metadata.getDataEntry();
			//
			//			for (Dependency aDependency : dataEntry.getCalculator().getDependencies()) {
			//
			//
			//
			//				List<Metadata> referencesMetadatas = new ArrayList<>();
			//				List<Metadata> metadatas = new ArrayList<>();
			//				if (aDependency instanceof LocalDependency) {
			//					LocalDependency dependency = (LocalDependency) aDependency;
			//					try {
			//						metadatas.add(metadata);
			//					} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
			//						//Metadata may be created later
			//					}
			//
			//				} else if (aDependency instanceof ReferenceDependency) {
			//					try {
			//						ReferenceDependency dependency = (ReferenceDependency) aDependency;
			//						Metadata refMetadata = schema.getMetadata(dependency.getLocalMetadataCode());
			//						MetadataSchemaType referencedType = builder.type(refMetadata.getReferencedSchemaTypeCode());
			//						Metadata dependentMetadata = referencedType.getDefaultSchema()
			//								.getMetadata(dependency.getDependentMetadataCode());
			//
			//						metadatas.add(dependentMetadata);
			//
			//					} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
			//						//Metadata may be created later
			//					}
			//
			//				}
			//
			//				int level = builder.getDependencyLevelRequiredFor(metadata, asList(refMetadata, copiedMetadata), false);
			//				builder.addNetworkLink(metadata, copiedMetadata, refMetadata, level, AUTOMATIC_METADATA_INPUT);
			//
			//				builder.addNetworkLink(metadata, metadatas, null, false, AUTOMATIC_METADATA_INPUT);
			//			}

		} else if (DataEntryType.SEQUENCE == metadata.getDataEntry().getType()) {
			SequenceDataEntry dataEntry = (SequenceDataEntry) metadata.getDataEntry();
			if (dataEntry.getMetadataProvidingSequenceCode() != null) {

				List<Metadata> metadatas = new ArrayList<>();

				if (dataEntry.getMetadataProvidingSequenceCode().contains(".")) {
					String[] splittedCode = dataEntry.getMetadataProvidingSequenceCode().split("\\.");
					Metadata firstMetadata = schema.getMetadata(splittedCode[0]);
					Metadata secondMetadata = builder.type(firstMetadata.getReferencedSchemaTypeCode()).getDefaultSchema()
							.getMetadata(splittedCode[1]);
					metadatas.add(firstMetadata);
					metadatas.add(secondMetadata);
				} else {
					metadatas.add(schema.getMetadata(dataEntry.getMetadataProvidingSequenceCode()));
				}

				for (Metadata sequenceInputMetadata : metadatas) {
					int level = builder.getDependencyLevelRequiredFor(metadata, asList(sequenceInputMetadata), false, true);
					builder.addNetworkLink(metadata, sequenceInputMetadata, null, level, SEQUENCE_INPUT, false, true);
				}
			}

		} else if (DataEntryType.AGGREGATED == metadata.getDataEntry().getType()) {
			AggregatedDataEntry dataEntry = (AggregatedDataEntry) metadata.getDataEntry();

			List<String> referenceMetadatas = dataEntry.getReferenceMetadatas();
			for (String referenceMetadata : referenceMetadatas) {
				Metadata refMetadata = builder.metadata(referenceMetadata);

				GetMetadatasUsedToCalculateParams params = new GetMetadatasUsedToCalculateParams(metadata, referenceMetadata) {
					@Override
					public Metadata getMetadata(String metadataCode) {
						return builder.metadata(metadataCode);
					}
				};

				List<Metadata> tos = getHandlerFor(metadata).getMetadatasUsedToCalculate(params);
				int level = builder.getDependencyLevelRequiredFor(metadata, tos, true, false);
				builder.addNetworkLink(metadata, tos, refMetadata, level, AGGREGATION_INPUT, true, false);
			}
		}
	}

}
