package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.ui.util.ViewErrorDisplay;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.google.common.base.Strings;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditTaxonomyViewImpl extends BaseViewImpl implements AddEditTaxonomyView {
	public static final String FOLDER = "folderObject";
	public static final String DOCUMENT = "documentObject";

	private AddEditTaxonomyPresenter presenter;

	private TaxonomyVO taxonomyVO;

	private HashMap<Language, BaseTextField> baseTextFieldTitleMap = null;
	private HashMap<Language, BaseTextField> baseTextFieldAbbreviationMap = null;

	private String originalStyleName;

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

	private void showExistingError(List<Language> valueLanguageError) {
		StringBuilder errorMessage = new StringBuilder();
		int i = 0;
		for (Language language : valueLanguageError) {
			if (i != 0) {
				errorMessage.append("<br/>");
			}
			errorMessage.append($("AddEditTaxonomyView.taxonomyTitleAlreadyExist", " (" + language.getCode().toUpperCase() + ")"));
			i++;
		}

		this.showErrorMessage(errorMessage.toString());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		baseTextFieldTitleMap = new HashMap<>();
		baseTextFieldAbbreviationMap = new HashMap<>();

		for (String languageCode : presenter.getCollectionLanguage()) {
			BaseTextField titleBaseTextField = new BaseTextField();
			BaseTextField abbreviationBaseTextField = new BaseTextField();

			int numberOfLanguage = getConstellioFactories().getAppLayerFactory().getCollectionsManager().getCollectionLanguages(getCollection()).size();

			if (numberOfLanguage > 1) {
				titleBaseTextField.setCaption($("title") + " (" + languageCode.toUpperCase() + ")");
				abbreviationBaseTextField.setCaption($("abbreviation") + " (" + languageCode.toUpperCase() + ")");
			} else {
				titleBaseTextField.setCaption($("title"));
				abbreviationBaseTextField.setCaption($("abbreviation"));
			}

			titleBaseTextField.setRequired(true);
			if (presenter.isActionEdit()) {
				titleBaseTextField.setValue(taxonomyVO.getTitle(Language.withCode(languageCode)));
				abbreviationBaseTextField.setValue(taxonomyVO.getAbbreviation(Language.withCode(languageCode)));
			}

			titleBaseTextField.addStyleName("title");
			baseTextFieldTitleMap.put(Language.withCode(languageCode), titleBaseTextField);

			abbreviationBaseTextField.addStyleName("title");
			baseTextFieldAbbreviationMap.put(Language.withCode(languageCode), abbreviationBaseTextField);
		}


		final AbstractField[] fieldArray =
				new AbstractField[1 + baseTextFieldTitleMap.size() + baseTextFieldAbbreviationMap.size() + 4];

		final CheckBox isMultiLingualCheckBox = new CheckBox($("ListValueDomainViewImpl.multilingual"));
		isMultiLingualCheckBox.setValue(true);
		isMultiLingualCheckBox.setVisible(presenter.getCollectionLanguage().size() > 1);
		isMultiLingualCheckBox.setId("multilingual");
		isMultiLingualCheckBox.setVisible(!presenter.isActionEdit() && presenter.getCollectionLanguage().size() > 1 && Toggle.MULTI_LINGUAL.isEnabled());
		Language currentLanguage = Language.withCode(getSessionContext().getCurrentLocale().getLanguage());

		fieldArray[0] = isMultiLingualCheckBox;
		fieldArray[1] = baseTextFieldTitleMap.get(currentLanguage);

		int i = 2;
		for (Language language : baseTextFieldTitleMap.keySet()) {
			if (currentLanguage.getCode().equals(language.getCode())) {
				continue;
			} else {
				fieldArray[i] = baseTextFieldTitleMap.get(language);
				i++;
			}
		}

		fieldArray[i++] = baseTextFieldAbbreviationMap.get(currentLanguage);
		for (Language language : baseTextFieldAbbreviationMap.keySet()) {
			if (currentLanguage.getCode().equals(language.getCode())) {
				continue;
			} else {
				fieldArray[i] = baseTextFieldAbbreviationMap.get(language);
				i++;
			}
		}

		userIdsField = new ListAddRemoveRecordLookupField(User.SCHEMA_TYPE);
		userIdsField.setCaption($("AddEditTaxonomyView.users"));
		userIdsField.setRequired(false);
		userIdsField.setId("userIds");
		userIdsField.addStyleName("userIds");

		fieldArray[1 + baseTextFieldTitleMap.size() + baseTextFieldAbbreviationMap.size()] = userIdsField;

		groupIdsField = new ListAddRemoveRecordLookupField(Group.SCHEMA_TYPE);
		groupIdsField.setCaption($("AddEditTaxonomyView.groups"));
		groupIdsField.setRequired(false);
		groupIdsField.setId("groupIds");
		groupIdsField.addStyleName("groupIds");

		fieldArray[1 + baseTextFieldTitleMap.size() + baseTextFieldAbbreviationMap.size() + 1] = groupIdsField;

		visibleInHomePageField = new CheckBox($("AddEditTaxonomyView.visibleInHomePageField"));
		visibleInHomePageField.setCaption($("AddEditTaxonomyView.visibleInHomePageField"));
		visibleInHomePageField.setRequired(false);
		visibleInHomePageField.setId("visibleInHomePageField");
		visibleInHomePageField.addStyleName("visibleInHomePageField");

		fieldArray[1 + baseTextFieldTitleMap.size() + baseTextFieldAbbreviationMap.size() + 2] = visibleInHomePageField;

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

		fieldArray[1 + baseTextFieldTitleMap.size() + baseTextFieldAbbreviationMap.size() + 3] = classifiedObjectsField;

		BaseForm<TaxonomyVO> baseForm = new BaseForm<TaxonomyVO>(taxonomyVO, this, fieldArray) {
			@Override
			protected void saveButtonClick(TaxonomyVO taxonomyVO)
					throws ValidationException {

				Map<Language, String> titleMap = new HashMap<>();
				for (Language language : baseTextFieldTitleMap.keySet()) {
					BaseTextField baseTextField = baseTextFieldTitleMap.get(language);
					String value = baseTextField.getValue();
					titleMap.put(language, value);
				}

				taxonomyVO.setTitle(titleMap);
				if (!ViewErrorDisplay.validateFieldsContent(baseTextFieldTitleMap, AddEditTaxonomyViewImpl.this)) {
					return;
				}

				Map<Language, String> abbreviationMap = new HashMap<>();
				for (Language language : baseTextFieldAbbreviationMap.keySet()) {
					BaseTextField baseTextField = baseTextFieldAbbreviationMap.get(language);
					String value = baseTextField.getValue();
					if (!Strings.isNullOrEmpty(value)) {
						abbreviationMap.put(language, value);
					}
				}
				taxonomyVO.setAbbreviation(abbreviationMap);

				boolean isMultiValue = false;

				if (isMultiLingualCheckBox.isVisible()) {
					isMultiValue = isMultiLingualCheckBox.getValue();
				}

				List<Language> languageList = presenter.saveButtonClicked(taxonomyVO, isMultiValue);
				if (languageList.size() > 0) {
					showExistingError(languageList);
					ViewErrorDisplay.setFieldErrors(languageList, baseTextFieldTitleMap, originalStyleName);
				}
			}

			@Override
			protected void cancelButtonClick(TaxonomyVO taxonomyVO) {
				presenter.cancelButtonClicked();
			}
		};

		originalStyleName = fieldArray[0].getStyleName();
		return baseForm;
	}
}
