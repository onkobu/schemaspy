/*
 * Copyright (C) 2018 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.integrationtesting.mariadb;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schemaspy.Config;
import org.schemaspy.IntegrationTestFixture;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.integrationtesting.MysqlSuite;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.testcontainers.containers.MySQLContainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;

/**
 * @author Nils Petzaell
 */
@Ignore
public class MariaDbRoutinesIT {

	@Mock
	private ProgressListener progressListener;

	private IntegrationTestFixture fixture;

	private static Database database;

	@SuppressWarnings("unchecked")
	@ClassRule
	public static JdbcContainerRule<MySQLContainer<?>> jdbcContainerRule = new SuiteOrTestJdbcContainerRule<MySQLContainer<?>>(
			MysqlSuite.jdbcContainerRule,
			new JdbcContainerRule<MySQLContainer<?>>(() -> new MySQLContainer<>("mariadb:10.2")).assumeDockerIsPresent()
					.withAssumptions(assumeDriverIsPresent())
					.withInitScript("integrationTesting/mysql/dbScripts/routinesit.sql").withInitUser("root", "test"));

	@Before
	public synchronized void createDatabaseRepresentation() throws SQLException, IOException {
		MockitoAnnotations.openMocks(this);
		if (database == null) {
			doCreateDatabaseRepresentation();
		}
	}

	private void doCreateDatabaseRepresentation() throws SQLException, IOException {
		String[] args = { "-t", "mariadb", "-db", "routinesit", "-s", "routinesit", "-cat", "%", "-o",
				"target/testout/integrationtesting/mariadb/routines", "-u", "test", "-p", "test", "-host",
				jdbcContainerRule.getContainer().getContainerIpAddress(), "-port",
				jdbcContainerRule.getContainer().getMappedPort(3306).toString() };
		fixture = IntegrationTestFixture.fromArgs(args);
		CommandLineArguments arguments = fixture.commandLineArguments();
		final SqlService sqlService = fixture.sqlService();

		Config config = new Config(args);
		sqlService.connect(config);
		Database database = new Database(sqlService.getDbmsMeta(), arguments.getDatabaseName(), arguments.getCatalog(),
				arguments.getSchema());
		new DatabaseServiceFactory(sqlService).simple(config).gatherSchemaDetails(database, null, progressListener);
		MariaDbRoutinesIT.database = database;
	}

	@Test
	public void databaseShouldExist() {
		assertThat(database).isNotNull();
		assertThat(database.getName()).isEqualToIgnoringCase("routinesit");
	}

	@Test
	public void databaseShouldHaveRoutines() {
		assertThat(database.getRoutinesMap().get("no_det").isDeterministic()).isFalse();
		assertThat(database.getRoutinesMap().get("yes_det").isDeterministic()).isTrue();
	}
}
