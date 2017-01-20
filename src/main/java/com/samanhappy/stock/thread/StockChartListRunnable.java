package com.samanhappy.stock.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samanhappy.stock.http.DataSpider;

public class StockChartListRunnable implements Runnable
{
    String symbol;

    StockChartListExecutor executor;

    private static Logger logger = LoggerFactory.getLogger(StockChartListRunnable.class);

    public StockChartListRunnable(String symbol, StockChartListExecutor executor)
    {
        this.symbol = symbol;
        this.executor = executor;
    }

    @Override
    public void run()
    {
        DataSpider.getStockChartListBySymbol(symbol);
        executor.completeOne();
        logger.info("stock {} handle complete", symbol);
    }
}
