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
1.只有当字符串是不可变的，字符串池才有可能实现。字符串池的实现可以在运行时节约很多heap空间，因为不同的字符串变量都指向池中的同一个字符串。但如果字符串是可变的，那么String interning将不能实现(译者注：String interning是指对不同的字符串仅仅只保存一个，即不会保存多个相同的字符串。)，因为这样的话，如果变量改变了它的值，那么其它指向这个值的变量的值也会一起改变。

2.如果字符串是可变的，那么会引起很严重的安全问题。譬如，数据库的用户名、密码都是以字符串的形式传入来获得数据库的连接，或者在socket编程中，主机名和端口都是以字符串的形式传入。因为字符串是不可变的，所以它的值是不可改变的，否则黑客们可以钻到空子，改变字符串指向的对象的值，造成安全漏洞。

3.因为字符串是不可变的，所以是多线程安全的，同一个字符串实例可以被多个线程共享。这样便不用因为线程安全问题而使用同步。字符串自己便是线程安全的。

4.类加载器要用到字符串，不可变性提供了安全性，以便正确的类被加载。譬如你想加载java.sql.Connection类，而这个值被改成了myhacked.Connection，那么会对你的数据库造成不可知的破坏。5.因为字符串是不可变的，所以在它创建的时候hashcode就被缓存了，不需要重新计算。这就使得字符串很适合作为Map中的键，字符串的处理速度要快过其它的键对象。这就是HashMap中的键往往都使用字符串。

####    ArrayList和LinkedList有什么区别

![java集合框架](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/java-basis/WX20190623-132959%402x.png)

实现接口区别、arrayList扩容、深拷贝、插入fail-fast
####	ArrayList
ArrayList继承AbstractList抽象类实现了 List、RandomAccess、Cloneable、Serializable接口

