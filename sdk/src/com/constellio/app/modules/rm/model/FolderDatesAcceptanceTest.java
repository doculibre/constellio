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
package com.constellio.app.modules.rm.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDate;
import org.junit.Before;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;

public class FolderDatesAcceptanceTest extends ConstellioTest {

	LocalDate november4_2009 = new LocalDate(2009, 11, 4);
	LocalDate november4_2010 = new LocalDate(2010, 11, 4);
	LocalDate november4_2011 = new LocalDate(2011, 11, 4);
	LocalDate november4_2012 = new LocalDate(2012, 11, 4);
	LocalDate november4_2013 = new LocalDate(2013, 11, 4);
	LocalDate november4_2014 = new LocalDate(2013, 11, 4);
	LocalDate november4_2015 = new LocalDate(2013, 11, 4);
	LocalDate november4_2016 = new LocalDate(2013, 11, 4);

	LocalDate december12_2009 = new LocalDate(2009, 12, 12);
	LocalDate january12_2010 = new LocalDate(2010, 1, 12);
	LocalDate january12_2012 = new LocalDate(2012, 1, 12);
	LocalDate january12_2014 = new LocalDate(2014, 1, 12);

	LocalDate february15_2015 = new LocalDate(2015, 2, 16);
	LocalDate february16_2015 = new LocalDate(2015, 2, 16);
	LocalDate february17_2015 = new LocalDate(2015, 2, 16);
	LocalDate february15_2017 = new LocalDate(2016, 2, 16);
	LocalDate february16_2017 = new LocalDate(2016, 2, 16);
	LocalDate february17_2017 = new LocalDate(2016, 2, 16);

	RMSchemasRecordsServices schemas;
	RMTestRecords records;
	RecordServices recordServices;

	Transaction transaction = new Transaction();

	String zeCategory;
	String aPrincipalAdminUnit;
	String anotherPrincipalAdminUnit;
	String aSecondaryAdminUnit;

	String PA;
	String MV;
	String MD;

	CopyType noEnteredCopyType = null;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule();
		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		zeCategory = records.categoryId_ZE42;
		aPrincipalAdminUnit = records.unitId_10;
		anotherPrincipalAdminUnit = records.unitId_20;
		aSecondaryAdminUnit = records.unitId_30;
		PA = records.PA;
		MV = records.MV;
		MD = records.MD;
	}

}
