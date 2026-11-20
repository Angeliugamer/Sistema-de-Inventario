package Models;

import Interfaces.Identificable;

import java.time.LocalDateTime;

public class AuditRecord implements Identificable {
    private String id;
    private String timestamp;

    private String actorUserId;
    private String actorUsername;
    private String actorRole;

    private String action;
    private String entityType;
    private String entityId;

    private String beforeData;
    private String afterData;

    public AuditRecord() {
    }

    public AuditRecord(
            String id,
            String actorUserId,
            String actorUsername,
            String actorRole,
            String action,
            String entityType,
            String entityId,
            String beforeData,
            String afterData) {

        this.id = id;
        this.timestamp = LocalDateTime.now().toString();

        this.actorUserId = actorUserId;
        this.actorUsername = actorUsername;
        this.actorRole = actorRole;

        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;

        this.beforeData = beforeData;
        this.afterData = afterData;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getActorUserId() {
        return actorUserId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public String getActorRole() {
        return actorRole;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getBeforeData() {
        return beforeData;
    }

    public String getAfterData() {
        return afterData;
    }
}