```java
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
}

/**
 * 只需要继承这个类（AbstractList）并且提供get(int)和size()方法就可实现一个不可修改的list
 * 因为添加方法都是不支持操作 throw new UnsupportedOperationException();
 * <p>To implement an unmodifiable list, the programmer needs only to extend
 * this class and provide implementations for the {@link #get(int)} and
 * {@link List#size() size()} methods.
 *
 * 与其他抽象集合实现不同的是，程序员不需要提供迭代器。迭代器和list iterator的功能被子类通过
 * 随机访问的方法实现
 * <p>Unlike the other abstract collection implementations, the programmer does
 * <i>not</i> have to provide an iterator implementation; the iterator and
 * list iterator are implemented by this class, on top of the "random access"
 * methods:
 * {@link #get(int)},
 * {@link #set(int, Object) set(int, E)},
 * {@link #add(int, Object) add(int, E)} and
 * {@link #remove(int)}.
 *
*/
public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> {

 protected AbstractList() {
    }

    /**
     * 像list尾部添加元素，可选操作。
     * Appends the specified element to the end of this list (optional
     * operation).
     *
     * 某些特定实现不允许添加null，应该在文档中给出说明
     * <p>Lists that support this operation may place limitations on what
     * elements may be added to this list.  In particular, some
     * lists will refuse to add null elements, and others will impose
     * restrictions on the type of elements that may be added.  List
     * classes should clearly specify in their documentation any restrictions
     * on what elements may be added.
     *
     * <p>This implementation calls {@code add(size(), e)}.
     *
     * 注意这个操作抛出不支持操作异常除非该方法被子类重写
     * <p>Note that this implementation throws an
     * {@code UnsupportedOperationException} unless
     * {@link #add(int, Object) add(int, E)} is overridden.
     *
     * 一些可能抛出的异常
     * @param e element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws UnsupportedOperationException if the {@code add} operation
     *         is not supported by this list
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this list
     * @throws NullPointerException if the specified element is null and this
     *         list does not permit null elements
     * @throws IllegalArgumentException if some property of this element
     *         prevents it from being added to this list
     */
    public boolean add(E e) {
        add(size(), e);
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    abstract public E get(int index);

    /**
     * {@inheritDoc}
     *
     * <p>This implementation always throws an
     * {@code UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation always throws an
     * {@code UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation always throws an
     * {@code UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }


    // Search Operations

    /**
     * {@inheritDoc}
     *
     * <p>This implementation first gets a list iterator (with
     * {@code listIterator()}).  Then, it iterates over the list until the
     * specified element is found or the end of the list is reached.
     *
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public int indexOf(Object o) {
        ListIterator<E> it = listIterator();
        if (o==null) {
            while (it.hasNext())
                if (it.next()==null)
                    return it.previousIndex();
        } else {
            while (it.hasNext())
                if (o.equals(it.next()))
                    return it.previousIndex();
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation first gets a list iterator that points to the end
     * of the list (with {@code listIterator(size())}).  Then, it iterates
     * backwards over the list until the specified element is found, or the
     * beginning of the list is reached.
     *
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public int lastIndexOf(Object o) {
        ListIterator<E> it = listIterator(size());
        if (o==null) {
            while (it.hasPrevious())
                if (it.previous()==null)
                    return it.nextIndex();
        } else {
            while (it.hasPrevious())
                if (o.equals(it.previous()))
                    return it.nextIndex();
        }
        return -1;
    }


    // Bulk Operations

    /**
     * Removes all of the elements from this list (optional operation).
     * The list will be empty after this call returns.
     *
     * <p>This implementation calls {@code removeRange(0, size())}.
     *
     * <p>Note that this implementation throws an
     * {@code UnsupportedOperationException} unless {@code remove(int
     * index)} or {@code removeRange(int fromIndex, int toIndex)} is
     * overridden.
     *
     * @throws UnsupportedOperationException if the {@code clear} operation
     *         is not supported by this list
     */
    public void clear() {
        removeRange(0, size());
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation gets an iterator over the specified collection
     * and iterates over it, inserting the elements obtained from the
     * iterator into this list at the appropriate position, one at a time,
     * using {@code add(int, E)}.
     * Many implementations will override this method for efficiency.
     *
     * <p>Note that this implementation throws an
     * {@code UnsupportedOperationException} unless
     * {@link #add(int, Object) add(int, E)} is overridden.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);
        boolean modified = false;
        for (E e : c) {
            add(index++, e);
            modified = true;
        }
        return modified;
    }

 }
```



内部使用数组 Object[] 保存元素
初始化容量为10

```java
 private static final int DEFAULT_CAPACITY = 10;
```

- RandomAccess随机访问速度大于迭代器访问

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
- fail-fast
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
- 迭代器中删除、添加动作会使expectedModCount++
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

- 扩容

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
- 高并发添加有现成安全问题

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



####	LinkedList
linkedlist 继承了AbstractSequentialList,实现了List、Deque、Cloneable、Serializable。

AbstractSequentialList（顺序访问）继承了AbstractList。

而ArrayList则继承了AbstractList（随机访问）

```java
public class LinkedList<E>
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{
}
/**
 * 这个类和AbstractList抽象list在实现"random access"上相反,在迭代器上实现随机访问
 * AbstractSequentialList 迭代器实现随机访问 通过遍历迭代器找出元素
 * (AbstractList是在子类get 等方法上实现，不是在迭代器上实现。)
 * This class is the opposite of the <tt>AbstractList</tt> class in the sense
 * that it implements the "random access" methods (<tt>get(int index)</tt>,
 * <tt>set(int index, E element)</tt>, <tt>add(int index, E element)</tt> and
 * <tt>remove(int index)</tt>) on top of the list's list iterator, instead of
 * the other way around.<p>
 */
public abstract class AbstractSequentialList<E> extends AbstractList<E> {

     /**
     * 在迭代器上实现
     * Returns the element at the specified position in this list.
     *
     * <p>This implementation first gets a list iterator pointing to the
     * indexed element (with <tt>listIterator(index)</tt>).  Then, it gets
     * the element using <tt>ListIterator.next</tt> and returns it.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index) {
        try {
            return listIterator(index).next();
        } catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }
    }

 }
```



