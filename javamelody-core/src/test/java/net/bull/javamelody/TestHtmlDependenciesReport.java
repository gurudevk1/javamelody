/*
 * Copyright 2008-2017 by Emeric Vernat
 *
 *     This file is part of Java Melody.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bull.javamelody;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;

/**
 * Test unitaire de la classe HtmlDependenciesReport.
 * @author Emeric Vernat
 */
public class TestHtmlDependenciesReport {
	/** Check. */
	@Before
	public void setUp() {
		Utils.initialize();
	}

	private static void assertNotEmptyAndClear(StringWriter writer) {
		assertTrue("rapport vide", writer.getBuffer().length() > 0);
		writer.getBuffer().setLength(0);
	}

	/** Test.
	 * @throws IOException e */
	@Test
	public void testDependencies() throws IOException {
		final ServletContext context = createNiceMock(ServletContext.class);
		final String javamelodyDir = "/META-INF/maven/net.bull.javamelody/";
		final String webapp = javamelodyDir + "javamelody-test-webapp/";
		expect(context.getResourcePaths("/META-INF/maven/"))
				.andReturn(Collections.singleton(javamelodyDir)).anyTimes();
		expect(context.getResourcePaths(javamelodyDir)).andReturn(Collections.singleton(webapp))
				.anyTimes();
		expect(context.getResourceAsStream(webapp + "pom.xml"))
				.andReturn(getClass().getResourceAsStream("/pom.xml")).anyTimes();
		final Set<String> dependencies = new LinkedHashSet<String>(
				Arrays.asList("/WEB-INF/lib/jrobin-1.5.9.jar",
						"/WEB-INF/lib/javamelody-core-1.65.0.jar", "/WEB-INF/lib/nothing.jar"));
		expect(context.getResourcePaths("/WEB-INF/lib/")).andReturn(dependencies).anyTimes();
		expect(context.getMajorVersion()).andReturn(2).anyTimes();
		expect(context.getMinorVersion()).andReturn(5).anyTimes();
		replay(context);
		Parameters.initialize(context);
		final Map<String, MavenArtifact> webappDependencies = MavenArtifact.getWebappDependencies();
		assertFalse("getWebappDependencies", webappDependencies.isEmpty());
		verify(context);

		final StringWriter writer = new StringWriter();
		final HtmlDependenciesReport htmlDependenciesReport = new HtmlDependenciesReport(
				webappDependencies, writer);
		htmlDependenciesReport.toHtml();
		assertNotEmptyAndClear(writer);
	}
}
