package xdi2.core.xri3.parser.manual;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.constants.XDIConstants;
import xdi2.core.impl.AbstractLiteral;
import xdi2.core.xri3.XDI3Parser;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;
import xdi2.core.xri3.XDI3SubSegment;
import xdi2.core.xri3.XDI3XRef;

/**
 * An XRI parser implemented manually in pure Java.
 * This parse has not been automatically generated from an ABNF. 
 */
public class XDI3ParserManual extends XDI3Parser {

	private static final Logger log = LoggerFactory.getLogger(XDI3ParserManual.class);

	@Override
	public XDI3Statement parseXDI3Statement(String string) {

		if (log.isTraceEnabled()) log.trace("Parsing statement: " + string);

		String temp = stripXs(string);

		String[] parts = temp.split("/", -1);
		if (parts.length != 3) throw new ParserException("Invalid statement: " + string + " (wrong number of segments: " + parts.length + ")");
		int split0 = parts[0].length();
		int split1 = parts[1].length();

		String subjectString = string.substring(0, split0);
		String predicateString = string.substring(split0 + 1, split0 + split1 + 1);
		String objectString = string.substring(split0 + split1 + 2);

		XDI3Segment subject = this.parseXDI3Segment(subjectString);
		XDI3Segment predicate = this.parseXDI3Segment(predicateString);

		if (XDIConstants.XRI_S_LITERAL.equals(predicateString)) {

			Object object = this.parseLiteralData(objectString);

			return this.makeXDI3Statement(string, subject, predicate, object);
		} else if (XDIConstants.XRI_S_CONTEXT.equals(predicateString)) {

			XDI3SubSegment object = this.parseXDI3SubSegment(objectString);

			return this.makeXDI3Statement(string, subject, predicate, object);
		} else {

			XDI3Segment object = this.parseXDI3Segment(objectString);

			return this.makeXDI3Statement(string, subject, predicate, object);
		}
	}

	@Override
	public XDI3Segment parseXDI3Segment(String string) {

		if (log.isTraceEnabled()) log.trace("Parsing segment: " + string);

		int start = 0, pos = 0;
		String pair;
		Stack<String> pairs = new Stack<String> ();
		List<XDI3SubSegment> subSegments = new ArrayList<XDI3SubSegment> ();

		while (pos < string.length()) {

			// parse beginning of subsegment

			if (pos < string.length() && (pair = cla(string.charAt(pos))) != null) { pairs.push(pair); pos++; }
			if (pos < string.length() && (pair = att(string.charAt(pos))) != null) { pairs.push(pair); pos++; }
			if (pos < string.length() && cs(string.charAt(pos)) != null) pos++;
			if (pos < string.length() && (pair = xs(string.charAt(pos))) != null) { pairs.push(pair); pos++; }

			// parse to the end of the subsegment

			while (pos < string.length()) {

				// no open pairs?

				if (pairs.isEmpty()) {

					// reached beginning of the next subsegment

					if (cla(string.charAt(pos)) != null) break;
					if (att(string.charAt(pos)) != null) break;
					if (cs(string.charAt(pos)) != null) break;
					if (xs(string.charAt(pos)) != null) break;
				}

				// at least one pair still open?

				if (! pairs.isEmpty()) {

					// new pair being opened?

					pair = cla(string.charAt(pos));
					if (pair == null) pair = att(string.charAt(pos));
					if (pair == null) pair = xs(string.charAt(pos));

					if (pair != null) { 

						pairs.push(pair); 
						pos++; 
						continue;
					}

					// pair being closed?

					if (string.charAt(pos) == pairs.peek().charAt(1)) {

						pairs.pop();
						pos++;
						continue;
					}
				}

				pos++;
			}

			if (! pairs.isEmpty()) throw new ParserException("Missing closing character '" + pairs.peek().charAt(1) + "' at position " + pos + ".");

			subSegments.add(this.parseXDI3SubSegment(string.substring(start, pos)));

			start = pos;
		}

		// done

		return this.makeXDI3Segment(string, subSegments);
	}

	@Override
	public XDI3SubSegment parseXDI3SubSegment(String string) {

		if (log.isTraceEnabled()) log.trace("Parsing subsegment: " + string);

		Character cs = null;
		String cla = null;
		String att = null;
		String literal = null;
		XDI3XRef xref = null;

		int pos = 0, len = string.length();

		// extract class pair

		if (pos < len && (cla = cla(string.charAt(pos))) != null) {

			if (string.charAt(len - 1) != cla.charAt(1)) throw new ParserException("Invalid subsegment: " + string + " (invalid closing '" + cla.charAt(1) + "' character for class at position " + pos + ")");

			pos++; len--;
		}

		// extract attribute pair

		if (pos < len && (att = att(string.charAt(pos))) != null) {

			if (string.charAt(len - 1) != att.charAt(1)) throw new ParserException("Invalid subsegment: " + string + " (invalid closing '" + att.charAt(1) + "' character for attribute at position " + pos + ")");

			pos++; len--;
		}

		// extract cs

		if (pos < len && (cs = cs(string.charAt(pos))) != null) {

			pos++;
		}

		// parse the rest, either xref or literal

		if (pos < len) {

			if (xs(string.charAt(pos)) != null) {

				xref = this.parseXDI3XRef(string.substring(pos, len));
			} else {

				if (pos == 0) throw new ParserException("Invalid subsegment: " + string + " (no context symbol or cross reference)");
				literal = parseLiteral(string.substring(pos, len));
			}
		}

		// done

		return this.makeXDI3SubSegment(string, cs, cla != null, att != null, literal, xref);
	}

