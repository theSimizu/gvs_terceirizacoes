package com.h_group.gvs_terceirizacoes;

import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleSchemaMigrationTest {
    @Test
    void oldAndNewAccountsDefaultToUserAfterMigration() throws Exception {
        try (var db = DriverManager.getConnection("jdbc:h2:mem:migration-" + UUID.randomUUID() + ";MODE=MySQL", "sa", "");
                var statement = db.createStatement();
                var resource = getClass().getResourceAsStream("/db/manual/V001__user_roles.sql")) {
            assertNotNull(resource);
            statement.execute("CREATE TABLE users (id INT PRIMARY KEY)");
            statement.execute("INSERT INTO users (id) VALUES (1)");
            statement.execute(new String(resource.readAllBytes(), StandardCharsets.UTF_8));
            statement.execute("INSERT INTO users (id) VALUES (2)");
            try (var rows = statement.executeQuery("SELECT role FROM users ORDER BY id")) {
                assertTrue(rows.next()); assertEquals("USER", rows.getString(1));
                assertTrue(rows.next()); assertEquals("USER", rows.getString(1));
            }
        }
    }
}
