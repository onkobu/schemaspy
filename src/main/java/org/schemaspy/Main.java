/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Ismail Simsek
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2018 Nils Petzaell
 *
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy;

import java.lang.invoke.MethodHandles;

import org.schemaspy.cli.SchemaSpyRunner;
import org.schemaspy.logging.StackTraceOmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Currier
 * @author Rafal Kasa
 * @author Wojciech Kasa
 * @author Thomas Traude
 * @author Ismail Simsek
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static void main(String... args) {
		SchemaSpyRunner schemaSpyRunner = new SchemaSpyRunner();
		schemaSpyRunner.run(args);
		if (StackTraceOmitter.hasOmittedStackTrace()) {
			LOGGER.info("StackTraces have been omitted, use `-debug` when executing SchemaSpy to see them");
		}
		System.exit(schemaSpyRunner.getExitCode());
	}

}