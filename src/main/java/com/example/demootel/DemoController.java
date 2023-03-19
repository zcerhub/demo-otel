package com.example.demootel;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@RestController
public class DemoController {

    @GetMapping("/resource")
    public void hello(@RequestHeader Map<String, String> headers) {
        headers.forEach((key,val)->{
            System.out.println(key+":"+val);
        });
        otelProcess(headers);
    }

    private void otelProcess(Map<String, String> headers) {

        TextMapGetter<Map<String, String>> getter = new TextMapGetter<Map<String, String>>() {
            @Override
            public Iterable<String> keys(Map<String, String> carrier) {
                return carrier.keySet();
            }

            @Override
            public String get(Map<String, String> carrier, String key) {
                return carrier.get(key);
            }
        };

        OpenTelemetry openTelemetry=OtelConfiguration.getOpenTelemetry();
        Context extractedContext = openTelemetry.getPropagators().getTextMapPropagator()
                .extract(Context.current(), headers, getter);
        Tracer tracer = openTelemetry.getTracer("type","http-span");

        try (Scope scope = extractedContext.makeCurrent()) {
            Span serverSpan = tracer.spanBuilder("GET /resource").setSpanKind(SpanKind.SERVER)
                    .startSpan();
            try{
                serverSpan.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
                serverSpan.setAttribute(SemanticAttributes.HTTP_SCHEME, "http");
                serverSpan.setAttribute(SemanticAttributes.HTTP_HOST, "localhost:18080");
                serverSpan.setAttribute(SemanticAttributes.HTTP_TARGET, "/resource");



            }finally {
                serverSpan.end();
            }



        }


    }


    @GetMapping("/resource1")
    public void resource1(@RequestHeader Map<String, String> headers) {
        headers.forEach((key,val)->{
            System.out.println(key+":"+val);
        });
        otelProcess1(headers);
    }

    private void otelProcess1(Map<String, String> headers) {

        TextMapGetter<Map<String, String>> getter = new TextMapGetter<Map<String, String>>() {
            @Override
            public Iterable<String> keys(Map<String, String> carrier) {
                return carrier.keySet();
            }

            @Override
            public String get(Map<String, String> carrier, String key) {
                return carrier.get(key);
            }
        };
        TextMapSetter<HttpURLConnection> setter = new TextMapSetter<HttpURLConnection>() {
            @Override
            public void set(HttpURLConnection carrier, String key, String value) {
                carrier.setRequestProperty(key, value);
            }
        };

        OpenTelemetry openTelemetry=OtelConfiguration.getOpenTelemetry();
        Context extractedContext = openTelemetry.getPropagators().getTextMapPropagator()
                .extract(Context.current(), headers, getter);
        Tracer tracer = openTelemetry.getTracer("type","http-span");

        try (Scope scope = extractedContext.makeCurrent()) {
            Span serverSpan = tracer.spanBuilder("GET /resource").setSpanKind(SpanKind.SERVER)
                    .startSpan();
            try{
                serverSpan.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
                serverSpan.setAttribute(SemanticAttributes.HTTP_SCHEME, "http");
                serverSpan.setAttribute(SemanticAttributes.HTTP_HOST, "localhost:8080");
                serverSpan.setAttribute(SemanticAttributes.HTTP_TARGET, "/resource");

                URL url = new URL("http://localhost:8080/resources");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(),httpURLConnection,setter);
                httpURLConnection.connect();
                sendGetRequest(httpURLConnection);

            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                serverSpan.end();
            }
        }


    }

    private void sendGetRequest(HttpURLConnection transportLayer) throws IOException {
        if (transportLayer.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = transportLayer.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            StringBuffer buffer = new StringBuffer();

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            reader.close();

            System.out.println(buffer.toString());

        }
    }


}
