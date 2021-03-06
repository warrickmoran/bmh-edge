/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.noaa.nws.bmh_edge;

import java.util.concurrent.Executor;

import javax.annotation.Resource;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import gov.noaa.nws.bmh_edge.services.NormalPlaylistService;

// TODO: Auto-generated Javadoc
//CHECKSTYLE:OFF
/**
 * A sample Spring Boot application that starts the Camel routes.
 */
@SpringBootApplication
@EnableAsync
// load the spring xml file from classpath
@ImportResource("classpath:bmh-edge-camel.xml")
@EnableConfigurationProperties(gov.noaa.nws.bmh_edge.utility.YAMLBmhConfig.class)
public class BmhEdgeCamelApplication {
	
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(BmhEdgeCamelApplication.class);
	
	/** The service. */
	// resource should be a singleton since object is used here and BmhPlaylistUtility
	@Resource
	private NormalPlaylistService service;
	
	/** The context path. */
	@Value("${camel.springboot.path}")
	String contextPath;
	
	// prevent thrift NumberFormat exception
	static {
		// increased to account for new BroadcastMsgGroup size
		System.setProperty("thrift.stream.maxsize", Integer.toString(1000));
	}

    /**
     * A main method to start this application.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BmhEdgeCamelApplication.class, args);
    }
    
    /**
     * Servlet registration bean.
     *
     * @return the servlet registration bean
     */
    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servlet = new ServletRegistrationBean(new CamelHttpTransportServlet(), contextPath+"/*");
        servlet.setName("CamelServlet");
        return servlet;
    }
    
    /**
     * Async executor.
     *
     * @return the executor
     */
    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("PlaylistService-");
        executor.initialize();
        return executor;
    }
}
//CHECKSTYLE:ON
