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
package com.constellio.app.ui.pages.management.schemaRecords;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class SchemaRecordsPresentersServices {

	AppLayerFactory appLayerFactory;
	MetadataSchemasManager schemasManager;
	RecordServices recordServices;
	TaxonomiesManager taxonomiesManager;

	public SchemaRecordsPresentersServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.schemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.taxonomiesManager = appLayerFactory.getModelLayerFactory().getTaxonomiesManager();
	}

	public boolean canManageSchemaType(String schemaTypeCode, final User user) {

		MetadataSchemaTypes types = schemasManager.getSchemaTypes(user.getCollection());
		final MetadataSchemaType metadataSchemaType = types.getSchemaType(schemaTypeCode);
		//
		//		Taxonomy taxonomyOfType = taxonomiesManager.getTaxonomyFor(user.getCollection(), schemaTypeCode);
		//		if (metadataSchemaType.hasSecurity() || taxonomyOfType != null) {
		//			return false;
		//		}

		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollectionOf(user);
		boolean defaultValue = schemaTypeCode.startsWith("ddv") && user.has(CorePermissions.MANAGE_VALUELIST).globally();
		return extensions.canManageSchema(defaultValue, user, metadataSchemaType);
	}

	public boolean canViewSchemaTypeRecord(final Record restrictedRecord, final User user) {

		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(restrictedRecord.getSchemaCode());
		if (!canManageSchemaType(schemaTypeCode, user)) {
			return false;
		}

		MetadataSchemaTypes types = schemasManager.getSchemaTypes(user.getCollection());
		final MetadataSchemaType metadataSchemaType = types.getSchemaType(schemaTypeCode);

		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollectionOf(user);
		boolean defaultValue = user.has(CorePermissions.MANAGE_VALUELIST).globally();
		return extensions.canViewSchemaRecord(defaultValue, user, metadataSchemaType, restrictedRecord);
	}

}
