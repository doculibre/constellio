package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.BaseDisplay.CaptionAndComponent;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.contextmenu.ConfirmDialogContextMenuItemClickListener;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class DisplayConnectorInstanceViewImpl extends BaseViewImpl implements DisplayConnectorInstanceView {
	public static final String STYLE_NAME = "display-connectorInstance";
	private RecordVO recordVO;
	private VerticalLayout mainLayout;
	private RecordDisplay recordDisplay;
	private DisplayConnectorInstancePresenter presenter;
	private Button editConnectorButton, startButton, stopButton, editSchemasButton, deleteDocumentsButton, indexationReportButton, errorsReportButton;
	private TextArea lastDocumentsField;

	private Label documentsCountLabel;

	public DisplayConnectorInstanceViewImpl() {
		presenter = new DisplayConnectorInstancePresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	public void setRecord(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	@Override
	protected String getTitle() {
		return presenter.getTitle();
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		recordDisplay = new RecordDisplay(recordVO, new ConnectorMetadataDisplayFactory());

		Component documentsCountComponent = newDocumentsCountComponent();
		lastDocumentsField = new TextArea();
		lastDocumentsField.setEnabled(false);
		lastDocumentsField.setSizeFull();

		mainLayout.addComponents(recordDisplay, documentsCountComponent, lastDocumentsField);
		mainLayout.setExpandRatio(lastDocumentsField, 1);
		return mainLayout;
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

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<>();
		editConnectorButton = new LinkButton($("DisplayConnectorInstanceView.edit")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editConnectorInstanceButtonClicked();
			}
		};
		startButton = new LinkButton($("DisplayConnectorInstanceView.start")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.start();
			}
		};
		startButton.setVisible(presenter.isStartButtonVisible());
		stopButton = new LinkButton($("DisplayConnectorInstanceView.stop")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.stop();
			}
		};
		stopButton.setVisible(presenter.isStopButtonVisible());
		editSchemasButton = new LinkButton($("DisplayConnectorInstanceView.editSchemas")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editSchemasButtonClicked();
			}
		};
		deleteDocumentsButton = new LinkButton($("DisplayConnectorInstanceView.deleteDocumentsButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				new ConfirmDialogContextMenuItemClickListener(ConfirmDialogButton.DialogMode.WARNING) {
					@Override
					protected String getConfirmDialogMessage() {
						return $("ConfirmDialog.confirmDelete");
					}

					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteDocumentsButtonClicked();
					}
				}.buttonClick(event);
			}
		};

		indexationReportButton = new LinkButton($("DisplayConnectorInstanceView.indexationReportButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.indexationReportButtonClicked();
			}
		};
		errorsReportButton = new LinkButton($("DisplayConnectorInstanceView.errorsReportButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.errorsReportButtonClicked();
			}
		};

		actionMenuButtons.add(startButton);
		actionMenuButtons.add(stopButton);
		actionMenuButtons.add(editConnectorButton);
		actionMenuButtons.add(editSchemasButton);
		actionMenuButtons.add(indexationReportButton);
		actionMenuButtons.add(errorsReportButton);
		actionMenuButtons.add(deleteDocumentsButton);

		return actionMenuButtons;
	}

	private BaseDisplay newDocumentsCountComponent() {
		String caption = $("DisplayConnectorInstanceView.docsCount");
		Label captionLabel = new Label(caption);
		String captionId = BaseDisplay.STYLE_CAPTION + "-docsCount";
		captionLabel.setId(captionId);
		captionLabel.addStyleName(captionId);
		String valueId = BaseDisplay.STYLE_VALUE + "-docsCount";
		documentsCountLabel = new Label("");
		documentsCountLabel.setId(valueId);
		documentsCountLabel.addStyleName(valueId);
		CaptionAndComponent captionsAndComponent = new CaptionAndComponent(captionLabel, documentsCountLabel);
		return new BaseDisplay(asList(captionsAndComponent));
	}

	@Override
	public boolean isBackgroundViewMonitor() {
		return true;
	}

	@Override
	protected void onBackgroundViewMonitor() {
		presenter.backgroundViewMonitor();
	}

	@Override
	public void setDocumentsCount(long count) {
		documentsCountLabel.setValue("" + count);
	}

	@Override
	public void setLastDocuments(String lastDocuments) {
		lastDocumentsField.setValue(lastDocuments);
	}

	public class ConnectorMetadataDisplayFactory extends MetadataDisplayFactory {
		@Override
		public Component buildSingleValue(RecordVO recordVO, MetadataVO metadata, Object displayValue) {
			if (metadata.getCode().endsWith(ConnectorInstance.PROPERTIES_MAPPING)) {
				return null;
			} else if (metadata.getCode().endsWith(ConnectorInstance.TRAVERSAL_CODE)) {
				return null;
			} else {
				return super.buildSingleValue(recordVO, metadata, displayValue);
			}
		}
	}
}
