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
package com.constellio.app.modules.rm.ui.pages.containers;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.reports.factories.labels.LabelsReportFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.LabelsButton.RecordSelector;
import com.constellio.app.ui.framework.buttons.ReportButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class DisplayContainerViewImpl extends BaseViewImpl implements DisplayContainerView, RecordSelector {

	private DisplayContainerPresenter presenter;

	public DisplayContainerViewImpl() {
		presenter = new DisplayContainerPresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();

		layout.addComponent(new RecordDisplay(presenter.getContainer(event.getParameters())));

		final String containerId = event.getParameters();

		presenter.setContainerId(containerId);

		layout.addComponent(buildFoldersTable(event.getParameters()));

		return layout;
	}

	private Component buildFoldersTable(final String containerId) {
		RecordVOLazyContainer recordVOLazyContainer = new RecordVOLazyContainer(presenter.getFoldersDataProvider(containerId));
		ButtonsContainer buttonsContainer = new ButtonsContainer(recordVOLazyContainer, "buttons");
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						RecordVO entity = presenter.getFoldersDataProvider(containerId).getRecordVO(index);
						presenter.displayFolderButtonClicked(entity);
					}
				};
			}
		});

		RecordVOTable table = new RecordVOTable($("DisplayContainerView.foldersTableTitle"), buttonsContainer);
		table.setWidth("100%");
		table.setColumnHeader("buttons", "");
		//		table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 120);
		table.setPageLength(table.getItemIds().size());

		return table;
	}

	@Override
	protected String getTitle() {
		return $("DisplayContainerView.viewTitle");
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();

		ReportButton printBordereau = new ReportButton("Reports.ContainerRecordReport", presenter);
		printBordereau.setCaption($("DisplayContainerView.slip"));
		printBordereau.setStyleName(ValoTheme.BUTTON_LINK);
		printBordereau.setEnabled(presenter.isPrintReportEnable());

		ContainerLabelsButton labelsButton = new ContainerLabelsButton($("SearchView.labels"), $("SearchView.printLabels"), this,
				presenter.getTemplates());
		labelsButton.setEnabled(presenter.isPrintReportEnable());

		actionMenuButtons.add(printBordereau);
		actionMenuButtons.add(labelsButton);
		return actionMenuButtons;
	}

	@Override
	public List<String> getSelectedRecordIds() {
		return Arrays.asList(presenter.getContainerId());
	}

	// TODO: Quick hack to make printing container labels work...
	public static class ContainerLabelsButton extends WindowButton {
		private final RecordSelector selector;
		private final List<LabelTemplate> labelTemplates;

		@PropertyId("startPosition") private ComboBox startPosition;
		@PropertyId("numberOfCopies") private TextField copies;
		@PropertyId("labelConfiguration") private ComboBox labelConfiguration;

		public ContainerLabelsButton(String caption, String windowCaption, RecordSelector selector,
				List<LabelTemplate> labelTemplates) {
			super(caption, windowCaption, WindowConfiguration.modalDialog("75%", "75%"));
			this.selector = selector;
			this.labelTemplates = labelTemplates;
		}

		@Override
		protected Component buildWindowContent() {
			startPosition = new ComboBox($("LabelsButton.startPosition"));
			for (int i = 1; i <= 10; i++) {
				startPosition.addItem(i);
			}
			startPosition.setNullSelectionAllowed(false);

			labelConfiguration = new ComboBox($("LabelsButton.labelFormat"));
			for (LabelTemplate labelTemplate : labelTemplates) {
				labelConfiguration.addItem(labelTemplate);
				labelConfiguration.setItemCaption(labelTemplate, $("LabelsButton.labelFormat." + labelTemplate.getKey()));
			}
			labelConfiguration.setNullSelectionAllowed(false);

			copies = new TextField($("LabelsButton.numberOfCopies"));
			copies.setConverter(Integer.class);

			return new BaseForm<LabelParametersVO>(
					new LabelParametersVO(labelTemplates.get(0)), this, labelConfiguration, startPosition,
					copies) {
				@Override
				protected void saveButtonClick(LabelParametersVO parameters)
						throws ValidationException {
					LabelsReportFactory factory = new LabelsReportFactory(
							selector.getSelectedRecordIds(), ((LabelTemplate) (labelConfiguration.getValue())),
							parameters.getStartPosition(), parameters.getNumberOfCopies());
					getWindow().setContent(new ReportViewer(factory));
				}

				@Override
				protected void cancelButtonClick(LabelParametersVO parameters) {
					getWindow().close();
				}
			};
		}
	}
}
