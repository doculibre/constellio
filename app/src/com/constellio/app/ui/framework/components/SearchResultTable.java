package com.constellio.app.ui.framework.components;

import com.vaadin.ui.Component;

import java.util.List;

public interface SearchResultTable extends Component {

	List<String> getSelectedRecordIds();

	List<String> getUnselectedRecordIds();

	Component createSummary(List<Component> actions, Component... zipButton);

	Component createSummary(Component component, Component... extra);

	Component createSummary(List<Component> alwaysActive, final List<Component> extra);
	
}
