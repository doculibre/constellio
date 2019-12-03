package com.constellio.app.ui.framework.components.viewers.pdftron;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class GetAnnotationsOfOtherVersionWindowButton extends WindowButton {

	private ComboBox versionToPickFrom;
	private CopyAnnotationsOfOtherVersionPresenter copyAnnotationsOfOtherVersionPresenter;

	public GetAnnotationsOfOtherVersionWindowButton(
			CopyAnnotationsOfOtherVersionPresenter copyAnnotationsOfOtherVersionPresenter) {
		super($("getAnnotationsOfPreviousVersionWindowButton.btnTitle"), $("getAnnotationsOfPreviousVersionWindowButton.btnTitle"),
				new WindowConfiguration(true, true, "800px", "300px"));

		this.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		this.addStyleName(ValoTheme.BUTTON_LINK);

		this.copyAnnotationsOfOtherVersionPresenter = copyAnnotationsOfOtherVersionPresenter;
	}

	@Override
	protected Component buildWindowContent() {
		HorizontalLayout horizontalLayout = new HorizontalLayout();

		versionToPickFrom = new BaseComboBox();

		for (ContentVersionVO contentVersionVO : copyAnnotationsOfOtherVersionPresenter.getAvailableVersion()) {
			versionToPickFrom.addItem(contentVersionVO);
			versionToPickFrom.setItemCaption(contentVersionVO, contentVersionVO.toString());
		}

		versionToPickFrom.setCaption($("getAnnotationsOfPreviousVersionWindowButton.pickVersion"));

		horizontalLayout.addComponent(versionToPickFrom);

		Button okButton = new BaseButton($("Ok")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				ContentVersionVO selectedContentVersionVO = (ContentVersionVO) versionToPickFrom.getValue();

				if (selectedContentVersionVO == null) {
					Notification.show($(""), Type.WARNING_MESSAGE);
				} else {
					copyAnnotationsOfOtherVersionPresenter.addAnnotation(selectedContentVersionVO);
				}
			}
		};

		horizontalLayout.addComponent(okButton);

		return horizontalLayout;
	}
}
