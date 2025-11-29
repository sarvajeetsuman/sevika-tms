package online.sevika.tm.aspect;
import online.sevika.tm.dto.ProjectResponseDTO;
import online.sevika.tm.dto.SubscriptionResponseDTO;
import online.sevika.tm.dto.TaskResponseDTO;
import online.sevika.tm.dto.UserResponseDTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.sevika.tm.dto.*;
import online.sevika.tm.entity.enums.AuditAction;
import online.sevika.tm.entity.enums.EntityType;
import online.sevika.tm.service.AuditLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Aspect for automatically logging audit entries for CRUD operations
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @AfterReturning(
            pointcut = "execution(* online.sevika.tm.service.impl.ProjectServiceImpl.createProject(..))",
            returning = "result"
    )
    public void logProjectCreation(JoinPoint joinPoint, Object result) {
        try {
            ProjectResponseDTO project = (ProjectResponseDTO) result;
            logActivity(
                    EntityType.PROJECT,
                    project.getId().toString(),
                    AuditAction.CREATED,
                    null,
                    objectMapper.writeValueAsString(project),
                    "Project created: " + project.getName()
            );
        } catch (Exception e) {
            log.error("Failed to log project creation: " + e.getMessage(), e);
        }
    }

    @AfterReturning(
            pointcut = "execution(* online.sevika.tm.service.impl.ProjectServiceImpl.updateProject(..))",
            returning = "result"
    )
    public void logProjectUpdate(JoinPoint joinPoint, Object result) {
        try {
            ProjectResponseDTO project = (ProjectResponseDTO) result;
            Object[] args = joinPoint.getArgs();
            String oldValue = args.length > 1 ? objectMapper.writeValueAsString(args[1]) : null;
            
            logActivity(
                    EntityType.PROJECT,
                    project.getId().toString(),
                    AuditAction.UPDATED,
                    oldValue,
                    objectMapper.writeValueAsString(project),
                    "Project updated: " + project.getName()
            );
        } catch (Exception e) {
            log.error("Failed to log project update: " + e.getMessage(), e);
        }
    }

    @AfterReturning(
            pointcut = "execution(* online.sevika.tm.service.impl.ProjectServiceImpl.deleteProject(..))"
    )
    public void logProjectDeletion(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            UUID projectId = (UUID) args[0];
            
            logActivity(
                    EntityType.PROJECT,
                    projectId.toString(),
                    AuditAction.DELETED,
                    null,
                    null,
                    "Project deleted"
            );
        } catch (Exception e) {
            log.error("Failed to log project deletion: " + e.getMessage(), e);
        }
    }

    @AfterReturning(
            pointcut = "execution(* online.sevika.tm.service.impl.TaskServiceImpl.createTask(..))",
            returning = "result"
    )
    public void logTaskCreation(JoinPoint joinPoint, Object result) {
        try {
            TaskResponseDTO task = (TaskResponseDTO) result;
            logActivity(
                    EntityType.TASK,
                    task.getId().toString(),
                    AuditAction.CREATED,
                    null,
                    objectMapper.writeValueAsString(task),
                    "Task created: " + task.getTitle()
            );
        } catch (Exception e) {
            log.error("Failed to log task creation: " + e.getMessage(), e);
        }
    }

    @AfterReturning(
            pointcut = "execution(* online.sevika.tm.service.impl.TaskServiceImpl.updateTask(..))",
            returning = "result"
    )
    public void logTaskUpdate(JoinPoint joinPoint, Object result) {
        try {
            TaskResponseDTO task = (TaskResponseDTO) result;
            Object[] args = joinPoint.getArgs();
            String oldValue = args.length > 1 ? objectMapper.writeValueAsString(args[1]) : null;
            
            logActivity(
                    EntityType.TASK,
                    task.getId().toString(),
                    AuditAction.UPDATED,
                    oldValue,
                    objectMapper.writeValueAsString(task),
                    "Task updated: " + task.getTitle()
            );
        } catch (Exception e) {
            log.error("Failed to log task update: " + e.getMessage(), e);
        }
    }

    @AfterReturning(
            pointcut = "execution(* online.sevika.tm.service.impl.TaskServiceImpl.deleteTask(..))"
    )
    public void logTaskDeletion(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            UUID taskId = (UUID) args[0];
            
            logActivity(
                    EntityType.TASK,
                    taskId.toString(),
                    AuditAction.DELETED,
                    null,
                    null,
                    "Task deleted"
            );
        } catch (Exception e) {
            log.error("Failed to log task deletion: " + e.getMessage(), e);
        }
    }

    @AfterReturning(
            pointcut = "execution(* online.sevika.tm.service.impl.TaskServiceImpl.updateTaskStatus(..))",
            returning = "result"
    )
    public void logTaskStatusChange(JoinPoint joinPoint, Object result) {
        try {
            TaskResponseDTO task = (TaskResponseDTO) result;
            Object[] args = joinPoint.getArgs();
            String newStatus = args.length > 1 ? args[1].toString() : "UNKNOWN";
            
            logActivity(
                    EntityType.TASK,
                    task.getId().toString(),
                    AuditAction.STATUS_CHANGED,
                    null,
                    newStatus,
                    "Task status changed to: " + newStatus
            );
        } catch (Exception e) {
            log.error("Failed to log task status change: " + e.getMessage(), e);
        }
    }

    @AfterReturning(
            pointcut = "execution(* online.sevika.tm.service.impl.UserServiceImpl.createUser(..))",
            returning = "result"
    )
    public void logUserCreation(JoinPoint joinPoint, Object result) {
        try {
            UserResponseDTO user = (UserResponseDTO) result;
            logActivity(
                    EntityType.USER,
                    user.getId().toString(),
                    AuditAction.CREATED,
                    null,
                    objectMapper.writeValueAsString(user),
                    "User created: " + user.getUsername()
            );
        } catch (Exception e) {
            log.error("Failed to log user creation: " + e.getMessage(), e);
        }
    }

    @AfterReturning(
            pointcut = "execution(* online.sevika.tm.service.impl.UserServiceImpl.deleteUser(..))"
    )
    public void logUserDeletion(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            UUID userId = (UUID) args[0];
            
            logActivity(
                    EntityType.USER,
                    userId.toString(),
                    AuditAction.DELETED,
                    null,
                    null,
                    "User deleted"
            );
        } catch (Exception e) {
            log.error("Failed to log user deletion: " + e.getMessage(), e);
        }
    }

    @AfterReturning(
            pointcut = "execution(* online.sevika.tm.service.impl.SubscriptionServiceImpl.createSubscription(..))",
            returning = "result"
    )
    public void logSubscriptionCreation(JoinPoint joinPoint, Object result) {
        try {
            SubscriptionResponseDTO subscription = (SubscriptionResponseDTO) result;
            logActivity(
                    EntityType.SUBSCRIPTION,
                    subscription.getId().toString(),
                    AuditAction.CREATED,
                    null,
                    objectMapper.writeValueAsString(subscription),
                    "Subscription created"
            );
        } catch (Exception e) {
            log.error("Failed to log subscription creation: " + e.getMessage(), e);
        }
    }

    @AfterReturning(
            pointcut = "execution(* online.sevika.tm.service.impl.SubscriptionServiceImpl.cancelSubscription(..))",
            returning = "result"
    )
    public void logSubscriptionCancellation(JoinPoint joinPoint, Object result) {
        try {
            SubscriptionResponseDTO subscription = (SubscriptionResponseDTO) result;
            logActivity(
                    EntityType.SUBSCRIPTION,
                    subscription.getId().toString(),
                    AuditAction.STATUS_CHANGED,
                    "ACTIVE",
                    "CANCELLED",
                    "Subscription cancelled"
            );
        } catch (Exception e) {
            log.error("Failed to log subscription cancellation: " + e.getMessage(), e);
        }
    }

    private void logActivity(
            EntityType entityType,
            String entityId,
            AuditAction action,
            String oldValue,
            String newValue,
            String description
    ) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = null;
            String username = "system";

            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                username = auth.getName();
                // Try to extract userId if available in the authentication
                if (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                    // Username is available, userId would need to be fetched from UserService if needed
                    // For now, we'll just use the username
                }
            }

            String ipAddress = null;
            String userAgent = null;

            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    ipAddress = getClientIP(request);
                    userAgent = request.getHeader("User-Agent");
                }
            } catch (Exception e) {
                log.debug("Could not extract request details: " + e.getMessage());
            }

            auditLogService.logActivity(
                    entityType,
                    entityId,
                    action,
                    userId,
                    username,
                    oldValue,
                    newValue,
                    description,
                    ipAddress,
                    userAgent
            );
        } catch (Exception e) {
            log.error("Failed to log activity: " + e.getMessage(), e);
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
