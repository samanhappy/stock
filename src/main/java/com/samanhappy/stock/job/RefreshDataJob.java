package com.samanhappy.stock.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.samanhappy.stock.http.DataSpider;

@Component
public class RefreshDataJob
{
    private static Logger logger = LoggerFactory.getLogger(RefreshDataJob.class);

    public void run()
    {
        DataSpider.refreshData();
        logger.info("start refresh data job");
    }
}
