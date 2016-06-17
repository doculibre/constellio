package com.constellio.app.ui.framework.components;

import java.util.List;

import com.vaadin.ui.Component;

public interface SearchResultTable extends Component {

	List<String> getSelectedRecordIds();

	Component createSummary(List<Component> actions, Component... zipButton);

	Component createSummary(Component component, Component... extra);

	Component createSummary(List<Component> alwaysActive, final List<Component> extra);
}
