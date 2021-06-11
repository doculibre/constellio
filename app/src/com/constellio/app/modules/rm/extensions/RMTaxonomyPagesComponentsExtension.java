package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.SelectionPanelReportPresenter;
import com.constellio.app.ui.framework.decorators.base.ActionMenuButtonsDecorator;
import com.constellio.app.ui.pages.base.BasePresenterUtils;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.management.taxonomy.TaxonomyManagementViewImpl;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class RMTaxonomyPagesComponentsExtension extends PagesComponentsExtension {

	String collection;
	AppLayerFactory appLayerFactory;

	public RMTaxonomyPagesComponentsExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void decorateMainComponentBeforeViewAssembledOnViewEntered(
			DecorateMainComponentAfterInitExtensionParams params) {
		super.decorateMainComponentAfterViewAssembledOnViewEntered(params);

		Component mainComponent = params.getMainComponent();
		if (mainComponent instanceof TaxonomyManagementViewImpl) {
			TaxonomyManagementViewImpl taxonomyManagementView = (TaxonomyManagementViewImpl) mainComponent;
			taxonomyManagementView.addActionMenuButtonsDecorator((ActionMenuButtonsDecorator) (view, actionMenuButtons) -> {
				if (taxonomyManagementView.getCurrentConcept() != null && (AdministrativeUnit.SCHEMA_TYPE.equals(taxonomyManagementView.getCurrentConcept().getSchema().getTypeCode()) ||
																		   Category.SCHEMA_TYPE.equals(taxonomyManagementView.getCurrentConcept().getSchema().getTypeCode()))) {
					actionMenuButtons.add(buildAdditionalMenuButton(taxonomyManagementView));
				}
			});
		}
	}

	private Button buildAdditionalMenuButton(TaxonomyManagementViewImpl taxonomyManagementView) {
		SelectionPanelReportPresenter selectionPanelReportPresenter = new SelectionPanelReportPresenter(appLayerFactory, collection, getCurrentUser(taxonomyManagementView)) {
			@Override
			public String getSelectedSchemaType() {
				return taxonomyManagementView.getCurrentConcept().getSchema().getTypeCode();
			}

			@Override
			public List<String> getSelectedRecordIds() {
				return asList(taxonomyManagementView.getCurrentConcept().getId());
			}
		};

		ReportTabButton reportGeneratorButton = new ReportTabButton($("SearchView.metadataReportTitle"),
				$("SearchView.metadataReportTitle"), appLayerFactory, collection,
				selectionPanelReportPresenter, taxonomyManagementView.getSessionContext()) {
			@Override
			public void buttonClick(ClickEvent event) {
				setRecordVoList(taxonomyManagementView.getCurrentConcept());
				super.buttonClick(event);
			}
		};
		return reportGeneratorButton;
	}

	private User getCurrentUser(BaseView view) {
		BasePresenterUtils basePresenterUtils = new BasePresenterUtils(view.getConstellioFactories(), view.getSessionContext());
		return basePresenterUtils.getCurrentUser();
	}
}
