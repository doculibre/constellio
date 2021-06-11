package com.constellio.app.services.actionDisplayManager;

import com.constellio.app.services.actionDisplayManager.MenuPositionActionOptions.Position;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MenuPositionActionOptionsTest {
	@Test
	public void constructorValidParam() {
		MenuPositionActionOptions after = MenuPositionActionOptions.displayActionAfter("after");
		assertThat(after.getPosition() == Position.AFTER).isTrue();
		assertThat(after.getRelativeActionCode()).isEqualTo("after");

		MenuPositionActionOptions before = MenuPositionActionOptions.displayActionBefore("before");
		assertThat(before.getPosition() == Position.BEFORE).isTrue();
		assertThat(before.getRelativeActionCode()).isEqualTo("before");

		MenuPositionActionOptions atEnd = MenuPositionActionOptions.displayActionAtEnd();
		assertThat(atEnd.getPosition() == Position.AT_END).isTrue();
		assertThat(atEnd.getRelativeActionCode()).isNull();

		MenuPositionActionOptions atBeginning = MenuPositionActionOptions.displayActionAtBeginning();
		assertThat(atBeginning.getPosition() == Position.AT_BEGINNING).isTrue();
		assertThat(atBeginning.getRelativeActionCode()).isNull();
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorInvalidBeforeMenuActionEmptyParams() {
		MenuPositionActionOptions after = MenuPositionActionOptions.displayActionBefore("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorInvalidBeforeMenuActionNullParams() {
		MenuPositionActionOptions after = MenuPositionActionOptions.displayActionBefore(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorInvalidAfterMenuActionEmptyParams() {
		MenuPositionActionOptions after = MenuPositionActionOptions.displayActionAfter("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorInvalidAfterMenuActionNullParams() {
		MenuPositionActionOptions after = MenuPositionActionOptions.displayActionAfter(null);
	}
}
