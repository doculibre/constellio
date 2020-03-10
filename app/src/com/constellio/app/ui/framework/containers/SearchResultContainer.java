package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResultContainer extends ContainerAdapter<SearchResultVOLazyContainer> implements RecordVOContainer {

	private RecordDisplayFactory displayFactory;
	String query;

	public SearchResultContainer(SearchResultVOLazyContainer adapted, RecordDisplayFactory displayFactory,
								 String query, boolean indexProperty) {
		super(adapted, indexProperty);
		this.displayFactory = displayFactory;
		this.query = query;
	}

	public SearchResultVODataProvider getDataProvider() {
		return adapted.getDataProvider();
	}

	private Property<Component> newSearchResultProperty(final Object itemId) {
		return new AbstractProperty<Component>() {
			@Override
			public Component getValue() {
				Integer index = (Integer) itemId;
				SearchResultVO searchResultVO = getSearchResultVO(index);
				ClickListener clickListener = getClickListener(searchResultVO, index);
				ClickListener elevationClickListener = getElevationClickListener(searchResultVO, index);
				ClickListener exclusionClickListener = getExclusionClickListener(searchResultVO, index);
				SearchResultDisplay searchResultDisplay = displayFactory.build(searchResultVO, query, clickListener, elevationClickListener, exclusionClickListener);
				ReferenceDisplay referenceDisplay = ComponentTreeUtils.getFirstChild(searchResultDisplay, ReferenceDisplay.class);
				if (referenceDisplay != null) {
					referenceDisplay.setIcon(null);
				}
				return searchResultDisplay;
			}

			@Override
			public void setValue(Component newValue)
					throws ReadOnlyException {
				throw new ReadOnlyException();
			}

			@Override
			public Class<Component> getType() {
				return Component.class;
			}
		};
	}

	protected ClickListener getClickListener(SearchResultVO searchResultVO, Integer index) {
		return null;
	}

	protected ClickListener getElevationClickListener(SearchResultVO searchResultVO, Integer index) {
		return null;
	}

	protected ClickListener getExclusionClickListener(SearchResultVO searchResultVO, Integer index) {
		return null;
	}

	@Override
	public RecordVO getRecordVO(Object itemId) {
		return adapted.getRecordVO(itemId);
	}

	public SearchResultVO getSearchResultVO(int itemId) {
		return adapted.getSearchResultVO(itemId);
	}

	public double getLastCallQTime() {
		return adapted.getLastCallQTime();
	}

	@Override
	public List<MetadataSchemaVO> getSchemas() {
		return adapted.getSchemas();
	}

	@Override
	public Map<String, List<String>> getHighlights(Object itemId) {
		return new HashMap<>();
	}

}
