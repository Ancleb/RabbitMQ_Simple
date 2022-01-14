package rabbitmq.boot;

import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JDK自带的延迟队列
 *
 * 优点： 效率高，任务触发延迟低
 * 缺点： 默认存储在内存中，集群拓展麻烦，内存有限制容易OOM。
 *
 *
 * @author: CoderWater
 * @create: 2022/1/14
 */
public class DelayQueueTest {
    public static void main(String[] args) {
        List<String> list = Stream.iterate("0000" + "1", s -> "0000" + (Integer.parseInt(s) + 1)).limit(6).collect(Collectors.toList());

        DelayQueue<OrderDelay> queue = new DelayQueue<>();

        long start = System.currentTimeMillis();
        for(int i = 0; i < 5; i++) {
            //延迟三秒取出
            queue.put(new OrderDelay(list.get(i), TimeUnit.NANOSECONDS.convert(3, TimeUnit.SECONDS)));
            try {
                queue.take().print();
                System.out.println("After " + (System.currentTimeMillis() - start) + " MilliSeconds");
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


class OrderDelay implements Delayed {

    // 订单ID
    private String orderId;

    // 延迟时间
    private long timeout;

    OrderDelay(String orderId, long timeout) {
        this.orderId = orderId;

        this.timeout = timeout + System.nanoTime();
    }

    /**
     * 返回距离你自定义的超时时间还有多少
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(timeout - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    public int compareTo(Delayed other) {
        if (other == this) {
            return 0;
        }

        long d = (getDelay(TimeUnit.NANOSECONDS) -
                other.getDelay(TimeUnit.NANOSECONDS));

        return (d == 0) ? 0 : ((d < 0) ? (-1) : 1);
    }

    void print() {
        System.out.println(orderId + "编号的订单要删除啦。。。。");
    }
}