package testing.client;

import com.google.common.collect.Lists;
import moxproxy.builders.MoxProxyRuleBuilder;
import moxproxy.client.MoxProxyClient;
import moxproxy.configuration.MoxProxyClientConfiguration;
import moxproxy.dto.MoxProxyProcessedTrafficEntry;
import moxproxy.dto.MoxProxyRule;
import moxproxy.enums.MoxProxyAction;
import moxproxy.enums.MoxProxyDirection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ClientTest {


    //@Test
    public void testClient(){
        var config = new MoxProxyClientConfiguration();
        var client = new MoxProxyClient(config);

        Iterable<MoxProxyProcessedTrafficEntry> traffic =  client.getAllRequestTraffic();
        var list = Lists.newArrayList(traffic);

        int statusCode = 500;
        var builder = new MoxProxyRuleBuilder();
        MoxProxyRule actual = builder
                .withDirection(MoxProxyDirection.REQUEST)
                .withSessionId("qw")
                .withAction(MoxProxyAction.RESPOND)
                /*.withMatchingStrategy()
                    .useMethod()
                    .backToParent()*/
                .withHttpObjectDefinition()
                .withMethod("Get")
                .withPathPattern("dsadsad")
                .withStatusCode(statusCode)
                .withBody("dupa")
                .havingHeaders()
                .addItem()
                .withHeader("aaa", "ss")
                .backToParent()
                .backToParent()
                .backToParent()
                .build();

        client.createRule(actual);

        client.enableSessionIdMatchingStrategy();
        client.disableSessionIdMatchingStrategy();

        client.cancelRule(actual.getId());
    }

}