- 与ArrayList不同的是linkedlist实现了 Deque无界双端队列的接口

  内部通过双向链表Node，首尾2个指针实现双端队列操作

  

```java
public class LinkedList<E>
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{
 /**
     * Pointer to first node.
     * Invariant: (first == null && last == null) ||
     *            (first.prev == null && first.item != null)
     */
    transient Node<E> first;

    /**
     * Pointer to last node.
     * Invariant: (first == null && last == null) ||
     *            (last.next == null && last.item != null)
     */
    transient Node<E> last;
    
     private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
  
}
```



###	讲讲类的实例化顺序

比如父类静态数据，构造函数，字段，子类静态数据，构造函数，字段，当new的时候，他们的执行顺序。



```
public class Parent {

    public static int A=1;

    static {
        System.out.println("父类：Parent 静态代码块"+A);
    }

    public Parent(){
        System.out.println("父类：Parent 构造器");
    }

    public void say(){
        System.out.println("父类：Parent 方法say");
    }
    
}

```



```java
public class Children extends Parent{


    public static int a=1;

    static {
        System.out.println("子类：Children静态代码块"+a);
    }

    public Children(){
        System.out.println("子类：Children 构造器");
    }

    @Override
    public void say() {
        super.say();
        System.out.println("子类：Children say方法");
        super.say();
    }

    public static void main(String[] args) {
        Parent children=new Children();
        children.say();
    }

  输出结果：
  /Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/bin/java -Dvisualvm.id=193678187082119 "-javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=59022:/Applications/IntelliJ IDEA.app/Contents/bin" -Dfile.encoding=UTF-8 -classpath /Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/charsets.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/deploy.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/cldrdata.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/dnsns.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/jaccess.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/jfxrt.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/localedata.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/nashorn.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/sunec.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/sunjce_provider.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/sunpkcs11.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/ext/zipfs.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/javaws.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/jce.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/jfr.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/jfxswt.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/jsse.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/management-agent.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/plugin.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/resources.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/lib/ant-javafx.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/lib/dt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/lib/javafx-mx.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/lib/jconsole.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/lib/packager.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/lib/sa-jdi.jar:
/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/lib/tools.jar:
/Users/haochencheng/Workspace/java/java-interview/java-interview-basis/target/classes classloader.Children
    
父类：Parent 静态代码块1
子类：Children静态代码块1
父类：Parent 构造器
子类：Children 构造器
父类：Parent 方法say
子类：Children say方法
父类：Parent 方法say

Process finished with exit code 0

}
```



1. java编译器将java文件编译为class文件
2. 加载: 将 Class 文件读入内存，并为之创建一个 java.lang.Class 对象
3. 连接: 为静态域分配内存
4. 初始化: 初始化超类，执行 static
5. 实例化: 创建一个 Object 对象

执行顺序

- 父类静态代码块 

- 父类静态变量

- 子类静态代码块

- 子类静态变量

- 父类构造函数

- 子类构造函数 

深入：

- java平台无关性

  1. class文件

  Java是与平台无关的语言，这得益于Java源代码编译后生成的存储字节码的文件，即Class文件，以及Java虚拟机的实现。不仅使用Java编译器可以把Java代码编译成存储字节码的Class文件，使用JRuby等其他语言的编译器也可以把程序代码编译成Class文件，虚拟机并不关心Class的来源是什么语言，只要它符合一定的结构，就可以在Java中运行。Java语言中的各种变量、关键字和运算符的语义最终都是由多条字节码命令组合而成的，因此字节码命令所能提供的语义描述能力肯定会比Java语言本身更强大，这便为其他语言实现一些有别于Java的语言特性提供了基础，而且这也正是在类加载时要进行安全验证的原因。

  2. class文件结构

     - class文件是一种8位字节的二进制流文件

     - 每个类（或者接口）都单独占据一个class文件，并且类中的所有信息都会在class文件中有相应的描述

     - class文件中的信息是一项一项排列的， 每项数据都有它的固定长度， 有的占一个字节， 有的占两个字节， 还有的占四个字节或8个字节， 数据项的不同长度分别用u1, u2, u4, u8表示， 分别表示一种数据项在class文件中占据一个字节， 两个字节， 4个字节和8个字节。 可以把u1, u2, u3, u4看做class文件数据项的“类型” 。

     一个典型的class文件分为：

     - MagicNumber

     - Version

     - Constant_pool

     - Access_flag

     - This_class

     - Super_class

     - Interfaces

     - Fields

     - Methods 和Attributes这十个部分

       用一个数据结构可以表示如下：

