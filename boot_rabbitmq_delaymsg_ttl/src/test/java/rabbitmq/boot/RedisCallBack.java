package rabbitmq.boot;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

/**
 * redis基于发布订阅的回调
 * 在redis.conf中，加入一条配置 notify-keyspace-events Ex
 *  官方说明：Redis的发布/订阅目前是即发即弃(fire and forget)模式的，因此无法实现事件的可靠通知。也就是说，如果发布/订阅的客户端断链之后又重连，则在客户端断链期间的所有事件都丢失了。
 *
 *  客户端离线状态消息丢失。
 *  sub订阅redis的线程会被阻塞，等待redis回调通知，所以需要单独开启一个线程一直等待。
 *
 * @author: CoderWater
 * @create: 2022/1/14
 */
public class RedisCallBack {
    private static final String ADDR = "127.0.0.1";
    private static final int PORT = 6379;
    private static JedisPool jedis = new JedisPool(ADDR, PORT);
    private static RedisSub sub = new RedisSub();

    public static void init() {
        new Thread(() -> jedis.getResource().subscribe(sub, "__keyevent@0__:expired")).start();
    }

    public static void main(String[] args) throws InterruptedException {
        init();
        for (int i = 0; i < 10; i++) {
            String orderId = "OID000000" + i;
            jedis.getResource().setex(orderId, 3, orderId);
            System.out.println(System.currentTimeMillis() + "ms:" + orderId + "订单生成");
        }
    }

    static class RedisSub extends JedisPubSub {
        public void onMessage(String channel, String message) {
            System.out.println(System.currentTimeMillis() + "ms:" + message + "订单取消");
        }
    }
}