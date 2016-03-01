package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.reports.factories.labels.LabelsReportFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.data.utils.Factory;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

public class LabelsButton extends WindowButton {
	private final RecordSelector selector;

	private final Factory<List<LabelTemplate>> templatesFactory;

	@PropertyId("startPosition") private ComboBox startPosition;
	@PropertyId("labelConfigurations") private ComboBox format;
	@PropertyId("numberOfCopies") private TextField copies;

	public LabelsButton(String caption, String windowCaption, RecordSelector selector,
			Factory<List<LabelTemplate>> templatesFactory) {
		super(caption, windowCaption, WindowConfiguration.modalDialog("75%", "75%"));
		this.selector = selector;
		this.templatesFactory = templatesFactory;
	}

	@Override
	protected Component buildWindowContent() {
		startPosition = new ComboBox($("LabelsButton.startPosition"));
		for (int i = 1; i <= 10; i++) {
			startPosition.addItem(i);
		}
		startPosition.setNullSelectionAllowed(false);

		List<LabelTemplate> configurations = templatesFactory.get();
		format = new ComboBox($("LabelsButton.labelFormat"));
		for (LabelTemplate configuration : configurations) {
			format.addItem(configuration);
			format.setItemCaption(configuration, $(configuration.getName()));
		}
		if(configurations.size() > 0) {
			format.select(configurations.get(0));
		}
		format.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
		format.setNullSelectionAllowed(false);

		copies = new TextField($("LabelsButton.numberOfCopies"));
		copies.setConverter(Integer.class);

		return new BaseForm<LabelParametersVO>(
				new LabelParametersVO(new LabelTemplate()), this, startPosition, format, copies) {
			@Override
			protected void saveButtonClick(LabelParametersVO parameters)
					throws ValidationException {
				LabelTemplate labelTemplate = format.getValue() != null ? (LabelTemplate) format.getValue() : new LabelTemplate();
				LabelsReportFactory factory = new LabelsReportFactory(
						selector.getSelectedRecordIds(), labelTemplate,
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
