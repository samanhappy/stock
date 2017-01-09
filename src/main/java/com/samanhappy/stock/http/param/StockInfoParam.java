package com.samanhappy.stock.http.param;

public class StockInfoParam
{

    private String code;

    private long _;
    
    public StockInfoParam(String code)
    {
        super();
        this.code = code;
        this._ = System.currentTimeMillis();
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
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
