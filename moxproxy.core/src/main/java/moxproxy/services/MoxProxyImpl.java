package moxproxy.services;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import moxproxy.adapters.MoxProxyFiltersAdapter;
import moxproxy.interfaces.*;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public final class MoxProxyImpl extends MoxProxyServiceImpl implements MoxProxy {

    private static final Logger LOG = LoggerFactory.getLogger(MoxProxyImpl.class);

    private HttpProxyServer proxyServer;

    private MoxProxyServiceConfiguration configuration;

    private MoxProxyTrafficRecorder recorder;

    private MoxProxyRuleProcessor processor;

    @Inject
    public MoxProxyImpl(MoxProxyServiceConfiguration configuration, MoxProxyTrafficRecorder recorder, MoxProxyRuleProcessor processor,
                        MoxProxyDatabase database, MoxProxyRulesMatcher matcher){
        super(database, matcher, configuration);
        this.configuration = configuration;
        this.recorder = recorder;
        this.processor = processor;
    }

    @Override
    public void startServer() {
        startDatabase();
        startProxyServer();
    }

    private void startDatabase(){
        database.startDatabase();
    }

    private void startProxyServer(){

        try {
            CertificateSniffingMitmManager mitm = new CertificateSniffingMitmManager(configuration.getAuthority());
            LOG.info("Starting MoxProxy on port {}", configuration.getProxyPort());

            proxyServer = DefaultHttpProxyServer.bootstrap()
                    .withPort(configuration.getProxyPort())
                    .withAllowLocalOnly(false)
                    .withFiltersSource(getFiltersSource())
                    .withManInTheMiddle(mitm)
                    .start();

            LOG.info("MoxProxy server started");
        } catch (Exception e) {
            LOG.error("Failed to start proxy", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopServer() {
        proxyServer.stop();
        database.stopDatabase();

        LOG.info("MoxProxy server stopped");
    }

    private HttpFiltersSource getFiltersSource(){
        return new HttpFiltersSource() {

            @Override
            public HttpFilters filterRequest(HttpRequest httpRequest, ChannelHandlerContext channelHandlerContext) {
                return new MoxProxyFiltersAdapter(httpRequest, channelHandlerContext, matcher, recorder, processor, configuration);
            }

            @Override
            public int getMaximumRequestBufferSizeInBytes() {
                return getBufferSize();
            }

            @Override
            public int getMaximumResponseBufferSizeInBytes() {
                return getBufferSize();
            }

            private int getBufferSize(){
                return Integer.MAX_VALUE;
            }
        };
    }
}