	@Override
	public XDI3XRef parseXDI3XRef(String string) {

		if (log.isTraceEnabled()) log.trace("Parsing xref: " + string);

		String xs = xs(string.charAt(0));
		if (xs == null) throw new ParserException("Invalid cross reference: " + string + " (no opening delimiter)");
		if (string.charAt(string.length() - 1) != xs.charAt(1)) throw new ParserException("Invalid cross reference: " + string + " (invalid closing '" + xs.charAt(1) + "' delimiter)");
		if (string.length() == 2) return this.makeXDI3XRef(string, xs, null, null, null, null, null, null);

		String value = string.substring(1, string.length() - 1);

		String temp = stripXs(value);

		XDI3Segment segment = null;
		XDI3Statement statement = null;
		XDI3Segment partialSubject = null;
		XDI3Segment partialPredicate = null;
		String iri = null;
		String literal = null;

		if (isIri(temp)) {

			iri = value;
		} else {

			int segments = StringUtils.countMatches(temp, "/") + 1;

			if (segments == 3) {

				statement = this.parseXDI3Statement(value);
			} else if (segments == 2) {

				String[] parts = temp.split("/");
				int split0 = parts[0].length();

				partialSubject = this.parseXDI3Segment(value.substring(0, split0));
				partialPredicate = this.parseXDI3Segment(value.substring(split0 + 1));
			} else if (cs(value.charAt(0)) != null || cla(value.charAt(0)) != null || att(value.charAt(0)) != null || xs(value.charAt(0)) != null) {

				segment = this.parseXDI3Segment(value);
			} else {

				literal = value;
			}
		}

		// done
		
		return this.makeXDI3XRef(string, xs, segment, statement, partialSubject, partialPredicate, iri, literal);
	}

	public Object parseLiteralData(String string) {

		if (log.isTraceEnabled()) log.trace("Parsing literal data: " + string);

		try {

			return AbstractLiteral.stringToLiteralData(string);
		} catch (Exception ex) {

			throw new ParserException("Invalid literal data: " + string);
		}
	}	

	private static String stripXs(String string) {

		string = stripPattern(string, Pattern.compile(".*(\\([^\\(\\)]*\\)).*"));
		string = stripPattern(string, Pattern.compile(".*(\\{[^\\{\\}]*\\}).*"));
		string = stripPattern(string, Pattern.compile(".*(\"[^\"]*\").*"));

		return string;
	}

	private static String stripPattern(String string, Pattern pattern) {

		String temp = string;

		while (true) {

			Matcher matcher = pattern.matcher(temp);
			if (! matcher.matches()) break;

			StringBuffer newtemp = new StringBuffer();
			newtemp.append(temp.substring(0, matcher.start(1)));
			for (int i=matcher.start(1); i<matcher.end(1); i++) newtemp.append(" ");
			newtemp.append(temp.substring(matcher.end(1)));

			temp = newtemp.toString();
		}

		return temp;
	}

	private static boolean isIri(String string) {

		int indexColon = string.indexOf(':');
		int indexEquals = string.indexOf(XDIConstants.CS_EQUALS.charValue());
		int indexAt = string.indexOf(XDIConstants.CS_AT.charValue());
		int indexPlus = string.indexOf(XDIConstants.CS_PLUS.charValue());
		int indexDollar = string.indexOf(XDIConstants.CS_DOLLAR.charValue());
		int indexStar = string.indexOf(XDIConstants.CS_STAR.charValue());
		int indexBang = string.indexOf(XDIConstants.CS_BANG.charValue());

		if (indexColon == -1) return false;

		if (indexEquals != -1 && indexEquals < indexColon) return false;
		if (indexAt != -1 && indexAt < indexColon) return false;
		if (indexPlus != -1 && indexPlus < indexColon) return false;
		if (indexDollar != -1 && indexDollar < indexColon) return false;
		if (indexStar != -1 && indexStar < indexColon) return false;
		if (indexBang != -1 && indexBang < indexColon) return false;

		return true;
	}

	/*
	 * Helper methods
	 */

	private static Character cs(char c) {

		for (Character cs : XDIConstants.CS_ARRAY) if (cs.charValue() == c) return cs;

		return null;
	}

	private static String cla(char c) {

		if (XDIConstants.XS_CLASS.charAt(0) == c) return XDIConstants.XS_CLASS;

		return null;
	}

	private static String att(char c) {

		if (XDIConstants.XS_ATTRIBUTE.charAt(0) == c) return XDIConstants.XS_ATTRIBUTE;

		return null;
	}

	private static String xs(char c) {

		if (XDIConstants.XS_ROOT.charAt(0) == c) return XDIConstants.XS_ROOT;
		if (XDIConstants.XS_VARIABLE.charAt(0) == c) return XDIConstants.XS_VARIABLE;
		if (XDIConstants.XS_DEFINITION.charAt(0) == c) return XDIConstants.XS_DEFINITION;

		return null;
	}

	private static String parseLiteral(String string) {

		try {

			string = URLDecoder.decode(string, "UTF-8");
		} catch (UnsupportedEncodingException ex) {

			throw new ParserException(ex.getMessage(), ex);
		}

		for (int pos=0; pos<string.length(); pos++) {

			char c = string.charAt(pos);

			if (c >= 0x41 && c <= 0x5A) continue;
			if (c >= 0x61 && c <= 0x7A) continue;
			if (c >= 0x30 && c <= 0x39) continue;
			if (c == '-') continue;
			if (c == '.') continue;
			if (c == ':') continue;
			if (c == '_') continue;
			if (c == '~') continue;
			if (c >= 0xA0 && c <= 0xD7FF) continue;
			if (c >= 0xF900 && c <= 0xFDCF) continue;
			if (c >= 0xFDF0 && c <= 0xFFEF) continue;

			throw new ParserException("Invalid character '" + c + "' at position " + pos + " of literal " + string);
		}

		return string;
	}
}
