package com.constellio.app.extensions.api;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;

public class SchemaMetadataExtension {

	public void metadataSavedFromView(SchemaMetadataExtensionParams params) {
	}

	public void metadataDeletedFromView(SchemaMetadataExtensionParams params) {
	}

	public static class SchemaMetadataExtensionParams {

		private Metadata metadata;
		private User user;

		public SchemaMetadataExtensionParams(Metadata metadata, User user) {
			this.metadata = metadata;
			this.user = user;
		}

		public Metadata getMetadata() {
			return metadata;
		}

		public User getUser() {
			return user;
		}
	}

}
