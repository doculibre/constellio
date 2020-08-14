package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.users.UserServices;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class CollectionsSelectWindowButton extends WindowButton {

	private SchemasRecordsServices core;
	private MenuItemActionBehaviorParams params;
	private AppLayerFactory appLayerFactory;
	private RecordServices recordServices;
	private UserServices userServices;
	private String collection;
	private List<Record> records;
	private AddedToCollectionRecordType addedRecordType;
	final OptionGroup collectionsField;

	public void addToCollections() {
		click();
	}

	public enum AddedToCollectionRecordType {
		USER, GROUP
	}


	public CollectionsSelectWindowButton(String title, List<Record> records, MenuItemActionBehaviorParams params) {
		super($("CollectionSecurityManagement.selectCollections"), title);

		this.params = params;
		this.appLayerFactory = params.getView().getConstellioFactories().getAppLayerFactory();
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		this.collection = params.getView().getSessionContext().getCurrentCollection();
		this.core = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		this.records = records;
		this.collectionsField = new OptionGroup($("CollectionSecurityManagement.selectCollections"));
	}

	public List<Record> getRecords() {
		return records;
	}

	public List<String> getSelectedValues() {
		return new ArrayList<String>((java.util.Collection<? extends String>) collectionsField.getValue());
	}

	public SchemasRecordsServices getCore() {
		return core;
	}

	@Override
	protected Component buildWindowContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setMargin(new MarginInfo(true, true, false, true));
		mainLayout.setSizeFull();

		HorizontalLayout collectionLayout = new HorizontalLayout();
		collectionLayout.setSpacing(true);

		collectionsField.addStyleName("collections");
		collectionsField.addStyleName("collections-username");
		collectionsField.setId("collections");
		collectionsField.setMultiSelect(true);
		//collectionsField.addStyleName("horizontal");
		collectionLayout.addComponent(collectionsField);
		for (String collection : appLayerFactory.getCollectionsManager().getCollectionCodes()) {
			if (!Collection.SYSTEM_COLLECTION.equals(collection)) {
				collectionsField.addItem(appLayerFactory.getCollectionsManager().getCollection(collection).getTitle());
			}
		}

		mainLayout.addComponents(collectionLayout);
		BaseButton saveButton;
		BaseButton cancelButton;
		HorizontalLayout buttonLayout = new HorizontalLayout();

		buttonLayout.addComponent(saveButton = new BaseButton($("save")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				try {
					saveButtonClick(params.getView());
					getWindow().close();
				} catch (Exception e) {
					e.printStackTrace();
					params.getView().showErrorMessage(MessageUtils.toMessage(e));
				}
			}
		});
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		buttonLayout.addComponent(cancelButton = new BaseButton($("cancel")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				getWindow().close();
			}
		});


		buttonLayout.setSpacing(true);
		mainLayout.addComponent(buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
		mainLayout.setHeight("100%");
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);


		return mainLayout;
	}

	protected abstract void saveButtonClick(BaseView baseView);
}
