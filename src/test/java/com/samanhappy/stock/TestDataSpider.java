package com.samanhappy.stock;

import org.junit.Test;

import com.samanhappy.stock.http.DataSpider;

public class TestDataSpider
{

    @Test
    public void testAnalazyData() throws Exception
    {
        DataSpider.analazyData();
    }

    @Test
    public void testRefreshData() throws Exception
    {
        DataSpider.refreshData();
        Thread.sleep(1000 * 60 * 60);
    }
    
    @Test
    public void testClearData() throws Exception
    {
        DataSpider.clearData();
    }
    
    @Test
    public void testClearChartData() throws Exception
    {
        DataSpider.clearChartData();
    }

    @Test
    public void testLoadStocks() throws Exception
    {
        DataSpider.loadStocksInfo();
    }

    @Test
    public void testPrintStocks() throws Exception
    {
        DataSpider.printStocks();
    }

    @Test
    public void testCleanStock() throws Exception
    {
        DataSpider.cleanStock();
    }

}
