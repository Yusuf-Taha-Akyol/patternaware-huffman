package com.pwha.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.PriorityQueue;

public class CustomPriorityQueue<T extends Comparable<? super T>> implements Serializable{
        private T[] queue;
        private int size = 0;
        private static final int DEFAULT_CAPACITY = 10;

        @SuppressWarnings("unchecked")
        public CustomPriorityQueue() {
            queue = (T[]) new Comparable[DEFAULT_CAPACITY];
        }

        public void add(T element) {
            if(element == null) {
                throw new NullPointerException();
            }

            if(size >= queue.length) {
                grow();
            }

            siftUp(size, element);
            size++;
        }

        public void addAll(CustomPriorityQueue<? extends T> other) {
            if(other == null) return;

            for(int i = 0; i < other.size; i++) {
                this.add(other.queue[i]);
            }
        }

        public T poll() {
            if(size == 0) return null;

            int s = --size;
            T result = queue[0];
            T x = queue[s];
            queue[s] = null;

            if(s != 0){
                siftDown(0, x);
            }

            return result;
        }

        public T peek() { return (size == 0) ? null : queue[0]; }

        public int size() { return size; }

        public boolean isEmpty() { return size == 0; }

        private void grow() {
            int oldCapacity = queue.length;

            int newCapacity = oldCapacity + (oldCapacity < 64 ? oldCapacity + 2 : oldCapacity >> 1);
            queue = Arrays.copyOf(queue, newCapacity);
        }

        private void siftUp(int k, T x) {
            while(k > 0) {
                int parent = (k - 1) >>> 1;
                T e = queue[parent];

                if(x.compareTo(e) >= 0) {
                    break;
                }

                queue[k] = e;
                k = parent;
            }
            queue[k] = x;
        }

        private void siftDown(int k, T x) {
            int half = size >>> 1;
            while(k < half) {
                int child = (k << 1) + 1;
                T c = queue[child];
                int right = child + 1;

                if(right < size && c.compareTo(queue[right]) > 0) c = queue[child = right];

                if(x.compareTo(c) <= 0) break;

                queue[k] = c;
                k = child;
            }

            queue[k] = x;
        }


}
