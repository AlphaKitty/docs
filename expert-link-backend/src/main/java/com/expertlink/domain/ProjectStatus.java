package com.expertlink.domain;

public enum ProjectStatus {
    DRAFT("草稿"),
    PLANNING("规划中"),
    IN_PROGRESS("进行中"),
    ON_HOLD("暂停"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    CLOSED("已关闭");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return this.name();
    }
}