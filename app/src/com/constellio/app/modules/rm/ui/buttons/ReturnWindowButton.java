package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.modules.rm.services.menu.behaviors.RMRecordsMenuItemBehaviors;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.joda.time.LocalDate;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ReturnWindowButton extends WindowButton {

	private RMRecordsMenuItemBehaviors recordsMenuItemBehaviors;

	private List<Record> records;
	private MenuItemActionBehaviorParams params;
	private boolean isFolder;

	public ReturnWindowButton(AppLayerFactory appLayerFactory, String collection, List<Record> records,
							  MenuItemActionBehaviorParams params, boolean isFolder) {
		super($(isFolder ? "DisplayFolderView.returnFolder" : "DisplayContainerView.checkIn"),
				$(isFolder ? "DisplayFolderView.returnFolder" : "DisplayContainerView.checkIn"));

		recordsMenuItemBehaviors = new RMRecordsMenuItemBehaviors(collection, appLayerFactory);

		this.records = records;
		this.params = params;
		this.isFolder = isFolder;
	}

	@Override
	protected Component buildWindowContent() {

		final JodaDateField returnDatefield = new JodaDateField();
		returnDatefield.setCaption($("DisplayFolderView.returnDate"));
		returnDatefield.setRequired(false);
		returnDatefield.setId("returnDate");
		returnDatefield.addStyleName("returnDate");
		returnDatefield.setValue(TimeProvider.getLocalDate().toDate());

		BaseButton returnFolderButton = new BaseButton(
				$(isFolder ? "DisplayFolderView.returnFolder" : "DisplayContainerView.checkIn")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				LocalDate returnLocalDate = null;
				if (returnDatefield.getValue() != null) {
					returnLocalDate = LocalDate.fromDateFields(returnDatefield.getValue());
				}
				if (recordsMenuItemBehaviors.returnRecords(records, returnLocalDate, params, isFolder)) {
					getWindow().close();
				}
			}
		};
		returnFolderButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		BaseButton cancelButton = new BaseButton($("cancel")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				getWindow().close();
			}
		};
		cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.addComponents(returnFolderButton, cancelButton);

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout
				.addComponents(returnDatefield, horizontalLayout);
		verticalLayout.setSpacing(true);

		return verticalLayout;
	}
}