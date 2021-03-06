package AtomicVar;

import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentStack<E> {
    AtomicReference<Node<E>> top = new AtomicReference<Node<E>>();

    private static class Node<E> {
        public final E item;
        public Node<E> next;

        public Node(E item) {
            this.item = item;
        }
    }

    public void push(E item) {
        Node<E> newNode = new Node<>(item);
        Node<E> oldNode;
        do {
            oldNode = top.get();
            newNode.next = oldNode;
        }
        while (!top.compareAndSet(oldNode, newNode));

    }

    public E pop() {
        Node<E> oldNode;
        Node<E> newNode;
        do {
            oldNode = top.get();
            if (oldNode == null)
                return null;
            newNode = oldNode.next;
        } while (!top.compareAndSet(oldNode, newNode));
        return oldNode.item;
    }
}
