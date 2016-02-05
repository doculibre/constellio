package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Date;

import org.joda.time.LocalDateTime;

import com.constellio.app.ui.framework.components.fields.date.BaseDateField;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ValoTheme;

public abstract class DateRangePanel extends Panel{
	public static final String OK_BUTTON = "seleniumOkButton";
	private DateField startDateField;
	private DateField endDateField;

	public DateRangePanel(Date startDate, Date endDate) {
		HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.setSizeFull();
		String startDateCaption = $("from_du");
		Date startDateValue;
		if (startDate != null){
			startDateValue = startDate;
		}else{
			startDateValue = new LocalDateTime().minusWeeks(1).toDate();
		}
		startDateField = new BaseDateField(startDateCaption, startDateValue);
		hLayout.addComponent(startDateField);
		Label separator1 = new Label("&nbsp;", ContentMode.HTML);
		hLayout.addComponent(separator1);

		Date endDateValue;
		if (endDate != null){
			endDateValue = endDate;
		}else{
			endDateValue = new LocalDateTime().toDate();
		}

		String endDateCaption = $("to_au");
		endDateField = new BaseDateField(endDateCaption, endDateValue);
		hLayout.addComponent(endDateField);
		Label separator2 = new Label("&nbsp;", ContentMode.HTML);
		hLayout.addComponent(separator2);

		Button okButton = new Button($("ListEventsView.dateRange.generate"));
		okButton.addStyleName(OK_BUTTON);
		okButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		okButton.addClickListener((new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				okButtonClick();
			}
		}));
		hLayout.addComponent(okButton);
		hLayout.setComponentAlignment(okButton, Alignment.BOTTOM_RIGHT);

		setContent(hLayout);
		addStyleName(ValoTheme.PANEL_BORDERLESS);
	}

	public Date getEventStartDate() {
		return startDateField.getValue();
	}

	public Date getEventEndDate() {
		return endDateField.getValue();
	}

	protected abstract void okButtonClick();
}
