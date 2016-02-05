package com.constellio.app.ui.pages.globalGroup;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.entities.GlobalGroupVO;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class GlobalGroupDisplay extends CustomComponent {

	public static final String STYLE_CAPTION = "display-globalGroup-caption";
	public static final String STYLE_VALUE = "display-globalGroup-value";

	GridLayout mainLayout;

	public GlobalGroupDisplay(GlobalGroupVO globalGroupVO) {
		setSizeFull();

		mainLayout = new GridLayout(2, 1);

		int row = 0;
		Component displayComponentCode = new Label(globalGroupVO.getCode());
		if (displayComponentCode != null) {
			String caption = $("Code");
			row = createComponentRow(globalGroupVO, row, displayComponentCode, caption);
		}

		Component displayComponentName = new Label(globalGroupVO.getName());
		if (displayComponentName != null) {
			String caption = $("Name");
			createComponentRow(globalGroupVO, row, displayComponentName, caption);
		}

		setCompositionRoot(mainLayout);
	}

	private int createComponentRow(GlobalGroupVO globalGroupVO, int row, Component displayComponent, String caption) {
		Label captionLabel = new Label(caption);

		captionLabel.addStyleName(STYLE_CAPTION);
		captionLabel.addStyleName(STYLE_CAPTION + "-" + globalGroupVO.getCode());

		displayComponent.addStyleName(STYLE_VALUE);
		displayComponent.addStyleName(STYLE_VALUE + "-" + globalGroupVO.getCode());

		mainLayout.addComponent(captionLabel, 0, row);
		mainLayout.addComponent(displayComponent, 1, row);

		row++;
		mainLayout.setRows(row + 1);
		return row;
	}

}
