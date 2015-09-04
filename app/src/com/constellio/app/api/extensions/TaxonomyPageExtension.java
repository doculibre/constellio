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
package com.constellio.app.api.extensions;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.constellio.app.api.extensions.taxonomies.GetTaxonomyExtraFieldsParam;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyManagementClassifiedTypesParams;
import com.constellio.app.api.extensions.taxonomies.TaxonomyExtraField;
import com.constellio.app.api.extensions.taxonomies.TaxonomyManagementClassifiedType;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;

public class TaxonomyPageExtension implements Serializable {

	public ExtensionBooleanResult canManageTaxonomy(User user, Taxonomy taxonomy) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public List<TaxonomyManagementClassifiedType> getClassifiedTypesFor(GetTaxonomyManagementClassifiedTypesParams params) {
		return Collections.emptyList();
	}

	public List<TaxonomyExtraField> getTaxonomyExtraFieldsFor(GetTaxonomyExtraFieldsParam params) {
		return Collections.emptyList();
	}
}
