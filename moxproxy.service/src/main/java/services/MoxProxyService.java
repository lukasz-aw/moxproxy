package services;

import dto.MoxProxyProcessedTrafficEntry;
import interfaces.IMoxProxyService;

public class MoxProxyService implements IMoxProxyService {

    public MoxProxyService(){

    }

    @Override
    public Iterable<MoxProxyProcessedTrafficEntry> getSessionNetworkTraffic(String sessionId) {
        return null;
    }

    @Override
    public Iterable<MoxProxyProcessedTrafficEntry> getAllNetworkTraffic() {
        return null;
    }

    @Override
    public String replaceEntry(MoxProxyProcessedTrafficEntry moxProxyRequest) {
        return null;
    }

    @Override
    public String modifyEntry(MoxProxyProcessedTrafficEntry moxProxyRequest) {
        return null;
    }

    @Override
    public void cancelRule(String ruleId) {

    }

    @Override
    public void clearSessionEntries(String sessionId) {
    }

    @Override
    public void clearAllSessionEntries() {

    }
}
