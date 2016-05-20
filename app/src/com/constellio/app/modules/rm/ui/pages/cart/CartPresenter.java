package com.constellio.app.modules.rm.ui.pages.cart;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.cart.CartEmlService;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVOWithDistinctSchemasDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenter;
import com.constellio.model.entities.enums.BatchProcessingMode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class CartPresenter extends SingleSchemaBasePresenter<CartView> implements BatchProcessingPresenter {
	private transient RMSchemasRecordsServices rm;
	private transient Cart cart;
	private String cartId;

	public CartPresenter(CartView view) {
		super(view, Cart.DEFAULT_SCHEMA);
	}

	public void itemRemovalRequested(RecordVO record) {
		Cart cart = cart();
		switch (record.getSchema().getTypeCode()) {
		case Folder.SCHEMA_TYPE:
			cart.removeFolder(record.getId());
			break;
		case Document.SCHEMA_TYPE:
			cart.removeDocument(record.getId());
			break;
		case ContainerRecord.SCHEMA_TYPE:
			cart.removeContainer(record.getId());
			break;
		}
		addOrUpdate(cart.getWrappedRecord());
		view.navigate().to(RMViews.class).cart(cart.getId());
	}

	public boolean canEmptyCart() {
		return cartHasRecords();
	}

	public void cartEmptyingRequested() {
		addOrUpdate(cart().empty().getWrappedRecord());
		view.navigate().to(RMViews.class).cart(cart().getId());
	}

	public boolean canPrepareEmail() {
		// TODO: Maybe better test
		return cartHasRecords();
	}

	public void emailPreparationRequested() {
		InputStream stream = new CartEmlService(collection, modelLayerFactory).createEmlForCart(cart());
		view.startDownload(stream);
	}

	public boolean canDuplicate() {
		return cartHasOnlyFolders() && canDuplicateFolders(getCurrentUser());
	}

	public void duplicationRequested() {
		if (!canDuplicate()) {
			view.showErrorMessage($("CartView.cannotDuplicate"));
			return;
		}
		DecommissioningService service = new DecommissioningService(view.getCollection(), modelLayerFactory);
		for (Folder folder : getCartFolders()) {
			service.duplicateStructureAndSave(folder, getCurrentUser());
		}
		view.showMessage($("CartView.duplicated"));
	}

	public boolean canDelete() {
		return cartHasRecords() && cart().getContainers().isEmpty()
				&& canDeleteFolders(getCurrentUser()) && canDeleteDocuments(getCurrentUser());
	}

	public void deletionRequested() {
		if (!canDelete()) {
			view.showErrorMessage($("CartView.cannotDelete"));
			return;
		}
		for (Record record : recordServices().getRecordsById(view.getCollection(), cart().getAllItems())) {
			delete(record, false);
		}
		cartEmptyingRequested();
	}

	public RecordVOWithDistinctSchemasDataProvider getRecords() {
		return new RecordVOWithDistinctSchemasDataProvider(
				getSchemas(), new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(fromAllSchemasIn(collection).where(Schemas.IDENTIFIER).isIn(cart().getAllItems()))
						.filteredWithUser(getCurrentUser()).filteredByStatus(StatusFilter.ACTIVES)
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	private List<MetadataSchemaVO> getSchemas() {
		MetadataSchemaToVOBuilder builder = new MetadataSchemaToVOBuilder();
		return Arrays.asList(
				builder.build(schema(Folder.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext()),
				builder.build(schema(Folder.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext()),
				builder.build(schema(Folder.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext()));
	}

	Cart cart() {
		if (cart == null) {
//			cart = rm().getOrCreateUserCart(getCurrentUser());
			cart = rm().getCart(cartId);
		}
		return cart;
	}

	private boolean cartHasRecords() {
		return !cart().isEmpty();
	}

	private boolean cartHasOnlyFolders() {
		return !cart().getFolders().isEmpty() && cart().getDocuments().isEmpty() && cart().getContainers().isEmpty();
	}

	private boolean canDuplicateFolders(User user) {
		for (Folder folder : getCartFolders()) {
			RecordWrapper parent = folder.getParentFolder() != null ?
					rm().getFolder(folder.getParentFolder()) :
					rm().getAdministrativeUnit(folder.getAdministrativeUnitEntered());
			if (!user.hasWriteAccess().on(parent)) {
				return false;
			}
			switch (folder.getPermissionStatus()) {
			case SEMI_ACTIVE:
				if (!user.has(RMPermissionsTo.DUPLICATE_SEMIACTIVE_FOLDER).on(folder)) {
					return false;
				}
				break;
			case INACTIVE_DEPOSITED:
			case INACTIVE_DESTROYED:
				if (!user.has(RMPermissionsTo.DUPLICATE_INACTIVE_FOLDER).on(folder)) {
					return false;
				}
				break;
			}
		}
		return true;
	}

	private boolean canDeleteFolders(User user) {
		for (Folder folder : getCartFolders()) {
			if (!user.hasDeleteAccess().on(folder)) {
				return false;
			}
			switch (folder.getPermissionStatus()) {
			case SEMI_ACTIVE:
				if (!user.has(RMPermissionsTo.DELETE_SEMIACTIVE_FOLDERS).on(folder)) {
					return false;
				}
				break;
			case INACTIVE_DEPOSITED:
			case INACTIVE_DESTROYED:
				if (!user.has(RMPermissionsTo.DELETE_INACTIVE_FOLDERS).on(folder)) {
					return false;
				}
				break;
			}
		}
		return true;
	}

	private boolean canDeleteDocuments(User user) {
		for (Document document : getCartDocuments()) {
			if (!user.hasDeleteAccess().on(document)) {
				return false;
			}
			switch (document.getArchivisticStatus()) {
			case SEMI_ACTIVE:
				if (!user.has(RMPermissionsTo.DELETE_SEMIACTIVE_DOCUMENT).on(document)) {
					return false;
				}
				break;
			case INACTIVE_DEPOSITED:
			case INACTIVE_DESTROYED:
				if (!user.has(RMPermissionsTo.DELETE_INACTIVE_DOCUMENT).on(document)) {
					return false;
				}
			}
		}
		return true;
	}

	private List<Folder> getCartFolders() {
		return rm().wrapFolders(recordServices().getRecordsById(view.getCollection(), cart().getFolders()));
	}

	private List<Document> getCartDocuments() {
		return rm().wrapDocuments(recordServices().getRecordsById(view.getCollection(), cart().getDocuments()));
	}

	private RMSchemasRecordsServices rm() {
		if (rm == null) {
			rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		}
		return rm;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void forParams(String parameters) {
		cartId = parameters;
	}

	public List<String> getRecordsIds(String schemaType) {
		switch (schemaType){
			case Folder.SCHEMA_TYPE : return cart().getFolders();
			case Document.SCHEMA_TYPE : return cart().getDocuments();
			case ContainerRecord.SCHEMA_TYPE : return cart().getContainers();
			default : throw new RuntimeException("Unsupported type : " + schemaType);
		}
	}

	@Override
	public String getOriginSchema(String schemaType, List<String> selectedRecordIds) {
		//TODO N
		return null;
	}

	@Override
	public List<String> getDestinationSchemata(String originSchema) {
		//TODO N
		return null;
	}

	@Override
	public RecordVO newRecordVO(String schema, SessionContext sessionContext) {
		//TODO N
		return null;
	}

	@Override
	public void simulateButtonClicked(RecordVO viewObject) {
		//TODO N

	}

	@Override
	public void saveButtonClicked(RecordVO viewObject) {
		//TODO N

	}

	@Override
	public BatchProcessingMode getBatchProcessingMode() {
		//TODO N
		return null;
	}

	public List<LabelTemplate> getTemplates(String schemaType) {
		LabelTemplateManager labelTemplateManager = appLayerFactory.getLabelTemplateManager();
		return labelTemplateManager.listTemplates(schemaType);
	}

	public boolean isLabelsButtonVisible(String schemaType) {
		switch (schemaType){
			case Folder.SCHEMA_TYPE : return cart().getFolders().size() != 0;
			case ContainerRecord.SCHEMA_TYPE : return cart().getContainers().size() != 0;
			default : throw new RuntimeException("No labels for type : " + schemaType);
		}
	}

	public boolean isBatchProcessingButtonVisible(String schemaType) {
		return getRecordsIds(schemaType).size() != 0;
	}

	public void shareWithUsersRequested(List<String> userids) {
		cart().setSharedWithUsers(userids);
		addOrUpdate(cart().getWrappedRecord());
	}
}
