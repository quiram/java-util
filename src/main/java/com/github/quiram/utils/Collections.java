package com.github.quiram.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.quiram.utils.ArgumentChecks.ensure;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toSet;

public class Collections {
    public static <T> Optional<T> head(List<T> list) {
        return isNull(list) || list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public static <T, R> R map(T o, Function<T, R> mapper) {
        return o == null ? null : mapper.apply(o);
    }

    public static <T> String mapToString(T o) {
        return map(o, T::toString);
    }

    public static <In, Out> List<Out> map(List<In> in, Function<In, Out> mapper) {
        return in.stream().map(mapper).collect(Collectors.toList());
    }

    public static <In, Out> List<Out> flatMap(List<In> in, Function<In, List<Out>> mapper) {
        return in.stream().map(mapper).flatMap(List::stream).collect(Collectors.toList());
    }

    public static <In, Mid, Out> List<Out> map(List<In> in, Function<In, Mid> mapper1, Function<Mid, Out> mapper2) {
        return in.stream().map(mapper1).map(mapper2).collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> List<T> filter(List<T> in, Predicate<T>... filters) {
        return stream(filters).reduce(in.stream(), Stream::filter, (s1, s2) -> s2).collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> Set<T> filter(Set<T> in, Predicate<T>... filters) {
        return stream(filters).reduce(in.stream(), Stream::filter, (s1, s2) -> s2).collect(toSet());
    }

    public static <T> List<T> concat(T item, List<T> list2) {
        return concatLists(singletonList(item), list2);
    }

    public static <T> List<T> concat(List<T> list2, T item) {
        return concatLists(list2, singletonList(item));
    }

    @SafeVarargs
    public static <T> List<T> concat(List<T>... lists) {
        return concatLists(lists);
    }

    @SafeVarargs
    public static <T> List<T> concatLists(List<T>... lists) {
        return Streams.concat(stream(lists).map(List::stream)).collect(Collectors.toList());
    }


    public static <T> Optional<T> findFirst(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).findFirst();
    }

    public static <T, R> Set<R> map(Set<T> in, Function<T, R> mapper) {
        return in.stream().map(mapper).collect(toSet());
    }

    public static <T> List<T> toList(Collection<T> collection) {
        return new ArrayList<>(collection);
    }

    public static <K, V> Map<K, V> toMap(Collection<V> items, Function<V, K> key) {
        return items.stream().collect(Collectors.toMap(key, identity()));
    }

    public static <K, V1, V2> Map<K, V2> toMap(Collection<V1> items, Function<V1, K> key, Function<V1, V2> value) {
        return items.stream().collect(Collectors.toMap(key, value));
    }

    @SafeVarargs
    public static <K, V> Map<K, V> mergeMaps(BiFunction<V, V, V> merger, Map<K, V> map1, Map<K, V> map2, Map<K, V>... moreMaps) {
        return merge(merger, map1, map2, moreMaps);
    }

    @SafeVarargs
    public static <K, V> Map<K, V> merge(BiFunction<V, V, V> merger, Map<K, V> map1, Map<K, V> map2, Map<K, V>... moreMaps) {
        Map<K, V> finalMap = new HashMap<>();
        List<Map<K, V>> allMaps = new LinkedList<>();
        allMaps.add(map1);
        allMaps.add(map2);
        allMaps.addAll(asList(moreMaps));
        allMaps.forEach(m -> m.forEach((i, bd) -> finalMap.merge(i, bd, merger)));

        return finalMap;
    }

    @SafeVarargs
    public static <T> Set<T> mergeSets(Set<T> set1, Set<T> set2, Set<T>... moreSets) {
        return merge(set1, set2, moreSets);
    }

    @SafeVarargs
    public static <T> Set<T> merge(Set<T> set1, Set<T> set2, Set<T>... moreSets) {
        final HashSet<T> finalSet = new HashSet<>(set1);
        finalSet.addAll(set2);
        asList(moreSets).forEach(finalSet::addAll);

        return finalSet;
    }

    public static <T> Set<T> intersect(Set<T> set1, Set<T> set2) {
        final HashSet<T> result = new HashSet<>(set1);
        result.retainAll(set2);
        return result;
    }

    public static <T> List<List<T>> transpose(List<List<T>> listOfLists) {
        if (listOfLists == null)
            return null;

        if (listOfLists.isEmpty() || listOfLists.get(0).isEmpty())
            return listOfLists;

        final String errorMessage = "be square (all sublists should be the same size)";
        ensure(listOfLists, "listOfLists", Collections::listsHaveSameSize, errorMessage);

        final LinkedList<List<T>> outerList = new LinkedList<>();

        IntStream.range(0, listOfLists.get(0).size()).forEach(i -> {
            final LinkedList<T> innerList = new LinkedList<>();
            outerList.add(innerList);
            for (List<T> listOfList : listOfLists) {
                innerList.add(listOfList.get(i));
            }
        });

        return outerList;
    }

    public static <T1, T2, R> Stream<Pair<T1, T2>> cartesianProduct(Collection<T1> collection1, Collection<T2> collection2) {
        return cartesianProduct(collection1, collection2, Pair::of);
    }

    public static <T1, T2, T3, R> Stream<Triple<T1, T2, T3>> cartesianProduct(Collection<T1> collection1, Collection<T2> collection2,
                                                                              Collection<T3> collection3) {
        return cartesianProduct(collection1, collection2, collection3, Triple::of);
    }

    public static <T1, T2, R> Stream<R> cartesianProduct(Collection<T1> collection1, Collection<T2> collection2, BiFunction<T1, T2, R> combiningFunction) {
        return collection1.stream().flatMap(
                item1 -> collection2.stream().map(item2 ->
                        combiningFunction.apply(item1, item2))
        );
    }

    public static <T1, T2, T3, R> Stream<R> cartesianProduct(Collection<T1> collection1, Collection<T2> collection2, Collection<T3> collection3,
                                                             TriFunction<T1, T2, T3, R> combiningFunction) {
        return collection1.stream().flatMap(
                item1 -> collection2.stream().flatMap(item2 ->
                        collection3.stream().map((item3 ->
                                combiningFunction.apply(item1, item2, item3))
                        )
                )
        );
    }

    private static <T> boolean listsHaveSameSize(List<List<T>> l) {
        return l.stream().map(List::size).distinct().count() == 1;
    }
}
