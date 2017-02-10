package com.samanhappy.stock.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.samanhappy.stock.domain.AnalyzeResult;
import com.samanhappy.stock.domain.Chart;
import com.samanhappy.stock.domain.Stock;
import com.samanhappy.stock.domain.StockListResp;
import com.samanhappy.stock.domain.StockResult;
import com.samanhappy.stock.http.param.StockInfoParam;
import com.samanhappy.stock.http.param.StockListParam;
import com.samanhappy.stock.storage.RedisClient;
import com.samanhappy.stock.thread.StockChartListExecutor;
import com.samanhappy.stock.thread.StockChartListRunnable;

public class DataSpider
{
    private static final String ANALYZE_RESULT_KEY = "analyze_result";

    private static final String DATA_REFRESH_TIME_KEY = "data_refresh_time";

    private static final String DATA_REFRESH_STATE_KEY = "data_refresh_state";

    private static final String STOCKLIST_KEY = "stocklist";

    private static HttpClientContext context = HttpClientContext.create();

    private static Logger logger = LoggerFactory.getLogger(DataSpider.class);

    private static final String STOCKLIST_URL = "https://xueqiu.com/stock/forchartk/stocklist.json";

    private static final String STOCKINFO_URL = "https://xueqiu.com/v4/stock/quote.json";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static void init() {
        Locale.setDefault(Locale.ENGLISH);
        HttpGet homeGet = new HttpGet("https://xueqiu.com");
        HttpResponse homeResponse;
        try
        {
            homeResponse = HttpClientHelper.getHttpClient().execute(homeGet, context);
            if (homeResponse.getStatusLine().getStatusCode() == 200)
            {
                logger.info("init success");
            }
            else
            {
                logger.error("init error {}", EntityUtils.toString(homeResponse.getEntity()));
            }
            homeResponse.getEntity().getContent().close();
        }
        catch (IOException e)
        {
            logger.error("init error", e);
        }
    }

    public static void refreshData() {
        init();
        Set<String> stocks = RedisClient.hkeys(STOCKLIST_KEY);
        logger.info("start refresh data stock number {}", stocks.size());
        RedisClient.set(DATA_REFRESH_STATE_KEY, dateFormat.format(new Date()) + " 开始刷新数据...");
        StockChartListExecutor executor = new StockChartListExecutor(stocks.size());
        for (String symbol : stocks)
        {
            executor.execute(new StockChartListRunnable(symbol, executor));
        }
        logger.info("end refresh data");
    }

    public static void clearData() {
        Set<String> stocks = RedisClient.hkeys(STOCKLIST_KEY);
        for (String symbol : stocks)
        {
            RedisClient.del(String.format("chartlist_%s", symbol));
            logger.info("clear chart data for {}", symbol);
        }
        logger.info("end clear data");
    }

    public static void clearChartData() {
        Set<String> stocks = RedisClient.keys("chartlist_*");
        for (String stock : stocks)
        {
            RedisClient.del(stock);
            logger.info("clear chart data for {}", stock);
        }
        logger.info("end clear data");
    }

    public static String refreshDataState() {
        return "<h1>" + RedisClient.get(DATA_REFRESH_STATE_KEY) + "</h1>";
    }

    public static String analyzeResult() {
        String result = RedisClient.get(ANALYZE_RESULT_KEY);
        if (StringUtils.isNotBlank(result))
        {
            AnalyzeResult res = JSONObject.parseObject(result, AnalyzeResult.class);
            StringBuilder sb = new StringBuilder();
            sb.append("<h1>");
            sb.append("本次分析结果状态：").append(res.getState()).append("<br/>");
            sb.append("分析时间：").append(res.getAnalyzeTime()).append("<br/>");
            sb.append("数据更新时间：").append(res.getDataRefreshTime()).append("<br/>");
            sb.append("筛选股票数据：").append("<br/>").append(res.getResult());
            sb.append("</h1>");
            return sb.toString();
        }
        return "不存在分析结果";
    }

