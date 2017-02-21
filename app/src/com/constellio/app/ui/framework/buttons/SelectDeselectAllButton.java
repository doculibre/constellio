package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class SelectDeselectAllButton extends BaseButton {
	
	private boolean allDeselected;
	
	private String selectAllCaption;
	
	private String deselectAllCaption;

	public SelectDeselectAllButton() {
		this(true);
	}

	public SelectDeselectAllButton(boolean allDeselected) {
		this($("selectAll"), $("deselectAll"), allDeselected);
	}

	public SelectDeselectAllButton(String selectAllCaption, String deselectAllCaption, boolean allDeselected) {
		super(allDeselected ? selectAllCaption : deselectAllCaption);
		this.selectAllCaption = selectAllCaption;
		this.deselectAllCaption = deselectAllCaption;
		this.allDeselected = allDeselected;
	}

	public boolean isAllDeselected() {
		return allDeselected;
	}

	public void setAllDeselected(boolean allSelected) {
		this.allDeselected = allSelected;
		if (allSelected) {
			setCaption(selectAllCaption);
		} else {
			setCaption(deselectAllCaption);
		}
	}

	@Override
	protected void buttonClick(ClickEvent event) {
		if (allDeselected) {
			onSelectAll(event);
			setCaption(deselectAllCaption);
		} else {
			onDeselectAll(event);
			setCaption(selectAllCaption);
		}
		allDeselected = !allDeselected;
	}
	
	protected abstract void onSelectAll(ClickEvent event);
	
	protected abstract void onDeselectAll(ClickEvent event);

}
