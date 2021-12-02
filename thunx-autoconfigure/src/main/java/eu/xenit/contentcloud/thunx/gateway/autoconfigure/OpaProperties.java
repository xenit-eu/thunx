package eu.xenit.contentcloud.thunx.gateway.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "opa.service")
public class OpaProperties {
    private String url;
}
