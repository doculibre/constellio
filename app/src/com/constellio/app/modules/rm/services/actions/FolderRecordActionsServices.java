package com.constellio.app.modules.rm.services.actions;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class FolderRecordActionsServices {

	private RMSchemasRecordsServices rm;
	private RMModuleExtensions rmModuleExtensions;

	public FolderRecordActionsServices(String collection, AppLayerFactory appLayerFactory) {
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
	}

	public boolean isAddDocumentActionPossible(Record record, User user) {
		return hasUserWriteAccess(record, user) && isEditActionPossible(record, user) &&
			   rmModuleExtensions.isAddDocumentActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isMoveActionPossible(Record record, User user) {
		return hasUserWriteAccess(record, user) && isEditActionPossible(record, user) &&
			   rmModuleExtensions.isMoveActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isAddSubFolderActionPossible(Record record, User user) {
		return hasUserWriteAccess(record, user) && isEditActionPossible(record, user) &&
			   rmModuleExtensions.isAddSubFolderActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isDisplayActionPossible(Record record, User user) {
		return hasUserReadAccess(record, user) &&
			   rmModuleExtensions.isDisplayActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isEditActionPossible(Record record, User user) {
		return hasUserWriteAccess(record, user) &&
			   rmModuleExtensions.isEditActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isDeleteActionPossible(Record record, User user) {
		return user.hasDeleteAccess().on(record) &&
			   rmModuleExtensions.isDeleteActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isDuplicateActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);
		if (!hasUserReadAccess(record, user) ||
			(folder.getPermissionStatus().isInactive() && !user.has(RMPermissionsTo.DUPLICATE_INACTIVE_FOLDER).on(folder)) ||
			(folder.getPermissionStatus().isSemiActive() && !user.has(RMPermissionsTo.DUPLICATE_SEMIACTIVE_FOLDER).on(folder))) {
			return false;
		}
		return rmModuleExtensions.isCopyActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	// linkTo

	public boolean isAddAuthorizationActionPossible(Record record, User user) {
		return hasUserWriteAccess(record, user) && isEditActionPossible(record, user) &&
			   rmModuleExtensions.isAddAuthorizationActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isShareActionPossible(Record record, User user) {
		return hasUserWriteAccess(record, user) &&
			   rmModuleExtensions.isShareActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isAddToCartActionPossible(Record record, User user) {
		return hasUserReadAccess(record, user) &&
			   (hasUserPermissionToUseCart(user) || hasUserPermissionToUseMyCart(user)) &&
			   rmModuleExtensions.isAddToCartActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	// TODO
	/*
	public boolean isAddToOrRemoveFromSelectionActionPossible(Record record, User user) {
		return hasUserWriteAccess(record, user) &&
			   rmModuleExtensions.isEditActionPossibleOnFolder(rm.wrapFolder(record), user);
	}
	*/

	public boolean isBorrowActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);
		return hasUserWriteAccess(record, user) && isEditActionPossible(record, user) && !folder.getBorrowed() &&
			   rmModuleExtensions.isBorrowingActionPossibleOnFolder(folder, user);
	}

	public boolean isReturnActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);
		return hasUserWriteAccess(record, user) && isEditActionPossible(record, user) && folder.getBorrowed() &&
			   rmModuleExtensions.isReturnActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	// TODO rendu ici

	public boolean isPrintLabelActionPossible(Record record, User user) {
		return hasUserWriteAccess(record, user) &&
			   rmModuleExtensions.isEditActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isGenerateReportActionPossible(Record record, User user) {
		return hasUserWriteAccess(record, user) &&
			   rmModuleExtensions.isEditActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isStartWorkflowActionPossible(Record record, User user) {
		return hasUserWriteAccess(record, user) &&
			   rmModuleExtensions.isEditActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	/*

			addDocumentButton = new AddButton($("DisplayFolderView.addDocument")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.addDocumentButtonClicked();
				}
			};

			moveInFolderButton = new WindowButton($("DisplayFolderView.parentFolder"), $("DisplayFolderView.parentFolder")
					, WindowButton.WindowConfiguration.modalDialog("50%", "20%")) {
				@Override
				protected Component buildWindowContent() {
					VerticalLayout verticalLayout = new VerticalLayout();
					verticalLayout.setSpacing(true);
					final LookupFolderField field = new LookupFolderField();
					verticalLayout.addComponent(field);
					BaseButton saveButton = new BaseButton($("save")) {
						@Override
						protected void buttonClick(ClickEvent event) {
							String parentId = (String) field.getValue();
							try {
								presenter.parentFolderButtonClicked(parentId);
							} catch (Throwable e) {
								LOGGER.warn("error when trying to modify folder parent to " + parentId, e);
								showErrorMessage("DisplayFolderView.parentFolderException");
							}
							moveInFolderButton.getWindow().close();
						}
					};
					saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
					HorizontalLayout hLayout = new HorizontalLayout();
					hLayout.setSizeFull();
					hLayout.addComponent(saveButton);
					hLayout.setComponentAlignment(saveButton, Alignment.BOTTOM_RIGHT);
					verticalLayout.addComponent(hLayout);
					return verticalLayout;
				}
			};

			addSubFolderButton = new AddButton($("DisplayFolderView.addSubFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.addSubFolderButtonClicked();
				}
			};

			displayFolderButton = new DisplayButton($("DisplayFolderView.displayFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.displayFolderButtonClicked();
				}
			};

			editFolderButton = new EditButton($("DisplayFolderView.editFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.editFolderButtonClicked();
				}
			};

			deleteFolderButton = new Button();
			if (!presenter.isNeedingAReasonToDeleteFolder()) {
				deleteFolderButton = new DeleteButton($("DisplayFolderView.deleteFolder"), false) {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteFolderButtonClicked(null);
					}

					@Override
					protected String getConfirmDialogMessage() {
						return $("ConfirmDialog.confirmDeleteWithRecord", recordVO.getTitle());
					}
				};
			} else {
				deleteFolderButton = new DeleteWithJustificationButton($("DisplayFolderView.deleteFolder"), false) {
					@Override
					protected void deletionConfirmed(String reason) {
						presenter.deleteFolderButtonClicked(reason);
					}

					@Override
					public Component getRecordCaption() {
						return new ReferenceDisplay(recordVO);
					}
				};
			}

			duplicateFolderButton = new WindowButton($("DisplayFolderView.duplicateFolder"),
					$("DisplayFolderView.duplicateFolderOnlyOrHierarchy")) {
				@Override
				protected Component buildWindowContent() {
					BaseButton folder = new BaseButton($("DisplayFolderView.folderOnly")) {
						@Override
						protected void buttonClick(ClickEvent event) {
							presenter.duplicateFolderButtonClicked();
						}
					};

					BaseButton structure = new BaseButton($("DisplayFolderView.hierarchy")) {
						@Override
						protected void buttonClick(ClickEvent event) {
							presenter.duplicateStructureButtonClicked();
						}
					};

					BaseButton cancel = new BaseButton($("cancel")) {
						@Override
						protected void buttonClick(ClickEvent event) {
							getWindow().close();
						}
					};
					cancel.addStyleName(ValoTheme.BUTTON_LINK);

					HorizontalLayout layout = new HorizontalLayout(folder, structure, cancel);
					layout.setComponentAlignment(folder, Alignment.TOP_LEFT);
					layout.setComponentAlignment(structure, Alignment.TOP_LEFT);
					layout.setComponentAlignment(cancel, Alignment.TOP_RIGHT);
					layout.setExpandRatio(cancel, 1);

					layout.setWidth("95%");
					layout.setSpacing(true);

					VerticalLayout wrapper = new VerticalLayout(layout);
					wrapper.setSizeFull();

					return wrapper;
				}
			};

			linkToFolderButton = new LinkButton($("DisplayFolderView.linkToFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.linkToFolderButtonClicked();
				}
			};
			linkToFolderButton.setVisible(false);

			addAuthorizationButton = new LinkButton($("DisplayFolderView.addAuthorization")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.addAuthorizationButtonClicked();
				}
			};

			shareFolderButton = new LinkButton($("DisplayFolderView.shareFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.shareFolderButtonClicked();
				}
			};

			addToCartButton = buildAddToCartButton();
			addToCartMyCartButton = buildAddToMyCartButton();

			addToOrRemoveFromSelectionButton = new AddToOrRemoveFromSelectionButton(recordVO,
					getSessionContext().getSelectedRecordIds().contains(recordVO.getId()));

			Factory<List<LabelTemplate>> customLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
				@Override
				public List<LabelTemplate> get() {
					return presenter.getCustomTemplates();
				}
			};
			Factory<List<LabelTemplate>> defaultLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
				@Override
				public List<LabelTemplate> get() {
					return presenter.getDefaultTemplates();
				}
			};
			try {
				printLabelButton = new LabelButtonV2($("DisplayFolderView.printLabel"),
						$("DisplayFolderView.printLabel"), customLabelTemplatesFactory, defaultLabelTemplatesFactory,
						getConstellioFactories().getAppLayerFactory(), getSessionContext().getCurrentCollection(), getSessionContext().getCurrentUser(), recordVO);
			} catch (Exception e) {
				showErrorMessage(e.getMessage());
			}

			borrowButton = buildBorrowButton();

			returnFolderButton = buildReturnFolderButton();

			reminderReturnFolderButton = new BaseButton($("DisplayFolderView.reminderReturnFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.reminderReturnFolder();
				}
			};

			alertWhenAvailableButton = new BaseButton($("RMObject.alertWhenAvailable")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.alertWhenAvailable();
				}
			};

			reportGeneratorButton = new ReportTabButton($("SearchView.metadataReportTitle"), $("SearchView.metadataReportTitle"), presenter.getApplayerFactory(),
					getCollection(), false, false, presenter.buildReportPresenter(), getSessionContext()) {
				@Override
				public void buttonClick(ClickEvent event) {
					setRecordVoList(recordVO);
					super.buttonClick(event);
				}
			};

			startWorkflowButton = new StartWorkflowButton();
			startWorkflowButton.setVisible(presenter.hasPermissionToStartWorkflow());

	 */

	private boolean hasUserWriteAccess(Record record, User user) {
		return user.hasWriteAccess().on(record);
	}

	private boolean hasUserReadAccess(Record record, User user) {
		return user.hasReadAccess().on(record);
	}

	private boolean hasUserPermissionToUseCart(User user) {
		return user.has(RMPermissionsTo.USE_GROUP_CART).globally();
	}

	public boolean hasUserPermissionToUseMyCart(User user) {
		return user.has(RMPermissionsTo.USE_MY_CART).globally();
	}

}
