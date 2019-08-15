package concurrent.synchronizedt;

import java.util.*;

/**
 * 监视器锁
 * @description:
 * @author: haochencheng
 * @create: 2019-08-13 23:33
 **/
public class ObjectMonitor {

    private ObjectMonitor(){

    }

    public static Set waitSet=new HashSet();

    public static LinkedList<ThreadNew> entryList=new LinkedList<>();

    /**
     * 拥有锁线程
     */
    public static ThreadNew owoner;

    public static Integer count=0;

    public static boolean sync(ThreadNew threadNew){
        System.out.printf("线程{%d}尝试获取锁\n",threadNew.getId());
        if (Objects.isNull(owoner)){
            owoner=threadNew;
            count+=ObjectMonitor.count;
            System.out.printf("成功！线程{%d}获取锁\n",threadNew.getId());
            return true;
        }else {
            //已经有线程持有锁
            if (owoner==threadNew){
                //当前线程持有锁
                count+=ObjectMonitor.count;
                System.out.printf("成功！线程{%d}获取锁\n",threadNew.getId());
                return true;
            }else {
                //将当前线程放入同步队列
                if (threadNew.isBusy()){
                    //如果线程被挂起 加入 waitSet
                    if (!waitSet.contains(threadNew)){
                        waitSet.add(threadNew);
                    }
                    System.out.printf("失败！线程{%d}被放入waitSet\n",threadNew.getId());
                    return false;
                }else {
                    if (!entryList.contains(threadNew)){
                        entryList.add(threadNew);
                        System.out.printf("失败！线程{%d}被放入entryList\n",threadNew.getId());
                    }
                    return false;
                }
            }
        }
    }

    public static void release(ThreadNew threadNew) throws InterruptedException {
        if (Objects.nonNull(owoner) && owoner==threadNew){
            ObjectMonitor.owoner=null;
            ObjectMonitor.count=0;
            if (entryList.size()!=0){
                entryList.pop().run();
            }
        }
    }


}
