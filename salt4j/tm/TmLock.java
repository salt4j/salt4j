package salt4j.tm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *  An upgradeable, deadlock-detecting, read-write lock.
 */
public class TmLock {
    /** The MAIN class start here */
    final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    int writeLockAcquisitions = 0;

    private final HashSet<Long> owners = new HashSet<Long>();
    
    void tryLock(final Lock l, boolean isWriteLock) throws LockException {
        boolean waitForSet = false;
        final long giveUpTime = System.currentTimeMillis() + 5000; //this magic number is ugly
        for(int i = 0; System.currentTimeMillis() < giveUpTime; i++) {
            if (l.tryLock()) {
                if (isWriteLock) writeLockAcquisitions ++; // need not be an atomicinteger (wlock guarantee)
                register();
                synchronized(DEADLOCK) {
                    owners.add(Thread.currentThread().getId());
                    WaitFor.clearMyWaitFor();
                }                
                return;
            } else {
                synchronized(DEADLOCK) {
                    if (!waitForSet) { WaitFor.setMyWaitFor(this); waitForSet = true; }
                    if (!deadLocked()) try { DEADLOCK.wait(); } catch (Exception e){} //no deadlock
                    else { WaitFor.clearMyWaitFor(); throw new LockException(); }
                }
            }
        }
        WaitFor.clearMyWaitFor(); throw new LockException();
    }

    private static final Object DEADLOCK = new Object();
    boolean deadLocked() { return deadLocked(new HashSet<Long>(), Thread.currentThread().getId()); }
    private boolean deadLocked(HashSet<Long> alreadyChecked, Long currentThread) {
        synchronized(DEADLOCK) { //stop the world
            for(Long idThread: owners) {
                if (idThread == currentThread) return true;
                else if (!alreadyChecked.contains(idThread)) {
                    alreadyChecked.add(idThread);
                    TmLock tm = WaitFor.WAITFOR.get(idThread);
                    if (tm != null && tm.deadLocked(alreadyChecked, currentThread)) return true;
                }
            }
            DEADLOCK.notify();
            return false;
        }
    }

    public final void unlockFully() {
        final int nWriteLocks = rw.getWriteHoldCount();
        if (nWriteLocks > 0) if (nWriteLocks == 1) rw.writeLock().unlock(); else throw new Error();
        final int nReadLocks = rw.getReadHoldCount();
        if (nReadLocks > 0) if (nReadLocks == 1) rw.readLock().unlock(); else throw new Error();
        synchronized(DEADLOCK) { owners.remove(Thread.currentThread().getId()); DEADLOCK.notify(); }
    }

    private final void upgradeLock() {
        final int initialWrites = writeLockAcquisitions;
        unlockFully(); //drop read lock.
        tryLock(rw.writeLock(), true); //try to acquire write lock immediately.
        if (writeLockAcquisitions != initialWrites + 1) throw new LockException();
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

    private void register() { LOCKS.get().add(this); }

    public static class LockException extends RuntimeException{
        public LockException rollback() { return this; }
    }

    private static ThreadLocal<ArrayList<Runnable>> UNDOLOG = new ThreadLocal<ArrayList<Runnable>>(){
        protected ArrayList<Runnable> initialValue() { return new ArrayList<Runnable>(); }
    };

    private static ThreadLocal<Set<TmLock>> LOCKS = new ThreadLocal<Set<TmLock>>(){
        protected Set<TmLock> initialValue() { return new HashSet<TmLock>(); }
    };

    private static class WaitFor {
        final static HashMap<Long, TmLock> WAITFOR = new HashMap<Long, TmLock>();

        static void setMyWaitFor(TmLock lock) {
            Long threadId = Thread.currentThread().getId();
            synchronized(DEADLOCK) {
                WAITFOR.put(threadId, lock);
                DEADLOCK.notify();
            }
        }

        static void clearMyWaitFor() {
            Long threadId = Thread.currentThread().getId();
            synchronized(DEADLOCK) {
                WAITFOR.remove(threadId);
                DEADLOCK.notify();
            }
        }
    }
 
    //reverse history with undo log:
    private static void rollbackWithUndoLog() {
        ArrayList<Runnable> x = UNDOLOG.get();
        for (int i=x.size()-1; i>=0; i--) x.get(i).run();
        x.clear();
    }

    private static void unlockAll() {
        for (TmLock lock: LOCKS.get()) lock.unlockFully();
        LOCKS.get().clear();
    }
    public static void rollback() { rollbackWithUndoLog(); unlockAll(); }

    public static void commit() { unlockAll(); }

    public static void addToUndoLog(Runnable runnable) { UNDOLOG.get().add(runnable); }
}