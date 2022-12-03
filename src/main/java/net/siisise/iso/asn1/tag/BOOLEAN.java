/*
 * Copyright 2019-2022 Siisise Net.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.siisise.iso.asn1.tag;

import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.iso.asn1.ASN1Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class BOOLEAN extends ASN1Object<Boolean> implements ASN1Tag {

    private boolean val;

    public BOOLEAN() {
        super(ASN1.BOOLEAN);
    }

    @Override
    public byte[] encodeBody() {
        return new byte[]{ (byte) (val ? 0xff : 0) }; // CER/DER では true は 0xff
    }

    @Override
    public void decodeBody( byte[] data ) {
        val = data[0] != 0;
    }

    @Override
    public Element encodeXML( Document doc ) {
        Element bool = doc.createElement( ASN1.BOOLEAN.name() );
        bool.appendChild(doc.createTextNode("" + val));
        return bool;
    }

    @Override
    public void decodeXML( Element element ) {
        val = Boolean.parseBoolean(element.getTextContent());
    }

    @Override
    public String toString() {
        return Boolean.toString(val);
    }

    @Override
    public Boolean getValue() {
        return val;
    }

    @Override
    public void setValue( Boolean v ) {
        val = v;
    }
}
