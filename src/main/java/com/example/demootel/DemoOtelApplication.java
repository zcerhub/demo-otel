package com.example.demootel;


import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class DemoOtelApplication {




	public static void main(String[] args) throws Exception {

		DemoOtelApplication application = new DemoOtelApplication();

//		application.test();
//		application.test1();
//		application.test2();
//		application.test3();
//		application.test4();
//		application.test5();
		application.test6();


		Thread.sleep(10000);
	}


	public void test6() throws Exception {
		TextMapSetter<HttpURLConnection> setter = new TextMapSetter<HttpURLConnection>() {
			@Override
			public void set(HttpURLConnection carrier, String key, String value) {
				carrier.setRequestProperty(key, value);
			}
		};

//		URL url = new URL("http://localhost:8080/resources");
		URL url = new URL("http://localhost:3333/resource1");


		OpenTelemetry openTelemetry = OtelConfiguration.getOpenTelemetry();
		Tracer tracer = openTelemetry.getTracer("type","http-span");
		Span outGoing=tracer.spanBuilder("/resource").setSpanKind(SpanKind.CLIENT).startSpan();
		try (Scope scope = outGoing.makeCurrent()) {
			outGoing.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
			outGoing.setAttribute(SemanticAttributes.HTTP_URL, url.toString());

			HttpURLConnection transportLayer = (HttpURLConnection) url.openConnection();
			openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(),transportLayer,setter);
			transportLayer.connect();

			sendGetRequest(transportLayer);

		}finally {
			outGoing.end();
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

	public void test5() {
		OpenTelemetry openTelemetry = OtelConfiguration.getOpenTelemetry();
		Tracer tracer = openTelemetry.getTracer("type","http-span");
		Span span=tracer.spanBuilder("parent1").setSpanKind(SpanKind.CLIENT).startSpan();

		try (Scope scope = span.makeCurrent()) {
			throw new Exception("error");
		} catch (Exception e) {
			span.setStatus(StatusCode.ERROR, "something is error");
			span.recordException(e);
		}finally {
			span.end();
		}
	}

	public void test4() {
		OpenTelemetry openTelemetry = OtelConfiguration.getOpenTelemetry();
		Tracer tracer = openTelemetry.getTracer("type","http-span");
		Span parentSpan1=tracer.spanBuilder("parent1").setSpanKind(SpanKind.CLIENT).startSpan();
		Span parentSpan2=tracer.spanBuilder("parent2").setSpanKind(SpanKind.CLIENT).startSpan();
		Span parentSpan3=tracer.spanBuilder("parent3").setSpanKind(SpanKind.CLIENT).startSpan();
		Span parentSpan4=tracer.spanBuilder("parent4").setSpanKind(SpanKind.CLIENT).startSpan();

		Span child = tracer.spanBuilder("child").addLink(parentSpan1.getSpanContext())
				.addLink(parentSpan2.getSpanContext()).addLink(parentSpan3.getSpanContext())
				.addLink(parentSpan4.getSpanContext()).startSpan();

		child.end();
		parentSpan1.end();
		parentSpan2.end();
		parentSpan3.end();
		parentSpan4.end();
	}

	public void test3() {
		OpenTelemetry openTelemetry = OtelConfiguration.getOpenTelemetry();
		Tracer tracer = openTelemetry.getTracer("type","http-span");
		Span span=tracer.spanBuilder("/resource/path").setSpanKind(SpanKind.CLIENT).startSpan();
		span.addEvent("Init");

		Attributes eventAttributes=Attributes.of(
				AttributeKey.stringKey("key"),"value",
				AttributeKey.longKey("result"),0L
		);

		span.addEvent("End Computation",eventAttributes);
		span.end();
	}

	public void test1() {
		OpenTelemetry openTelemetry = OtelConfiguration.getOpenTelemetry();
		Tracer tracer = openTelemetry.getTracer("type","http-span");
		Span span=tracer.spanBuilder("/resource/path").setSpanKind(SpanKind.CLIENT).startSpan();
		span.setAttribute("http.method", "get");
		span.setAttribute("http.url", "http://hello.com/api");
		span.end();
	}

	public void test2() {
		OpenTelemetry openTelemetry = OtelConfiguration.getOpenTelemetry();
		Tracer tracer = openTelemetry.getTracer("type","http-span");
		Span span=tracer.spanBuilder("/resource/path").setSpanKind(SpanKind.CLIENT).startSpan();
		span.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
		span.setAttribute(SemanticAttributes.HTTP_URL, "http://hello.com/api");
		span.end();
	}

	public void test()  {
		OpenTelemetry openTelemetry = OtelConfiguration.getOpenTelemetry();
		Tracer tracer = openTelemetry.getTracer("type", "opentelemtry-demo");
		Span span = tracer.spanBuilder("test-span").setAttribute("type", "test").startSpan();

		try (Scope scope = span.makeCurrent()) {
			System.out.println(LocalDateTime.now()+" "+"end");
		}finally {
			span.end();
		}

	}

}
