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
package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.modules.rm.reports.factories.labels.LabelConfiguration;
import com.constellio.app.modules.rm.reports.factories.labels.LabelsReportFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

public class LabelsButton extends WindowButton {
	private final RecordSelector selector;

	@PropertyId("startPosition") private ComboBox startPosition;
	@PropertyId("labelConfigurations") private ComboBox format;
	@PropertyId("numberOfCopies") private TextField copies;

	public LabelsButton(String caption, String windowCaption, RecordSelector selector) {
		super(caption, windowCaption, WindowConfiguration.modalDialog("75%", "75%"));
		this.selector = selector;
	}

	@Override
	protected Component buildWindowContent() {
		startPosition = new ComboBox($("LabelsButton.startPosition"));
		for (int i = 1; i <= 10; i++) {
			startPosition.addItem(i);
		}
		startPosition.setNullSelectionAllowed(false);

		List<LabelConfiguration> configurations = LabelConfiguration.getSupportedConfigurations();
		format = new ComboBox($("LabelsButton.labelFormat"));
		for (LabelConfiguration configuration : configurations) {
			format.addItem(configuration);
			format.setItemCaption(configuration, $("LabelsButton.labelFormat." + configuration.getKey()));
		}
		format.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
		format.setNullSelectionAllowed(false);

		copies = new TextField($("LabelsButton.numberOfCopies"));
		copies.setConverter(Integer.class);

		return new BaseForm<LabelParametersVO>(
				new LabelParametersVO(configurations.get(0)), this, startPosition, format, copies) {
			@Override
			protected void saveButtonClick(LabelParametersVO parameters)
					throws ValidationException {
				LabelsReportFactory factory = new LabelsReportFactory(
						selector.getSelectedRecordIds(), parameters.getLabelConfiguration(),
						parameters.getStartPosition(), parameters.getNumberOfCopies());
				getWindow().setContent(new ReportViewer(factory));
			}

			@Override
			protected void cancelButtonClick(LabelParametersVO parameters) {
				getWindow().close();
			}
		};
	}

	public static interface RecordSelector extends Serializable {
		List<String> getSelectedRecordIds();
	}
}
