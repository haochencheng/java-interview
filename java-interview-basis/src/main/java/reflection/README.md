### java 反射
java反射 调用 通过Method.invoke 委托给sun.reflect.MethodAccessor 来处理
jdk1.6中反射安全校验中存在synchronized锁 性能较差 jdk 8中移除
```java
//jdk1.8
public final class Method extends Executable {
    
    /**
    * 访问器方法 优化反射
    */
    private volatile MethodAccessor methodAccessor;
    
        @CallerSensitive
        public Object invoke(Object obj, Object... args)
            throws IllegalAccessException, IllegalArgumentException,
               InvocationTargetException
        {
            if (!override) {
                if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                    Class<?> caller = Reflection.getCallerClass();
                    checkAccess(caller, clazz, obj, modifiers);
                }
            }
            MethodAccessor ma = methodAccessor;             // read volatile
            if (ma == null) {
                ma = acquireMethodAccessor();
            }
            return ma.invoke(obj, args);
        }
        
}

//jdk 1.6

public final  
    class Method extends AccessibleObject implements GenericDeclaration,   
                             Member {  
    // ...  
      
    private volatile MethodAccessor methodAccessor;  
    // For sharing of MethodAccessors. This branching structure is  
    // currently only two levels deep (i.e., one root Method and  
    // potentially many Method objects pointing to it.)  
    private Method              root;  
  
    // ...  
      
    public Object invoke(Object obj, Object... args)  
            throws IllegalAccessException, IllegalArgumentException,  
            InvocationTargetException  
    {  
        if (!override) {  
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {  
                Class caller = Reflection.getCallerClass(1);  
                Class targetClass = ((obj == null || !Modifier.isProtected(modifiers))  
                                     ? clazz  
                                     : obj.getClass());  
                boolean cached;  
                //同步锁 - - 性能开销大
                synchronized (this) {  
                    cached = (securityCheckCache == caller)  
                        && (securityCheckTargetClassCache == targetClass);  
                }  
                if (!cached) {  
                    Reflection.ensureMemberAccess(caller, clazz, obj, modifiers);  
                    synchronized (this) {  
                    securityCheckCache = caller;  
                    securityCheckTargetClassCache = targetClass;  
                    }  
                }  
            }  
        }  
        if (methodAccessor == null) acquireMethodAccessor();  
        return methodAccessor.invoke(obj, args);  
    }  
      
    // NOTE that there is no synchronization used here. It is correct  
    // (though not efficient) to generate more than one MethodAccessor  
    // for a given Method. However, avoiding synchronization will  
    // probably make the implementation more scalable.  
    private void acquireMethodAccessor() {  
        // First check to see if one has been created yet, and take it  
        // if so  
        MethodAccessor tmp = null;  
        if (root != null) tmp = root.getMethodAccessor();  
        if (tmp != null) {  
            methodAccessor = tmp;  
            return;  
        }  
        // Otherwise fabricate one and propagate it up to the root  
        tmp = reflectionFactory.newMethodAccessor(this);  
        setMethodAccessor(tmp);  
    }  
      
    // ...  
}  

``` 

