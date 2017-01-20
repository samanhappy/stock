package com.samanhappy.stock.thread;

import com.samanhappy.stock.http.DataSpider;

public class StockChartListRunnable implements Runnable
{
    String symbol;

    StockChartListExecutor executor;

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
    }
}
