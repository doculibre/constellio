package com.constellio.app.extensions.records.params;

import com.constellio.app.ui.application.ConstellioUI.Navigation;
import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.server.Page;
import com.vaadin.ui.AbstractComponent;

public class NavigationParams {
	private Navigation navigate;
	private String recordId;
	private RecordVO recordVO;
	private String schemaTypeCode;
	private Page page;
	private AbstractComponent component;

	public NavigationParams(Navigation navigate, RecordVO recordVO, String schemaTypeCode, Page page,
			AbstractComponent component) {
		this.navigate = navigate;
		this.recordId = recordVO.getId();
		this.recordVO = recordVO;
		this.schemaTypeCode = schemaTypeCode;
		this.page = page;
		this.component = component;
	}

	public NavigationParams(Navigation navigate, String recordId, String schemaTypeCode, Page page,
			AbstractComponent component) {
		this.navigate = navigate;
		this.recordId = recordId;
		this.schemaTypeCode = schemaTypeCode;
		this.page = page;
		this.component = component;
	}

	public Navigation navigate() {
		return navigate;
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public RecordVO getRecordVO() {
		return recordVO;
	}

	public void setRecordVO(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	public String getSchemaTypeCode() {
		return schemaTypeCode;
	}

	public void setSchemaTypeCode(String schemaTypeCode) {
		this.schemaTypeCode = schemaTypeCode;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public AbstractComponent getComponent() {
		return component;
	}

	public void setComponent(AbstractComponent component) {
		this.component = component;
	}
}