####    MethodAccessor
MethodAccessor接口 定义了反射调用方法
```java
public interface MethodAccessor {
    Object invoke(Object var1, Object[] var2) throws IllegalArgumentException, InvocationTargetException;
}
```
####    NativeMethodAccessorImpl类
NativeMethodAccessorImpl类 默认的 MethodAccessor 实现类，MethodAccessorImpl是抽象类，（为什么不用Abstract开头。。。）
```java
class NativeMethodAccessorImpl extends MethodAccessorImpl {
    private final Method method;
    private DelegatingMethodAccessorImpl parent;
    private int numInvocations;

    NativeMethodAccessorImpl(Method var1) {
        this.method = var1;
    }
    
    //超过15次 使用java生成代理类
     private static int inflationThreshold = 15;
    
    //参考 R大 https://rednaxelafx.iteye.com/blog/548536
    @CallerSensitive
    public Object invoke(Object var1, Object[] var2) throws IllegalArgumentException, InvocationTargetException {
        // Sun的JDK使用了“inflation”的技巧 开头若干次使用 native版，等反射调用次数超过阈值时则生成一个专用的MethodAccessor实现类，
        // 生成其中的invoke()方法的字节码，以后对该Java方法的反射调用就会使用Java版。 
        //统计反射调用次数 超过膨胀阈值 使用 MethodAccessorGenerator 使用 asm 生成代理类 用于反射优化
        // Java实现的版本在初始化时需要较多时间，但长久来说性能较好
        if (++this.numInvocations > ReflectionFactory.inflationThreshold() && !ReflectUtil.isVMAnonymousClass(this.method.getDeclaringClass())) {
        //每次NativeMethodAccessorImpl.invoke()方法被调用时，都会增加一个调用次数计数器，看超过阈值没有；一旦超过，则调用MethodAccessorGenerator.generateMethod()来生成Java版的MethodAccessor的实现类，并且改变DelegatingMethodAccessorImpl所引用的MethodAccessor为Java版。后续经由DelegatingMethodAccessorImpl.invoke()调用到的就是Java版的实现了。
            MethodAccessorImpl var3 = (MethodAccessorImpl)(new MethodAccessorGenerator()).generateMethod(this.method.getDeclaringClass(), this.method.getName(), this.method.getParameterTypes(), this.method.getReturnType(), this.method.getExceptionTypes(), this.method.getModifiers());
            this.parent.setDelegate(var3);
        }
        // 调用native 方法 ，运行时 解释 字节码为 机器码 ，解释一行运行一行 ， 时间短 执行时间 快
        // native版本正好相反，启动时相对较快，但运行时间长了之后速度就比不过Java版了。
        
        return invoke0(this.method, var1, var2);
    }

}

```

####    @CallSensitive
@CallSensitive是JVM中专用的注解，在类加载过过程中是可以常常看到这个注解的身影的，
@CallSensitive用来找到真正发起反射请求的类
```
    @CallerSensitive
    public static Class<?> forName(String className)
    throws ClassNotFoundException {
    Class<?> caller = Reflection.getCallerClass();
    return forName0(className, true, ClassLoader.getClassLoader(caller), caller);
    }
```
注意：Reflection.getCallerClass()方法调用所在的方法必须用@CallerSensitive进行注解

这个注解是为了堵住漏洞用的。曾经有黑客通过构造双重反射来提升权限，原理是当时反射只检查固定深度的调用者的类，
看它有没有特权，例如固定看两层的调用者（getCallerClass(2)）。如果我的类本来没足够权限群访问某些信息
，那我就可以通过双重反射去达到目的：反射相关的类是有很高权限的，而在 我->反射1->反射2 这样的调用链上，
反射2检查权限时看到的是反射1的类，这就被欺骗了，导致安全漏洞。使用CallerSensitive后，
getCallerClass不再用固定深度去寻找actual caller（“我”），
而是把所有跟反射相关的接口方法都标注上CallerSensitive，搜索时凡看到该注解都直接跳过，
这样就有效解决了前面举例的问题 


