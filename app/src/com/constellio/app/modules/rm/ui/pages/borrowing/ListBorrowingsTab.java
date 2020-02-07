package com.constellio.app.modules.rm.ui.pages.borrowing;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToStringConverter;
import com.constellio.app.ui.framework.components.converters.JodaDateToStringConverter;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionChangeEvent;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionManager;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
import com.constellio.app.ui.framework.containers.RecordVOContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.Table.ColumnGenerator;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public abstract class ListBorrowingsTab {
	protected AppLayerFactory appLayerFactory;
	protected SessionContext sessionContext;
	protected RMSchemasRecordsServices recordsServices;
	protected JodaDateTimeToStringConverter jodaDateTimeConverter;
	protected JodaDateToStringConverter jodaDateConverter;

	private HorizontalLayout layout;

	public ListBorrowingsTab(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		this.appLayerFactory = appLayerFactory;
		this.sessionContext = sessionContext;
		recordsServices = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(), appLayerFactory);
		jodaDateTimeConverter = new JodaDateTimeToStringConverter();
		jodaDateConverter = new JodaDateToStringConverter();

		layout = new HorizontalLayout();
		layout.setSizeFull();
	}

	public Component getLayout() {
		return layout;
	}

	public String getCaption() {
		Language currentLanguage = Language.withCode(sessionContext.getCurrentLocale().getLanguage());
		return getSchemaType().getLabel(currentLanguage);
	}

	public void refresh(Component selectedLayout, String administrativeUnit, boolean showOnlyOverdue) {
		layout.removeAllComponents();

		if (selectedLayout.equals(layout)) {
			buildTable(administrativeUnit, showOnlyOverdue);
		}
	}

	private void buildTable(String administrativeUnit, boolean showOnlyOverdue) {
		final ViewableRecordVOTablePanel table = buildViewableRecordItemTable(getDataProvider(administrativeUnit, showOnlyOverdue));
		table.addStyleName("record-table");
		table.setSizeFull();
		table.setAllItemsVisible(true);
		layout.addComponent(table);
	}

	private RecordVODataProvider getDataProvider(String administrativeUnit, boolean showOnlyOverdue) {
		MetadataSchemaVO schema = new MetadataSchemaToVOBuilder().build(
				getSchemaType().getDefaultSchema(), VIEW_MODE.TABLE, sessionContext);
		return new RecordVODataProvider(schema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
			@Override
			public LogicalSearchQuery getQuery() {
				List<LogicalSearchCondition> conditions = new ArrayList<>();
				conditions.add(getCheckedOutCondition());
				if (StringUtils.isNotBlank(administrativeUnit)) {
					conditions.add(getAdministrativeUnitCondition(administrativeUnit));
				}
				if (showOnlyOverdue) {
					conditions.add(getOverdueCondition());
				}
				return new LogicalSearchQuery(from(getSchemaType())
						.whereAllConditions(conditions))
						.sortDesc(Schemas.MODIFIED_ON);
			}
		};
	}

	protected abstract SelectionTable buildViewableRecordItemTable(RecordVODataProvider dataProvider);

	protected abstract MetadataSchemaType getSchemaType();

	protected abstract LogicalSearchCondition getCheckedOutCondition();

	protected abstract LogicalSearchCondition getAdministrativeUnitCondition(String administrativeUnit);

	protected abstract LogicalSearchCondition getOverdueCondition();

	protected abstract class ViewableRecordItemTable extends SelectionTable implements ColumnGenerator {
		protected static final String BORROWING_USER = "borrowingUser";
		protected static final String BORROWING_DATE = "borrowingDate";
		protected static final String BORROWING_DUE_DATE = "borrowingDueDate";

		protected ViewableRecordItemTable(RecordVODataProvider dataProvider) {
			super(new RecordVOLazyContainer(dataProvider) {
			});

			setSelectionActionButtons();
			setTableMode(TableMode.TABLE);
			setColumns();
		}

		private void setColumns() {
			getActualTable().addGeneratedColumn(BORROWING_USER, this);
			getActualTable().setColumnHeader(BORROWING_USER, $("ListBorrowingsView.borrower"));
			getActualTable().setColumnExpandRatio(BORROWING_USER, 0.3f);

			getActualTable().addGeneratedColumn(BORROWING_DATE, this);
			getActualTable().setColumnHeader(BORROWING_DATE, $("ListBorrowingsView.borrowDate"));
			getActualTable().setColumnExpandRatio(BORROWING_DATE, 0.3f);

			getActualTable().addGeneratedColumn(BORROWING_DUE_DATE, this);
			getActualTable().setColumnHeader(BORROWING_DUE_DATE, $("ListBorrowingsView.previewReturnDate"));
			getActualTable().setColumnExpandRatio(BORROWING_DUE_DATE, 0.3f);

			// TODO::JOLA --> Add action column

			getActualTable().setCellStyleGenerator(new ListBorrowingTableStyleGenerator(this, getActualTable().getCellStyleGenerator()));
		}

		protected String getBorrowingDueDateTitle() {
			return BORROWING_DUE_DATE;
		}

		protected abstract boolean isOverdue(RecordVO recordVO);
	}

	protected class ListBorrowingTableStyleGenerator implements CellStyleGenerator {
		private static final String OVERDUE_STYLE = "error";

		private ViewableRecordItemTable table;
		private CellStyleGenerator previousStyle;

		protected ListBorrowingTableStyleGenerator(ViewableRecordItemTable table, CellStyleGenerator previousStyle) {
			this.table = table;
			this.previousStyle = previousStyle;
		}

		@Override
		public String getStyle(Table source, Object itemId, Object propertyId) {
			if (isDueDateColumn(propertyId)) {
				RecordVO recordVO = ((RecordVOItem) source.getItem(itemId)).getRecord();
				if (table.isOverdue(recordVO)) {
					return OVERDUE_STYLE;
				}
			}

			return previousStyle.getStyle(source, itemId, propertyId);
		}

		private boolean isDueDateColumn(Object propertyId) {
			if (propertyId instanceof String && propertyId.equals(table.getBorrowingDueDateTitle())) {
				return true;
			}
			return false;
		}
	}

	protected class SelectionTable extends ViewableRecordVOTablePanel {
		protected Set<Object> selectedItemIds;

		protected SelectionTable(RecordVOContainer container) {
			super(container, TableMode.LIST, null, false);
			setAllItemsVisible(true);
		}

		private void initSelectedItemCache() {
			if (selectedItemIds == null) {
				selectedItemIds = new HashSet<>();
			}
		}

		@Override
		protected boolean isSelectColumn() {
			return true;
		}

		@Override
		protected SelectionManager newSelectionManager() {
			return new SelectionManager() {
				@Override
				public void selectionChanged(SelectionChangeEvent event) {
					initSelectedItemCache();

					List<Object> selectedItemIdsFromEvent = event.getSelectedItemIds();
					List<Object> deselectedItemIdsFromEvent = event.getDeselectedItemIds();

					if (deselectedItemIdsFromEvent != null && !deselectedItemIdsFromEvent.isEmpty()) {
						for (Object currentDeselectedItem : deselectedItemIdsFromEvent) {
							selectedItemIds.remove(currentDeselectedItem);
						}
					}

					if (selectedItemIdsFromEvent != null && !selectedItemIdsFromEvent.isEmpty()) {
						for (Object currentselectedItem : selectedItemIdsFromEvent) {
							selectedItemIds.add(currentselectedItem);
						}
					}


					boolean allItemsSelected = event.isAllItemsSelected();
					boolean allItemsDeselected = event.isAllItemsDeselected();
					if (allItemsSelected) {
						Collection<?> itemIds = getItemIds();

						selectedItemIds.addAll(itemIds);
					} else if (allItemsDeselected) {
						selectedItemIds.clear();
					}
				}

				@Override
				public List<Object> getAllSelectedItemIds() {
					initSelectedItemCache();
					return new ArrayList<>(selectedItemIds);
				}

				@Override
				public boolean isAllItemsSelected() {
					boolean allItemsSelected = true;
					for (Object itemId : getItemIds()) {
						if (!isSelected(itemId)) {
							allItemsSelected = false;
							break;
						}
					}
					return allItemsSelected;
				}

				@Override
				public boolean isAllItemsDeselected() {
					boolean allItemsDeselected = true;
					for (Object itemId : getItemIds()) {
						if (isSelected(itemId)) {
							allItemsDeselected = false;
							break;
						}
					}
					return allItemsDeselected;
				}

				@Override
				public boolean isSelected(Object itemId) {
					initSelectedItemCache();


					return selectedItemIds.contains(itemId);
				}

				private Collection<?> getItemIds() {
					return getActualTable().getItemIds();
				}
			};
		}
	}
}
