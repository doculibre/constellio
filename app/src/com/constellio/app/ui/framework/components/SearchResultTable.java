package com.constellio.app.ui.framework.components;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Component;

import java.util.List;

public interface SearchResultTable extends Component {

	String SEARCH_RESULT_TABLE_STYLE = "search-result-table";

	List<String> getSelectedRecordIds();

	List<String> getUnselectedRecordIds();

	Component createSummary(List<Component> actions, Component... zipButton);

	Component createSummary(Component component, Component... extra);

	Component createSummary(List<Component> alwaysActive, final List<Component> extra);
	void addItemClickListener(final ItemClickListener listener);
}
