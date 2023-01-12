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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.siisise.io.BASE64;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.iso.asn1.ASN1Tag;
import net.siisise.iso.asn1.ASN1Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 箱の場合もあるのでその場合はこちらではなく ASN1Struct を使う
 */
public class OCTETSTRING extends ASN1Object<byte[]> implements ASN1Tag {
    private byte[] data;
    
    public OCTETSTRING() {
        super( ASN1.OCTETSTRING );
    }

    public OCTETSTRING(byte[] d) {
        super( ASN1.OCTETSTRING );
        data = d; // コピーしたほうがいい?
    }

    @Override
    public byte[] encodeBody() {
        return data;
    }

    @Override
    public void decodeBody(byte[] src) {
        data = src;
    }

    @Override
    public Element encodeXML( Document doc ) {
        Element ele = doc.createElement( ASN1.OCTETSTRING.name() );
       // ele.setAttribute("ex", new String(data, StandardCharsets.UTF_8));
        BASE64 b64 = new BASE64();
        String val = b64.encode(data);
        ele.setTextContent(val);
        return ele;
    }

    @Override
    public void decodeXML( Element element ) {
        data = BASE64.decodeBase(element.getTextContent());
    }
    
    public String toString() {
            try {
                return "OCTET STRING len;" + data.length + ASN1Util.toASN1List(data);
            } catch (java.lang.UnsupportedOperationException ex) {
//                Logger.getLogger(OCTETSTRING.class.getName()).log(Level.SEVERE, null, ex);
//                System.out.println(ex.getLocalizedMessage());
                // まだ
            } catch (IOException ex) {
                Logger.getLogger(OCTETSTRING.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        
        return "OCTET STRING (略) len:" + data.length + " " + dump();  // + new String(data,0,16); // + new String( data );
    }
    
    String dump() {
        StringBuilder sb = new StringBuilder();
        for (byte d : data ) {
            if ( (d > 0x21 && d<=0x24) || (d >= 0x26 && d <= 0x7e) ) {
                sb.append((char)d);
            } else {
                sb.append("%");
                sb.append(Integer.toHexString(256 + (d & 0xff)).substring(1));
            }
        }
        return sb.toString();
    }

    /**
     * TODO: 場合によっては要コピー
     * @return 
     */
    @Override
    public byte[] getValue() {
        return data;
    }

    @Override
    public void setValue( byte[] val ) {
        data = val;
    }
}
