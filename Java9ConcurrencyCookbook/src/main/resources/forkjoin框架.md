# fork/join 框架

本章内容
+   创建一个fork/join 池
+   合并任务的执行结果
+   异步地运行任务
+   在任务中抛出异常
+   取消一个任务

## 简介

Java5增加了Executor和ExecutorService来将任务和执行分离。

Java7增加了fork/join用来处理特定问题的额外实现---fork/join框架。该框架的目的是以分而治之的思想来解决那些可以分解成较小任务的问题。

该框架基于两个操作
+   fork操作，当将任务分割成更小的任务时使用该框架执行
+   join操作，组合执行结果

fork/join框架与Executor框架的主要区别是工作量测 算法，当一个任务使用join操作等待它创建的子任务执行结束时，执行任务的线程，会寻找其他未执行的任务
并执行，线程可以最大力度的利用，从而提升性能。

由于线程池的优化，提交的任务和线程数量并不是一对一的关系。在绝大多数情况下，一个物理线程实际上是需要处理多个逻辑任务的。
因此，每个线程必然需要拥有一个任务队列。因此，在实际执行过程中，可能遇到这么一种情况：线程A已经把自己的任务都处理完了，而线程B还有一堆任务等着处理，
此时，线程A就会“帮助” 线程B，从线程 B的任务队列中拿一个任务来处理，尽可能的达到平衡。
值得注意的是：当线程试图帮助别人时，总是从任务队列的底部开始拿数据，而线程试图执行自己的任务时，则从相反的顶部开始拿。因此这种行为也十分有利于避免数据竞争。

因此，这个框架有如下限制
+   任务只能使用fork和join作为同步机制，若在任务中使用来其他同步机制，则工作线程无法在同步操作里执行其他任务。比如让一个任务进入休眠，则此线程也不能干别的事了。
+   任务不执行任何io操作，包括读写
+   任务不能抛出受检异常，任务必须包含必要的处理异常的代码

fork/join核心由以下两个类组成
+   ForkJoinPool    实现了ExecutorService接口和工作测量算法，管理工作线程并提供任务本身和任务执行过程的信息
+   ForkJoinTask    在Pool里执行的基类，提供里fork和join操作。一般来说，需要实现该类的三个子类中的一个RecursiveAction（不返回结果）；RecursiveTask（返回结果）
CountedCompleter（所有子任务完成后执行一个操作方法）

函数的实现需要按照javaAPI的规定来实现

## 创建一个ForkJoin池

见ProductTask，执行的参数必须是ForkJoinTask，才会使用分而治之的池子来操作。

## 合并任务的执行结果

见DocumentTask，一个文本结构，查找某个单词出现的次数，把文档，分解成一行行去查

+   DocumentTask:根据start和end对文档的处理行集合。若行小于10则新建lineTask，执行完毕后返回结果。若大于10则新建2个子任务，最后合并。
+   LineTask:处理一行的单词，若单词数小于100，直接搜索，否则分成两个子集，最后合并。 
   
ForkJoinTask还提供里另一个方法来执行一个任务并返回结果，即Complete（），看api的解释。

## 异步地运行任务

在ForkJoinPool执行task时，可以选择同步，也可以选择异步。

采用同步时，才会去度量工作线程和任务数量；而异步，则任务继续执行，pool不会去提升性能。

CountedCompleter类，使用此类，你可以加入一个完成方法。当任务启动且没有处理其中的子任务时，会调用此方法，
该机制基于ForkJoinTask类中的onCompletion方法和一个处理任务的计数器来实现。

该计数器默认值为0，需要时自增。一般情况，启动子任务，计数器自动+1，当一个任务执行完成，可以尝试结束任务，并调用onCompletion方法，若计数器值大于0，
则递减，若为0，则父方法会尝试结束

## 在任务中抛出异常

见ExceptionTask

## 取消任务

ForkJoinTask自带cancel方法，但是要考虑以下情况
+   ForkJoinPool 不提供取消池中全部等待执行和正执行的方法
+   当取消一个任务时，不能取消已经执行的任务

取消还未执行的任务，其实无法停止正在运行的任务

ForkJoin自身是不允许直接取消所有任务的，但是在例子中使用了TaskManager来取消

总结，还是太弱鸡，都是class，以后必然重写为接口而非固定实现