    public static List<StockResult> analazyData() {
        List<StockResult> results = new ArrayList<StockResult>();
        Set<String> stocks = RedisClient.hkeys(STOCKLIST_KEY);
        Date now = new Date();
        for (String symbol : stocks)
        {
            if (symbol.startsWith("SZ1") || symbol.startsWith("SH2"))
            {
                continue;
            }

            if (!RedisClient.exists(String.format("chartlist_%s", symbol)))
            {
                // getStockChartListBySymbol(symbol);
                // logger.info("cannot find stock {}", symbol);
                continue;
            }

            List<String> charts = RedisClient.lrange(String.format("chartlist_%s", symbol), 0, 10);
            if (charts.size() >= 3)
            {
                Chart today = JSONObject.parseObject(charts.get(0), Chart.class);
                Chart yesterday = JSONObject.parseObject(charts.get(1), Chart.class);
                Chart lastday = JSONObject.parseObject(charts.get(2), Chart.class);

                // 昨天上涨今天下跌
                if (yesterday.getPercent() > 7 && today.getPercent() < -1)
                {
                    float percent = (float) today.getVolume() / yesterday.getVolume();
                    // 今天相比昨天缩量
                    if (today.getVolume() < yesterday.getVolume() && percent < 0.7)
                    {
                        String stockInfo = RedisClient.hget(STOCKLIST_KEY, symbol);
                        Stock stock = JSONObject.parseObject(stockInfo, Stock.class);
                        results.add(new StockResult(symbol, stock.getName(), percent, 1, yesterday.getPercent(), today
                                .getPercent()));
                        // logger.info("昨天上涨今天下跌缩量股票 {} 缩量比{} {} {} {} {}", symbol, percent, yesterday.getPercent(),
                        //        today.getPercent(), yesterday.getVolume(), today.getVolume());
                    }
                }

                // 前天上涨今天下跌
                if (lastday.getPercent() > 7 && today.getPercent() < -1)
                {
                    float percent = (float) today.getVolume() / lastday.getVolume();
                    // 今天相比前天缩量
                    if (today.getVolume() < lastday.getVolume() && percent < 0.8)
                    {
                        String stockInfo = RedisClient.hget(STOCKLIST_KEY, symbol);
                        Stock stock = JSONObject.parseObject(stockInfo, Stock.class);
                        results.add(new StockResult(symbol, stock.getName(), percent, 2, lastday.getPercent(), today
                                .getPercent()));
                        // logger.info("前天上涨今天下跌缩量股票 {} 缩量比 {} {} {} {} {}", symbol, percent, lastday.getPercent(),
                        //        today.getPercent(), lastday.getVolume(), today.getVolume());
                    }
                }

                // 放量上涨
                /*if (today.getPercent() > 3 && today.getVolume() > yesterday.getVolume())
                {
                    float percent = (float) today.getVolume() / yesterday.getVolume();
                    if (percent > 2)
                    {
                        String stockInfo = RedisClient.hget(STOCKLIST_KEY, symbol);
                        Stock stock = JSONObject.parseObject(stockInfo, Stock.class);
                        results.add(new StockResult(symbol, stock.getName(), percent, 4, yesterday.getPercent(), today
                                .getPercent()));
                    }
                }*/

                // 近三天没有数据的不处理
                if ((now.getTime() - today.getTime().getTime()) > 1000 * 3600 * 24 * 4)
                {
                    continue;
                }

                // 下跌下影线
                if (today.getPercent() < 0 && (today.getHigh() - today.getLow()) / today.getLow() > 0.06)
                {
                    // 以开盘收盘低点来计算
                    float val = today.getClose() < today.getOpen() ? today.getClose() : today.getOpen();
                    float percent = (float) (val - today.getLow()) / (today.getHigh() - today.getLow());
                    if (percent > 0.35)
                    {
                        String stockInfo = RedisClient.hget(STOCKLIST_KEY, symbol);
                        Stock stock = JSONObject.parseObject(stockInfo, Stock.class);
                        results.add(new StockResult(symbol, stock.getName(), percent, 3, lastday.getPercent(), today
                                .getPercent()));
                    }
                }
            }
        }
        Collections.sort(results);

        logger.info("find {} results", results.size());
        StringBuilder sb = new StringBuilder();
        for (StockResult result : results)
        {
            System.out.println(result);
            sb.append(result).append("<br/>");
        }
        AnalyzeResult analResult = new AnalyzeResult();
        analResult.setResult(results.size() > 0 ? sb.toString() : "没有符合条件的股票");
        analResult.setState("成功");
        analResult.setDataRefreshTime(RedisClient.get(DATA_REFRESH_TIME_KEY));
        analResult.setAnalyzeTime(dateFormat.format(now));
        RedisClient.set(ANALYZE_RESULT_KEY, JSONObject.toJSONString(analResult));
        return results;
    }

