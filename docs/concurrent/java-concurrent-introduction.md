# 并发简介

> **📦 本文以及示例源码已归档在 [javacore](https://github.com/dunwu/javacore/)**

## 一、并发基本概念

学习并发编程，免不了大量接触一些术语。其中，有些术语经常容易被混淆或分不清它们的差异，这里汇总一下。

### 并发和并行

- 例子
  - 你吃饭吃到一半，电话来了，你一直到吃完了以后才去接，这就说明你不支持并发也不支持并行。
  - 你吃饭吃到一半，电话来了，你停了下来接了电话，接完后继续吃饭，这说明你支持并发。
  - 你吃饭吃到一半，电话来了，你一边打电话一边吃饭，这说明你支持并行。
- 定义
  - **并发**的关键是你有处理多个任务的能力，不一定要同时。
  - **并行**的关键是你有同时处理多个任务的能力。
  - 最关键的点就是：是否是**同时**。

<p align="center">
  <img src="http://dunwu.test.upcdn.net/cs/java/javacore/concurrent/concurrent-vs-parallel.jpg">
</p>

### 同步和异步

- 例子
  - 同步就像是打电话，不挂电话，通话不会结束。
  - 异步就像是发短信，发完短信后，就可以做其他事，短信来了，手机会提醒。
- 定义
  - 所谓同步，就是在发出一个调用时，在没有得到结果之前，该调用就不返回。但是一旦调用返回，就得到返回值了。
  - 异步则是相反，调用在发出之后，这个调用就直接返回了，所以没有返回结果。换句话说，当一个异步过程调用发出后，调用者不会立刻得到结果。而是在调用发出后，被调用者通过状态、通知来通知调用者，或通过回调函数处理这个调用。

<p align="center">
  <img src="http://dunwu.test.upcdn.net/cs/java/javacore/concurrent/synchronous-vs-asynchronous.gif">
</p>

### 阻塞和非阻塞

- 例子
  - 阻塞调用就像是打电话，通话不结束，不能放下。
  - 非阻塞调用就像是发短信，发完短信后，就可以做其他事，短信来了，手机会提醒。
- 定义
  - 阻塞和非阻塞关注的是程序在等待调用结果（消息，返回值）时的状态.
  - 阻塞调用是指调用结果返回之前，当前线程会被挂起。调用线程只有在得到结果之后才会返回。
  - 非阻塞调用指在不能立刻得到结果之前，该调用不会阻塞当前线程。

### 进程和线程

- 什么是进程
  - 简言之，进程可视为一个正在运行的程序。
  - 进程是具有一定独立功能的程序关于某个数据集合上的一次运行活动。进程是操作系统进行资源分配的基本单位。
- 什么是线程
  - 线程是操作系统进行调度的基本单位。
- 进程 vs. 线程
  - 一个程序至少有一个进程，一个进程至少有一个线程。
  - 线程比进程划分更细，所以执行开销更小，并发性更高
  - 进程是一个实体，拥有独立的资源；而同一个进程中的多个线程共享进程的资源。

<p align="center">
  <img src="http://dunwu.test.upcdn.net/cs/java/javacore/concurrent/processes-vs-threads.jpg">
</p>

JVM 在单个进程中运行，JVM 中的线程共享属于该进程的堆。这就是为什么几个线程可以访问同一个对象。线程共享堆并拥有自己的堆栈空间。这是一个线程如何调用一个方法以及它的局部变量是如何保持线程安全的。但是堆不是线程安全的并且为了线程安全必须进行同步。

## 二、多线程的优点

- 更好的资源利用
- 更简单的编程模型
- 程序响应更灵敏

### 更好的资源利用

想象一下，一个应用程序需要从本地文件系统中读取和处理文件的情景。比方说，从磁盘读取一个文件需要 5 秒，处理一个文件需要 2 秒。处理两个文件则需要：

```
5秒读取文件A
2秒处理文件A
5秒读取文件B
2秒处理文件B
---------------------
总共需要14秒
```

从磁盘中读取文件的时候，大部分的 CPU 时间用于等待磁盘去读取数据。在这段时间里，CPU 非常的空闲。它可以做一些别的事情。通过改变操作的顺序，就能够更好的使用 CPU 资源。看下面的顺序：

```
5秒读取文件A
5秒读取文件B + 2秒处理文件A
2秒处理文件B
---------------------
总共需要12秒
```

CPU 等待第一个文件被读取完。然后开始读取第二个文件。当第二文件在被读取的时候，CPU 会去处理第一个文件。记住，在等待磁盘读取文件的时候，CPU 大 部分时间是空闲的。

总的说来，CPU 能够在等待 IO 的时候做一些其他的事情。这个不一定就是磁盘 IO。它也可以是网络的 IO，或者用户输入。通常情况下，网络和磁盘的 IO 比 CPU 和内存的 IO 慢的多。

### 程序设计更简单

在单线程应用程序中，如果你想编写程序手动处理上面所提到的读取和处理的顺序，你必须记录每个文件读取和处理的状态。相反，你可以启动两个线程，每个线程处理一个文件的读取和操作。线程会在等待磁盘读取文件的过程中被阻塞。在等待的时候，其他的线程能够使用 CPU 去处理已经读取完的文件。其结果就是，磁盘总是在繁忙地读取不同的文件到内存中。这会带来磁盘和 CPU 利用率的提升。而且每个线程只需要记录一个文件，因此这种方式也很容易编程实现。

### 程序响应更快

将一个单线程应用程序变成多线程应用程序的另一个常见的目的是实现一个响应更快的应用程序。设想一个服务器应用，它在某一个端口监听进来的请求。当一个请求到来时，它去处理这个请求，然后再返回去监听。

服务器的流程如下所述：

```java
while(server is active) {
    listen for request
    process request
}
```

如果一个请求需要占用大量的时间来处理，在这段时间内新的客户端就无法发送请求给服务端。只有服务器在监听的时候，请求才能被接收。另一种设计是，监听线程把请求传递给工作者线程(worker thread)，然后立刻返回去监听。而工作者线程则能够处理这个请求并发送一个回复给客户端。这种设计如下所述：

```java
while(server is active) {
    listen for request
    hand request to worker thread
}
```

这种方式，服务端线程迅速地返回去监听。因此，更多的客户端能够发送请求给服务端。这个服务也变得响应更快。

桌面应用也是同样如此。如果你点击一个按钮开始运行一个耗时的任务，这个线程既要执行任务又要更新窗口和按钮，那么在任务执行的过程中，这个应用程序看起来好像没有反应一样。相反，任务可以传递给工作者线程（word thread)。当工作者线程在繁忙地处理任务的时候，窗口线程可以自由地响应其他用户的请求。当工作者线程完成任务的时候，它发送信号给窗口线程。窗口线程便可以更新应用程序窗口，并显示任务的结果。对用户而言，这种具有工作者线程设计的程序显得响应速度更快。

