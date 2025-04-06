package service;

import java.util.ArrayList;
import java.util.List;

class CustomHeap<T> {
    private static class Node<T> {
        T data;
        double priority;

        Node(T data, double priority) {
            this.data = data;
            this.priority = priority;
        }
    }

    private final List<CustomHeap.Node<T>> heap;

    public CustomHeap() {
        this.heap = new ArrayList<>();
    }

    public void clear() {
        heap.clear();
    }

    private void swap(int i, int j) {
        CustomHeap.Node<T> temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            if (heap.get(index).priority > heap.get(parentIndex).priority) {
                swap(index, parentIndex);
                index = parentIndex;
            } else {
                break;
            }
        }
    }

    private void heapifyDown(int index) {
        int size = heap.size();
        while (index < size) {
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;
            int largest = index;

            if (leftChild < size && heap.get(leftChild).priority > heap.get(largest).priority) {
                largest = leftChild;
            }
            if (rightChild < size && heap.get(rightChild).priority > heap.get(largest).priority) {
                largest = rightChild;
            }

            if (largest != index) {
                swap(index, largest);
                index = largest;
            } else {
                break;
            }
        }
    }

    public void add(T data, double priority) {
        heap.add(new CustomHeap.Node<>(data, priority));
        heapifyUp(heap.size() - 1);
    }

    public T peek() {
        return heap.isEmpty() ? null : heap.get(0).data;
    }

    public T poll() {
        if (heap.isEmpty()) {
            return null;
        }
        T result = heap.get(0).data;
        CustomHeap.Node<T> lastNode = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, lastNode);
            heapifyDown(0);
        }
        return result;
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public int size() {
        return heap.size();
    }
}
