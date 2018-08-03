package com.constellio.app.ui.framework.reports;

public class ReportWithCaptionVO {
	String title;
	String caption;

	public ReportWithCaptionVO(String title, String caption) {
		this.title = title;
		this.caption = caption;
	}

	public String getTitle() {
		return title;
	}

	public String getCaption() {
		return caption;
	}
}
