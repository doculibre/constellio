package com.constellio.app.services.actionDisplayManager;

import org.junit.Test;

import java.util.Locale;
import java.util.Map;

import static com.constellio.app.services.actionDisplayManager.MenuDisplayItemTest.ACTIVE;
import static com.constellio.app.services.actionDisplayManager.MenuDisplayItemTest.ALWAYS_ACTIVE;
import static com.constellio.app.services.actionDisplayManager.MenuDisplayItemTest.CODE;
import static com.constellio.app.services.actionDisplayManager.MenuDisplayItemTest.ICON;
import static com.constellio.app.services.actionDisplayManager.MenusDisplayManagerAcceptanceTest.createStringMap;
import static org.assertj.core.api.Assertions.assertThat;

public class MenuDisplayContainerTest {
	public static final Map<Locale, String> LABELS = createStringMap("containerLabel1Fr", "containerLabel1En");
	public static final Map<Locale, String> LABELS2 = createStringMap("containerLabel2Fr", "containerLabel2En");

	private MenuDisplayContainer menuDisplayContainer = new MenuDisplayContainer(CODE, LABELS, ICON, ACTIVE, ALWAYS_ACTIVE);

	private void assertLabel(MenuDisplayContainer menuDisplayContainer, String fr, String en) {
		assertThat(menuDisplayContainer.getLabels()).hasSize(2);
		assertThat(menuDisplayContainer.getLabels().get(Locale.forLanguageTag("fr"))).isEqualTo(fr);
		assertThat(menuDisplayContainer.getLabels().get(Locale.forLanguageTag("en"))).isEqualTo(en);
	}

	@Test
	public void newMenuDisplayWithNewLabelsIntegrityTest() {
		MenuDisplayContainer newMenuDisplayContainer = menuDisplayContainer.newMenuDisplayContainerWithLabels(LABELS2);

		assertThat(newMenuDisplayContainer.getParentCode()).isEqualTo(null);
		assertLabel(newMenuDisplayContainer, "containerLabel2Fr", "containerLabel2En");
		assertThat(newMenuDisplayContainer.getCode()).isEqualTo(CODE);
		assertThat(newMenuDisplayContainer.getIcon()).isEqualTo(ICON);
		assertThat(newMenuDisplayContainer.getI18nKey()).isEqualTo(null);
		assertThat(newMenuDisplayContainer.isContainer()).isTrue();
		assertThat(newMenuDisplayContainer.isActive()).isTrue();
		assertThat(newMenuDisplayContainer.isAlwaysActive()).isFalse();
	}

	@Test
	public void newMenuDisplayWithNewIconIntegrityTest() {
		MenuDisplayContainer newMenuDisplayContainer = menuDisplayContainer.newMenuDisplayContainerWithIcon("newIcon");

		assertThat(newMenuDisplayContainer.getParentCode()).isEqualTo(null);
		assertLabel(newMenuDisplayContainer, "containerLabel1Fr", "containerLabel1En");
		assertThat(newMenuDisplayContainer.getCode()).isEqualTo(CODE);
		assertThat(newMenuDisplayContainer.getIcon()).isEqualTo("newIcon");
		assertThat(newMenuDisplayContainer.getI18nKey()).isEqualTo(null);
		assertThat(newMenuDisplayContainer.isContainer()).isTrue();
		assertThat(newMenuDisplayContainer.isActive()).isTrue();
		assertThat(newMenuDisplayContainer.isAlwaysActive()).isFalse();
	}

	@Test
	public void newMenuDisplayWithNewActiveIntegrityTest() {
		MenuDisplayContainer newMenuDisplayContainer = menuDisplayContainer.newMenuDisplayContainerWithActive(false);

		assertThat(newMenuDisplayContainer.getParentCode()).isEqualTo(null);
		assertLabel(newMenuDisplayContainer, "containerLabel1Fr", "containerLabel1En");
		assertThat(newMenuDisplayContainer.getCode()).isEqualTo(CODE);
		assertThat(newMenuDisplayContainer.getIcon()).isEqualTo(ICON);
		assertThat(newMenuDisplayContainer.getI18nKey()).isEqualTo(null);
		assertThat(newMenuDisplayContainer.isContainer()).isTrue();
		assertThat(newMenuDisplayContainer.isActive()).isFalse();
		assertThat(newMenuDisplayContainer.isAlwaysActive()).isFalse();
	}

	@Test
	public void newMenuDisplayWithAlwaysActiveIntegrityTest() {
		MenuDisplayContainer newMenuDisplayContainer = menuDisplayContainer.newMenuDisplayContainerWithAlwaysActive(true);

		assertThat(newMenuDisplayContainer.getParentCode()).isEqualTo(null);
		assertLabel(newMenuDisplayContainer, "containerLabel1Fr", "containerLabel1En");
		assertThat(newMenuDisplayContainer.getCode()).isEqualTo(CODE);
		assertThat(newMenuDisplayContainer.getIcon()).isEqualTo(ICON);
		assertThat(newMenuDisplayContainer.getI18nKey()).isEqualTo(null);
		assertThat(newMenuDisplayContainer.isContainer()).isTrue();
		assertThat(newMenuDisplayContainer.isActive()).isTrue();
		assertThat(newMenuDisplayContainer.isAlwaysActive()).isTrue();
	}
}
