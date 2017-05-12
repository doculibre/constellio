package com.constellio.data.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import com.constellio.data.utils.Octets;

public class RandomWordsIterator implements Iterator<String> {

	static int RANDOM_INDEXES_LENGTH = 1000000;

	int current = 0;
	List<String> words;
	List<Integer> randomIndexes;

	private RandomWordsIterator(List<String> words, List<Integer> randomIndexes) {
		this.words = words;
		this.randomIndexes = randomIndexes;
	}

	public static RandomWordsIterator createFor(File file) {
		List<String> words = null;
		try {
			words = loadDicWords(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		List<Integer> randomIndexesLength = createRandomIndexesListOfSize(words.size());

		return new RandomWordsIterator(words, randomIndexesLength);
	}

	private static List<Integer> createRandomIndexesListOfSize(int size) {
		List<Integer> randomIndexes = new ArrayList<>();

		Random random = new Random();
		for (int i = 0; i < RANDOM_INDEXES_LENGTH; i++) {
			randomIndexes.add(random.nextInt(size));
		}

		for (int i = 2; i < (RANDOM_INDEXES_LENGTH / 10); i++) {
			int current = 0;
			while (current < RANDOM_INDEXES_LENGTH) {
				randomIndexes.add(randomIndexes.get(current));
				current += i;
			}
			current = 0;
			while (current < RANDOM_INDEXES_LENGTH) {
				randomIndexes.add(randomIndexes.get(RANDOM_INDEXES_LENGTH - current));
				current += i;
			}
		}

		return randomIndexes;
	}

	private static List<String> loadDicWords(File file)
			throws IOException {
		List<String> words = new ArrayList<>();
		for (String line : FileUtils.readLines(file, "latin1")) {
			int slashIndex = line.indexOf("/");
			if (slashIndex > 0) {
				words.add(line.substring(0, slashIndex).toLowerCase());
			}
		}
		Collections.shuffle(words);
		return words;
	}

	public String nextWords(int nbWords) {
		StringBuilder stringBuilder = new StringBuilder(next());
		for (int i = 1; i < nbWords; i++) {
			stringBuilder.append(" ");
			stringBuilder.append(next());
		}

		return stringBuilder.toString();
	}

	public String nextWordsOfLength(Octets octets) {
		StringBuilder stringBuilder = new StringBuilder(next());
		stringBuilder.append(next());
		long nbCharactersEstimation = (long) (0.95 * octets.getOctets());
		while (stringBuilder.length() < nbCharactersEstimation) {
			stringBuilder.append(" ");
			stringBuilder.append(next());
		}
		return stringBuilder.toString();
	}

	@Override
	public boolean hasNext() {
		throw new RuntimeException("This iterator is infinite, do not try to iterate on it");
	}

	@Override
	public String next() {

		int randomIndex = randomIndexes.get(current);
		String word = words.get(randomIndex);
		current++;
		if (current >= randomIndexes.size()) {
			current = 0;
		}
		return word;
	}

	@Override
	public void remove() {
		throw new RuntimeException("Unsupported");
	}

	public RandomWordsIterator createCopy() {
		return new RandomWordsIterator(words, randomIndexes);
	}
}
