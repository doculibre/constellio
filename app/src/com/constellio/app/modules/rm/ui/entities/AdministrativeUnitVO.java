/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.entities;

import static com.constellio.app.modules.rm.wrappers.AdministrativeUnit.CODE;
import static com.constellio.app.modules.rm.wrappers.AdministrativeUnit.DESCRIPTION;
import static com.constellio.app.modules.rm.wrappers.AdministrativeUnit.FILING_SPACES;
import static com.constellio.app.modules.rm.wrappers.AdministrativeUnit.PARENT;

import java.util.List;

import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;

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
