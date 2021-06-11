package com.constellio.app.modules.rm.services.menu.behaviors.ui;

import com.constellio.app.entities.batchProcess.ChangeValueOfMetadataBatchAsyncTask;
import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.utils.CartUtil;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenter;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenterService;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessResults;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

public class CartBatchProcessingPresenter implements BatchProcessingPresenter {

	private BatchProcessingPresenterService batchProcessingPresenterService;
	private RMSchemasRecordsServices rm;
	private CartUtil cartUtil;
	private String cartId;
	private User user;
	private RecordServices recordServices;
	private AppLayerFactory appLayerFactory;
	private BaseView view;
	private ModelLayerCollectionExtensions modelLayerCollectionExtensions;
	private MetadataSchemasManager metadataSchemasManager;
	private SchemasDisplayManager schemasDisplayManager;
	private SearchServices searchServices;


	public CartBatchProcessingPresenter(AppLayerFactory appLayerFactory,
										User user, String cartId, BaseView view) {
		this.batchProcessingPresenterService = new BatchProcessingPresenterService(view.getCollection(), appLayerFactory, view.getSessionContext().getCurrentLocale());
		this.rm = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
		this.cartUtil = new CartUtil(view.getCollection(), appLayerFactory);
		this.user = user;
		this.cartId = cartId;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.appLayerFactory = appLayerFactory;
		this.view = view;
		this.modelLayerCollectionExtensions = appLayerFactory.getModelLayerFactory().getExtensions().forCollection(view.getCollection());
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		this.schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	public BaseView getView() {
		return view;
	}

	public List<String> getNotDeletedRecordsIds(String schemaType) {
		switch (schemaType) {
			case Folder.SCHEMA_TYPE:
				List<String> folders = cartUtil.getCartFolderIds(cartId);
				return cartUtil.getNonDeletedRecordsIds(rm.getFolders(folders), user);
			case Document.SCHEMA_TYPE:
				List<String> documents = cartUtil.getCartDocumentIds(cartId);
				return cartUtil.getNonDeletedRecordsIds(rm.getDocuments(documents), user);
			case ContainerRecord.SCHEMA_TYPE:
				List<String> containers = cartUtil.getCartContainersIds(cartId);
				return cartUtil.getNonDeletedRecordsIds(rm.getContainerRecords(containers), user);
			default:
				throw new RuntimeException("Unsupported type : " + schemaType);
		}
	}


	@Override
	public String getOriginSchema(String schemaType, String selectedType) {
		return batchProcessingPresenterService.getOriginSchema(schemaType, selectedType, getNotDeletedRecordsIds(schemaType));
	}


	@Override
	public RecordVO newRecordVO(String schema, String schemaType, SessionContext sessionContext) {
		return newRecordVO(getNotDeletedRecordsIds(schemaType), schema, sessionContext);
	}

	public RecordVO newRecordVO(List<String> selectedRecordIds, String schema, SessionContext sessionContext) {
		return batchProcessingPresenterService.newRecordVO(schema, sessionContext, selectedRecordIds);
	}

	@Override
	public InputStream simulateButtonClicked(String selectedType, String schemaType, RecordVO viewObject,
											 List<String> metadatasToEmpty)
			throws RecordServicesException {
		return simulateButtonClicked(selectedType, getNotDeletedRecordsIds(schemaType), viewObject, metadatasToEmpty);
	}

	public InputStream simulateButtonClicked(String selectedType, List<String> records, RecordVO viewObject,
											 List<String> metadatasToEmpty)
			throws RecordServicesException {
		BatchProcessResults results = batchProcessingPresenterService
				.simulate(selectedType, records, viewObject, metadatasToEmpty, user);
		return batchProcessingPresenterService.formatBatchProcessingResults(results);
	}

	@Override
	public boolean processBatchButtonClicked(String selectedType, String schemaType, RecordVO viewObject,
											 List<String> metadatasToEmpty)
			throws RecordServicesException {
		return processBatchButtonClicked(selectedType, getNotDeletedRecordsIds(schemaType), viewObject, metadatasToEmpty);
	}

	public boolean processBatchButtonClicked(String selectedType, List<String> records, RecordVO viewObject,
											 List<String> metadatasToEmpty)
			throws RecordServicesException {
		for (Record record : recordServices.getRecordsById(view.getCollection(), records)) {
			if (modelLayerCollectionExtensions.isModifyBlocked(record, user)) {
				view.showErrorMessage($("CartView.actionBlockedByExtension"));
				return false;
			}
		}

		batchProcessingPresenterService
				.execute(selectedType, records, viewObject, metadatasToEmpty, user);
		view.navigate().to(RMViews.class).cart(cartId);
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
		return newRecordFieldFactory(schemaType, selectedType, getNotDeletedRecordsIds(schemaType));
	}

	public RecordFieldFactory newRecordFieldFactory(String schemaType, String selectedType, List<String> records) {
		return batchProcessingPresenterService.newRecordFieldFactory(schemaType, selectedType, records);
	}

	@Override
	public boolean hasWriteAccessOnAllRecords(String schemaType) {
		return hasWriteAccessOnAllRecords(getNotDeletedRecordsIds(schemaType));
	}

	public boolean hasWriteAccessOnAllRecords(List<String> selectedRecordIds) {
		return batchProcessingPresenterService.hasWriteAccessOnAllRecords(user, selectedRecordIds);
	}

	@Override
	public long getNumberOfRecords(String schemaType) {
		return (long) getNotDeletedRecordsIds(schemaType).size();
	}

	@Override
	public void allSearchResultsButtonClicked() {
		throw new RuntimeException("Should not have been called");
	}

	@Override
	public void selectedSearchResultsButtonClicked() {
		throw new RuntimeException("Should not have been called");
	}

	@Override
	public boolean isSearchResultsSelectionForm() {
		return false;
	}

	public boolean batchEditRequested(String code, Object value, String schemaType) {
		List<String> recordIds = schemaType.equals(Folder.SCHEMA_TYPE) ? cartUtil.getCartFolderIds(cartId) : cartUtil.getCartDocumentIds(cartId);
		for (Record record : recordServices.getRecordsById(view.getCollection(), recordIds)) {
			if (modelLayerCollectionExtensions.isModifyBlocked(record, user)) {
				view.showErrorMessage($("CartView.actionBlockedByExtension"));
				return false;
			}
		}

		Map<String, Object> changes = new HashMap<>();
		changes.put(code, value);

		LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemasIn(view.getCollection()).where(IDENTIFIER)
				.isIn(getNotDeletedRecordsIds(schemaType))).filteredWithUserWrite(user);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		ModifiableSolrParams params = searchServices.addSolrModifiableParams(query);

		AsyncTask asyncTask = new ChangeValueOfMetadataBatchAsyncTask(changes, SolrUtils.toSingleQueryString(params),
				null, searchServices.getResultsCount(query));

		String username = user == null ? null : user.getUsername();
		AsyncTaskCreationRequest asyncTaskRequest = new AsyncTaskCreationRequest(asyncTask, view.getCollection(), "userBatchProcess");
		asyncTaskRequest.setUsername(username);

		BatchProcessesManager manager = appLayerFactory.getModelLayerFactory().getBatchProcessesManager();
		manager.addAsyncTask(asyncTaskRequest);
		return true;
	}

