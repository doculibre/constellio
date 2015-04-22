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
package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.entities.RecordVORuntimeException.RecordVORuntimeException_NoSuchMetadata;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.schemas.Schemas;

@SuppressWarnings("serial")
public class RecordVO implements Serializable {

	public enum VIEW_MODE {FORM, DISPLAY, TABLE}

	String id;

	final List<MetadataValueVO> metadataValues;

	VIEW_MODE viewMode;

	public RecordVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
		this.id = id;
		LangUtils.ensureNoNullItems(metadataValues);
		this.metadataValues = metadataValues;
		this.viewMode = viewMode;
	}

	protected boolean getBooleanWithDefaultValue(String param, boolean defaultValue) {
		Boolean value = get(param);
		return value == null ? defaultValue : value;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return get(Schemas.TITLE.getLocalCode());
	}

	public void setTitle(String title) {
		set(Schemas.TITLE.getLocalCode(), title);
	}

	public final VIEW_MODE getViewMode() {
		return viewMode;
	}

	public MetadataSchemaVO getSchema() {
		MetadataSchemaVO schema = null;
		if (!metadataValues.isEmpty()) {
			schema = metadataValues.get(0).getMetadata().getSchema();
		}
		return schema;
	}

	public List<MetadataValueVO> getMetadataValues() {
		return metadataValues;
	}

	public MetadataValueVO getMetadataValue(MetadataVO metadata) {
		MetadataValueVO match = null;
		for (MetadataValueVO metadataValue : metadataValues) {
			if (metadata.equals(metadataValue.getMetadata())) {
				match = metadataValue;
				break;
			}
		}
		return match;
	}

	public List<MetadataVO> getMetadatas() {
		List<MetadataVO> metadatas = new ArrayList<MetadataVO>();
		for (MetadataValueVO metadataValue : metadataValues) {
			metadatas.add(metadataValue.getMetadata());
		}
		return metadatas;
	}

	public MetadataVO getMetadataOrNull(String code) {
		String codeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(code);
		for (MetadataValueVO metadataValue : metadataValues) {
			MetadataVO metadata = metadataValue.getMetadata();
			String metadataCode = metadata.getCode();
			String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
			if (code.equals(metadataCode) || codeWithoutPrefix.equals(metadataCodeWithoutPrefix)) {
				return metadataValue.getMetadata();
			}
		}
		return null;
	}

	public MetadataVO getMetadata(String code) {
		MetadataVO metadataVO = getMetadataOrNull(code);
		if (metadataVO == null) {
			throw new RecordVORuntimeException_NoSuchMetadata(code);
		} else {
			return metadataVO;
		}
	}

	public <T extends Object> T get(MetadataVO metadata) {
		T value = null;
		if (metadata != null) {
			for (MetadataValueVO metadataValue : metadataValues) {
				if (metadata.equals(metadataValue.getMetadata())) {
					value = metadataValue.getValue();
					break;
				}
			}
		}
		return value;
	}

	public void set(MetadataVO metadata, Object value) {
		for (MetadataValueVO metadataValue : metadataValues) {
			if (metadata.equals(metadataValue.getMetadata())) {
				metadataValue.setValue(value);
				break;
			}
		}
	}

	public <T extends Object> T get(String metadataCode) {
		MetadataVO metadata = getMetadata(metadataCode);
		return get(metadata);
	}

	public void set(String metadataCode, Object value) {
		MetadataVO metadata = getMetadata(metadataCode);
		set(metadata, value);
	}

	public <T extends Object> List<T> getList(MetadataVO metadata) {
		return get(metadata);
	}

	public <T extends Object> List<T> getList(String metadataCode) {
		return get(metadataCode);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (!(obj instanceof RecordVO)) {
			return false;
		} else {
			RecordVO other = (RecordVO) obj;
			if (id == null) {
				if (other.id != null) {
					return false;
				}
			} else if (!id.equals(other.id)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return id;
	}

}
