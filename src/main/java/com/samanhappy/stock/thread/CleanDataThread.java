package com.samanhappy.stock.thread;

import com.samanhappy.stock.http.DataSpider;

public class CleanDataThread extends Thread
{
    @Override
    public void run()
    {
        DataSpider.cleanStock();
    }
}
