package latex_formatter.structure;

public class Tuple<I, J, K> {
    private final I i;
    private final J j;
    private final K k;

    public Tuple(I i, J j, K k) {
        this.i = i;
        this.j = j;
        this.k = k;
    }

    public I getFirst() {
        return i;
    }

    public J getSecond() {
        return j;
    }

    public K getThird() {
        return k;
    }

    public String toString() {
        return "[" + i.toString() + ", " + j.toString() + ", " + k.toString() + "]";
    }
}
