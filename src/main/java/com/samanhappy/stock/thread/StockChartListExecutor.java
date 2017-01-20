package com.samanhappy.stock.thread;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samanhappy.stock.storage.RedisClient;

public class StockChartListExecutor
{
    private static Logger logger = LoggerFactory.getLogger(StockChartListExecutor.class);

    private static final String DATA_REFRESH_TIME_KEY = "data_refresh_time";

    private static final String DATA_REFRESH_STATE_KEY = "data_refresh_state";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int count;

    public StockChartListExecutor(int count)
    {
        this.count = count;
    }

    public synchronized void completeOne()
    {
        this.count--;
        if (count == 0)
        {
            RedisClient.set(DATA_REFRESH_TIME_KEY, dateFormat.format(new Date()));
            RedisClient.set(DATA_REFRESH_STATE_KEY, dateFormat.format(new Date()) + " 分析完成！！！");
            logger.info("all symbol handle complete");
        }
    }

    static ExecutorService cachedThreadPool = Executors.newFixedThreadPool(20);

    public void execute(StockChartListRunnable runnable)
    {
        cachedThreadPool.execute(runnable);
    }
}
