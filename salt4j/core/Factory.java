package salt4j.core;

/** Use Factory<E> when you need to create a generic object.  New E() doesn't compile. */
public abstract class Factory<E> {
    abstract public E create();
}