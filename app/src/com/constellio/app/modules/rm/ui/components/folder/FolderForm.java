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
package com.constellio.app.modules.rm.ui.components.folder;

import java.io.Serializable;

import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;

/**
 * Implemented:
 *
 * Folder.TYPE
 * Folder.ADMINISTRATIVE_UNIT : String
 * Folder.FILING_SPACE
 * Folder.CATEGORY_ENTERED
 * Folder.RETENTION_RULE
 * Folder.COPY_STATUS_ENTERED
 * Folder.ACTUAL_TRANSFER_DATE
 * Folder.ACTUAL_DEPOSIT_DATE
 * Folder.ACTUAL_DESTRUCTION_DATE
 *
 * Always invisible:
 * Folder.CONTAINER
 *
 * Folder.MEDIA_TYPE should have an enum-backed CHECKBOXES input type
 *
 * @author Vincent
 */
public interface FolderForm extends Serializable {

	void reload();
	
	void commit();

	ConstellioFactories getConstellioFactories();

	SessionContext getSessionContext();

	CustomFolderField<?> getCustomField(String metadataCode);

}
