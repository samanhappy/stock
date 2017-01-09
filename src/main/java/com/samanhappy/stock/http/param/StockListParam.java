package com.samanhappy.stock.http.param;

import java.util.Calendar;

import com.samanhappy.stock.util.TimeUtil;

public class StockListParam
{
    private String symbol;

    private String period = "1day";

    private String type = "normal";

    private long begin;

    private long end;

    private long _;

    public StockListParam(String symbol)
    {
        super();
        this.symbol = symbol;
        this.end = System.currentTimeMillis();
        this._ = end;
        this.begin = TimeUtil.getAddTs(this.end, Calendar.DATE, -10);
    }

    public String getSymbol()
    {
        return symbol;
    }

    public void setSymbol(String symbol)
    {
        this.symbol = symbol;
    }

    public String getPeriod()
    {
        return period;
    }

    public void setPeriod(String period)
    {
        this.period = period;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public long getBegin()
    {
        return begin;
    }

    public void setBegin(long begin)
    {
        this.begin = begin;
    }

    public long getEnd()
    {
        return end;
    }

    public void setEnd(long end)
    {
        this.end = end;
    }

    public long get_()
    {
        return _;
    }

    public void set_(long _)
    {
        this._ = _;
    }

}
