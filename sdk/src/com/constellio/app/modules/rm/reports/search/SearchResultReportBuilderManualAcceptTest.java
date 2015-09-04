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
package com.constellio.app.modules.rm.reports.search;

import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportBuilder;
import com.constellio.app.modules.rm.reports.model.search.SearchResultReportModel;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchResultReportBuilderManualAcceptTest extends ReportBuilderTestFramework {
    SearchResultReportModel model;

    @Before
    public void setUp()
            throws Exception {
    }

    @Test
    public void whenBuildEmptyReportThenOk() {
        model = new SearchResultReportModel();
        build(new SearchResultReportBuilder(model,
                getModelLayerFactory().getFoldersLocator(), new Locale("fr")));
    }

    @Test
    public void whenBuildReportWithResultsThenOk() {
        model = configModel();
        build(new SearchResultReportBuilder(model,
                getModelLayerFactory().getFoldersLocator(), new Locale("fr")));
    }

    private SearchResultReportModel configModel() {

        SearchResultReportModel model = new SearchResultReportModel();
        model.addTitle("title1");
        model.addTitle("title2");
        model.addTitle("number");
        model.addTitle("date");

        List<Object> line1 = new ArrayList<>();
        line1.add("cell11");
        line1.add("cell12");
        line1.add(new Integer(1));
        line1.add(null);
        model.addLine(line1);

        List<Object> line2 = new ArrayList<>();
        line2.add("cell21");
        line2.add(null);
        line2.add(new Double(2));
        line2.add(new Date());
        model.addLine(line2);

        List<Object> line3 = new ArrayList<>();
        line3.add("cell31");
        line3.add(null);
        line3.add(new Float(3));
        line3.add(new Date());
        model.addLine(line3);

        return model;
    }
}
