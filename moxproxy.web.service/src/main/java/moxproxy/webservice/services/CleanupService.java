package moxproxy.webservice.services;

import moxproxy.interfaces.IMoxProxyScheduleFunctionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
public class CleanupService {

    private static final Logger LOG = LoggerFactory.getLogger(CleanupService.class);

    @Autowired
    IMoxProxyScheduleFunctionService moxProxyService;

    @Scheduled(fixedRate = 300000, initialDelay = 300000)
    public void performCleanup(){
        LOG.info("Staring cleanup service");
        Date today = Calendar.getInstance().getTime();
        moxProxyService.cleanProcessedTraffic(today);
        moxProxyService.cleanRules(today);
        LOG.info("Cleanup service complete");
    }
}