# 学习笔记
## 作业
1. GC 总结：

### 标记-清除算法

- Marking，**遍历所有可达对象，并在本地内存（native）中分门别类记下来（标记的时候需要STW，让世界停止，停止所有用户线程）**
- Sweeping，不可达对象占用的内存，在之后进行内存分配时可以重用

并行GC和CMS基本原理

优势：可以处理循环依赖，只扫描部分对象

除了清除，还要做压缩。（碎片整理，让内存连续）

怎么标记和清除上百万的对象？

答案就是 STW (Stop The World )，系统停止，开始GC

### 分代假设

假设大部分对象是快速消亡的，基于此，区分老年代和新生代，

根据不同类型的对象，划分不同区域，进行不同策略处理。

- 大对象直接到老年代（-XX: PretenureSizeThreshold）（如果对象大于Eden区，直接划分到老年代），
- 在Young区经历了15次的GC的对象划分到老年代


### 并行GC Parallel GC

```shell
# 
-XX: +UseParallelGC
-XX: +UseParallelOldGC
-XX: +UseParallelGC -XX:UseParallelOldGC

# 指定 GC 线程数，其默认值为 CPU 核心数
-XX: +ParallelGCThreads=N 
```

年轻代和老年代都会触发 STW ，在年轻代使用 标记-复制 (mark-copy) 算法，在老年代使用 标记-清除-整理 （mark-sweep-compact）算法。

- 在暂停期间，所有CPU内核都在并行清理垃圾，所以总暂停时间更短
- 在两次GC周期的间隔期，没有GC线程在运行，不会消耗任何系统资源

### 默认GC策略

JDK8默认GC策略是并行GC （ParallelGC）

JDK9之后版本的默认GC策略是 G1

## CMS GC 并发GC

```sql
-XX: +UseConcMarkSweepGC

```

对年轻代采用STW方法的 `mark-copy` 标记-复制 算法，对老年代使用 `mark-sweep` 标记-清除 算法。

### OOM 的原因

堆内存的空间不足以存放新的对象，堆内存过小，调大即可。

- 超出预期的访问量/数据量，加载到内存中的数据量过大，过多，会导致内存超出设计的限制
- 内存泄漏，由于代码中的隐蔽错误，导致系统占用的内存越来越多，如果某个方法/某段代码中存在代码泄漏，每执行一次，就会占用更多的内存。

OutOfMemoryError：PermGen space 的主要原因，是加载到内存中的class数量太多或体积太大，超过了 PermGen区的大小

解决方法，加大 PermGen / Metaspace 区

-XX: MaxPermSize=512m

-XX: MaxMetaspaceSize=512m

2. 



## 题目
Week02 作业题目（周四）：

1.使用 GCLogAnalysis.java 自己演练一遍串行 / 并行 /CMS/G1 的案例。

2.使用压测工具（wrk 或 sb），演练 gateway-server-0.0.1-SNAPSHOT.jar 示例。

3.（选做） 如果自己本地有可以运行的项目，可以按照 2 的方式进行演练。
4.（必做） 根据上述自己对于 1 和 2 的演示，写一段对于不同 GC 的总结，提交到 Github。

Week02 作业题目（周六）：

1.（选做）运行课上的例子，以及 Netty 的例子，分析相关现象。
2.（必做）写一段代码，使用 HttpClient 或 OkHttp 访问 http://localhost:8801 ，代码提交到 Github。

以上作业，要求 2 道必做题目提交到 Github 上面，Week02 作业提交地址：
https://github.com/JAVA-000/JAVA-000/issues/113

请务必按照示例格式进行提交，不要复制其他同学的格式，以免格式错误无法抓取作业。
作业提交截止时间 10 月 28 日（下周三）23:59 前。

Github 使用教程： https://u.geekbang.org/lesson/51?article=294701

学号查询方式：PC 端登录 time.geekbang.org, 点击右上角头像进入我的教室，左侧头像下方 G 开头的为学号


