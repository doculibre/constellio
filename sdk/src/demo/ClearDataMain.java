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
package demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClearDataMain {

	public static void main(String[] argv)
			throws Exception {

		DemoUtils.printConfiguration();

		System.out.println("Êtes-vous certain de vouloir supprimer les données? [y/n] + Enter");

		String choice = readChoice();

		if (choice.toLowerCase().equals("y")) {
			System.out.println("\nSuppression des données en cours...\n");
			DemoUtils.clearData();
			System.out.println("\nSuppression des données terminée\n");
		} else {
			System.out.println("\nSuppression des données ANNULÉE\n");
		}

	}

	private static String readChoice() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			return br.readLine();
		} catch (IOException ioe) {
			return "n";
		}
	}

}
