package boot.rabbitmq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootTest
class RabbitmqConfirmReturnApplicationTests {

    @Test
    void contextLoads() {
    }


    @Test
    void linkedHashMap(){

    }

    // public static void main(String[] args) {
    //     LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<>(5, 0.75F, true){
    //         @Override
    //         protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
    //             return size() > 5;
    //         }
    //     };
    //     linkedHashMap.put("1", 1);
    //     linkedHashMap.put("2", 2);
    //     linkedHashMap.put("3", 3);
    //     linkedHashMap.put("4", 4);
    //     linkedHashMap.put("5", 5);
    //     linkedHashMap.get("1");
    //     linkedHashMap.put("6", 6);
    //     System.out.println(linkedHashMap.keySet());
    // }\



    static int num = 0;
    static volatile boolean flag = false;

    public static void main(String[] args) {

        Thread t1 = new Thread(() -> {
            for (; 100 > num; ) {
                if (!flag && (num == 0 || ++num % 2 == 0)) {
                    System.out.println(num);
                    flag = true;
                }
            }
        }
        );

        Thread t2 = new Thread(() -> {
            for (; 100 > num; ) {
                if (flag && (++num % 2 != 0)) {
                    System.out.println(num);
                    flag = false;
                }
            }
        }
        );

        t1.start();
        t2.start();
    }
}
