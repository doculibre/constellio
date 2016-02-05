package com.constellio.app.ui.pages.management.taxonomy;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Map;

import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

public class AddEditTaxonomyViewImpl extends BaseViewImpl implements AddEditTaxonomyView {
	public static final String FOLDER = "folderObject";
	public static final String DOCUMENT = "documentObject";

	private AddEditTaxonomyPresenter presenter;

	private TaxonomyVO taxonomyVO;

	@PropertyId("title")
	private TextField titleField;

	@PropertyId("userIds")
	private ListAddRemoveRecordLookupField userIdsField;

	@PropertyId("groupIds")
	private ListAddRemoveRecordLookupField groupIdsField;

	@PropertyId("visibleInHomePage")
	private CheckBox visibleInHomePageField;

	@PropertyId("classifiedObjects")
	private ListOptionGroup classifiedObjectsField;

	public AddEditTaxonomyViewImpl() {
		this.presenter = new AddEditTaxonomyPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		setupParamsAndVO(event);
	}

	private void setupParamsAndVO(ViewChangeEvent event) {
		Map<String, String> params = ParamUtils.getParamsMap(event.getParameters());
		String taxonomyCode = params.get("taxonomyCode");

		if (taxonomyCode != null) {
			presenter.setEditAction(true);

			Taxonomy taxonomy = presenter.fetchTaxonomy(taxonomyCode);
			taxonomyVO = presenter.newTaxonomyVO(taxonomy);
		} else {
			taxonomyVO = new TaxonomyVO();
		}
	}

	@Override
	protected String getTitle() {
		return $("AddEditTaxonomyView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		titleField = new TextField();
		titleField.setCaption($("AddEditTaxonomyView.title"));
		titleField.setRequired(true);
		titleField.setNullRepresentation("");
		titleField.setId("title");
		titleField.addStyleName("title");

		userIdsField = new ListAddRemoveRecordLookupField(User.SCHEMA_TYPE);
		userIdsField.setCaption($("AddEditTaxonomyView.users"));
		userIdsField.setRequired(false);
		userIdsField.setId("userIds");
		userIdsField.addStyleName("userIds");

		groupIdsField = new ListAddRemoveRecordLookupField(Group.SCHEMA_TYPE);
		groupIdsField.setCaption($("AddEditTaxonomyView.groups"));
		groupIdsField.setRequired(false);
		groupIdsField.setId("groupIds");
		groupIdsField.addStyleName("groupIds");

		visibleInHomePageField = new CheckBox($("AddEditTaxonomyView.visibleInHomePageField"));
		visibleInHomePageField.setCaption($("AddEditTaxonomyView.visibleInHomePageField"));
		visibleInHomePageField.setRequired(false);
		visibleInHomePageField.setId("visibleInHomePageField");
		visibleInHomePageField.addStyleName("visibleInHomePageField");

		classifiedObjectsField = new ListOptionGroup($("AddEditTaxonomyView.classifiedObjectsField"));
		classifiedObjectsField.setEnabled(presenter.canEditClassifiedObjects(taxonomyVO));
		classifiedObjectsField.setCaption($("AddEditTaxonomyView.classifiedObjectsField"));
		classifiedObjectsField.setRequired(false);
		classifiedObjectsField.setMultiSelect(true);
		classifiedObjectsField.setId("classifiedObjects");
		classifiedObjectsField.addStyleName("classifiedObjects");
		classifiedObjectsField.addItem(FOLDER);
		classifiedObjectsField.setItemCaption(FOLDER, $("AddEditTaxonomyView.classifiedObject.folder"));
		classifiedObjectsField.addItem(DOCUMENT);
		classifiedObjectsField.setItemCaption(DOCUMENT, $("AddEditTaxonomyView.classifiedObject.document"));

		return new BaseForm<TaxonomyVO>(taxonomyVO, this, titleField, userIdsField, groupIdsField, visibleInHomePageField,
				classifiedObjectsField) {
			@Override
			protected void saveButtonClick(TaxonomyVO taxonomyVO)
					throws ValidationException {
				presenter.saveButtonClicked(taxonomyVO);
			}

			@Override
			protected void cancelButtonClick(TaxonomyVO taxonomyVO) {
				presenter.cancelButtonClicked();
			}
		};
	}
}
