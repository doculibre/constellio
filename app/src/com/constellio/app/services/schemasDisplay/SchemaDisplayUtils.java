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
package com.constellio.app.services.schemasDisplay;

import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataListFilter;

public class SchemaDisplayUtils {

	public static MetadataList getRequiredMetadatasInSchemaForm(MetadataSchema schema) {
		return getAvailableMetadatasInSchemaForm(schema).onlyEssentialMetadatasAndCodeTitle();
	}

	public static MetadataList getAvailableMetadatasInSchemaForm(MetadataSchema schema) {

		MetadataListFilter filter = new MetadataListFilter() {
			@Override
			public boolean isReturned(Metadata metadata) {
				return notAComment(metadata) && notIdentifier(metadata);
			}

			private boolean notIdentifier(Metadata metadata) {
				return !"recordIdentifier".equals(metadata.getLocalCode());
			}

			private boolean notAComment(Metadata metadata) {
				return metadata.getStructureFactory() == null
						|| !CommentFactory.class.equals(metadata.getStructureFactory().getClass());
			}
		};

		return schema.getMetadatas().onlyManuals().onlyNonSystemReserved().onlyEnabled().only(filter);
	}

}
