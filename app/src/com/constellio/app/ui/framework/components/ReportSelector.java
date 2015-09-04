/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

public class ReportSelector extends HorizontalLayout {
	private final ReportPresenter presenter;
	private AbstractSelect selector;
	private final Button button;

	public ReportSelector(ReportPresenter presenter) {
		this.presenter = presenter;
		button = buildActivationButton();
		selector = buildSelector();
		addComponents(selector, button);
	}

	@Override
	public void setEnabled(boolean enabled) {
		selector.setEnabled(enabled);
		button.setEnabled(enabled && selector.getValue() != null);
	}

	public String getSelectedReport() {
		return (String) selector.getValue();
	}

	private AbstractSelect buildSelector() {
		ComboBox comboBox = new ComboBox();
		List<String> supportedReports = presenter.getSupportedReports();
		if (supportedReports.isEmpty()) {
			setVisible(false);
		} else {
			for (String report : supportedReports) {
				comboBox.addItem(report);
				comboBox.setItemCaption(report, $(report));
			}
			comboBox.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
			comboBox.setNullSelectionAllowed(false);
			comboBox.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					button.setEnabled(true);
				}
			});
		}
		return comboBox;
	}

	private Button buildActivationButton() {
		return new WindowButton($("ReportSelector.go"), null, WindowConfiguration.modalDialog("75%", "75%")) {
			@Override
			protected Component buildWindowContent() {
				ReportBuilderFactory factory = presenter.getReport(getSelectedReport());
				return new ReportViewer(factory);
			}

			@Override
			protected String getWindowCaption() {
				return $(getSelectedReport());
			}
		};
	}

}
