package moxproxy.adapters;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import moxproxy.interfaces.HttpTrafficAdapter;
import moxproxy.model.MoxProxyHeader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseHttpTrafficAdapter implements HttpTrafficAdapter {

    private HttpObject httpObject;
    private List<MoxProxyHeader> headers;
    private String body;
    private String method;
    private String url;
    private String sessionId;
    String connectedUrl;
    HttpRequest originalRequest;
    boolean isSessionIdStrategy;

    BaseHttpTrafficAdapter(HttpObject httpObject, HttpRequest originalRequest, String connectedUrl, boolean isSessionIdStrategy) {
        this.httpObject = httpObject;
        this.originalRequest = originalRequest;
        this.connectedUrl = connectedUrl;
        this.isSessionIdStrategy = isSessionIdStrategy;
        headers = transformToProxyHeaders(getHeaders());
        body = readContent(getContent());
        method = getMethod().name();
        url = getUrl();
        getSessionId();
    }

    HttpObject getHttpObject(){
        return httpObject;
    }

    protected abstract HttpHeaders getHeaders();
    protected abstract ByteBuf getContent();
    protected abstract HttpMethod getMethod();
    protected abstract String getUrl();

    protected abstract void getSessionId();

    @Override
    public List<MoxProxyHeader> headers() {
        return headers;
    }

    @Override
    public String body() {
        return body;
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public String sessionId() {
        return sessionId;
    }

    List<MoxProxyHeader> transformToProxyHeaders(HttpHeaders headers) {

        ArrayList<MoxProxyHeader> transformed = new ArrayList<MoxProxyHeader>();
        headers.forEach(x -> transformed.add(transformToProxyHeader(x)));
        return transformed;
    }

    private MoxProxyHeader transformToProxyHeader(Map.Entry<String, String> header){

        MoxProxyHeader transformed = new MoxProxyHeader();
        transformed.setName(header.getKey());
        transformed.setValue(header.getValue());

        return transformed;
    }

    private String readContent(ByteBuf content) {
        return content.toString(0, content.readableBytes(), StandardCharsets.UTF_8);
    }

    void extractSessionId() {
        extractSessionId(headers);
    }

    void extractSessionId(List<MoxProxyHeader> headers) {
        sessionId = headers.stream().map(header -> extractSessionId(header.getValue().toString())).filter(Objects::nonNull).findFirst().orElse(sessionId);
    }

    private String extractSessionId(String value) {

        String pattern = "MOXSESSIONID=(.*?)(;|$)";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(value);
        if(matcher.find()){
            return matcher.group(1);
        }
        return null;
    }
}