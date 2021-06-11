package com.constellio.app.modules.rm.ui.pages.legalrequirement.requirement;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.components.RMMetadataDisplayFactory;
import com.constellio.app.modules.rm.ui.pages.legalrequirement.component.LegalRequirementReferenceEditableRecordTableField;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.SelectionPanelReportPresenter;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayLegalRequirementViewImpl extends BaseViewImpl implements DisplayLegalRequirementView {

	private static Logger LOGGER = LoggerFactory.getLogger(DisplayLegalRequirementViewImpl.class);

	private DisplayLegalRequirementPresenter presenter;

	public DisplayLegalRequirementViewImpl() {
		presenter = new DisplayLegalRequirementPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected String getTitle() {
		return $("LegalRequirementManagement.legalRequirement");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);

		if (presenter.canEdit()) {
			HorizontalLayout buttonLayout = new HorizontalLayout();
			buttonLayout.setSpacing(true);
			buttonLayout.addComponent(buildEditButton());
			buttonLayout.addComponent(buildDeleteButton());
			buttonLayout.addComponent(buildGenerateReportButton());
			mainLayout.addComponent(buttonLayout);
			mainLayout.setComponentAlignment(buttonLayout, Alignment.TOP_RIGHT);
		}

		RecordDisplay newRecordDisplay = new RecordDisplay(presenter.getRecordVO(), new RMMetadataDisplayFactory());
		mainLayout.addComponent(newRecordDisplay);

		LegalRequirementReferenceEditableRecordTableField editableRecordsTableField =
				new LegalRequirementReferenceEditableRecordTableField(presenter.getRequirementReferenceFieldPresenter()) {
					@Override
					public void attach() {
						super.attach();
						if (getValue().isEmpty()) {
							setVisible(false);
						}
					}
				};
		editableRecordsTableField.setReadOnly(true);
		editableRecordsTableField.setResizedIfRowsDoesNotFillHeight(true);

		mainLayout.addComponent(editableRecordsTableField);

		return mainLayout;
	}

	private Component buildEditButton() {
		BaseButton editButton = new EditButton($("edit")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editButtonClicked();
			}
		};

		editButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		editButton.addStyleName(ValoTheme.BUTTON_LINK);

		return editButton;
	}

	private Component buildDeleteButton() {
		BaseButton deleteButton = new DeleteButton($("delete")) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				try {
					presenter.deleteButtonClicked();
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
					showErrorMessage($("LegalRequirementManagement.deleteRecordFailed"));
				}
			}
		};

		deleteButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		deleteButton.addStyleName(ValoTheme.BUTTON_LINK);

		return deleteButton;
	}

	private Button buildGenerateReportButton() {
		SelectionPanelReportPresenter selectionPanelReportPresenter = presenter.getSelectionPanelReportPresenter();
		ReportTabButton reportGeneratorButton = new ReportTabButton($("SearchView.metadataReportTitle"),
				$("SearchView.metadataReportTitle"), getConstellioFactories().getAppLayerFactory(), getCollection(),
				selectionPanelReportPresenter, getSessionContext()) {
			@Override
			public void buttonClick(ClickEvent event) {
				setRecordVoList(presenter.getRecordVO());
				super.buttonClick(event);
			}
		};

		reportGeneratorButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		reportGeneratorButton.addStyleName(ValoTheme.BUTTON_LINK);

		return reportGeneratorButton;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return new TitleBreadcrumbTrail(this, getTitle()) {
			@Override
			public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
				return Collections.singletonList(new IntermediateBreadCrumbTailItem() {
					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public String getTitle() {
						return $("AdminView.legalRequirements");
					}

					@Override
					public void activate(Navigation navigate) {
						navigate.to(RMViews.class).listLegalRequirements();
					}
				});
			}
		};
	}
}
