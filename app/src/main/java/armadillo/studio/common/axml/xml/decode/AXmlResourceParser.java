/*
 * Copyright 2008 Android4ME
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
package armadillo.studio.common.axml.xml.decode;

import android.util.TypedValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import armadillo.studio.common.axml.io.ZInput;
import armadillo.studio.common.axml.util.StringDecoder;


/**
 * @author Dmitry Skiba
 *         <p/>
 *         Binary xml files parser.
 *         <p/>
 *         Parser has only two states:
 *         (1) Operational state, which parser obtains after first successful call
 *         to next() and retains until open(), close(), or failed call to next().
 *         (2) Closed state, which parser obtains after open(), close(), or failed
 *         call to next(). In this state methods return invalid values or throw exceptions.
 *         <p/>
 *         * check all methods in closed state
 */
public class AXmlResourceParser implements XmlPullParser {

    public AXmlResourceParser() {
        resetEventInfo();
    }

    public void open(InputStream stream) throws IOException {
        close();
        if (stream != null) {
            m_reader = new ZInput(stream);
        }
    }

    public void open(InputStream stream, StringDecoder strings) throws IOException {
        close();
        m_reader = new ZInput(stream);
        m_strings = strings;
        m_namespaces.increaseDepth();
        m_operational = true;
    }

    public void close() {
        if (!m_operational) {
            return;
        }
        m_operational = false;
        try {
            m_reader.close();
        } catch (IOException e) {
        }
        m_reader = null;
        m_strings = null;
        m_resourceIDs = null;
        m_namespaces.reset();
        resetEventInfo();
    }

    /////////////////////////////////// iteration

