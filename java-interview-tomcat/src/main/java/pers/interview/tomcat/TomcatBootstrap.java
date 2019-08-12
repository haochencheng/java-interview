package pers.interview.tomcat;

import com.google.common.base.Strings;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.*;
import java.io.File;
import java.io.IOException;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-07-24 14:19
 **/
public class TomcatBootstrap {

    private static final int PORT = 9939;
    private static final String HOST_NAME = "localhost";


    public static void main(String[] args) throws Exception {
        String hostName = System.getProperty("hostName");
        hostName = Strings.isNullOrEmpty(hostName) ? HOST_NAME : hostName;
        String portStr= System.getProperty("port");
        Integer port=Strings.isNullOrEmpty(portStr)?PORT:Integer.parseInt(portStr);
        System.out.println("hostName="+hostName);
        String contextPath = "";
        String tomcatBaseDir = createTempDir("tomcat", port).getAbsolutePath();
        String contextDocBase = createTempDir("tomcat-docBase", port).getAbsolutePath();

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(tomcatBaseDir);

        tomcat.setPort(port);
        tomcat.setHostname(hostName);
        Context context = tomcat.addWebapp(tomcat.getHost(), contextPath, contextDocBase);
        ClassLoader classLoader = TomcatBootstrap.class.getClassLoader();
        context.setParentClassLoader(classLoader);

        Servlet bbHttpServlet = new GenericServlet() {
            @Override
            public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
                System.out.println(req.getRemoteAddr());
                System.out.println(req.getDispatcherType());
                System.out.println(req.getRemoteHost());
                res.getWriter().println("bb servlet mapping!");
                res.flushBuffer();
            }
        };

        Context context1 = tomcat.addContext("/bb", null);
        Tomcat.addServlet(context1, "bbServlet", bbHttpServlet);
        context1.addServletMappingDecoded("/*", "bbServlet");

        tomcat.start();
        tomcat.getServer().await();
    }

    public static File createTempDir(String prefix, int port) throws IOException {
        File tempDir = File.createTempFile(prefix + ".", "." + port);
        tempDir.delete();
        tempDir.mkdir();
        tempDir.deleteOnExit();
        System.out.println("path:"+tempDir.getAbsolutePath());
        return tempDir;
    }

}
