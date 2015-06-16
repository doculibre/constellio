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
package com.constellio.app.modules.rm.extensions;

import static com.constellio.app.modules.rm.constants.RMTypes.FILING_SPACE;
import static com.constellio.app.modules.rm.constants.RMTypes.RETENTION_RULE;
import static com.constellio.app.modules.rm.constants.RMTypes.UNIFORM_SUBDIVISION;
import static com.constellio.data.frameworks.extensions.ExtensionBooleanResult.FORCE_TRUE;
import static com.constellio.data.frameworks.extensions.ExtensionBooleanResult.NOT_APPLICABLE;
import static java.util.Arrays.asList;

import com.constellio.app.api.extensions.SchemaTypeAccessExtension;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public class RMSchemaTypeAccessExtension extends SchemaTypeAccessExtension {

	@Override
	public ExtensionBooleanResult canManageSchema(User user, MetadataSchemaType schemaType) {
		if (schemaType.getCode().startsWith(FilingSpace.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.forceTrueIf(user.has(CorePermissions.MANAGE_SECURITY).globally());

		} else if (schemaType.getCode().startsWith(UniformSubdivision.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.forceTrueIf(user.has(RMPermissionsTo.MANAGE_UNIFORMSUBDIVISIONS).globally());

			//		} else if (schemaType.getCode().startsWith(RetentionRule.SCHEMA_TYPE)) {
			//			return ExtensionBooleanResult.forceTrueIf(user.has(RMPermissionsTo.MANAGE_RETENTIONRULE).globally());

		} else {
			return NOT_APPLICABLE;
		}
	}

	@Override
	public ExtensionBooleanResult isSchemaTypeConfigurable(MetadataSchemaType schemaType) {
		return asList(RETENTION_RULE, FILING_SPACE, UNIFORM_SUBDIVISION).contains(schemaType.getCode()) ?
				FORCE_TRUE : NOT_APPLICABLE;
	}
}
