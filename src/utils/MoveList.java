package src.utils;

import java.util.Arrays;

public class MoveList {
    private int[] data;
    private int size;

    public MoveList(int initial_capacity) {
        data = new int[initial_capacity];
        size = 0;
    }

    private void grow(int increase) {
        data = Arrays.copyOf(data, size + increase);
    }

    private void grow() {
        int capacity = data.length + (data.length >> 1);
        data = Arrays.copyOf(data, capacity);
    }

    public void add(int move) {
        if (size >= data.length) grow();
        data[size++] = move;
    }

    public int get(int index) {
        return data[index];
    }

    public int size() {
        return size;
    }

    public void addAll(MoveList move_list) {
        if (size + move_list.size >= data.length) {
            grow(move_list.size);
        }

        System.arraycopy(move_list.data, 0, data, size, move_list.size);
        size += move_list.size;
    }
}   