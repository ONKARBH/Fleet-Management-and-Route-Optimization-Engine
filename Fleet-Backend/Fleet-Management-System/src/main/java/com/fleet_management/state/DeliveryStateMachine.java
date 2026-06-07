// src/main/java/com/fleet_management/state/DeliveryStateMachine.java
package com.fleet_management.state;

import com.fleet_management.entity.DeliveryStatus;
import com.fleet_management.entity.DeliveryTask;
import com.fleet_management.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class DeliveryStateMachine {

    // Define allowed transitions
    private static final Map<DeliveryStatus, Set<DeliveryStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(DeliveryStatus.class);

    // Define required time gaps between statuses (in minutes)
    private static final Map<DeliveryStatus, Integer> MIN_TIME_GAPS = new EnumMap<>(DeliveryStatus.class);

    static {
        // Allowed transitions
        ALLOWED_TRANSITIONS.put(DeliveryStatus.UNASSIGNED,
                EnumSet.of(DeliveryStatus.DISPATCHED, DeliveryStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DeliveryStatus.DISPATCHED,
                EnumSet.of(DeliveryStatus.IN_TRANSIT, DeliveryStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DeliveryStatus.IN_TRANSIT,
                EnumSet.of(DeliveryStatus.DELIVERED, DeliveryStatus.FAILED));
        ALLOWED_TRANSITIONS.put(DeliveryStatus.DELIVERED,
                EnumSet.noneOf(DeliveryStatus.class));
        ALLOWED_TRANSITIONS.put(DeliveryStatus.FAILED,
                EnumSet.of(DeliveryStatus.DISPATCHED, DeliveryStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DeliveryStatus.CANCELLED,
                EnumSet.noneOf(DeliveryStatus.class));

        // Minimum time requirements (in minutes)
        MIN_TIME_GAPS.put(DeliveryStatus.UNASSIGNED, 0);
        MIN_TIME_GAPS.put(DeliveryStatus.DISPATCHED, 0);
        MIN_TIME_GAPS.put(DeliveryStatus.IN_TRANSIT, 5);  // Min 5 mins from DISPATCHED
        MIN_TIME_GAPS.put(DeliveryStatus.DELIVERED, 10); // Min 10 mins from IN_TRANSIT
        MIN_TIME_GAPS.put(DeliveryStatus.FAILED, 5);
        MIN_TIME_GAPS.put(DeliveryStatus.CANCELLED, 0);
    }

    /**
     * Validate and perform state transition
     */
    public DeliveryTask transition(DeliveryTask task, DeliveryStatus newStatus, LocalDateTime timestamp) {
        DeliveryStatus currentStatus = task.getStatus();

        // Validate transition
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new InvalidStateTransitionException(
                    String.format("Invalid transition from %s to %s for delivery %d",
                            currentStatus, newStatus, task.getId())
            );
        }

        // Validate time requirement
        if (!isTimeRequirementMet(task, newStatus, timestamp)) {
            throw new InvalidStateTransitionException(
                    String.format("Cannot transition from %s to %s too quickly. Minimum time requirement not met.",
                            currentStatus, newStatus)
            );
        }

        // Perform the transition
        task.setStatus(newStatus);

        // Set actual delivery time when delivered
        if (newStatus == DeliveryStatus.DELIVERED) {
            task.setActualDeliveryTime(timestamp);
        }

        return task;
    }

    /**
     * Check if transition is allowed
     */
    private boolean isValidTransition(DeliveryStatus from, DeliveryStatus to) {
        Set<DeliveryStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    /**
     * Check if time requirement is met for the transition
     */
    private boolean isTimeRequirementMet(DeliveryTask task, DeliveryStatus newStatus, LocalDateTime timestamp) {
        // Get minimum time required for this transition
        Integer minMinutes = MIN_TIME_GAPS.get(newStatus);
        if (minMinutes == null || minMinutes == 0) {
            return true; // No time requirement
        }

        // Check when the task entered its current status
        LocalDateTime lastUpdate = task.getUpdatedAt();
        if (lastUpdate == null) {
            return true;
        }

        long minutesElapsed = java.time.Duration.between(lastUpdate, timestamp).toMinutes();
        return minutesElapsed >= minMinutes;
    }

    /**
     * Check if delivery can be cancelled
     */
    public boolean canCancel(DeliveryTask task) {
        return task.getStatus() != DeliveryStatus.DELIVERED &&
                task.getStatus() != DeliveryStatus.CANCELLED;
    }

    /**
     * Check if delivery can be retried (for failed deliveries)
     */
    public boolean canRetry(DeliveryTask task) {
        return task.getStatus() == DeliveryStatus.FAILED;
    }

    /**
     * Get next possible statuses for a delivery
     */
    public Set<DeliveryStatus> getNextPossibleStatuses(DeliveryTask task) {
        return ALLOWED_TRANSITIONS.getOrDefault(task.getStatus(), EnumSet.noneOf(DeliveryStatus.class));
    }
}