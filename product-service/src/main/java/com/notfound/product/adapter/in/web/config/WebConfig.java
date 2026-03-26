package com.notfound.product.adapter.in.web.config;

import com.notfound.product.adapter.in.web.HeaderAuthArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final HeaderAuthArgumentResolver headerAuthArgumentResolver;

    public WebConfig(HeaderAuthArgumentResolver headerAuthArgumentResolver) {
        this.headerAuthArgumentResolver = headerAuthArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(headerAuthArgumentResolver);
    }
}