## 三、多线程的风险

凡事有利有弊，引入多线程除了带来的好处以外，也产生了一些问题：

- 安全性问题
- 活跃性问题
- 性能问题

### 安全性问题

#### 什么是线程安全

当多个线程访问一个对象时，如果不用考虑这些线程在运行时环境下的调度和交替执行，也不需要进行额外的同步，或者在调用方进行任何其他的协调操作，调用这个对象的行为都可以获得正确的结果，那这个对象是线程安全的。

在同一程序中运行多个线程本身不会导致问题，问题在于多个线程访问了相同的资源。如：同一内存区（变量，数组，或对象）、系统（数据库，web services 等）或文件。

按照线程的"安全程度"由强至弱来排序，可以将各种操作共享的数据分为五类：不可变、绝对线程安全、相对线程安全、线程兼容和线程对立。

- **不可变** - **不可变（Immutable）的对象一定是线程安全的**，无论是对象的方法实现还是方法的调用者，都不再需要采取任何的线程安全保障措施。
- **绝对线程安全** - 一个类要达到 不管运行时环境如何，调用者都不需要任何额外的同步措施，通常需要付出很大的，甚至是不切实际的代价。
- **相对线程安全** - 相对线程安全是通常意义上所讲的线程安全，它需要保证对这个对象单独的操作是线程安全的，我们在调用时不需要做额外的保障措施。
- **线程兼容** - 线程兼容是指对象本身不是线程安全的，但是可以通过在调用端正确地使用同步手段来保证对象在并发环境中可以安全地使用。
- **线程对立** - 线程对立是指无论调用端是否采取了同步措施，都无法在多线程环境中并发使用的代码。

