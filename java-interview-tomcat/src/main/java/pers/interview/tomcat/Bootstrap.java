package pers.interview.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.io.IOException;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-07-24 14:19
 **/
public class Bootstrap {

    private static final int PORT = 9939;

    public static void main(String[] args)throws Exception  {
        String hostName = "localhost";
        String contextPath = "";
        String tomcatBaseDir = createTempDir("tomcat", PORT).getAbsolutePath();
        String contextDocBase = createTempDir("tomcat-docBase", PORT).getAbsolutePath();

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(tomcatBaseDir);

        tomcat.setPort(PORT);
        tomcat.setHostname(hostName);
        Context context = tomcat.addWebapp(tomcat.getHost(), contextPath, contextDocBase);

        ClassLoader classLoader = Bootstrap.class.getClassLoader();
        context.setParentClassLoader(classLoader);

        tomcat.start();
        tomcat.getServer().await();

    }

    public static File createTempDir(String prefix, int port) throws IOException {
        File tempDir = File.createTempFile(prefix + ".", "." + port);
        tempDir.delete();
        tempDir.mkdir();
        tempDir.deleteOnExit();
        return tempDir;
    }

}
