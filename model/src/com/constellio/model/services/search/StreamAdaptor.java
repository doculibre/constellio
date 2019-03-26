package com.constellio.model.services.search;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Iterator;
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
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class StreamAdaptor<T> implements Stream<T> {

	Stream<T> adapted;

	public StreamAdaptor(Stream<T> adapted) {
		this.adapted = adapted;
	}

	@Override
	public Stream<T> filter(Predicate<? super T> predicate) {
		return adapted.filter(predicate);
	}

	@Override
	public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
		return adapted.map(mapper);
	}

	@Override
	public IntStream mapToInt(ToIntFunction<? super T> mapper) {
		return adapted.mapToInt(mapper);
	}

	@Override
	public LongStream mapToLong(ToLongFunction<? super T> mapper) {
		return adapted.mapToLong(mapper);
	}

	@Override
	public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
		return adapted.mapToDouble(mapper);
	}

	@Override
	public <R> Stream<R> flatMap(
			Function<? super T, ? extends Stream<? extends R>> mapper) {
		return adapted.flatMap(mapper);
	}

	@Override
	public IntStream flatMapToInt(
			Function<? super T, ? extends IntStream> mapper) {
		return adapted.flatMapToInt(mapper);
	}

	@Override
	public LongStream flatMapToLong(
			Function<? super T, ? extends LongStream> mapper) {
		return adapted.flatMapToLong(mapper);
	}

	@Override
	public DoubleStream flatMapToDouble(
			Function<? super T, ? extends DoubleStream> mapper) {
		return adapted.flatMapToDouble(mapper);
	}

	@Override
	public Stream<T> distinct() {
		return adapted.distinct();
	}

	@Override
	public Stream<T> sorted() {
		return adapted.sorted();
	}

	@Override
	public Stream<T> sorted(Comparator<? super T> comparator) {
		return adapted.sorted(comparator);
	}

	@Override
	public Stream<T> peek(Consumer<? super T> action) {
		return adapted.peek(action);
	}

	@Override
	public Stream<T> limit(long maxSize) {
		return adapted.limit(maxSize);
	}

	@Override
	public Stream<T> skip(long n) {
		return adapted.skip(n);
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		adapted.forEach(action);
	}

	@Override
	public void forEachOrdered(Consumer<? super T> action) {
		adapted.forEachOrdered(action);
	}

	@NotNull
	@Override
	public Object[] toArray() {
		return adapted.toArray();
	}

	@NotNull
	@Override
	public <A> A[] toArray(IntFunction<A[]> generator) {
		return adapted.toArray(generator);
	}

	@Override
	public T reduce(T identity, BinaryOperator<T> accumulator) {
		return adapted.reduce(identity, accumulator);
	}

	@NotNull
	@Override
	public Optional<T> reduce(BinaryOperator<T> accumulator) {
		return adapted.reduce(accumulator);
	}

	@Override
	public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator,
						BinaryOperator<U> combiner) {
		return adapted.reduce(identity, accumulator, combiner);
	}

	@Override
	public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator,
						 BiConsumer<R, R> combiner) {
		return adapted.collect(supplier, accumulator, combiner);
	}

	@Override
	public <R, A> R collect(Collector<? super T, A, R> collector) {
		return adapted.collect(collector);
	}

	@NotNull
	@Override
	public Optional<T> min(Comparator<? super T> comparator) {
		return adapted.min(comparator);
	}

	@NotNull
	@Override
	public Optional<T> max(Comparator<? super T> comparator) {
		return adapted.max(comparator);
	}

	@Override
	public long count() {
		return adapted.count();
	}

	@Override
	public boolean anyMatch(Predicate<? super T> predicate) {
		return adapted.anyMatch(predicate);
	}

	@Override
	public boolean allMatch(Predicate<? super T> predicate) {
		return adapted.allMatch(predicate);
	}

	@Override
	public boolean noneMatch(Predicate<? super T> predicate) {
		return adapted.noneMatch(predicate);
	}

	@NotNull
	@Override
	public Optional<T> findFirst() {
		return adapted.findFirst();
	}

	@NotNull
	@Override
	public Optional<T> findAny() {
		return adapted.findAny();
	}

	public static <T1> Builder<T1> builder() {
		return Stream.builder();
	}

	@NotNull
	public static <T1> Stream<T1> empty() {
		return Stream.empty();
	}

	@NotNull
	public static <T1> Stream<T1> of(T1 t1) {
		return Stream.of(t1);
	}

	@NotNull
	@SafeVarargs
	public static <T1> Stream<T1> of(T1... values) {
		return Stream.of(values);
	}

	@NotNull
	public static <T1> Stream<T1> iterate(T1 seed, UnaryOperator<T1> f) {
		return Stream.iterate(seed, f);
	}

	@NotNull
	public static <T1> Stream<T1> generate(Supplier<T1> s) {
		return Stream.generate(s);
	}

	@NotNull
	public static <T1> Stream<T1> concat(@NotNull Stream<? extends T1> a,
										 @NotNull Stream<? extends T1> b) {
		return Stream.concat(a, b);
	}

	@NotNull
	@Override
	public Iterator<T> iterator() {
		return adapted.iterator();
	}

	@NotNull
	@Override
	public Spliterator<T> spliterator() {
		return adapted.spliterator();
	}

	@Override
	public boolean isParallel() {
		return adapted.isParallel();
	}

	@NotNull
	@Override
	public Stream<T> sequential() {
		return adapted.sequential();
	}

	@NotNull
	@Override
	public Stream<T> parallel() {
		return adapted.parallel();
	}

	@NotNull
	@Override
	public Stream<T> unordered() {
		return adapted.unordered();
	}

	@NotNull
	@Override
	public Stream<T> onClose(Runnable closeHandler) {
		return adapted.onClose(closeHandler);
	}

	@Override
	public void close() {
		adapted.close();
	}
}
