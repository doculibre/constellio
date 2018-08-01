package com.constellio.app.services.schemasDisplay;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.jdom2.Document;

import java.util.List;

public class SchemasDisplayReader2 extends SchemasDisplayReader1 {

	public static final String FORMAT_VERSION = "2";

	public SchemasDisplayReader2(Document document, MetadataSchemaTypes types, List<Language> languages) {
		super(document, types, languages);
	}
}