package com.constellio.app.api.cmis.utils;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaUtils;

public class CmisRecordUtils {

	private ModelLayerFactory modelLayerFactory;

	public CmisRecordUtils(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public void setParentOfRecord(Record record, Record newParentRecord, MetadataSchema schema) {
		List<Metadata> parentReferencesMetadatas = schema.getParentReferences();
		List<Metadata> referencesMetadatas = new ArrayList<>();
		MetadataSchema targetSchema = null;

		if (newParentRecord != null) {
			String schemaType = new SchemaUtils().getSchemaTypeCode(newParentRecord.getSchemaCode());
			Taxonomy taxonomy = modelLayerFactory.getTaxonomiesManager().getTaxonomyFor(record.getCollection(), schemaType);

			if (taxonomy != null) {
				referencesMetadatas = schema.getTaxonomyRelationshipReferences(asList(taxonomy));
			}

		}
		if (newParentRecord != null) {
			targetSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(record.getCollection())
					.getSchema(newParentRecord.getSchemaCode());
		}

		List<Metadata> allReferencesMetadatas = new ArrayList<>();
		allReferencesMetadatas.addAll(parentReferencesMetadatas);
		allReferencesMetadatas.addAll(referencesMetadatas);

		for (Metadata referenceMetadata : allReferencesMetadatas) {
			if (targetSchema != null && referenceMetadata.getAllowedReferences().isAllowed(targetSchema)) {
				record.set(referenceMetadata, newParentRecord);
			}
		}
	}

	public static GregorianCalendar toGregorianCalendar(Object value) {
		if (value != null && value instanceof LocalDateTime) {
			return ((LocalDateTime) value).toDateTime().toGregorianCalendar();

		} else if (value != null && value instanceof LocalDate) {
			return ((LocalDate) value).toDateTimeAtStartOfDay().toGregorianCalendar();

		} else {
			return null;
		}
	}
}
