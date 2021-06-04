package com.genersoft.iot.vmp.conf;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Locale;
import java.util.Map;


@Configuration
public class ProxyServletConfig {

    private final static Logger logger = LoggerFactory.getLogger(ProxyServletConfig.class);

    @Autowired
    private MediaConfig mediaConfig;

    @Bean
    public ServletRegistrationBean zlmServletRegistrationBean(){
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new ZLMProxySerlet(),"/zlm/*");
        servletRegistrationBean.setName("zlm_Proxy");
        servletRegistrationBean.addInitParameter("targetUri", String.format("http://%s:%s", mediaConfig.getIp(), mediaConfig.getHttpPort()));
        servletRegistrationBean.addUrlMappings();
        if (logger.isDebugEnabled()) {
            servletRegistrationBean.addInitParameter("log", "true");
        }
        return servletRegistrationBean;
    }

    class  ZLMProxySerlet extends ProxyServlet{



        @Override
        protected String rewriteQueryStringFromRequest(HttpServletRequest servletRequest, String queryString) {
            String queryStr = super.rewriteQueryStringFromRequest(servletRequest, queryString);
            if (queryStr != null) {
                queryStr += "&";
            }else {
                queryStr = "?";
            }
            queryStr += "secret=" + mediaConfig.getSecret();
            return queryStr;
        }

        @Override
        protected void handleRequestException(HttpRequest proxyRequest, HttpResponse proxyResonse, Exception e){
            System.out.println(e.getMessage());
            try {
                super.handleRequestException(proxyRequest, proxyResonse, e);
            } catch (ServletException servletException) {
                logger.error("zlm 代理失败： ", e);
            } catch (IOException ioException) {
                if (ioException instanceof ConnectException) {
                    logger.error("zlm 连接失败");
                }else {
                    logger.error("zlm 代理失败： ", e);
                }
            } catch (RuntimeException exception){
                logger.error("zlm 代理失败： ", e);
            }
        }
    }

}