#### 线程不安全的示例

```java
public class Counter {
    protected long count = 0;
    public void add(long value){
        this.count = this.count + value;
    }
}
```

想象下线程 A 和 B 同时执行同一个 Counter 对象的 add()方法，我们无法知道操作系统何时会在两个线程之间切换。JVM 并不是将这段代码视为单条指令来执行的，而是按照下面的顺序：

```
从内存获取 this.count 的值放到寄存器
将寄存器中的值增加 value
将寄存器中的值写回内存
```

观察线程 A 和 B 交错执行会发生什么：

```
this.count = 0;
A: 读取 this.count 到一个寄存器 (0)
B: 读取 this.count 到一个寄存器 (0)
B: 将寄存器的值加 2
B: 回写寄存器值(2)到内存. this.count 现在等于 2
A: 将寄存器的值加 3
A: 回写寄存器值(3)到内存. this.count 现在等于 3
```

两个线程分别加了 2 和 3 到 count 变量上，两个线程执行结束后 count 变量的值应该等于 5。然而由于两个线程是交叉执行的，两个线程从内存中读出的初始值都是 0。然后各自加了 2 和 3，并分别写回内存。最终的值并不是期望的 5，而是最后写回内存的那个线程的值，上面例子中最后写回内存的是线程 A，但实际中也可能是线程 B。如果没有采用合适的同步机制，线程间的交叉执行情况就无法预料。

#### 竞态条件和临界区

> **竞态条件（Race Condition）**：当两个线程竞争同一资源时，如果对资源的访问顺序敏感，就称存在竞态条件。
>
> **临界区（Critical Sections）**：导致竞态条件发生的代码区称作临界区。

上例中 add()方法就是一个临界区,它会产生竞态条件。在临界区中使用适当的同步就可以避免竞态条件。

#### 线程安全的实现

##### 互斥同步

互斥同步是最常见的并发正确性保障手段。

**同步是指在多线程并发访问共享数据时，保证共享数据在同一时刻只能被一个线程访问**。

互斥是实现同步的一种手段。临界区（Critical Sections）、互斥量（Mutex）和信号量（Semaphore）都是主要的互斥实现方式。

最典型的案例是使用 `synchronized` 或 `ReentrantLock` 。

**互斥同步最主要的问题是线程阻塞和唤醒所带来的性能问题**，互斥同步属于一种悲观的并发策略，总是认为只要不去做正确的同步措施，那就肯定会出现问题。无论共享数据是否真的会出现竞争，它都要进行加锁（这里讨论的是概念模型，实际上虚拟机会优化掉很大一部分不必要的加锁）、用户态核心态转换、维护锁计数器和检查是否有被阻塞的线程需要唤醒等操作。

##### 非阻塞同步

随着硬件指令集的发展，我们可以使用基于冲突检测的乐观并发策略：先进行操作，如果没有其它线程争用共享数据，那操作就成功了，否则采取补偿措施（不断地重试，直到成功为止）。这种乐观的并发策略的许多实现都不需要将线程阻塞，因此这种同步操作称为非阻塞同步。

为什么说乐观锁需要 **硬件指令集的发展** 才能进行？因为需要操作和冲突检测这两个步骤具备原子性。而这点是由硬件来完成，如果再使用互斥同步来保证就失去意义了。

这类乐观锁指令常见的有：

- 测试并设置（Test-amd-Set）
- 获取并增加（Fetch-and-Increment）
- 交换（Swap）
- 比较并交换（CAS）
- 加载链接、条件存储（Load-linked / Store-Conditional）

