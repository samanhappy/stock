package com.samanhappy.stock.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.samanhappy.stock.http.DataSpider;
import com.samanhappy.stock.thread.AnalyzeDataThread;
import com.samanhappy.stock.thread.LoadDataThread;
import com.samanhappy.stock.thread.RefreshDataThread;

@Controller
@RequestMapping(value = "/analyze")
public class StockAnalyzeController
{

    @RequestMapping(value = "/refresh", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String refresh()
    {
        new RefreshDataThread().start();
        return "启动刷新数据成功";
    }

    @RequestMapping(value = "/result", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String result()
    {
        return DataSpider.analyzeResult();
    }

    @RequestMapping(value = "/state", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String state()
    {
        return DataSpider.refreshDataState();
    }

    @RequestMapping(value = "/analyze", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String analyze()
    {
        new AnalyzeDataThread().start();
        return "启动刷新分析成功";
    }

    @RequestMapping(value = "/loadData", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String loadData()
    {
        new LoadDataThread().start();
        return "启动加载数据成功";
    }
}
