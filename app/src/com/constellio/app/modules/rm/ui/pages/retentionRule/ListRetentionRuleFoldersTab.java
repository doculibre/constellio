//package com.constellio.app.modules.rm.ui.pages.retentionRule;
//
//import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
//import com.constellio.app.services.factories.AppLayerFactory;
//import com.constellio.app.ui.entities.MetadataSchemaVO;
//import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
//import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
//import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
//import com.constellio.app.ui.framework.components.converters.JodaDateTimeToStringConverter;
//import com.constellio.app.ui.framework.components.converters.JodaDateToStringConverter;
//import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionChangeEvent;
//import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionManager;
//import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
//import com.constellio.app.ui.framework.containers.RecordVOContainer;
//import com.constellio.app.ui.framework.data.RecordVODataProvider;
//import com.constellio.app.ui.pages.base.SessionContext;
//import com.constellio.model.entities.Language;
//import com.constellio.model.entities.schemas.MetadataSchemaType;
//import com.constellio.model.entities.schemas.Schemas;
//import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
//import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
//import com.vaadin.shared.ui.MarginInfo;
//import com.vaadin.ui.Component;
//import com.vaadin.ui.HorizontalLayout;
//
//import javax.mail.Session;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Objects;
//import java.util.Set;
//
//import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
//
//public abstract class ListRetentionRuleFoldersTab {
//	protected AppLayerFactory appLayerFactory;
//	protected SessionContext sessionContext;
//	protected RMSchemasRecordsServices recordsServices;
//	protected JodaDateTimeToStringConverter jodaDateTimeToStringConverter;
//	protected JodaDateToStringConverter jodaDateToStringConverter;
//
//	private HorizontalLayout layout;
//
//	public ListRetentionRuleFoldersTab(AppLayerFactory appLayerFactory, SessionContext sessionContext){
//		this.appLayerFactory = appLayerFactory;
//		this.sessionContext = sessionContext;
//		recordsServices = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(), appLayerFactory);
//		jodaDateToStringConverter =  new JodaDateToStringConverter();
//		jodaDateTimeToStringConverter = new JodaDateTimeToStringConverter();
//
//		layout = new HorizontalLayout();
//		layout.setSizeFull();
////		layout.setMargin(new MarginInfo(true, true, false, true));
//	}
//
//	public Component getLayout(){
//		return layout;
//	}
//
//	public String getCaption(){
//		Language currentLanguage = Language.withCode(sessionContext.getCurrentLocale().getLanguage());
//		return getSchemaType().getLabel(currentLanguage);
//	}
//
//	public void refresh(Component selectedLayout, String retentionRuleCode){
//		layout.removeAllComponents();
//
//		if(selectedLayout.equals(layout)){
//			buildTable(retentionRuleCode);
//		}
//	}
//
//	private void buildTable(String retentionRuleCode){
//		final ViewableRecordVOTablePanel table = buildViewableRecordItemTable(getDataProvider(retentionRuleCode));
//		table.addStyleName("record-table");
//		table.setSizeFull();
//		table.setAllItemsVisible(true);
//		layout.addComponent(table);
//	}
//
//	private RecordVODataProvider getDataProvider(String retentionRule){
//		MetadataSchemaVO schema = new MetadataSchemaToVOBuilder().build(
//				getSchemaType().getDefaultSchema(), VIEW_MODE.TABLE, sessionContext
//		);
//
//		return new RecordVODataProvider(schema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
//			@Override
//			public LogicalSearchQuery getQuery() {
//				List<LogicalSearchCondition> conditions = new ArrayList<>();
//
//
//				return new LogicalSearchQuery(from(getSchemaType())
//						.whereAllConditions(conditions))
//						.sortDesc(Schemas.MODIFIED_ON);
//			}
//		};
//	}
//
//	protected abstract MetadataSchemaType getSchemaType();
//
//	protected abstract SelectionTable buildViewableRecordItemTable(RecordVODataProvider dataProvider);
//
//	protected class SelectionTable extends ViewableRecordVOTablePanel{
//		protected Set<Object> selectedItemIds;
//
//		protected SelectionTable(RecordVOContainer container){
//			super(container, TableMode.LIST, null, false);
//			setAllItemsVisible(true);
//		}
//
//		private void initSelectedItemCache(){
//			if(selectedItemIds == null){
//				selectedItemIds = new HashSet<>();
//			}
//		}
//
//		@Override
//		protected boolean isSelectColumn(){
//			return true;
//		}
//
//		@Override SelectionManager newSelectionManager(){
//			return new SelectionManager() {
//				@Override
//				public List<Object> getAllSelectedItemIds() {
//					return null;
//				}
//
//				@Override
//				public boolean isAllItemsSelected() {
//					return false;
//				}
//
//				@Override
//				public boolean isAllItemsDeselected() {
//					return false;
//				}
//
//				@Override
//				public boolean isSelected(Object itemId) {
//					return false;
//				}
//
//				@Override
//				public void selectionChanged(SelectionChangeEvent event) {
//					initSelectedItemCache();
//
//					List<Object> selectedItemsIdsFromEvent = event.getSelectedItemIds();
//					List<Object> deselectedItemsIdsFromEvent = event.getDeselectedItemIds();
//
//					if(deselectedItemsIdsFromEvent != null && !deselectedItemsIdsFromEvent.isEmpty()){
//						for(Object currentDeselectedItem : deselectedItemsIdsFromEvent){
////							selected
//						}
//					}
//
//				}
//			}
//		}
//	}
//}
