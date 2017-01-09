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
    }
    
    @Test
    public void testLoadStocks() throws Exception
    {
        DataSpider.loadStocks();
    }

    @Test
    public void testCleanStock() throws Exception
    {
        DataSpider.cleanStock();
    }

}
