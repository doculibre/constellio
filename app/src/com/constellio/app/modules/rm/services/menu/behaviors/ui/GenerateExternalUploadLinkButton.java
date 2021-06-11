package com.constellio.app.modules.rm.services.menu.behaviors.ui;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ExternalUploadLink;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.clipboard.CopyToClipBoard;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.ExternalAccessUrl;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

import static com.constellio.app.ui.i18n.i18n.$;

public class GenerateExternalUploadLinkButton extends WindowButton {

	private User requester;
	private Folder folder;
	private BaseView view;

	AppLayerFactory appLayerFactory;
	private RMSchemasRecordsServices rm;
	private RecordServices recordsServices;
	private ConstellioEIMConfigs configs;

	private JodaDateField dateField;
	private BaseButton saveButton;

	public GenerateExternalUploadLinkButton(AppLayerFactory appLayerFactory, String collection, User requester,
											Folder folder,
											BaseView view) {
		super($("ExternalUploadViewImpl.generateExternalUploadLink"),
				$("ExternalUploadViewImpl.generateExternalUploadLink"),
				WindowConfiguration.modalDialog("570px", "400px"));

		this.requester = requester;
		this.folder = folder;
		this.view = view;

		this.appLayerFactory = appLayerFactory;
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		recordsServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		configs = new ConstellioEIMConfigs(appLayerFactory.getModelLayerFactory());
	}

	@Override
	protected Component buildWindowContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setHeight("100%");
		mainLayout.setMargin(new MarginInfo(true));
		mainLayout.setSpacing(true);

		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		List<ExternalUploadLink> externalUploadLinks = rm.wrapExternalUploadLinks(searchServices.getAllRecords(rm.externalAccessUrl_upload.schemaType()));

		ExternalUploadLink existingLinkForFolder = externalUploadLinks.stream().filter(link -> folder.getId().equals(link.getAccessRecord())).findFirst().orElse(null);
		if (existingLinkForFolder != null) {
			Label replaceWithNewLinkLabel = new Label($("ExternalUploadViewImpl.replaceWithNewLink"));
			mainLayout.addComponents(buildLayoutForExistingLink(existingLinkForFolder), replaceWithNewLinkLabel);
		}


		dateField = new JodaDateField();
		dateField.setCaption($("DocumentMenuItemActionBehaviors.expirationDate"));
		mainLayout.addComponent(dateField);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setWidth("100%");
		buttonLayout.setHeight("100%");
		mainLayout.addComponent(buttonLayout);
		mainLayout.setExpandRatio(buttonLayout, 1);

		saveButton = new BaseButton($("LabelsButton.send")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				generateRecordAndLink((LocalDate) dateField.getConvertedValue(), existingLinkForFolder);
			}
		};
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		buttonLayout.addComponent(saveButton);
		buttonLayout.setComponentAlignment(saveButton, Alignment.BOTTOM_CENTER);

		return mainLayout;
	}


	private Component buildLayoutForExistingLink(ExternalUploadLink existingLinkForFolder) {
		VerticalLayout layout = new VerticalLayout();

		Label useExistingLinkLabel = new Label($("ExternalUploadViewImpl.useExistingLink"));


		IconButton copyToClipboard = new IconButton(FontAwesome.CLIPBOARD, "") {
			@Override
			protected void buttonClick(ClickEvent event) {
				CopyToClipBoard.copyToClipBoard(generateLinkFrom(existingLinkForFolder));
			}
		};
		copyToClipboard.addStyleName(ValoTheme.BUTTON_LINK);

		IconButton deleteButton = new IconButton(FontAwesome.TRASH, "") {
			@Override
			protected void buttonClick(ClickEvent event) {
				recordsServices.physicallyDeleteNoMatterTheStatus(existingLinkForFolder, User.GOD, new RecordPhysicalDeleteOptions());
				getWindow().setContent(buildWindowContent());
			}
		};
		deleteButton.addStyleName(ValoTheme.BUTTON_LINK);
		I18NHorizontalLayout buttonsLayout = new I18NHorizontalLayout(copyToClipboard, deleteButton);

		I18NHorizontalLayout linkAndButtonsLayout = new I18NHorizontalLayout(useExistingLinkLabel, buttonsLayout);
		linkAndButtonsLayout.setSpacing(true);
		linkAndButtonsLayout.setComponentAlignment(useExistingLinkLabel, Alignment.MIDDLE_LEFT);
		linkAndButtonsLayout.setDefaultComponentAlignment(Alignment.MIDDLE_RIGHT);

		LocalDate expirationDate = existingLinkForFolder.getExpirationDate();
		Label expirationDateInfoLabel = new Label($("ExternalUploadViewImpl.expirationDate",
				expirationDate == null ? $("ExternalUploadViewImpl.doesNotExpire") : expirationDate.toString()));
		expirationDateInfoLabel.addStyleName(ValoTheme.LABEL_TINY);

		layout.addComponents(linkAndButtonsLayout, expirationDateInfoLabel);

		return layout;
	}


	private void generateRecordAndLink(LocalDate expirationDate,
									   ExternalUploadLink existingLinkForFolder) {
		try {
			ExternalAccessUrl externalAccessRecord = generateRecord(expirationDate);
			if (existingLinkForFolder != null) {
				recordsServices.physicallyDeleteNoMatterTheStatus(existingLinkForFolder, User.GOD, new RecordPhysicalDeleteOptions());
			}

			String url = generateLinkFrom(externalAccessRecord);
			CopyToClipBoard.copyToClipBoard(url);
			getWindow().close();
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}

	private ExternalAccessUrl generateRecord(LocalDate expirationDate) throws RecordServicesException {
		ExternalAccessUrl accessUrl = rm.newExternalUploadLink()
				.setToken(UUID.randomUUID().toString())
				.setAccessRecord(folder.getId())
				.setStatus(ExternalAccessUrlStatus.OPEN)
				.setExpirationDate(expirationDate);

		accessUrl.setCreatedBy(requester.getId());
		accessUrl.setCreatedOn(new LocalDateTime());

		Transaction transaction = new Transaction();
		transaction.add(accessUrl);
		recordsServices.execute(transaction);

		return accessUrl;
	}

	private String generateLinkFrom(ExternalAccessUrl externalAccess) {
		String url = configs.getConstellioUrl();

		StringBuilder sb = new StringBuilder();
		sb.append(url);
		sb.append(RMNavigationConfiguration.EXTERNAL_UPLOAD + "?id=");
		sb.append(externalAccess.getId());
		sb.append("&token=");
		sb.append(externalAccess.getToken());
		return sb.toString();
	}
}