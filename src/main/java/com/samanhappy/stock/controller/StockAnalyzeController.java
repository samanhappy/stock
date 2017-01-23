package com.samanhappy.stock.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.samanhappy.stock.http.DataSpider;
import com.samanhappy.stock.thread.AnalyzeDataThread;
import com.samanhappy.stock.thread.CleanDataThread;
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
        return "<h1>启动刷新数据成功</h1>";
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
        return "<h1>启动刷新分析成功</h1>";
    }

    @RequestMapping(value = "/clean", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String clean()
    {
        new CleanDataThread().start();
        return "<h1>启动清理成功</h1>";
    }

    @RequestMapping(value = "/loadData", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String loadData()
    {
        new LoadDataThread().start();
        return "<h1>启动加载数据成功</h1>";
    }
}
