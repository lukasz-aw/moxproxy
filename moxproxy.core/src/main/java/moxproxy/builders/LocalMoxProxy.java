package moxproxy.builders;

import moxproxy.buildes.BaseBuilder;
import moxproxy.buildes.NullType;
import moxproxy.configuration.MoxProxyServiceConfigurationImpl;
import moxproxy.di.DaggerServiceComponent;
import moxproxy.di.ServiceComponent;
import moxproxy.di.ServiceModule;
import moxproxy.exceptions.BuilderValidationException;
import moxproxy.interfaces.MoxProxy;
import moxproxy.interfaces.MoxProxyServiceConfiguration;

import java.util.ArrayList;
import java.util.List;

public class LocalMoxProxy extends BaseBuilder<NullType, LocalMoxProxy, MoxProxy, LocalMoxProxyServerBuilderValidator> {

    private int port;
    private boolean sessionIdMatchStrategy;
    private List<String> recorderWhiteList;

    private LocalMoxProxy() {
        super(null, new LocalMoxProxyServerBuilderValidator());
        recorderWhiteList = new ArrayList<>();
    }

    @Override
    protected MoxProxy performBuild() throws BuilderValidationException {
        MoxProxyServiceConfiguration configuration = new MoxProxyServiceConfigurationImpl(port, recorderWhiteList, sessionIdMatchStrategy);
        ServiceComponent component = DaggerServiceComponent.builder().serviceModule(new ServiceModule(configuration)).build();
        return component.getMoxProxy();
    }

    @Override
    protected LocalMoxProxy getCurrentBuilder() {
        return this;
    }

    public static LocalMoxProxy builder(){
        return new LocalMoxProxy();
    }

    public LocalMoxProxy withPort(int port){
        this.port = port;
        return this;
    }

    public LocalMoxProxy withSessionIdMatchStrategy(){
        sessionIdMatchStrategy = true;
        return this;
    }

    public LocalMoxProxy withRecorderWhiteList(List<String> recorderWhiteList){
        this.recorderWhiteList = recorderWhiteList;
        return this;
    }

    int getPort() {
        return port;
    }
}
