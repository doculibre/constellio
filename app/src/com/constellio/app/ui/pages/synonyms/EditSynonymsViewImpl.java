package com.constellio.app.ui.pages.synonyms;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.searchConfig.SearchConfigurationViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class EditSynonymsViewImpl extends BaseViewImpl implements EditSynonymsView {

    public static final String BUTTONS_LAYOUT = "base-form-buttons-layout";

    public static final String SAVE_BUTTON = "base-form-save";

    public static final String CANCEL_BUTTON = "base-form_cancel";
    
    private String synonymsText;

    @PropertyId("synonymsText")
    private TextArea textArea;
    
    EditSynonymsPresenter presenter;
    
    public EditSynonymsViewImpl() {
        presenter = new EditSynonymsPresenter(this);
    }

    @Override
    protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
        return SearchConfigurationViewImpl.getSearchConfigurationBreadCrumbTrail(this, getTitle());
    }

    public String getSynonymsText() {
		return synonymsText;
	}

	public void setSynonymsText(String synonymsText) {
		this.synonymsText = synonymsText;
	}

	@Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		synonymsText = presenter.getSynonmsAsOneString();
        this.textArea = new BaseTextArea();
        this.textArea.setHeight("600px");
        this.textArea.setWidth("95%");

        BaseForm<EditSynonymsViewImpl> baseForm = new BaseForm<EditSynonymsViewImpl>(this, this, textArea) {
			@Override
			protected void saveButtonClick(EditSynonymsViewImpl viewObject) throws ValidationException {
                presenter.saveButtonClicked(textArea.getValue());
	        }
			
			@Override
			protected void cancelButtonClick(EditSynonymsViewImpl viewObject) {
                presenter.cancelButtonClicked();
			}
		};
		baseForm.setSizeFull();
		
		return baseForm;
    }


    protected String getSaveButtonCaption() {
        return $("save");
    }

    protected String getCancelButtonCaption() {
        return $("cancel");
    }

    @Override
    protected String getTitle() {
        return $("EditViewSynonymsView.title");
    }

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}
	
}
