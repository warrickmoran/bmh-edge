package gov.noaa.nws.bmh_edge.utility;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.interceptor.PerformanceMonitorInterceptor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;


// TODO: Auto-generated Javadoc
/**
 * The Class BmhEdgePerformanceGoogleApiAop.
 */
@Configuration
@EnableAspectJAutoProxy
@Aspect
public class BmhEdgePerformanceGoogleApiAop {
	     
	    /**
    	 * Monitor.
    	 */
    	@Pointcut(
	      "execution(* gov.noaa.nws.bmh_edge.utility.GoogleSpeechUtility.*(..))"
	    )
	    public void monitor() { }
	     
	    /**
    	 * Performance monitor interceptor.
    	 *
    	 * @return the performance monitor interceptor
    	 */
    	@Bean
	    public PerformanceMonitorInterceptor performanceMonitorInterceptor() {
	        return new PerformanceMonitorInterceptor(true);
	    }
	 
	    /**
    	 * Performance monitor advisor.
    	 *
    	 * @return the advisor
    	 */
    	@Bean
	    public Advisor performanceMonitorAdvisor() {
	        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
	        pointcut.setExpression("gov.noaa.nws.bmh_edge.utility.BmhEdgePerformanceGoogleApiAop.monitor()");
	        return new DefaultPointcutAdvisor(pointcut, performanceMonitorInterceptor());
	    }
	    
	    /**
    	 * Dac playlist message metadata.
    	 *
    	 * @return the dac playlist message metadata
    	 */
    	@Bean DacPlaylistMessageMetadata dacPlaylistMessageMetadata() {
	    	return new DacPlaylistMessageMetadata();
	    }
}
