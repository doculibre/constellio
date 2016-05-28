package com.constellio.app.services.schemasDisplay;

import org.jdom2.Document;

import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class SchemasDisplayReader2 extends SchemasDisplayReader1 {

	public static final String FORMAT_VERSION = "2";

	public SchemasDisplayReader2(Document document, MetadataSchemaTypes types) {
		super(document, types);
	}
}