![classStructure](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/java-basis/class_code.png)

1、**magic**

在class文件开头的四个字节， 存放着class文件的魔数， 这个魔数是class文件的标志，他是一个固定的值： 0XCAFEBABE 。 也就是说他是判断一个文件是不是class格式的文件的标准， 如果开头四个字节不是0XCAFEBABE， 那么就说明它不是class文件， 不能被JVM识别。

2、**minor_version 和 major_version**

紧接着魔数的四个字节是class文件的此版本号和主版本号。

3、**constant_pool**

在class文件中， 位于版本号后面的就是常量池相关的数据项。 常量池是class文件中的一项非常重要的数据。 常量池中存放了文字字符串， 常量值， 当前类的类名， 字段名， 方法名， 各个字段和方法的描述符， 对当前类的字段和方法的引用信息， 当前类中对其他类的引用信息等等。

常量池是一个类的结构索引，其它地方对“对象”的引用可以通过索引位置来代替，我们知道在程序中一个变量可以不断地被调用，要快速获取这个变量常用的方法就是通过索引变量。这种索引我们可以直观理解为“虚拟的内存地址”。

常量池中的第一项的索引为1, 而不为0。

常量池的数据类型：

![constant-pool](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/java-basis/constant_pool.png)

4、**access_flag** 

保存了当前类的访问权限

5、**this_cass** 

保存了当前类的全局限定名在常量池里的索引

6、**super class** 

保存了当前类的父类的全局限定名在常量池里的索引

7、**interfaces** 

保存了当前类实现的接口列表，包含两部分内容：interfaces_count 和interfaces[interfaces_count]
interfaces_count 指的是当前类实现的接口数目
interfaces[] 是包含interfaces_count个接口的全局限定名的索引的数组

8、**fields** 

保存了当前类的成员列表，包含两部分的内容：fields_count 和 fields[fields_count]
fields_count是类变量和实例变量的字段的数量总和。
fileds[]是包含字段详细信息的列表。

9、**methods** 

保存了当前类的方法列表，包含两部分的内容：methods_count和methods[methods_count]
methods_count是该类或者接口显示定义的方法的数量。
method[]是包含方法信息的一个详细列表。

10、**attributes** 

包含了当前类的attributes列表，包含两部分内容：attributes_count 和 attributes[attributes_count]

如图idea工具查看class文件结构

![javaclass](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/java-basis/java-class.png)

16进制编辑器查看Parent.class文件