    public static void cleanStock() {
        Set<String> stocks = RedisClient.hkeys(STOCKLIST_KEY);
        for (String symbol : stocks)
        {
            if (symbol.startsWith("SZ1") || symbol.startsWith("SH2") || symbol.startsWith("SH5"))
            {
                RedisClient.hdel(STOCKLIST_KEY, symbol);
                logger.info("clean stock {}", symbol);
            }
        }
    }

    public static void printStocks() {
        Set<String> stocks = RedisClient.hkeys(STOCKLIST_KEY);
        for (String symbol : stocks)
        {
            System.out.println(symbol + ";" + RedisClient.hget(STOCKLIST_KEY, symbol));
        }
    }

    public static void loadStocksInfo() {
        init();
        try
        {
            BufferedReader br = new BufferedReader(new FileReader("src/main/resources/stocks.txt"));
            String symbol = br.readLine();
            while (symbol != null)
            {
                addStockBySymbol(symbol.toUpperCase());
                symbol = br.readLine();
            }
            br.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void loadStocksFromFile() {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(new File(DataSpider.class.getClassLoader()
                    .getResource("stockjson.txt").getFile())));
            String symbol = br.readLine();
            while (symbol != null)
            {
                String[] strs = symbol.split(";");
                RedisClient.hset(STOCKLIST_KEY, strs[0], strs[1]);
                symbol = br.readLine();
            }
            br.close();
            logger.info("loadStocks success");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void getStockChartListBySymbol(String symbol) {
        HttpGet get = new HttpGet(STOCKLIST_URL + generateUrlParams(new StockListParam(symbol)));
        HttpResponse response;
        try
        {
            response = HttpClientHelper.getHttpClient().execute(get, context);
            if (response.getStatusLine().getStatusCode() == 200)
            {
                StockListResp resp = JSONObject.parseObject(EntityUtils.toString(response.getEntity()),
                        StockListResp.class);
                if ("true".equals(resp.getSuccess()))
                {
                    for (Chart chart : resp.getChartlist())
                    {
                        RedisClient.lpush(String.format("chartlist_%s", symbol), JSONObject.toJSONString(chart));
                    }
                    logger.info("stock chartlist success for {}", symbol);
                }
                else
                {
                    logger.error("getStockListBySymbol error {}", EntityUtils.toString(response.getEntity()));
                }
            }
            else
            {
                logger.error("getStockListBySymbol error {}", EntityUtils.toString(response.getEntity()));
            }
            response.getEntity().getContent().close();
        }
        catch (IOException e)
        {
            logger.error("getStockListBySymbol error", e);
        }
    }

    public static void addStockBySymbol(String... symbols) {
        for (String symbol : symbols)
        {
            if (!RedisClient.hexists(STOCKLIST_KEY, symbol))
            {
                HttpGet get = new HttpGet(STOCKINFO_URL + generateUrlParams(new StockInfoParam(symbol)));
                HttpResponse response;
                try
                {
                    response = HttpClientHelper.getHttpClient().execute(get, context);
                    if (response.getStatusLine().getStatusCode() == 200)
                    {
                        JSONObject json = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
                        if (json.containsKey(symbol))
                        {
                            RedisClient.hset(STOCKLIST_KEY, symbol, json.getString(symbol));
                            logger.info("add stock success for {}", symbol);
                        }
                    }
                    else
                    {
                        logger.error("getStockInfoBySymbol error {}", EntityUtils.toString(response.getEntity()));
                    }
                    response.getEntity().getContent().close();
                }
                catch (IOException e)
                {
                    logger.error("getStockInfoBySymbol error", e);
                }
            }
        }
    }

    public static HttpEntity generateEntity(Object param) throws UnsupportedEncodingException,
            IllegalArgumentException, IllegalAccessException
    {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        for (Field field : param.getClass().getDeclaredFields())
        {
            field.setAccessible(true);
            Object val = field.get(param);
            if (val != null)
            {
                NameValuePair nameValue = new BasicNameValuePair(field.getName(), String.valueOf(val));
                parameters.add(nameValue);
            }
        }
        return new UrlEncodedFormEntity(parameters, "UTF-8");
    }

    public static String generateUrlParams(Object param) {
        StringBuilder parameters = new StringBuilder("?");
        Field[] fields = param.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];
            field.setAccessible(true);
            Object val;
            try
            {
                val = field.get(param);
                if (val != null)
                {
                    parameters.append(field.getName()).append('=').append(val).append('&');
                }
            }
            catch (Exception e)
            {
                logger.error("failed to get field val", e);
            }
        }
        return parameters.toString();
    }

}
