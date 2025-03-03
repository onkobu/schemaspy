/*
 * Copyright (C) 2019 Nils Petzaell
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
package org.schemaspy.integrationtesting;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;

import com.beust.jcommander.IDefaultProvider;

/**
 * @author Nils Petzaell
 */
public class SqliteIT {

	private SqlService sqlService = new SqlService();

	@Mock
	private ProgressListener progressListener;

	@Mock
	private IDefaultProvider defaultProvider;

	private CommandLineArgumentParser commandLineArgumentParser;

	private static Database database;

	@Before
	public synchronized void createDatabaseRepresentation() throws SQLException, IOException {
		MockitoAnnotations.openMocks(this);
		commandLineArgumentParser = new CommandLineArgumentParser(new CommandLineArguments(), defaultProvider);
		if (database == null) {
			doCreateDatabaseRepresentation();
		}
	}

	private void doCreateDatabaseRepresentation() throws SQLException, IOException {
		String[] args = { "-t", "sqlite-xerial", "-db",
				"src/test/resources/integrationTesting/sqlite/database/chinook.db", "-s", "chinook", "-cat", "chinook",
				"-o", "target/integrationtesting/sqlite", "-sso" };
		CommandLineArguments arguments = commandLineArgumentParser.parse(args);
		Config config = new Config(args);
		sqlService.connect(config);
		Database database = new Database(sqlService.getDbmsMeta(), arguments.getDatabaseName(), arguments.getCatalog(),
				arguments.getSchema());
		new DatabaseServiceFactory(sqlService).simple(config).gatherSchemaDetails(database, null, progressListener);
		this.database = database;
	}

	@Test
	public void databaseContainsTable() {
		assertThat(database.getTables().size()).isEqualTo(11);
	}

	@Test
	public void databaseTablePlaylistsContainsPrimaryKey() {
		assertThat(database.getTablesMap().get("playlists").getPrimaryColumns().size()).isGreaterThanOrEqualTo(1);
	}
}
