package lk.ac.ruh.fot.ams.model;

import java.time.LocalDateTime;

import lk.ac.ruh.fot.ams.model.enums.NoticeTarget;

/** [COMMON PART] A notice published by the admin. */
public class Notice {

    private int noticeId;
    private String title;
    private String description;
    private Integer createdBy;
    private String createdByName;
    private NoticeTarget targetRole = NoticeTarget.ALL;
    private LocalDateTime createdAt;
    private String status = "ACTIVE";

    public int getNoticeId() { return noticeId; }
    public void setNoticeId(int noticeId) { this.noticeId = noticeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public NoticeTarget getTargetRole() { return targetRole; }
    public void setTargetRole(NoticeTarget targetRole) { this.targetRole = targetRole; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
