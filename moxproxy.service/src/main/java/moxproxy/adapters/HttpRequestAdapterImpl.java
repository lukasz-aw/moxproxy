package moxproxy.adapters;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import moxproxy.interfaces.HttpRequestAdapter;

public class HttpRequestAdapterImpl extends BaseHttpTrafficAdapter implements HttpRequestAdapter {

    HttpRequestAdapterImpl(HttpObject request, HttpRequest originalRequest, String connectedUrl, boolean isSessionIdStrategy) {
        super(request, originalRequest, connectedUrl, isSessionIdStrategy);
    }

    @Override
    protected HttpHeaders getHeaders() {
        return ((FullHttpRequest)getHttpObject()).headers();
    }

    @Override
    protected ByteBuf getContent() {
        return ((FullHttpRequest)getHttpObject()).content();
    }

    @Override
    protected HttpMethod getMethod() {
        return ((FullHttpRequest)getHttpObject()).method();
    }

    @Override
    protected String getUrl() {
        return connectedUrl + ((FullHttpRequest)getHttpObject()).uri();
    }

    @Override
    protected void getSessionId() {
        if (isSessionIdStrategy) {
            extractSessionId();
        }
    }
}
