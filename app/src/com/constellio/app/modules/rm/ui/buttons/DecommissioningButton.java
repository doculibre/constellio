package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningListParams;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningBuilderViewImpl;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

@Slf4j
public class DecommissioningButton extends WindowButton {
	public static final String TITLE = "dl-title";
	public static final String DESCRIPTION = "dl-description";

	@PropertyId("title") private BaseTextField title;
	@PropertyId("description") private BaseTextArea description;

	private List<String> recordIds;
	private MenuItemActionBehaviorParams behaviorParams;
	private AppLayerFactory appLayerFactory;

	public DecommissioningButton(String caption, List<String> recordIds, MenuItemActionBehaviorParams behaviorParams,
								 AppLayerFactory appLayerFactory) {
		super(caption, caption);
		this.recordIds = recordIds;
		this.behaviorParams = behaviorParams;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	protected Component buildWindowContent() {
		title = new BaseTextField($("DecommissioningBuilderView.title"));
		title.setRequired(true);
		title.setId(TITLE);

		description = new BaseTextArea($("DecommissioningBuilderView.description"));
		description.setId(DESCRIPTION);

		return new BaseForm<DecommissioningListParams>(
				new DecommissioningListParams(), this, title, description) {
			@Override
			protected void saveButtonClick(DecommissioningListParams params)
					throws ValidationException {
				getWindow().close();
				params.setSelectedRecordIds(recordIds);
				if (isDecommissioningListWithSelectedFolders()) {
					params.setFolderDetailStatus(FolderDetailStatus.SELECTED);
				} else {
					params.setFolderDetailStatus(FolderDetailStatus.INCLUDED);
				}
				decommissioningListCreationRequested(params);
			}

			@Override
			protected void cancelButtonClick(DecommissioningListParams params) {
				getWindow().close();
			}
		};
	}

	private boolean isDecommissioningListWithSelectedFolders() {
		return new RMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager())
				.isDecommissioningListWithSelectedFolders();
	}

	private void decommissioningListCreationRequested(DecommissioningListParams params) {
		DecommissioningBuilderViewImpl view = (DecommissioningBuilderViewImpl) behaviorParams.getView();
		User user = behaviorParams.getUser();

		DecommissioningService decommissioningService = new DecommissioningService(view.getCollection(), appLayerFactory);
		params.setAdministrativeUnit(view.getAdminUnitId());
		params.setSearchType(view.getSearchType());
		try {
			if (params.getSelectedRecordIds() != null && params.getSelectedRecordIds().size() > 1000) {
				view.showErrorMessage($("DecommissioningBuilderView.cannotBuildADecommissioningListWithMoreThan1000Records"));
			} else {
				DecommissioningList decommissioningList = decommissioningService
						.createDecommissioningList(params, user);
				if (decommissioningList.getDecommissioningListType().isFolderList()) {
					view.navigate().to(RMViews.class).displayDecommissioningList(decommissioningList.getId());
				} else {
					view.navigate().to(RMViews.class).displayDocumentDecommissioningList(decommissioningList.getId());
				}
			}
		} catch (Exception e) {
			log.error("Error while creating decommissioning list", e);
			view.showErrorMessage($("DecommissioningBuilderView.unableToSave"));
		}
	}
}
