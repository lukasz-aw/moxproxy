package moxproxy.adapters;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import moxproxy.dto.MoxProxyHeader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseHttpTrafficAdapter implements IHttpTrafficAdapter{

    private HttpObject httpObject;
    private List<MoxProxyHeader> headers;
    private String body;
    private String method;
    private String url;
    private String sessionId;

    protected BaseHttpTrafficAdapter(HttpObject httpObject){
        this.httpObject = httpObject;
        headers = transformToProxyHeaders(getHeaders());
        body = readContent(getContent());
        method = getMethod().name();
        url = getUrl();
        extractSessionId();
    }

    protected HttpObject getHttpObject(){
        return httpObject;
    }

    protected abstract HttpHeaders getHeaders();
    protected abstract ByteBuf getContent();
    protected abstract HttpMethod getMethod();
    protected abstract String getUrl();

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

    private List<MoxProxyHeader> transformToProxyHeaders(HttpHeaders headers){

        var transformed = new ArrayList<MoxProxyHeader>();
        headers.forEach(x -> transformed.add(transformToProxyHeader(x)));
        return transformed;
    }

    private MoxProxyHeader transformToProxyHeader(Map.Entry<String, String> header){

        var transformed = new MoxProxyHeader();
        transformed.setName(header.getKey());
        transformed.setValue(header.getValue());

        return transformed;
    }

    private String readContent(ByteBuf content) {
        return content.toString(0, content.readableBytes(), StandardCharsets.UTF_8);
    }

    private void extractSessionId(){
        for(MoxProxyHeader header : headers){
            var extracted = extractSessionId(header.getValue());
            if(extracted != null){
                sessionId = extracted;
                break;
            }
        }
    }

    private String extractSessionId(String value){

        String pattern = "MOXSESSIONID=(.*);|$";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(value);
        if(matcher.find()){
            return matcher.group(1);
        }
        return null;
    }
}
