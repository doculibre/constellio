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

import com.constellio.app.api.extensions.TaxonomyAccessExtension;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;

public class RMTaxonomyTypeAccessExtension extends TaxonomyAccessExtension {
	@Override
	public ExtensionBooleanResult canManageTaxonomy(User user, Taxonomy taxonomy) {
		if (taxonomy.getCode().equals(RMTaxonomies.ADMINISTRATIVE_UNITS)) {
			return ExtensionBooleanResult.forceTrueIf(user.has(CorePermissions.MANAGE_SECURITY).globally());

		} else if (taxonomy.getCode().equals(RMTaxonomies.CLASSIFICATION_PLAN)) {
			return ExtensionBooleanResult.forceTrueIf(user.has(RMPermissionsTo.MANAGE_CLASSIFICATION_PLAN).globally());

		} else if (taxonomy.getCode().equals(RMTaxonomies.STORAGES)) {
			return ExtensionBooleanResult.forceTrueIf(user.has(RMPermissionsTo.MANAGE_STORAGE_SPACES).globally());

		} else {
			return ExtensionBooleanResult.NOT_APPLICABLE;
		}
	}
}
