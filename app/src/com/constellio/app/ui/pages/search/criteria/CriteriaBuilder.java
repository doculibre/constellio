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
		String enumClassName = null;
		if (metadata.getEnumClass() != null) {
			enumClassName = metadata.getEnumClass().getName();
		}
		criterion.setMetadata(metadata.getCode(), metadata.getType(), enumClassName);
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
