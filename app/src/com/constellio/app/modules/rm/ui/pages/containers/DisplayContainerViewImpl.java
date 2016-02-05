package com.constellio.app.modules.rm.ui.pages.containers;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Arrays;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.reports.factories.labels.LabelsReportFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.LabelsButton.RecordSelector;
import com.constellio.app.ui.framework.buttons.ReportButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class DisplayContainerViewImpl extends BaseViewImpl implements DisplayContainerView, RecordSelector {
	private final DisplayContainerPresenter presenter;

	public DisplayContainerViewImpl() {
		presenter = new DisplayContainerPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forContainerId(event.getParameters());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		layout.setSpacing(true);

		RecordVO container = presenter.getContainer();
		layout.addComponent(new RecordDisplay(container));

		try {
			Double fillRatio = presenter.getFillRatio(container);
			layout.addComponent(new ContainerRatioPanel(fillRatio));
		} catch (ContainerWithoutCapacityException e) {
			layout.addComponent(new ContainerRatioPanel($("ContainerWithoutCapacityException")));
		} catch (RecordInContainerWithoutLinearMeasure e) {
			layout.addComponent(new ContainerRatioPanel($("RecordInContainerWithoutLinearMeasure")));
		}

		layout.addComponent(buildFoldersTable(presenter.getFolders()));

		return layout;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	private Component buildFoldersTable(final RecordVODataProvider provider) {
		RecordVOLazyContainer folders = new RecordVOLazyContainer(provider);
		ButtonsContainer<RecordVOLazyContainer> container = new ButtonsContainer<>(folders, "buttons");
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						RecordVO entity = provider.getRecordVO(index);
						presenter.displayFolderButtonClicked(entity);
					}
				};
			}
		});

		RecordVOTable table = new RecordVOTable($("DisplayContainerView.foldersTableTitle"), container);
		table.setWidth("100%");
		table.setColumnHeader("buttons", "");
		table.setPageLength(provider.size());

		return table;
	}

	@Override
	protected String getTitle() {
		return $("DisplayContainerView.viewTitle");
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);

		Button edit = new EditButton($("DisplayContainerView.edit")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editContainer();
			}
		};
		buttons.add(edit);

		Button slip = new ReportButton("Reports.ContainerRecordReport", presenter);
		slip.setCaption($("DisplayContainerView.slip"));
		slip.setStyleName(ValoTheme.BUTTON_LINK);
		slip.setEnabled(presenter.canPrintReports());
		buttons.add(slip);

		Button labels = new ContainerLabelsButton($("SearchView.labels"), $("SearchView.printLabels"), this,
				presenter.getTemplates());
		labels.setEnabled(presenter.canPrintReports());
		buttons.add(labels);

		Button empty = new ConfirmDialogButton($("DisplayContainerView.empty")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("DisplayContainerView.confirmEmpty");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.emptyButtonClicked();
			}
		};
		ComponentState state = presenter.getEmptyButtonState();
		empty.setVisible(state.isVisible());
		empty.setEnabled(state.isEnabled());
		buttons.add(empty);

		return buttons;
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
				labelConfiguration.setItemCaption(labelTemplate, $(labelTemplate.getName()));
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
