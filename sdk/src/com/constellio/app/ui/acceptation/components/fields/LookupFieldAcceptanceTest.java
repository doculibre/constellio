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
package com.constellio.app.ui.acceptation.components.fields;

import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.LookupTreeDataProvider;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.TextInputDataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@UiTest
@InDevelopmentTest
public class LookupFieldAcceptanceTest extends ConstellioTest {

	ConstellioWebDriver driver;
	RecordVO dummyViewRecord;

	MetadataSchemaVO schema;

	String dummyPage = "dummyPage";

	private List<DummyBean> allDummyBeans = new ArrayList<DummyBean>();

	private List<DummyBean> rootDummyBeans = new ArrayList<DummyBean>();

	private int beanPerTreeLevel = 5;
	private int treeDepth = 3;

	@Before
	public void setUp()
			throws Exception {
		withSpiedServices(AppLayerFactory.class);
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();

		loadDummyBeans();

		AppLayerFactory factory = getAppLayerFactory();
		NavigatorConfigurationService navigatorConfigurationService = new NavigatorConfigurationService() {
			@Override
			public void configure(Navigator navigator) {
				super.configure(navigator);
				navigator.addView(dummyPage, DummyView.class);
			}
		};
		when(factory.getNavigatorConfigurationService()).thenReturn(navigatorConfigurationService);

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		schema = new MetadataSchemaVO("zeSchema", zeCollection, asLocaleMap("The schema", "Ze schema"));
	}

	private void loadDummyBeans() {
		for (int i = 0; i < beanPerTreeLevel; i++) {
			String itemCaption = "node" + (i + 1);
			DummyBean root = new DummyBean(itemCaption);
			rootDummyBeans.add(root);
			allDummyBeans.add(root);
			loadDummyBeanChildren(root, 0);
		}
	}

	private void loadDummyBeanChildren(DummyBean parent, int currentDepth) {
		if (currentDepth < treeDepth) {
			for (int i = 0; i < beanPerTreeLevel; i++) {
				String itemCaption = parent.name + (i + 1);
				DummyBean child = new DummyBean(itemCaption);
				parent.addChild(child);
				allDummyBeans.add(child);
				loadDummyBeanChildren(child, currentDepth + 1);
			}
		}
	}

	private static Map<Locale, String> asLocaleMap(String englishValue, String frenchValue) {
		Map<Locale, String> map = new HashMap<>();
		map.put(Locale.ENGLISH, englishValue);
		map.put(Locale.FRENCH, frenchValue);
		return map;
	}

	@Test
	public void givenNoListForTextFieldThenTextFieldDisplayed() {
		DummyView.setComponentFactory(new DummyComponentFactory<DummyLookupField>() {
			@Override
			public List<DummyLookupField> build(ViewChangeEvent event) {
				return Arrays.asList(new DummyLookupField());
			}
		});
		driver.navigateTo().url(dummyPage);
		waitUntilICloseTheBrowsers();
	}

	private List<DummyBean> search(String text, boolean suggest) {
		List<DummyBean> matches = new ArrayList<DummyBean>();
		for (DummyBean dummyBean : allDummyBeans) {
			if (dummyBean.name.toLowerCase().startsWith(text)) {
				matches.add(dummyBean);
			} else if (!suggest && dummyBean.nameReverse.toLowerCase().startsWith(text)) {
				matches.add(dummyBean);
			}
		}
		return matches;
	}

	private class DummyLookupField extends LookupField<DummyBean> {

		public DummyLookupField() {
			super(new DummyLookupInputDataProvider(true), new DummyLookupTreeDataProvider());
			setItemConverter(new DummyBeanConverter());
			setTreeBufferSize(2);
		}

		@Override
		public Class<? extends DummyBean> getType() {
			return DummyBean.class;
		}

	}

	private class DummyBeanConverter implements Converter<String, DummyBean> {

		@Override
		public DummyBean convertToModel(String value, Class<? extends DummyBean> targetType, Locale locale)
				throws com.vaadin.data.util.converter.Converter.ConversionException {
			return search(value, false).get(0);
		}

		@Override
		public String convertToPresentation(DummyBean value, Class<? extends String> targetType, Locale locale)
				throws com.vaadin.data.util.converter.Converter.ConversionException {
			return value.name;
		}

