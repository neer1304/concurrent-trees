/**
 * Copyright 2012 Niall Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.concurrenttrees.suffix;

import com.googlecode.concurrenttrees.common.KeyValuePair;
import com.googlecode.concurrenttrees.common.PrettyPrintUtil;
import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.node.NodeFactory;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * @author Niall Gallagher
 */
public class ConcurrentSuffixTreeTest {

    private final NodeFactory nodeFactory = new DefaultCharArrayNodeFactory();

    @Test
    public void testPut_SingleKey() throws Exception {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();
        tree.put("BANANA", 1);

        // Suffixes:
        //
        //    BANANA
        //    ANANA
        //    NANA
        //    ANA
        //    NA
        //    A

        // Expected suffix tree:
        //
        //    ○
        //    ├── ○ A ([BANANA])
        //    │   └── ○ NA ([BANANA])
        //    │       └── ○ NA ([BANANA])
        //    ├── ○ BANANA ([BANANA])
        //    └── ○ NA ([BANANA])
        //        └── ○ NA ([BANANA])

        String expected =
                "○\n" +
                "├── ○ A ([BANANA])\n" +
                "│   └── ○ NA ([BANANA])\n" +
                "│       └── ○ NA ([BANANA])\n" +
                "├── ○ BANANA ([BANANA])\n" +
                "└── ○ NA ([BANANA])\n" +
                "    └── ○ NA ([BANANA])\n";
        String actual = PrettyPrintUtil.prettyPrint(tree);
        assertEquals(expected, actual);
    }

    @Test
    public void testPut_MultipleKeys() throws Exception {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();
        tree.put("BANANA", 1);
        tree.put("BANDANA", 2);

        // Suffixes:
        //
        //    BANANA
        //    ANANA
        //    NANA
        //    ANA
        //    NA
        //    A
        //
        //    BANDANA
        //    ANDANA
        //    NDANA
        //    DANA
        //    ANA
        //    NA
        //    A

        // Expected suffix tree:
        //
        //    ○
        //    ├── ○ A ([BANANA, BANDANA])
        //    │   └── ○ N
        //    │       ├── ○ A ([BANANA, BANDANA])
        //    │       │   └── ○ NA ([BANANA])
        //    │       └── ○ DANA ([BANDANA])
        //    ├── ○ BAN
        //    │   ├── ○ ANA ([BANANA])
        //    │   └── ○ DANA ([BANDANA])
        //    ├── ○ DANA ([BANDANA])
        //    └── ○ N
        //        ├── ○ A ([BANANA, BANDANA])
        //        │   └── ○ NA ([BANANA])
        //        └── ○ DANA ([BANDANA])


        String expected =
                "○\n" +
                "├── ○ A ([BANANA, BANDANA])\n" +
                "│   └── ○ N\n" +
                "│       ├── ○ A ([BANANA, BANDANA])\n" +
                "│       │   └── ○ NA ([BANANA])\n" +
                "│       └── ○ DANA ([BANDANA])\n" +
                "├── ○ BAN\n" +
                "│   ├── ○ ANA ([BANANA])\n" +
                "│   └── ○ DANA ([BANDANA])\n" +
                "├── ○ DANA ([BANDANA])\n" +
                "└── ○ N\n" +
                "    ├── ○ A ([BANANA, BANDANA])\n" +
                "    │   └── ○ NA ([BANANA])\n" +
                "    └── ○ DANA ([BANDANA])\n";
        String actual = PrettyPrintUtil.prettyPrint(tree);
        assertEquals(expected, actual);
    }

    @Test
    public void testPut_ReplaceValue() throws Exception {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();
        tree.put("BANANA", 1);
        tree.put("BANANA", 2);

        String expected =
                "○\n" +
                "├── ○ A ([BANANA])\n" +
                "│   └── ○ NA ([BANANA])\n" +
                "│       └── ○ NA ([BANANA])\n" +
                "├── ○ BANANA ([BANANA])\n" +
                "└── ○ NA ([BANANA])\n" +
                "    └── ○ NA ([BANANA])\n";
        String actual = PrettyPrintUtil.prettyPrint(tree);
        assertEquals(expected, actual);
        assertEquals(Integer.valueOf(2), tree.getValueForExactKey("BANANA"));
    }

