package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.joda.time.LocalDate;

import static com.constellio.app.ui.i18n.i18n.$;

public class PublishDocumentViewImpl extends BaseViewImpl implements PublishDocumentView {

	private final PublishDocumentPresenter presenter;
	private RecordVO record;

	@PropertyId("publishStartDate") private JodaDateField publishStartDate;
	@PropertyId("publishEndDate") private JodaDateField publishEndDate;

	public PublishDocumentViewImpl() {
		presenter = new PublishDocumentPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		record = presenter.forRequestParams(event.getParameters()).getRecordVO();
	}

	@Override
	protected String getTitle() {
		return $("PublishDocumentView.viewTitle", record.getTitle());
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				returnFromPage();
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		buildDateFields();
		return new BaseForm<DocumentVO>(
				new DocumentVO(record), this, publishStartDate, publishEndDate) {

			@Override
			protected String getSaveButtonCaption() {
				return $("DocumentContextMenu.publish");
			}

			@Override
			protected void saveButtonClick(DocumentVO documentVO){
				try {
					Document document = presenter.publishDocument(record.getId(), publishStartDate.getValue(), publishEndDate.getValue());
					returnFromPage();
					linkToDocument(document);
				} catch (RecordServicesException e) {
					returnFromPage();
				}
			}

			@Override
			protected void cancelButtonClick(DocumentVO documentVO) {
				returnFromPage();
			}
		};
	}

	@Override
	public void returnFromPage() {
		presenter.backButtonClicked();
	}

	private void buildDateFields() {
		publishStartDate = new JodaDateField();
		publishStartDate.setCaption($("AuthorizationsView.startDate"));
		publishStartDate.setId("startDate");

		publishEndDate = new JodaDateField();
		publishEndDate.setCaption($("AuthorizationsView.endDate"));
		publishEndDate.setId("endDate");
	}

	public void linkToDocument(Document document) {
		WindowButton.WindowConfiguration publicLinkConfig = new WindowConfiguration(true, false, "75%", "125px");
		WindowButton publicLinkButton = new WindowButton(
				$("DocumentContextMenu.publicLink"), $("DocumentContextMenu.publicLink"), publicLinkConfig) {
			@Override
			protected Component buildWindowContent() {
				String url = presenter.getConstellioUrl();
				String publicLink = url + "dl?id=" + document.getId();

				Label link = new Label(publicLink);
				Label message = new Label($("DocumentContextMenu.publicLinkInfo"));
				message.addStyleName(ValoTheme.LABEL_BOLD);
				return new VerticalLayout(message, link);
			}
		};
		publicLinkButton.click();
	}
}