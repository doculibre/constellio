package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.framework.buttons.LabelsButton.RecordSelector;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class ContainersButton extends WindowButton {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContainersButton.class);
	private final ContainersAssigner selector;

	private LookupRecordField containerLookup;

	public ContainersButton(ContainersAssigner selector) {
		this($("ContainersButton.container"), $("ContainersButton.containerAssigner"), selector);
	}

	public ContainersButton(String caption, String windowCaption, ContainersAssigner selector) {
		super(caption, windowCaption, WindowConfiguration.modalDialog("45%", "25%"));
		this.selector = selector;
	}

	@Override
	protected Component buildWindowContent() {
		VerticalLayout formLayout = new VerticalLayout();
		formLayout.setSpacing(true);
		containerLookup = new LookupRecordField(ContainerRecord.SCHEMA_TYPE);
		containerLookup.setCaption($("ContainersButton.containerTitle"));
		containerLookup.setRequired(true);
		HorizontalLayout containerLayout = new HorizontalLayout();
		containerLayout.addComponent(containerLookup);
		containerLayout.setComponentAlignment(containerLookup, Alignment.MIDDLE_CENTER);
		formLayout.addComponent(containerLayout);

		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.addStyleName(BaseForm.BUTTONS_LAYOUT);
		buttonsLayout.setSpacing(true);

		Button saveButton = new Button($("ContainersButton.putInContent"));
		saveButton.addStyleName(BaseForm.SAVE_BUTTON);
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		saveButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				try{
					if(StringUtils.isNotBlank(containerLookup.getValue())){
						selector.putRecordsInContainer(selector.getSelectedRecordIds(), containerLookup.getValue());
						getWindow().close();
					}
				}catch(Throwable e){
					showErrorMessage(e);
				}

			}
		});

		Button cancelButton = new Button($("cancel"));
		cancelButton.addStyleName(BaseForm.CANCEL_BUTTON);
		cancelButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				getWindow().close();
			}
		});
		formLayout.addComponent(buttonsLayout);
		buttonsLayout.addComponents(saveButton, cancelButton);
		return formLayout;
	}

	private void showErrorMessage(Throwable e) {
		Notification notification = new Notification(e.getMessage() + "<br/><br/>" + $("clickToClose"), Type.WARNING_MESSAGE);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
		LOGGER.warn(e.getMessage(), e);
	}

	public static interface ContainersAssigner extends Serializable, RecordSelector {
		List<String> getSelectedRecordIds();
		void putRecordsInContainer(List<String> recordsIds, String containerId);
	}

}
