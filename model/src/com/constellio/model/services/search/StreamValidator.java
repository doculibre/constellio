package com.constellio.model.services.search;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class StreamValidator<T> implements Stream<T> {

	Stream<T> stream1;
	Stream<T> stream2;

	public StreamValidator(Stream<T> stream1, Stream<T> stream2) {
		this.stream1 = stream1;
		this.stream2 = stream2;
	}


	@Override
	public Stream<T> filter(Predicate<? super T> predicate) {
		stream1.filter(predicate);
		stream2.filter(predicate);
		return this;
	}

	@Override
	public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
		Stream<R> s1 = stream1.map(mapper);
		Stream<R> s2 = stream2.map(mapper);
		return new StreamValidator<>(s1, s2);
	}

	@Override
	public IntStream mapToInt(ToIntFunction<? super T> mapper) {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().mapToInt(mapper);
	}

	@Override
	public LongStream mapToLong(ToLongFunction<? super T> mapper) {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().mapToLong(mapper);
	}

	@Override
	public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().mapToDouble(mapper);
	}

	@Override
	public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
		Stream<R> s1 = stream1.flatMap(mapper);
		Stream<R> s2 = stream2.flatMap(mapper);
		return new StreamValidator<>(s1, s2);
	}

	@Override
	public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().flatMapToInt(mapper);
	}

	@Override
	public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().flatMapToLong(mapper);
	}

	@Override
	public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().flatMapToDouble(mapper);
	}

	@Override
	public Stream<T> distinct() {
		Stream<T> s1 = stream1.distinct();
		Stream<T> s2 = stream2.distinct();
		return new StreamValidator<>(s1, s2);
	}

	@Override
	public Stream<T> sorted() {
		Stream<T> s1 = stream1.sorted();
		Stream<T> s2 = stream2.sorted();
		return new StreamValidator<>(s1, s2);
	}

	@Override
	public Stream<T> sorted(Comparator<? super T> comparator) {
		Stream<T> s1 = stream1.sorted(comparator);
		Stream<T> s2 = stream2.sorted(comparator);
		return new StreamValidator<>(s1, s2);
	}

	@Override
	public Stream<T> peek(Consumer<? super T> action) {
		Stream<T> s1 = stream1.peek(action);
		Stream<T> s2 = stream2.peek(action);
		return new StreamValidator<>(s1, s2);
	}

	@Override
	public Stream<T> limit(long maxSize) {
		Stream<T> s1 = stream1.limit(maxSize);
		Stream<T> s2 = stream2.limit(maxSize);
		return new StreamValidator<>(s1, s2);
	}

	@Override
	public Stream<T> skip(long n) {
		Stream<T> s1 = stream1.skip(n);
		Stream<T> s2 = stream2.skip(n);
		return new StreamValidator<>(s1, s2);
	}

	@Override
	public void forEach(Consumer<? super T> action) {

		List<T> stream1Records = consumeAndValidateEquality();

		stream1Records.stream().forEach(action);

	}

	@NotNull
	private List<T> consumeAndValidateEquality() {
		List<T> stream1Records = stream1.collect(Collectors.toList());
		List<T> stream2Records = stream2.collect(Collectors.toList());

		if (!stream1Records.equals(stream2Records)) {
			throw new IllegalArgumentException("Lists are different");
		}
		return stream1Records;
	}

	@Override
	public void forEachOrdered(Consumer<? super T> action) {
		forEach(action);
	}

	@NotNull
	@Override
	public Object[] toArray() {
		List<Object> items = collect(toList());
		return items.toArray();
	}

	@NotNull
	@Override
	public <A> A[] toArray(IntFunction<A[]> generator) {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().toArray(generator);
	}

	@Override
	public T reduce(T identity, BinaryOperator<T> accumulator) {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().reduce(identity, accumulator);
	}

	@NotNull
	@Override
	public Optional<T> reduce(BinaryOperator<T> accumulator) {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().reduce(accumulator);
	}

	@Override
	public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().reduce(identity, accumulator, combiner);
	}

	@Override
	public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().collect(supplier, accumulator, combiner);
	}

	@Override
	public <R, A> R collect(Collector<? super T, A, R> collector) {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().collect(collector);
	}

	@NotNull
	@Override
	public Optional<T> min(Comparator<? super T> comparator) {
		throw new UnsupportedOperationException("Unsupported");
	}

	@NotNull
	@Override
	public Optional<T> max(Comparator<? super T> comparator) {
		throw new UnsupportedOperationException("Unsupported");
	}

	@Override
	public long count() {
		return collect(toList()).size();
	}

	@Override
	public boolean anyMatch(Predicate<? super T> predicate) {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().anyMatch(predicate);
	}

	@Override
	public boolean allMatch(Predicate<? super T> predicate) {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().allMatch(predicate);
	}

	@Override
	public boolean noneMatch(Predicate<? super T> predicate) {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().noneMatch(predicate);
	}

	@NotNull
	@Override
	public Optional<T> findFirst() {
		List<T> list = collect(toList());
		if (!list.isEmpty()) {
			return Optional.ofNullable(list.get(0));
		} else {
			return Optional.empty();
		}
	}

	@NotNull
	@Override
	public Optional<T> findAny() {
		return findFirst();
	}

	@NotNull
	@Override
	public Iterator<T> iterator() {
		List<T> list = collect(toList());
		return list.iterator();
	}

	@NotNull
	@Override
	public Spliterator<T> spliterator() {
		List<T> list = collect(toList());
		return list.spliterator();
	}

	@Override
	public boolean isParallel() {
		return false;
	}

	@NotNull
	@Override
	public Stream<T> sequential() {
		List<T> stream1Records = consumeAndValidateEquality();
		return stream1Records.stream().sequential();
	}

	@NotNull
	@Override
	public Stream<T> parallel() {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().parallel();
	}

	@NotNull
	@Override
	public Stream<T> unordered() {
		List<T> stream1Records = consumeAndValidateEquality();

		return stream1Records.stream().unordered();
	}

	@NotNull
	@Override
	public Stream<T> onClose(Runnable closeHandler) {
		return this;
	}

	@Override
	public void close() {

	}
}
