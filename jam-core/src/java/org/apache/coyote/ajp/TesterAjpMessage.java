/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.coyote.ajp;

import java.util.ArrayList;
import java.util.List;

/**
 * Extends {@link AjpMessage} to provide additional methods for reading from the
 * message.
 * TODO: See if it makes sense for any/all of these methods to be transferred to
 *       AjpMessage
 */
public class TesterAjpMessage extends AjpMessage {

    private final List<Header> headers = new ArrayList<>();


    public TesterAjpMessage(int packetSize) {
        super(packetSize);
    }

    public byte readByte() {
        return buf[pos++];
    }

    public int readInt() {
        int val = (buf[pos++] & 0xFF ) << 8;
        val += buf[pos++] & 0xFF;
        return val;
    }

    public String readString() {
        int len = readInt();
        return readString(len);
    }

    public String readString(int len) {
        StringBuilder buffer = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            char c = (char) buf[pos++];
            buffer.append(c);
        }
        // Read end of string marker
        readByte();

        return buffer.toString();
    }

    public String readHeaderName() {
        byte b = readByte();
        if ((b & 0xFF) == 0xA0) {
            // Coded header
            return Constants.getResponseHeaderForCode(readByte());
        } else {
            int len = (b & 0xFF) << 8;
            len += getByte() & 0xFF;
            return readString(len);
        }
    }


    public void addHeader(int code, String value) {
        headers.add(new Header(code, value));
    }


    public void addHeader(String name, String value) {
        headers.add(new Header(name, value));
    }


    @Override
    public void end() {
        // Add the header count
        appendInt(headers.size());

        for (Header header : headers) {
            header.append(this);
        }

        // Terminator
        appendByte(0xFF);

        len = pos;
        int dLen = len - 4;

        buf[0] = (byte) 0x12;
        buf[1] = (byte) 0x34;
        buf[2] = (byte) ((dLen>>>8) & 0xFF);
        buf[3] = (byte) (dLen & 0xFF);
    }


    @Override
    public void reset() {
        super.reset();
        headers.clear();
    }




    private static class Header {
        private final int code;
        private final String name;
        private final String value;

        public Header(int code, String value) {
            this.code = code;
            this.name = null;
            this.value = value;
        }

        public Header(String name, String value) {
            this.code = 0;
            this.name = name;
            this.value = value;
        }

        public void append(TesterAjpMessage message) {
            if (code == 0) {
                message.appendString(name);
            } else {
                message.appendInt(code);
            }
            message.appendString(value);
        }
    }
    
    /**
     * Source from
     * https://apache.googlesource.com/tomcat/+/2d7d3c45e9c8a26959310f955f553d53776e55f8/java/org/apache/coyote/ajp/AjpMessage.java
     * Refactored.
     * 
     * 
     * Write a String out at the current write position.  Strings are
     * encoded with the length in two bytes first, then the string, and
     * then a terminating \0 (which is <B>not</B> included in the
     * encoded length).  The terminator is for the convenience of the C
     * code, where it saves a round of copying.  A null string is
     * encoded as a string with length 0.
     */
    public void appendString(String str) {
        if (str == null) {
//            log.error(sm.getString("ajpmessage.null"),
//                    new NullPointerException());
            appendInt(0);
            appendByte(0);
            return;
        }
        int len = str.length();
        appendInt(len);
        for (int i = 0; i < len; i++) {
            char c = str.charAt (i);
            // Note:  This is clearly incorrect for many strings,
            // but is the only consistent approach within the current
            // servlet framework.  It must suffice until servlet output
            // streams properly encode their output.
            if (((c <= 31) && (c != 9)) || c == 127 || c > 255) {
                c = ' ';
            }
            appendByte(c);
        }
        appendByte(0);
    }
    
    /**
     * Source from
     * https://apache.googlesource.com/tomcat/+/2d7d3c45e9c8a26959310f955f553d53776e55f8/java/org/apache/coyote/ajp/AjpMessage.java
     */
    public int getHeaderLength() {
      return Constants.H_SIZE;
    } 
}
