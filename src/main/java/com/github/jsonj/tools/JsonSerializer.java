/**
 * Copyright (c) 2011, Jilles van Gurp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.jsonj.tools;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonType;

/**
 * Utility class to serialize Json.
 */
public class JsonSerializer {
    public static final Charset UTF8=Charset.forName("utf-8");
    public static final byte[] ESCAPED_CARRIAGE_RETURN = "\\r".getBytes(UTF8);
    public static final byte[] ESCAPED_TAB = "\\t".getBytes(UTF8);
    public static final byte[] ESCAPED_BACKSLASH = "\\\\".getBytes(UTF8);
    public static final byte[] ESCAPED_NEWLINE = "\\n".getBytes(UTF8);
    public static final byte[] ESCAPED_QUOTE = "\\\"".getBytes(UTF8);
    public static final byte[] OPEN_BRACKET="[".getBytes(UTF8);
    public static final byte[] CLOSE_BRACKET="]".getBytes(UTF8);
    public static final byte[] OPEN_BRACE="{".getBytes(UTF8);
    public static final byte[] CLOSE_BRACE="}".getBytes(UTF8);
    public static final byte[] COLON=":".getBytes(UTF8);
    public static final byte[] QUOTE="\"".getBytes(UTF8);
    public static final byte[] COMMA=",".getBytes(UTF8);

	private JsonSerializer() {
		// utility class, don't instantiate
	}

	/**
	 * @param json
	 * @return string representation of the json
	 */
	public static String serialize(final JsonElement json) {
		return serialize(json, false);
	}

    public static void serialize(final JsonElement json, OutputStream out) {
        try {
            write(out, json, false);
        } catch (IOException e) {
            throw new IllegalStateException("cannot serialize json to output stream", e);
        }
    }

