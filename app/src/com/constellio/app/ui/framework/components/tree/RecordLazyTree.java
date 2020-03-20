package com.constellio.app.ui.framework.components.tree;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.table.TablePropertyCache.CellKey;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.framework.data.TreeDataProviderFactory;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.vaadin.data.Item;
import com.vaadin.server.Resource;

public class RecordLazyTree extends LazyTree<String> {

	private RecordIdToCaptionConverter itemsConverter;

	public RecordLazyTree(String taxonomyCode, int bufferSize) {
		super(TreeDataProviderFactory.forTaxonomy(taxonomyCode, ConstellioUI.getCurrentSessionContext().getCurrentCollection()), bufferSize);
		init();
	}

	public RecordLazyTree(String taxonomyCode) {
		super(TreeDataProviderFactory.forTaxonomy(taxonomyCode, ConstellioUI.getCurrentSessionContext().getCurrentCollection()));
		init();
	}

	public RecordLazyTree(LazyTreeDataProvider<String> dataProvider, int bufferSize) {
		this(dataProvider, bufferSize, false);
	}

	public RecordLazyTree(LazyTreeDataProvider<String> dataProvider, int bufferSize, boolean multiValue) {
		super(dataProvider, bufferSize, multiValue);
		init();
	}

	public RecordLazyTree(LazyTreeDataProvider<String> dataProvider) {
		super(dataProvider);
		init();
	}

	private void init() {
		itemsConverter = new RecordIdToCaptionConverter();
	}

	@Override
	public String getItemCaption(String id) {
		return getDataProvider().getCaption(id);
	}

	@Override
	public Resource getItemIcon(Object itemId) {
		boolean expanded = isExpanded(itemId);
		return getDataProvider().getIcon((String) itemId, expanded);
	}

	@Override
	public Class<String> getType() {
		return String.class;
	}

	@Override
	protected CellKey getCellKey(Object itemId, Object propertyId) {
		RecordVO recordVO;
		Item item = getItem(itemId);
		if (item instanceof RecordVOItem) {
			RecordVOItem recordVOItem = (RecordVOItem) item;
			recordVO = recordVOItem.getRecord();
		} else {
			recordVO = null;
		}

		CellKey cellKey;
		if (recordVO != null) {
			String recordId = recordVO.getId();
			if (propertyId instanceof MetadataVO) {
				MetadataVO metadataVO = (MetadataVO) propertyId;
				cellKey = new CellKey(recordId, metadataVO.getCode());
			} else {
				cellKey = new CellKey(recordId, propertyId);
			}
		} else {
			cellKey = null;
		}
		return cellKey;
	}

}
