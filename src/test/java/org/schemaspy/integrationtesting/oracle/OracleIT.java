/*
 * Copyright (C) 2017, 2018 Nils Petzaell
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
package org.schemaspy.integrationtesting.oracle;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import javax.script.ScriptException;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schemaspy.Config;
import org.schemaspy.IntegrationTestFixture;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.testing.AssumeClassIsPresentRule;
import org.testcontainers.containers.OracleContainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;

/**
 * @author Nils Petzaell
 */
public class OracleIT {

	private static final Path outputPath = Paths.get("target", "testout", "integrationtesting", "oracle", "oracle");

	@Mock
	private ProgressListener progressListener;

	private IntegrationTestFixture fixture;

	private static Database database;

	public static TestRule jdbcDriverClassPresentRule = new AssumeClassIsPresentRule("oracle.jdbc.OracleDriver");

	@SuppressWarnings("unchecked")
	public static JdbcContainerRule<OracleContainer> jdbcContainerRule = new JdbcContainerRule<>(
			() -> new OracleContainer("gvenzl/oracle-xe:11").usingSid()).assumeDockerIsPresent()
					.withAssumptions(assumeDriverIsPresent())
					.withInitScript("integrationTesting/oracle/dbScripts/oracle.sql");

	@ClassRule
	public static final TestRule chain = RuleChain.outerRule(jdbcContainerRule).around(jdbcDriverClassPresentRule);

	@Before
	public synchronized void gatheringSchemaDetailsTest()
			throws SQLException, IOException, ScriptException, URISyntaxException {
		MockitoAnnotations.openMocks(this);
		if (database == null) {
			createDatabaseRepresentation();
		}
	}

	private void createDatabaseRepresentation() throws SQLException, IOException {
		String[] args = { "-t", "orathin", "-db", jdbcContainerRule.getContainer().getSid(), "-s", "ORAIT", "-cat", "%",
				"-o", outputPath.toString(), "-u", "orait", "-p", "orait123", "-host",
				jdbcContainerRule.getContainer().getContainerIpAddress(), "-port",
				jdbcContainerRule.getContainer().getOraclePort().toString() };
		fixture = IntegrationTestFixture.fromArgs(args);
		CommandLineArguments arguments = fixture.commandLineArguments();
		final SqlService sqlService = fixture.sqlService();

		Config config = new Config(args);
		sqlService.connect(config);
		Database database = new Database(sqlService.getDbmsMeta(), arguments.getDatabaseName(), arguments.getCatalog(),
				arguments.getSchema());
		new DatabaseServiceFactory(sqlService).simple(config).gatherSchemaDetails(database, null, progressListener);
		OracleIT.database = database;
	}

	@Test
	public void databaseShouldBePopulatedWithTableTest() {
		Table table = getTable("TEST");
		assertThat(table).isNotNull();
	}

	@Test
	public void databaseShouldBePopulatedWithTableTestAndHaveColumnName() {
		Table table = getTable("TEST");
		TableColumn column = table.getColumn("NAME");
		assertThat(column).isNotNull();
	}

	@Test
	public void databaseShouldBePopulatedWithTableTestAndHaveColumnNameWithComment() {
		Table table = getTable("TEST");
		TableColumn column = table.getColumn("NAME");
		assertThat(column.getComments()).isEqualToIgnoringCase("the name");
	}

	private Table getTable(String tableName) {
		return database.getTablesMap().get(tableName);
	}
}
