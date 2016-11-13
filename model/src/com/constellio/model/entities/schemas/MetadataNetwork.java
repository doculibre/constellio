package com.constellio.model.entities.schemas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.data.utils.KeySetMap;
import com.itextpdf.text.Meta;

public class MetadataNetwork {

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

	public List<MetadataNetworkLink> getLinksFrom(Metadata metadata) {
		return Collections.unmodifiableList(linksFromMetadata.get(metadata));
	}

	public List<MetadataNetworkLink> getLinksTo(Metadata metadata) {
		return Collections.unmodifiableList(linksToMetadata.get(metadata));
	}

	public static MetadataNetwork EMPTY() {
		return new MetadataNetwork(Collections.<MetadataNetworkLink>emptyList(),
				Collections.<String, List<MetadataNetworkLink>>emptyMap(),
				Collections.<String, List<MetadataNetworkLink>>emptyMap()
		);
	}
}
