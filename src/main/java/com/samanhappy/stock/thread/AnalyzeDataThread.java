package com.samanhappy.stock.thread;

import com.samanhappy.stock.http.DataSpider;

public class AnalyzeDataThread extends Thread
{
    @Override
    public void run()
    {
        DataSpider.analazyData();
    }
}