		@Override
		public Class<DummyBean> getModelType() {
			return DummyBean.class;
		}

		@Override
		public Class<String> getPresentationType() {
			return String.class;
		}

	}

	private static class DummyBean implements Serializable {

		private String name;
		private String nameReverse;

		private DummyBean parent;

		private List<DummyBean> children = new ArrayList<DummyBean>();

		public DummyBean(String name) {
			this(name, null);
		}

		public DummyBean(String name, DummyBean parent) {
			super();
			this.name = name;
			this.parent = parent;
			this.nameReverse = StringUtils.reverse(name);
		}

		public void addChild(DummyBean dummyBean) {
			dummyBean.parent = this;
			children.add(dummyBean);
		}

	}

	private class DummyLookupInputDataProvider implements TextInputDataProvider<DummyBean> {

		private boolean suggest;

		public DummyLookupInputDataProvider(boolean suggest) {
			this.suggest = suggest;
		}

		@Override
		public List<DummyBean> getData(String text, int startIndex, int count) {
			text = text.toLowerCase();
			List<DummyBean> matches = search(text, suggest);
			List<DummyBean> subMatches = new ArrayList<DummyBean>();
			for (int i = startIndex; i < matches.size() && subMatches.size() <= count; i++) {
				DummyBean match = matches.get(i);
				subMatches.add(match);
			}
			return subMatches;
		}

		@Override
		public int size(String text) {
			return search(text, suggest).size();
		}

	}

	private class DummyLookupTreeDataProvider implements LookupTreeDataProvider<DummyBean> {

		private List<DummyBean> subList(List<DummyBean> list, int start, int maxSize) {
			List<DummyBean> subList = new ArrayList<DummyBean>();
			for (int i = start; i < list.size() && subList.size() < maxSize; i++) {
				DummyBean match = list.get(i);
				subList.add(match);
			}
			return subList;
		}

		@Override
		public int getRootObjectsCount() {
			return rootDummyBeans.size();
		}

		@Override
		public List<DummyBean> getRootObjects(int start, int maxSize) {
			return subList(rootDummyBeans, start, maxSize);
		}

		@Override
		public DummyBean getParent(DummyBean child) {
			return child.parent;
		}

		@Override
		public int getChildrenCount(DummyBean parent) {
			return parent.children.size();
		}

		@Override
		public List<DummyBean> getChildren(DummyBean parent, int start, int maxSize) {
			return subList(parent.children, start, maxSize);
		}

		@Override
		public boolean hasChildren(DummyBean parent) {
			return !parent.children.isEmpty();
		}

		@Override
		public boolean isLeaf(DummyBean object) {
			return object.children.isEmpty();
		}

		@Override
		public boolean isSelectable(DummyBean selection) {
			return true;
		}

		@Override
		public TextInputDataProvider<DummyBean> search() {
			return new DummyLookupInputDataProvider(false);
		}

	}

	private static interface DummyComponentFactory<T extends Component> extends Serializable {

		List<T> build(ViewChangeEvent event);

	}

	public static class DummyView extends BaseViewImpl implements View {

		private static DummyComponentFactory<?> componentFactory;

		public static DummyComponentFactory<?> getComponentFactory() {
			return componentFactory;
		}

		public static void setComponentFactory(DummyComponentFactory<?> componentFactory) {
			DummyView.componentFactory = componentFactory;
		}

		@Override
		protected String getTitle() {
			return "LookupFieldAcceptanceTest";
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Component buildMainComponent(ViewChangeEvent event) {
			System.out.println("session id > " + ConstellioUI.getCurrent().getSession().getSession().getId());

			VerticalLayout mainLayout = new VerticalLayout();
			mainLayout.setWidth("100%");
			mainLayout.setSpacing(true);

			Label titleLabel = new Label(LazyTreeAcceptanceTest.class.getSimpleName());
			titleLabel.addStyleName(ValoTheme.LABEL_H1);

			//			listAddRemoveField.setWidth("500px");

			mainLayout.addComponent(titleLabel);
			if (componentFactory != null) {
				List<Component> components = (List<Component>) componentFactory.build(event);
				for (Component component : components) {
					mainLayout.addComponent(component);
				}
			}

			return mainLayout;
		}

	}

}
