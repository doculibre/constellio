package com.constellio.app.services.actionDisplayManager;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MenuDisplayItemTest {
	public static final String ICON = "icon";
	public static final String PARENT_CODE = "parentCode";
	public static final String CODE = "code";
	public static final String I18NKEY = "i18nkey";
	public static final boolean ACTIVE = true;
	public static final boolean ALWAYS_ACTIVE = false;

	private static MenuDisplayItem MENU_DISPLAY_ITEM = new MenuDisplayItem(CODE, ICON, I18NKEY, ACTIVE, PARENT_CODE, ALWAYS_ACTIVE);
	private static MenuDisplayContainer MENU_DISPLAY_CONTAINER = new MenuDisplayContainer(CODE, MenuDisplayContainerTest.LABELS, ICON, ACTIVE, ALWAYS_ACTIVE);

	@Test(expected = IllegalArgumentException.class)
	public void newMenuDisplayWithParentCodeOnlyMenuDisplayItem() {
		MENU_DISPLAY_CONTAINER.newMenuDisplayWithParentCode("parentCode2");
	}

	@Test(expected = IllegalArgumentException.class)
	public void newMenuDisplayItemWithIconMenuDisplayItem() {
		MENU_DISPLAY_CONTAINER.newMenuDisplayItemWithIcon("newIcon");
	}

	@Test(expected = IllegalArgumentException.class)
	public void newMenuDisplayItemWithActiveTest() {
		MENU_DISPLAY_CONTAINER.newMenuDisplayItemWithActive(false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void newMenuDisplayItemWithAlwaysActiveTest() {
		MENU_DISPLAY_CONTAINER.newMenuDisplayItemWithActive(true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void newMenuDisplayItemWithI18nKeyTest() {
		MENU_DISPLAY_CONTAINER.newMenuDisplayItemWithI18nKey("newi18nkey");
	}

	@Test
	public void newMenuDisplayWithParentCodeIntegrityTest() {
		MenuDisplayItem menuDisplayItem = MENU_DISPLAY_ITEM.newMenuDisplayWithParentCode("parentCode2");
		assertThat(menuDisplayItem.getParentCode()).isEqualTo("parentCode2");
		assertThat(menuDisplayItem.getCode()).isEqualTo(CODE);
		assertThat(menuDisplayItem.getIcon()).isEqualTo(ICON);
		assertThat(menuDisplayItem.getI18nKey()).isEqualTo(I18NKEY);
		assertThat(menuDisplayItem.isContainer()).isFalse();
		assertThat(menuDisplayItem.isActive()).isTrue();
		assertThat(menuDisplayItem.isAlwaysActive()).isFalse();
	}

	@Test
	public void newMenuDisplayItemWithIconIntegrityTest() {
		MenuDisplayItem menuDisplayItem = MENU_DISPLAY_ITEM.newMenuDisplayItemWithIcon("newIcon");
		assertThat(menuDisplayItem.getParentCode()).isEqualTo(PARENT_CODE);
		assertThat(menuDisplayItem.getCode()).isEqualTo(CODE);
		assertThat(menuDisplayItem.getIcon()).isEqualTo("newIcon");
		assertThat(menuDisplayItem.getI18nKey()).isEqualTo(I18NKEY);
		assertThat(menuDisplayItem.isContainer()).isFalse();
		assertThat(menuDisplayItem.isActive()).isTrue();
		assertThat(menuDisplayItem.isAlwaysActive()).isFalse();
	}

	@Test
	public void newMenuDisplayItemWithActiveIntegrityTest() {
		MenuDisplayItem menuDisplayItem = MENU_DISPLAY_ITEM.newMenuDisplayItemWithActive(false);
		assertThat(menuDisplayItem.getParentCode()).isEqualTo(PARENT_CODE);
		assertThat(menuDisplayItem.getCode()).isEqualTo(CODE);
		assertThat(menuDisplayItem.getIcon()).isEqualTo(ICON);
		assertThat(menuDisplayItem.getI18nKey()).isEqualTo(I18NKEY);
		assertThat(menuDisplayItem.isContainer()).isFalse();
		assertThat(menuDisplayItem.isActive()).isFalse();
		assertThat(menuDisplayItem.isAlwaysActive()).isFalse();
	}

	@Test
	public void newMenuDisplayItemWithAlwaysActiveIntegrityTest() {
		MenuDisplayItem menuDisplayItem = MENU_DISPLAY_ITEM.newMenuDisplayItemWithAlwaysActive(true);
		assertThat(menuDisplayItem.getParentCode()).isEqualTo(PARENT_CODE);
		assertThat(menuDisplayItem.getCode()).isEqualTo(CODE);
		assertThat(menuDisplayItem.getIcon()).isEqualTo(ICON);
		assertThat(menuDisplayItem.getI18nKey()).isEqualTo(I18NKEY);
		assertThat(menuDisplayItem.isContainer()).isFalse();
		assertThat(menuDisplayItem.isActive()).isTrue();
		assertThat(menuDisplayItem.isAlwaysActive()).isTrue();
	}

	@Test
	public void newMenuDisplayItemWithI18nKeyIntegrityTest() {
		MenuDisplayItem menuDisplayItem = MENU_DISPLAY_ITEM.newMenuDisplayItemWithI18nKey("newi18nkey");
		assertThat(menuDisplayItem.getParentCode()).isEqualTo(PARENT_CODE);
		assertThat(menuDisplayItem.getCode()).isEqualTo(CODE);
		assertThat(menuDisplayItem.getIcon()).isEqualTo(ICON);
		assertThat(menuDisplayItem.getI18nKey()).isEqualTo("newi18nkey");
		assertThat(menuDisplayItem.isContainer()).isFalse();
		assertThat(menuDisplayItem.isActive()).isTrue();
		assertThat(menuDisplayItem.isAlwaysActive()).isFalse();
	}
}
