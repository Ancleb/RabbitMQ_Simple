package rabbitmq.boot;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

/**
 * 时间轮，延迟队列
 * 简易度比DelayQueue简单
 * 延迟任务基于内存，重启数据丢失，内从容易OOM
 * 集群拓展麻烦
 *
 * @author: CoderWater
 * @create: 2022/1/14
 */
public class WheelTimer {
    static volatile boolean flag = true;
    static class MyTimerTask implements TimerTask {
        public void run(Timeout timeout) {
            System.out.println("要去数据库删除订单了。。。。");
            flag = false;
        }
    }
    public static void main(String[] argv) {
        MyTimerTask timerTask = new MyTimerTask();
        Timer timer = new HashedWheelTimer();
        timer.newTimeout(timerTask, 5, TimeUnit.SECONDS);
        int i = 1;
        while(flag) {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(i + "秒过去了");
            i++;
        }
        System.out.println("时间轮线程运行中");
    }
}
