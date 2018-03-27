package com.constellio.model.entities.schemas;

import static com.constellio.model.entities.schemas.MetadataNetworkLinkType.AGGREGATION_INPUT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.constellio.data.utils.KeyListMap;

public class MetadataNetwork implements Serializable {

	List<MetadataNetworkLink> links;
	Map<String, List<MetadataNetworkLink>> linksFromMetadata;
	Map<String, List<MetadataNetworkLink>> linksToMetadata;

	public MetadataNetwork(List<MetadataNetworkLink> links,
			Map<String, List<MetadataNetworkLink>> linksFromMetadata,
			Map<String, List<MetadataNetworkLink>> linksToMetadata) {
		this.links = links;
		this.linksFromMetadata = linksFromMetadata;
		this.linksToMetadata = linksToMetadata;
	}

	public List<MetadataNetworkLink> getLinks() {
		return links;
	}

	public List<MetadataNetworkLink> getLinksWithinSchemaType(String schemaType) {
		List<MetadataNetworkLink> linksWithinSchemaType = new ArrayList<>();

		for (MetadataNetworkLink link : links) {
			if (link.getFromMetadata().getCode().startsWith(schemaType)
					|| link.getToMetadata().getCode().startsWith(schemaType)) {
				linksWithinSchemaType.add(link);
			}
		}

		return linksWithinSchemaType;
	}

	public Map<String, List<MetadataNetworkLink>> getAggregationMetadataNetworkLinkRegroupedByReference(String schemaType) {
		KeyListMap<String, MetadataNetworkLink> linksWithinSchemaType = new KeyListMap<>();

		for (MetadataNetworkLink link : links) {
			if (link.getLinkType() == AGGREGATION_INPUT && link.getToMetadata().getSchemaTypeCode().equals(schemaType)) {
				linksWithinSchemaType.add(link.refMetadata.getCode(), link);
			}

		}

		return linksWithinSchemaType.getNestedMap();
	}

	public List<MetadataNetworkLink> getAggregationMetadataNetworkLinksFromSchemaType(String schemaType) {
		List<MetadataNetworkLink> returnedLinks = new ArrayList<>();

		for (MetadataNetworkLink link : links) {
			if (link.getLinkType() == AGGREGATION_INPUT && link.getFromMetadata().getSchemaTypeCode().equals(schemaType)) {
				returnedLinks.add(link);
			}

		}

		return returnedLinks;
	}

	public List<MetadataNetworkLink> getLinksFrom(String metadataCode) {
		List<MetadataNetworkLink> links = linksFromMetadata.get(metadataCode);
		return links == null ? Collections.<MetadataNetworkLink>emptyList() : Collections.unmodifiableList(links);
	}

	public List<MetadataNetworkLink> getLinksFrom(Metadata metadata) {
		List<MetadataNetworkLink> links = linksFromMetadata.get(metadata.getCode());
		return links == null ? Collections.<MetadataNetworkLink>emptyList() : Collections.unmodifiableList(links);
	}

	public List<MetadataNetworkLink> getLinksTo(String metadataCode) {
		List<MetadataNetworkLink> links = linksToMetadata.get(metadataCode);
		return links == null ? Collections.<MetadataNetworkLink>emptyList() : Collections.unmodifiableList(links);
	}

	public List<MetadataNetworkLink> getLinksTo(Metadata metadata) {
		List<MetadataNetworkLink> links = linksToMetadata.get(metadata.getCode());
		return links == null ? Collections.<MetadataNetworkLink>emptyList() : Collections.unmodifiableList(links);
	}

	public static MetadataNetwork EMPTY() {
		return new MetadataNetwork(Collections.<MetadataNetworkLink>emptyList(),
				Collections.<String, List<MetadataNetworkLink>>emptyMap(),
				Collections.<String, List<MetadataNetworkLink>>emptyMap()
		);
	}

	public int getMaxLevelOf(String typeCode) {
		int max = 0;

		for (MetadataNetworkLink link : links) {
			if (link.getFromMetadata().getCode().startsWith(typeCode + "_")) {
				max = Math.max(max, link.getLevel());
			}
		}
		return max;
	}
}