	/**
	 * @param json
	 * @param pretty if true, a properly indented version of the json is returned
	 * @return string representation of the json
	 */
	public static String serialize(final JsonElement json, final boolean pretty) {
	    if(pretty) {
    		StringWriter sw = new StringWriter();
    		try {
    			write(sw,json,pretty);
    		} catch (IOException e) {
    			throw new IllegalStateException("cannot serialize json to a string", e);
    		} finally {
    			try {
    				sw.close();
    			} catch (IOException e) {
    				throw new IllegalStateException("cannot serialize json to a string", e);
    			}
    		}
    		return sw.getBuffer().toString();
	    } else {
	        try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                BufferedOutputStream buffered = new BufferedOutputStream(bos);
                json.serialize(buffered);
                buffered.flush();
                bos.close();
                return bos.toString("utf8");
            } catch (IOException e) {
                throw new IllegalStateException("cannot serialize json to a string", e);
            }
	    }
	}

	/**
	 * Writes the object out as json.
	 * @param out output channel
	 * @param json
	 * @param pretty if true, a properly indented version of the json is written
	 * @throws IOException
	 */
	public static void write(final Writer out, final JsonElement json, final boolean pretty) throws IOException {
		BufferedWriter bw = new BufferedWriter(out);
		write(bw, json, pretty, 0);
		if(pretty) {
			out.write('\n');
		}
		bw.flush();
	}

	public static void write(final OutputStream out, final JsonElement json, final boolean pretty) throws IOException {
	    if(pretty) {
            write(new OutputStreamWriter(out, Charset.forName("UTF-8")), json, pretty);
        } else {
            BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
            json.serialize(bufferedOut);
            bufferedOut.flush();
        }
	}

	private static void write(final BufferedWriter bw, final JsonElement json, final boolean pretty, final int indent) throws IOException {
		if(json==null) {
            return;
        }
	    JsonType type = json.type();
		switch (type) {
		case object:
			bw.write('{');
			newline(bw, indent+1, pretty);
			Iterator<Entry<String, JsonElement>> iterator = json.asObject().entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, JsonElement> entry = iterator.next();
				String key = entry.getKey();
				JsonElement value = entry.getValue();
				if(value != null) {
    				bw.write('"');
    				bw.write(jsonEscape(key));
    				bw.write("\":");
    				write(bw,value,pretty,indent+1);
    				if(iterator.hasNext()) {
    					bw.write(',');
    					newline(bw, indent+1, pretty);
    				}
				}
			}
			newline(bw, indent, pretty);
			bw.write('}');
			break;
		case array:
			bw.write('[');
			newline(bw, indent+1, pretty);
			Iterator<JsonElement> arrayIterator = json.asArray().iterator();
			while (arrayIterator.hasNext()) {
				JsonElement value = arrayIterator.next();
				boolean nestedPretty=false;
				if(value.isObject()) {
				    nestedPretty=true;
				}
				write(bw,value,nestedPretty,indent+1);
				if(arrayIterator.hasNext()) {
					bw.write(',');
					newline(bw, indent+1, nestedPretty);
				}
			}
			newline(bw, indent, pretty);
			bw.write(']');
			break;
		case string:
			bw.write(json.toString());
			break;
		case bool:
            bw.write(json.toString());
			break;
		case number:
            bw.write(json.toString());
			break;
		case nullValue:
            bw.write(json.toString());
			break;

		default:
			throw new IllegalArgumentException("unhandled type " + type);
		}
	}

    /**
     * The xml specification defines these character hex codes as allowed: #x9 | #xA | #xD | [#x20-#xD7FF] |
     * [#xE000-#xFFFD] | [#x10000-#x10FFFF] Characters outside this range will cause parsers to reject the xml as not
     * well formed. Probably should not allow these in Json either.
     *
     * @param c
     *        a character
     * @return true if character is allowed in an XML document
     */
    public static boolean isAllowedInXml(final int c) {
        boolean ok = false;
        if (c >= 0x10000 && c <= 0x10FFFF) {
            ok = true;
        } else if (c >= 0xE000 && c <= 0xFFFD) {
            ok = true;
        } else if (c >= 0x20 && c <= 0xD7FF) {
            ok = true;
        } else if (c == 0x9 || c == 0xA || c == 0xD) {
            ok = true;
        }
        return ok;
    }

    public static String jsonEscape(String raw) {
        // can't use StringEscapeUtils here because it escapes all non ascii characters and doesn't unescape them.
        // this is unacceptable for most utf8 content where in fact you only want to escape if you really have to

        StringBuilder buf = new StringBuilder(raw.length());
        for (char c : raw.toCharArray()) {
            // escape control characters
            if (c < 32) {
                switch (c) {
                case '\b':
                    buf.append('\\');
                    buf.append('b');
                    break;
                case '\n':
                    buf.append('\\');
                    buf.append('n');
                    break;
                case '\t':
                    buf.append('\\');
                    buf.append('t');
                    break;
                case '\f':
                    buf.append('\\');
                    buf.append('f');
                    break;
                case '\r':
                    buf.append('\\');
                    buf.append('r');
                    break;
                default:
                    // note, these characters are not unescaped.
                    if (c > 0xf) {
                        buf.append("\\u00" + hex(c));
                    } else {
                        buf.append("\\u000" + hex(c));
                    }
                    break;
                }
            } else if (isAllowedInXml(c)) {
                // note, this silently drops characters that would not be allowed in XML anyway.
                switch (c) {
                case '"':
                    buf.append('\\');
                    buf.append('"');
                    break;
                case '\\':
                    buf.append('\\');
                    buf.append('\\');
                    break;
                default:
                    buf.append(c);
                    break;
                }
            }
        }
        return buf.toString();
    }

    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
    }

	public static String jsonUnescape(String escaped) {
        StringBuilder buf=new StringBuilder(escaped.length());
        char[] chars = escaped.toCharArray();
        if(chars.length >= 2) {
            int i=1;
            while(i<chars.length) {
                if(chars[i-1] == '\\') {
                    if(chars[i]=='t') {
                        buf.append('\t');
                        i+=2;
                    } else if(chars[i]=='n') {
                        buf.append('\n');
                        i+=2;
                    } else if(chars[i]=='r') {
                        buf.append('\r');
                        i+=2;
                    } else if(chars[i] == '"') {
                        buf.append('"');
                        i+=2;
                    } else if(chars[i] == '\\') {
                        buf.append('\\');
                        i+=2;
                    } else {
                        buf.append(chars[i-1]);
                        buf.append(chars[i]);
                        i+=2;
                    }
                } else {
                    buf.append(chars[i-1]);
                    i++;
                }
            }
            if(i==chars.length) {
                // make sure to add the last character
                buf.append(chars[i-1]);
            }
            return buf.toString();
        } else {
            return escaped;
        }
	}

    private static void newline(final BufferedWriter bw, final int n, final boolean pretty) throws IOException {
		if(pretty) {
			bw.write('\n');
			for(int i=0;i<n;i++) {
				bw.write('\t');
			}
		}
	}
}
