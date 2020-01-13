package com.constellio.app.ui.framework.data.trees;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.taxonomies.FastContinueInfos;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.users.UserServices;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.model.services.search.query.ReturnedMetadatasFilter.idVersionSchemaTitlePath;

public class VisibleRecordTreeNodesDataProvider implements RecordTreeNodesDataProvider {

	String taxonomyCode;

	public VisibleRecordTreeNodesDataProvider(String taxonomyCode) {
		this.taxonomyCode = taxonomyCode;
	}

	@Override
	public LinkableTaxonomySearchResponse getChildrenNodes(String parentId, int start, int maxSize,
														   FastContinueInfos infos) {
		ModelLayerFactory modelLayerFactory = getInstance().getModelLayerFactory();
		User currentUser = getCurrentUser(modelLayerFactory);
		Record record = getRecord(modelLayerFactory, parentId);

		TaxonomiesSearchOptions taxonomiesSearchOptions = newTaxonomiesSearchOptions(maxSize, start, infos);
		return modelLayerFactory.newTaxonomiesSearchService()
				.getVisibleChildConceptResponse(currentUser, taxonomyCode, record, taxonomiesSearchOptions);
	}

	@Override
	public LinkableTaxonomySearchResponse getRootNodes(int start, int maxSize, FastContinueInfos infos) {
		ModelLayerFactory modelLayerFactory = getInstance().getModelLayerFactory();
		User currentUser = getCurrentUser(modelLayerFactory);

		TaxonomiesSearchOptions taxonomiesSearchOptions = newTaxonomiesSearchOptions(maxSize, start, infos);
		return modelLayerFactory.newTaxonomiesSearchService().getVisibleRootConceptResponse(
				currentUser, currentUser.getCollection(), taxonomyCode, taxonomiesSearchOptions, null);
	}

	@Override
	public String getTaxonomyCode() {
		return taxonomyCode;
	}

	private TaxonomiesSearchOptions newTaxonomiesSearchOptions(int rows, int startRow, FastContinueInfos infos) {
		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions(rows, startRow, StatusFilter.ACTIVES)
				.setFastContinueInfos(infos)
				.setReturnedMetadatasFilter(idVersionSchemaTitlePath().withIncludedMetadata(Schemas.CODE)
						.withIncludedMetadata(Schemas.DESCRIPTION_TEXT).withIncludedMetadata(Schemas.DESCRIPTION_STRING));
		ModelLayerFactory modelLayerFactory = getInstance().getModelLayerFactory();
		Boolean showOnlyTriangleIfContent = modelLayerFactory.getSystemConfigurationsManager().<Boolean>getValue(ConstellioEIMConfigs.SHOW_TRIANGLE_ONLY_WHEN_FOLDER_HAS_CONTENT);
		if (showOnlyTriangleIfContent) {
			options.setHasChildrenFlagCalculated(TaxonomiesSearchOptions.HasChildrenFlagCalculated.ALWAYS);
		} else {
			options.setHasChildrenFlagCalculated(TaxonomiesSearchOptions.HasChildrenFlagCalculated.CONCEPTS_ONLY);
		}
		return options;
	}

	private User getCurrentUser(ModelLayerFactory modelLayerFactory) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		String currentCollection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();
		UserServices userServices = modelLayerFactory.newUserServices();

		return userServices.getUserInCollection(currentUserVO.getUsername(), currentCollection);
	}

	private Record getRecord(ModelLayerFactory modelLayerFactory, String id) {
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		return recordServices.realtimeGetRecordSummaryById(id);
	}
}
