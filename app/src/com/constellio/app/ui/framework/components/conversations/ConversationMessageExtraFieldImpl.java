package com.constellio.app.ui.framework.components.conversations;

import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConversationMessageExtraFieldImpl extends CustomComponent {

	public static final String CSS_ROOT = "conversation-message-extra-field";
	public static final String CSS_TITLE = CSS_ROOT + "-title";

	private final String title;

	private final ConversationMessageExtraFieldPresenter presenter;

	private final Function<RecordVO, Component> buildRecordComponent;

	public ConversationMessageExtraFieldImpl(String title, Supplier<List<String>> getRecordIds,
											 Function<RecordVO, Component> buildRecordComponent) {
		this.title = title;
		this.buildRecordComponent = buildRecordComponent;

		presenter = new ConversationMessageExtraFieldPresenter(getRecordIds);
	}

	@Override
	public void attach() {
		super.attach();

		setCompositionRoot(buildComponent());
	}

	protected Component buildComponent() {
		Layout layout = new VerticalLayout();
		layout.addStyleName(CSS_ROOT);

		layout.addComponents(
				buildTitle(),
				buildFieldList());

		return layout;
	}

	private Component buildTitle() {
		Label label = new Label(title);
		label.addStyleName(CSS_TITLE);

		return label;
	}

	private Component buildFieldList() {
		Layout layout = new VerticalLayout();

		layout.addComponents(presenter.getLinkedRecordVOs().stream().map(buildRecordComponent).toArray(Component[]::new));

		return layout;
	}
}
