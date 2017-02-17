package com.samanhappy.stock.domain;

public class StockResult implements Comparable<StockResult>
{
    private String symbol;

    private String name;

    // 缩量比
    private float volumeRatio;

    // 选中策略，1昨天上涨今日下跌缩量，2前天上涨今日下跌缩量，3下影线，4今日放量上涨, 5碎阳
    private int strategy;

    private float firstPercent;

    private float secondPercent;

    public StockResult(String symbol, String name, float volumeRatio, int strategy, float firstPercent,
            float secondPercent)
    {
        super();
        this.symbol = symbol;
        this.name = name;
        this.volumeRatio = volumeRatio;
        this.strategy = strategy;
        this.firstPercent = firstPercent;
        this.secondPercent = secondPercent;
    }

    @Override
    public int compareTo(StockResult o) {
        if (o != null)
        {
            if (this.strategy == o.getStrategy())
            {
                if (strategy == 3 || strategy == 4 || strategy == 5)
                {
                    if (this.volumeRatio <= o.getVolumeRatio())
                    {
                        return 1;
                    }
                    else
                    {
                        return -1;
                    }
                }
                else
                {
                    if (this.volumeRatio <= o.getVolumeRatio())
                    {
                        return -1;
                    }
                    else
                    {
                        return 1;
                    }
                }
            }
            else if (this.strategy < o.getStrategy())
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (strategy == 1)
        {
            sb.append("ZZJD");
        }
        else if (strategy == 2)
        {
            sb.append("QZJD");
        }
        else if (strategy == 3)
        {
            sb.append("XYX");
        }
        else if (strategy == 4)
        {
            sb.append("FLSZ");
        }
        else if (strategy == 5)
        {
            sb.append("SYZ");
        }
        sb.append(' ');
        sb.append(symbol).append(' ');
        sb.append(name).append(' ');
        sb.append(volumeRatio).append(' ');
        sb.append(firstPercent).append(' ');
        sb.append(secondPercent).append(' ');
        return sb.toString();
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getVolumeRatio() {
        return volumeRatio;
    }

    public void setVolumeRatio(float volumeRatio) {
        this.volumeRatio = volumeRatio;
    }

    public int getStrategy() {
        return strategy;
    }

    public void setStrategy(int strategy) {
        this.strategy = strategy;
    }

    public float getFirstPercent() {
        return firstPercent;
    }

    public void setFirstPercent(float firstPercent) {
        this.firstPercent = firstPercent;
    }

    public float getSecondPercent() {
        return secondPercent;
    }

    public void setSecondPercent(float secondPercent) {
        this.secondPercent = secondPercent;
    }
}