    @Test
    public void testPutIfAbsent() throws Exception {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();
        tree.putIfAbsent("BANANA", 1);
        tree.putIfAbsent("BANANA", 2); // should be ignored

        String expected =
                "○\n" +
                "├── ○ A ([BANANA])\n" +
                "│   └── ○ NA ([BANANA])\n" +
                "│       └── ○ NA ([BANANA])\n" +
                "├── ○ BANANA ([BANANA])\n" +
                "└── ○ NA ([BANANA])\n" +
                "    └── ○ NA ([BANANA])\n";
        String actual = PrettyPrintUtil.prettyPrint(tree);
        assertEquals(expected, actual);
        assertEquals(Integer.valueOf(1), tree.getValueForExactKey("BANANA"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPut_ArgumentValidation1() {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();
        //noinspection NullableProblems
        tree.put(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPut_ArgumentValidation2() {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();
        //noinspection NullableProblems
        tree.put("FOO", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPut_ArgumentValidation3() {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();
        //noinspection NullableProblems
        tree.put("", 1);
    }

    @Test
    public void testRemove_RemoveSecondKey() throws Exception {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();

        String expected, actual;
        tree.put("BANANA", 1);
        tree.put("BANDANA", 2);

        expected =
                "○\n" +
                "├── ○ A ([BANANA, BANDANA])\n" +
                "│   └── ○ N\n" +
                "│       ├── ○ A ([BANANA, BANDANA])\n" +
                "│       │   └── ○ NA ([BANANA])\n" +
                "│       └── ○ DANA ([BANDANA])\n" +
                "├── ○ BAN\n" +
                "│   ├── ○ ANA ([BANANA])\n" +
                "│   └── ○ DANA ([BANDANA])\n" +
                "├── ○ DANA ([BANDANA])\n" +
                "└── ○ N\n" +
                "    ├── ○ A ([BANANA, BANDANA])\n" +
                "    │   └── ○ NA ([BANANA])\n" +
                "    └── ○ DANA ([BANDANA])\n";
        actual = PrettyPrintUtil.prettyPrint(tree);
        assertEquals(expected, actual);

        boolean removed = tree.remove("BANDANA");
        assertTrue(removed);

        expected =
                "○\n" +
                "├── ○ A ([BANANA])\n" +
                "│   └── ○ NA ([BANANA])\n" +
                "│       └── ○ NA ([BANANA])\n" +
                "├── ○ BANANA ([BANANA])\n" +
                "└── ○ NA ([BANANA])\n" +
                "    └── ○ NA ([BANANA])\n";
        actual = PrettyPrintUtil.prettyPrint(tree);
        assertEquals(expected, actual);
        assertNull(tree.getValueForExactKey("BANDANA"));
    }

    @Test
    public void testRemove_RemoveFirstKey() throws Exception {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();

        String expected, actual;
        tree.put("BANANA", 1);
        tree.put("BANDANA", 2);

        expected =
                "○\n" +
                "├── ○ A ([BANANA, BANDANA])\n" +
                "│   └── ○ N\n" +
                "│       ├── ○ A ([BANANA, BANDANA])\n" +
                "│       │   └── ○ NA ([BANANA])\n" +
                "│       └── ○ DANA ([BANDANA])\n" +
                "├── ○ BAN\n" +
                "│   ├── ○ ANA ([BANANA])\n" +
                "│   └── ○ DANA ([BANDANA])\n" +
                "├── ○ DANA ([BANDANA])\n" +
                "└── ○ N\n" +
                "    ├── ○ A ([BANANA, BANDANA])\n" +
                "    │   └── ○ NA ([BANANA])\n" +
                "    └── ○ DANA ([BANDANA])\n";
        actual = PrettyPrintUtil.prettyPrint(tree);
        assertEquals(expected, actual);

        boolean removed = tree.remove("BANANA");
        assertTrue(removed);

        expected =
                "○\n" +
                "├── ○ A ([BANDANA])\n" +
                "│   └── ○ N\n" +
                "│       ├── ○ A ([BANDANA])\n" +
                "│       └── ○ DANA ([BANDANA])\n" +
                "├── ○ BANDANA ([BANDANA])\n" +
                "├── ○ DANA ([BANDANA])\n" +
                "└── ○ N\n" +
                "    ├── ○ A ([BANDANA])\n" +
                "    └── ○ DANA ([BANDANA])\n";
        actual = PrettyPrintUtil.prettyPrint(tree);
        assertEquals(expected, actual);
        assertNull(tree.getValueForExactKey("BANANA"));
    }

    @Test
    public void testRemove_RemoveNonExistentKey() throws Exception {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();

        String expected, actual;
        tree.put("BANANA", 1);
        tree.put("BANDANA", 2);

        expected =
                "○\n" +
                "├── ○ A ([BANANA, BANDANA])\n" +
                "│   └── ○ N\n" +
                "│       ├── ○ A ([BANANA, BANDANA])\n" +
                "│       │   └── ○ NA ([BANANA])\n" +
                "│       └── ○ DANA ([BANDANA])\n" +
                "├── ○ BAN\n" +
                "│   ├── ○ ANA ([BANANA])\n" +
                "│   └── ○ DANA ([BANDANA])\n" +
                "├── ○ DANA ([BANDANA])\n" +
                "└── ○ N\n" +
                "    ├── ○ A ([BANANA, BANDANA])\n" +
                "    │   └── ○ NA ([BANANA])\n" +
                "    └── ○ DANA ([BANDANA])\n";
        actual = PrettyPrintUtil.prettyPrint(tree);
        assertEquals(expected, actual);

        boolean removed = tree.remove("APPLE");
        assertFalse(removed);

        actual = PrettyPrintUtil.prettyPrint(tree);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetValueForExactKey() throws Exception {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();

        tree.put("BANANA", 1);
        tree.put("BANDANA", 2);

        assertEquals(Integer.valueOf(1), tree.getValueForExactKey("BANANA"));
        assertEquals(Integer.valueOf(2), tree.getValueForExactKey("BANDANA"));
        assertNull(tree.getValueForExactKey("BAN"));
        assertNull(tree.getValueForExactKey("ANA"));
    }

    @Test
    public void testGetKeysEndingWith() throws Exception {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();

        tree.put("BANANA", 1);
        tree.put("BANDANA", 2);

        assertEquals("[BANANA, BANDANA]", tree.getKeysEndingWith("ANA").toString());
        assertEquals("[BANDANA]", tree.getKeysEndingWith("DANA").toString());
        assertEquals("[]", tree.getKeysEndingWith("BAN").toString());
        assertEquals("[]", tree.getKeysEndingWith("").toString());
    }

    @Test
    public void testGetValuesForKeysEndingWith() throws Exception {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();

        tree.put("BANANA", 1);
        tree.put("BANDANA", 2);

        assertEquals("[1, 2]", tree.getValuesForKeysEndingWith("ANA").toString());
        assertEquals("[2]", tree.getValuesForKeysEndingWith("DANA").toString());
        assertEquals("[]", tree.getValuesForKeysEndingWith("BAN").toString());
        assertEquals("[]", tree.getValuesForKeysEndingWith("").toString());
    }

    @Test
    public void testGetKeyValuePairsForKeysEndingWith() throws Exception {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();

        tree.put("BANANA", 1);
        tree.put("BANDANA", 2);

        assertEquals("[(BANANA, 1), (BANDANA, 2)]", tree.getKeyValuePairsForKeysEndingWith("ANA").toString());
        assertEquals("[(BANDANA, 2)]", tree.getKeyValuePairsForKeysEndingWith("DANA").toString());
        assertEquals("[]", tree.getKeyValuePairsForKeysEndingWith("BAN").toString());
        assertEquals("[]", tree.getKeyValuePairsForKeysEndingWith("").toString());
    }

    @Test
    public void testGetKeysContaining() throws Exception {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();

        tree.put("BANANA", 1);
        tree.put("BANDANA", 2);

        assertEquals("[BANANA]", tree.getKeysContaining("ANAN").toString());
        assertEquals("[BANDANA]", tree.getKeysContaining("DA").toString());
        assertEquals("[BANANA, BANDANA]", tree.getKeysContaining("AN").toString());
        assertEquals("[BANANA, BANDANA]", tree.getKeysContaining("BAN").toString());
        assertEquals("[BANANA, BANDANA]", tree.getKeysContaining("ANA").toString());
        assertEquals("[]", tree.getKeysContaining("APPLE").toString());
        assertEquals("[BANANA, BANDANA]", tree.getKeysContaining("").toString());
    }

    @Test
    public void testGetValuesForKeysContaining() throws Exception {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();

        tree.put("BANANA", 1);
        tree.put("BANDANA", 2);

        assertEquals("[1]", tree.getValuesForKeysContaining("ANAN").toString());
        assertEquals("[2]", tree.getValuesForKeysContaining("DA").toString());
        assertEquals("[1, 2]", tree.getValuesForKeysContaining("AN").toString());
        assertEquals("[1, 2]", tree.getValuesForKeysContaining("BAN").toString());
        assertEquals("[1, 2]", tree.getValuesForKeysContaining("ANA").toString());
        assertEquals("[]", tree.getValuesForKeysContaining("APPLE").toString());
        assertEquals("[1, 2]", tree.getValuesForKeysContaining("").toString());
    }

    @Test
    public void testGetKeyValuePairsForKeysContaining() throws Exception {
        ConcurrentSuffixTree<Integer> tree = newConcurrentSuffixTreeForUnitTests();

        tree.put("BANANA", 1);
        tree.put("BANDANA", 2);

        assertEquals("[(BANANA, 1)]", tree.getKeyValuePairsForKeysContaining("ANAN").toString());
        assertEquals("[(BANDANA, 2)]", tree.getKeyValuePairsForKeysContaining("DA").toString());
        assertEquals("[(BANANA, 1), (BANDANA, 2)]", tree.getKeyValuePairsForKeysContaining("AN").toString());
        assertEquals("[(BANANA, 1), (BANDANA, 2)]", tree.getKeyValuePairsForKeysContaining("BAN").toString());
        assertEquals("[(BANANA, 1), (BANDANA, 2)]", tree.getKeyValuePairsForKeysContaining("ANA").toString());
        assertEquals("[]", tree.getKeyValuePairsForKeysContaining("APPLE").toString());
        assertEquals("[(BANANA, 1), (BANDANA, 2)]", tree.getKeyValuePairsForKeysContaining("").toString());
    }

    @Test
    public void testRestrictConcurrency() {
        ConcurrentSuffixTree<Integer> tree = new ConcurrentSuffixTree<Integer>(nodeFactory, true);
        assertNotNull(tree);
    }

    @Test
    public void testCreateSetForOriginalKeys() {
        // Test the default (production) implementation of this method, should return a set based on ConcurrentHashMap...
        ConcurrentSuffixTree<Integer> tree = new ConcurrentSuffixTree<Integer>(nodeFactory, true);
        assertTrue(tree.createSetForOriginalKeys().getClass().equals(Collections.newSetFromMap(new ConcurrentHashMap<Object, Boolean>()).getClass()));
    }

    @Test
    public void testNullValueHandlingOnRaceCondition_ValueSet() {
        Set<Integer> results = new HashSet<Integer>();
        //noinspection NullableProblems
        ConcurrentSuffixTree.addIfNotNull(null, results);
        assertTrue(results.isEmpty());
        ConcurrentSuffixTree.addIfNotNull(1, results);
        assertTrue(results.contains(1));
    }

    @Test
    public void testNullValueHandlingOnRaceCondition_KeyValuePairSet() {
        Set<KeyValuePair<Integer>> results = new HashSet<KeyValuePair<Integer>>();
        //noinspection NullableProblems
        ConcurrentSuffixTree.addIfNotNull("FOO", null, results);
        assertTrue(results.isEmpty());
        ConcurrentSuffixTree.addIfNotNull("FOO", 1, results);
        assertTrue(results.contains(new ConcurrentRadixTree.KeyValuePairImpl("FOO", 1)));
    }

    /**
     * Creates a new {@link ConcurrentSuffixTree} but overrides
     * {@link com.googlecode.concurrenttrees.suffix.ConcurrentSuffixTree#createSetForOriginalKeys()} to return a set
     * which provides consistent iteration order (useful for unit tests).
     */
    @SuppressWarnings({"JavaDoc"})
    <O> ConcurrentSuffixTree<O> newConcurrentSuffixTreeForUnitTests() {
        return new ConcurrentSuffixTree<O>(nodeFactory) {
            // Override this method to return a set which has consistent iteration order, for unit testing...
            @Override
            protected Set<String> createSetForOriginalKeys() {
                return new LinkedHashSet<String>();
            }
        };
    }
}
