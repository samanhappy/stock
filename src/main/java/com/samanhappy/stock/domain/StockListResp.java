package com.samanhappy.stock.domain;

import java.util.List;

public class StockListResp
{
    private String success;

    private Stock stock;

    private List<Chart> chartlist;

    public String getSuccess()
    {
        return success;
    }

    public void setSuccess(String success)
    {
        this.success = success;
    }

    public Stock getStock()
    {
        return stock;
    }

    public void setStock(Stock stock)
    {
        this.stock = stock;
    }

    public List<Chart> getChartlist()
    {
        return chartlist;
    }

    public void setChartlist(List<Chart> chartlist)
    {
        this.chartlist = chartlist;
    }

}
