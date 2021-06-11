package com.constellio.app.modules.restapi.apis.v1.category.dao;

import com.constellio.app.modules.restapi.apis.v1.category.dto.CategoryDto;
import com.constellio.app.modules.restapi.apis.v1.core.BaseDao;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.ui.framework.data.AutocompleteQuery;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CategoryDao extends BaseDao {

	private static final Logger LOGGER = Logger.getLogger(CategoryDao.class);

	public List<CategoryDto> search(User user, String expression) throws Exception {
		SystemConfigurationsManager configsManager = modelLayerFactory.getSystemConfigurationsManager();

		AutocompleteQuery query = AutocompleteQuery.builder()
				.appLayerFactory(appLayerFactory)
				.schemaTypeCode(Category.SCHEMA_TYPE)
				.hasSecurity(true)
				.expression(expression)
				.user(user).writeAccessRequired(true)
				.startRow(0).rowCount(configsManager.getValue(ConstellioEIMConfigs.AUTOCOMPLETE_SIZE))
				.metadataFilter(ReturnedMetadatasFilter.onlyMetadatas(Schemas.IDENTIFIER, Schemas.TITLE))
				.build();

		List<Record> results = searchServices.search(query.searchQuery());
		return categoryToDto(results);
	}

	private List<CategoryDto> categoryToDto(List<Record> categories) {
		List<CategoryDto> results = new ArrayList<>();
		for (Record category : categories) {
			results.add(CategoryDto.builder().id(category.getId()).title(category.getTitle()).build());
		}
		return results;
	}
}