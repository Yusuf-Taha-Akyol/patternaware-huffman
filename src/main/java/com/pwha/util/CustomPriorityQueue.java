package com.pwha.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Custom Min-Priority Queue Implementation (Built from Scratch).
 * <p>
 * This class replaces {@code java.util.PriorityQueue} to demonstrate a deep understanding of
 * core data structures. It implements a Binary Min-Heap to efficiently manage elements
 * based on their natural ordering.
 * <p>
 * Key Features:
 * 1. **Generic Implementation:** Can store any Comparable type.
 * 2. **Dynamic Resizing:** Automatically grows the internal array when full.
 * 3. **Min-Heap Logic:** Ensures the smallest element is always at the root (index 0).
 */
public class CustomPriorityQueue<T extends Comparable<? super T>> implements Serializable{

    // Internal array to store heap elements.
    private T[] queue;

    // Current number of elements in the heap.
    private int size = 0;

    // Initial capacity of the array.
    private static final int DEFAULT_CAPACITY = 10;

    // Constructor to initialize the priority queue with default capacity.
    // Suppresses unchecked warnings because generic array creation is not directly supported in Java.
    @SuppressWarnings("unchecked")
    public CustomPriorityQueue() {
        queue = (T[]) new Comparable[DEFAULT_CAPACITY];
    }

    /**
     * Adds a new element to the priority queue.
     * Time Complexity: O(log N) due to the siftUp operation.
     *
     * @param element The element to add.
     */
    public void add(T element) {
        if(element == null) {
            throw new NullPointerException();
        }

        // Check if the array is full and needs to grow.
        if(size >= queue.length) {
            grow();
        }

        // Insert at the end and bubble up to correct position.
        siftUp(size, element);
        size++;
    }

    /**
     * Adds all elements from another CustomPriorityQueue to this one.
     * Useful for merging sub-queues in the Huffman algorithm.
     */
    public void addAll(CustomPriorityQueue<? extends T> other) {
        if(other == null) return;

        for(int i = 0; i < other.size; i++) {
            this.add(other.queue[i]);
        }
    }

    /**
     * Retrieves and removes the head (minimum element) of the queue.
     * Time Complexity: O(log N) due to the siftDown operation.
     *
     * @return The minimum element, or null if empty.
     */
    public T poll() {
        if(size == 0) return null;

        int s = --size;
        T result = queue[0]; // The root (minimum value)
        T x = queue[s];      // The last element in the heap
        queue[s] = null;     // Clear reference to help Garbage Collection

        // If the queue is not empty, move the last element to the root and sift it down.
        if(s != 0){
            siftDown(0, x);
        }

        return result;
    }

    /**
     * Retrieves, but does not remove, the head of the queue.
     * Time Complexity: O(1).
     */
    public T peek() { return (size == 0) ? null : queue[0]; }

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    /**
     * Increases the capacity of the internal array.
     * Strategy: Similar to ArrayList, grows by ~50% to ensure amortized constant time insertions.
     */
    private void grow() {
        int oldCapacity = queue.length;

        int newCapacity = oldCapacity + (oldCapacity < 64 ? oldCapacity + 2 : oldCapacity >> 1);
        queue = Arrays.copyOf(queue, newCapacity);
    }

    /**
     * Re-establishes the heap invariant by moving a node UP the tree.
     * Used after insertion.
     *
     * @param k The index to start sifting from.
     * @param x The element to insert.
     */
    private void siftUp(int k, T x) {
        while(k > 0) {
            int parent = (k - 1) >>> 1; // Unsigned right shift to find parent index
            T e = queue[parent];

            // If current element is greater than or equal to parent, heap property is satisfied.
            if(x.compareTo(e) >= 0) {
                break;
            }

            // Swap with parent
            queue[k] = e;
            k = parent;
        }
        queue[k] = x;
    }

    /**
     * Re-establishes the heap invariant by moving a node DOWN the tree.
     * Used after removal (poll).
     *
     * @param k The index to start sifting from (usually 0).
     * @param x The element to position.
     */
    private void siftDown(int k, T x) {
        int half = size >>> 1; // Only need to check non-leaf nodes
        while(k < half) {
            int child = (k << 1) + 1; // Left child index
            T c = queue[child];
            int right = child + 1;    // Right child index

            // Find the smaller of the two children
            if(right < size && c.compareTo(queue[right]) > 0) c = queue[child = right];

            // If x is smaller than or equal to the smallest child, we are done.
            if(x.compareTo(c) <= 0) break;

            // Swap with the smaller child
            queue[k] = c;
            k = child;
        }

        queue[k] = x;
    }


}
