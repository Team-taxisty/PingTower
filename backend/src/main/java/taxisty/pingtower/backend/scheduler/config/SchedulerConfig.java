package taxisty.pingtower.backend.scheduler.config;

import java.util.Properties;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * Configuration for the Quartz scheduler used by PingTower monitoring.
 * Configures job persistence, clustering, and Spring integration.
 */
@Configuration
public class SchedulerConfig {
    
    /**
     * Creates a custom job factory that enables Spring dependency injection in Quartz jobs.
     */
    @Bean
    public SpringBeanJobFactory springBeanJobFactory(ApplicationContext applicationContext) {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }
    
    /**
     * Main scheduler factory bean configuration.
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(SpringBeanJobFactory jobFactory,
                                                   SchedulerProperties schedulerProperties) {
        
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(createQuartzProperties(schedulerProperties));
        factory.setWaitForJobsToCompleteOnShutdown(true);
        factory.setAutoStartup(true);
        
        return factory;
    }
    
    /**
     * Scheduler bean for direct injection.
     */
    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws SchedulerException {
        return schedulerFactoryBean.getScheduler();
    }
    
    /**
     * Configuration properties for scheduler behavior.
     */
    @Bean
    @ConfigurationProperties(prefix = "pingtower.scheduler")
    public SchedulerProperties schedulerProperties() {
        return new SchedulerProperties();
    }
    
    private Properties createQuartzProperties(SchedulerProperties props) {
        Properties properties = new Properties();
        
        // Scheduler configuration
        properties.put("org.quartz.scheduler.instanceName", props.getInstanceName());
        properties.put("org.quartz.scheduler.instanceId", "AUTO");
        properties.put("org.quartz.scheduler.skipUpdateCheck", "true");
        
        // Thread pool configuration
        properties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.put("org.quartz.threadPool.threadCount", String.valueOf(props.getThreadCount()));
        properties.put("org.quartz.threadPool.threadPriority", "5");
        
        // Job store configuration (using RAM for development)
        properties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        
        // Misfire handling
        properties.put("org.quartz.jobStore.misfireThreshold", String.valueOf(props.getMisfireThreshold()));
        
        return properties;
    }
}