    public int next() throws IOException {
        if (m_reader == null) {
            throw new IOException("Parser is not opened.");
        }
        try {
            doNext();
            return m_event;
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public int nextToken() throws IOException {
        return next();
    }

    private static void readCheckType(ZInput reader, int expectedType) throws IOException {
        int type = reader.readInt();
        if (type != expectedType) {
            throw new IOException(
                    "Expected chunk of type 0x" + Integer.toHexString(expectedType) +
                            ", read 0x" + Integer.toHexString(type) + ".");
        }
    }

    public int nextTag() throws IOException {
        int eventType = next();
        if (eventType == TEXT && isWhitespace()) {
            eventType = next();
        }
        if (eventType != START_TAG && eventType != END_TAG) {
            throw new IOException("Expected start or end tag.");
        }
        return eventType;
    }

    public String nextText() throws IOException {
        if (getEventType() != START_TAG) {
            throw new IOException("Parser must be on START_TAG to read next text.");
        }
        int eventType = next();
        if (eventType == TEXT) {
            String result = getText();
            eventType = next();
            if (eventType != END_TAG) {
                throw new IOException("Event TEXT must be immediately followed by END_TAG.");
            }
            return result;
        } else if (eventType == END_TAG) {
            return "";
        } else {
            throw new IOException("Parser must be on START_TAG or TEXT to read text.");
        }
    }

    public void require(int type, String namespace, String name) throws IOException {
        if (type != getEventType() ||
                (namespace != null && !namespace.equals(getNamespace())) ||
                (name != null && !name.equals(getName()))) {
            throw new IOException(TYPES[type] + " is expected.");
        }
    }

    public int getDepth() {
        return m_namespaces.getDepth() - 1;
    }

    public int getEventType() throws IOException {
        return m_event;
    }

    public int getLineNumber() {
        return m_lineNumber;
    }

    public String getName() {
        if (m_name == -1 || (m_event != START_TAG && m_event != END_TAG)) {
            return null;
        }
        return m_strings.getString(m_name);
    }

    public String getText() {
        if (m_name == -1 || m_event != TEXT) {
            return null;
        }
        return m_strings.getString(m_name);
    }

    public char[] getTextCharacters(int[] holderForStartAndLength) {
        String text = getText();
        if (text == null) {
            return null;
        }
        holderForStartAndLength[0] = 0;
        holderForStartAndLength[1] = text.length();
        char[] chars = new char[text.length()];
        text.getChars(0, text.length(), chars, 0);
        return chars;
    }

    public String getNamespace() {
        return m_strings.getString(m_namespaceUri);
    }

    public String getPrefix() {
        int prefix = m_namespaces.findPrefix(m_namespaceUri);
        return m_strings.getString(prefix);
    }

    public String getPositionDescription() {
        return "XML line #" + getLineNumber();
    }

    public int getNamespaceCount(int depth) throws IOException {
        return m_namespaces.getAccumulatedCount(depth);
    }

    public String getNamespacePrefix(int pos) throws IOException {
        int prefix = m_namespaces.getPrefix(pos);
        return m_strings.getString(prefix);
    }

    public String getNamespaceUri(int pos) throws IOException {
        int uri = m_namespaces.getUri(pos);
        return m_strings.getString(uri);
    }

    /////////////////////////////////// attributes

    public String getClassAttribute() {
        if (m_classAttribute == -1) {
            return null;
        }
        int offset = getAttributeOffset(m_classAttribute);
        int value = m_attributes[offset + ATTRIBUTE_IX_VALUE_STRING];
        return m_strings.getString(value);
    }

    public String getIdAttribute() {
        if (m_idAttribute == -1) {
            return null;
        }
        int offset = getAttributeOffset(m_idAttribute);
        int value = m_attributes[offset + ATTRIBUTE_IX_VALUE_STRING];
        return m_strings.getString(value);
    }

    public int getIdAttributeResourceValue(int defaultValue) {
        if (m_idAttribute == -1) {
            return defaultValue;
        }
        int offset = getAttributeOffset(m_idAttribute);
        int valueType = m_attributes[offset + ATTRIBUTE_IX_VALUE_TYPE];
        if (valueType != TypedValue.TYPE_REFERENCE) {
            return defaultValue;
        }
        return m_attributes[offset + ATTRIBUTE_IX_VALUE_DATA];
    }

    public int getStyleAttribute() {
        if (m_styleAttribute == -1) {
            return 0;
        }
        int offset = getAttributeOffset(m_styleAttribute);
        return m_attributes[offset + ATTRIBUTE_IX_VALUE_DATA];
    }

    public int getAttributeCount() {
        if (m_event != START_TAG) {
            return -1;
        }
        return m_attributes.length / ATTRIBUTE_LENGHT;
    }

    public String getAttributeNamespace(int index) {
        int offset = getAttributeOffset(index);
        int namespace = m_attributes[offset + ATTRIBUTE_IX_NAMESPACE_URI];
        if (namespace == -1) {
            return "";
        }
        return m_strings.getString(namespace);
    }

    public String getAttributePrefix(int index) {
        int offset = getAttributeOffset(index);
        int uri = m_attributes[offset + ATTRIBUTE_IX_NAMESPACE_URI];
        int prefix = m_namespaces.findPrefix(uri);
        if (prefix == -1) {
            return "";
        }
        return m_strings.getString(prefix);
    }

    public String getAttributeName(int index) {
        int offset = getAttributeOffset(index);
        int name = m_attributes[offset + ATTRIBUTE_IX_NAME];
        if (name == -1) {
            return "";
        }
        return m_strings.getString(name);
    }

    public String getString(int index) {
        return m_strings.getString(index);
    }

    public int getAttributeNameResource(int index) {
        int offset = getAttributeOffset(index);
        int name = m_attributes[offset + ATTRIBUTE_IX_NAME];
        if (m_resourceIDs == null ||
                name < 0 || name >= m_resourceIDs.length) {
            return 0;
        }
        return m_resourceIDs[name];
    }

    public int findResourceID(int id) {
        if (m_resourceIDs == null)
            return -1;
        for (int i = 0; i < m_resourceIDs.length; i++) {
            if (m_resourceIDs[i] == id)
                return i;
        }
        return -1;
    }

    public int getAttributeValueType(int index) {
        int offset = getAttributeOffset(index);
        return m_attributes[offset + ATTRIBUTE_IX_VALUE_TYPE];
    }

    public int getAttributeValueData(int index) {
        int offset = getAttributeOffset(index);
        return m_attributes[offset + ATTRIBUTE_IX_VALUE_DATA];
    }

    public int getAttributeValueString(int index) {
        int offset = getAttributeOffset(index);
        return m_attributes[offset + ATTRIBUTE_IX_VALUE_STRING];
    }

    public String getAttributeValue(int index) {
        int offset = getAttributeOffset(index);
        int valueType = m_attributes[offset + ATTRIBUTE_IX_VALUE_TYPE];
        if (valueType == TypedValue.TYPE_STRING) {
            int valueString = m_attributes[offset + ATTRIBUTE_IX_VALUE_STRING];
            return m_strings.getString(valueString);
        }
        //int valueData=m_attributes[offset+ATTRIBUTE_IX_VALUE_DATA];
        return "";//TypedValue.coerceToString(valueType,valueData);
    }

    public boolean getAttributeBooleanValue(int index, boolean defaultValue) {
        return getAttributeIntValue(index, defaultValue ? 1 : 0) != 0;
    }

    public float getAttributeFloatValue(int index, float defaultValue) {
        int offset = getAttributeOffset(index);
        int valueType = m_attributes[offset + ATTRIBUTE_IX_VALUE_TYPE];
        if (valueType == TypedValue.TYPE_FLOAT) {
            int valueData = m_attributes[offset + ATTRIBUTE_IX_VALUE_DATA];
            return Float.intBitsToFloat(valueData);
        }
        return defaultValue;
    }

    public int getAttributeIntValue(int index, int defaultValue) {
        int offset = getAttributeOffset(index);
        int valueType = m_attributes[offset + ATTRIBUTE_IX_VALUE_TYPE];
        if (valueType >= TypedValue.TYPE_FIRST_INT &&
                valueType <= TypedValue.TYPE_LAST_INT) {
            return m_attributes[offset + ATTRIBUTE_IX_VALUE_DATA];
        }
        return defaultValue;
    }

    public int getAttributeUnsignedIntValue(int index, int defaultValue) {
        return getAttributeIntValue(index, defaultValue);
    }

    public int getAttributeResourceValue(int index, int defaultValue) {
        int offset = getAttributeOffset(index);
        int valueType = m_attributes[offset + ATTRIBUTE_IX_VALUE_TYPE];
        if (valueType == TypedValue.TYPE_REFERENCE) {
            return m_attributes[offset + ATTRIBUTE_IX_VALUE_DATA];
        }
        return defaultValue;
    }

    public String getAttributeValue(String namespace, String attribute) {
        int index = findAttribute(namespace, attribute);
        if (index == -1) {
            return null;
        }
        return getAttributeValue(index);
    }

    public boolean getAttributeBooleanValue(String namespace, String attribute, boolean defaultValue) {
        int index = findAttribute(namespace, attribute);
        if (index == -1) {
            return defaultValue;
        }
        return getAttributeBooleanValue(index, defaultValue);
    }

    public float getAttributeFloatValue(String namespace, String attribute, float defaultValue) {
        int index = findAttribute(namespace, attribute);
        if (index == -1) {
            return defaultValue;
        }
        return getAttributeFloatValue(index, defaultValue);
    }

    public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
        int index = findAttribute(namespace, attribute);
        if (index == -1) {
            return defaultValue;
        }
        return getAttributeIntValue(index, defaultValue);
    }

    public int getAttributeUnsignedIntValue(String namespace, String attribute, int defaultValue) {
        int index = findAttribute(namespace, attribute);
        if (index == -1) {
            return defaultValue;
        }
        return getAttributeUnsignedIntValue(index, defaultValue);
    }

    public int getAttributeResourceValue(String namespace, String attribute, int defaultValue) {
        int index = findAttribute(namespace, attribute);
        if (index == -1) {
            return defaultValue;
        }
        return getAttributeResourceValue(index, defaultValue);
    }

    public int getAttributeListValue(int index, String[] options, int defaultValue) {
        return 0;
    }

    public int getAttributeListValue(String namespace, String attribute, String[] options, int defaultValue) {
        return 0;
    }

    public String getAttributeType(int index) {
        return "CDATA";
    }

    public boolean isAttributeDefault(int index) {
        return false;
    }

    /////////////////////////////////// dummies

    public void setInput(InputStream stream, String inputEncoding) throws IOException {
        throw new IOException(E_NOT_SUPPORTED);
    }

    public void setInput(Reader reader) throws IOException {
        throw new IOException(E_NOT_SUPPORTED);
    }

    public String getInputEncoding() {
        return null;
    }

    public int getColumnNumber() {
        return -1;
    }

    public boolean isEmptyElementTag() throws IOException {
        return false;
    }

    public boolean isWhitespace() throws IOException {
        return false;
    }

    public void defineEntityReplacementText(String entityName, String replacementText) throws IOException {
        throw new IOException(E_NOT_SUPPORTED);
    }

    public String getNamespace(String prefix) {
        throw new RuntimeException(E_NOT_SUPPORTED);
    }

    public Object getProperty(String name) {
        return null;
    }

    public void setProperty(String name, Object value) throws IOException {
        throw new IOException(E_NOT_SUPPORTED);
    }

    public boolean getFeature(String feature) {
        return false;
    }

    public void setFeature(String name, boolean value) throws IOException {
        throw new IOException(E_NOT_SUPPORTED);
    }

    ///////////////////////////////////////////// implementation

    /**
     * Namespace stack, holds prefix+uri pairs, as well as
     * depth information.
     * All information is stored in one int[] array.
     * Array consists of depth frames:
     * Data=DepthFrame*;
     * DepthFrame=Count+[Prefix+Uri]*+Count;
     * Count='count of Prefix+Uri pairs';
     * Yes, count is stored twice, to enable bottom-up traversal.
     * increaseDepth adds depth frame, decreaseDepth removes it.
     * push/pop operations operate only in current depth frame.
     * decreaseDepth removes any remaining (not pop'ed) namespace pairs.
     * findXXX methods search all depth frames starting
     * from the last namespace pair of current depth frame.
     * All functions that operate with int, use -1 as 'invalid value'.
     * <p/>
     * !! functions expect 'prefix'+'uri' pairs, not 'uri'+'prefix' !!
     */
    @SuppressWarnings("unused")
    private static final class NamespaceStack {
        public NamespaceStack() {
            m_data = new int[32];
        }

        public final void reset() {
            m_dataLength = 0;
            m_count = 0;
            m_depth = 0;
        }


        public final int getTotalCount() {
            return m_count;
        }

        public final int getCurrentCount() {
            if (m_dataLength == 0) {
                return 0;
            }
            int offset = m_dataLength - 1;
            return m_data[offset];
        }

        public final int getAccumulatedCount(int depth) {
            if (m_dataLength == 0 || depth < 0) {
                return 0;
            }
            if (depth > m_depth) {
                depth = m_depth;
            }
            int accumulatedCount = 0;
            int offset = 0;
            for (; depth != 0; --depth) {
                int count = m_data[offset];
                accumulatedCount += count;
                offset += (2 + count * 2);
            }
            return accumulatedCount;
        }

        public final void push(int prefix, int uri) {
            if (m_depth == 0) {
                increaseDepth();
            }
            ensureDataCapacity(2);
            int offset = m_dataLength - 1;
            int count = m_data[offset];
            m_data[offset - 1 - count * 2] = count + 1;
            m_data[offset] = prefix;
            m_data[offset + 1] = uri;
            m_data[offset + 2] = count + 1;
            m_dataLength += 2;
            m_count += 1;
        }

        public final boolean pop(int prefix, int uri) {
            if (m_dataLength == 0) {
                return false;
            }
            int offset = m_dataLength - 1;
            int count = m_data[offset];
            for (int i = 0, o = offset - 2; i != count; ++i, o -= 2) {
                if (m_data[o] != prefix || m_data[o + 1] != uri) {
                    continue;
                }
                count -= 1;
                if (i == 0) {
                    m_data[o] = count;
                    o -= (1 + count * 2);
                    m_data[o] = count;
                } else {
                    m_data[offset] = count;
                    offset -= (1 + 2 + count * 2);
                    m_data[offset] = count;
                    System.arraycopy(
                            m_data, o + 2,
                            m_data, o,
                            m_dataLength - o);
                }
                m_dataLength -= 2;
                m_count -= 1;
                return true;
            }
            return false;
        }

        public final boolean pop() {
            if (m_dataLength == 0) {
                return false;
            }
            int offset = m_dataLength - 1;
            int count = m_data[offset];
            if (count == 0) {
                return false;
            }
            count -= 1;
            offset -= 2;
            m_data[offset] = count;
            offset -= (1 + count * 2);
            m_data[offset] = count;
            m_dataLength -= 2;
            m_count -= 1;
            return true;
        }

        public final int getPrefix(int index) {
            return get(index, true);
        }

        public final int getUri(int index) {
            return get(index, false);
        }

        public final int findPrefix(int uri) {
            return find(uri, false);
        }

        public final int findUri(int prefix) {
            return find(prefix, true);
        }

        public final int getDepth() {
            return m_depth;
        }

        public final void increaseDepth() {
            ensureDataCapacity(2);
            int offset = m_dataLength;
            m_data[offset] = 0;
            m_data[offset + 1] = 0;
            m_dataLength += 2;
            m_depth += 1;
        }

        public final void decreaseDepth() {
            if (m_dataLength == 0) {
                return;
            }
            int offset = m_dataLength - 1;
            int count = m_data[offset];
            if ((offset - 1 - count * 2) == 0) {
                return;
            }
            m_dataLength -= 2 + count * 2;
            m_count -= count;
            m_depth -= 1;
        }

        private void ensureDataCapacity(int capacity) {
            int available = (m_data.length - m_dataLength);
            if (available > capacity) {
                return;
            }
            int newLength = (m_data.length + available) * 2;
            int[] newData = new int[newLength];
            System.arraycopy(m_data, 0, newData, 0, m_dataLength);
            m_data = newData;
        }

        private final int find(int prefixOrUri, boolean prefix) {
            if (m_dataLength == 0) {
                return -1;
            }
            int offset = m_dataLength - 1;
            for (int i = m_depth; i != 0; --i) {
                int count = m_data[offset];
                offset -= 2;
                for (; count != 0; --count) {
                    if (prefix) {
                        if (m_data[offset] == prefixOrUri) {
                            return m_data[offset + 1];
                        }
                    } else {
                        if (m_data[offset + 1] == prefixOrUri) {
                            return m_data[offset];
                        }
                    }
                    offset -= 2;
                }
            }
            return -1;
        }

        private final int get(int index, boolean prefix) {
            if (m_dataLength == 0 || index < 0) {
                return -1;
            }
            int offset = 0;
            for (int i = m_depth; i != 0; --i) {
                int count = m_data[offset];
                if (index >= count) {
                    index -= count;
                    offset += (2 + count * 2);
                    continue;
                }
                offset += (1 + index * 2);
                if (!prefix) {
                    offset += 1;
                }
                return m_data[offset];
            }
            return -1;
        }

        private int[] m_data;
        private int m_dataLength;
        private int m_count;
        private int m_depth;
    }

    /////////////////////////////////// package-visible

//	final void fetchAttributes(int[] styleableIDs,TypedArray result) {
//		result.resetIndices();
//		if (m_attributes==null || m_resourceIDs==null) {
//			return;
//		}
//		boolean needStrings=false;
//		for (int i=0,e=styleableIDs.length;i!=e;++i) {
//			int id=styleableIDs[i];
//			for (int o=0;o!=m_attributes.length;o+=ATTRIBUTE_LENGHT) {
//				int name=m_attributes[o+ATTRIBUTE_IX_NAME];
//				if (name>=m_resourceIDs.length ||
//					m_resourceIDs[name]!=id)
//				{
//					continue;
//				}
//				int valueType=m_attributes[o+ATTRIBUTE_IX_VALUE_TYPE];
//				int valueData;
//				int assetCookie;
//				if (valueType==TypedValue.TYPE_STRING) {
//					valueData=m_attributes[o+ATTRIBUTE_IX_VALUE_STRING];
//					assetCookie=-1;
//					needStrings=true;
//				} else {
//					valueData=m_attributes[o+ATTRIBUTE_IX_VALUE_DATA];
//					assetCookie=0;
//				}
//				result.addValue(i,valueType,valueData,assetCookie,id,0);
//			}
//		}
//		if (needStrings) {
//			result.setStrings(m_strings);
//		}
//	}

    final StringDecoder getStrings() {
        return m_strings;
    }

    ///////////////////////////////////

    private final int getAttributeOffset(int index) {
        if (m_event != START_TAG) {
            throw new IndexOutOfBoundsException("Current event is not START_TAG.");
        }
        int offset = index * 5;
        if (offset >= m_attributes.length) {
            throw new IndexOutOfBoundsException("Invalid attribute index (" + index + ").");
        }
        return offset;
    }

    private final int findAttribute(String namespace, String attribute) {
        if (m_strings == null || attribute == null) {
            return -1;
        }
        int name = m_strings.find(attribute);
        if (name == -1) {
            return -1;
        }
        int uri = (namespace != null) ?
                m_strings.find(namespace) :
                -1;
        for (int o = 0; o != m_attributes.length; ++o) {
            if (name == m_attributes[o + ATTRIBUTE_IX_NAME] &&
                    (uri == -1 || uri == m_attributes[o + ATTRIBUTE_IX_NAMESPACE_URI])) {
                return o / ATTRIBUTE_LENGHT;
            }
        }
        return -1;
    }

    private final void resetEventInfo() {
        m_event = -1;
        m_lineNumber = -1;
        m_name = -1;
        m_namespaceUri = -1;
        m_attributes = null;
        m_idAttribute = -1;
        m_classAttribute = -1;
        m_styleAttribute = -1;
    }

    private void doNext() throws IOException {
        // Delayed initialization.//延迟初始化。

        //m_strings 字符常量池  为null进行第一次读取
        if (m_strings == null) {
            readCheckType(m_reader, CHUNK_AXML_FILE);
            /*chunkSize*/
            m_reader.skipInt();
            m_strings = StringDecoder.read(m_reader);
            m_namespaces.increaseDepth();
            m_operational = true;
        }

        if (m_event == END_DOCUMENT) {
            return;
        }

        int event = m_event;

        //初始化为-1
        resetEventInfo();

        while (true) {
            if (m_decreaseDepth) {
                m_decreaseDepth = false;
                m_namespaces.decreaseDepth();
            }

            // Fake END_DOCUMENT event.
            if (event == END_TAG &&
                    m_namespaces.getDepth() == 1 &&
                    m_namespaces.getCurrentCount() == 0) {
                m_event = END_DOCUMENT;
                break;
            }

            int chunkType;
            if (event == START_DOCUMENT) {
                // Fake event, see CHUNK_XML_START_TAG handler.
                chunkType = CHUNK_XML_START_TAG;
            } else {
                chunkType = m_reader.readInt();
            }

            if (chunkType == CHUNK_RESOURCEIDS) {
                int chunkSize = m_reader.readInt();
                if (chunkSize < 8 || (chunkSize % 4) != 0) {
                    throw new IOException("Invalid resource ids size (" + chunkSize + ").");
                }
                m_resourceIDs = m_reader.readIntArray(chunkSize / 4 - 2);
                continue;
            }

            if (chunkType < CHUNK_XML_FIRST || chunkType > CHUNK_XML_LAST) {
                throw new IOException("Invalid chunk type (" + chunkType + ").");
            }

            // Fake START_DOCUMENT event.
            if (chunkType == CHUNK_XML_START_TAG && event == -1) {
                m_event = START_DOCUMENT;
                break;
            }

            // Common header.
            /*chunkSize*/
            m_reader.skipInt();//头部的大小
            int lineNumber = m_reader.readInt();//等于命名空间开始标签在原来文本格式的Xml文件出现的行号
			/*0xFFFFFFFF*/
            m_reader.skipInt();

            if (chunkType == CHUNK_XML_START_NAMESPACE ||
                    chunkType == CHUNK_XML_END_NAMESPACE) {
                if (chunkType == CHUNK_XML_START_NAMESPACE) {
                    int prefix = m_reader.readInt();//等于字符串“android”在字符串资源池中的索引
                    int uri = m_reader.readInt();//等于字符串“http://schemas.android.com/apk/res/android”在字符串资源池中的索引
                    m_namespaces.push(prefix, uri);
                } else {
					/*prefix*/
                    m_reader.skipInt();
					/*uri*/
                    m_reader.skipInt();
                    m_namespaces.pop();
                }
                continue;
            }

            m_lineNumber = lineNumber;

            if (chunkType == CHUNK_XML_START_TAG) {
                m_namespaceUri = m_reader.readInt();
                m_name = m_reader.readInt();
				
				/*flags?*/
                m_reader.skipInt();

                int attributeCount = m_reader.readInt();
                m_idAttribute = (attributeCount >>> 16) - 1;

                attributeCount &= 0xFFFF;

                m_classAttribute = m_reader.readInt();
                m_styleAttribute = (m_classAttribute >>> 16) - 1;
                m_classAttribute = (m_classAttribute & 0xFFFF) - 1;


                currentAttributeStart = m_reader.getOffset();
                m_attributes = m_reader.readIntArray(attributeCount * ATTRIBUTE_LENGHT);
                for (int i = ATTRIBUTE_IX_VALUE_TYPE; i < m_attributes.length; ) {
                    m_attributes[i] = (m_attributes[i] >>> 24);
                    i += ATTRIBUTE_LENGHT;
                }
                m_namespaces.increaseDepth();
                m_event = START_TAG;
                break;
            }

            if (chunkType == CHUNK_XML_END_TAG) {
                m_namespaceUri = m_reader.readInt();
                m_name = m_reader.readInt();
                m_event = END_TAG;
                m_decreaseDepth = true;
                break;
            }

            if (chunkType == CHUNK_XML_TEXT) {
                m_name = m_reader.readInt();

				/*?*/
                m_reader.skipInt();
				/*?*/
                m_reader.skipInt();
                m_event = TEXT;
                break;
            }
        }
    }

    public int currentAttributeStart;

    /////////////////////////////////// data
	
	/*
	 * All values are essentially indices, e.g. m_name is
	 * an index of name in m_strings.
	 */

    private ZInput m_reader;
    private boolean m_operational = false;

    private StringDecoder m_strings;
    private int[] m_resourceIDs;
    private NamespaceStack m_namespaces = new NamespaceStack();

    private boolean m_decreaseDepth;

    private int m_event;
    private int m_lineNumber;
    private int m_name;
    private int m_namespaceUri;
    private int[] m_attributes;
    private int m_idAttribute;
    private int m_classAttribute;
    private int m_styleAttribute;

    private static final String
            E_NOT_SUPPORTED = "Method is not supported.";

    private static final int
            ATTRIBUTE_IX_NAMESPACE_URI = 0,
            ATTRIBUTE_IX_NAME = 1,
            ATTRIBUTE_IX_VALUE_STRING = 2,
            ATTRIBUTE_IX_VALUE_TYPE = 3,
            ATTRIBUTE_IX_VALUE_DATA = 4,
            ATTRIBUTE_LENGHT = 5;

    private static final int
            CHUNK_AXML_FILE = 0x00080003,
            CHUNK_RESOURCEIDS = 0x00080180,
            CHUNK_XML_FIRST = 0x00100100,
            CHUNK_XML_START_NAMESPACE = 0x00100100,
            CHUNK_XML_END_NAMESPACE = 0x00100101,
            CHUNK_XML_START_TAG = 0x00100102,
            CHUNK_XML_END_TAG = 0x00100103,
            CHUNK_XML_TEXT = 0x00100104,
            CHUNK_XML_LAST = 0x00100104;
}
