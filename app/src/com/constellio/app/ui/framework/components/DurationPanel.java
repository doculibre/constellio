package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;

import static com.constellio.app.ui.i18n.i18n.$;

public class DurationPanel extends Panel {
	private final TextField daysComponent;
	private final TextField hoursComponent;
	private final TextField minComponent;

	public DurationPanel() {
		Layout horizontalLayout = new HorizontalLayout();
		setSizeFull();
		addStyleName(ValoTheme.PANEL_BORDERLESS);

		daysComponent = new BaseTextField($("days"));
		//daysComponent.addValidator(new IntegerRangeValidator($("com.vaadin.data.validator.IntegerRangeValidator_withMinValue"), 0, 100));
		horizontalLayout.addComponent(daysComponent);

		hoursComponent = new BaseTextField($("hours"));
		//hoursComponent.addValidator(new IntegerRangeValidator($("com.vaadin.data.validator.IntegerRangeValidator"), 0, 23));
		horizontalLayout.addComponent(hoursComponent);

		minComponent = new BaseTextField($("mns"));
		//minComponent.addValidator(new IntegerRangeValidator($("com.vaadin.data.validator.IntegerRangeValidator"), 0, 59));

		horizontalLayout.addComponent(minComponent);

		setContent(horizontalLayout);
	}

	public Duration getDuration() {
		Integer days = 0, hours = 0, mns = 0;
		if (StringUtils.isNotBlank(daysComponent.getValue())) {
			days = Integer.valueOf(daysComponent.getValue());
		}
		if (StringUtils.isNotBlank(hoursComponent.getValue())) {
			hours = Integer.valueOf(hoursComponent.getValue());
		}
		if (StringUtils.isNotBlank(minComponent.getValue())) {
			mns = Integer.valueOf(minComponent.getValue());
		}
		long durationInMilliSeconds = ((((days * 24) + hours) * 60) + mns) * 60 * 1000;
		return new Duration(durationInMilliSeconds);
	}

	public void setDuration(Duration duration) {
		long days = 0, hours = 0, mns = 0;

		if (duration != null) {
			days = duration.getStandardDays();
			hours = duration.getStandardHours() % 24;
			mns = duration.getStandardMinutes() % 60;
		}

		daysComponent.setValue(days + "");
		hoursComponent.setValue(hours + "");
		minComponent.setValue(mns + "");
	}
}