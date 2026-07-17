package com.lab.governance.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import feign.Capability;
import feign.Client;
import feign.Request;
import feign.Response;

import java.io.IOException;

final class SentinelFeignCapability implements Capability {
    private final SentinelProperties properties;

    SentinelFeignCapability(SentinelProperties properties) {
        this.properties = properties;
    }

    @Override
    public Client enrich(Client delegate) {
        return new SentinelFeignClient(delegate, properties);
    }

    private static final class SentinelFeignClient implements Client {
        private final Client delegate;
        private final SentinelProperties properties;

        private SentinelFeignClient(Client delegate, SentinelProperties properties) {
            this.delegate = delegate;
            this.properties = properties;
        }

        @Override
        public Response execute(Request request, Request.Options options) throws IOException {
            var resource = resourceName(request);
            Entry entry = null;
            ContextUtil.enter(properties.getFeign().getContextName());
            try {
                entry = SphU.entry(resource, EntryType.OUT);
                var response = delegate.execute(request, options);
                entry.exit();
                return response;
            } catch (BlockException ex) {
                throw new SentinelFeignBlockException(resource, ex);
            } catch (IOException | RuntimeException ex) {
                if (entry != null) Tracer.traceEntry(ex, entry);
                throw ex;
            } finally {
                ContextUtil.exit();
            }
        }

        private String resourceName(Request request) {
            var prefix = properties.getFeign().getResourcePrefix();
            return prefix + request.httpMethod().name() + " " + request.url();
        }
    }

    static final class SentinelFeignBlockException extends IOException {
        SentinelFeignBlockException(String resource, BlockException cause) {
            super("Sentinel blocked Feign request: " + resource, cause);
        }
    }
}
