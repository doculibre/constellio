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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.tree.LazyTree;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@UiTest
@InDevelopmentTest
public class LazyTreeAcceptanceTest extends ConstellioTest {

	ConstellioWebDriver driver;
	RecordVO dummyViewRecord;

	MetadataSchemaVO schema;

	String dummyPage = "dummyPage";

	private List<DummyBean> allDummyBeans = new ArrayList<DummyBean>();

	private List<DummyBean> rootDummyBeans = new ArrayList<DummyBean>();

	@Before
	public void setUp()
			throws Exception {
		withSpiedServices(AppLayerFactory.class);
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);

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
		{
			DummyBean root0 = new DummyBean("root0");
			rootDummyBeans.add(root0);
			allDummyBeans.add(root0);

			DummyBean child00 = new DummyBean("child00");
			root0.addChild(child00);
			allDummyBeans.add(child00);
			{
				DummyBean child000 = new DummyBean("child000");
				child00.addChild(child000);
				allDummyBeans.add(child000);

				DummyBean child001 = new DummyBean("child001");
				child00.addChild(child001);
				allDummyBeans.add(child001);

				DummyBean child002 = new DummyBean("child002");
				child00.addChild(child002);
				allDummyBeans.add(child002);
			}

			DummyBean child01 = new DummyBean("child01");
			root0.addChild(child01);
			allDummyBeans.add(child01);
			{
				DummyBean child010 = new DummyBean("child010");
				child01.addChild(child010);
				allDummyBeans.add(child010);

				DummyBean child011 = new DummyBean("child011");
				child01.addChild(child011);
				allDummyBeans.add(child011);

				DummyBean child012 = new DummyBean("child012");
				child01.addChild(child012);
				allDummyBeans.add(child012);
			}

			DummyBean child02 = new DummyBean("child02");
			root0.addChild(child02);
			allDummyBeans.add(child02);
			{
				DummyBean child020 = new DummyBean("child020");
				child02.addChild(child020);
				allDummyBeans.add(child020);

				DummyBean child021 = new DummyBean("child021");
				child02.addChild(child021);
				allDummyBeans.add(child021);

				DummyBean child022 = new DummyBean("child022");
				child02.addChild(child022);
				allDummyBeans.add(child022);
			}
		}

		{
			DummyBean root1 = new DummyBean("root1");
			rootDummyBeans.add(root1);
			allDummyBeans.add(root1);

			DummyBean child10 = new DummyBean("child10");
			root1.addChild(child10);
			allDummyBeans.add(child10);
			{
				DummyBean child110 = new DummyBean("child100");
				child10.addChild(child110);
				allDummyBeans.add(child110);

				DummyBean child111 = new DummyBean("child101");
				child10.addChild(child111);
				allDummyBeans.add(child111);

				DummyBean child112 = new DummyBean("child102");
				child10.addChild(child112);
				allDummyBeans.add(child112);
			}

			DummyBean child11 = new DummyBean("child11");
			root1.addChild(child11);
			allDummyBeans.add(child11);
			{
				DummyBean child110 = new DummyBean("child110");
				child11.addChild(child110);
				allDummyBeans.add(child110);

				DummyBean child111 = new DummyBean("child111");
				child11.addChild(child111);
				allDummyBeans.add(child111);

				DummyBean child112 = new DummyBean("child112");
				child11.addChild(child112);
				allDummyBeans.add(child112);
			}

			DummyBean child12 = new DummyBean("child12");
			root1.addChild(child12);
			allDummyBeans.add(child12);
			{
				DummyBean child120 = new DummyBean("child120");
				child12.addChild(child120);
				allDummyBeans.add(child120);

				DummyBean child121 = new DummyBean("child121");
				child12.addChild(child121);
				allDummyBeans.add(child121);

				DummyBean child122 = new DummyBean("child122");
				child12.addChild(child122);
				allDummyBeans.add(child122);
			}
		}

