package com.constellio.app.modules.rm;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.configs.AbstractSystemConfigurationScript;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMFolderCopyStatusPriorityScript extends AbstractSystemConfigurationScript<Boolean> {

	private static final String CONTAINS_SUBFOLDER_WITH_COPY_STATUS_ENTERED = "containsSubfolderWithCopyStatusEntered";

	@Override
	public void validate(Boolean newValue, ValidationErrors errors) {
		if (Boolean.TRUE.equals(newValue)) {
			AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
			SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();

			appLayerFactory.getCollectionsManager().getCollectionCodesExcludingSystem()
					.forEach(code -> {
						RMSchemasRecordsServices rm = new RMSchemasRecordsServices(code, appLayerFactory);

						LogicalSearchQuery query = new LogicalSearchQuery(from(rm.folderSchemaType())
								.where(rm.folder.parentFolder()).isNotNull()
								.andWhere(rm.folder.copyStatusEntered()).isNotNull());
						if (searchServices.hasResults(query)) {
							errors.add(getClass(), CONTAINS_SUBFOLDER_WITH_COPY_STATUS_ENTERED);
						}
					});
		}
	}
}
