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
package com.constellio.app.ui.pages.search.criteria;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public class CriteriaBuilder {

	List<Criterion> criteria = new ArrayList<>();

	MetadataSchemaType schemaType;

	SessionContext sessionContext;

	public CriteriaBuilder(MetadataSchemaType schemaType, SessionContext sessionContext) {
		this.schemaType = schemaType;
		this.sessionContext = sessionContext;
	}

	public CriterionBuilder addCriterion(MetadataVO metadata) {
		Criterion criterion = new Criterion(schemaType.getCode());
		criteria.add(criterion);
		criterion.setMetadata(metadata);
		return new CriterionBuilder(criterion);
	}

	public CriterionBuilder addCriterion(String metadataCode) {
		if (metadataCode.contains("_")) {
			return addCriterion(schemaType.getMetadata(metadataCode));
		} else {
			return addCriterion(schemaType.getDefaultSchema().getMetadata(metadataCode));
		}

	}

	public CriterionBuilder addCriterion(Metadata metadata) {
		return addCriterion(new MetadataToVOBuilder().build(metadata, sessionContext));
	}

	public List<Criterion> build() {
		return criteria;
	}
}
