### jmap
查看java 内存信息命令
 ```
Usage:
    jmap [option] <pid>
        (to connect to running process)
    jmap [option] <executable <core>
        (to connect to a core file)
    jmap [option] [server_id@]<remote server IP or hostname>
        (to connect to remote debug server)


  <none>               to print same info as Solaris pmap
    -heap                to print java heap summary
    -histo[:live]        to print histogram of java object heap; if the "live"
                         suboption is specified, only count live objects
    -clstats             to print class loader statistics
    -finalizerinfo       to print information on objects awaiting finalization
    -dump:<dump-options> to dump java heap in hprof binary format
                         dump-options:
                           live         dump only live objects; if not specified,
                                        all objects in the heap are dumped.
                           format=b     binary format
                           file=<file>  dump heap to <file>
                         Example: jmap -dump:live,format=b,file=heap.bin <pid>
    -F                   force. Use with -dump:<dump-options> <pid> or -histo
                         to force a heap dump or histogram when <pid> does not
                         respond. The "live" suboption is not supported
                         in this mode.
    -h | -help           to print this help message
    -J<flag>             to pass <flag> directly to the runtime system
 ```

####    jmap -heap ${pid}

```
Attaching to process ID 68032, please wait...
Debugger attached successfully.
Server compiler detected.
JVM version is 25.231-b11

using thread-local object allocation.
# 垃圾回收器  Parallel 只对青年代有用。老代仍旧使用串行收集。 
Parallel GC with 8 thread(s)

Heap Configuration:
   MinHeapFreeRatio         = 0
   MaxHeapFreeRatio         = 100
   MaxHeapSize              = 4269801472 (4072.0MB)
   NewSize                  = 89128960 (85.0MB)
   MaxNewSize               = 1422917632 (1357.0MB)
   OldSize                  = 179306496 (171.0MB)
   NewRatio                 = 2
   SurvivorRatio            = 8
   MetaspaceSize            = 21807104 (20.796875MB)
   CompressedClassSpaceSize = 1073741824 (1024.0MB)
   MaxMetaspaceSize         = 17592186044415 MB
   G1HeapRegionSize         = 0 (0.0MB)

Heap Usage:
PS Young Generation
Eden Space:
   capacity = 863502336 (823.5MB)
   used     = 725121496 (691.5297470092773MB)
   free     = 138380840 (131.97025299072266MB)
   83.97446836785396% used
From Space:
   capacity = 21495808 (20.5MB)
   used     = 1802240 (1.71875MB)
   free     = 19693568 (18.78125MB)
   8.384146341463415% used
To Space:
   capacity = 21495808 (20.5MB)
   used     = 0 (0.0MB)
   free     = 21495808 (20.5MB)
   0.0% used
PS Old Generation
   capacity = 128450560 (122.5MB)
   used     = 41170320 (39.26307678222656MB)
   free     = 87280240 (83.23692321777344MB)
   32.05149125079719% used

26743 interned Strings occupying 3163832 bytes.

```