# Java Multithreading: Race Condition vs. Thread-Safe Counters

This project demonstrates one of the fundamental concepts in Java concurrent programming: **race conditions**, **lost updates**, and how to achieve thread safety using **`synchronized`** methods and **`AtomicInteger`**.

---

## 📌 Problem Overview

When multiple threads concurrently modify a shared mutable variable without proper synchronization, concurrent updates can be lost due to thread interleaving.

In this experiment:
- We spin up **5 concurrent threads**.
- Each thread performs **100,000 increments** on the same counter instances.
- **Expected Final Count**: $5 \times 100,000 = 500,000$.

---

## 🔍 Why Does the Unsafe Counter Fail?

The operation:
```java
count++;
```
appears as a single atomic operation in high-level Java code, but the JVM and CPU execute it in **three separate steps**:

1. **Read**: Fetch the current value of `count` from memory into a CPU register.
2. **Modify**: Add `1` to the register value.
3. **Write**: Write the new value back to main memory.

### Race Condition Illustration (Lost Update)

Suppose `count = 10` and two threads increment concurrently:

```text
Time    Thread A                           Thread B
 ───    ────────                           ────────
 T1     Reads count (10)                   
 T2                                        Reads count (10)
 T3     Calculates 10 + 1 = 11             
 T4                                        Calculates 10 + 1 = 11
 T5     Writes count = 11                  
 T6                                        Writes count = 11 (Overwrites Thread A!)
```

**Result**: Even though two increments were performed, `count` only advanced from `10` to `11`. The update from Thread A is permanently lost.

---

## 🛠️ Counter Implementations

### 1. `UnsafeCounter` (Vulnerable to Race Conditions)
```java
class UnsafeCounter {
    private int count = 0;

    public void increment() {
        count++; // Non-atomic read-modify-write
    }

    public int getCount() {
        return count;
    }
}
```

### 2. `SafeCounter` (Protected via `synchronized`)
```java
class SafeCounter {
    private int count = 0;

    public synchronized void increment() {
        count++; // Guarded by the object's intrinsic monitor lock
    }

    public int getCount() {
        return count;
    }
}
```
* **How it works**: Before entering `increment()`, the thread acquires the intrinsic monitor lock of the `SafeCounter` instance (`this`). Other threads attempting to execute any `synchronized` method on the same object are blocked until the lock is released.

### 3. `AtomicCounter` (Lock-Free via `AtomicInteger`)
```java
import java.util.concurrent.atomic.AtomicInteger;

class AtomicCounter {
    private final AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet(); // Atomic CAS (Compare-And-Swap)
    }

    public int getCount() {
        return count.get();
    }
}
```
* **How it works**: Uses CPU-level atomic instructions (**Compare-And-Swap / CAS**) instead of acquiring operating system locks. It provides thread safety with high throughput under moderate contention.

---

## ⚠️ Important Note: Why `volatile` Is Not Enough

Declaring `private volatile int count;` ensures memory **visibility** (threads immediately see writes to main memory instead of reading stale CPU cache lines). However, it does **not** make compound operations like `count++` atomic. Threads will still read identical values concurrently and overwrite each other's increments.

---

## 📊 Comparison Summary

| Feature | `UnsafeCounter` | `SafeCounter` (`synchronized`) | `AtomicCounter` (`AtomicInteger`) |
| :--- | :--- | :--- | :--- |
| **Increment Statement** | `count++` | `synchronized void increment()` | `count.incrementAndGet()` |
| **Synchronization** | None | Intrinsic monitor lock | Lock-free hardware CAS |
| **Race Conditions** | Yes (Lost updates occur) | Prevented | Prevented |
| **Guaranteed 500,000** | ❌ No (e.g. ~497,000) | ✔️ Yes (Consistent 500,000) | ✔️ Yes (Consistent 500,000) |
| **Overhead** | Minimal | Lock acquisition & context switching | Low (CPU CAS loop) |
| **Best Used For** | Single-threaded code | Complex multi-operation critical sections | High-performance counters & metrics |

---

## 🚀 How to Run

### Prerequisites
- Java Development Kit (JDK 8 or higher).

### Compilation & Execution
From the project directory:

```bash
# 1. Compile the Java file
javac Practise.java

# 2. Run the compiled application
java Practise
```

### Sample Output
```text
Expected count:          500000
Unsafe count:            497258
Safe count (sync):       500000
Atomic count (atomic):   500000
```

---

## 💬 Interview Summary

> *"A counter updated by multiple threads can suffer from a race condition because `count++` is a read-modify-write operation, not an atomic operation. Multiple threads can read the same value concurrently and overwrite one another's updates (lost updates). We can solve this using `synchronized`, which restricts execution to one thread at a time under an object's intrinsic monitor lock, or by using `AtomicInteger`, which executes atomic increments via lock-free Compare-And-Swap (CAS) instructions."*
