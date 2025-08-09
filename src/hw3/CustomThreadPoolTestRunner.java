package hw3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CustomThreadPoolTestRunner {
    public static void main(String[] args) {
        System.out.println("\n" + "=== Test 1: poll size equals 3 ===" + "\n");

        runTest(3);

        System.out.println("\n" + "=== Test 2: poll size equals 5 ===" + "\n");

        runTest(5);
    }

    private static void runTest(int poolSize) {
        CustomThreadPool pool = new CustomThreadPool(poolSize);

        for (int i = 1; i <= 10; i++) {
            int taskId = i;

            pool.execute(() -> {
                System.out.println("Task " + taskId + " started by " + Thread.currentThread().getName());

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}

                System.out.println("Task " + taskId + " finished");
            });
        }

        pool.shutdown();
        pool.awaitTermination();

        System.out.println("All tasks completed.");
    }

    static class CustomThreadPool {
        private final List<CustomThread> customThreads = new ArrayList<>();
        private final LinkedList<Runnable> taskQueue = new LinkedList<>();
        private volatile boolean isRunning = true;

        public CustomThreadPool(int poolSize) {
            for (int i = 0; i < poolSize; i++) {
                CustomThread customThread = new CustomThread("Worker-" + i);
                customThreads.add(customThread);
                customThread.start();
            }
        }

        public void execute(Runnable task) {
            if (!isRunning) {
                awaitTermination();
                throw new IllegalStateException();
            }

            synchronized (taskQueue) {
                taskQueue.add(task);
                taskQueue.notify();
            }
        }

        public void shutdown() {
            isRunning = false;

            synchronized (taskQueue) {
                taskQueue.notifyAll();
            }
        }

        public void awaitTermination() {
            for (CustomThread customThread : customThreads) {
                try {
                    customThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private class CustomThread extends Thread {
            public CustomThread(String name) {
                super(name);
            }

            @Override
            public void run() {
                while (isRunning || !taskQueue.isEmpty()) {
                    Runnable task;

                    synchronized (taskQueue) {
                        while (taskQueue.isEmpty()) {
                            if (!isRunning) return;

                            try {
                                taskQueue.wait();
                            } catch (InterruptedException e) {
                                return;
                            }
                        }

                        task = taskQueue.removeFirst();
                    }

                    try {
                        task.run();
                    } catch (Exception e) {
                        System.out.println(getName() + " error: " + e.getMessage());
                    }
                }
            }
        }
    }
}
