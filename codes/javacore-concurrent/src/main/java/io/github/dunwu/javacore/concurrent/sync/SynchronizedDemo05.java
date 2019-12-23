package io.github.dunwu.javacore.concurrent.sync;

import io.github.dunwu.javacore.concurrent.annotation.ThreadSafe;

/**
 * 线程安全的计数器示例 - 使用 synchronized 修饰代码块
 * <p>
 * 示例说明：
 * <p>
 * 定义一个计数器，循环执行自增操作 100000 次。
 * <p>
 * 启动两个线程并行执行，期望最终值为 200000，实际值也为 200000。
 *
 * @author Zhang Peng
 * @since 2018/8/1
 */
@ThreadSafe
public class SynchronizedDemo05 implements Runnable {

    private static final int MAX = 100000;

    private static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        SynchronizedDemo05 instance = new SynchronizedDemo05();
        Thread t1 = new Thread(instance);
        Thread t2 = new Thread(instance);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(count);
    }

    @Override
    public void run() {
        for (int i = 0; i < MAX; i++) {
            increase();
        }
    }

    /**
     * synchronized 修饰代码块
     */
    public void increase() {
        synchronized (this) {
            count++;
        }
    }

}
