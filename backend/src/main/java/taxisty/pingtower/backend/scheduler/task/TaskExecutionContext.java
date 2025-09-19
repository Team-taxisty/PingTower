package taxisty.pingtower.backend.scheduler.task;

import java.time.LocalDateTime;

/**
 * Represents an execution context for a scheduled task.
 * Contains metadata about the task execution including timing and service information.
 */
public record TaskExecutionContext(
        Long serviceId,
        String taskType,
        LocalDateTime scheduledTime,
        LocalDateTime actualExecutionTime,
        String executionId,
        int retryCount,
        boolean isRetry
) {
    
    /**
     * Creates a new execution context for initial task execution.
     */
    public static TaskExecutionContext createInitial(Long serviceId, String taskType, LocalDateTime scheduledTime) {
        return new TaskExecutionContext(
                serviceId,
                taskType,
                scheduledTime,
                LocalDateTime.now(),
                generateExecutionId(serviceId, taskType),
                0,
                false
        );
    }
    
    /**
     * Creates a retry execution context based on previous attempt.
     */
    public TaskExecutionContext createRetry() {
        return new TaskExecutionContext(
                serviceId,
                taskType,
                scheduledTime,
                LocalDateTime.now(),
                executionId,
                retryCount + 1,
                true
        );
    }
    
    private static String generateExecutionId(Long serviceId, String taskType) {
        return String.format("%s-%d-%d", taskType, serviceId, System.currentTimeMillis());
    }
}