package de.thm.mni.compilerbau.utils;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A linked list.
 * Simplifies construction of lists when following a linked-list like structure.
 * Useful to construct lists in semantic actions when parsing right-recursive list structures.
 *
 * @param <E> Type parameter of the list
 */
public class List<E> extends AbstractList<E> {
    private static final List<?> Nil = new List<>();

    /**
     * Equivalent to new List<E>()
     *
     * @param <E> Type parameter of the list
     * @return An empty list
     */
    @SuppressWarnings("unchecked")
    public static <E> List<E> nil() {
        return (List<E>) Nil;
    }

    /**
     * Constructs a List containing a single element
     *
     * @param element The element of the list
     * @param <E>     The type parameter of the list
     * @return A list containing the element.
     */
    public static <E> List<E> of(E element) {
        return new List<>(element, nil());
    }

    /**
     * Constructs a list from a head and a tail.
     * Equivalent to new List<E>(head, tail)
     *
     * @param head The head of the list
     * @param tail The tail of the list
     * @param <E>  The type of the list elements
     * @return A list containing the head element and all elements of tail
     */
    public static <E> List<E> cons(E head, List<E> tail) {
        return new List<>(head, tail);
    }


    private final List<E> tail;
    private final E head;

    private final boolean isEmpty;

    /**
     * Constructs an empty list
     */
    private List() {
        this.isEmpty = true;
        this.head = null;
        this.tail = null;
    }

    /**
     * Constructs a list from a head and a tail.
     *
     * @param head The head of the list
     * @param tail The tail of the list
     */
    public List(E head, List<E> tail) {
        this.isEmpty = false;
        this.head = head;
        this.tail = Objects.requireNonNull(tail);
    }

    @Override
    public int size() {
        return (isEmpty) ? 0 : 1 + tail.size();
    }

    @Override
    public boolean isEmpty() {
        return isEmpty;
    }

    @Override
    public E get(int i) {
        if (isEmpty) {
            throw new IndexOutOfBoundsException();
        }

        return (i == 0) ? head : tail.get(i - 1);
    }

    @Override
    public Iterator<E> iterator() {
        return new ImmutableListIterator<>(this);
    }

    private static class ImmutableListIterator<E> implements Iterator<E> {
        private List<E> current;

        ImmutableListIterator(List<E> current) {
            this.current = current;
        }

        @Override
        public boolean hasNext() {
            return !current.isEmpty;
        }

        @Override
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();

            E result = current.head;
            current = current.tail;
            return result;
        }
    }
}
