package com.constellio.sdk.tests;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.constellio.app.modules.es.navigation.ESViews;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.robots.ui.navigation.RobotViews;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.pages.base.BaseView;

public class SDKViewNavigation {

	public CoreViews coreViews;

	public ESViews esViews;

	public RMViews rmViews;

	public TaskViews taskViews;

	public RobotViews robotViews;

	public SDKViewNavigation(BaseView view) {
		Navigation navigation = mock(Navigation.class, "navigation");
		coreViews = mock(CoreViews.class, "coreViews");
		esViews = mock(ESViews.class, "esViews");
		rmViews = mock(RMViews.class, "rmViews");
		taskViews = mock(TaskViews.class, "taskViews");
		robotViews = mock(RobotViews.class, "robotViews");

		when(view.navigate()).thenReturn(navigation);
		when(navigation.to()).thenReturn(coreViews);
		when(navigation.to(anyObject())).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				return getMockedViews((Class) invocation.getArguments()[0]);
			}
		});
	}

	public <T> T getMockedViews(Class<T> viewsClass) {
		if (CoreViews.class.equals(viewsClass)) {
			return (T) coreViews;

		} else if (RMViews.class.equals(viewsClass)) {
			return (T) rmViews;

		} else if (ESViews.class.equals(viewsClass)) {
			return (T) esViews;

		} else if (TaskViews.class.equals(viewsClass)) {
			return (T) taskViews;

		} else if (RobotViews.class.equals(viewsClass)) {
			return (T) robotViews;

		} else {
			throw new RuntimeException("Unsupported view class : " + viewsClass);
		}
	}
}
