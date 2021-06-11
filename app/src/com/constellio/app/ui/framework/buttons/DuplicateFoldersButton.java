package com.constellio.app.ui.framework.buttons;

import com.constellio.app.modules.rm.services.actions.FolderRecordActionsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DuplicateFoldersButton extends WindowButton {
	private FolderRecordActionsServices folderRecordActionsServices;
	private List<Folder> folders;
	private MenuItemActionBehaviorParams params;
	private DecommissioningService decommissioningService;

	public DuplicateFoldersButton(List<Folder> folders, FolderRecordActionsServices folderRecordActionsServices,
								  MenuItemActionBehaviorParams params, DecommissioningService decommissioningService) {
		super("DisplayFolderView.duplicateFolder",
				$("DisplayFolderView.duplicateFolderOnlyOrHierarchy"),
				WindowConfiguration.modalDialog("50%", "20%"));
		this.folderRecordActionsServices = folderRecordActionsServices;
		this.folders = folders;
		this.params = params;
		this.decommissioningService = decommissioningService;
	}

	@Override
	protected Component buildWindowContent() {
		BaseButton folderButton = buildFolderOnlyButton();
		BaseButton structure = buildHierarchyButton();
		BaseButton cancel = buildCancelButton();

		HorizontalLayout layout = new HorizontalLayout(folderButton, structure, cancel);
		layout.setSpacing(true);

		VerticalLayout wrapper = new VerticalLayout(layout);
		wrapper.setSizeFull();
		wrapper.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);

		return wrapper;
	}

	protected BaseButton buildFolderOnlyButton() {
		BaseButton folderButton = new BaseButton($("DisplayFolderView.folderOnly")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (isDuplicationPossible()) {
					try {
						duplicateFoldersForm();
						params.getView().showMessage($("DuplicateFoldersButton.duplicatedFolderOnly"));
						refresh();
					} catch (RecordServicesException.ValidationException e) {
						params.getView().showErrorMessage($(e.getErrors()));
					} catch (Exception e) {
						params.getView().showErrorMessage(e.getMessage());
					}
				}
				if (!params.isNestedView()) {
					params.getView().closeAllWindows();
				}
			}
		};
		return folderButton;
	}

	protected BaseButton buildHierarchyButton() {
		BaseButton hierarchyButton = new BaseButton($("DisplayFolderView.hierarchy")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (isDuplicationPossible()) {
					try {
						validateStructure();
						duplicateFoldersStructure();
						params.getView().showMessage($("DuplicateFoldersButton.duplicatedHierarchy"));
						refresh();
					} catch (RecordServicesException.ValidationException e) {
						params.getView().showErrorMessage($(e.getErrors()));
					} catch (Exception e) {
						params.getView().showErrorMessage(e.getMessage());
					}
				}
				if (!params.isNestedView()) {
					params.getView().closeAllWindows();
				}
			}
		};
		return hierarchyButton;
	}

	private BaseButton buildCancelButton() {
		BaseButton cancelButton = new BaseButton($("cancel")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				getWindow().close();
			}
		};
		cancelButton.addStyleName(ValoTheme.BUTTON_LINK);
		return cancelButton;
	}

	private boolean isDuplicationPossible() {
		for (Folder folder : folders) {
			if (!folderRecordActionsServices.isCopyActionPossible(folder.getWrappedRecord(), params.getUser())) {
				return false;
			}
		}
		return true;
	}

	private void validateStructure() throws RecordServicesException {
		for (Folder folder : folders) {
			decommissioningService.validateDuplicateStructure(folder, params.getUser(), false);
		}
	}

	private void duplicateFoldersForm() throws RecordServicesException {
		decommissioningService.batchDuplicateForm(folders, params.getUser());
	}

	private void duplicateFoldersStructure() throws RecordServicesException {
		decommissioningService.batchDuplicateStructure(folders, params.getUser(), false);
	}

	private void refresh() {
		params.getView().updateUI();
		params.getView().refreshActionMenu();
	}
};

