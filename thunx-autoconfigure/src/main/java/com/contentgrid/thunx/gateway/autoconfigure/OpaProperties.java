package com.contentgrid.thunx.gateway.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "opa")
public class OpaProperties {

    static final String DEFAULT_SERVICE_URL = "http://localhost:8181/";

    private OpaServiceProperties service = new OpaServiceProperties();
    private String query;

    @Data
    public static class OpaServiceProperties {
        private String url = DEFAULT_SERVICE_URL;
    }
}
