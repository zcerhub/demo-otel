package com.example.demootel;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import java.util.concurrent.TimeUnit;

public class OtelConfiguration {

    public static OpenTelemetry getOpenTelemetry() {
        //		Resource resouce = Resource.getDefault().merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "span-test11")));
        Resource resouce = Resource.getDefault().merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "logical-service-name")));

        String jaegerEndpoint = "http://192.168.28.132:49158";
        OtlpGrpcSpanExporter jaegerOtlpExporter =
                OtlpGrpcSpanExporter.builder()
                        .setEndpoint(jaegerEndpoint)
                        .setTimeout(30, TimeUnit.SECONDS)
                        .build();

        LoggingSpanExporter exporter = LoggingSpanExporter.create();

        SpanProcessor simpleSpanProcessor = SimpleSpanProcessor.create(jaegerOtlpExporter);
        SpanProcessor logProcessor = SimpleSpanProcessor.create(exporter);

        SpanProcessor multiSpanProcessor =
                SpanProcessor.composite(simpleSpanProcessor, logProcessor);

        SdkTracerProvider sdkTraceProvider = SdkTracerProvider.builder().addSpanProcessor(multiSpanProcessor).setResource(resouce).build();

        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(sdkTraceProvider).
                setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance())).build();


        return openTelemetry;
    }

}
