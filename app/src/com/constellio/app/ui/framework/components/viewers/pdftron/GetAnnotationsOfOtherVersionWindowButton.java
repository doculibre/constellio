package com.constellio.app.ui.framework.components.viewers.pdftron;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import static com.constellio.app.ui.i18n.i18n.$;

public class GetAnnotationsOfOtherVersionWindowButton extends WindowButton {

	private ComboBox versionToPickFrom;
	private CopyAnnotationsOfOtherVersionPresenter copyAnnotationsOfOtherVersionPresenter;

	public GetAnnotationsOfOtherVersionWindowButton(
			CopyAnnotationsOfOtherVersionPresenter copyAnnotationsOfOtherVersionPresenter) {
		super($("getAnnotationsOfPreviousVersionWindowButton.btnTitle"), $("getAnnotationsOfPreviousVersionWindowButton.btnTitle"),
				new WindowConfiguration(true, true, "800px", "300px"));

		this.copyAnnotationsOfOtherVersionPresenter = copyAnnotationsOfOtherVersionPresenter;

	}

	@Override
	protected Component buildWindowContent() {
		HorizontalLayout horizontalLayout = new HorizontalLayout();

		versionToPickFrom = new BaseComboBox();

		for (ContentVersionVO contentVersionVO : copyAnnotationsOfOtherVersionPresenter.getAvalibleVersion()) {
			versionToPickFrom.addItem(contentVersionVO);
			versionToPickFrom.setItemCaption(contentVersionVO, contentVersionVO.toString());
		}

		versionToPickFrom.setCaption("getAnnotationsOfPreviousVersionWindowButton.pickVersion");

		horizontalLayout.addComponent(versionToPickFrom);

		horizontalLayout.addComponent(new Button($("Ok")));

		return null;
	}

	private String version;
}
