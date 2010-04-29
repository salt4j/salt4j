package salt4j.cache.acid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *  Manages locking and undo logging. Makes transactional data structures easy to implement.
 *  We may need this to implement Transactional caching.  Extracted from STMLib.
 */
public class TmLock {

    /** The MAIN class start here */
    final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    int writeLockAcquisitions = 0;

    void tryLock(final Lock l, boolean isWriteLock) throws LockException {
        boolean success = l.tryLock(); // don't wait for locks. extremely optimistic but fast.
        if (success) {
            if (isWriteLock) writeLockAcquisitions ++; // need not be an atomicinteger (wlock guarantee)
            LOCKS.get().add(this);
        } else throw new LockException();
    }

    public final void unlockFully() {
        final int nWriteLocks = rw.getWriteHoldCount();
        if (nWriteLocks > 0) if (nWriteLocks == 1) rw.writeLock().unlock(); else throw new Error();
        final int nReadLocks = rw.getReadHoldCount();
        if (nReadLocks > 0) if (nReadLocks == 1) rw.readLock().unlock(); else throw new Error();
    }

    private final void upgradeLock() {
        final int initialWrites = writeLockAcquisitions;
        unlockFully(); //drop read lock.
        tryLock(rw.writeLock(), true); //try to acquire write lock immediately.
        if (writeLockAcquisitions != initialWrites + 1) throw new LockException().rollback();
    }

    /** Acquire a write lock. May upgrade a read lock. */
    public final void write(){
        if(rw.isWriteLockedByCurrentThread()) return; //only do this once.
        else if (rw.getReadHoldCount() > 0) upgradeLock(); //read to write lock
        else tryLock(rw.writeLock(), true); // acquire the lock.
    }

    /** Acquire a read lock. */
    public final void read() {
        if (rw.isWriteLockedByCurrentThread()) return;  //write lock supercedes read lock
        else if(rw.getReadHoldCount()>0) return; //don't acquire a read lock more than once.
        else tryLock(rw.readLock(), false); //ok, get the lock.
    }

    public static class LockException extends RuntimeException{
        public LockException rollback() { rollback(); return this; }
    }

    private static ThreadLocal<ArrayList<Runnable>> UNDOLOG = new ThreadLocal<ArrayList<Runnable>>(){
        protected ArrayList<Runnable> initialValue() { return new ArrayList<Runnable>(); }
    };

    private static ThreadLocal<Set<TmLock>> LOCKS = new ThreadLocal<Set<TmLock>>(){
        protected Set<TmLock> initialValue() { return new HashSet<TmLock>(); }
    };

    //clear all locks:
    private static void clearAllLocks() {
        Set<TmLock> y = LOCKS.get();
        for (TmLock lock: y) lock.unlockFully();
        y.clear();
    }

    //reverse history with undo log:
    private static void rollbackWithUndoLog() {
        ArrayList<Runnable> x = UNDOLOG.get();
        for (int i=x.size()-1; i>=0; i--) x.get(i).run();
        x.clear();
    }
    public static void rollback() { rollbackWithUndoLog(); clearAllLocks(); }

    public static void commit() { clearAllLocks(); }

    public static void addToUndoLog(Runnable runnable) { UNDOLOG.get().add(runnable); }
}