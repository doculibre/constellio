package com.constellio.app.ui.entities;

import com.constellio.model.entities.records.wrappers.Group;

import java.util.List;

public class GroupVO extends RecordVO {

	public GroupVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
		super(id, metadataValues, viewMode);
	}

	public String getTitle() {
		return get(Group.TITLE);
	}

	public void setTitle(String title) {
		set(Group.TITLE, title);
	}

	public String isGlobal() {
		return get(Group.IS_GLOBAL);
	}

	public void setGlobal(String global) {
		set(Group.IS_GLOBAL, global);
	}

	public String getCode() {
		return get(Group.CODE);
	}

	public String setCode() {
		return get(Group.CODE);
	}


}
