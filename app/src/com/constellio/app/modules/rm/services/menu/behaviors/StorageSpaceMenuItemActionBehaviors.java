package com.constellio.app.modules.rm.services.menu.behaviors;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.menu.behaviors.util.RMUrlUtil;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.framework.clipboard.CopyToClipBoard;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.SelectionPanelReportPresenter;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.util.UrlUtil.getConstellioUrl;
import static java.util.Arrays.asList;

public class StorageSpaceMenuItemActionBehaviors {
	private String collection;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private RecordServices recordServices;


	public StorageSpaceMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.recordServices = modelLayerFactory.newRecordServices();
	}

	public void generateReport(StorageSpace storageSpace, MenuItemActionBehaviorParams params) {
		SelectionPanelReportPresenter reportPresenter =
				new SelectionPanelReportPresenter(appLayerFactory, collection, params.getUser()) {
					@Override
					public String getSelectedSchemaType() {
						return StorageSpace.SCHEMA_TYPE;
					}

					@Override
					public List<String> getSelectedRecordIds() {
						return asList(storageSpace.getId());
					}
				};

		ReportTabButton reportGeneratorButton = new ReportTabButton($("SearchView.metadataReportTitle"), $("SearchView.metadataReportTitle"), appLayerFactory,
				params.getView().getCollection(), reportPresenter, params.getView().getSessionContext()) {
			@Override
			public void buttonClick(ClickEvent event) {
				setRecordVoList(params.getRecordVO());
				super.buttonClick(event);
			}
		};

		reportGeneratorButton.click();
	}

	public void consult(StorageSpace storageSpace, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to(RMViews.class).taxonomyManagement("containers", storageSpace.getId());
	}

	public void edit(StorageSpace storageSpace, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to(RMViews.class).editTaxonomyConcept("containers",
				storageSpace.getId(), storageSpace.getSchema().getCode());
	}

	public void delete(StorageSpace storageSpace, MenuItemActionBehaviorParams params) {
		BaseView view = params.getView();

		ValidationErrors validationErrors =
				recordServices.validateLogicallyThenPhysicallyDeletable(storageSpace.getWrappedRecord(), params.getUser());
		if (validationErrors.isEmpty()) {
			SchemaPresenterUtils utils = new SchemaPresenterUtils(storageSpace.getSchema().getCode(), view.getConstellioFactories(),
					view.getSessionContext());
			utils.delete(storageSpace.getWrappedRecord(), null, true);
			view.partialRefresh();
		} else {
			MessageUtils.getCannotDeleteWindow(validationErrors).openWindow();
		}
	}

	public void getConsultationLink(StorageSpace storageSpace, MenuItemActionBehaviorParams params) {
		String constellioURL = getConstellioUrl(modelLayerFactory);

		CopyToClipBoard.copyToClipBoard(constellioURL + RMUrlUtil.getPathToConsultLinkForContainerRecord(storageSpace.getId()));
	}
}