		{
			DummyBean root2 = new DummyBean("root2");
			rootDummyBeans.add(root2);
			allDummyBeans.add(root2);

			DummyBean child20 = new DummyBean("child20");
			root2.addChild(child20);
			allDummyBeans.add(child20);
			{
				DummyBean child200 = new DummyBean("child200");
				child20.addChild(child200);
				allDummyBeans.add(child200);

				DummyBean child201 = new DummyBean("child201");
				child20.addChild(child201);
				allDummyBeans.add(child201);

				DummyBean child202 = new DummyBean("child202");
				child20.addChild(child202);
				allDummyBeans.add(child202);
			}

			DummyBean child21 = new DummyBean("child21");
			root2.addChild(child21);
			allDummyBeans.add(child21);
			{
				DummyBean child210 = new DummyBean("child210");
				child21.addChild(child210);
				allDummyBeans.add(child210);

				DummyBean child211 = new DummyBean("child211");
				child21.addChild(child211);
				allDummyBeans.add(child211);

				DummyBean child212 = new DummyBean("child212");
				child21.addChild(child212);
				allDummyBeans.add(child212);
			}

			DummyBean child22 = new DummyBean("child22");
			root2.addChild(child22);
			allDummyBeans.add(child22);
			{
				DummyBean child220 = new DummyBean("child220");
				child22.addChild(child220);
				allDummyBeans.add(child220);

				DummyBean child221 = new DummyBean("child221");
				child22.addChild(child221);
				allDummyBeans.add(child221);

				DummyBean child222 = new DummyBean("child222");
				child22.addChild(child222);
				allDummyBeans.add(child222);
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
	public void givenLazyTreeWithDataProviderThenLazyTreeDisplayed() {
		DummyView.setComponentFactory(new DummyComponentFactory<DummyLazyTree>() {
			@Override
			public List<DummyLazyTree> build(ViewChangeEvent event) {
				return Arrays.asList(new DummyLazyTree());
			}
		});
		driver.navigateTo().url(dummyPage);
		waitUntilICloseTheBrowsers();
	}

	private class DummyLazyTree extends LazyTree<DummyBean> {

		private static final String CAPTION_PROPERTY = "caption";

		public DummyLazyTree() {
			super(new DummyLazyTreeDataProvider(), 1);
			setItemCaptionMode(ItemCaptionMode.PROPERTY);
			setItemCaptionPropertyId(CAPTION_PROPERTY);
			addContainerProperty(CAPTION_PROPERTY, String.class, null);
		}

		@Override
		public Collection<?> getContainerPropertyIds() {
			return Arrays.asList(CAPTION_PROPERTY);
		}

		@Override
		public Property<?> getContainerProperty(Object itemId, Object propertyId) {
			Property<?> property;
			if (CAPTION_PROPERTY.equals(propertyId)) {
				property = new ObjectProperty<String>(((DummyBean) itemId).name, String.class);
			} else {
				property = super.getContainerProperty(itemId, propertyId);
			}
			return property;
		}

		@Override
		public Class<? extends DummyBean> getType() {
			return DummyBean.class;
		}

	}

	private static class DummyBean implements Serializable {

		private String name;

		private DummyBean parent;

		private List<DummyBean> children = new ArrayList<DummyBean>();

		public DummyBean(String name) {
			this(name, null);
		}

		public DummyBean(String name, DummyBean parent) {
			super();
			this.name = name;
			this.parent = parent;
		}

		public void addChild(DummyBean dummyBean) {
			dummyBean.parent = this;
			children.add(dummyBean);
		}

	}

	private class DummyLazyTreeDataProvider implements LazyTreeDataProvider<DummyBean> {
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
		public String getTaxonomyCode() {
			return null;
		}
	}

	private interface DummyComponentFactory<T extends Component> extends Serializable {

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
			return "LazyTreeAcceptanceTest";
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
