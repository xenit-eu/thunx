package eu.contentcloud.security.abac.pdp.opa;

import eu.contentcloud.security.abac.pdp.RequestContext;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;

public class DiscoveryClientOpaQueryProvider implements OpaQueryProvider {

    private ReactiveDiscoveryClient discoveryClient;

    public DiscoveryClientOpaQueryProvider(ReactiveDiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public String createQuery(RequestContext requestContext) {
        throw new UnsupportedOperationException();
    }
}