Java 典型应用场景：J.U.C 包中的原子类（基于 `Unsafe` 类的 CAS 操作）

##### 无同步方案

要保证线程安全，不一定非要进行同步。同步只是保证共享数据争用时的正确性，如果一个方法本来就不涉及共享数据，那么自然无须同步。

Java 中的 **无同步方案** 有：

- **可重入代码** - 也叫纯代码。如果一个方法，它的 **返回结果是可以预测的**，即只要输入了相同的数据，就能返回相同的结果，那它就满足可重入性，当然也是线程安全的。
- **线程本地存储** - 使用 **`ThreadLocal` 为共享变量在每个线程中都创建了一个本地副本**，这个副本只能被当前线程访问，其他线程无法访问，那么自然是线程安全的。

### 活跃性问题

#### 死锁（Deadlock）

##### 什么是死锁

多个线程互相等待对方释放锁。

死锁是当线程进入无限期等待状态时发生的情况，因为所请求的锁被另一个线程持有，而另一个线程又等待第一个线程持有的另一个锁。

<p align="center">
  <img src="http://dunwu.test.upcdn.net/cs/java/javacore/concurrent/deadlock.png">
</p>

##### 避免死锁的方法

（1）加锁顺序

当多个线程需要相同的一些锁，但是按照不同的顺序加锁，死锁就很容易发生。

如果能确保所有的线程都是按照相同的顺序获得锁，那么死锁就不会发生。

按照顺序加锁是一种有效的死锁预防机制。但是，这种方式需要你事先知道所有可能会用到的锁(译者注：并对这些锁做适当的排序)，但总有些时候是无法预知的。

（2）加锁时限

另外一个可以避免死锁的方法是在尝试获取锁的时候加一个超时时间，这也就意味着在尝试获取锁的过程中若超过了这个时限该线程则放弃对该锁请求。若一个线程没有在给定的时限内成功获得所有需要的锁，则会进行回退并释放所有已经获得的锁，然后等待一段随机的时间再重试。这段随机的等待时间让其它线程有机会尝试获取相同的这些锁，并且让该应用在没有获得锁的时候可以继续运行(译者注：加锁超时后可以先继续运行干点其它事情，再回头来重复之前加锁的逻辑)。

（3）死锁检测

死锁检测是一个更好的死锁预防机制，它主要是针对那些不可能实现按序加锁并且锁超时也不可行的场景。

每当一个线程获得了锁，会在线程和锁相关的数据结构中（map、graph 等等）将其记下。除此之外，每当有线程请求锁，也需要记录在这个数据结构中。

当一个线程请求锁失败时，这个线程可以遍历锁的关系图看看是否有死锁发生。

如果检测出死锁，有两种处理手段：

- 释放所有锁，回退，并且等待一段随机的时间后重试。这个和简单的加锁超时类似，不一样的是只有死锁已经发生了才回退，而不会是因为加锁的请求超时了。虽然有回退和等待，但是如果有大量的线程竞争同一批锁，它们还是会重复地死锁（编者注：原因同超时类似，不能从根本上减轻竞争）。
- 一个更好的方案是给这些线程设置优先级，让一个（或几个）线程回退，剩下的线程就像没发生死锁一样继续保持着它们需要的锁。如果赋予这些线程的优先级是固定不变的，同一批线程总是会拥有更高的优先级。为避免这个问题，可以在死锁发生的时候设置随机的优先级。

#### 活锁（Livelock）

##### 什么是活锁？

活锁是一个递归的情况，两个或更多的线程会不断重复一个特定的代码逻辑。预期的逻辑通常为其他线程提供机会继续支持'this'线程。

想象这样一个例子：两个人在狭窄的走廊里相遇，二者都很礼貌，试图移到旁边让对方先通过。但是他们最终在没有取得任何进展的情况下左右摇摆，因为他们都在同一时间向相同的方向移动。

<p align="center">
  <img src="http://dunwu.test.upcdn.net/cs/java/javacore/concurrent/livelock.png">
</p>

