package src.utils;

public class MoveList {
    private long[] data;
    private int size;

    public MoveList() {
        data = new long[256];
        size = 0;
    }

    public void add(int move) {
        data[size++] = move;
    }

    public void scoreMove(int index, long score) {
        data[index] = (score << 32) | (data[index] & 0xFFFFFFFFL);
    }

    public int getMove(int index) {
        return (int) data[index];
    }

    public int getScore(int index) {
        return (int) (data[index] >> 32);
    }

    public int size() {
        return size;
    }

    public void clear() {
        size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void swap(int i, int j) {
        long temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }

    public void addAll(MoveList move_list) {
        System.arraycopy(move_list.data, 0, data, size, move_list.size);
        size += move_list.size;
    }
}   