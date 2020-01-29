package com.constellio.app.services.menu.behavior.ui;

import com.constellio.app.api.extensions.BatchProcessingExtension;
import com.constellio.app.api.extensions.BatchProcessingExtension.BatchProcessFeededByIdsParams;
import com.constellio.app.api.extensions.BatchProcessingExtension.BatchProcessFeededByQueryParams;
import com.constellio.app.entities.batchProcess.ChangeValueOfMetadataBatchAsyncTask;
import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.AdvancedSearchPresenterExtension;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.AdvancedSearchViewImpl;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenter;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenterService;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessResults;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancedViewBatchProcessingPresenter implements BatchProcessingPresenter {

	private ModelLayerFactory modelLayerFactory;
	private BatchProcessingPresenterService batchProcessingPresenterService;
	private LogicalSearchQuery searchQuery;
	private String searchID;
	private SearchServices searchServices;
	private boolean batchProcessOnAllSearchResults;
	private User user;
	private String collection;
	private AdvancedSearchViewImpl view;
	private VaultBehaviorsList<BatchProcessingExtension> batchProcessingExtensions;
	private RMModuleExtensions rmModuleExtensions;
	private String schemaTypeCode;
	private SchemasDisplayManager schemasDisplayManager;
	private String searchExpression;

	public AdvancedViewBatchProcessingPresenter(AppLayerFactory appLayerFactory, AdvancedSearchViewImpl view, User user,
												LogicalSearchQuery searchQuery) {
		this.view = view;
		this.user = user;
		this.searchQuery = searchQuery;

		collection = view.getCollection();
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		searchServices = modelLayerFactory.newSearchServices();
		schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();

		schemaTypeCode = view.getSchemaType();
		searchExpression = view.getSearchExpression();
		searchID = view.getSavedSearchId();

		batchProcessingPresenterService = new BatchProcessingPresenterService(collection, appLayerFactory,
				view.getSessionContext().getCurrentLocale());

		batchProcessingExtensions = appLayerFactory.getExtensions().forCollection(collection).batchProcessingExtensions;
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
	}

	@Override
	public String getOriginSchema(String schemaType, String selectedType) {
		return batchProcessingPresenterService.getOriginSchema(schemaType, selectedType, buildBatchProcessLogicalSearchQuery());
	}

	@Override
	public RecordVO newRecordVO(String schema, String schemaType, SessionContext sessionContext) {
		return batchProcessingPresenterService.newRecordVO(schema, sessionContext, buildBatchProcessLogicalSearchQuery());
	}

	@Override
	public InputStream simulateButtonClicked(String selectedType, String schemaType, RecordVO viewObject,
											 List<String> metadatasToEmpty)
			throws RecordServicesException {
		BatchProcessResults results = batchProcessingPresenterService
				.simulate(selectedType, buildBatchProcessLogicalSearchQuery().setNumberOfRows(100), viewObject, metadatasToEmpty, user);
		return batchProcessingPresenterService.formatBatchProcessingResults(results);
	}

	@Override
	public boolean processBatchButtonClicked(String selectedType, String schemaType, RecordVO viewObject,
											 List<String> metadatasToEmpty)
			throws RecordServicesException {
		batchProcessingPresenterService
				.execute(selectedType, buildBatchProcessLogicalSearchQuery(), viewObject, metadatasToEmpty, user);
		if (searchID != null) {
			view.navigate().to().advancedSearchReplay(searchID);
		} else {
			view.navigate().to().advancedSearch();
		}
		return true;
	}

	@Override
	public AppLayerCollectionExtensions getBatchProcessingExtension() {
		return batchProcessingPresenterService.getBatchProcessingExtension();
	}

	@Override
	public String getTypeSchemaType(String schemaType) {
		return batchProcessingPresenterService.getTypeSchemaType(schemaType);
	}

	@Override
	public RecordFieldFactory newRecordFieldFactory(String schemaType, String selectedType) {
		return batchProcessingPresenterService
				.newRecordFieldFactory(schemaType, selectedType, buildBatchProcessLogicalSearchQuery());
	}

	@Override
	public boolean hasWriteAccessOnAllRecords(String schemaType) {
		LogicalSearchQuery query = buildBatchProcessLogicalSearchQuery();
		return searchServices.getResultsCount(query.filteredWithUserWrite(user)) == searchServices
				.getResultsCount(query);
	}

	@Override
	public long getNumberOfRecords(String schemaType) {
		return (int) searchServices.getResultsCount(buildBatchProcessLogicalSearchQuery());
	}

	@Override
	public void allSearchResultsButtonClicked() {
		batchProcessOnAllSearchResults = true;
	}

	@Override
	public void selectedSearchResultsButtonClicked() {
		batchProcessOnAllSearchResults = false;
	}

	@Override
	public boolean isSearchResultsSelectionForm() {
		return true;
	}

	@Override
	public boolean batchEditRequested(String code, Object convertedValue, String schemaType) {
		Map<String, Object> changes = new HashMap<>();
		changes.put(code, convertedValue);

		LogicalSearchQuery query = buildBatchProcessLogicalSearchQuery();
		ModifiableSolrParams params = searchServices.addSolrModifiableParams(query);

		AsyncTask asyncTask = new ChangeValueOfMetadataBatchAsyncTask(changes, SolrUtils.toSingleQueryString(params),
				null, searchServices.getResultsCount(query));

		String username = user == null ? null : user.getUsername();
		AsyncTaskCreationRequest asyncTaskRequest = new AsyncTaskCreationRequest(asyncTask, collection, "userBatchProcess");
		asyncTaskRequest.setUsername(username);

		BatchProcessesManager manager = modelLayerFactory.getBatchProcessesManager();
		manager.addAsyncTask(asyncTaskRequest);
		return true;
	}

	@Override
	public List<MetadataVO> getMetadataAllowedInBatchEdit(String schemaType) {
		MetadataToVOBuilder builder = new MetadataToVOBuilder();

		List<MetadataVO> result = new ArrayList<>();
		Language language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
		for (Metadata metadata : types().getSchemaType(schemaTypeCode).getAllMetadatas().sortAscTitle(language)) {
			if (isBatchEditable(metadata)) {
				result.add(builder.build(metadata, view.getSessionContext()));
			}
		}
		return result;
	}

	@Override
	public ValidationErrors validateBatchProcessing() {
		ValidationErrors errors = new ValidationErrors();

		for (BatchProcessingExtension extension : batchProcessingExtensions) {
			if (batchProcessOnAllSearchResults) {
				extension.validateBatchProcess(
						new BatchProcessFeededByQueryParams(errors, buildBatchProcessLogicalSearchQuery(), schemaTypeCode));
			} else {
				extension.validateBatchProcess(
						new BatchProcessFeededByIdsParams(errors, view.getSelectedRecordIds(), schemaTypeCode));
			}
		}
		return errors;
	}

	@Override
	public boolean validateUserHaveBatchProcessPermissionOnAllRecords(String schemaType) {
		LogicalSearchQuery logicalSearchQuery = buildBatchProcessLogicalSearchQuery();
		long numFound = searchServices.query(logicalSearchQuery).getNumFound();
		logicalSearchQuery = logicalSearchQuery.filteredWithUser(user, CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS);
		SPEQueryResponse speQueryResponse = searchServices.query(logicalSearchQuery);
		long numFoundWithFilter = speQueryResponse.getNumFound();

		return numFoundWithFilter == numFound;
	}

	public BaseView getView() {
		return view;
	}

	private LogicalSearchQuery buildBatchProcessLogicalSearchQuery() {
		LogicalSearchQuery query = buildLogicalSearchQuery();
		for (AdvancedSearchPresenterExtension extension : rmModuleExtensions.getAdvancedSearchPresenterExtensions()) {
			query = extension.addAdditionalSearchQueryFilters(
					new AdvancedSearchPresenterExtension.AddAdditionalSearchQueryFiltersParams(query, schemaTypeCode));
		}
		return query;
	}

	private LogicalSearchQuery buildLogicalSearchQuery() {
		List<String> selectedRecordIds = view.getSelectedRecordIds();
		LogicalSearchQuery query = null;
		if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode) || StorageSpace.SCHEMA_TYPE.equals(schemaTypeCode)) {
			if (!batchProcessOnAllSearchResults) {
				query = buildUnsecuredLogicalSearchQueryWithSelectedIds();
			} else if (selectedRecordIds != null && !selectedRecordIds.isEmpty()) {
				query = buildUnsecuredLogicalSearchQueryWithUnselectedIds();
			} else {
				query = buildUnsecuredLogicalSearchQueryWithAllRecords();
			}
		} else {
			if (!batchProcessOnAllSearchResults) {
				query = buildLogicalSearchQueryWithSelectedIds();
			} else if (selectedRecordIds != null && !selectedRecordIds.isEmpty()) {
				query = buildLogicalSearchQueryWithUnselectedIds();
			} else {
				query = buildLogicalSearchQueryWithAllRecords();
			}
		}

		if (searchExpression != null && !searchExpression.isEmpty()) {
			query.setFreeTextQuery(searchExpression);
		}

		return query;
	}

	private LogicalSearchQuery buildLogicalSearchQueryWithSelectedIds() {
		LogicalSearchQuery query = getSearchQuery();
		query.setCondition(query.getCondition().andWhere(Schemas.IDENTIFIER).isIn(view.getSelectedRecordIds())
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.filteredWithUser(user).filteredWithUserWrite(user)
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	private LogicalSearchQuery buildLogicalSearchQueryWithUnselectedIds() {
		LogicalSearchQuery query = getSearchQuery();
		query.setCondition(query.getCondition().andWhere(Schemas.IDENTIFIER).isNotIn(view.getUnselectedRecordIds())
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.filteredWithUser(user).filteredWithUserWrite(user)
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	private LogicalSearchQuery buildLogicalSearchQueryWithAllRecords() {
		LogicalSearchQuery query = getSearchQuery();
		query.setCondition(query.getCondition().andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.filteredWithUser(user).filteredWithUserWrite(user)
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	private LogicalSearchQuery buildUnsecuredLogicalSearchQueryWithSelectedIds() {
		LogicalSearchQuery query = getSearchQuery();
		query.setCondition(query.getCondition().andWhere(Schemas.IDENTIFIER).isIn(view.getSelectedRecordIds())
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	private LogicalSearchQuery buildUnsecuredLogicalSearchQueryWithUnselectedIds() {
		LogicalSearchQuery query = getSearchQuery();
		query.setCondition(query.getCondition().andWhere(Schemas.IDENTIFIER).isNotIn(view.getUnselectedRecordIds())
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	private LogicalSearchQuery buildUnsecuredLogicalSearchQueryWithAllRecords() {
		LogicalSearchQuery query = getSearchQuery();
		query.setCondition(query.getCondition().andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	private boolean isPreferAnalyzedFields() {
		return false;
	}

	private LogicalSearchQuery getSearchQuery() {
		return new LogicalSearchQuery(searchQuery);
	}

	private boolean isBatchEditable(Metadata metadata) {
		return !metadata.isSystemReserved()
			   && !metadata.isUnmodifiable()
			   && metadata.isEnabled()
			   && !metadata.getType().isStructureOrContent()
			   && metadata.getDataEntry().getType() == DataEntryType.MANUAL
			   && isNotHidden(metadata)
			   // XXX: Not supported in the backend
			   && metadata.getType() != MetadataValueType.ENUM;
	}

	private boolean isNotHidden(Metadata metadata) {
		MetadataDisplayConfig config = schemasDisplayManager.getMetadata(view.getCollection(), metadata.getCode());
		return config.getInputType() != MetadataInputType.HIDDEN;
	}

	private MetadataSchemaTypes types() {
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		return metadataSchemasManager.getSchemaTypes(collection);
	}

	private Metadata getMetadata(String code) {
		if (code.startsWith("global_")) {
			return Schemas.getGlobalMetadata(code);
		}
		SchemaUtils utils = new SchemaUtils();
		String schemaCode = utils.getSchemaCode(code);
		return schema(schemaCode).getMetadata(utils.getLocalCode(code, schemaCode));
	}

	private MetadataSchema schema(String code) {
		return types().getSchema(code);
	}
}