```
CAFEBABE 00000034 00360A00 0E001C09 001D001E 08001F0A 00200021 08002209 000D0023 0700240A 0007001C 0800250A 00070026 0A000700 270A0007 00280700 2907002A 01000141 01000149 0100063C 696E6974 3E010003 28295601 0004436F 64650100 0F4C696E 654E756D 62657254 61626C65 0100124C 6F63616C 56617269 61626C65 5461626C 65010004 74686973 0100144C 636C6173 736C6F61 6465722F 50617265 6E743B01 00037361 79010008 3C636C69 6E69743E 01000A53 6F757263 6546696C 6501000B 50617265 6E742E6A 6176610C 00110012 07002B0C 002C002D 010019E7 88B6E7B1 BBEFBC9A 50617265 6E7420E6 9E84E980 A0E599A8 07002E0C 002F0030 010019E7 88B6E7B1 BBEFBC9A 50617265 6E7420E6 96B9E6B3 95736179 0C000F00 10010017 6A617661 2F6C616E 672F5374 72696E67 4275696C 64657201 001FE788 B6E7B1BB EFBC9A50 6172656E 7420E99D 99E68081 E4BBA3E7 A081E59D 970C0031 00320C00 3100330C 00340035 01001263 6C617373 6C6F6164 65722F50 6172656E 74010010 6A617661 2F6C616E 672F4F62 6A656374 0100106A 6176612F 6C616E67 2F537973 74656D01 00036F75 74010015 4C6A6176 612F696F 2F507269 6E745374 7265616D 3B010013 6A617661 2F696F2F 5072696E 74537472 65616D01 00077072 696E746C 6E010015 284C6A61 76612F6C 616E672F 53747269 6E673B29 56010006 61707065 6E640100 2D284C6A 6176612F 6C616E67 2F537472 696E673B 294C6A61 76612F6C 616E672F 53747269 6E674275 696C6465 723B0100 1C284929 4C6A6176 612F6C61 6E672F53 7472696E 67427569 6C646572 3B010008 746F5374 72696E67 01001428 294C6A61 76612F6C 616E672F 53747269 6E673B00 21000D00 0E000000 01000900 0F001000 00000300 01001100 12000100 13000000 3F000200 01000000 0D2AB700 01B20002 1203B600 04B10000 00020014 0000000E 00030000 00110004 0012000C 00130015 0000000C 00010000 000D0016 00170000 00010018 00120001 00130000 00370002 00010000 0009B200 021205B6 0004B100 00000200 14000000 0A000200 00001600 08001700 15000000 0C000100 00000900 16001700 00000800 19001200 01001300 00004000 03000000 00002004 B30006B2 0002BB00 0759B700 081209B6 000AB200 06B6000B B6000CB6 0004B100 00000100 14000000 0E000300 00000B00 04000E00 1F000F00 01001A00 00000200 1B
```

使用javap 解析 class信息

`javap -v -p -s -sysinfo -constants Parent`

