package com.constellio.app.ui.framework.components.table.field;

import com.constellio.app.events.EventArgs;
import com.constellio.app.events.EventListener;
import com.constellio.app.events.EventObservable;
import com.constellio.app.modules.rm.wrappers.LegalRequirementReference;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.DirtyableListenable;
import com.constellio.app.ui.framework.DirtyableListenable.DirtiedArgs.DirtiedListener;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.RecordVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.components.table.field.EditableRecordsTableField.TableRecorsdUpdatedArgs.TableRecorsdUpdatedListener;
import com.constellio.app.ui.framework.components.table.field.EditableRecordsTableField.TableRecorsdUpdatedArgs.TableRecorsdUpdatedObservable;
import com.constellio.app.ui.framework.components.table.field.EditableRecordsTableField.TableRecorsdUpdatedArgs.UpdateType;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class EditableRecordsTableField extends CustomField<List<String>> implements DirtyableListenable {
	public static final String CSS_TABLE_ROOT = "editable-record-table";
	public static final String CSS_FIELD_ROOT = CSS_TABLE_ROOT + "-field";
	public static final String CSS_FIELD_LAYOUT = "main-layout";
	public static final String CSS_CHECKBOX_CELL_STYLE = "boolean";
	public static final String CSS_LABEL_CELL_STYLE = "text";

	public static final String TABLE_HEIGHT_DEFAULT = "250px";
	public static final int TABLE_HEADER_HEIGHT = 40;
	public static final int TABLE_ROW_AVERAGE_HEIGHT = 40;
	public static final int TABLE_ROW_COUNT_BEFORE_SCROLLBAR_REQUIRED = 4;
	public static final boolean IS_RESIZED_IF_ROWS_DOES_NOT_FILL_HEIGHT = false;

	public static final String EDIT_DELETE_CONTROLS_COLUMNS_ID = "editDeleteColumn";
	public static final String EDIT_DELETE_CONTROLS_COLUMNS_CSS = "edit-delete";

	private EditableRecordsTablePresenter presenter;

	private final TableRecorsdUpdatedObservable tableRecorsdUpdatedObservable;

	private VerticalLayout mainLayout;
	private RecordVOLazyContainer container;
	private RecordVOTable currentTable;
	private String tableHeight;
	private boolean isResizedIfRowsDoesNotFillHeight;

	public EditableRecordsTableField(EditableRecordsTablePresenter presenter) {
		tableRecorsdUpdatedObservable = new TableRecorsdUpdatedObservable();

		this.presenter = Objects.requireNonNull(presenter);
		this.tableHeight = TABLE_HEIGHT_DEFAULT;
		this.isResizedIfRowsDoesNotFillHeight = IS_RESIZED_IF_ROWS_DOES_NOT_FILL_HEIGHT;
	}

	protected Component buildMainComponent() {
		addStyleName(CSS_FIELD_ROOT);

		mainLayout = new VerticalLayout();
		mainLayout.addStyleName(CSS_FIELD_LAYOUT);

		if (!isReadOnly() && isRecordAddable()) {
			Component addButton = buildAddControls();
			mainLayout.addComponent(addButton);
			mainLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		}

		presenter.getRecordVOS(this.createNewDataProviderAvailableCallback(mainLayout));

		return mainLayout;
	}

	private RecordVOTable buildTable(RecordVODataProvider dataProvider) {

		container = new RecordVOLazyContainer(dataProvider, true) {

		};

		RecordVOTable table = new RecordVOTable(container) {
			@Override
			protected ColumnGenerator newMenuBarColumnGenerator() {
				return (source, itemId, columnId) -> buildEditDeleteControls(source, itemId);
			}

			@Override
			public boolean isMenuBarColumn() {
				return !isReadOnly() && (isRecordDeletable() || isRecordEditable());
			}

			@Override
			protected TableColumnsManager newColumnsManager() {
				return new RecordVOTableColumnsManager() {
					@Override
					protected List<String> getDefaultVisibleColumnIds(Table table) {
						List<String> defaultVisibleColumnIds = super.getDefaultVisibleColumnIds(table);
						List<MetadataSchemaVO> schemas = dataProvider.getExtraSchemas();
						schemas.add(0, dataProvider.getSchema());

						List<MetadataVO> metadatasToHideEvenIfItsInTheDisplayConfig = presenter.getMetadatasToHideEvenIfItsInTheDisplayConfig();
						if (metadatasToHideEvenIfItsInTheDisplayConfig != null && !metadatasToHideEvenIfItsInTheDisplayConfig.isEmpty()) {
							List<String> metadataCodesToHide = metadatasToHideEvenIfItsInTheDisplayConfig.stream().map(MetadataVO::getCode).collect(Collectors.toList());

							defaultVisibleColumnIds = defaultVisibleColumnIds.stream()
									.filter(title -> !metadataCodesToHide.contains(title))
									.collect(Collectors.toList());
						}

						return defaultVisibleColumnIds;
					}
				};
			}

			@Override
			protected Property<?> loadContainerProperty(Object itemId, Object propertyId) {
				if (propertyId instanceof MetadataVO) {
					RecordVOItem recordVOItem = (RecordVOItem) getItem(itemId);
					RecordVO recordVO = recordVOItem.getRecord();
					MetadataVO metadataVO = (MetadataVO) propertyId;
					if ((LegalRequirementReference.DEFAULT_SCHEMA + "_" + LegalRequirementReference.RULE_REFERENCE).equals(metadataVO.getCode())) {
						MetadataValueVO metadataValue = recordVO.getMetadataValue(metadataVO);
						ReferenceDisplay metadataDisplay = (ReferenceDisplay) buildMetadataComponent(itemId, metadataValue, recordVO);
//						metadataDisplay.addExtension(new NiceTitle(getLegalRequirementReferenceDescription(metadataValue)));
						return new ObjectProperty<>(metadataDisplay, Component.class);
					} else {
						return super.loadContainerProperty(itemId, propertyId);
					}
				}
				return super.loadContainerProperty(itemId, propertyId);
			}

			private String getLegalRequirementReferenceDescription(MetadataValueVO metadataValue) {
				ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
				ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
				RecordServices recordServices = modelLayerFactory.newRecordServices();
				Record record = recordServices.getDocumentById(metadataValue.getValue());
				MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
				String collection = record.getCollection();
				MetadataSchemaTypes metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(collection);
				Metadata metadata = metadataSchemaTypes.getMetadata(LegalRequirementReference.DEFAULT_SCHEMA + "_" + LegalRequirementReference.DESCRIPTION);
				return record.get(metadata);
			}
		};

		table.addStyleName(CSS_TABLE_ROOT);
		table.addStyleName("wordwrap-headers");

		table.setWidth(100, Unit.PERCENTAGE);
		resizeHeightBasedOnRowCount(table, dataProvider.size());

		return table;
	}

	private Component buildEditDeleteControls(Table table, Object itemId) {
		Layout editDeleteLayout = new I18NHorizontalLayout();

		if (!isReadOnly() && isRecordEditable()) {
			editDeleteLayout.addComponent(new EditButton() {
				@Override
				protected void buttonClick(ClickEvent event) {
					RecordVO recordVO = container.getRecordVO(itemId);
					editThisRecord(recordVO, editedRecord -> {
						if (recordVO.equals(editedRecord)) {

							presenter.updateRecord(recordVO, recordUpdated -> {
								container.forceRefresh();
								postModification(editedRecord, UpdateType.UPDATED);
							});
						}
					});
				}
			});

			if (!isReadOnly() && isRecordDeletable()) {
				editDeleteLayout.addComponent(new IconButton(DeleteButton.ICON_RESOURCE, "") {
					@Override
					protected void buttonClick(ClickEvent event) {
						RecordVO recordVO = container.getRecordVO(itemId);
						thisRecordWillBeDeleted(recordVO, recordToDelete -> {
							if (recordVO.equals(recordToDelete)) {
								presenter.removeRecord(recordToDelete, recordDeleted -> {
									container.forceRefresh();
									postModification(recordDeleted, UpdateType.DELETED);
								});
							}
						});
					}
				});
			}
		}

		table.setColumnWidth(RecordVOTable.MENUBAR_PROPERTY_ID, 75);

		return editDeleteLayout;
	}

	private Component buildAddControls() {

		Button addRecordButton = new AddButton(getAddButtonCaption(), FontAwesome.PLUS, true) {
			@Override
			protected void buttonClick(ClickEvent event) {
				createNewRecord(newRecord -> {
					if (newRecord != null) {
						presenter.addRecord(newRecord, recordAdded -> {
							container.forceRefresh();
							postModification(recordAdded, UpdateType.ADDED);
						});
					}
				});
			}
		};

		addRecordButton.addStyleName(ValoTheme.BUTTON_LINK);

		return addRecordButton;
	}

	private void postModification(RecordVO recordVO, UpdateType updateType) {
		tableRecorsdUpdatedObservable.fire(new TableRecorsdUpdatedArgs(EditableRecordsTableField.this, recordVO, updateType));
		commit();
		markAsDirty();
		valueChange(new ValueChangeEvent(EditableRecordsTableField.this));
	}

	private Consumer<RecordVODataProvider> createNewDataProviderAvailableCallback(Layout mainLayout) {

		return recordVOs -> {
			if (currentTable != null) {
				mainLayout.removeComponent(currentTable);
			}

			currentTable = buildTable(recordVOs);
			mainLayout.addComponent(currentTable);

			tableRecorsdUpdatedObservable.fire(new TableRecorsdUpdatedArgs(this, getRecordVOS(), UpdateType.LOADED));
		};
	}

	public boolean isRecordEditable() {
		return true;
	}

	public void editThisRecord(RecordVO record, Consumer<RecordVO> recordEditedCallback) {
	}


	public boolean isRecordDeletable() {
		return true;
	}

	public void thisRecordWillBeDeleted(RecordVO record, Consumer<RecordVO> deleteCallback) {
		deleteCallback.accept(record);
	}

	public String getAddButtonCaption() {
		return $("add");
	}

	public boolean isRecordAddable() {
		return true;
	}

	public void createNewRecord(Consumer<RecordVO> newRecordCreatedCallback) {
	}

	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	public AppLayerFactory getAppLayerFactory() {
		return ConstellioFactories.getInstanceIfAlreadyStarted().getAppLayerFactory();
	}

	public String getTableHeight() {
		return tableHeight;
	}

	public void setTableHeight(String tableHeight) {
		this.tableHeight = tableHeight;
	}

	public boolean isResizedIfRowsDoesNotFillHeight() {
		return isResizedIfRowsDoesNotFillHeight;
	}

	public void setResizedIfRowsDoesNotFillHeight(boolean resizedIfRowsDoesNotFillHeight) {
		isResizedIfRowsDoesNotFillHeight = resizedIfRowsDoesNotFillHeight;
	}

	public List<RecordVO> getRecordVOS() {
		return container != null ? container.getRecordsVO(Arrays.asList(container.getItemIds().toArray())) : new ArrayList<>();
	}

	public void addTableRecorsdUpdatedListener(TableRecorsdUpdatedListener listener) {
		tableRecorsdUpdatedObservable.addListener(listener);
	}

	public void removeTableRecorsdUpdatedListener(TableRecorsdUpdatedListener listener) {
		tableRecorsdUpdatedObservable.removeListener(listener);
	}

	@Override
	protected void setInternalValue(List<String> newValue) {
		setInternalValue(newValue, false);
	}

	private void setInternalValue(List<String> newValue, boolean internalSet) {
		if (!internalSet) {
			presenter.setUseTheseRecordIdsInstead(newValue);

			if (mainLayout != null) {
				presenter.getRecordVOS(createNewDataProviderAvailableCallback(mainLayout));
			}
		}

		super.setInternalValue(newValue);
	}

	private void resizeHeightBasedOnRowCount(RecordVOTable table, int rowCount) {
		if (isResizedIfRowsDoesNotFillHeight() && rowCount <= TABLE_ROW_COUNT_BEFORE_SCROLLBAR_REQUIRED) {
			table.setHeight(rowCount * TABLE_ROW_AVERAGE_HEIGHT + TABLE_HEADER_HEIGHT + "px");
		} else {
			table.setHeight(getTableHeight());
		}
	}

	@Override
	protected List<String> getInternalValue() {
		return getRecordVOS().stream().map(RecordVO::getId).collect(Collectors.toList());
	}

	@Override
	public Class getType() {
		return List.class;
	}

	@Override
	protected Component initContent() {
		return buildMainComponent();
	}

	public EditableRecordsTablePresenter getPresenter() {
		return presenter;
	}

	@Override
	public boolean isDirty() {
		return presenter.isDirty();
	}

	@Override
	public void addComponentDirtiedListener(DirtiedListener listener) {
		presenter.addComponentDirtiedListener(listener);
	}

	@Override
	public void removeComponentDirtiedListener(DirtiedListener listener) {
		presenter.removeComponentDirtiedListener(listener);
	}

	public static class TableRecorsdUpdatedArgs extends EventArgs<EditableRecordsTableField> {
		public enum UpdateType {
			LOADED, ADDED, UPDATED, DELETED
		}

		private final List<RecordVO> recordVOS;
		private final RecordVO recordVO;
		private final int size;
		private final UpdateType updateType;

		public TableRecorsdUpdatedArgs(EditableRecordsTableField sender,
									   RecordVO recordVO,
									   UpdateType updateType) {
			this(sender, Collections.singletonList(recordVO), updateType);
		}

		public TableRecorsdUpdatedArgs(EditableRecordsTableField sender,
									   List<RecordVO> recordVOS,
									   UpdateType updateType) {
			super(sender);
			this.recordVOS = recordVOS;
			this.updateType = updateType;

			if (recordVOS != null && !recordVOS.isEmpty()) {
				recordVO = recordVOS.get(0);
				size = recordVOS.size();
			} else {
				recordVO = null;
				size = 0;
			}
		}

		public List<RecordVO> getRecordVOS() {
			return recordVOS;
		}

		public RecordVO getRecordVO() {
			return recordVO;
		}

		public int size() {
			return size;
		}

		public UpdateType getUpdateType() {
			return updateType;
		}

		public interface TableRecorsdUpdatedListener extends EventListener<TableRecorsdUpdatedArgs> {
		}

		public static class TableRecorsdUpdatedObservable extends EventObservable<TableRecorsdUpdatedArgs> {
		}
	}
}