如图所示：两个线程想要通过一个 Worker 对象访问共享公共资源的情况，但是当他们看到另一个 Worker（在另一个线程上调用）也是“活动的”时，它们会尝试将该资源交给其他工作者并等待为它完成。如果最初我们让两名工作人员都活跃起来，他们将会面临活锁问题。

##### 避免活锁

没有避免活锁的通用指南，但是在我们改变其他线程也使用的公共对象状态的场景中，例如在上述场景中，我们必须小心 Worker 对象。

#### 饥饿（Starvation）

##### 导致饥饿问题的原因

- 高优先级线程吞噬所有的低优先级线程的 CPU 时间。
- 线程被永久堵塞在一个等待进入同步块的状态，因为其他线程总是能在它之前持续地对该同步块进行访问。
- 线程在等待一个本身(在其上调用 wait())也处于永久等待完成的对象，因为其他线程总是被持续地获得唤醒。

<p align="center">
  <img src="http://dunwu.test.upcdn.net/cs/java/javacore/concurrent/starvation-and-fairness.png!zp">
</p>

饥饿问题最经典的例子就是哲学家问题。如图所示：有五个哲学家用餐，每个人要活得两把叉子才可以就餐。当 2、4 就餐时，1、3、5 永远无法就餐，只能看着盘中的美食饥饿的等待着。

##### 解决饥饿问题的方法

Java 不可能实现 100% 的公平性，我们依然可以通过同步结构在线程间实现公平性的提高。

- 使用锁，而不是同步块。
- 公平锁。

### 性能问题

**并发执行一定比串行执行快吗？**

答案是：不一定。因为有创建线程和线程上下文切换的开销。

#### 上下文切换

##### 什么是上下文切换？

当 CPU 从执行一个线程切换到执行另一个线程时，CPU 需要保存当前线程的本地数据，程序指针等状态，并加载下一个要执行的线程的本地数据，程序指针等。这个开关被称为“上下文切换”。

##### 减少上下文切换的方法

- 无锁并发编程 - 多线程竞争锁时，会引起上下文切换，所以多线程处理数据时，可以用一些办法来避免使用锁，如将数据的 ID 按照 Hash 算法取模分段，不同的线程处理不同段的数据。
- CAS 算法 - Java 的 Atomic 包使用 CAS 算法来更新数据，而不需要加锁。
- 使用最少线程 - 避免创建不需要的线程，比如任务很少，但是创建了很多线程来处理，这样会造成大量线程都处于等待状态。
- 使用协程 - 在单线程里实现多任务的调度，并在单线程里维持多个任务间的切换。

#### 资源限制

##### 什么是资源限制

资源限制是指在进行并发编程时，程序的执行速度受限于计算机硬件资源或软件资源。

##### 资源限制引发的问题

在并发编程中，将代码执行速度加快的原则是将代码中串行执行的部分变成并发执行，但是如果将某段串行的代码并发执行，因为受限于资源，仍然在串行执行，这时候程序不仅不会加快执行，反而会更慢，因为增加了上下文切换和资源调度的时间。

##### 如何解决资源限制的问题

在资源限制情况下进行并发编程，根据不同的资源限制调整程序的并发度。

- 对于硬件资源限制，可以考虑使用集群并行执行程序。
- 对于软件资源限制，可以考虑使用资源池将资源复用。

## 参考资料

- [《Java 并发编程实战》](https://item.jd.com/10922250.html)
- [《Java 并发编程的艺术》](https://item.jd.com/11740734.html)
- [《深入理解 Java 虚拟机》](https://item.jd.com/11252778.html)
- http://tutorials.jenkov.com/java-concurrency/benefits.html
- https://www.logicbig.com/tutorials/core-java-tutorial/java-multi-threading/thread-deadlock.html
- https://www.logicbig.com/tutorials/core-java-tutorial/java-multi-threading/thread-livelock.html
- https://www.logicbig.com/tutorials/core-java-tutorial/java-multi-threading/thread-starvation.html
- https://www.zhihu.com/question/33515481
- https://blog.csdn.net/yaosiming2011/article/details/44280797
