package com.samanhappy.stock.http;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.samanhappy.stock.domain.AnalazyResult;
import com.samanhappy.stock.domain.Chart;
import com.samanhappy.stock.domain.Stock;
import com.samanhappy.stock.domain.StockListResp;
import com.samanhappy.stock.http.param.StockInfoParam;
import com.samanhappy.stock.http.param.StockListParam;
import com.samanhappy.stock.storage.RedisSupport;

public class DataSpider
{
    private static final String STOCKLIST_KEY = "stocklist";

    private static CloseableHttpClient client = HttpClientBuilder.create().build();

    private static HttpClientContext context = HttpClientContext.create();

    private static Logger logger = LoggerFactory.getLogger(DataSpider.class);

    private static boolean usable = false;

    private static final String STOCKLIST_URL = "https://xueqiu.com/stock/forchartk/stocklist.json";

    private static final String STOCKINFO_URL = "https://xueqiu.com/v4/stock/quote.json";

    static
    {
        if (!usable)
        {
            init();
        }
    }

    private static void init()
    {
        Locale.setDefault(Locale.ENGLISH);
        HttpGet homeGet = new HttpGet("https://xueqiu.com");
        HttpResponse homeResponse;
        try
        {
            homeResponse = client.execute(homeGet, context);
            if (homeResponse.getStatusLine().getStatusCode() == 200)
            {
                logger.info("init success");
                usable = true;
            }
            else
            {
                logger.error("init error {}", EntityUtils.toString(homeResponse.getEntity()));
            }
        }
        catch (IOException e)
        {
            logger.error("init error", e);
        }
    }

    public static void refreshData()
    {
        Set<String> stocks = RedisSupport.getJedis().hkeys(STOCKLIST_KEY);
        logger.info("stock number {}", stocks.size());
        for (String symbol : stocks)
        {
            getStockChartListBySymbol(symbol);
        }
    }

    public static void analazyData()
    {
        List<AnalazyResult> results = new ArrayList<AnalazyResult>();
        Set<String> stocks = RedisSupport.getJedis().hkeys(STOCKLIST_KEY);
        for (String symbol : stocks)
        {
            if (symbol.startsWith("SZ1") || symbol.startsWith("SH2"))
            {
                continue;
            }

            if (!RedisSupport.getJedis().exists(String.format("chartlist_%s", symbol)))
            {
                // getStockChartListBySymbol(symbol);
                logger.info("cannot find stock {}", symbol);
                continue;
            }

            List<String> charts = RedisSupport.getJedis().lrange(String.format("chartlist_%s", symbol), 0, 10);
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
                        String stockInfo = RedisSupport.getJedis().hget(STOCKLIST_KEY, symbol);
                        Stock stock = JSONObject.parseObject(stockInfo, Stock.class);
                        results.add(new AnalazyResult(symbol, stock.getName(), percent, 1, yesterday.getPercent(),
                                today.getPercent()));
                        // logger.info("昨天上涨今天下跌缩量股票 {} 缩量比{} {} {} {} {}", symbol, percent, yesterday.getPercent(),
                        //        today.getPercent(), yesterday.getVolume(), today.getVolume());
                    }
                }

                // 前天上涨今天下跌
                if (lastday.getPercent() > 7 && today.getPercent() < -1)
                {
                    float percent = (float) today.getVolume() / lastday.getVolume();
                    // 今天相比前天缩量
                    if (today.getVolume() < lastday.getVolume() && percent < 0.7)
                    {
                        String stockInfo = RedisSupport.getJedis().hget(STOCKLIST_KEY, symbol);
                        Stock stock = JSONObject.parseObject(stockInfo, Stock.class);
                        results.add(new AnalazyResult(symbol, stock.getName(), percent, 2, lastday.getPercent(), today
                                .getPercent()));
                        // logger.info("前天上涨今天下跌缩量股票 {} 缩量比 {} {} {} {} {}", symbol, percent, lastday.getPercent(),
                        //        today.getPercent(), lastday.getVolume(), today.getVolume());
                    }
                }
            }
        }
        Collections.sort(results);

        for (AnalazyResult result : results)
        {
            System.out.println(result);
        }
    }

    public static void cleanStock()
    {
        Set<String> stocks = RedisSupport.getJedis().hkeys(STOCKLIST_KEY);
        for (String symbol : stocks)
        {
            if (symbol.startsWith("SZ1") || symbol.startsWith("SH2"))
            {
                RedisSupport.getJedis().hdel(STOCKLIST_KEY, symbol);
                logger.info("clean stock {}", symbol);
            }
        }
    }

    public static void loadStocks()
    {
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

    private static void getStockChartListBySymbol(String symbol)
    {
        HttpGet get = new HttpGet(STOCKLIST_URL + generateUrlParams(new StockListParam(symbol)));
        HttpResponse response;
        try
        {
            response = client.execute(get, context);
            if (response.getStatusLine().getStatusCode() == 200)
            {
                StockListResp resp = JSONObject.parseObject(EntityUtils.toString(response.getEntity()),
                        StockListResp.class);
                if ("true".equals(resp.getSuccess()))
                {
                    for (Chart chart : resp.getChartlist())
                    {
                        RedisSupport.getJedis().lpush(String.format("chartlist_%s", symbol),
                                JSONObject.toJSONString(chart));
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
        }
        catch (IOException e)
        {
            logger.error("getStockListBySymbol error", e);
        }
    }

    public static void addStockBySymbol(String... symbols)
    {
        for (String symbol : symbols)
        {
            if (!RedisSupport.getJedis().hexists(STOCKLIST_KEY, symbol))
            {
                HttpGet get = new HttpGet(STOCKINFO_URL + generateUrlParams(new StockInfoParam(symbol)));
                HttpResponse response;
                try
                {
                    response = client.execute(get, context);
                    if (response.getStatusLine().getStatusCode() == 200)
                    {
                        JSONObject json = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
                        if (json.containsKey(symbol))
                        {
                            RedisSupport.getJedis().hset(STOCKLIST_KEY, symbol, json.getString(symbol));
                            logger.info("add stock success for {}", symbol);
                        }
                    }
                    else
                    {
                        logger.error("getStockInfoBySymbol error {}", EntityUtils.toString(response.getEntity()));
                    }
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

    public static String generateUrlParams(Object param)
    {
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
