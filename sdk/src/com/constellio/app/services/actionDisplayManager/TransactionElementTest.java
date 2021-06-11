package com.constellio.app.services.actionDisplayManager;

import com.constellio.app.services.actionDisplayManager.MenuPositionActionOptions.Position;
import com.constellio.app.services.actionDisplayManager.MenusDisplayTransaction.Action;
import com.constellio.app.services.actionDisplayManager.MenusDisplayTransaction.TransactionElement;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionElementTest {

	MenuDisplayItem menuDisplayitemOriginal = new MenuDisplayItem("MenuCode", "icon", "i18nKey");

	@Test
	public void constructorValidParameters() {


		TransactionElement menusDisplayTransaction1 = new TransactionElement("schemaType", Action.REMOVE, menuDisplayitemOriginal, null);
		assertThat(menusDisplayTransaction1.getAction() == Action.REMOVE).isTrue();
		assertThat(menusDisplayTransaction1.getSchemaType()).isEqualTo("schemaType");
		assertThat(menusDisplayTransaction1.getMenuPositionActionOptions()).isEqualTo(null);

		MenuDisplayItem menuDisplayItem1 = menusDisplayTransaction1.getMenuDisplayItem();
		assertThat(menuDisplayItem1.getCode()).isEqualTo("MenuCode");
		assertThat(menuDisplayItem1.getIcon()).isEqualTo("icon");
		assertThat(menuDisplayItem1.getI18nKey()).isEqualTo("i18nKey");


		TransactionElement menusDisplayTransaction2 = new TransactionElement("schemaType", Action.ADD_UPDATE, menuDisplayitemOriginal, MenuPositionActionOptions.displayActionAfter("after"));
		assertThat(menusDisplayTransaction2.getAction() == Action.ADD_UPDATE).isTrue();
		assertThat(menusDisplayTransaction2.getMenuPositionActionOptions()).isNotNull();
		assertThat(menusDisplayTransaction2.getMenuPositionActionOptions().getPosition() == Position.AFTER);
		assertThat(menusDisplayTransaction2.getMenuPositionActionOptions().getRelativeActionCode()).isEqualTo("after");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorNoSchema() {
		new TransactionElement(null, Action.REMOVE, menuDisplayitemOriginal, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorNoMenuDisplay() {
		new TransactionElement("schemaType", Action.REMOVE, null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorNoAction() {
		new TransactionElement("schemaType", null, menuDisplayitemOriginal, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorNO() {
		new TransactionElement("schemaType", Action.ADD_UPDATE, menuDisplayitemOriginal, null);
	}
}
