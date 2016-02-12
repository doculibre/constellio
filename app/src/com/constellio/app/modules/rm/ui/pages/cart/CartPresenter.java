package com.constellio.app.modules.rm.ui.pages.cart;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.cart.CartEmlService;
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
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class CartPresenter extends SingleSchemaBasePresenter<CartView> {
	private transient RMSchemasRecordsServices rm;
	private transient Cart cart;

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
		view.navigateTo().cart();
	}

	public boolean canEmptyCart() {
		return cartHasRecords();
	}

	public void cartEmptyingRequested() {
		addOrUpdate(cart().empty().getWrappedRecord());
		view.navigateTo().cart();
	}

	public boolean canPrepareEmail() {
		// TODO: Maybe better test
		return cartHasRecords();
	}

	public void emailPreparationRequested() {
		InputStream stream = new CartEmlService(collection, modelLayerFactory).createEmlForCart(cart());
		view.startDownload(stream);
	}

	public boolean canDelete() {
		// TODO: Add permission test for folders and documents
		return cartHasRecords() && cart.getContainers().isEmpty();
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

	private Cart cart() {
		if (cart == null) {
			cart = rm().getOrCreateUserCart(getCurrentUser());
		}
		return cart;
	}

	private boolean cartHasRecords() {
		return !cart().isEmpty();
	}

	private boolean canDeleteFolders(User user) {
		for (Folder folder : rm.wrapFolders(recordServices().getRecordsById(view.getCollection(), cart().getFolders()))) {
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
			}
		}
		return true;
	}

	private boolean canDeleteDocuments(User user) {
		for (Document document : rm.wrapDocuments(recordServices().getRecordsById(view.getCollection(), cart().getDocuments()))) {
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
}
