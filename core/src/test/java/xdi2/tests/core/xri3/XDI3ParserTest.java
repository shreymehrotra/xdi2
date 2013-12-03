package xdi2.tests.core.xri3;


import junit.framework.TestCase;
import xdi2.core.constants.XDIConstants;
import xdi2.core.xri3.XDI3Parser;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;
import xdi2.core.xri3.XDI3SubSegment;
import xdi2.core.xri3.XDI3XRef;
import xdi2.core.xri3.parser.manual.ParserException;

public abstract class XDI3ParserTest extends TestCase {

	public void testBasic() throws Exception {

		XDI3Parser parser = this.getParser();

		XDI3Statement statement = parser.parseXDI3Statement("=markus[<+email>]!1&/&/\"xxx\"");

		assertEquals(statement.getSubject(), parser.parseXDI3Segment("=markus[<+email>]!1&"));
		assertEquals(statement.getPredicate(), parser.parseXDI3Segment("&"));
		assertEquals(statement.getObject(), "xxx");

		assertEquals(statement.getContextNodeXri(), parser.parseXDI3Segment("=markus[<+email>]!1&"));
		assertNull(statement.getContextNodeArcXri());
		assertNull(statement.getTargetContextNodeXri());
		assertEquals(statement.getLiteralData(), "xxx");

		assertEquals(statement.getSubject().getNumSubSegments(), 4);
		assertEquals(statement.getSubject().getSubSegment(0), statement.getSubject().getFirstSubSegment());
		assertEquals(statement.getSubject().getSubSegment(3), statement.getSubject().getLastSubSegment());
		assertEquals(statement.getSubject().getSubSegment(0), parser.parseXDI3SubSegment("=markus"));
		assertEquals(statement.getSubject().getSubSegment(0).getCs(), XDIConstants.CS_EQUALS);
		assertEquals(statement.getSubject().getSubSegment(0).getLiteral(), "markus");
		assertNull(statement.getSubject().getSubSegment(0).getXRef());
		assertEquals(statement.getSubject().getSubSegment(1), parser.parseXDI3SubSegment("[<+email>]"));
		assertEquals(statement.getSubject().getSubSegment(1).getCs(), XDIConstants.CS_PLUS);
		assertTrue(statement.getSubject().getSubSegment(1).isClassXs());
		assertTrue(statement.getSubject().getSubSegment(1).isAttributeXs());
		assertEquals(statement.getSubject().getSubSegment(1).getLiteral(), "email");
		assertNull(statement.getSubject().getSubSegment(1).getXRef());
		assertEquals(statement.getSubject().getSubSegment(2), parser.parseXDI3SubSegment("!1"));
		assertEquals(statement.getSubject().getSubSegment(2).getCs(), XDIConstants.CS_BANG);
		assertFalse(statement.getSubject().getSubSegment(2).isClassXs());
		assertFalse(statement.getSubject().getSubSegment(2).isAttributeXs());
		assertEquals(statement.getSubject().getSubSegment(2).getLiteral(), "1");
		assertNull(statement.getSubject().getSubSegment(2).getXRef());
		assertEquals(statement.getSubject().getSubSegment(3), parser.parseXDI3SubSegment("&"));
		assertEquals(statement.getSubject().getSubSegment(3).getCs(), XDIConstants.CS_VALUE);
		assertFalse(statement.getSubject().getSubSegment(3).isClassXs());
		assertFalse(statement.getSubject().getSubSegment(3).isAttributeXs());
		assertNull(statement.getSubject().getSubSegment(3).getLiteral());
		assertNull(statement.getSubject().getSubSegment(3).getXRef());

		assertEquals(statement.getPredicate().getNumSubSegments(), 1);
		assertEquals(statement.getPredicate().getSubSegment(0), statement.getPredicate().getFirstSubSegment());
		assertEquals(statement.getPredicate().getSubSegment(0), statement.getPredicate().getLastSubSegment());
		assertEquals(statement.getPredicate().getSubSegment(0), parser.parseXDI3SubSegment("&"));
	}

	public void testBasicXRef() throws Exception {

		XDI3Parser parser = this.getParser();

		XDI3Segment segment = parser.parseXDI3Segment("+(user)<+(first_name)>");

		assertEquals(segment.getNumSubSegments(), 2);
		assertEquals(segment.getSubSegment(0), parser.parseXDI3SubSegment("+(user)"));
		assertEquals(segment.getSubSegment(1), parser.parseXDI3SubSegment("<+(first_name)>"));
	}

