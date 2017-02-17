package com.constellio.app.ui.framework.components.tree;

import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.vaadin.server.Resource;

public class RecordLazyTree extends LazyTree<String> {

	private RecordIdToCaptionConverter itemsConverter;

	public RecordLazyTree(String taxonomyCode, int bufferSize) {
		super(new RecordLazyTreeDataProvider(taxonomyCode), bufferSize);
		init();
	}

	public RecordLazyTree(String taxonomyCode) {
		super(new RecordLazyTreeDataProvider(taxonomyCode));
		init();
	}

	public RecordLazyTree(LazyTreeDataProvider<String> dataProvider, int bufferSize) {
		super(dataProvider, bufferSize);
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

}
