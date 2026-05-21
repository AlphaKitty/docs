package com.expertlink.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Patch legacy schema differences that Hibernate ddl-auto does not evolve,
 * e.g. MySQL ENUM value set changes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSchemaPatchRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        patchEngagementRequestStatusEnum();
        patchEngagementRequestModeEnum();
        patchEngagementRequestSelfPickColumns();
    }

    private void patchEngagementRequestStatusEnum() {
        String currentType = jdbcTemplate.query(
                """
                SELECT COLUMN_TYPE
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'engagement_requests'
                  AND COLUMN_NAME = 'status'
                """,
                rs -> rs.next() ? rs.getString(1) : null
        );

        if (currentType == null) {
            log.warn("Skip schema patch: engagement_requests.status not found");
            return;
        }
        if (currentType.contains("'CANCELLED'")) {
            return;
        }

        log.warn("Applying schema patch: add CANCELLED to engagement_requests.status enum");
        jdbcTemplate.execute(
                """
                ALTER TABLE engagement_requests
                MODIFY COLUMN status ENUM(
                  'DRAFT',
                  'PENDING_STEWARD_ASSIGN',
                  'PENDING_EXPERT_CONFIRM',
                  'IN_PROGRESS',
                  'PENDING_STEWARD_SCORE_RELEASE',
                  'COMPLETED',
                  'REJECTED',
                  'CANCELLED'
                ) NOT NULL
                """
        );
        log.info("Schema patch applied: engagement_requests.status supports CANCELLED");
    }

    private void patchEngagementRequestModeEnum() {
        String currentType = jdbcTemplate.query(
                """
                SELECT COLUMN_TYPE
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'engagement_requests'
                  AND COLUMN_NAME = 'mode'
                """,
                rs -> rs.next() ? rs.getString(1) : null
        );

        if (currentType == null) {
            log.warn("Skip schema patch: engagement_requests.mode not found");
            return;
        }
        if (currentType.contains("'SELF'")) {
            return;
        }

        log.warn("Applying schema patch: add SELF to engagement_requests.mode enum");
        jdbcTemplate.execute(
                """
                ALTER TABLE engagement_requests
                MODIFY COLUMN mode ENUM(
                  'NAMED',
                  'STEWARD_ASSIGN',
                  'SELF'
                ) NOT NULL
                """
        );
        log.info("Schema patch applied: engagement_requests.mode supports SELF");
    }

    private void patchEngagementRequestSelfPickColumns() {
        boolean hasApplyCategory = jdbcTemplate.query(
                """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'engagement_requests'
                  AND COLUMN_NAME = 'apply_category'
                """,
                rs -> rs.next() && rs.getInt(1) > 0
        );
        if (hasApplyCategory) {
            return;
        }
        log.warn("Applying schema patch: add self-pick columns to engagement_requests");
        jdbcTemplate.execute("ALTER TABLE engagement_requests ADD COLUMN apply_category VARCHAR(20)");
        jdbcTemplate.execute("ALTER TABLE engagement_requests ADD COLUMN points_category VARCHAR(20)");
        jdbcTemplate.execute("ALTER TABLE engagement_requests ADD COLUMN points_item VARCHAR(30)");
        log.info("Schema patch applied: self-pick columns added to engagement_requests");
    }
}
