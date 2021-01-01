### jmeter
query-http-api 

https://www.jianshu.com/p/20fac18f478f

1.安装
2.修改语言为中文
3.创建线程组
4.创建http请求
5.填写接口参数
6.设置header
    Content-Type: application/json; charset=UTF-8
7.添加断言(响应断言[Response Assertion])
8 添加监听器（察看结果树）


{
    "sessionId":"",
    "sessionId":"",
    "originQuery":"",
    "trimmedQuery":"",
    "removedStopWordQuery":"",
    "stemmedQuery":"",
    "country":""
}

https://p1htmlkernalweb.mybluemix.net/articles/IDEA%E7%9A%84Http+Client%E4%BD%BF%E7%94%A8%E6%95%99%E7%A8%8B_4111266_csdn.html


### jmeter sign请求方法
https://www.cnblogs.com/qiaoyeye/p/6953099.html


http://testapi.qury.me/open/v1/search?channelId=d98ccb88200744d68cc7b064a0c060c1&channelVersion=0.0.1&encodeData=eyJxdWVyeSI6InRheWxvciBzd2lmdCIsInBhZ2VOdW0iOjB9&timestamp=1595850567730&userId=0b77ee80-f917-421e-8b96-ff4baec260bD&sign=e0324705d518c1ee878938a79bf9272c


// md5 sign
import org.apache.commons.codec.digest.DigestUtils; 
import org.apache.jmeter.config.*; 

// 获取入参
Arguments args = sampler.getArguments(); // 截获请求，包含url、headers 和 body 三部分
Argument arg_body = args.getArgument(0); // 获取请求body
String body = arg_body.getValue();  // 获取body的值保存成字符串
log.info("=======================body================"+body);  // 打印下看看，跑自动化\性能时把log注释掉
// 解析参数


// 签名
String path = "testapi.qury.me/open/v1/search?";
String timestamp = String.valueOf(System.currentTimeMillis());
//将时间戳赋值给ts变量，方便以 ${timestamp} 的方式引用
vars.put("timestamp",timestamp);
//此处的SPhone的值可以用csv参数化
String data = "{\"SPhone\":\"18662255783\",\"EType\":0}";
String key = "a323f9b6-1f04-420e-adb9-b06ty67b0e63";
String bsign = "z417App" + timestamp + data + key;
//MD5加密赋值给sign变量
vars.put("sign",DigestUtils.md5Hex(bsign));

// 将新body替换到取样器的参数中
// 将sign 添加到路径后
arg_body.setValue(body); 