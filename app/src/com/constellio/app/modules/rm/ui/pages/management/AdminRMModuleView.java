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
package com.constellio.app.modules.rm.ui.pages.management;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminModuleViewGroup;

public interface AdminRMModuleView extends BaseView, AdminModuleViewGroup {

	void setManageTaxonomiesVisible(boolean visible);

	void setManageUniformSubdivisionsVisible(boolean visible);

	void setManageRetentionRuleVisible(boolean visible);

	void setManageValueListVisible(boolean visible);

	void setManageMetadataSchemasVisible(boolean visible);

	void setManageFilingSpaceVisible(boolean visible);

	void setManageSecurityVisible(boolean visible);

	void setManageRolesVisible(boolean visible);

	void setManageMetadataExtractorVisible(boolean visible);

	void setManageConnectorsVisible(boolean visible);

	void setManageSearchEngineVisible(boolean visible);

	void setManageTrashVisible(boolean visible);

	void setManageSystemConfiguration(boolean visible);

	void setManageSystemGroups(boolean visible);

	void setManageSystemUsers(boolean visible);

	void setManageSystemCollections(boolean visible);

	void setManageSystemModules(boolean visible);

	void setManageSystemDataImports(boolean visible);

	void setManageSystemServers(boolean visible);

	void setManageSystemUpdates(boolean visible);
}
