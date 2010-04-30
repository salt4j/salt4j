package salt4j.cache.acid;

import java.util.ArrayList;
import java.util.HashMap;
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
    final ReentrantReadWriteLock rw = new ReentrantReadWriteLock(true);
    int writeLockAcquisitions = 0;

    private final HashSet<Long> owners = new HashSet<Long>();
    
    void tryLock(final Lock l, boolean isWriteLock) throws LockException {
        boolean waitForSet = false;
        final long giveUpTime = System.currentTimeMillis() + 250;
        for(int i = 0; System.currentTimeMillis() < giveUpTime ; i++) {
            boolean success = false;
            for (int j=0; j<1; j++) if ((success = l.tryLock()) == true) break;

            if (success) {
                if (isWriteLock) writeLockAcquisitions ++; // need not be an atomicinteger (wlock guarantee)
                ThreadDB.registerLock(this);
                synchronized(DEADLOCK) {
                    synchronized(owners) { owners.add(Thread.currentThread().getId()); }
                    ThreadDB.clearMyWaitFor();
                }                
                return;
            } else {
                boolean b;
                synchronized(DEADLOCK) {
                    if (!waitForSet) { ThreadDB.setMyWaitFor(this); DEADLOCK.notify(); }
                    b = deadLocked(new HashSet<Long>(), Thread.currentThread().getId());
                    if (b) { System.out.print("."); System.out.flush(); break; } //deadlock!
                    else try { DEADLOCK.wait(); } catch (Exception e){} //no deadlock
                }
            }
        }
        ThreadDB.clearMyWaitFor(); throw new LockException();
    }

    static final Object DEADLOCK = new Object();
    /** Failed to acquire this lock.  */
    boolean deadLocked(HashSet<Long> alreadyChecked, Long currentThread) {
        synchronized(DEADLOCK) {
            HashSet<Long> threadsImWaitingFor = new HashSet<Long>();
            synchronized(owners) {
                for(Long idThread: owners) {
                    if (idThread == currentThread) return true;
                    else if (!alreadyChecked.contains(idThread)) {
                        threadsImWaitingFor.add(idThread);
                        alreadyChecked.add(idThread);
                    }
                }
            }

            HashSet<TmLock> locksTheyWant = new HashSet<TmLock>();
            for(Long idThread:threadsImWaitingFor) {
                TmLock tm;
                synchronized(ThreadDB.WAITFOR) {tm = ThreadDB.WAITFOR.get(idThread);}
                if (tm != null) locksTheyWant.add(tm);
            }

            for (TmLock tm : locksTheyWant) {
                if (tm.deadLocked(alreadyChecked, currentThread)) return true;
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
        synchronized(DEADLOCK) { synchronized(owners) { owners.clear(); } DEADLOCK.notify(); }
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

    public static class LockException extends RuntimeException{
        public LockException rollback() { return this; }
    }

    private static ThreadLocal<ArrayList<Runnable>> UNDOLOG = new ThreadLocal<ArrayList<Runnable>>(){
        protected ArrayList<Runnable> initialValue() { return new ArrayList<Runnable>(); }
    };

    private static ThreadLocal<Set<TmLock>> LOCKS = new ThreadLocal<Set<TmLock>>(){
        protected Set<TmLock> initialValue() { return new HashSet<TmLock>(); }
    };

    private static class ThreadDB {
        final static HashMap<Long, HashSet<TmLock>> threadIdToLocks = new HashMap<Long, HashSet<TmLock>>();
        
        static void registerLock(TmLock lock) {
            Long threadId = Thread.currentThread().getId();
            HashSet<TmLock> locks;
            synchronized(threadIdToLocks) {
                locks = threadIdToLocks.get(threadId);
                if (locks == null) {
                    locks = new HashSet<TmLock>();
                    threadIdToLocks.put(threadId, locks);
                }
            }
            synchronized(locks) { locks.add(lock); }
        }

        static HashSet<TmLock> getMyLocks() {
            Long threadId = Thread.currentThread().getId();
            synchronized(threadIdToLocks) {
                return threadIdToLocks.get(threadId);
            }            
        }
        
        static void clearMyLocks() {
            HashSet<TmLock> locks = getMyLocks();
            if (locks != null) {
                synchronized(locks) {
                    for (TmLock lock: locks) lock.unlockFully();
                    locks.clear();
                }
            }
        }

        final static HashMap<Long, TmLock> WAITFOR = new HashMap<Long, TmLock>();

        static void setMyWaitFor(TmLock lock) {
            Long threadId = Thread.currentThread().getId();
            synchronized(DEADLOCK) {
                synchronized (WAITFOR) { WAITFOR.put(threadId, lock); }
                DEADLOCK.notify();
            }
        }

        static void clearMyWaitFor() {
            Long threadId = Thread.currentThread().getId();
            synchronized(DEADLOCK) {
                synchronized (WAITFOR) { WAITFOR.remove(threadId); }
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
    public static void rollback() { rollbackWithUndoLog(); ThreadDB.clearMyLocks(); }

    public static void commit() { ThreadDB.clearMyLocks(); }

    public static void addToUndoLog(Runnable runnable) { UNDOLOG.get().add(runnable); }
}