package com.constellio.app.ui.framework.data.trees;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.taxonomies.FastContinueInfos;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomiesSearchFilter;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.users.UserServices;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.model.services.search.query.ReturnedMetadatasFilter.idVersionSchemaTitlePath;

public class LinkableRecordTreeNodesDataProvider implements RecordTreeNodesDataProvider {

	String taxonomyCode;
	String schemaTypeCode;
	boolean writeAccess;
	TaxonomiesSearchFilter filter;
	boolean ignoreLinkability;
	boolean isShowAllIfHasAccessToManageSecurity = true;

	public LinkableRecordTreeNodesDataProvider(String taxonomyCode, String schemaTypeCode, boolean writeAccess) {
		this(taxonomyCode, schemaTypeCode, writeAccess, null);
	}

	public LinkableRecordTreeNodesDataProvider(String taxonomyCode, String schemaTypeCode, boolean writeAccess,
											   TaxonomiesSearchFilter filter) {
		this.taxonomyCode = taxonomyCode;
		this.schemaTypeCode = schemaTypeCode;
		this.writeAccess = writeAccess;
		this.filter = filter;
	}

	public void setIgnoreLinkability(boolean ignoreLinkability) {
		this.ignoreLinkability = ignoreLinkability;
	}

	@Override
	public LinkableTaxonomySearchResponse getChildrenNodes(String parentId, int start, int maxSize,
														   FastContinueInfos infos) {
		ModelLayerFactory modelLayerFactory = getInstance().getModelLayerFactory();
		User currentUser = getCurrentUser(modelLayerFactory);
		Record record = getRecord(modelLayerFactory, parentId);

		TaxonomiesSearchOptions taxonomiesSearchOptions = newTaxonomiesSearchOptions(maxSize, start, infos);
		return modelLayerFactory.newTaxonomiesSearchService().getLinkableChildConceptResponse(
				currentUser, record, taxonomyCode, schemaTypeCode, taxonomiesSearchOptions);
	}

	@Override
	public LinkableTaxonomySearchResponse getRootNodes(int start, int maxSize, FastContinueInfos infos) {
		ModelLayerFactory modelLayerFactory = getInstance().getModelLayerFactory();
		User currentUser = getCurrentUser(modelLayerFactory);

		TaxonomiesSearchOptions taxonomiesSearchOptions = newTaxonomiesSearchOptions(maxSize, start, infos);
		return modelLayerFactory.newTaxonomiesSearchService().getLinkableRootConceptResponse(
				currentUser, currentUser.getCollection(), taxonomyCode, schemaTypeCode, taxonomiesSearchOptions);
	}

	@Override
	public String getTaxonomyCode() {
		return taxonomyCode;
	}

	protected TaxonomiesSearchOptions newTaxonomiesSearchOptions(int rows, int startRow, FastContinueInfos infos) {
		ModelLayerFactory modelLayerFactory = getInstance().getModelLayerFactory();
		Boolean showOnlyTriangleIfContent = modelLayerFactory.getSystemConfigurationsManager().<Boolean>getValue(ConstellioEIMConfigs.SHOW_TRIANGLE_ONLY_WHEN_FOLDER_HAS_CONTENT);
		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions(rows, startRow, StatusFilter.ACTIVES)
				.setFastContinueInfos(infos)
				.setReturnedMetadatasFilter(idVersionSchemaTitlePath().withIncludedMetadata(Schemas.CODE)
						.withIncludedMetadata(Schemas.DESCRIPTION_TEXT).withIncludedMetadata(Schemas.DESCRIPTION_STRING));

		if (writeAccess) {
			options.setRequiredAccess(Role.WRITE);
		}

		if (showOnlyTriangleIfContent) {
			options.setHasChildrenFlagCalculated(TaxonomiesSearchOptions.HasChildrenFlagCalculated.ALWAYS);
		} else {
			options.setHasChildrenFlagCalculated(TaxonomiesSearchOptions.HasChildrenFlagCalculated.CONCEPTS_ONLY);
		}

		options.setAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable(ignoreLinkability);

		options.setFilter(filter);

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

		return recordServices.getDocumentById(id);
	}

	public LinkableRecordTreeNodesDataProvider setShowAllIfHasAccessToManageSecurity(
			boolean showAllIfHasAccessToManageSecurity) {
		isShowAllIfHasAccessToManageSecurity = showAllIfHasAccessToManageSecurity;
		return this;
	}

	public TaxonomiesSearchFilter getFilter() {
		return filter;
	}
}
