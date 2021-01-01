package pers.interview.ribbon.common;

import lombok.Data;
import lombok.ToString;

/**
 * Class that represents a typical Server
 * @description:
 * @author: haochencheng
 * @create: 2019-09-11 18:10
 **/
@Data
@ToString
public class Server {

    private String host;
    private int port = 80;
    private volatile String id;

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
        this.id = host + ":" + port;
    }

}
