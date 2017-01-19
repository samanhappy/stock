package com.samanhappy.stock.domain;


public class AnalyzeResult
{
    private String dataRefreshTime;

    private String analyzeTime;

    private String state;

    private String result;

    public String getDataRefreshTime()
    {
        return dataRefreshTime;
    }

    public void setDataRefreshTime(String dataRefreshTime)
    {
        this.dataRefreshTime = dataRefreshTime;
    }

    public String getAnalyzeTime()
    {
        return analyzeTime;
    }

    public void setAnalyzeTime(String analyzeTime)
    {
        this.analyzeTime = analyzeTime;
    }

    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

}