	public void testXDI3Statement() throws Exception {

		XDI3Parser parser = this.getParser();

		XDI3Statement statement;

		statement = parser.parseXDI3Statement("=markus<+email>&/&/\"markus.sabadello@gmail.com\"");
		assertEquals(statement.getSubject(), parser.parseXDI3Segment("=markus<+email>&"));
		assertEquals(statement.getPredicate(), parser.parseXDI3Segment("&"));
		assertTrue(statement.getObject() instanceof String);
		assertEquals(statement.getObject(), "markus.sabadello@gmail.com");
		assertFalse(statement.isContextNodeStatement());
		assertTrue(statement.isLiteralStatement());
		assertFalse(statement.isRelationStatement());

		statement = parser.parseXDI3Statement("=markus/+friend/=neustar*animesh");
		assertEquals(statement.getSubject(), parser.parseXDI3Segment("=markus"));
		assertEquals(statement.getPredicate(), parser.parseXDI3Segment("+friend"));
		assertEquals(statement.getObject(), parser.parseXDI3Segment("=neustar*animesh"));
		assertFalse(statement.isContextNodeStatement());
		assertFalse(statement.isLiteralStatement());
		assertTrue(statement.isRelationStatement());

		statement = parser.parseXDI3Statement("=neustar*animesh<+email>&/&/\"animesh@gmail.com\"");
		assertEquals(statement.getSubject(), parser.parseXDI3Segment("=neustar*animesh<+email>&"));
		assertEquals(statement.getPredicate(), parser.parseXDI3Segment("&"));
		assertEquals(statement.getObject(), "animesh@gmail.com");
		assertFalse(statement.isContextNodeStatement());
		assertTrue(statement.isLiteralStatement());
		assertFalse(statement.isRelationStatement());

		statement = parser.parseXDI3Statement("=neustar*animesh<+age>&/&/99");
		assertEquals(statement.getSubject(), parser.parseXDI3Segment("=neustar*animesh<+age>&"));
		assertEquals(statement.getPredicate(), parser.parseXDI3Segment("&"));
		assertEquals(statement.getObject(), Double.valueOf(99));
		assertFalse(statement.isContextNodeStatement());
		assertTrue(statement.isLiteralStatement());
		assertFalse(statement.isRelationStatement());

		statement = parser.parseXDI3Statement("=neustar*animesh<+smoker>&/&/false");
		assertEquals(statement.getSubject(), parser.parseXDI3Segment("=neustar*animesh<+smoker>&"));
		assertEquals(statement.getPredicate(), parser.parseXDI3Segment("&"));
		assertEquals(statement.getObject(), Boolean.valueOf(false));
		assertFalse(statement.isContextNodeStatement());
		assertTrue(statement.isLiteralStatement());
		assertFalse(statement.isRelationStatement());

		statement = parser.parseXDI3Statement("=neustar*animesh<+color>&/&/null");
		assertEquals(statement.getSubject(), parser.parseXDI3Segment("=neustar*animesh<+color>&"));
		assertEquals(statement.getPredicate(), parser.parseXDI3Segment("&"));
		assertNull(statement.getObject());
		assertFalse(statement.isContextNodeStatement());
		assertTrue(statement.isLiteralStatement());
		assertFalse(statement.isRelationStatement());

		try {

			statement = parser.parseXDI3Statement("=neustar*animesh<+err>&/&/test");
			fail();
		} catch (ParserException ex) {

		}

		statement = parser.parseXDI3Statement("=neustar*animesh/+friend/=markus");
		assertEquals(statement.getSubject(), parser.parseXDI3Segment("=neustar*animesh"));
		assertEquals(statement.getPredicate(), parser.parseXDI3Segment("+friend"));
		assertEquals(statement.getObject(), parser.parseXDI3Segment("=markus"));
		assertFalse(statement.isContextNodeStatement());
		assertFalse(statement.isLiteralStatement());
		assertTrue(statement.isRelationStatement());
	}

