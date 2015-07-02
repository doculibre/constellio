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
package com.constellio.app.modules.rm.ui.components;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.vaadin.ui.Component;

public class RMMetadataDisplayFactory extends MetadataDisplayFactory {

	@Override
	public Component buildSingleValue(RecordVO recordVO, MetadataVO metadata, Object displayValue) {
		Component displayComponent = super.buildSingleValue(recordVO, metadata, displayValue);
		return displayComponent;
	}

//	@Override
//	protected Component newContentVersionDisplayComponent(RecordVO recordVO, ContentVersionVO contentVersionVO) {
//		Component displayComponent;
//		String agentURL = ConstellioAgentUtils.getAgentURL(recordVO, contentVersionVO);
//		if (agentURL != null) {
//			displayComponent = new ConstellioAgentLink(agentURL, contentVersionVO);
//		} else {
//			displayComponent = new DownloadContentVersionLink(contentVersionVO);
//		}
//		return displayComponent;
//	}

}
