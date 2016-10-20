package test;

import java.util.HashMap;
import java.util.Map;

/**
 * User: shimingliang
 * Date: 16-9-23
 * Time: 上午10:25
 */
public class Test {
    private static Map<String,String> map = new HashMap<String, String>();
    public static void main(String[] args) {

       map.put("1","sml");
       map.put("2","sml");

        System.out.println(map.get("1"));
    }
}
