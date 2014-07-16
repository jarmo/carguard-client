package me.guard.car;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LimitedQueueTest {
  @Test
  public void add() {
    LimitedQueue<Integer> queue = new LimitedQueue<Integer>(3);
    queue.add(1);
    assertTrue(queue.size() == 1);
    assertTrue(queue.get(0) == 1);

    queue.add(2);
    assertTrue(queue.size() == 2);
    assertTrue(queue.get(0) == 1);
    assertTrue(queue.get(1) == 2);

    queue.add(3);
    assertTrue(queue.size() == 3);
    assertTrue(queue.get(0) == 1);
    assertTrue(queue.get(1) == 2);
    assertTrue(queue.get(2) == 3);

    queue.add(4);
    assertTrue(queue.size() == 3);
    assertTrue(queue.get(0) == 2);
    assertTrue(queue.get(1) == 3);
    assertTrue(queue.get(2) == 4);
  }
}