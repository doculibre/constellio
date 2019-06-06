package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.SelectionPanelExtension;
import com.constellio.app.api.extensions.params.AvailableActionsParam;
import com.constellio.app.api.extensions.params.EmailMessageParams;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.cart.CartEmailService;
import com.constellio.app.modules.rm.services.cart.CartEmailServiceRuntimeException;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCategoryFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderRetentionRuleFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;
import com.constellio.app.modules.rm.ui.pages.pdf.ConsolidatedPdfButton;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMUserFolder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.BaseLink;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.SIPButton.SIPButtonImpl;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.RMSelectionPanelReportPresenter;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.table.SelectionTableAdapter;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.emails.EmailServices;
import com.constellio.model.services.emails.EmailServices.EmailMessage;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.zipContents.ZipContentsService;
import com.constellio.model.services.search.zipContents.ZipContentsService.NoContentToZipRuntimeException;
import com.constellio.model.services.users.UserServices;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class RMSelectionPanelExtension extends SelectionPanelExtension {

	private static Logger LOGGER = LoggerFactory.getLogger(RMSelectionPanelExtension.class);

	private static final String ZIP_CONTENT_RESOURCE = "zipContentsFolder";
	
	AppLayerFactory appLayerFactory;
	String collection;
	IOServices ioServices;
	RMSchemasRecordsServices rm;
	private RMModuleExtensions rmModuleExtensions;

	public RMSelectionPanelExtension(AppLayerFactory appLayerFactory, String collection) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.ioServices = this.appLayerFactory.getModelLayerFactory().getDataLayerFactory().getIOServicesFactory().newIOServices();
		this.rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	@Override
	public void addAvailableActions(AvailableActionsParam param) {
		UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		boolean hasAccessToSIP = userServices.getUserInCollection(param.getUser().getUsername(), collection)
				.has(RMPermissionsTo.GENERATE_SIP_ARCHIVES).globally();
		addZipButton(param);
		addDeleteButton(param);
		addMoveButton(param);
		addDuplicateButton(param);
		addClassifyButton(param);
		addCheckInButton(param);
		addSendEmailButton(param);
		addMetadataReportButton(param);
		addPdfButton(param);
		if (hasAccessToSIP) {
			addSIPbutton(param);
		}
	}

	public void addZipButton(final AvailableActionsParam param) {
		final String zippedContentsFilename = $("SearchView.contentZip");
		StreamSource zippedContents = new StreamSource() {
			@Override
			public InputStream getStream() {
				ModelLayerFactory modelLayerFactory = param.getView().getConstellioFactories().getModelLayerFactory();
				File folder = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newFileService()
						.newTemporaryFolder(ZIP_CONTENT_RESOURCE);
				File file = new File(folder, zippedContentsFilename);
				try {
					new ZipContentsService(modelLayerFactory, collection)
							.zipContentsOfRecords(param.getIds(), file);
					return new FileInputStream(file);
				} catch (NoContentToZipRuntimeException e) {
					LOGGER.error("Error while zipping", e);
					showErrorMessage($("SearchView.noContentInSelectedRecords"));
					return null;
				} catch (Exception e) {
					LOGGER.error("Error while zipping", e);
					showErrorMessage($("SearchView.zipContentsError"));
					return null;
				}
			}
		};

		Component zipButton = new BaseLink($("ReportViewer.download", "(zip)"),
				new DownloadStreamResource(zippedContents, zippedContentsFilename));
		zipButton.setIcon(FontAwesome.FILE_ARCHIVE_O);
		zipButton.setPrimaryStyleName("v-button");
		zipButton.removeStyleName("link");
		zipButton.addStyleName(ValoTheme.BUTTON_LINK);
		zipButton.setEnabled(!param.getIds().isEmpty() && containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE)));
		zipButton.setVisible(!param.getIds().isEmpty() && containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE)));
		((VerticalLayout) param.getComponent()).addComponent(zipButton);
	}

	public void addDeleteButton(final AvailableActionsParam param) {
		Button deleteButton = new DeleteButton(FontAwesome.TRASH_O, $("delete"), false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
			}
		};
		deleteButton.addStyleName(ValoTheme.BUTTON_LINK);
		// FIXME
		deleteButton.setEnabled(!param.getIds().isEmpty());
		deleteButton.setVisible(!param.getIds().isEmpty());
		((VerticalLayout) param.getComponent()).addComponent(deleteButton);
	}

	public void addPdfButton(final AvailableActionsParam param) {
		final User currentUser = param.getUser();
		WindowButton pdfButton = new ConsolidatedPdfButton(param) {
			@Override
			public void buttonClick(ClickEvent event) {
				List<Record> records = recordServices().getRecordsById(collection, param.getIds());
				for (Record record : records) {
					if (!rmModuleExtensions.isCreatePDFAActionPossibleOnDocument(rm.wrapDocument(record), currentUser)) {
						showErrorMessage(i18n.$("ConstellioHeader.pdfGenerationBlockedByExtension"));
						return;
					}
				}
				super.buttonClick(event);
			}
		};
		setStyles(pdfButton);
		pdfButton.setEnabled(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE)));
		pdfButton.setVisible(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE)));
		((VerticalLayout) param.getComponent()).addComponent(pdfButton);
	}

	public void addMoveButton(final AvailableActionsParam param) {
		WindowButton moveInFolderButton = new WindowButton($("ConstellioHeader.selection.actions.moveInFolder"), $("ConstellioHeader.selection.actions.moveInFolder")
				, WindowButton.WindowConfiguration.modalDialog("50%", "140px")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout.addStyleName("no-scroll");
				verticalLayout.setSpacing(true);
				final LookupFolderField field = new LookupFolderField(true);
				field.focus();
				field.setWindowZIndex(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX + 1);
				verticalLayout.addComponent(field);
				BaseButton saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						String parentId = (String) field.getValue();
						try {
							parentFolderButtonClicked(parentId, param);
						} catch (Throwable e) {
							//                            LOGGER.warn("error when trying to modify folder parent to " + parentId, e);
							//                            showErrorMessage("DisplayFolderView.parentFolderException");
							e.printStackTrace();
						}
						getWindow().close();
					}

					@Override
					public boolean isVisible() {
						return containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE));
					}

					@Override
					public boolean isEnabled() {
						return isVisible();
					}
				};
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				HorizontalLayout hLayout = new HorizontalLayout();
				hLayout.setSpacing(true);
				hLayout.setSizeFull();
				hLayout.addComponent(saveButton);
				hLayout.setComponentAlignment(saveButton, Alignment.BOTTOM_RIGHT);
				verticalLayout.addComponent(hLayout);
				return verticalLayout;
			}
		};
		setStyles(moveInFolderButton);
		moveInFolderButton.setEnabled(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)));
		moveInFolderButton.setVisible(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)));
		((VerticalLayout) param.getComponent()).addComponent(moveInFolderButton);
	}


	public void addDuplicateButton(final AvailableActionsParam param) {
		WindowButton duplicateButton = new WindowButton($("ConstellioHeader.selection.actions.duplicate"), $("ConstellioHeader.selection.actions.duplicate")
				, WindowButton.WindowConfiguration.modalDialog("550px", "200px")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout.setSizeFull();
				verticalLayout.setSpacing(true);
				final LookupFolderField field = new LookupFolderField(true);
				field.focus();
				field.setWindowZIndex(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX + 1);
				verticalLayout.addComponent(field);
				BaseButton saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						String parentId = (String) field.getValue();
						duplicateButtonClicked(parentId, param);
						getWindow().close();
					}

					@Override
					public boolean isVisible() {
						return containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE));
					}

					@Override
					public boolean isEnabled() {
						return isVisible();
					}
				};
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				HorizontalLayout hLayout = new HorizontalLayout();
				hLayout.setSizeFull();
				hLayout.addComponent(saveButton);
				hLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_CENTER);
				verticalLayout.addComponent(hLayout);
				return verticalLayout;
			}
		};
		setStyles(duplicateButton);
		duplicateButton.setEnabled(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)));
		duplicateButton.setVisible(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)));
		((VerticalLayout) param.getComponent()).addComponent(duplicateButton);
	}

	public void addMetadataReportButton(final AvailableActionsParam param) {
		ReportTabButton tabButton = new ReportTabButton($("SearchView.metadataReportTitle"), $("SearchView.metadataReportTitle"),
				appLayerFactory, collection, param.getSchemaTypeCodes().size() != 1, false, buildReportPresenter(param), param.getView().getSessionContext()) {

			@Override
			public void buttonClick(ClickEvent event) {
				List<RecordVO> recordVOS = getRecordVOFromIds(param.getIds(), param);
				setRecordVoList(recordVOS.toArray(new RecordVO[0]));
				super.buttonClick(event);
			}
		};
		setStyles(tabButton);
		tabButton.setEnabled(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, Task.SCHEMA_TYPE)));
		tabButton.setVisible(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, Task.SCHEMA_TYPE)));
		((VerticalLayout) param.getComponent()).addComponent(tabButton);
	}

	private RMSelectionPanelReportPresenter buildReportPresenter(final AvailableActionsParam param) {
		return new RMSelectionPanelReportPresenter(appLayerFactory, collection, param.getUser()) {
			@Override
			public String getSelectedSchemaType() {
				return param.getSchemaTypeCodes().get(0);
			}

			@Override
			public List<String> getSelectedRecordIds() {
				return param.getIds();
			}
		};
	}

	public void addSIPbutton(final AvailableActionsParam param) {
		SIPButtonImpl tabButton = new SIPButtonImpl($("SIPButton.caption"), $("SIPButton.caption"), param.getView(), true) {
			@Override
			public void buttonClick(ClickEvent event) {
				List<RecordVO> recordVOS = getRecordVOFromIds(param.getIds(), param);
				addAllObject(recordVOS.toArray(new RecordVO[0]));
				super.buttonClick(event);
			}
		};
		setStyles(tabButton);
		tabButton.setEnabled(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)));
		tabButton.setVisible(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)));
		((VerticalLayout) param.getComponent()).addComponent(tabButton);
	}

	public void addClassifyButton(final AvailableActionsParam param) {
		WindowButton classifyButton = new WindowButton($("ConstellioHeader.selection.actions.classify"), $("ConstellioHeader.selection.actions.classify")
				, WindowButton.WindowConfiguration.modalDialog("90%", "300px")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout.addStyleName("no-scroll");
				verticalLayout.setSpacing(true);

				final LookupFolderField folderField = new LookupFolderField(true);
				folderField.setWindowZIndex(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX + 1);
				folderField.setVisible(true);
				folderField.setRequired(true);
				folderField.focus();

				final FolderCategoryFieldImpl categoryField = new FolderCategoryFieldImpl();
				categoryField.setWindowZIndex(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX + 1);
				categoryField.setVisible(false);
				categoryField.setRequired(false);

				final FolderRetentionRuleFieldImpl retentionRuleField = new FolderRetentionRuleFieldImpl(collection);
				retentionRuleField.setVisible(false);

				final ListOptionGroup classificationOption = new ListOptionGroup($("ConstellioHeader.selection.actions.classificationChoice"), asList(true, false));
				classificationOption.addStyleName("horizontal");
				classificationOption.setNullSelectionAllowed(false);
				classificationOption.setItemCaption(true, $("ConstellioHeader.selection.actions.classifyInClassificationPlan"));
				classificationOption.setItemCaption(false, $("ConstellioHeader.selection.actions.classifyInFolder"));
				classificationOption.setValue(false);
				classificationOption.addValueChangeListener(new Property.ValueChangeListener() {
					@Override
					public void valueChange(Property.ValueChangeEvent event) {
						boolean categoryClassification = Boolean.TRUE.equals(event.getProperty().getValue());
						folderField.setVisible(!categoryClassification);
						folderField.setRequired(!categoryClassification);
						categoryField.setVisible(categoryClassification);
						categoryField.setRequired(categoryClassification);
						if (categoryClassification) {
							String categoryId = (String) categoryField.getValue();
							adjustRetentionRuleField(categoryId, retentionRuleField);
						} else {
							retentionRuleField.setVisible(false);
						}
						if (categoryClassification) {
							categoryField.focus();
						} else {
							folderField.focus();
						}
					}
				});
				classificationOption.setVisible(containsOnly(param.getSchemaTypeCodes(), asList(UserFolder.SCHEMA_TYPE)));

				categoryField.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						String categoryId = (String) event.getProperty().getValue();
						adjustRetentionRuleField(categoryId, retentionRuleField);
					}
				});

				verticalLayout.addComponents(classificationOption, folderField, categoryField, retentionRuleField);
				BaseButton saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						String parentId = (String) folderField.getValue();
						String categoryId = (String) categoryField.getValue();
						String retentionRuleId = retentionRuleField.getValue();
						if (parentId == null && categoryId == null) {
							if (folderField.isVisible()) {
								showErrorMessage($("ConstellioHeader.noParentFolderSelectedForClassification"));
								return;
							} else {
								showErrorMessage($("ConstellioHeader.noCategorySelectedForClassification"));
								return;
							}
						}
						boolean isClassifiedInFolder = !Boolean.TRUE.equals(classificationOption.getValue());
						try {
							classifyButtonClicked(parentId, categoryId, retentionRuleId, isClassifiedInFolder, param);
						} catch (Throwable e) {
							//                            LOGGER.warn("error when trying to modify folder parent to " + parentId, e);
							//                            showErrorMessage("DisplayFolderView.parentFolderException");
							e.printStackTrace();
						}
						getWindow().close();
						ConstellioUI.getCurrent().updateContent();
					}
				};
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				HorizontalLayout hLayout = new HorizontalLayout();
				hLayout.setSpacing(true);
				hLayout.setSizeFull();
				hLayout.addComponent(saveButton);
				hLayout.setComponentAlignment(saveButton, Alignment.BOTTOM_CENTER);
				verticalLayout.addComponent(hLayout);
				return verticalLayout;
			}

			private void adjustRetentionRuleField(String categoryId, FolderRetentionRuleFieldImpl retentionRuleField) {
				if (categoryId != null) {
					RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
					Category category = rm.getCategory(categoryId);
					List<String> retentionRules = category.getRententionRules();
					boolean manyRetentionRules = retentionRules.size() > 1;
					if (manyRetentionRules) {
						retentionRuleField.setVisible(true);
						retentionRuleField.setOptions(retentionRules);
						retentionRuleField.setRequired(true);
					} else {
						retentionRuleField.setVisible(false);
						retentionRuleField.setRequired(false);
					}
				} else {
					retentionRuleField.setRequired(false);
					retentionRuleField.setVisible(false);
				}
			}

			@Override
			public boolean isVisible() {
				return containsOnly(param.getSchemaTypeCodes(), asList(UserDocument.SCHEMA_TYPE, UserFolder.SCHEMA_TYPE));
			}

			@Override
			public boolean isEnabled() {
				return isVisible();
			}
		};
		setStyles(classifyButton);
		classifyButton.setEnabled(containsOnly(param.getSchemaTypeCodes(), asList(UserDocument.SCHEMA_TYPE, UserFolder.SCHEMA_TYPE)));
		classifyButton.setVisible(containsOnly(param.getSchemaTypeCodes(), asList(UserDocument.SCHEMA_TYPE, UserFolder.SCHEMA_TYPE)));
		((VerticalLayout) param.getComponent()).addComponent(classifyButton);
	}

	public void addCheckInButton(final AvailableActionsParam param) {
		Button checkInButton = new Button($("ConstellioHeader.selection.actions.checkIn")) {
			@Override
			public boolean isVisible() {
				return containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE)) && areAllCheckedOut(param.getIds());
			}

			@Override
			public boolean isEnabled() {
				return isVisible();
			}
		};
		checkInButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				if (!param.getIds().isEmpty()) {
					RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
					Map<RecordVO, MetadataVO> records = new HashMap<>();
					RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();
					for (String id : param.getIds()) {
						Record record = recordServices.getDocumentById(id);
						if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
							if (isCheckInPossible(param, id)) {
								RecordVO documentVo = recordToVOBuilder.build(appLayerFactory.getModelLayerFactory().newRecordServices()
										.getDocumentById(id), RecordVO.VIEW_MODE.TABLE, param.getView().getSessionContext());
								records.put(documentVo, documentVo.getMetadata(Document.CONTENT));
							}
						}
					}
					final int numberOfRecords = records.size();
					if (numberOfRecords > 0) {
						final UpdateContentVersionWindowImpl uploadWindow = new UpdateContentVersionWindowImpl(records) {

							@Override
							public void close() {
								super.close();
								if (!this.isCancel()) {
									if (numberOfRecords != param.getIds().size()) {
										RMSelectionPanelExtension.this.showErrorMessage($("ConstellioHeader.selection.actions.couldNotCheckIn",
												numberOfRecords, param.getIds().size()));
									} else {
										RMSelectionPanelExtension.this.showErrorMessage($("ConstellioHeader.selection.actions.actionCompleted", numberOfRecords));
									}
								}
							}
						};

						uploadWindow.open(true);
					} else {
						showErrorMessage($("ConstellioHeader.selection.actions.noApplicableRecords"));
					}
				} else {
					showErrorMessage($("ConstellioHeader.selection.actions.noApplicableRecords"));
				}
			}
		});

		setStyles(checkInButton);
		checkInButton.setEnabled(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE)) && areAllCheckedOut(param.getIds()));
		checkInButton.setVisible(checkInButton.isEnabled());
		((VerticalLayout) param.getComponent()).addComponent(checkInButton);
	}

	private boolean areAllCheckedOut(List<String> ids) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		List<Document> documents = rm.getDocuments(ids);
		for (Document document : documents) {
			if (document.getContent() == null || document.getContent().getCheckoutUserId() == null) {
				return false;
			}
		}
		return true;
	}

	private void addSendEmailButton(final AvailableActionsParam param) {
		Button button = new Button($("ConstellioHeader.selection.actions.prepareEmail")) {
			@Override
			public boolean isVisible() {
				return containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE));
			}

			@Override
			public boolean isEnabled() {
				return isVisible();
			}
		};
		button.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				emailPreparationRequested(param);
			}
		});
		setStyles(button);
		button.setEnabled(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE)));
		button.setVisible(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE)));
		((VerticalLayout) param.getComponent()).addComponent(button);
	}

	private DecommissioningService decommissioningService(AvailableActionsParam param) {
		return new DecommissioningService(param.getUser().getCollection(), appLayerFactory);
	}

	public void parentFolderButtonClicked(String parentId, AvailableActionsParam param)
			throws RecordServicesException {
		List<String> recordIds = param.getIds();
		List<String> couldNotMove = new ArrayList<>();
		if (isNotBlank(parentId)) {
			RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
			RMSchemasRecordsServices rmSchemas = new RMSchemasRecordsServices(collection, appLayerFactory);
			for (String id : recordIds) {
				Record record = recordServices.getDocumentById(id);
				boolean isMovePossible = true;
				try {
					switch (record.getTypeCode()) {
						case Folder.SCHEMA_TYPE:
							Folder folder = rmSchemas.getFolder(id);
							if (!rmModuleExtensions.isMoveActionPossibleOnFolder(folder, param.getUser())) {
								isMovePossible = false;
								couldNotMove.add(record.getTitle());
								break;
							}
							if (isMovePossible) {
								recordServices.update(folder.setParentFolder(parentId));
							}
							break;
						case Document.SCHEMA_TYPE:
							if (!rmModuleExtensions.isMoveActionPossibleOnDocument(rm.wrapDocument(record), param.getUser())) {
								isMovePossible = false;
								couldNotMove.add(record.getTitle());
								break;
							}
							if (isMovePossible) {
								recordServices.update(rmSchemas.getDocument(id).setFolder(parentId));
							}
							break;
						default:
							couldNotMove.add(record.getTitle());
					}
				} catch (RecordServicesException.ValidationException e) {
					e.printStackTrace();
					couldNotMove.add(record.getTitle());
				}
			}
		}

		if (couldNotMove.isEmpty()) {
			showErrorMessage($("ConstellioHeader.selection.actions.actionCompleted", recordIds.size()));
		} else {
			int successCount = recordIds.size() - couldNotMove.size();
			showErrorMessage($("ConstellioHeader.selection.actions.couldNotMove", successCount, recordIds.size()));
		}
	}

	public void duplicateButtonClicked(String parentId, final AvailableActionsParam param) {
		List<String> recordIds = param.getIds();
		List<String> couldNotDuplicate = new ArrayList<>();
		if (isNotBlank(parentId)) {
			RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
			RMSchemasRecordsServices rmSchemas = new RMSchemasRecordsServices(collection, appLayerFactory);
			for (String id : recordIds) {
				boolean isCopyPossible = true;
				Record record = recordServices.getDocumentById(id);

				try {
					switch (record.getTypeCode()) {
						case Folder.SCHEMA_TYPE:
							if (!rmModuleExtensions.isCopyActionPossibleOnFolder(rmSchemas.wrapFolder(record), param.getUser())) {
								isCopyPossible = false;
								couldNotDuplicate.add(record.getTitle());
								break;
							}
							if (!isCopyPossible) {
								break;
							}

							Folder oldFolder = rmSchemas.wrapFolder(record);
							Folder newFolder = decommissioningService(param).duplicateStructureAndDocuments(oldFolder, param.getUser(), false);
							newFolder.setParentFolder(parentId);
							recordServices.add(newFolder);
							break;
						case Document.SCHEMA_TYPE:
							if (!rmModuleExtensions.isCopyActionPossibleOnDocument(rm.wrapDocument(record), param.getUser())) {
								isCopyPossible = false;
								couldNotDuplicate.add(record.getTitle());
								break;
							}
							if (!isCopyPossible) {
								break;
							}

							Document oldDocument = rmSchemas.wrapDocument(record);
							Document newDocument = rmSchemas.newDocumentWithType(oldDocument.getType());
							for (Metadata metadata : oldDocument.getSchema().getMetadatas().onlyNonSystemReserved().onlyManuals().onlyDuplicable()) {
								newDocument.set(metadata, record.get(metadata));
							}
							LocalDateTime now = LocalDateTime.now();
							newDocument.setFormCreatedBy(param.getUser());
							newDocument.setFormCreatedOn(now);
							newDocument.setCreatedBy(param.getUser().getId()).setModifiedBy(param.getUser().getId());
							newDocument.setCreatedOn(now).setModifiedOn(now);
							if (newDocument.getContent() != null) {
								User user = param.getUser();
								Content content = newDocument.getContent();
								ContentVersion contentVersion = content.getCurrentVersion();
								String filename = contentVersion.getFilename();
								ContentManager contentManager = rmSchemas.getModelLayerFactory().getContentManager();
								ContentVersionDataSummary contentVersionDataSummary = contentManager.getContentVersionSummary(contentVersion.getHash()).getContentVersionDataSummary();
								Content newContent = contentManager.createMajor(user, filename, contentVersionDataSummary);
								newDocument.setContent(newContent);
							}
							if (Boolean.TRUE == newDocument.getBorrowed()) {
								newDocument.setBorrowed(false);
							}
							String title = record.getTitle() + " (" + $("AddEditDocumentViewImpl.copy") + ")";
							newDocument.setTitle(title);
							newDocument.setFolder(parentId);
							recordServices.add(newDocument);
							break;
						default:
							couldNotDuplicate.add(record.getTitle());
					}
				} catch (RecordServicesException e) {
					couldNotDuplicate.add(record.getTitle());
				}
			}
		}

		if (couldNotDuplicate.isEmpty()) {
			showErrorMessage($("ConstellioHeader.selection.actions.actionCompleted", recordIds.size()));
		} else {
			int successCount = recordIds.size() - couldNotDuplicate.size();
			showErrorMessage($("ConstellioHeader.selection.actions.couldNotDuplicate", successCount, recordIds.size()));
		}
	}

	public void classifyButtonClicked(String parentId, String categoryId, String retentionRuleId,
									  boolean isClassifiedInFolder, AvailableActionsParam param)
			throws RecordServicesException {

		List<String> recordIds = param.getIds();
		List<String> couldNotMove = new ArrayList<>();
		if ((isClassifiedInFolder && isNotBlank(parentId)) || (!isClassifiedInFolder && isNotBlank(categoryId))) {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			RecordServices recordServices = recordServices();
			for (String id : recordIds) {
				Record record = null;
				try {
					record = recordServices.getDocumentById(id);
					switch (record.getTypeCode()) {
						case UserFolder.SCHEMA_TYPE:
							Folder newFolder = rm.newFolder();
							RMUserFolder userFolder = rm.wrapUserFolder(record);
							if (!isClassifiedInFolder) {
								classifyUserFolderInCategory(param, categoryId, retentionRuleId, userFolder);
							}
							decommissioningService(param).populateFolderFromUserFolder(newFolder, userFolder, param.getUser());
							if (isClassifiedInFolder) {
								newFolder.setParentFolder(parentId);
							}
							recordServices.add(newFolder);
							decommissioningService(param).duplicateSubStructureAndSave(newFolder, userFolder, param.getUser());
							deleteUserFolder(param, userFolder, param.getUser());
							break;
						case UserDocument.SCHEMA_TYPE:
							Document newDocument = rm.newDocument();
							UserDocument userDocument = rm.wrapUserDocument(record);
							decommissioningService(param).populateDocumentFromUserDocument(newDocument, userDocument, param.getUser());
							newDocument.setFolder(parentId);
							recordServices.add(newDocument);
							deleteUserDocument(param, rm.wrapUserDocument(record), param.getUser());
							break;
						default:
							couldNotMove.add(record.getTitle());
					}
				} catch (RecordServicesException e) {
					if (record != null) {
						couldNotMove.add(record.getTitle());
					}
					e.printStackTrace();
				} catch (IOException e) {
					if (record != null) {
						couldNotMove.add(record.getTitle());
					}
					e.printStackTrace();
				}
			}
		}

		if (couldNotMove.isEmpty()) {
			showErrorMessage($("ConstellioHeader.selection.actions.actionCompleted", recordIds.size()));
		} else {
			int successCount = recordIds.size() - couldNotMove.size();
			showErrorMessage($("ConstellioHeader.selection.actions.couldNotClassify", successCount, recordIds.size()));
		}
	}

	protected RecordServices recordServices() {
		return appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	protected void deleteUserFolder(AvailableActionsParam param, RMUserFolder rmUserFolder, User user) {
		decommissioningService(param).deleteUserFolder(rmUserFolder, user);
		refreshSelectionTables(param, rmUserFolder);
	}

	protected void deleteUserDocument(AvailableActionsParam param, UserDocument userDocument, User user) {
		decommissioningService(param).deleteUserDocument(userDocument, user);
		refreshSelectionTables(param, userDocument);
	}

	private void refreshSelectionTables(AvailableActionsParam param, RecordWrapper recordWrapper) {
		String recordId = recordWrapper.getId();
		Collection<Window> windows = UI.getCurrent().getWindows();
		for (Window window : windows) {
			SelectionTableAdapter selectionTableAdapter = ComponentTreeUtils.getFirstChild(window, SelectionTableAdapter.class);
			if (selectionTableAdapter != null) {
				try {
					selectionTableAdapter.getTable().removeItem(recordId);
				} catch (Throwable t) {
					selectionTableAdapter.refresh();
				}
			}
		}
		//        View currentView = ConstellioUI.getCurrent().getCurrentView();
		//        if (currentView instanceof ListUserDocumentsView) {
		//        	((ListUserDocumentsView) currentView).refresh();
		//        }
	}

	private void emailPreparationRequested(AvailableActionsParam param) {
		EmailMessage emailMessage = createEmail(param);
		String filename = emailMessage.getFilename();
		InputStream stream = emailMessage.getInputStream();
		startDownload(stream, filename);
	}

	private EmailMessage createEmail(AvailableActionsParam param) {
		File newTempFile = null;
		try {
			newTempFile = ioServices.newTemporaryFile("RMSelectionPanelExtension-emailFile");
			return createEmail(param, newTempFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			ioServices.deleteQuietly(newTempFile);
		}
	}

	private EmailMessage createEmail(AvailableActionsParam param, File messageFile) {
		try (OutputStream outputStream = ioServices.newFileOutputStream(messageFile, RMSelectionPanelExtension.class.getSimpleName() + ".createMessage.out")) {
			User user = param.getUser();
			String signature = getSignature(user);
			String subject = "";
			String from = user.getEmail();
			List<EmailServices.MessageAttachment> attachments = getAttachments(param);
			if (attachments == null || attachments.isEmpty()) {
				showErrorMessage($("ConstellioHeader.selection.actions.noApplicableRecords"));
				return null;
			} else if (attachments.size() != param.getIds().size()) {
				showErrorMessage($("ConstellioHeader.selection.actions.couldNotSendEmail", attachments.size(), param.getIds().size()));
			}

			AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
			EmailMessageParams params = new EmailMessageParams("selection", signature, subject, from, attachments);
			EmailMessage emailMessage = appLayerFactory.getExtensions().getSystemWideExtensions().newEmailMessage(params);
			if (emailMessage == null) {
				EmailServices emailServices = new EmailServices();
				ConstellioEIMConfigs configs = new ConstellioEIMConfigs(appLayerFactory.getModelLayerFactory());
				MimeMessage message = emailServices.createMimeMessage(from, subject, signature, attachments, configs);
				message.writeTo(outputStream);
				String filename = "cart.eml";
				InputStream inputStream = ioServices.newFileInputStream(messageFile, CartEmailService.class.getSimpleName() + ".createMessageForCart.in");
				emailMessage = new EmailMessage(filename, inputStream);
				closeAllInputStreams(attachments);
			}
			if (attachments.size() == param.getIds().size()) {
				showErrorMessage($("ConstellioHeader.selection.actions.actionCompleted", attachments.size()));
			}
			return emailMessage;
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String getSignature(User user) {
		return user.getSignature() != null ? user.getSignature() : user.getTitle();
	}

	private List<EmailServices.MessageAttachment> getAttachments(AvailableActionsParam param)
			throws IOException {
		//FIXME current version get only cart documents attachments
		List<EmailServices.MessageAttachment> returnList = new ArrayList<>();
		returnList.addAll(getDocumentsAttachments(param.getIds()));
		return returnList;
	}

	private List<EmailServices.MessageAttachment> getDocumentsAttachments(List<String> recordIds)
			throws IOException {
		List<EmailServices.MessageAttachment> returnList = new ArrayList<>();
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		for (String currentDocumentId : recordIds) {
			Record record = recordServices.getDocumentById(currentDocumentId);
			if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
				try {
					Document document = rmSchemasRecordsServices.wrapDocument(record);
					if (document.getContent() != null) {
						EmailServices.MessageAttachment contentFile = createAttachment(document);
						returnList.add(contentFile);
					}
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					throw new CartEmailServiceRuntimeException.CartEmlServiceRuntimeException_InvalidRecordId(e);
				}
			}
		}
		return returnList;
	}

	private EmailServices.MessageAttachment createAttachment(Document document)
			throws IOException {
		Content content = document.getContent();
		String hash = content.getCurrentVersion().getHash();
		ContentManager contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		InputStream inputStream = contentManager.getContentInputStream(hash, content.getCurrentVersion().getFilename());
		String mimeType = content.getCurrentVersion().getMimetype();
		String attachmentName = content.getCurrentVersion().getFilename();
		return new EmailServices.MessageAttachment().setMimeType(mimeType).setAttachmentName(attachmentName).setInputStream(inputStream);
	}

	private void closeAllInputStreams(List<EmailServices.MessageAttachment> attachments) {
		for (EmailServices.MessageAttachment attachment : attachments) {
			ioServices.closeQuietly(attachment.getInputStream());
			IOUtils.closeQuietly(attachment.getInputStream());
		}
	}

	@SuppressWarnings("deprecation")
	private void startDownload(final InputStream stream, String filename) {
		Resource resource = new ReportViewer.DownloadStreamResource(new StreamResource.StreamSource() {
			@Override
			public InputStream getStream() {
				return stream;
			}
		}, filename);
		Page.getCurrent().open(resource, null, false);
	}

	protected boolean isCheckInPossible(AvailableActionsParam param, String id) {
		boolean email = isEmail(id);
		return !email && (getContent(id) != null && isCurrentUserBorrower(param, id));
	}

	private boolean isEmail(String id) {
		Record record = appLayerFactory.getModelLayerFactory().newRecordServices().getDocumentById(id);
		return Email.SCHEMA.equals(record.getSchemaCode());
	}

	protected Content getContent(String id) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		Record record = appLayerFactory.getModelLayerFactory().newRecordServices().getDocumentById(id);
		Document document = rm.wrapDocument(record);
		return document.getContent();
	}

	protected boolean isCurrentUserBorrower(AvailableActionsParam param, String id) {
		User currentUser = param.getUser();
		Content content = getContent(id);
		return content != null && currentUser.getId().equals(content.getCheckoutUserId());
	}

	public void showErrorMessage(String errorMessage) {
		Notification notification = new Notification(errorMessage + "<br/><br/>" + $("clickToClose"), Notification.Type.WARNING_MESSAGE);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}

	public boolean containsOnly(List<String> list, List<String> values) {
		for (String value : list) {
			if (!values.contains(value)) {
				return false;
			}
		}
		return true && list.size() > 0;
	}

	public void classifyUserFolderInCategory(AvailableActionsParam param, String categoryId, String retentionRuleId,
											 RMUserFolder userFolder) {
		User currentUser = param.getUser();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		Category category = rm.getCategory(categoryId);
		userFolder.setCategory(category);
		List<String> retentionRules = category.getRententionRules();
		if (retentionRuleId != null) {
			userFolder.setRetentionRule(retentionRuleId);
		} else if (retentionRules.size() == 1) {
			userFolder.setRetentionRule(retentionRules.get(0));
		}
		AdministrativeUnit administrativeUnit = getDefaultAdministrativeUnit(currentUser);
		userFolder.setAdministrativeUnit(administrativeUnit);
	}

	private AdministrativeUnit getDefaultAdministrativeUnit(User user) {
		String collection = user.getCollection();
		AdministrativeUnit defaultAdministrativeUnit;
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		MetadataSchemaType administrativeUnitSchemaType = types.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
		LogicalSearchQuery visibleAdministrativeUnitsQuery = new LogicalSearchQuery();
		visibleAdministrativeUnitsQuery.filteredWithUserWrite(user);
		LogicalSearchCondition visibleAdministrativeUnitsCondition = from(administrativeUnitSchemaType).returnAll();
		visibleAdministrativeUnitsQuery.setCondition(visibleAdministrativeUnitsCondition);
		if (searchServices.getResultsCount(visibleAdministrativeUnitsQuery) > 0) {
			Record defaultAdministrativeUnitRecord = searchServices.search(visibleAdministrativeUnitsQuery).get(0);
			defaultAdministrativeUnit = rm.wrapAdministrativeUnit(defaultAdministrativeUnitRecord);
		} else {
			defaultAdministrativeUnit = null;
		}
		return defaultAdministrativeUnit;
	}

	private List<RecordVO> getRecordVOFromIds(List<String> ids, AvailableActionsParam param) {
		List<RecordVO> recordVOS = new ArrayList<>();
		RecordServices recordServices = recordServices();
		RecordToVOBuilder builder = new RecordToVOBuilder();
		for (String id : ids) {
			recordVOS.add(builder.build(recordServices.getDocumentById(id), RecordVO.VIEW_MODE.FORM, param.getView().getSessionContext()));
		}
		return recordVOS;
	}
}
