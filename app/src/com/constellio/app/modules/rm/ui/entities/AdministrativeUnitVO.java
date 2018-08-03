package com.constellio.app.modules.rm.ui.entities;

import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;

import java.util.List;

import static com.constellio.app.modules.rm.wrappers.AdministrativeUnit.*;

public class AdministrativeUnitVO extends RecordVO {

	public AdministrativeUnitVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
		super(id, metadataValues, viewMode);
	}

	public String getCode() {
		return get(CODE);
	}

	public void setCode(String code) {
		set(CODE, code);
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public void setDescription(String description) {
		set(DESCRIPTION, description);
	}

	public void getParent() {
		get(PARENT);
	}

	public void setParent(AdministrativeUnitVO parent) {
		set(PARENT, parent);
	}

	public void setParent(String parent) {
		set(PARENT, parent);
	}

	public List<String> getFilingSpaces() {
		return getList(FILING_SPACES);
	}

	public void setFilingSpaces(List<?> filingSpaces) {
		set(FILING_SPACES, filingSpaces);
	}

}
