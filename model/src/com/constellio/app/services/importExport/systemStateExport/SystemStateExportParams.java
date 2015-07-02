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
package com.constellio.app.services.importExport.systemStateExport;

import java.util.ArrayList;
import java.util.List;

public class SystemStateExportParams {

	List<String> onlyExportContentOfRecords = null;

	public boolean isExportAllContent() {
		return onlyExportContentOfRecords == null;
	}

	public SystemStateExportParams setExportAllContent() {
		this.onlyExportContentOfRecords = null;
		return this;
	}

	public SystemStateExportParams setExportNoContent() {
		this.onlyExportContentOfRecords = new ArrayList<>();
		return this;
	}

	public List<String> getOnlyExportContentOfRecords() {
		return onlyExportContentOfRecords;
	}

	public SystemStateExportParams setOnlyExportContentOfRecords(List<String> onlyExportContentOfRecords) {
		this.onlyExportContentOfRecords = onlyExportContentOfRecords;
		return this;
	}
}