	public List<MetadataVO> getMetadataAllowedInBatchEdit(String schemaType) {
		MetadataToVOBuilder builder = new MetadataToVOBuilder();

		List<MetadataVO> result = new ArrayList<>();
		Language language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
		for (Metadata metadata : types().getSchemaType(schemaType).getAllMetadatas().sortAscTitle(language)) {
			if (isBatchEditable(metadata) && !metadata.isEssential()) {
				result.add(builder.build(metadata, view.getSessionContext()));
			}
		}
		return result;
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
		return metadataSchemasManager.getSchemaTypes(view.getCollection());
	}

	@Override
	public ValidationErrors validateBatchProcessing() {
		// FIXME
		return new ValidationErrors();
	}

	@Override
	public boolean validateUserHaveBatchProcessPermissionOnAllRecords(String schemaType) {
		if (!user.has(CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS).globally()) {
			return false;
		}

		switch (schemaType) {
			case Folder.SCHEMA_TYPE:
				return doesQueryAndQueryWithFilterOnBatchProcessPermHaveSameResult(cartUtil.getCartFoldersLogicalSearchQuery(cartId));
			case ContainerRecord.SCHEMA_TYPE:
				return doesQueryAndQueryWithFilterOnBatchProcessPermHaveSameResult(cartUtil.getCartContainersLogicalSearchQuery(cartId));
			case Document.SCHEMA_TYPE:
				return doesQueryAndQueryWithFilterOnBatchProcessPermHaveSameResult(cartUtil.getCartDocumentsLogicalSearchQuery(cartId));

			default:
				throw new RuntimeException("No labels for type : " + schemaType);
		}
	}

	private boolean doesQueryAndQueryWithFilterOnBatchProcessPermHaveSameResult(LogicalSearchQuery logicalSearchQuery) {
		SPEQueryResponse speQueryResponse = searchServices.query(logicalSearchQuery);

		LogicalSearchQuery logicalSearchQueryWithFilter = logicalSearchQuery
				.filteredWithUserRead(user, CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS);
		long numberFound = searchServices.getResultsCount(logicalSearchQueryWithFilter);

		return speQueryResponse.getNumFound() == numberFound;
	}

	@Override
	public boolean validateUserHaveBatchProcessPermissionForRecordCount(String schemaType) {
		if (!user.has(CorePermissions.MODIFY_UNLIMITED_RECORDS_USING_BATCH_PROCESS).globally()) {
			ConstellioEIMConfigs systemConfigs = appLayerFactory.getModelLayerFactory().getSystemConfigs();
			int batchProcessingLimit = systemConfigs.getBatchProcessingLimit();
			if (batchProcessingLimit != -1 && getNumberOfRecords(schemaType) > batchProcessingLimit) {
				return false;
			}
		}

		return true;
	}
}
