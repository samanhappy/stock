package com.samanhappy.stock.thread;

import com.samanhappy.stock.http.DataSpider;

public class LoadDataThread extends Thread
{
    @Override
    public void run()
    {
        DataSpider.loadStocksFromFile();
    }
}
