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

- java编译器将java文件便以为class文件

javac 将.java文件编译为.class 文件

- java class 文件结构

  