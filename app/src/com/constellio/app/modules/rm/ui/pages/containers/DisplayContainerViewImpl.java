package com.constellio.app.modules.rm.ui.pages.containers;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.tasks.ui.components.fields.StarredButton;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.ReportButton;
import com.constellio.app.ui.framework.buttons.report.LabelButtonV2;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.utils.Factory;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayContainerViewImpl extends BaseViewImpl implements DisplayContainerView {
	private final DisplayContainerPresenter presenter;
	private Label borrowedLabel;
	private boolean popup = false;

	public DisplayContainerViewImpl() {
		this(null, false);
	}

	public DisplayContainerViewImpl(RecordVO recordVO, boolean popup) {
		presenter = new DisplayContainerPresenter(this, recordVO, popup);
		this.popup = popup;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		if (event != null) {
			presenter.forContainerId(event.getParameters());
		}
	}

	public DisplayContainerPresenter getPresenter() {
		return this.presenter;
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		setBorrowedMessage(presenter.getBorrowMessageState(presenter.getContainer()));
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		final VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		layout.setSpacing(true);

		final RecordVO recordVO = presenter.getContainer();
		borrowedLabel = new Label();
		borrowedLabel.setVisible(false);
		borrowedLabel.addStyleName(ValoTheme.LABEL_COLORED);
		borrowedLabel.addStyleName(ValoTheme.LABEL_BOLD);
		MetadataDisplayFactory metadataDisplayFactory = new MetadataDisplayFactory() {
			@Override
			public Component build(RecordVO recordVO, MetadataValueVO metadataValue) {
				if (metadataValue.getMetadata().getLocalCode().equals(ContainerRecord.FILL_RATIO_ENTRED)) {
					return buildSingleValue(recordVO, metadataValue.getMetadata(), metadataValue.getValue());
				} else {
					return super.build(recordVO, metadataValue);
				}
			}

			@Override
			public Component buildSingleValue(RecordVO recordVO, MetadataVO metadata, Object displayValue) {
				if (metadata.getLocalCode().equals(ContainerRecord.FILL_RATIO_ENTRED)) {
					try {
						Double fillRatio = presenter.getFillRatio(recordVO);
						return new Label(fillRatio.toString());
					} catch (ContainerWithoutCapacityException e) {
						return new Label($("ContainerWithoutCapacityException"));
					} catch (RecordInContainerWithoutLinearMeasure e) {
						return new Label($("RecordInContainerWithoutLinearMeasure"));
					}
				} else {
					return super.buildSingleValue(recordVO, metadata, displayValue);
				}
			}
		};
		layout.addComponents(borrowedLabel, new RecordDisplay(recordVO, metadataDisplayFactory) {
			@Override
			protected void addCaptionAndDisplayComponent(Label captionLabel, Component displayComponent) {
				super.addCaptionAndDisplayComponent(captionLabel, displayComponent);
			}
		});

		layout.addComponent(buildFoldersTable(presenter.getFolders()));

		return layout;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		if (!popup) {
			return new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					presenter.backButtonClicked();
				}
			};
		} else {
			return null;
		}
	}

	private Component buildFoldersTable(final RecordVODataProvider provider) {
		RecordVOLazyContainer folders = new RecordVOLazyContainer(provider);
		ButtonsContainer<RecordVOLazyContainer> container = new ButtonsContainer<>(folders, "buttons");
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
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
		if (popup) {
			table.setWidth("80%");
		} else {
			table.setWidth("100%");
		}

		table.setColumnHeader("buttons", "");

		return table;
	}

	@Override
	protected String getTitle() {
		return $("DisplayContainerView.viewTitle");
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);

		StarredButton favoriteStar = new StarredButton() {
			@Override
			public void addToDefaultFavorites() {
				presenter.addToDefaultFavorite();
			}

			@Override
			public void removeFromDefaultFavorites() {
				presenter.removeFromDefaultFavorites();
			}
		};
		favoriteStar.setStarred(presenter.containerInDefaultFavorites());
		buttons.add(favoriteStar);

		Button edit = new EditButton($("DisplayContainerView.edit")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editContainer();
			}
		};
		edit.setVisible(presenter.isEditButtonVisible());
		buttons.add(edit);

		Button slip = new ReportButton(new ReportWithCaptionVO("Reports.ContainerRecordReport", $("Reports.ContainerRecordReport")), presenter) {
			@Override
			protected Component buildWindowContent() {
				presenter.saveIfFirstTimeReportCreated();
				return super.buildWindowContent();
			}
		};
		slip.setCaption($("DisplayContainerView.slip"));
		slip.setStyleName(ValoTheme.BUTTON_LINK);
		slip.setEnabled(presenter.canPrintReports());
		buttons.add(slip);
		Factory<List<LabelTemplate>> customLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
			@Override
			public List<LabelTemplate> get() {
				return presenter.getCustomTemplates();
			}
		};
		Factory<List<LabelTemplate>> defaultLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
			@Override
			public List<LabelTemplate> get() {
				return presenter.getDefaultTemplates();
			}
		};
		Button labels = new LabelButtonV2($("SearchView.labels"), $("SearchView.printLabels"), customLabelTemplatesFactory,
				defaultLabelTemplatesFactory, getConstellioFactories().getAppLayerFactory(),
				getSessionContext().getCurrentCollection(), presenter.getContainer());
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

		Button delete = new DeleteButton($("DisplayContainerView.delete")) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.deleteButtonClicked();
			}
		};
		delete.setVisible(presenter.canDelete());
		delete.setEnabled(presenter.canDelete());
		buttons.add(delete);

		return buttons;
	}

	// TODO: Quick hack to make printing container labels work...
	//	public static class ContainerLabelsButton extends WindowButton {
	//		private final RecordSelector selector;
	//		private final List<LabelTemplate> labelTemplates;
	//		private NewReportWriterFactory<LabelsReportParameters> labelsReportFactory;
	//
	//		@PropertyId("startPosition") private ComboBox startPosition;
	//		@PropertyId("numberOfCopies") private TextField copies;
	//		@PropertyId("labelConfiguration") private ComboBox labelConfiguration;
	//
	//		public ContainerLabelsButton(String caption, String windowCaption, RecordSelector selector,
	//				List<LabelTemplate> labelTemplates, final NewReportWriterFactory<LabelsReportParameters> labelsReportFactory) {
	//			super(caption, windowCaption, WindowConfiguration.modalDialog("75%", "75%"));
	//			this.selector = selector;
	//			this.labelTemplates = labelTemplates;
	//			this.labelsReportFactory = labelsReportFactory;
	//		}
	//
	//		@Override
	//		protected Component buildWindowContent() {
	//			startPosition = new ComboBox($("LabelsButton.startPosition"));
	//			if (labelTemplates.size() > 0) {
	//				int size = labelTemplates.get(0).getLabelsReportLayout().getNumberOfLabelsPerPage();
	//				startPosition.clear();
	//				for (int i = 1; i <= size; i++) {
	//					startPosition.addItem(i);
	//				}
	//			}
	//			for (int i = 1; i <= 10; i++) {
	//				startPosition.addItem(i);
	//			}
	//			startPosition.setNullSelectionAllowed(false);
	//
	//			labelConfiguration = new ComboBox($("LabelsButton.labelFormat"));
	//			for (LabelTemplate labelTemplate : labelTemplates) {
	//				labelConfiguration.addItem(labelTemplate);
	//				labelConfiguration.setItemCaption(labelTemplate, $(labelTemplate.getName()));
	//			}
	//			labelConfiguration.setNullSelectionAllowed(false);
	//			labelConfiguration.setImmediate(true);
	//			labelConfiguration.addValueChangeListener(new ValueChangeListener() {
	//				@Override
	//				public void valueChange(ValueChangeEvent event) {
	//					LabelTemplate labelTemplate = (LabelTemplate) event.getProperty().getValue();
	//					int size = labelTemplate.getLabelsReportLayout().getNumberOfLabelsPerPage();
	//					startPosition.clear();
	//					startPosition.removeAllItems();
	//					for (int i = 1; i <= size; i++) {
	//
	//						startPosition.addItem(i);
	//					}
	//				}
	//			});
	//
	//			copies = new TextField($("LabelsButton.numberOfCopies"));
	//			copies.setConverter(Integer.class);
	//
	//			return new BaseForm<LabelParametersVO>(
	//					new LabelParametersVO(labelTemplates.get(0)), this, labelConfiguration, startPosition,
	//					copies) {
	//				@Override
	//				protected void saveButtonClick(LabelParametersVO parameters)
	//						throws ValidationException {
	//					LabelsReportParameters labelsReportParameters = new LabelsReportParameters(
	//							selector.getSelectedRecordIds(),
	//							parameters.getLabelConfiguration(),
	//							parameters.getStartPosition(),
	//							parameters.getNumberOfCopies());
	//
	//					getWindow().setContent(new ReportViewer(labelsReportFactory.getReportBuilder(labelsReportParameters),
	//							labelsReportFactory.getFilename(null)));
	//				}
	//
	//				@Override
	//				protected void cancelButtonClick(LabelParametersVO parameters) {
	//					getWindow().close();
	//				}
	//			};
	//		}
	//	}

	@Override
	public void setBorrowedMessage(String borrowedMessage) {
		if (borrowedMessage != null) {
			borrowedLabel.setVisible(true);
			borrowedLabel.setValue($(borrowedMessage));
		} else {
			borrowedLabel.setVisible(false);
			borrowedLabel.setValue(null);
		}
	}
}
