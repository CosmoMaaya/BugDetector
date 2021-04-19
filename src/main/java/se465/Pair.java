package se465;

public class Pair {

    public int left, right;

    Pair(int left, int right) {
        if (left <= right) {
            this.left = left;
            this.right = right;
        } else {
            this.left = right;
            this.right = left;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair pair = (Pair) o;
        return left == pair.left && right == pair.right;
    }

    @Override
    public int hashCode() {
        int result = left;
        result = 31 * result + right;
        return result;
    }
}
