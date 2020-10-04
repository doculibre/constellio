package com.constellio.app.services.menu.behavior.ui;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCategoryFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderRetentionRuleFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMUserFolder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.table.SelectionTableAdapter;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class ClassifyWindow {

	private AppLayerFactory appLayerFactory;

	public ClassifyWindow(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	public void classfiy(List<String> recordIds, MenuItemActionBehaviorParams param) {
		WindowButton classifyButton = new WindowButton($("ConstellioHeader.selection.actions.classify"), $("ConstellioHeader.selection.actions.classify")
				, WindowButton.WindowConfiguration.modalDialog("90%", "300px")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout.addStyleName("no-scroll");
				verticalLayout.setSpacing(true);

				final LookupFolderField folderField = new LookupFolderField(true);
				folderField.setVisible(true);
				folderField.setRequired(true);
				folderField.focus();

				final FolderCategoryFieldImpl categoryField = new FolderCategoryFieldImpl();
				categoryField.setVisible(false);
				categoryField.setRequired(false);

				final FolderRetentionRuleFieldImpl retentionRuleField = new FolderRetentionRuleFieldImpl(param.getView().getCollection());
				retentionRuleField.setVisible(false);

				final ListOptionGroup classificationOption = new ListOptionGroup($("ConstellioHeader.selection.actions.classificationChoice"), asList(true, false));
				classificationOption.addStyleName("horizontal");
				classificationOption.setNullSelectionAllowed(false);
				classificationOption.setItemCaption(true, $("ConstellioHeader.selection.actions.classifyInClassificationPlan"));
				classificationOption.setItemCaption(false, $("ConstellioHeader.selection.actions.classifyInFolder"));
				classificationOption.setValue(false);
				classificationOption.addValueChangeListener(new Property.ValueChangeListener() {
					@Override
					public void valueChange(Property.ValueChangeEvent event) {
						boolean categoryClassification = Boolean.TRUE.equals(event.getProperty().getValue());
						folderField.setVisible(!categoryClassification);
						folderField.setRequired(!categoryClassification);
						categoryField.setVisible(categoryClassification);
						categoryField.setRequired(categoryClassification);
						if (categoryClassification) {
							String categoryId = (String) categoryField.getValue();
							adjustRetentionRuleField(categoryId, retentionRuleField);
						} else {
							retentionRuleField.setVisible(false);
						}
						if (categoryClassification) {
							categoryField.focus();
						} else {
							folderField.focus();
						}
					}
				});

				List<String> recordsIdSchemaTypes = new ArrayList<>();

				RecordServices recordServices = param.getView().getConstellioFactories().getModelLayerFactory().newRecordServices();

				for (String currentId : recordIds) {
					Record record = recordServices.getDocumentById(currentId);
					String schemaType = record.getSchemaCode().split("_")[0];
					if (!recordsIdSchemaTypes.contains(schemaType)) {
						recordsIdSchemaTypes.add(schemaType);
					}
				}
				classificationOption.setVisible(containsOnly(recordsIdSchemaTypes, asList(UserFolder.SCHEMA_TYPE)));

				categoryField.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						String categoryId = (String) event.getProperty().getValue();
						adjustRetentionRuleField(categoryId, retentionRuleField);
					}
				});

				verticalLayout.addComponents(classificationOption, folderField, categoryField, retentionRuleField);
				BaseButton saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						String parentId = (String) folderField.getValue();
						String categoryId = (String) categoryField.getValue();
						String retentionRuleId = retentionRuleField.getValue();
						if (parentId == null && categoryId == null) {
							if (folderField.isVisible()) {
								param.getView().showErrorMessage($("ConstellioHeader.noParentFolderSelectedForClassification"));
								return;
							} else {
								param.getView().showErrorMessage($("ConstellioHeader.noCategorySelectedForClassification"));
								return;
							}
						}
						boolean isClassifiedInFolder = !Boolean.TRUE.equals(classificationOption.getValue());
						try {
							classifyButtonClicked(parentId, categoryId, retentionRuleId, isClassifiedInFolder, recordIds, param);
						} catch (Throwable e) {
							//                            LOGGER.warn("error when trying to modify folder parent to " + parentId, e);
							//                            showErrorMessage("DisplayFolderView.parentFolderException");
							e.printStackTrace();
						}
						getWindow().close();
						ConstellioUI.getCurrent().updateContent();
					}
				};
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				HorizontalLayout hLayout = new HorizontalLayout();
				hLayout.setSpacing(true);
				hLayout.setSizeFull();
				hLayout.addComponent(saveButton);
				hLayout.setComponentAlignment(saveButton, Alignment.BOTTOM_CENTER);
				verticalLayout.addComponent(hLayout);
				return verticalLayout;
			}

			private void adjustRetentionRuleField(String categoryId, FolderRetentionRuleFieldImpl retentionRuleField) {
				if (categoryId != null) {
					RMSchemasRecordsServices rm = new RMSchemasRecordsServices(param.getView().getCollection(),
							appLayerFactory);
					Category category = rm.getCategory(categoryId);
					List<String> retentionRules = category.getRententionRules();
					boolean manyRetentionRules = retentionRules.size() > 1;
					if (manyRetentionRules) {
						retentionRuleField.setVisible(true);
						retentionRuleField.setOptions(retentionRules);
						retentionRuleField.setRequired(true);
					} else {
						retentionRuleField.setVisible(false);
						retentionRuleField.setRequired(false);
					}
				} else {
					retentionRuleField.setRequired(false);
					retentionRuleField.setVisible(false);
				}
			}
		};

		classifyButton.click();
	}

	public void classifyButtonClicked(String parentId, String categoryId, String retentionRuleId,
									  boolean isClassifiedInFolder, List<String> recordIds,
									  MenuItemActionBehaviorParams param) {

		List<String> couldNotMove = new ArrayList<>();
		if ((isClassifiedInFolder && isNotBlank(parentId)) || (!isClassifiedInFolder && isNotBlank(categoryId))) {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(param.getView().getCollection(), appLayerFactory);
			for (String id : recordIds) {
				Record record = null;
				RecordServices recordServices = param.getView().getConstellioFactories().getModelLayerFactory().newRecordServices();
				try {
					record = recordServices.getDocumentById(id);
					switch (record.getTypeCode()) {
						case UserFolder.SCHEMA_TYPE:
							Folder newFolder = rm.newFolder();
							RMUserFolder userFolder = rm.wrapUserFolder(record);
							if (!isClassifiedInFolder) {
								classifyUserFolderInCategory(param, categoryId, retentionRuleId, userFolder);
							}
							decommissioningService(param).populateFolderFromUserFolder(newFolder, userFolder, param.getUser());
							if (isClassifiedInFolder) {
								newFolder.setParentFolder(parentId);
							}
							recordServices.add(newFolder);
							decommissioningService(param).duplicateSubStructureAndSave(newFolder, userFolder, param.getUser());
							deleteUserFolder(param, userFolder, param.getUser());
							break;
						case UserDocument.SCHEMA_TYPE:
							Document newDocument = rm.newDocument();
							UserDocument userDocument = rm.wrapUserDocument(record);
							decommissioningService(param).populateDocumentFromUserDocument(newDocument, userDocument, param.getUser());
							newDocument.setFolder(parentId);
							recordServices.add(newDocument);
							deleteUserDocument(param, rm.wrapUserDocument(record), param.getUser());
							break;
						default:
							couldNotMove.add(record.getTitle());
					}
				} catch (RecordServicesException e) {
					if (record != null) {
						couldNotMove.add(record.getTitle());
					}
					e.printStackTrace();
				} catch (IOException e) {
					if (record != null) {
						couldNotMove.add(record.getTitle());
					}
					e.printStackTrace();
				}
			}
		}

		if (couldNotMove.isEmpty()) {
			param.getView().showErrorMessage($("ConstellioHeader.selection.actions.actionCompleted", recordIds.size()));
		} else {
			int successCount = recordIds.size() - couldNotMove.size();
			param.getView().showErrorMessage($("ConstellioHeader.selection.actions.couldNotClassify", successCount, recordIds.size()));
		}
	}

	protected void deleteUserDocument(MenuItemActionBehaviorParams param, UserDocument userDocument, User user) {
		decommissioningService(param).deleteUserDocument(userDocument, user);
		refreshSelectionTables(userDocument);
	}

	protected void deleteUserFolder(MenuItemActionBehaviorParams param, RMUserFolder rmUserFolder, User user) {
		decommissioningService(param).deleteUserFolder(rmUserFolder, user);
		refreshSelectionTables(rmUserFolder);
	}

	private void refreshSelectionTables(RecordWrapper recordWrapper) {
		String recordId = recordWrapper.getId();
		Collection<Window> windows = UI.getCurrent().getWindows();
		for (Window window : windows) {
			SelectionTableAdapter selectionTableAdapter = ComponentTreeUtils.getFirstChild(window, SelectionTableAdapter.class);
			if (selectionTableAdapter != null) {
				try {
					selectionTableAdapter.getTable().removeItem(recordId);
				} catch (Throwable t) {
					selectionTableAdapter.refresh();
				}
			}
		}
		//        View currentView = ConstellioUI.getCurrent().getCurrentView();
		//        if (currentView instanceof ListUserDocumentsView) {
		//        	((ListUserDocumentsView) currentView).refresh();
		//        }
	}

	private DecommissioningService decommissioningService(MenuItemActionBehaviorParams param) {
		return new DecommissioningService(param.getUser().getCollection(), appLayerFactory);
	}

	public void classifyUserFolderInCategory(MenuItemActionBehaviorParams param, String categoryId,
											 String retentionRuleId,
											 RMUserFolder userFolder) {
		User currentUser = param.getUser();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(param.getView().getCollection(), appLayerFactory);
		Category category = rm.getCategory(categoryId);
		userFolder.setCategory(category);
		List<String> retentionRules = category.getRententionRules();
		if (retentionRuleId != null) {
			userFolder.setRetentionRule(retentionRuleId);
		} else if (retentionRules.size() == 1) {
			userFolder.setRetentionRule(retentionRules.get(0));
		}
		AdministrativeUnit administrativeUnit = getDefaultAdministrativeUnit(currentUser);
		userFolder.setAdministrativeUnit(administrativeUnit);
	}

	private AdministrativeUnit getDefaultAdministrativeUnit(User user) {
		String collection = user.getCollection();
		AdministrativeUnit defaultAdministrativeUnit;
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		MetadataSchemaType administrativeUnitSchemaType = types.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
		LogicalSearchQuery visibleAdministrativeUnitsQuery = new LogicalSearchQuery();
		visibleAdministrativeUnitsQuery.filteredWithUserWrite(user);
		LogicalSearchCondition visibleAdministrativeUnitsCondition = from(administrativeUnitSchemaType).returnAll();
		visibleAdministrativeUnitsQuery.setCondition(visibleAdministrativeUnitsCondition);
		if (searchServices.getResultsCount(visibleAdministrativeUnitsQuery) > 0) {
			Record defaultAdministrativeUnitRecord = searchServices.search(visibleAdministrativeUnitsQuery).get(0);
			defaultAdministrativeUnit = rm.wrapAdministrativeUnit(defaultAdministrativeUnitRecord);
		} else {
			defaultAdministrativeUnit = null;
		}
		return defaultAdministrativeUnit;
	}

	public boolean containsOnly(List<String> list, List<String> values) {
		for (String value : list) {
			if (!values.contains(value)) {
				return false;
			}
		}
		return true && list.size() > 0;
	}
}