```
警告: 二进制文件Parent包含classloader.Parent
Classfile /Users/haochencheng/Workspace/java/java-interview/java-interview-basis/target/classes/classloader/Parent.class
  Last modified 2019-6-23; size 889 bytes
  MD5 checksum ef9781e7c739f3c463c8e55f31651d68
  Compiled from "Parent.java"
public class classloader.Parent
  minor version: 0
  major version: 49
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   #1 = Methodref          #14.#28        // java/lang/Object."<init>":()V
   #2 = Fieldref           #29.#30        // java/lang/System.out:Ljava/io/PrintStream;
   #3 = String             #31            // 父类：Parent 构造器
   #4 = Methodref          #32.#33        // java/io/PrintStream.println:(Ljava/lang/String;)V
   #5 = String             #34            // 父类：Parent 方法say
   #6 = Fieldref           #13.#35        // classloader/Parent.A:I
   #7 = Class              #36            // java/lang/StringBuilder
   #8 = Methodref          #7.#28         // java/lang/StringBuilder."<init>":()V
   #9 = String             #37            // 父类：Parent 静态代码块
  #10 = Methodref          #7.#38         // java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
  #11 = Methodref          #7.#39         // java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
  #12 = Methodref          #7.#40         // java/lang/StringBuilder.toString:()Ljava/lang/String;
  #13 = Class              #41            // classloader/Parent
  #14 = Class              #42            // java/lang/Object
  #15 = Utf8               A
  #16 = Utf8               I
  #17 = Utf8               <init>
  #18 = Utf8               ()V
  #19 = Utf8               Code
  #20 = Utf8               LineNumberTable
  #21 = Utf8               LocalVariableTable
  #22 = Utf8               this
  #23 = Utf8               Lclassloader/Parent;
  #24 = Utf8               say
  #25 = Utf8               <clinit>
  #26 = Utf8               SourceFile
  #27 = Utf8               Parent.java
  #28 = NameAndType        #17:#18        // "<init>":()V
  #29 = Class              #43            // java/lang/System
  #30 = NameAndType        #44:#45        // out:Ljava/io/PrintStream;
  #31 = Utf8               父类：Parent 构造器
  #32 = Class              #46            // java/io/PrintStream
  #33 = NameAndType        #47:#48        // println:(Ljava/lang/String;)V
  #34 = Utf8               父类：Parent 方法say
  #35 = NameAndType        #15:#16        // A:I
  #36 = Utf8               java/lang/StringBuilder
  #37 = Utf8               父类：Parent 静态代码块
  #38 = NameAndType        #49:#50        // append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
  #39 = NameAndType        #49:#51        // append:(I)Ljava/lang/StringBuilder;
  #40 = NameAndType        #52:#53        // toString:()Ljava/lang/String;
  #41 = Utf8               classloader/Parent
  #42 = Utf8               java/lang/Object
  #43 = Utf8               java/lang/System
  #44 = Utf8               out
  #45 = Utf8               Ljava/io/PrintStream;
  #46 = Utf8               java/io/PrintStream
  #47 = Utf8               println
  #48 = Utf8               (Ljava/lang/String;)V
  #49 = Utf8               append
  #50 = Utf8               (Ljava/lang/String;)Ljava/lang/StringBuilder;
  #51 = Utf8               (I)Ljava/lang/StringBuilder;
  #52 = Utf8               toString
  #53 = Utf8               ()Ljava/lang/String;
{
  public static int A;
    descriptor: I
    flags: ACC_PUBLIC, ACC_STATIC

  public classloader.Parent();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
         7: ldc           #3                  // String 父类：Parent 构造器
         9: invokevirtual #4                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        12: return
      LineNumberTable:
        line 17: 0
        line 18: 4
        line 19: 12
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      13     0  this   Lclassloader/Parent;

  public void say();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=1, args_size=1
         0: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
         3: ldc           #5                  // String 父类：Parent 方法say
         5: invokevirtual #4                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
         8: return
      LineNumberTable:
        line 22: 0
        line 23: 8
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       9     0  this   Lclassloader/Parent;

  static {};
    descriptor: ()V
    flags: ACC_STATIC
    Code:
      stack=3, locals=0, args_size=0
         0: iconst_1
         1: putstatic     #6                  // Field A:I
         4: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
         7: new           #7                  // class java/lang/StringBuilder
        10: dup
        11: invokespecial #8                  // Method java/lang/StringBuilder."<init>":()V
        14: ldc           #9                  // String 父类：Parent 静态代码块
        16: invokevirtual #10                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
        19: getstatic     #6                  // Field A:I
        22: invokevirtual #11                 // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
        25: invokevirtual #12                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
        28: invokevirtual #4                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        31: return
      LineNumberTable:
        line 11: 0
        line 14: 4
        line 15: 31
}
SourceFile: "Parent.java"
```



接下来我们就按照class文件的格式来分析上面的一串数字，还是按照之前的顺序来：

1. **magic** 

   > CAFEBABE 
   >
   > 代表该文件是一个字节码文件，我们平时区分文件类型都是通过后缀名来区分的，不过后缀名是可以随便修改的，所以仅靠后缀名不能真正区分一个文件的类型。区分文件类型的另个办法就是magic数字，JVM 就是通过 CA FE BA BE 来判断该文件是不是class文件

2. **version字段**：

   >00000034
   >
   >前两个字节00是minor_version，后两个字节0034是major_version字段，对应的十进制值为52，也就是说当前class文件的主版本号为52，次版本号为0。下表是jdk 1.6 以后对应支持的 Class 文件版本号：

   ![jdkversion](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/java-basis/jdkversion.png)

3. **常量池，constant_pool**

   > `constant_pool_count`
   > 紧接着version字段下来的两个字节是：`00 36`代表常量池里包含的常量数目，因为字节码的常量池是从1开始计数的，这个常量池包含53个（0x0036-1）常量。




###	

###	用过哪些Map类，都有什么区别，HashMap是线程安全的吗,并发下使用的Map是什么，他们内部原理分别是什么，比如存储方式，hashcode，扩容，默认容量等。

java集合框架顶层接口

Collection,Map。

HashMap 键值对数据结构,底层存储数组加链表结构。

