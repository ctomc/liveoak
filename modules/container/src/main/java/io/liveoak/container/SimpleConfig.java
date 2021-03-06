/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.liveoak.spi.Config;
import io.liveoak.spi.ConfigurationException;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class SimpleConfig implements Config {

    private final Map<String, Object> fields;

    public SimpleConfig() {
        this(new HashMap<>());
    }

    public SimpleConfig(Map<String, Object> fields) {
        this.fields = fields;
    }

    @SuppressWarnings("unchecked")
    public <T> T put(String name, T o) {
        return (T) fields.put(name, o);
    }

    @Override
    public <T> T get(String name, T defaultValue) throws ConfigurationException {
        Object field = fields.get(name);
        if (field == null) {
            return defaultValue;
        } else {
            //TODO: Throw ConfigurationException for invalid types based on defaultValue type
            return (T) field;
        }
    }

    public void putAll(Map<String, Object> config) {
        config.putAll(this.fields);
    }

    @Override
    public Set<String> names() {
        return fields.keySet();
    }
}
