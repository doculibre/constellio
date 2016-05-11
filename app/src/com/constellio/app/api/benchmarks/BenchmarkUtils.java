package com.constellio.app.api.benchmarks;

import static org.mockito.Mockito.when;

import java.util.Locale;

import org.mockito.Mockito;

import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.SimpleSearchView;
import com.constellio.model.entities.records.wrappers.User;

public class BenchmarkUtils {

	public static final SimpleSearchView createDummySimpleSearchViewFor(User user) {
		String collection = user.getCollection();
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();

		SessionContext sessionContext = Mockito.mock(SessionContext.class);
		when(sessionContext.getCurrentCollection()).thenReturn(collection);
		when(sessionContext.getCurrentLocale()).thenReturn(Locale.FRENCH);
		UserVO currentUser = new UserToVOBuilder().build(user.getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		when(sessionContext.getCurrentUser()).thenReturn(currentUser);

		SimpleSearchView searchView = Mockito.mock(SimpleSearchView.class);
		when(searchView.getCollection()).thenReturn(collection);
		when(searchView.getSessionContext()).thenReturn(sessionContext);
		when(searchView.getConstellioFactories()).thenReturn(constellioFactories);
		return searchView;
	}

}
