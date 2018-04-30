package com.constellio.app.ui.framework.builders;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;

@SuppressWarnings("serial")
public class TaxonomyToVOBuilder implements Serializable {

	public TaxonomyVO build(Taxonomy taxonomy) {
		String code = taxonomy.getCode();
		String collection = taxonomy.getCollection();
		List<String> schemaTypes = taxonomy.getSchemaTypes();
		List<String> userIds = taxonomy.getUserIds();
		List<String> groupIds = taxonomy.getGroupIds();
		boolean visibleInHomePage = taxonomy.isVisibleInHomePage();

		return new TaxonomyVO(code, taxonomy.getTitle(), schemaTypes, collection, userIds, groupIds, visibleInHomePage);
	}
}