	public void testXDI3XRef() throws Exception {

		XDI3Parser parser = this.getParser();

		XDI3XRef xref;

		xref = parser.parseXDI3XRef("()");
		assertTrue(xref.isEmpty());

		xref = parser.parseXDI3XRef("(=markus)");
		assertFalse(xref.isEmpty());
		assertEquals(parser.parseXDI3Segment("=markus"), xref.getSegment());

		xref = parser.parseXDI3XRef("(=markus/$add)");
		assertFalse(xref.isEmpty());
		assertEquals(parser.parseXDI3Segment("=markus"), xref.getPartialSubject());
		assertEquals(parser.parseXDI3Segment("$add"), xref.getPartialPredicate());

		xref = parser.parseXDI3XRef("(=markus/+friend/=drummond)");
		assertFalse(xref.isEmpty());
		assertEquals(parser.parseXDI3Statement("=markus/+friend/=drummond"), xref.getStatement());

		xref = parser.parseXDI3XRef("(data:,markus.sabadello@gmail.com)");
		assertFalse(xref.isEmpty());
		assertEquals("data:,markus.sabadello@gmail.com", xref.getIri());

		xref = parser.parseXDI3XRef("(email)");
		assertFalse(xref.isEmpty());
		assertEquals("email", xref.getLiteral());
	}

	public void testLiteralXRef() {

		XDI3Parser parser = this.getParser();

		XDI3SubSegment s;

		s = parser.parseXDI3SubSegment("{[<+(name)>]}");
		assertTrue(s.hasXRef());
		assertEquals(s.getXRef(), parser.parseXDI3XRef("{[<+(name)>]}"));
		assertEquals(s.getXRef().getXs(), XDIConstants.XS_VARIABLE);
		assertTrue(s.getXRef().hasSegment());
		assertEquals(s.getXRef().getSegment(), parser.parseXDI3Segment("[<+(name)>]"));
		assertEquals(s.getXRef().getSegment().getNumSubSegments(), 1);
		assertEquals(s.getXRef().getSegment().getFirstSubSegment(), parser.parseXDI3SubSegment("[<+(name)>]"));
		assertTrue(s.getXRef().getSegment().getFirstSubSegment().isClassXs());
		assertTrue(s.getXRef().getSegment().getFirstSubSegment().isAttributeXs());
		assertTrue(s.getXRef().getSegment().getFirstSubSegment().hasXRef());
		assertEquals(s.getXRef().getSegment().getFirstSubSegment().getXRef(), parser.parseXDI3XRef("(name)"));
		assertEquals(s.getXRef().getSegment().getFirstSubSegment().getXRef().getXs(), XDIConstants.XS_ROOT);
		assertTrue(s.getXRef().getSegment().getFirstSubSegment().getXRef().hasLiteral());
		assertEquals(s.getXRef().getSegment().getFirstSubSegment().getXRef().getLiteral(), "name");
	}

	public void testComponents() throws Exception {

		XDI3Statement contextNodeStatement = XDI3Statement.create("=markus//[<+email>]");
		XDI3Statement contextNodeStatement2 = XDI3Statement.fromComponents(XDI3Segment.create("=markus"), XDIConstants.XRI_S_CONTEXT, XDI3SubSegment.create("[<+email>]"));
		XDI3Statement contextNodeStatement3 = XDI3Statement.fromContextNodeComponents(XDI3Segment.create("=markus"), XDI3SubSegment.create("[<+email>]"));

		assertEquals(contextNodeStatement.getSubject(), XDI3Segment.create("=markus"));
		assertEquals(contextNodeStatement.getPredicate(), XDIConstants.XRI_S_CONTEXT);
		assertEquals(contextNodeStatement.getObject(), XDI3SubSegment.create("[<+email>]"));

		assertEquals(contextNodeStatement, contextNodeStatement2);
		assertEquals(contextNodeStatement, contextNodeStatement3);

		XDI3Statement relationStatement = XDI3Statement.create("=markus/+friend/=animesh");
		XDI3Statement relationStatement2 = XDI3Statement.fromComponents(XDI3Segment.create("=markus"), XDI3Segment.create("+friend"), XDI3Segment.create("=animesh"));
		XDI3Statement relationStatement3 = XDI3Statement.fromRelationComponents(XDI3Segment.create("=markus"), XDI3Segment.create("+friend"), XDI3Segment.create("=animesh"));

		assertEquals(relationStatement, relationStatement2);
		assertEquals(relationStatement, relationStatement3);

		assertEquals(relationStatement.getSubject(), XDI3Segment.create("=markus"));
		assertEquals(relationStatement.getPredicate(), XDI3Segment.create("+friend"));
		assertEquals(relationStatement.getObject(), XDI3Segment.create("=animesh"));

		XDI3Statement literalStatement = XDI3Statement.create("=markus<+name>&/&/\"Markus Sabadello\"");
		XDI3Statement literalStatement2 = XDI3Statement.fromComponents(XDI3Segment.create("=markus<+name>&"), XDIConstants.XRI_S_LITERAL, "Markus Sabadello");
		XDI3Statement literalStatement3 = XDI3Statement.fromLiteralComponents(XDI3Segment.create("=markus<+name>&"), "Markus Sabadello");

		assertEquals(literalStatement.getSubject(), XDI3Segment.create("=markus<+name>&"));
		assertEquals(literalStatement.getPredicate(), XDIConstants.XRI_S_LITERAL);
		assertEquals(literalStatement.getObject(), "Markus Sabadello");

		assertEquals(literalStatement, literalStatement2);
		assertEquals(literalStatement, literalStatement3);
	}

