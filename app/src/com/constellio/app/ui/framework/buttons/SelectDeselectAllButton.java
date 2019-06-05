package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class SelectDeselectAllButton extends BaseButton {

	private boolean selectAllMode;

	private String selectAllCaption;

	private String deselectAllCaption;

	public SelectDeselectAllButton() {
		this($("selectAll"), $("deselectAll"));
	}

	public SelectDeselectAllButton(String selectAllCaption, String deselectAllCaption) {
		this(selectAllCaption, deselectAllCaption, true);
	}

	public SelectDeselectAllButton(String selectAllCaption, String deselectAllCaption, boolean selectAllMode) {
		super(selectAllMode ? selectAllCaption : deselectAllCaption);
		this.selectAllCaption = selectAllCaption;
		this.deselectAllCaption = deselectAllCaption;
		this.selectAllMode = selectAllMode;
		addStyleName("select-deselect-all-button");
	}

	public boolean isSelectAllMode() {
		return selectAllMode;
	}

	public void setSelectAllMode(boolean selectAllMode) {
		this.selectAllMode = selectAllMode;
		if (selectAllMode) {
			setCaption(selectAllCaption);
		} else {
			setCaption(deselectAllCaption);
		}
	}

	@Override
	protected void buttonClick(ClickEvent event) {
		if (selectAllMode) {
			onSelectAll(event);
			setCaption(deselectAllCaption);
		} else {
			onDeselectAll(event);
			setCaption(selectAllCaption);
		}
		selectAllMode = !selectAllMode;
		buttonClickCallBack(selectAllMode);
	}

	protected abstract void onSelectAll(ClickEvent event);

	protected abstract void onDeselectAll(ClickEvent event);

	protected abstract void buttonClickCallBack(boolean selectAllMode);

}
