package com.constellio.model.entities.schemas;

import com.constellio.model.services.schemas.SchemaUtils;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class LegacyGlobalMetadatas {

	private static List<Metadata> allLegacyGlobalMetadatas = new ArrayList<>();

	public static final Metadata AUTHORIZATIONS = add(new Metadata("authorizations_ss", STRING, true));
	public static final Metadata INHERITED_AUTHORIZATIONS = add(new Metadata("inheritedauthorizations_ss", STRING, true));
	public static final Metadata ALL_AUTHORIZATIONS = add(new Metadata("allauthorizations_ss", STRING, true));
	public static final Metadata FOLLOWERS = add(new Metadata("followers_ss", STRING, true));
	public static final Metadata SEARCHABLE = add(new Metadata("searchable_s", BOOLEAN, false));

	public static Metadata add(Metadata metadata) {
		String localCode = metadata.getLocalCode();
		if (localCode.startsWith("USR") || localCode.startsWith("MAP")) {
			throw new RuntimeException("Invalid local code for global metadata : " + localCode);
		}
		allLegacyGlobalMetadatas.add(metadata);
		return metadata;
	}

	public static boolean isLegacyGlobalMetadata(String metadata) {
		String metadataLocalCode = new SchemaUtils().toLocalMetadataCode(metadata);
		for (Metadata globalMetadata : allLegacyGlobalMetadatas) {

			if (globalMetadata.getLocalCode().equals(metadataLocalCode)) {
				return true;
			}
		}
		return false;
	}
}
