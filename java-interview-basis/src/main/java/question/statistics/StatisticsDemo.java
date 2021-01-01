package question.statistics;


import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: haochencheng
 * @create: 2020-02-18 11:35
 **/
public class StatisticsDemo {

    public static void main(String[] args) throws IOException {
        List<AccessLog> accessLogList = readResource();
        statistics(accessLogList);
    }

    /**
     * 根据路径ip去重，统计路径访问数量前5
     * @param accessLogList
     */
    private static void statistics(List<AccessLog> accessLogList) {
        HashMap<String, String> hashMap = new HashMap(accessLogList.size());
        accessLogList.forEach(accessLog -> hashMap.put(accessLog.getPath() + "-" + accessLog.getIp(), accessLog.getPath()));
        HashMap<String, Integer> resultMap = new HashMap(hashMap.size());
        hashMap.values().forEach(path -> resultMap.compute(path, (k, v)->{
            if (Objects.isNull(v)){
                v=1;
            }else {
                v++;
            }
            return v;
        }));
        ArrayList<Map.Entry<String, Integer>> arrayList = new ArrayList<>(resultMap.entrySet());
        Collections.sort(arrayList, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        List<Map.Entry<String, Integer>> collect = arrayList.stream().limit(5).collect(Collectors.toList());
        collect.forEach(v-> System.out.println(v.getKey()+"\t"+v.getValue()));
    }

    /**
     * 读取文件到集合
     *
     * @return
     * @throws IOException
     */
    private static List<AccessLog> readResource() throws IOException {
        List<AccessLog> accessLogList = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream("/Users/haochencheng/Workspace/interview/java-interview/java-interview-basis/src/main/resources/access.log");
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                String[] array = line.split("\t");
                System.out.println(Arrays.toString(array));
                try {
                    String accessTimeStr = array[0];
                    String path = array[1];
                    String ip = array[2];
                    LocalDateTime accessTime = LocalDateTime.parse(accessTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    AccessLog accessLog = new AccessLog(accessTime, path, ip);
                    System.out.println(accessLog.toString());
                    accessLogList.add(accessLog);
                } catch (ArrayIndexOutOfBoundsException ae) {
                    //ignore
                } catch (NullPointerException ne) {
                    //ignore
                }
            }
        }
        return accessLogList;
    }

    static class AccessLog {

        /**
         * 访问时间
         */
        private LocalDateTime accessTime;

        /**
         * 访问路径
         */
        private String path;

        /**
         * 访问ip
         */
        private String ip;

        public AccessLog(LocalDateTime accessTime, String path, String ip) {
            this.accessTime = accessTime;
            this.path = path;
            this.ip = ip;
        }

        public LocalDateTime getAccessTime() {
            return accessTime;
        }

        public void setAccessTime(LocalDateTime accessTime) {
            this.accessTime = accessTime;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }
    }

}
