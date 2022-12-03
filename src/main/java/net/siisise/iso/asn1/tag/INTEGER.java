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

import java.math.BigInteger;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.iso.asn1.ASN1Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 際限なく整数
 * JavaではBigIntegerに相当する
 */
public class INTEGER extends ASN1Object<BigInteger> implements ASN1Tag {
    private BigInteger val;
    
    public INTEGER() {
        super( ASN1.INTEGER );
    }

    @Override
    public byte[] encodeBody() {
        return val.toByteArray();
    }

    @Override
    public void decodeBody(byte[] data) {
        val = new BigInteger( data );
    }

    @Override
    public Element encodeXML(Document doc) {
        Element ele = doc.createElement( ASN1.INTEGER.name() );
        ele.setTextContent(val.toString());
        return ele;
    }

    @Override
    public void decodeXML(Element ele) {
        String txt = ele.getTextContent();
        val = new BigInteger(txt);
    }

    @Override
    public String toString() {
        return "INTEGER " + val.toString();
    }
    
    @Override
    public BigInteger getValue() {
        return val;
    }
    
    @Override
    public void setValue( BigInteger v ) {
        val = v;
    }
}
