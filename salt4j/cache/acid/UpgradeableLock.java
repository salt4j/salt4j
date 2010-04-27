package salt4j.cache.acid;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *  Manages locking and undo logging. Makes transactional data structures easy to implement.
 *  We may need this to implement Transactional caching.  Extracted from STMLib.
 */
public class UpgradeableLock {
    public static class LockFailure extends RuntimeException{}

    final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    int writeLockAcquisitions = 0;

    void tryLock(Lock l, boolean isWriteLock) throws LockFailure {
        boolean success = l.tryLock(); // don't wait for locks. extremely optimistic but fast.
        if (success) {
            if (isWriteLock) writeLockAcquisitions ++; // need not be an atomicinteger (wlock guarantee)
        } else throw new LockFailure();
    }

    public final void unlockAll() {
        final int nWriteLocks = rw.getWriteHoldCount();
        if (nWriteLocks > 0) if (nWriteLocks == 1) rw.writeLock().unlock(); else throw new Error();
        final int nReadLocks = rw.getReadHoldCount();
        if (nReadLocks > 0) if (nReadLocks == 1) rw.readLock().unlock(); else throw new Error();
    }

    private final void upgradeLock() {
        final int initialWrites = writeLockAcquisitions;
        unlockAll(); //drop read lock.
        tryLock(rw.writeLock(), true); //try to acquire write lock immediately.
        if (writeLockAcquisitions != initialWrites + 1) throw new LockFailure();
    }

    /**
     *  Acquire a write lock. May upgrade a read lock.
     */
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
}