	public void testInnerRootNotation() throws Exception {

		XDI3Statement statement1 = XDI3Statement.create("=a/+b/(=x/+y/=z)");
		XDI3Statement statement2 = XDI3Statement.create("(=a/+b)=x/+y/(=a/+b)=z");

		assertTrue(statement1.isInnerRootNotation());
		assertEquals(statement1.getInnerRootNotationStatement(), XDI3Statement.create("=x/+y/=z"));

		assertFalse(statement2.isInnerRootNotation());
		assertNull(statement2.getInnerRootNotationStatement());

		assertEquals(statement1.fromInnerRootNotation(false), statement2);
		assertEquals(statement1.fromInnerRootNotation(true), statement2);
		assertEquals(statement2.toInnerRootNotation(false), statement1);
		assertEquals(statement2.toInnerRootNotation(true), statement1);

		assertEquals(statement1.toInnerRootNotation(false), statement1);
		assertEquals(statement1.toInnerRootNotation(true), statement1);
		assertEquals(statement2.fromInnerRootNotation(false), statement2);
		assertEquals(statement2.fromInnerRootNotation(true), statement2);

		assertEquals(statement1.fromInnerRootNotation(false).toInnerRootNotation(false), statement1);
		assertEquals(statement1.fromInnerRootNotation(true).toInnerRootNotation(true), statement1);
		assertEquals(statement2.toInnerRootNotation(false).fromInnerRootNotation(false), statement2);
		assertEquals(statement2.toInnerRootNotation(true).fromInnerRootNotation(true), statement2);

		XDI3Statement statement3 = XDI3Statement.create("=a/+b/(=x/+y/(=mm/+nn/=oo))");
		XDI3Statement statement4 = XDI3Statement.create("(=a/+b)(=x/+y)=mm/+nn/(=a/+b)(=x/+y)=oo");

		assertTrue(statement3.isInnerRootNotation());
		assertTrue(statement3.getInnerRootNotationStatement().isInnerRootNotation());
		assertEquals(statement3.getInnerRootNotationStatement(), XDI3Statement.create("=x/+y/(=mm/+nn/=oo)"));
		assertEquals(statement3.getInnerRootNotationStatement().getInnerRootNotationStatement(), XDI3Statement.create("=mm/+nn/=oo"));

		assertFalse(statement4.isInnerRootNotation());
		assertNull(statement4.getInnerRootNotationStatement());

		assertNotEquals(statement3.fromInnerRootNotation(false), statement4);
		assertEquals(statement3.fromInnerRootNotation(false).fromInnerRootNotation(false), statement4);
		assertEquals(statement3.fromInnerRootNotation(true), statement4);
		assertNotEquals(statement4.toInnerRootNotation(false), statement3);
		assertEquals(statement4.toInnerRootNotation(false).toInnerRootNotation(false), statement3);
		assertEquals(statement4.toInnerRootNotation(true), statement3);

		assertEquals(statement3.toInnerRootNotation(false), statement3);
		assertEquals(statement3.toInnerRootNotation(true), statement3);
		assertEquals(statement4.fromInnerRootNotation(false), statement4);
		assertEquals(statement4.fromInnerRootNotation(true), statement4);

		assertEquals(statement3.fromInnerRootNotation(false).toInnerRootNotation(false), statement3);
		assertEquals(statement3.fromInnerRootNotation(false).fromInnerRootNotation(false).toInnerRootNotation(false).toInnerRootNotation(false), statement3);
		assertEquals(statement3.fromInnerRootNotation(true).toInnerRootNotation(true), statement3);
		assertEquals(statement4.toInnerRootNotation(false).fromInnerRootNotation(false), statement4);
		assertEquals(statement4.toInnerRootNotation(false).toInnerRootNotation(false).fromInnerRootNotation(false).fromInnerRootNotation(false), statement4);
		assertEquals(statement4.toInnerRootNotation(true).fromInnerRootNotation(true), statement4);

		assertEquals(statement3.fromInnerRootNotation(false), statement4.toInnerRootNotation(false));

		XDI3Statement statement5 = XDI3Statement.create("=a/+b/(=x/+y/(=mm/+nn/(=oo/+pp/=qq)))");
		XDI3Statement statement6 = XDI3Statement.create("(=a/+b)(=x/+y)(=mm/+nn)=oo/+pp/(=a/+b)(=x/+y)(=mm/+nn)=qq");

		assertTrue(statement5.isInnerRootNotation());
		assertTrue(statement5.getInnerRootNotationStatement().isInnerRootNotation());
		assertEquals(statement5.getInnerRootNotationStatement(), XDI3Statement.create("=x/+y/(=mm/+nn/(=oo/+pp/=qq))"));
		assertEquals(statement5.getInnerRootNotationStatement().getInnerRootNotationStatement(), XDI3Statement.create("=mm/+nn/(=oo/+pp/=qq)"));
		assertEquals(statement5.getInnerRootNotationStatement().getInnerRootNotationStatement().getInnerRootNotationStatement(), XDI3Statement.create("=oo/+pp/=qq"));

		assertFalse(statement6.isInnerRootNotation());
		assertNull(statement6.getInnerRootNotationStatement());

		assertNotEquals(statement5.fromInnerRootNotation(false), statement6);
		assertNotEquals(statement5.fromInnerRootNotation(false).fromInnerRootNotation(false), statement6);
		assertEquals(statement5.fromInnerRootNotation(false).fromInnerRootNotation(false).fromInnerRootNotation(false), statement6);
		assertEquals(statement5.fromInnerRootNotation(true), statement6);
		assertNotEquals(statement6.toInnerRootNotation(false), statement5);
		assertNotEquals(statement6.toInnerRootNotation(false).toInnerRootNotation(false), statement5);
		assertEquals(statement6.toInnerRootNotation(false).toInnerRootNotation(false).toInnerRootNotation(false), statement5);
		assertEquals(statement6.toInnerRootNotation(true), statement5);

		assertEquals(statement5.toInnerRootNotation(false), statement5);
		assertEquals(statement5.toInnerRootNotation(true), statement5);
		assertEquals(statement6.fromInnerRootNotation(false), statement6);
		assertEquals(statement6.fromInnerRootNotation(true), statement6);

		assertEquals(statement5.fromInnerRootNotation(false).toInnerRootNotation(false), statement5);
		assertEquals(statement5.fromInnerRootNotation(false).fromInnerRootNotation(false).toInnerRootNotation(false).toInnerRootNotation(false), statement5);
		assertEquals(statement5.fromInnerRootNotation(false).fromInnerRootNotation(false).fromInnerRootNotation(false).toInnerRootNotation(false).toInnerRootNotation(false).toInnerRootNotation(false), statement5);
		assertEquals(statement5.fromInnerRootNotation(true).toInnerRootNotation(true), statement5);
		assertEquals(statement6.toInnerRootNotation(false).fromInnerRootNotation(false), statement6);
		assertEquals(statement6.toInnerRootNotation(false).toInnerRootNotation(false).fromInnerRootNotation(false).fromInnerRootNotation(false), statement6);
		assertEquals(statement6.toInnerRootNotation(false).toInnerRootNotation(false).toInnerRootNotation(false).fromInnerRootNotation(false).fromInnerRootNotation(false).fromInnerRootNotation(false), statement6);
		assertEquals(statement6.toInnerRootNotation(true).fromInnerRootNotation(true), statement6);

		assertEquals(statement5.fromInnerRootNotation(false).fromInnerRootNotation(false), statement6.toInnerRootNotation(false));
		assertEquals(statement5.fromInnerRootNotation(false), statement6.toInnerRootNotation(false).toInnerRootNotation(false));
	}

	public abstract XDI3Parser getParser();

	private static void assertNotEquals(Object o1, Object o2) throws Exception {

		assertFalse(o1.equals(o2));
	}
}
