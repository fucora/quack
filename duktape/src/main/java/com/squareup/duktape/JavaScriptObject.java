package com.squareup.duktape;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;

public class JavaScriptObject implements DuktapeObject {
    final public Duktape duktape;
    public final long context;
    final public long pointer;
    public JavaScriptObject(Duktape duktape, long context, long pointer) {
        this.duktape = duktape;
        this.context = context;
        this.pointer = pointer;
    }

    @Override
    public Object get(String key) {
        return duktape.getKeyString(pointer, key);
    }

    @Override
    public Object get(int index) {
        return duktape.getKeyInteger(pointer, index);
    }

    private void coerceArgs(Object... args) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                args[i] = duktape.coerceJavaToJavascript(args[i]);
            }
        }
    }

    @Override
    public Object call(Object... args) {
        coerceArgs(args);
        return duktape.coerceJavaScriptToJava(null, duktape.call(pointer, args));
    }

    @Override
    public Object callMethod(Object thiz, Object... args) {
        coerceArgs(args);
        return duktape.coerceJavaScriptToJava(null, duktape.callMethod(pointer, thiz, args));
    }

    @Override
    public Object callProperty(Object property, Object... args) {
        coerceArgs(args);
        return duktape.coerceJavaScriptToJava(null, duktape.callProperty(pointer, property, args));
    }

    @Override
    public Object get(Object key) {
        if (key instanceof String)
            return get((String)key);

        if (key instanceof Number) {
            Number number = (Number)key;
            if (((Integer)number.intValue()).equals(number))
                return get(number.intValue());
        }

        return duktape.getKeyObject(pointer, key);
    }

    @Override
    public void set(String key, Object value) {
        duktape.setKeyString(pointer, key, value);
    }

    @Override
    public void set(int index, Object value) {
        duktape.setKeyInteger(pointer, index, value);
    }

    @Override
    public void set(Object key, Object value) {
        if (key instanceof String) {
            set((String)key, value);
            return;
        }

        if (key instanceof Number) {
            Number number = (Number)key;
            if (number.doubleValue() == number.intValue()) {
                set(number.intValue(), value);
                return;
            }
        }

        duktape.setKeyObject(pointer, key, value);
    }

    @Override
    public String toString() {
        Object ret = callProperty("toString");
        if (ret == null)
            return null;
        return ret.toString();
    }

    public InvocationHandler createInvocationHandler() {
        return (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class)
                return method.invoke(JavaScriptObject.this, args);
            return duktape.coerceJavaScriptToJava(method.getReturnType(), JavaScriptObject.this.callProperty(method.getName(), args));
        };
    }

    public <T> T proxyInterface(Class<T> clazz, Class... more) {
        ArrayList<Class> classes = new ArrayList<>();
        classes.add(clazz);
        if (more != null)
            Collections.addAll(classes, more);

        return (T)Proxy.newProxyInstance(clazz.getClassLoader(), classes.toArray(new Class[0]), createInvocationHandler());
    }
}
