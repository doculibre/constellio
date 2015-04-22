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
package com.constellio.app.ui.framework.builders;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.model.entities.Taxonomy;

@SuppressWarnings("serial")
public class TaxonomyToVOBuilder implements Serializable {

	public TaxonomyVO build(Taxonomy taxonomy) {
		String code = taxonomy.getCode();
		String collection = taxonomy.getCollection();
		String title = taxonomy.getTitle();
		List<String> schemaTypes = taxonomy.getSchemaTypes();
		List<String> userIds = taxonomy.getUserIds();
		List<String> groupIds = taxonomy.getGroupIds();
		boolean visibleInHomePage = taxonomy.isVisibleInHomePage();

		return new TaxonomyVO(code, title, schemaTypes, collection, userIds, groupIds, visibleInHomePage);
	}
}
