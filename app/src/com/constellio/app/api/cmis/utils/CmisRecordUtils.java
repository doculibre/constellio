/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.api.cmis.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_TargetIsNotInPrincipalTaxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;

public class CmisRecordUtils {

	private ModelLayerFactory modelLayerFactory;

	public CmisRecordUtils(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public void setParentOfRecord(Record record, Record targetRecord, MetadataSchema schema) {
		List<Metadata> parentReferencesMetadatas = schema.getParentReferences();
		List<Metadata> referencesMetadatas = schema.getTaxonomyRelationshipReferences(Arrays.asList(modelLayerFactory
				.getTaxonomiesManager().getPrincipalTaxonomy(record.getCollection())));
		MetadataSchema targetSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(record.getCollection())
				.getSchema(targetRecord.getSchemaCode());

		String principalPathTargetRecord = targetRecord.get(Schemas.PRINCIPAL_PATH);
		if (principalPathTargetRecord == null) {
			throw new CmisExceptions_TargetIsNotInPrincipalTaxonomy(targetRecord.getId());
		}

		List<Metadata> allReferencesMetadatas = new ArrayList<>();
		allReferencesMetadatas.addAll(parentReferencesMetadatas);
		allReferencesMetadatas.addAll(referencesMetadatas);

		for (Metadata referenceMetadata : allReferencesMetadatas) {
			if (referenceMetadata.getAllowedReferences().isAllowed(targetSchema)) {
				record.set(referenceMetadata, targetRecord);
			} else {
				record.set(referenceMetadata, null);
			}
		}
	}
}
