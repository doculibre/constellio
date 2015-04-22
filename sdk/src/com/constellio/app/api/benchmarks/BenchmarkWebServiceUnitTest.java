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
package com.constellio.app.api.benchmarks;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.api.benchmarks.BenchmarkWebServiceRuntimeException.BenchmarkWebServiceRuntimeException_BenchmarkServiceMustBeEnabled;
import com.constellio.app.api.benchmarks.BenchmarkWebServiceRuntimeException.BenchmarkWebServiceRuntimeException_ParameterInvalid;
import com.constellio.app.api.benchmarks.BenchmarkWebServiceRuntimeException.BenchmarkWebServiceRuntimeException_ParameterRequired;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

public class BenchmarkWebServiceUnitTest extends ConstellioTest {

	@Mock HttpServletRequest request;
	@Mock HttpServletResponse response;
	@Mock RecordServices recordServices;
	@Mock SearchServices searchServices;
	@Mock CollectionsManager collectionsManager;
	@Mock MetadataSchemaTypes metadataSchemaTypes;
	@Mock Random random;
	BenchmarkWebService benchmarkWebService;

	@Mock Record aRecord;
	@Mock Record anotherRecord;

	@Mock BenchmarkWebServiceSetup setup;

	@Before
	public void setUp()
			throws Exception {
		System.setProperty("benchmarkServiceEnabled", "true");
		benchmarkWebService = spy(new BenchmarkWebService());
		doReturn(recordServices).when(benchmarkWebService).getRecordServices();
		doReturn(searchServices).when(benchmarkWebService).getSearchServices();
		doReturn(metadataSchemaTypes).when(benchmarkWebService).getSchemaTypes(BenchmarkWebService.COLLECTION);
		doReturn(collectionsManager).when(benchmarkWebService).getCollectionsServices();
		doReturn(setup).when(benchmarkWebService).newTestSetup();
	}

	@Test(expected = BenchmarkWebServiceRuntimeException_BenchmarkServiceMustBeEnabled.class)
	public void givenBenchmarServiceIsDisabledThenCannotInvokeIt()
			throws Exception {
		System.getProperties().remove("benchmarkServiceEnabled");
		benchmarkWebService.doGet(request, response);

	}

	@Test(expected = BenchmarkWebServiceRuntimeException_BenchmarkServiceMustBeEnabled.class)
	public void givenBenchmarServiceHasNotTheEnabledValueThenCannotInvokeIt()
			throws Exception {
		System.setProperty("benchmarkServiceEnabled", "aValue");
		benchmarkWebService.doGet(request, response);
	}

	@Test
	public void whenDoGetWithAddFolderActionThenAddFolder()
			throws Exception {

		doNothing().when(benchmarkWebService).doAddFolder(eq(request), eq(response), anyInt());
		when(request.getParameter(BenchmarkWebService.ACTION_PARAMETER)).thenReturn(BenchmarkWebService.ACTION_ADD_FOLDER);
		when(request.getParameter(BenchmarkWebService.QUANTITY_PARAMETER)).thenReturn("12");
		when(request.getParameter(BenchmarkWebService.SIZE_IN_OCTETS_PARAMETER)).thenReturn("12");
		benchmarkWebService.doGet(request, response);

		verify(benchmarkWebService).doAddFolder(eq(request), eq(response), anyInt());
	}

	@Test
	public void whenDoGetWithUpdateRecordActionThenUpdateRecords()
			throws Exception {

		doNothing().when(benchmarkWebService).doUpdateRecords(eq(request), eq(response), anyInt());
		when(request.getParameter(BenchmarkWebService.ACTION_PARAMETER)).thenReturn(BenchmarkWebService.ACTION_UPDATE_FOLDERS);
		when(request.getParameter(BenchmarkWebService.QUANTITY_PARAMETER)).thenReturn("12");
		when(request.getParameter(BenchmarkWebService.SIZE_IN_OCTETS_PARAMETER)).thenReturn("12");
		benchmarkWebService.doGet(request, response);

		verify(benchmarkWebService).doUpdateRecords(eq(request), eq(response), anyInt());
	}

	@Test
	public void whenDoGetWithAddContentActionThenAddContent()
			throws Exception {

		doNothing().when(benchmarkWebService).doAddContent(eq(request), eq(response), anyInt());
		when(request.getParameter(BenchmarkWebService.ACTION_PARAMETER)).thenReturn(BenchmarkWebService.ACTION_ADD_CONTENT);
		when(request.getParameter(BenchmarkWebService.SIZE_IN_OCTETS_PARAMETER)).thenReturn("12");
		benchmarkWebService.doGet(request, response);

		verify(benchmarkWebService).doAddContent(eq(request), eq(response), anyInt());
	}

	@Test
	public void whenDoGetWithSearchActionThenSearch()
			throws Exception {

		doNothing().when(benchmarkWebService).doSearch(eq(request), eq(response), anyInt());
		when(request.getParameter(BenchmarkWebService.ACTION_PARAMETER)).thenReturn(BenchmarkWebService.ACTION_SEARCH);
		when(request.getParameter(BenchmarkWebService.QUANTITY_PARAMETER)).thenReturn("12");
		benchmarkWebService.doGet(request, response);

		verify(benchmarkWebService).doSearch(eq(request), eq(response), anyInt());
	}

	@Test(expected = BenchmarkWebServiceRuntimeException_ParameterRequired.class)
	public void whenDoGetWithoutActionThenException()
			throws Exception {

		benchmarkWebService.doGet(request, response);
	}

	@Test(expected = BenchmarkWebServiceRuntimeException_ParameterInvalid.class)
	public void whenDoGetWithInvalidActionThenException()
			throws Exception {
		when(request.getParameter(BenchmarkWebService.ACTION_PARAMETER)).thenReturn("invalid");
		benchmarkWebService.doGet(request, response);
	}
}
