package com.constellio.app.ui.pages.management.collections;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.data.CollectionVODataProvider.CollectionVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.collections.exceptions.NoMoreCollectionAvalibleException;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditCollectionViewImpl extends BaseViewImpl implements AddEditCollectionView {
	public static final String CODE_FIELD_STYLE = "seleniumCodeFieldStyle";
	public static final String NAME_FIELD_STYLE = "seleniumNameFieldStyle";
	public static final String BASE_FORM_STYLE = "seleniumBaseFormStyle";
	private AddEditCollectionPresenter presenter;

	private CollectionVO collectionVO;

	@PropertyId("code")
	private TextField code;
	@PropertyId("name")
	private TextField name;
	@PropertyId("conservationCalendarNumber")
	private TextField conservationCalendarNumber;
	@PropertyId("organizationNumber")
	private TextField organizationNumber;
	@PropertyId("modules")
	private OptionGroup modules;
	@PropertyId("supportedLanguages")
	private OptionGroup supportedLanguages;

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		setupParamsAndVO(event);
	}

	private void setupParamsAndVO(ViewChangeEvent event) {
		String parameters = event.getParameters();
		Map<String, String> paramsMap = ParamUtils.getParamsMap(parameters);
		String code = paramsMap.get("collectionCode");
		this.presenter = new AddEditCollectionPresenter(this, code);
		collectionVO = presenter.getCollectionVO();
	}

	@Override
	protected String getTitle() {
		return $("AddEditCollectionView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClick();
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		code = new BaseTextField($("AddEditCollectionView.code"));
		code.addStyleName(CODE_FIELD_STYLE);
		code.setRequired(true);
		code.setNullRepresentation("");
		code.setId("code");
		code.addStyleName("code");
		code.addStyleName("code-" + collectionVO.getCode());
		code.setEnabled(!presenter.getActionEdit());
		code.addValidator(new Validator() {
			@Override
			public void validate(Object value)
					throws InvalidValueException {
				if (code.getValue() != null && code.getValue().contains("-")) {
					throw new InvalidValueException($("AddEditCollectionView.validate.invalidCode"));
				}
			}
		});

		name = new BaseTextField($("AddEditCollectionView.name"));
		name.addStyleName(NAME_FIELD_STYLE);
		name.setNullRepresentation("");
		name.setId("name");
		name.addStyleName("name");
		name.addStyleName("name-" + collectionVO.getName());

		organizationNumber = new BaseTextField($("AddEditCollectionView.organizationNumber"));
		organizationNumber.setNullRepresentation("");
		organizationNumber.setId("organizationNumber");
		organizationNumber.setVisible(collectionVO.getModules() != null && collectionVO.getModules().contains(ConstellioRMModule.ID));

		conservationCalendarNumber = new BaseTextField($("AddEditCollectionView.conservationCalendarNumber"));
		conservationCalendarNumber.setNullRepresentation("");
		conservationCalendarNumber.setId("conservationCalendarNumber");
		conservationCalendarNumber.setVisible(collectionVO.getModules() != null && collectionVO.getModules().contains(ConstellioRMModule.ID));

		supportedLanguages = new OptionGroup($("AddEditCollectionView.language"));
		supportedLanguages.setMultiSelect(true);
		supportedLanguages.setRequired(true);
		for (String languageCode : presenter.getAllLanguages()) {
			supportedLanguages.addItem(languageCode);
			supportedLanguages.setItemEnabled(languageCode, presenter.isLanguageEnabled(languageCode));
			supportedLanguages.setItemCaption(languageCode, $("Language." + languageCode));
		}
		supportedLanguages.setEnabled(!presenter.getActionEdit());

		modules = new OptionGroup($("AddEditCollectionView.modules"));
		modules.setMultiSelect(true);
		for (String module : presenter.getAvailableModules()) {
			modules.addItem(module);
			modules.setItemEnabled(module, !presenter.isModuleSelected(module, collectionVO));
			modules.setItemCaption(module, presenter.getModuleCaption(module));
		}
		//modules.setEnabled(!presenter.getActionEdit());

		BaseForm<CollectionVO> baseFormComponent = new BaseForm<CollectionVO>(collectionVO, this, code, name, organizationNumber, conservationCalendarNumber,
				supportedLanguages, modules) {
			@Override
			protected void saveButtonClick(CollectionVO viewObject)
					throws ValidationException {
				try {
					presenter.saveButtonClicked(collectionVO);
				} catch (AddEditCollectionPresenterException addEditCollectionPresenterException) {
					showErrorMessage(addEditCollectionPresenterException.getMessage());
				} catch (NoMoreCollectionAvalibleException noMoreCollectionAvalible) {
					showErrorMessage($("com.constellio.app.ui.pages.management.collections.AddEditCollectionViewImpl_NoMoreCollectionAvalible"));
				}
			}

			@Override
			protected void cancelButtonClick(CollectionVO viewObject) {
				presenter.cancelButtonClicked();
			}

		};
		baseFormComponent.addStyleName(BASE_FORM_STYLE);
		return baseFormComponent;
	}
}
