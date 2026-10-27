import java.util.concurrent.atomic.AtomicInteger;

class UnsafeCounter {
    private int count = 0;

    public void increment() {
        count++; // Not thread-safe (Read-Modify-Write is not atomic)
    }

    public int getCount() {
        return count;
    }
}

class SafeCounter {
    private int count = 0;

    public synchronized void increment() {
        count++; // Thread-safe using intrinsic lock (monitor)
    }

    public int getCount() {
        return count;
    }
}

class AtomicCounter {
    private final AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet(); // Thread-safe using lock-free CAS (Compare-And-Swap)
    }

    public int getCount() {
        return count.get();
    }
}

public class Practise {
    public static void main(String[] args) throws InterruptedException {

        int numberOfThreads = 5;
        int incrementsPerThread = 100_000;

        UnsafeCounter unsafeCounter = new UnsafeCounter();
        SafeCounter safeCounter = new SafeCounter();
        AtomicCounter atomicCounter = new AtomicCounter();

        Thread[] threads = new Thread[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    unsafeCounter.increment();
                    safeCounter.increment();
                    atomicCounter.increment();
                }
            });

            threads[i].start();
        }

        // Wait for every thread to finish
        for (Thread thread : threads) {
            thread.join();
        }

        int expected = numberOfThreads * incrementsPerThread;

        System.out.println("Expected count:          " + expected);
        System.out.println("Unsafe count:            " + unsafeCounter.getCount());
        System.out.println("Safe count (sync):       " + safeCounter.getCount());
        System.out.println("Atomic count (atomic):    " + atomicCounter.getCount());
    }
}
