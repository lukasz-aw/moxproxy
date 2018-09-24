package moxproxy.interfaces;

import moxproxy.dto.MoxProxyRule;
import moxproxy.interfaces.IHttpTrafficAdapter;

import java.util.List;

public interface IMoxProxyRulesMatcher {

    void useSessionIdMatchingStrategy(boolean value);

    List<MoxProxyRule> match(IHttpTrafficAdapter adapter);
}
