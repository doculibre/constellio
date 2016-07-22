package com.constellio.app.ui.framework.components.table.columns;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.ui.Table;

public class RecordVOTableColumnsManager extends TableColumnsManager {

	public RecordVOTableColumnsManager() {
	}

	public RecordVOTableColumnsManager(RecordVOTable table, String tableId) {
		super(table, tableId);
	}

	@Override
	protected List<String> getDefaultVisibleColumnIds(Table table) {
		List<String> defaultVisibleColumnIds;
		RecordVOTable recordVOTable = (RecordVOTable) table;
		MetadataSchemaVO schemaVO = recordVOTable.getSchema();
		if (schemaVO != null) {
			defaultVisibleColumnIds = new ArrayList<>();
			SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
			String collection = sessionContext.getCurrentCollection();
			String schemaCode = schemaVO.getCode();

			AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
			SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
			
			SchemaDisplayConfig schemaDisplayConfig = schemasDisplayManager.getSchema(collection, schemaCode);
			defaultVisibleColumnIds.addAll(schemaDisplayConfig.getTableMetadataCodes());
			
			Object[] tableVisibleColumns = table.getVisibleColumns();
			for (Object tableVisibleColumn : tableVisibleColumns) {
				if (!(tableVisibleColumn instanceof MetadataVO)) {
					String columnId = toColumnId(tableVisibleColumn);
					defaultVisibleColumnIds.add(columnId);
				}
			}
		} else {
			defaultVisibleColumnIds = super.getDefaultVisibleColumnIds(table);
		}
		return defaultVisibleColumnIds;
	}

	@Override
	protected String toColumnId(Object propertyId) {
		String columnId;
		if (propertyId instanceof MetadataVO) {
			columnId = ((MetadataVO) propertyId).getCode();
		} else {
			columnId = super.toColumnId(propertyId);
		}
		return columnId;
	}

}
