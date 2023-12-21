package projekt.model;

public record Position(int row, int column) {
    public String toString() {
        return String.format("(%d, %d)", row, column);
    }
}
