package com.lab.foundation.context;

public final class RequestContextHolder {

    public static final String REACTOR_CONTEXT_KEY = RequestContextHolder.class.getName();

    private static final ThreadLocal<RequestContext> CURRENT = new ThreadLocal<>();

    private RequestContextHolder() {
    }

    public static void set(RequestContext context) { CURRENT.set(context); }
    public static RequestContext get() { return CURRENT.get(); }
    public static void clear() { CURRENT.remove(); }
}
