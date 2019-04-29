package com.constellio.model.services.records.cache2;

public class test {

	public static void main(String[] args) {

		test(999999);
		test(1000000);
		test(1000001);

	}

	private static void test(int i) {
		System.out.println("Testing with " + i);

		System.out.println(i / 1000000);
		System.out.println(i % 1000000);

		System.out.println();
	}

}
