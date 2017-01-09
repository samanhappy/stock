package com.samanhappy.stock.domain;

public class Stock
{
    private String symbol;

    private String name;

    private float current;

    private float percentage;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public float getCurrent()
    {
        return current;
    }

    public void setCurrent(float current)
    {
        this.current = current;
    }

    public float getPercentage()
    {
        return percentage;
    }

    public void setPercentage(float percentage)
    {
        this.percentage = percentage;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public void setSymbol(String symbol)
    {
        this.symbol = symbol;
    }

}
