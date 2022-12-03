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

import net.siisise.io.BASE64;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.iso.asn1.ASN1Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * CER/DER 長さが0のとき 1オクテットの0で符号化しますょ
 */
public class BITSTRING extends ASN1Object<byte[]> implements ASN1Tag {

    private byte[] data;
    /**
     * 全長ビット
     */
    private long bitlen;

    public BITSTRING() {
        super(ASN1.BITSTRING);
    }

    @Override
    public byte[] encodeBody() {
        byte[] out = new byte[data.length + 1];
        out[0] = (byte) ((-(bitlen % 8)) & 0x7);
        System.arraycopy(data, 0, out, 1, data.length);
        return out;
    }

    @Override
    public void decodeBody( byte[] data ) {
        int 未使用ビット数 = (int) data[0] & 0xff;
        data[0] = 0;

        bitlen = data.length * 8L - 8 - 未使用ビット数;
        if ( data.length > 0 ) {
            this.data = new byte[data.length - 1];
            System.arraycopy(data, 1, this.data, 0, data.length - 1);
        }
    }

    @Override
    public Element encodeXML( Document doc ) {
        Element ele = doc.createElement( ASN1.BITSTRING.name() );
        ele.setAttribute("bitlen", String.valueOf(bitlen));
        BASE64 b64 = new BASE64();
        String val = b64.encode(data);
        ele.setTextContent(val);
        return ele;
    }

    @Override
    public void decodeXML( Element element ) {
        bitlen = Long.parseLong(element.getAttribute("bitlen"));
        data = BASE64.decodeBase(element.getTextContent());
    }

    /**
     * TODO: bit
     *
     * @return
     */
    public String toString() {
        BASE64 b64 = new BASE64();
        return b64.encode(data);
    }

    /** 未使用ビット数を考慮しない */
    @Override
    public byte[] getValue() {
        return data;
    }

    /** 未使用ビット数を考慮しない */
    @Override
    public void setValue( byte[] val ) {
        data = val;
        bitlen = data.length * 8;
    }
}
