package com.constellio.app.ui.framework.components;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import java.util.List;

public interface SearchResultTable extends Component {

	List<String> getSelectedRecordIds();

	Component createSummary(List<Component> actions, Component... zipButton);

	Component createSummary(Component component, Component... extra);

	Component createSummary(List<Component> alwaysActive, final List<Component> extra);
}