####    MethodAccessorGenerator
生成反射 代理类 
```java
class MethodAccessorGenerator extends AccessorGenerator {
    
   protected ClassFileAssembler asm;
    
   public MethodAccessor generateMethod(Class<?> var1, String var2, Class<?>[] var3, Class<?> var4, Class<?>[] var5, int var6) {
        return (MethodAccessor)this.generate(var1, var2, var3, var4, var5, var6, false, false, (Class)null);
   }
   
   //使用 类文件汇编 ClassFileAssembler asm 生成代理类
   private MagicAccessorImpl generate(final Class<?> var1, String var2, Class<?>[] var3, Class<?> var4, Class<?>[] var5, int var6, boolean var7, boolean var8, Class<?> var9) {
           ByteVector var10 = ByteVectorFactory.create();
           this.asm = new ClassFileAssembler(var10);
           this.declaringClass = var1;
           this.parameterTypes = var3;
           this.returnType = var4;
           this.modifiers = var6;
           this.isConstructor = var7;
           this.forSerialization = var8;
           this.asm.emitMagicAndVersion();
           short var11 = 42;
           boolean var12 = this.usesPrimitiveTypes();
           if (var12) {
               var11 = (short)(var11 + 72);
           }
   
           if (var8) {
               var11 = (short)(var11 + 2);
           }
   
           var11 += (short)(2 * this.numNonPrimitiveParameterTypes());
           this.asm.emitShort(add(var11, (short)1));
           final String var13 = generateName(var7, var8);
           this.asm.emitConstantPoolUTF8(var13);
           this.asm.emitConstantPoolClass(this.asm.cpi());
           this.thisClass = this.asm.cpi();
           if (var7) {
               if (var8) {
                   this.asm.emitConstantPoolUTF8("sun/reflect/SerializationConstructorAccessorImpl");
               } else {
                   this.asm.emitConstantPoolUTF8("sun/reflect/ConstructorAccessorImpl");
               }
           } else {
               this.asm.emitConstantPoolUTF8("sun/reflect/MethodAccessorImpl");
           }
   
           this.asm.emitConstantPoolClass(this.asm.cpi());
           this.superClass = this.asm.cpi();
           this.asm.emitConstantPoolUTF8(getClassName(var1, false));
           this.asm.emitConstantPoolClass(this.asm.cpi());
           this.targetClass = this.asm.cpi();
           short var14 = 0;
           if (var8) {
               this.asm.emitConstantPoolUTF8(getClassName(var9, false));
               this.asm.emitConstantPoolClass(this.asm.cpi());
               var14 = this.asm.cpi();
           }
   
           this.asm.emitConstantPoolUTF8(var2);
           this.asm.emitConstantPoolUTF8(this.buildInternalSignature());
           this.asm.emitConstantPoolNameAndType(sub(this.asm.cpi(), (short)1), this.asm.cpi());
           if (this.isInterface()) {
               this.asm.emitConstantPoolInterfaceMethodref(this.targetClass, this.asm.cpi());
           } else if (var8) {
               this.asm.emitConstantPoolMethodref(var14, this.asm.cpi());
           } else {
               this.asm.emitConstantPoolMethodref(this.targetClass, this.asm.cpi());
           }
   
           this.targetMethodRef = this.asm.cpi();
           if (var7) {
               this.asm.emitConstantPoolUTF8("newInstance");
           } else {
               this.asm.emitConstantPoolUTF8("invoke");
           }
   
           this.invokeIdx = this.asm.cpi();
           if (var7) {
               this.asm.emitConstantPoolUTF8("([Ljava/lang/Object;)Ljava/lang/Object;");
           } else {
               this.asm.emitConstantPoolUTF8("(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
           }
   
           this.invokeDescriptorIdx = this.asm.cpi();
           this.nonPrimitiveParametersBaseIdx = add(this.asm.cpi(), (short)2);
   
           for(int var15 = 0; var15 < var3.length; ++var15) {
               Class var16 = var3[var15];
               if (!isPrimitive(var16)) {
                   this.asm.emitConstantPoolUTF8(getClassName(var16, false));
                   this.asm.emitConstantPoolClass(this.asm.cpi());
               }
           }
   
           this.emitCommonConstantPoolEntries();
           if (var12) {
               this.emitBoxingContantPoolEntries();
           }
   
           if (this.asm.cpi() != var11) {
               throw new InternalError("Adjust this code (cpi = " + this.asm.cpi() + ", numCPEntries = " + var11 + ")");
           } else {
               this.asm.emitShort((short)1);
               this.asm.emitShort(this.thisClass);
               this.asm.emitShort(this.superClass);
               this.asm.emitShort((short)0);
               this.asm.emitShort((short)0);
               this.asm.emitShort((short)2);
               this.emitConstructor();
               this.emitInvoke();
               this.asm.emitShort((short)0);
               var10.trim();
               final byte[] var17 = var10.getData();
               return (MagicAccessorImpl)AccessController.doPrivileged(new PrivilegedAction<MagicAccessorImpl>() {
                   public MagicAccessorImpl run() {
                       try {
                           return (MagicAccessorImpl)ClassDefiner.defineClass(var13, var17, 0, var17.length, var1.getClassLoader()).newInstance();
                       } catch (IllegalAccessException | InstantiationException var2) {
                           throw new InternalError(var2);
                       }
                   }
               });
           }
       }
    
}
```
我们想看下 
MethodAccessorGenerator 生成的代理类，怎么做呢。


