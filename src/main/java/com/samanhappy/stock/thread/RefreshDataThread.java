package com.samanhappy.stock.thread;

import com.samanhappy.stock.http.DataSpider;

public class RefreshDataThread extends Thread
{
    @Override
    public void run()
    {
        DataSpider.refreshData();
    }
}
