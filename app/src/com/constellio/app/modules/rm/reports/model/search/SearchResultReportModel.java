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
package com.constellio.app.modules.rm.reports.model.search;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchResultReportModel {
    private final List<List<Object>> results = new ArrayList<>();
    private final List<String> columnsTitles = new ArrayList<>();

    public List<List<Object>> getResults() {
        return new ArrayList<>(CollectionUtils.unmodifiableCollection(results));
    }

    public List<String> getColumnsTitles() {
        return new ArrayList<>(CollectionUtils.unmodifiableCollection(columnsTitles));
    }

    public void addTitle(String title) {
        columnsTitles.add(title);
    }

    public void addLine(List<Object> recordLine) {
        results.add(recordLine);
    }
}
