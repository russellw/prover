package olivine;

import static org.junit.Assert.*;

import org.junit.Test;

public class WeakTrieTest {
  @Test
  public void add() {
    var trie = new WeakTrie<String>();
    add(trie, "zero-zero-zero", 0, 0, 0);
    add(trie, "zero-zero-one", 0, 0, 1);
    add(trie, "zero-zero-two", 0, 0, 2);
    add(trie, "zero-one-zero", 0, 1, 0);
    add(trie, "zero-one-one", 0, 1, 1);
    add(trie, "zero-one-two", 0, 1, 2);
    var s = trie.findLessEqual(new int[] {0, 0, 0}, t -> true);
    assertEquals(s, "zero-zero-zero");
  }

  private void add(WeakTrie<String> trie, String value, int... key) {
    trie.add(key, value);
  }
}
