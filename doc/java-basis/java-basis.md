#### JAVA中的几种基本数据类型是什么，各自占用多少字节
byte 1字节 = 8bit
short 2字节 即 (-2)的15次方 ~ (2的15次方) - 1
int 4字节 即 (-2)的31次方 ~ (2的31次方) - 1
long 8字节 即 (-2)的63次方 ~ (2的63次方) - 1

float 4字节 float 类型的数值有一个后缀 F（例如：3.14F）    
double 8字节 没有后缀 F  的浮点数值（例如：3.14）默认为 double   
boolean 1字节   
char 2字节 Java中，只要是字符，不管是数字还是英文还是汉字，都占两个字节。使用 Unicode 字符，所有字符均以2个字节存储。

####    String类能被继承吗，为什么。
不能 String 类是final的

```
public final class String implements java.io.Serializable, Comparable<String>, CharSequence {
}
```
####    String，Stringbuffer，StringBuilder的区别。
![String，Stringbuffer，StringBuilder类图](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/java-basis/WX20190623-132039%402x.png)
String 不可变常量字符串

Stringbuffer，StringBuilder 继承 AbstractStringbuilder，全部用父类实现，Stringbuffer线程安全 所有方法使用synchronized 同步保证线程安全。StringBuilder非线程安全


#####   为什么String要设计成不可变
1. 字符串常量池的需要   
字符串常量池(String pool, String intern pool, String保留池) 是Java堆内存中一个特殊的存储区域, 当创建一个String对象时,假如此字符串值已经存在于常量池中,则不会创建一个新的对象,而是引用已经存在的对象。

2. 允许String对象缓存HashCode   
Java中String对象的哈希码被频繁地使用, 比如在hashMap 等容器中。字符串不变性保证了hash码的唯一性,因此可以放心地进行缓存.这也是一种性能优化手段,意味着不必每次都去计算新的哈希码. 在String类的定义中有如下代码:private int hash;//用来缓存HashCode

3. 安全性   
String被许多的Java类(库)用来当做参数,例如 网络连接地址URL,文件路径path,还有反射机制所需要的String参数等, 假若String不是固定不变的,将会引起各种安全隐患。

#####   String类不可变性的好处
1.只有当字符串是不可变的，字符串池才有可能实现。字符串池的实现可以在运行时节约很多heap空间，因为不同的字符串变量都指向池中的同一个字符串。但如果字符串是可变的，那么String interning将不能实现(译者注：String interning是指对不同的字符串仅仅只保存一个，即不会保存多个相同的字符串。)，因为这样的话，如果变量改变了它的值，那么其它指向这个值的变量的值也会一起改变。2.如果字符串是可变的，那么会引起很严重的安全问题。譬如，数据库的用户名、密码都是以字符串的形式传入来获得数据库的连接，或者在socket编程中，主机名和端口都是以字符串的形式传入。因为字符串是不可变的，所以它的值是不可改变的，否则黑客们可以钻到空子，改变字符串指向的对象的值，造成安全漏洞。3.因为字符串是不可变的，所以是多线程安全的，同一个字符串实例可以被多个线程共享。这样便不用因为线程安全问题而使用同步。字符串自己便是线程安全的。4.类加载器要用到字符串，不可变性提供了安全性，以便正确的类被加载。譬如你想加载java.sql.Connection类，而这个值被改成了myhacked.Connection，那么会对你的数据库造成不可知的破坏。5.因为字符串是不可变的，所以在它创建的时候hashcode就被缓存了，不需要重新计算。这就使得字符串很适合作为Map中的键，字符串的处理速度要快过其它的键对象。这就是HashMap中的键往往都使用字符串。

####    ArrayList和LinkedList有什么区别
实现接口区别、arrayList扩容、深拷贝、插入fail-fast
- ArrayList
ArrayList继承AbstractList抽象类实现了 List、RandomAccess、Cloneable接口
内部使用数组 Object[] 保存元素
初始化容量为10

```
 private static final int DEFAULT_CAPACITY = 10;
```


RandomAccess随机访问速度大于迭代器访问
```java
 for (int i=0, n=list.size(); i &lt; n; i++)
 *         list.get(i);
 * </pre>
 * runs faster than this loop:
 * <pre>
 *     for (Iterator i=list.iterator(); i.hasNext(); )
 *         i.next();
 * </pre>
```
fail-fast
内部通过修改次数modCount维护，添加、删除、排序都会使modCount++，
迭代访问时使用除了迭代器删除的方法抛出ConcurrentModificationException并发修改异常
```java
if the list is
 * structurally modified at any time after the iterator is created, in
 * any way except through the Iterator's own {@code remove} or
 * {@code add} methods, the iterator will throw a {@link
 * ConcurrentModificationException}. 
 
 public boolean add(E e) {
    ensureCapacityInternal(size + 1);  // Increments modCount!!
    elementData[size++] = e;
    return true;
}

 private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

//创建迭代器的时候 将modCount 赋值给expectedModCount
 private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;


 //如果修改次数modCount不等于预期expectedModCount，抛出异常
 final void checkForComodification() {
    if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
}
 
```
迭代器中删除、添加动作会使expectedModCount++
不会抛出异常

```java
public void add(E e) {
    checkForComodification();

    try {
        int i = cursor;
        ArrayList.this.add(i, e);
        cursor = i + 1;
        lastRet = -1;
        expectedModCount = modCount;
    } catch (IndexOutOfBoundsException ex) {
        throw new ConcurrentModificationException();
    }
}
```

扩容
```java
 // 当前添加索引大于集合容量的时候，进行扩容
 // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
            
  //扩容
  //oldCapacity + (oldCapacity >> 1);原来容量的1.5倍 浅拷贝替换扩容后数组
  
  private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }


```
高并发添加有现成安全问题

```java
 private static List<Integer> list = new ArrayList<>();

    private static ExecutorService executorService = Executors.newFixedThreadPool(1000);

    private static class IncreaseTask extends Thread{
        @Override
        public void run() {
            for(int i =0; i < 100; i++){
                list.add(i);
            }
        }
    }

    public static void main(String[] args){
        for(int i=0; i < 1000; i++){
            executorService.submit(new IncreaseTask());
        }
        executorService.shutdown();
        while (!executorService.isTerminated()){
            try {
                Thread.sleep(1000*10);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        System.out.println("All task finished!");
        System.out.println("list size is :" + list.size());
    }
```



- LinkedList
linkedlist 比ArrayList