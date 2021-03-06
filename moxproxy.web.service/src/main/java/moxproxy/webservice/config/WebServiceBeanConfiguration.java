package moxproxy.webservice.config;

import moxproxy.builders.AuthorityBuilder;
import moxproxy.configuration.MoxProxyServiceConfigurationImpl;
import moxproxy.interfaces.*;
import moxproxy.rules.MoxProxyRuleProcessorImpl;
import moxproxy.rules.MoxProxyRulesMatcherImpl;
import moxproxy.services.MoxProxyDatabaseImpl;
import moxproxy.services.MoxProxyImpl;
import moxproxy.services.MoxProxyServiceImpl;
import moxproxy.services.MoxProxyTrafficRecorderImpl;
import moxproxy.webservice.ApplicationShutdown;
import org.littleshoot.proxy.mitm.Authority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

@Configuration
public class WebServiceBeanConfiguration {

    @Autowired
    private WebServiceConfiguration webServiceConfiguration;

    @Bean
    ApplicationShutdown applicationShutdown(){
        return new ApplicationShutdown();
    }

    @Bean
    MoxProxyServiceConfiguration moxProxyServiceConfiguration(){

        Authority authority;

        if(webServiceConfiguration.getMitm().getCreateOwn()){
            authority = AuthorityBuilder.create().withKeyStoreDir(webServiceConfiguration.getMitm().getKeyStoreDir())
                    .withAlias(webServiceConfiguration.getMitm().getAlias())
                    .withPassword(webServiceConfiguration.getMitm().getPassword())
                    .withOrganization(webServiceConfiguration.getMitm().getOrganization())
                    .withCommonName(webServiceConfiguration.getMitm().getCommonName())
                    .withOrganizationalUnitName(webServiceConfiguration.getMitm().getOrganizationalUnitName())
                    .withCertOrganization(webServiceConfiguration.getMitm().getCertOrganization())
                    .withCertOrganizationalUnitName(webServiceConfiguration.getMitm().getCertOrganizationalUnitName()).build();
        }else {
            authority = AuthorityBuilder.create().build();
        }

        return new MoxProxyServiceConfigurationImpl(webServiceConfiguration.getProxy().getProxyPort(),
                webServiceConfiguration.getProxy().getUrlWhiteListForTrafficRecorder(),
                webServiceConfiguration.getProxy().isSessionIdMatchStrategy(),
                webServiceConfiguration.getProxy().isRecordContent(), authority);
    }

    @Bean
    MoxProxyDatabase moxProxyDatabase(){
        return new MoxProxyDatabaseImpl();
    }

    @Bean
    MoxProxyRuleProcessor moxProxyRuleProcessor(MoxProxyDatabase moxProxyDatabase){
        return new MoxProxyRuleProcessorImpl(moxProxyDatabase);
    }

    @Bean
    TaskScheduler taskScheduler(){
        return new ConcurrentTaskScheduler();
    }

    @Bean
    MoxProxyRulesMatcher moxProxyRules(MoxProxyDatabase database){
        return new MoxProxyRulesMatcherImpl(database);
    }

    @Bean
    MoxProxyTrafficRecorder moxProxyTrafficRecorder(MoxProxyServiceConfiguration moxProxyServiceConfiguration, MoxProxyDatabase moxProxyDatabase){
        return new MoxProxyTrafficRecorderImpl(moxProxyServiceConfiguration, moxProxyDatabase);
    }

    @Bean(name = "moxProxyScheduleService")
    MoxProxyScheduleFunctionService moxProxyScheduleService(MoxProxyDatabase database, MoxProxyRulesMatcher matcher, MoxProxyServiceConfiguration configuration){
        return new MoxProxyServiceImpl(database, matcher, configuration);
    }

    @Bean(name = "moxProxyService")
    MoxProxyService moxProxyService(MoxProxyDatabase moxProxyDatabase, MoxProxyRulesMatcher matcher, MoxProxyServiceConfiguration configuration){
        return new MoxProxyServiceImpl(moxProxyDatabase, matcher, configuration);
    }

    @Bean(name = "moxProxyServer")
    MoxProxy moxProxyServer(MoxProxyServiceConfiguration configuration, MoxProxyTrafficRecorder trafficRecorder,
                            MoxProxyRuleProcessor processor, MoxProxyDatabase database, MoxProxyRulesMatcher matcher){
        return new MoxProxyImpl(configuration, trafficRecorder, processor, database, matcher);
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint(){
        return new AuthenticationEntryPoint();
    }
}
