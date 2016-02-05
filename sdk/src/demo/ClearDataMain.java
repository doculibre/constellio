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
