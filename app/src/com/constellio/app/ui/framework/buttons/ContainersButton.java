package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.reports.factories.labels.LabelsReportFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.framework.buttons.LabelsButton.RecordSelector;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

public class ContainersButton extends WindowButton {
	private final RecordSelector selector;
	private final Map<String, String> containersLabelsMappedById;

	@PropertyId("startPosition") private ComboBox startPosition;

	public ContainersButton(String caption, String windowCaption, RecordSelector selector,
			Map<String, String> containersLabelsMappedById) {
		super(caption, windowCaption, WindowConfiguration.modalDialog("75%", "75%"));
		this.selector = selector;
		this.containersLabelsMappedById = containersLabelsMappedById;
	}

	@Override
	protected Component buildWindowContent() {
		startPosition = new ComboBox($("LabelsButton.startPosition"), this.containersLabelsMappedById.values());
		startPosition.setNullSelectionAllowed(false);

		/*List<LabelTemplate> configurations = templatesFactory.get();
		format = new ComboBox($("LabelsButton.labelFormat"));
		for (LabelTemplate configuration : configurations) {
			format.addItem(configuration);
			format.setItemCaption(configuration, $(configuration.getName()));
		}
		if(configurations.size() > 0) {
			format.select(configurations.get(0));
		}*/

		return new BaseForm<LabelParametersVO>(
				new LabelParametersVO(new LabelTemplate()), this, startPosition) {
			@Override
			protected void saveButtonClick(LabelParametersVO parameters)
					throws ValidationException {
				/*LabelTemplate labelTemplate = format.getValue() != null ? (LabelTemplate) format.getValue() : new LabelTemplate();
				LabelsReportFactory factory = new LabelsReportFactory(
						selector.getSelectedRecordIds(), labelTemplate,
						parameters.getStartPosition(), parameters.getNumberOfCopies());
				getWindow().setContent(new ReportViewer(factory));*/
			}

			@Override
			protected void cancelButtonClick(LabelParametersVO parameters) {
				getWindow().close();
			}
		};
	}

}
