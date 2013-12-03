package xdi2.tests.core.util;

import junit.framework.TestCase;
import xdi2.core.Graph;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.util.StatementUtil;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;

public class StatementUtilTest extends TestCase {

	public void testStatementUtil() throws Exception {

		String contextNodeStatements[] = new String[] {
				"=markus//[<+email>]",
				"=markus//",
				"//=markus"
		};

		String relationStatements[] = new String[] {
				"=markus/+friend/=animesh",
				"=markus/$ref/[=]!1111",
				"()/$is$ref/[=]!1111",
				"[=]!1111/$ref/",
				"[=]!1111+tel/+home+fax/[=]!1111+tel!1"
		};

		String literalStatements[] = new String[] {
				"=markus<+name>&/&/\"Markus Sabadello\"",
				"[=]!1111<+tel>&/&/\"+1-206-555-1212\"",
				"[=]!1111<+tel>[1]&/&/\"+1.206.555.1111\""
		};

		String invalidStatements[] = new String[] {
				"=markus&/&/=markus",
				"=markus&/&/{}"
		};

		for (String contextNodeStatement : contextNodeStatements) {

			assertTrue(XDI3Statement.create(contextNodeStatement).isContextNodeStatement());
			assertFalse(XDI3Statement.create(contextNodeStatement).isRelationStatement());
			assertFalse(XDI3Statement.create(contextNodeStatement).isLiteralStatement());
		}

		for (String relationStatement : relationStatements) {

			assertFalse(XDI3Statement.create(relationStatement).isContextNodeStatement());
			assertTrue(XDI3Statement.create(relationStatement).isRelationStatement());
			assertFalse(XDI3Statement.create(relationStatement).isLiteralStatement());
		}

		for (String literalStatement : literalStatements) {

			assertFalse(XDI3Statement.create(literalStatement).isContextNodeStatement());
			assertFalse(XDI3Statement.create(literalStatement).isRelationStatement());
			assertTrue(XDI3Statement.create(literalStatement).isLiteralStatement());
		}

		for (String invalidStatement : invalidStatements) {

			Graph graph = null;

			try {

				graph = MemoryGraphFactory.getInstance().openGraph();
				graph.setStatement(XDI3Statement.create(invalidStatement));

				fail();
			} catch (Exception ex) {

				if (graph != null) graph.close();
			}
		} 
	}

	public void testremoveStartXriStatement() throws Exception {

		XDI3Statement contextStatement = XDI3Statement.create("=markus+full//<+name>");

		XDI3Statement reducedContextStatement = StatementUtil.removeStartXriStatement(contextStatement, XDI3Segment.create("=markus"), false);

		assertEquals(reducedContextStatement, XDI3Statement.create("+full//<+name>"));
		assertEquals(reducedContextStatement.getSubject(), XDI3Segment.create("+full"));
		assertEquals(reducedContextStatement.getPredicate(), XDI3Segment.create(""));
		assertEquals(reducedContextStatement.getObject(), "<+name>");

		assertEquals(StatementUtil.removeStartXriStatement(reducedContextStatement, XDI3Segment.create("{}"), false, false, true), XDI3Statement.create("//<+name>"));

		XDI3Statement literalStatement = XDI3Statement.create("=markus<+name>&/&/\"Markus Sabadello\"");

		XDI3Statement reducedLiteralStatement = StatementUtil.removeStartXriStatement(literalStatement, XDI3Segment.create("=markus"), false);

		assertEquals(reducedLiteralStatement, XDI3Statement.create("<+name>&/&/\"Markus Sabadello\""));
		assertEquals(reducedLiteralStatement.getSubject(), XDI3Segment.create("<+name>&"));
		assertEquals(reducedLiteralStatement.getPredicate(), XDI3Segment.create("&"));
		assertEquals(reducedLiteralStatement.getObject(), "Markus Sabadello");

		assertEquals(StatementUtil.removeStartXriStatement(reducedLiteralStatement, XDI3Segment.create("{}"), false, false, true), XDI3Statement.create("&/&/\"Markus Sabadello\""));
		assertEquals(StatementUtil.removeStartXriStatement(reducedLiteralStatement, XDI3Segment.create("{}{}"), false, false, true), XDI3Statement.create("/&/\"Markus Sabadello\""));

		XDI3Statement relationStatement = XDI3Statement.create("=markus<+name>/$ref/=markus+full<+name>");

		XDI3Statement reducedRelationStatement1 = StatementUtil.removeStartXriStatement(relationStatement, XDI3Segment.create("=markus"), true);

		assertEquals(reducedRelationStatement1, XDI3Statement.create("<+name>/$ref/+full<+name>"));
		assertEquals(reducedRelationStatement1.getSubject(), XDI3Segment.create("<+name>"));
		assertEquals(reducedRelationStatement1.getPredicate(), XDI3Segment.create("$ref"));
		assertEquals(reducedRelationStatement1.getObject(), "+full<+name>");

		assertEquals(StatementUtil.removeStartXriStatement(reducedRelationStatement1, XDI3Segment.create("{}"), true, false, true), XDI3Statement.create("/$ref/<+name>"));

		XDI3Statement reducedRelationStatement2 = StatementUtil.removeStartXriStatement(relationStatement, XDI3Segment.create("=markus"), false);

		assertEquals(reducedRelationStatement2, XDI3Statement.create("<+name>/$ref/=markus+full<+name>"));
		assertEquals(reducedRelationStatement2.getSubject(), XDI3Segment.create("<+name>"));
		assertEquals(reducedRelationStatement2.getPredicate(), XDI3Segment.create("$ref"));
		assertEquals(reducedRelationStatement2.getObject(), "=markus+full<+name>");

		assertEquals(StatementUtil.removeStartXriStatement(reducedRelationStatement2, XDI3Segment.create("{}"), false, false, true), XDI3Statement.create("/$ref/=markus+full<+name>"));
	}
}
