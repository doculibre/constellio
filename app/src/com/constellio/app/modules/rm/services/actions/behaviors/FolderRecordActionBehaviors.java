package com.constellio.app.modules.rm.services.actions.behaviors;

import com.constellio.app.api.extensions.taxonomies.FolderDeletionEvent;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;
import com.constellio.app.modules.rm.util.RMNavigationUtils;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DeleteWithJustificationButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.dialogs.ConfirmDialog;

import static com.constellio.app.ui.framework.components.ErrorDisplayUtil.showErrorMessage;
import static com.constellio.app.ui.i18n.i18n.$;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Slf4j
public class FolderRecordActionBehaviors {

	private String collection;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;

	public FolderRecordActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		recordServices = modelLayerFactory.newRecordServices();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public void addToDocument(RecordActionBehaviorParams params) {
		Button addDocumentButton = new AddButton($("DisplayFolderView.addDocument")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				params.getView().navigate().to(RMViews.class).addDocument(params.getRecordVO().getId());
			}
		};
		addDocumentButton.click();
	}

	public void move(RecordActionBehaviorParams params) {
		Button moveInFolderButton = new WindowButton($("DisplayFolderView.parentFolder"),
				$("DisplayFolderView.parentFolder"), WindowButton.WindowConfiguration.modalDialog("50%", "20%")) {
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
							RMSchemasRecordsServices rmSchemas = new RMSchemasRecordsServices(collection, appLayerFactory);

							String currentFolderId = params.getRecordVO().getId();
							if (isNotBlank(parentId)) {
								try {
									recordServices.update(rmSchemas.getFolder(currentFolderId).setParentFolder(parentId));
									params.getView().navigate().to(RMViews.class).displayFolder(currentFolderId);
								} catch (RecordServicesException.ValidationException e) {
									params.getView().showErrorMessage($(e.getErrors()));
								}
							}
						} catch (Throwable e) {
							log.warn("error when trying to modify folder parent to " + parentId, e);
							showErrorMessage("DisplayFolderView.parentFolderException");
						}
						getWindow().close();
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
		moveInFolderButton.click();
	}

	public void addSubFolder(RecordActionBehaviorParams params) {
		Button addSubFolderButton = new AddButton($("DisplayFolderView.addSubFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				params.getView().navigate().to(RMViews.class).addFolder(params.getRecordVO().getId());
			}
		};
		addSubFolderButton.click();
	}

	public void display(RecordActionBehaviorParams params) {
		Button displayFolderButton = new DisplayButton($("DisplayFolderView.displayFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				RMNavigationUtils.navigateToDisplayFolder(params.getRecordVO().getId(), params.getFormParams(),
						appLayerFactory, collection);
			}
		};
		displayFolderButton.click();
	}

	public void edit(RecordActionBehaviorParams params) {
		Button editFolderButton = new EditButton($("DisplayFolderView.editFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				RMNavigationUtils.navigateToEditFolder(params.getRecordVO().getId(), params.getFormParams(),
						appLayerFactory, collection);
			}
		};
		editFolderButton.click();
	}

	public void delete(RecordActionBehaviorParams params) {
		boolean needAReasonToDeleteFolder = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager())
				.isNeedingAReasonBeforeDeletingFolders();

		Button deleteFolderButton = new Button();
		if (!needAReasonToDeleteFolder) {
			deleteFolderButton = new DeleteButton($("DisplayFolderView.deleteFolder"), false) {
				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					deleteFolder(null, params);
				}

				@Override
				protected String getConfirmDialogMessage() {
					return $("ConfirmDialog.confirmDeleteWithRecord", params.getRecordVO().getTitle());
				}
			};
		} else {
			deleteFolderButton = new DeleteWithJustificationButton($("DisplayFolderView.deleteFolder"), false) {
				@Override
				protected void deletionConfirmed(String reason) {
					deleteFolder(reason, params);
				}

				@Override
				public Component getRecordCaption() {
					return new ReferenceDisplay(params.getRecordVO());
				}
			};
		}
		deleteFolderButton.click();
	}

	private void deleteFolder(String reason, RecordActionBehaviorParams params) {
		String parentId = params.getRecordVO().get(Folder.PARENT_FOLDER);
		SchemaPresenterUtils presenterUtils = new SchemaPresenterUtils(Folder.DEFAULT_SCHEMA,
				params.getView().getConstellioFactories(), params.getView().getSessionContext());
		Record record = presenterUtils.toRecord(params.getRecordVO());
		ValidationErrors validateLogicallyDeletable = recordServices.validateLogicallyDeletable(record, params.getUser());
		if (validateLogicallyDeletable.isEmpty()) {
			appLayerFactory.getExtensions().forCollection(collection)
					.notifyFolderDeletion(new FolderDeletionEvent(rm.wrapFolder(record)));

			boolean isDeleteSuccessful = delete(presenterUtils, params.getView(), record, reason, false, 1);
			if (isDeleteSuccessful) {
				if (parentId != null) {
					RMNavigationUtils.navigateToDisplayFolder(parentId, params.getFormParams(), appLayerFactory, collection);
				} else {
					params.getView().navigate().to().home();
				}
			}
		} else {
			MessageUtils.getCannotDeleteWindow(validateLogicallyDeletable).openWindow();
		}
	}

	private boolean delete(SchemaPresenterUtils presenterUtils, BaseView view, Record record, String reason,
						   boolean physically, int waitSeconds) {
		boolean isDeletetionSuccessful = false;
		try {
			presenterUtils.delete(record, reason, physically, waitSeconds);
			isDeletetionSuccessful = true;
		} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord exception) {
			view.showErrorMessage(MessageUtils.toMessage(exception));
		} catch (RecordDeleteServicesRuntimeException exception) {
			view.showErrorMessage(i18n.$("deletionFailed") + "\n" + MessageUtils.toMessage(exception));
		}

		return isDeletetionSuccessful;
	}
}
