###	TCP/IP

分为以下四层

- 应用层

- 传输层

- 网络层

- 数据链路层

  

1. 应用层决定了向用户提供应用服务时通信的活动。tcp/ip协议族内预存了各类通用的应用服务，比如ftp，dns，http。

2. 传输层对应上层应用层，提供处于网络连接中的两台计算机之间的数据传输。

   在传输层有两个性质不同的协议，tcp（Transmission Control Protocol ，传输控制协议）和UDP（User Data Protocol，用户数据报协议）

   

   ​	UDP：在[TCP/IP](https://zh.wikipedia.org/wiki/TCP/IP)模型中，UDP为[网络层](https://zh.wikipedia.org/wiki/网络层)以上和[应用层](https://zh.wikipedia.org/wiki/应用层)以下提供了一个简单的接口。UDP只提供[数据](https://zh.wikipedia.org/wiki/数据)的不可靠传递，它一旦把应用程序发给网络层的数据发送出去，就不保留数据备份（所以UDP有时候也被认为是不可靠的数据报协议）.

   ​		

3. 网络层用来处理网络上流动的数据包。数据包是网络传输的最小数据单位。

   该层规定了通过怎么样的路径（所谓的传输路线）到达对方的计算机，并把数据包传给对方。

   与对方计算机之间通过多台计算机或网络设备进行传输时，网络层所起的作用就是在众多的选项内选择一条传输路线。

4. 链路层（数据链路层，网络接口层）

   用来处理连接网络的硬件部分。包括操作系统，设备的硬件驱动，NIC（Neetwork Interface Card，网络适配器，即网卡）以及光纤等物理可见部分（还包括连接器等一切传播介质）。硬件上的范畴都在链路层的作用范围之内。

传输图：

![传输图](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/tcp%3Aip/tcp%3Aip传输.png)



####	负责传输的IP协议

IP（Internet Protocol）网际协议位于网络层。ip协议的作用是吧各种数据包传送给对方，要确保正确的传送数据包，有两个重要的条件，IP和MAC地址（Media Access Control Address ）

####	确保可靠性的TCP协议

按层次分，tcp属于传输层，提供可靠地字节流服务。

所谓的字节流服务（Byte Stream Service）是指，为了方便传输，将大块的数据分割成以报文段为单位的数据包进行管理。

而可靠地传输服务是指，能够把数据准确可靠地传输给对方。

**确保数据能够到达 **

TCP采用三次握手（three-way handshaking ）策略，在把数据包送出去后，tcp会向对方确认是否送达成功。

握手过程中使用 TCP的标志 （flag） SYN （synchronize）和 ACK （acknowledgement）。

![三次握手](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/tcp%3Aip/三次握手.png)



####	负载域名解析的DNS服务

DNS（Domain Name System）服务和Http服务协议一样是位于应用层的协议。它提供域名到ip之间的解析服务。

####	各种协议与Http协议的关系

1.A请求 DNS 通过域名获取B的 ip

2.http协议生成请求报文

3.tcp协议方便传输，将http请求报文分割成报文段（按序号分割多个），把每个报文段可靠地传输给对方并接收ACK

4.ip协议职责，搜索对方地址，一边中转一边传送



####	URI URL

URI是Uniform Resource Identifier 的缩写，是某个协议方案表示的资源定位标识符，协议方案是指访问资源所使用的协议类型名称。

采用Http时协议方案就是http，还有ftp等30多种Uri协议方案。

URL标识资源的地点（互联网上所处的位置） 是 URI的子集。

http://www.ietf.org/rfc/a.txt

ftp://ftp.os.za/rfc/a.txt

####	Http协议

http协议用于客户端和服务端之间的通讯，是无状态协议。



