package com.constellio.app.ui.pages.management.extractors;

import com.constellio.app.ui.entities.MetadataExtractorVO;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveTextField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.extractors.fields.ListAddRemoveRegexConfigField;
import com.constellio.model.entities.Language;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditMetadataExtractorViewImpl extends BaseViewImpl implements AddEditMetadataExtractorView {

	private MetadataExtractorVO metadataExtractorVO;

	private List<MetadataSchemaTypeVO> schemaTypeOptions = new ArrayList<>();

	private boolean schemaTypeFieldVisible;

	private boolean schemaFieldVisible;

	private boolean metadataFieldEnabled;

	private BaseForm<MetadataExtractorVO> baseForm;

	private ComboBox schemaTypeField;

	private ComboBox schemaField;

	@PropertyId("metadataVO")
	private ComboBox metadataField;

	@PropertyId("styles")
	private ListAddRemoveTextField stylesField;

	@PropertyId("properties")
	private ListAddRemoveTextField propertiesField;

	@PropertyId("regexes")
	private ListAddRemoveRegexConfigField regexesField;

	@PropertyId("isAddOnly")
	private CheckBox isAddOnly;

	private AddEditMetadataExtractorPresenter presenter;

	public AddEditMetadataExtractorViewImpl() {
		this.presenter = new AddEditMetadataExtractorPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.afterViewAssembled();
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		schemaTypeField = new ComboBox($("AddEditMetadataExtractorView.schemaType")) {
			@Override
			public String getItemCaption(Object itemId) {
				MetadataSchemaTypeVO schemaTypeVO = (MetadataSchemaTypeVO) itemId;
				return schemaTypeVO.getLabel(Language.withCode(getLocale().getLanguage()));
			}
		};
		schemaTypeField.addItems(schemaTypeOptions);
		schemaTypeField.setVisible(schemaTypeFieldVisible);
		schemaTypeField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				MetadataSchemaTypeVO schemaTypeVO = (MetadataSchemaTypeVO) event.getProperty().getValue();
				presenter.schemaTypeSelected(schemaTypeVO);
			}
		});
		schemaTypeField.setNullSelectionAllowed(false);

		schemaField = new ComboBox($("AddEditMetadataExtractorView.schema")) {
			@Override
			public String getItemCaption(Object itemId) {
				MetadataSchemaVO schemaVO = (MetadataSchemaVO) itemId;
				return schemaVO.getLabel(getLocale());
			}
		};
		schemaField.setVisible(schemaFieldVisible);
		schemaField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				MetadataSchemaVO schemaVO = (MetadataSchemaVO) event.getProperty().getValue();
				presenter.schemaSelected(schemaVO);
			}
		});
		schemaField.setNullSelectionAllowed(false);

		metadataField = new ComboBox($("AddEditMetadataExtractorView.metadata")) {
			@Override
			public String getItemCaption(Object itemId) {
				MetadataVO metadataVO = (MetadataVO) itemId;
				return metadataVO.getLabel(getLocale());
			}
		};
		metadataField.setEnabled(metadataFieldEnabled);
		if (metadataExtractorVO.getMetadataVO() != null) {
			metadataField.addItem(metadataExtractorVO.getMetadataVO());
		}
		metadataField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if (metadataField.getValue() != null && schemaField.getValue() != null) {
					MetadataSchemaVO schemaVO = (MetadataSchemaVO) schemaField.getValue();
					presenter.setMetadataVOsForRegexes(schemaVO.getCode(), (MetadataVO) metadataField.getValue());
				}
			}
		});
		metadataField.setNullSelectionAllowed(false);

		stylesField = new ListAddRemoveTextField();
		stylesField.setCaption($("AddEditMetadataExtractorView.styles"));

		propertiesField = new ListAddRemoveTextField();
		propertiesField.setCaption($("AddEditMetadataExtractorView.properties"));

		regexesField = new ListAddRemoveRegexConfigField();
		regexesField.setCaption($("AddEditMetadataExtractorView.regexes"));

		isAddOnly = new CheckBox();
		isAddOnly.setCaption($("AddEditMetadataExtractorView.isAddOnly"));

		baseForm = new BaseForm<MetadataExtractorVO>(metadataExtractorVO, this, schemaTypeField, schemaField,
				metadataField,
				stylesField, propertiesField, regexesField, isAddOnly) {
			@Override
			protected void saveButtonClick(MetadataExtractorVO viewObject)
					throws ValidationException {
				presenter.saveButtonClicked();
			}

			@Override
			protected void cancelButtonClick(MetadataExtractorVO viewObject) {
				presenter.cancelButtonClicked();
			}

		};

		return baseForm;
	}

	@Override
	protected String getTitle() {
		return $("AddEditMetadataExtractorView.viewTitle");
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
	public void setMetadataExtractorVO(MetadataExtractorVO metadataExtractorVO) {
		this.metadataExtractorVO = metadataExtractorVO;
	}

	@Override
	public void setSchemaTypeOptions(List<MetadataSchemaTypeVO> schemaTypeVOs) {
		this.schemaTypeOptions = schemaTypeVOs;
	}

	@Override
	public void setSchemaOptions(List<MetadataSchemaVO> schemaVOs) {
		schemaField.removeAllItems();
		schemaField.addItems(schemaVOs);
	}

	@Override
	public void setMetadataOptions(List<MetadataVO> metadataVOs) {
		metadataField.removeAllItems();
		metadataField.addItems(metadataVOs);
	}

	@Override
	public void setRegexMetadataOptions(List<MetadataVO> metadataVOs) {
		regexesField.setMetadataOptions(metadataVOs);
	}

	@Override
	public void setSchemaTypeFieldVisible(boolean visible) {
		this.schemaTypeFieldVisible = visible;
	}

	@Override
	public void setSchemaFieldVisible(boolean visible) {
		this.schemaFieldVisible = visible;
	}

	@Override
	public void setMetadataFieldEnabled(boolean enabled) {
		this.metadataFieldEnabled = enabled;
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		ArrayList<Button> buttons = new ArrayList<>();
		WindowButton analyserButton = new WindowButton($("PropertiesAnalyserView.viewTitle"), $("PropertiesAnalyserView.viewTitle"),
				WindowConfiguration.modalDialog("80%", "80%")) {
			@Override
			protected Component buildWindowContent() {
				PropertiesAnalyserViewImpl view = new PropertiesAnalyserViewImpl() {
					@Override
					protected void handlePropertyClick(ItemClickEvent event) {
						super.handlePropertyClick(event);
						propertiesField.addValue((String) event.getItemId());
						showMessage($("AddEditMetadataExtractorView.propertyAdded"));
					}

					@Override
					protected void handleStyleClick(ItemClickEvent event) {
						super.handleStyleClick(event);
						stylesField.addValue((String) event.getItemId());
						showMessage($("AddEditMetadataExtractorView.styleAdded"));
					}

					@Override
					protected boolean isBreadcrumbsVisible() {
						return false;
					}
				};
				DragAndDropWrapper wrapper = new DragAndDropWrapper(view) {
					@Override
					public DropHandler getDropHandler() {
						return view;
					}
				};
				view.enter(null);
				return wrapper;
			}
		};
		buttons.add(analyserButton);
		return buttons;
	}
}
