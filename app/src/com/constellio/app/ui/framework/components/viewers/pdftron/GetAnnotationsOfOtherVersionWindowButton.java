package com.constellio.app.ui.framework.components.viewers.pdftron;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.model.services.pdf.pdtron.PdfTronXMLException.PdfTronXMLException_IOExeption;
import com.constellio.model.services.pdf.pdtron.PdfTronXMLException.PdfTronXMLException_XMLParsingException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static com.constellio.app.ui.i18n.i18n.$;

@Slf4j
public class GetAnnotationsOfOtherVersionWindowButton extends WindowButton {

	private ComboBox versionToPickFrom;
	private CopyAnnotationsOfOtherVersionPresenter copyAnnotationsOfOtherVersionPresenter;
	private Refresh refresh;

	public GetAnnotationsOfOtherVersionWindowButton(
			CopyAnnotationsOfOtherVersionPresenter copyAnnotationsOfOtherVersionPresenter, Refresh refresh) {
		super($("getAnnotationsOfPreviousVersionWindowButton.btnTitle"), $("getAnnotationsOfPreviousVersionWindowButton.btnTitle"),
				new WindowConfiguration(true, true, "490px", "125px"));
		this.refresh = refresh;
		this.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		this.addStyleName(ValoTheme.BUTTON_LINK);

		this.copyAnnotationsOfOtherVersionPresenter = copyAnnotationsOfOtherVersionPresenter;
	}

	@Override
	protected Component buildWindowContent() {
		HorizontalLayout horizontalLayout = new HorizontalLayout();

		horizontalLayout.setSpacing(true);

		versionToPickFrom = new BaseComboBox();

		versionToPickFrom.setWidth("350px");

		for (ContentVersionVO contentVersionVO : copyAnnotationsOfOtherVersionPresenter.getAvailableVersion()) {
			versionToPickFrom.addItem(contentVersionVO);
			versionToPickFrom.setItemCaption(contentVersionVO, contentVersionVO.toString());
		}

		versionToPickFrom.setCaption($("getAnnotationsOfPreviousVersionWindowButton.pickVersion"));

		horizontalLayout.addComponent(versionToPickFrom);


		Button okButton = new BaseButton($("getAnnotationsOfPreviousVersionWindowButton.transfer")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				ContentVersionVO selectedContentVersionVO = (ContentVersionVO) versionToPickFrom.getValue();

				if (selectedContentVersionVO == null) {
					Notification.show($("getAnnotationsOfPreviousVersionWindowButton.selectAVersion"), Type.WARNING_MESSAGE);
				} else {
					try {
						copyAnnotationsOfOtherVersionPresenter.addAnnotation(selectedContentVersionVO);
						GetAnnotationsOfOtherVersionWindowButton.this.refresh.refresh();
						GetAnnotationsOfOtherVersionWindowButton.this.getWindow().close();
					} catch (PdfTronXMLException_IOExeption e) {
						log.error("annotation transfer failed." + getCurrentContentInfo(selectedContentVersionVO), e);
						Notification.show($("getAnnotationsOfPreviousVersionWindowButton.unexpectedError"));
					} catch (PdfTronXMLException_XMLParsingException e) {
						log.error("annotation transfer failed." + getCurrentContentInfo(selectedContentVersionVO), e);
						Notification.show(($("getAnnotationsOfPreviousVersionWindowButton.unexpectedError")));
					}
				}
			}
		};

		horizontalLayout.addComponent(okButton);
		horizontalLayout.setComponentAlignment(okButton, Alignment.BOTTOM_RIGHT);

		return horizontalLayout;
	}

	@NotNull
	private String getCurrentContentInfo(ContentVersionVO contentVersionVO) {
		return " Hash : " + copyAnnotationsOfOtherVersionPresenter.getHash()
			   + " recordId : " + copyAnnotationsOfOtherVersionPresenter.getRecordId()
			   + " version : " + copyAnnotationsOfOtherVersionPresenter.getVersion()
			   + " contentId of content being merged : " + contentVersionVO.getContentId();
	}
}