使用 ``sudo java -cp $JAVA_HOME/lib/sa-jdi.jar sun.jvm.hotspot.HSDB`` 命令 
jps 查看pid 
点击File->Attach to...输入pid
点击 Tools -> Class Browser
🔍  GeneratedMethodAccessor
点击 save class  
保存如下 ：  /sun/reflect/GeneratedMethodAccessor1.class

这种方式 生成的不是代理对象而是字节码的自己数组，所以只能用来保存成文件，用于反编译。

用反编译工具 JD-GUI查看 字节码为：
```
package sun.reflect;

public class GeneratedMethodAccessor1
  extends MethodAccessorImpl
{
  /* Error */
  public Object invoke(Object paramObject, Object[] paramArrayOfObject)
    throws java.lang.reflect.InvocationTargetException
  {
    // Byte code:
    //   0: aload_1
    //   1: ifnonnull +11 -> 12
    //   4: new 18	java/lang/NullPointerException
    //   7: dup
    //   8: invokespecial 26	java/lang/NullPointerException:<init>	()V
    //   11: athrow
    //   12: aload_1
    //   13: checkcast 6	proxy/Subject
    //   16: aload_2
    //   17: ifnull +19 -> 36
    //   20: aload_2
    //   21: arraylength
    //   22: sipush 0
    //   25: if_icmpeq +11 -> 36
    //   28: new 20	java/lang/IllegalArgumentException
    //   31: dup
    //   32: invokespecial 27	java/lang/IllegalArgumentException:<init>	()V
    //   35: athrow
    //   36: invokeinterface 10 1 0
    //   41: aconst_null
    //   42: areturn
    //   43: invokespecial 40	java/lang/Object:toString	()Ljava/lang/String;
    //   46: new 20	java/lang/IllegalArgumentException
    //   49: dup_x1
    //   50: swap
    //   51: invokespecial 30	java/lang/IllegalArgumentException:<init>	(Ljava/lang/String;)V
    //   54: athrow
    //   55: new 22	java/lang/reflect/InvocationTargetException
    //   58: dup_x1
    //   59: swap
    //   60: invokespecial 33	java/lang/reflect/InvocationTargetException:<init>	(Ljava/lang/Throwable;)V
    //   63: athrow
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	64	0	this	GeneratedMethodAccessor1
    //   0	64	1	paramObject	Object
    //   0	64	2	paramArrayOfObject	Object[]
    //   43	1	3	localClassCastException	ClassCastException
    //   55	1	4	localThrowable	Throwable
    // Exception table:
    //   from	to	target	type
    //   12	36	43	java/lang/ClassCastException
    //   12	36	43	java/lang/NullPointerException
    //   36	41	55	java/lang/Throwable
  }
}

```
源码为：
```java

package sun.reflect;

import java.lang.reflect.InvocationTargetException;
import proxy.Subject;

public class GeneratedMethodAccessor1 extends MethodAccessorImpl {
    public Object invoke(Object var1, Object[] var2) throws InvocationTargetException {
        if (var1 == null) {
            throw new NullPointerException();
        } else {
            Subject var10000;
            try {
                var10000 = (Subject)var1;
                if (var2 != null && var2.length != 0) {
                    throw new IllegalArgumentException();
                }
            } catch (NullPointerException | ClassCastException var4) {
                throw new IllegalArgumentException(var4.toString());
            }

            try {
                var10000.speak();
                return null;
            } catch (Throwable var3) {
                throw new InvocationTargetException(var3);
            }
        }
    }

    public GeneratedMethodAccessor1() {
    }
}

```
可以看到jdk生成的 反射调用 代理类 很简单，和直接调用相差无几。
多了一些校验 如 类型转换校验 
13: checkcast 6	proxy/Subject