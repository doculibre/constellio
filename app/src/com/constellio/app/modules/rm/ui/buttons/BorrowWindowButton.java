package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.services.menu.behaviors.RMRecordsMenuItemBehaviors;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.roles.Roles;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

@Slf4j
public class BorrowWindowButton extends WindowButton {

	private final RMConfigs rmConfigs;
	private MenuItemActionBehaviorParams params;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private RecordServices recordServices;
	private String collection;
	private List<Record> records;
	private BorrowingServices borrowingServices;
	private MetadataSchemaTypes schemaTypes;

	public BorrowWindowButton(List<Record> records, MenuItemActionBehaviorParams params) {
		super($("$(DisplayFolderView.borrow"), $("DisplayFolderView.borrow"));

		this.params = params;
		this.appLayerFactory = params.getView().getConstellioFactories().getAppLayerFactory();
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.collection = params.getView().getSessionContext().getCurrentCollection();
		this.rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		this.borrowingServices = new BorrowingServices(collection, modelLayerFactory);
		this.schemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		this.records = records;

	}

	@Override
	protected Component buildWindowContent() {
		final JodaDateField borrowDatefield = new JodaDateField();
		borrowDatefield.setCaption($("DisplayFolderView.borrowDate"));
		borrowDatefield.setRequired(true);
		borrowDatefield.setId("borrowDate");
		borrowDatefield.addStyleName("borrowDate");
		borrowDatefield.setValue(TimeProvider.getLocalDate().toDate());

		final Field<?> lookupUser = new LookupRecordField(User.SCHEMA_TYPE);
		lookupUser.setCaption($("DisplayFolderView.borrower"));
		lookupUser.setId("borrower");
		lookupUser.addStyleName("user-lookup");
		lookupUser.setRequired(true);

		final ComboBox borrowingTypeField = new BaseComboBox();
		borrowingTypeField.setCaption($("DisplayFolderView.borrowingType"));
		for (BorrowingType borrowingType : BorrowingType.values()) {
			borrowingTypeField.addItem(borrowingType);
			borrowingTypeField
					.setItemCaption(borrowingType, $("DisplayFolderView.borrowingType." + borrowingType.getCode()));
		}
		borrowingTypeField.setRequired(true);
		borrowingTypeField.setNullSelectionAllowed(false);

		final JodaDateField previewReturnDatefield = new JodaDateField();
		previewReturnDatefield.setCaption($("DisplayFolderView.previewReturnDate"));
		previewReturnDatefield.setRequired(true);
		previewReturnDatefield.setId("previewReturnDate");
		previewReturnDatefield.addStyleName("previewReturnDate");

		final JodaDateField returnDatefield = new JodaDateField();
		returnDatefield.setCaption($("DisplayFolderView.returnDate"));
		returnDatefield.setRequired(false);
		returnDatefield.setId("returnDate");
		returnDatefield.addStyleName("returnDate");

		borrowDatefield.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				previewReturnDatefield.setValue(
						getPreviewReturnDate(borrowDatefield.getValue(), borrowingTypeField.getValue()));
			}
		});
		borrowingTypeField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				previewReturnDatefield.setValue(
						getPreviewReturnDate(borrowDatefield.getValue(), borrowingTypeField.getValue()));
			}
		});

		BaseButton borrowButton = new BaseButton($("DisplayFolderView.borrow")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				String userId = null;
				BorrowingType borrowingType = null;
				if (lookupUser.getValue() != null) {
					userId = (String) lookupUser.getValue();
				}
				if (borrowingTypeField.getValue() != null) {
					borrowingType = BorrowingType.valueOf(borrowingTypeField.getValue().toString());
				}
				LocalDate borrowLocalDate = null;
				LocalDate previewReturnLocalDate = null;
				LocalDate returnLocalDate = null;
				if (borrowDatefield.getValue() != null) {
					borrowLocalDate = LocalDate.fromDateFields(borrowDatefield.getValue());
				}
				if (previewReturnDatefield.getValue() != null) {
					previewReturnLocalDate = LocalDate.fromDateFields(previewReturnDatefield.getValue());
				}
				if (returnDatefield.getValue() != null) {
					returnLocalDate = LocalDate.fromDateFields(returnDatefield.getValue());
				}
				if (borrowRecords(borrowLocalDate, previewReturnLocalDate, userId, borrowingType, returnLocalDate)) {
					getWindow().close();
				}
			}
		};
		borrowButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		BaseButton cancelButton = new BaseButton($("cancel")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				getWindow().close();
			}
		};
		cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.addComponents(borrowButton, cancelButton);

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout
				.addComponents(borrowDatefield, borrowingTypeField, lookupUser, previewReturnDatefield, returnDatefield,
						horizontalLayout);
		verticalLayout.setSpacing(true);
		verticalLayout.addStyleName("no-scroll");

		return verticalLayout;
	}

	private boolean borrowRecords(LocalDate borrowingDate, LocalDate previewReturnDate, String userId,
								  BorrowingType borrowingType, LocalDate returnDate) {
		boolean areRecordsFolders = records.get(0).isOfSchemaType(Folder.SCHEMA_TYPE);
		boolean borrowed;
		String errorMessage = borrowingServices
				.validateBorrowingInfos(userId, borrowingDate, previewReturnDate, borrowingType, returnDate);
		if (errorMessage != null) {
			params.getView().showErrorMessage($(errorMessage));
			borrowed = false;
		} else {
			User borrowerEntered = wrapUser(recordServices.getDocumentById(userId));

			try {
				if (areRecordsFolders) {
					borrowingServices.borrowFolders(records, borrowingDate, previewReturnDate,
							params.getUser(), borrowerEntered, borrowingType, true);
				} else {
					borrowingServices.borrowContainers(records, borrowingDate, previewReturnDate,
							params.getUser(), borrowerEntered, borrowingType, true);
				}

				params.getView().refreshActionMenu();
				params.getView().showMessage($(areRecordsFolders ? "DisplayFolderView.multipleCheckOut"
																 : "DisplayContainerView.multipleCheckOut"));
				borrowed = true;
			} catch (RecordServicesException e) {
				log.error(e.getMessage(), e);
				params.getView().showErrorMessage($(areRecordsFolders ? "DisplayFolderView.cannotBorrowMultipleFolder"
																	  : "DisplayContainerView.cannotBorrowMultipleContainer"));
				borrowed = false;
			}
		}

		if (returnDate != null) {
			return new RMRecordsMenuItemBehaviors(collection, appLayerFactory).returnRecords(records, returnDate, params, areRecordsFolders);
		}

		return borrowed;
	}

	private Date getPreviewReturnDate(Date borrowDate, Object borrowingTypeValue) {
		BorrowingType borrowingType;
		Date previewReturnDate = TimeProvider.getLocalDate().toDate();
		if (borrowDate != null && borrowingTypeValue != null) {
			borrowingType = (BorrowingType) borrowingTypeValue;
			if (borrowingType == BorrowingType.BORROW) {
				int addDays = rmConfigs.getFolderBorrowingDurationDays();
				previewReturnDate = LocalDate.fromDateFields(borrowDate).plusDays(addDays).toDate();
			} else {
				previewReturnDate = borrowDate;
			}
		}
		return previewReturnDate;
	}

	private User wrapUser(Record record) {
		return new User(record, schemaTypes, getCollectionRoles());
	}

	private Roles getCollectionRoles() {
		return modelLayerFactory.getRolesManager().getCollectionRoles(collection);
	}
}