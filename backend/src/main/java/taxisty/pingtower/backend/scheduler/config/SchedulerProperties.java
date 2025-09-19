package taxisty.pingtower.backend.scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the PingTower monitoring scheduler.
 * Maps to application properties with prefix 'pingtower.scheduler'.
 */
@ConfigurationProperties(prefix = "pingtower.scheduler")
public class SchedulerProperties {
    
    /**
     * Name of the scheduler instance.
     */
    private String instanceName = "PingTowerScheduler";
    
    /**
     * Number of threads in the scheduler thread pool.
     */
    private int threadCount = 10;
    
    /**
     * Whether this scheduler instance is part of a cluster.
     */
    private boolean clustered = false;
    
    /**
     * Cluster check-in interval in milliseconds.
     */
    private long clusterCheckinInterval = 20000;
    
    /**
     * Misfire threshold in milliseconds.
     * Jobs that are delayed by more than this amount are considered misfired.
     */
    private long misfireThreshold = 60000;
    
    /**
     * Maximum retry attempts for failed monitoring tasks.
     */
    private int maxRetryAttempts = 3;
    
    /**
     * Delay between retry attempts in seconds.
     */
    private int retryDelaySeconds = 30;
    
    /**
     * Default timeout for HTTP requests in seconds.
     */
    private int defaultTimeoutSeconds = 30;
    
    /**
     * Whether to automatically start the scheduler on application startup.
     */
    private boolean autoStart = true;
    
    // Getters and setters
    
    public String getInstanceName() {
        return instanceName;
    }
    
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
    
    public int getThreadCount() {
        return threadCount;
    }
    
    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
    
    public boolean isClustered() {
        return clustered;
    }
    
    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }
    
    public long getClusterCheckinInterval() {
        return clusterCheckinInterval;
    }
    
    public void setClusterCheckinInterval(long clusterCheckinInterval) {
        this.clusterCheckinInterval = clusterCheckinInterval;
    }
    
    public long getMisfireThreshold() {
        return misfireThreshold;
    }
    
    public void setMisfireThreshold(long misfireThreshold) {
        this.misfireThreshold = misfireThreshold;
    }
    
    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }
    
    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }
    
    public int getRetryDelaySeconds() {
        return retryDelaySeconds;
    }
    
    public void setRetryDelaySeconds(int retryDelaySeconds) {
        this.retryDelaySeconds = retryDelaySeconds;
    }
    
    public int getDefaultTimeoutSeconds() {
        return defaultTimeoutSeconds;
    }
    
    public void setDefaultTimeoutSeconds(int defaultTimeoutSeconds) {
        this.defaultTimeoutSeconds = defaultTimeoutSeconds;
    }
    
    public boolean isAutoStart() {
        return autoStart;
    }
    
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }
}