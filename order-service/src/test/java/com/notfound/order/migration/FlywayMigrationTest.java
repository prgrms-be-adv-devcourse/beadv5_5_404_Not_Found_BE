package com.notfound.order.migration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flyway 마이그레이션 스모크 테스트.
 * H2 PostgreSQL 모드에서 V1 스크립트가 정상 실행되고
 * Hibernate validate가 통과하는지 검증합니다.
 */
@Tag("flyway")
@SpringBootTest
@ActiveProfiles("flyway-test")
class FlywayMigrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Flyway 마이그레이션 후 orders 테이블 존재 확인")
    void migrationCreatesOrdersTable() throws Exception {
        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, "ORDERS", null)) {
            assertThat(rs.next()).isTrue();
        }
    }

    @Test
    @DisplayName("Flyway 마이그레이션 후 version 컬럼 존재 확인")
    void migrationCreatesVersionColumn() throws Exception {
        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getColumns(null, null, "ORDERS", "VERSION")) {
            assertThat(rs.next()).isTrue();
        }
    }

    @Test
    @DisplayName("Flyway 마이그레이션 후 address_id 컬럼 존재 확인")
    void migrationCreatesAddressIdColumn() throws Exception {
        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getColumns(null, null, "ORDERS", "ADDRESS_ID")) {
            assertThat(rs.next()).isTrue();
        }
    }
}
