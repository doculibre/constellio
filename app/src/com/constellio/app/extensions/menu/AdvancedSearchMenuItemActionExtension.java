package com.constellio.app.extensions.menu;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.services.menu.behavior.ui.AdvancedViewBatchProcessingPresenter;
import com.constellio.app.services.menu.behavior.ui.AdvancedViewBatchProcessingViewImpl;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.pages.search.AdvancedSearchViewImpl;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingButton;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

@Slf4j
public abstract class AdvancedSearchMenuItemActionExtension extends MenuItemActionsExtension {
	protected String collection;
	protected AppLayerFactory appLayerFactory;
	protected ModelLayerFactory modelLayerFactory;
	protected RecordServices recordServices;

	private static final String RECORDS_GENERATE_REPORT = "RECORDS_GENERATE_REPORT";
	private static final String RECORDS_BATCH = "RECORDS_GENERATE_REPORT";

	public AdvancedSearchMenuItemActionExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		recordServices = modelLayerFactory.newRecordServices();
	}

	@Override
	public void addMenuItemActionsForQuery(MenuItemActionExtensionAddMenuItemActionsForQueryParams params) {
		if (!(params.getBehaviorParams().getView() instanceof AdvancedSearchViewImpl)) {
			return;
		}

		MenuItemAction menuItemAction = MenuItemAction.builder()
				.type(RECORDS_BATCH)
				.state(getActionStateForBatchProcessingInternal(params.getQuery(), params.getBehaviorParams().getUser(), params.isReturnedResults()))
				.caption($("AdvancedSearchView.batchProcessing"))
				.icon(null)
				.group(-1)
				.priority(1100)
				.recordsLimit(-1)
				.command((ids) -> batchProcess(params.getQuery(), params.getBehaviorParams()))
				.build();
		params.getMenuItemActions().add(menuItemAction);

	}

	@Override
	public MenuItemActionState getActionStateForQuery(MenuItemActionExtensionGetActionStateForQueryParams params) {
		if (params.getMenuItemActionType().equals(RECORDS_GENERATE_REPORT)) {
			return getActionStateForBatchProcessing(params.getQuery(), params.getBehaviorParams().getUser());
		} else if (params.getMenuItemActionType().equals(RECORDS_BATCH)) {
			return getActionStateForReports(params.getQuery());
		}
		return null;
	}

	private MenuItemActionState getActionStateForBatchProcessingInternal(LogicalSearchQuery logicalSearchQuery,
																		 User user, boolean hasResults) {
		if (logicalSearchQuery == null || !hasResults) {
			return MenuItemActionState.visibleOrHidden(false);
		}

		return getActionStateForBatchProcessing(logicalSearchQuery, user);
	}

	protected abstract MenuItemActionState getActionStateForBatchProcessing(LogicalSearchQuery query, User user);

	protected abstract MenuItemActionState getActionStateForReports(LogicalSearchQuery logicalSearchQuery);

	protected abstract boolean noPDFButton(String schemaType);

	private void batchProcess(LogicalSearchQuery query, MenuItemActionBehaviorParams params) {
		AdvancedViewBatchProcessingPresenter batchProcessingPresenter =
				new AdvancedViewBatchProcessingPresenter(appLayerFactory, (AdvancedSearchViewImpl) params.getView(),
						params.getUser(), query);
		AdvancedViewBatchProcessingViewImpl batchProcessingView =
				new AdvancedViewBatchProcessingViewImpl(batchProcessingPresenter);

		WindowButton button = new BatchProcessingButton(batchProcessingPresenter, batchProcessingView)
					.hasResultSelected(!batchProcessingView.getSelectedRecordIds().isEmpty());
		button.click();
	}

	public final MetadataSchemaTypes types() {
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		return metadataSchemasManager.getSchemaTypes(collection);
	}

	public final MetadataSchema metadataSchema(String code) {
		return types().getSchema(code);
	}

	public final MetadataSchemaType metadataSchemaType(String code) {
		return types().getSchemaType(code);
	}

	protected String getSchemaType(LogicalSearchQuery query) {
		List<String> schemaTypes = query.getCondition().getFilterSchemaTypesCodes();
		return schemaTypes != null && !schemaTypes.isEmpty() ? schemaTypes.get(0) : null;
	}
}
