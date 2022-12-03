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

import java.nio.charset.StandardCharsets;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.iso.asn1.ASN1Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 文字列の共通処理
 */
public class ASN1String extends ASN1Object<String> implements ASN1Tag {

    private String string;
//    private byte[] data; // バイト型でも持つ?

    public ASN1String( ASN1 id ) {
        super(id);
    }

    @Override
    public void decodeBody( byte[] val ) {
//        data = (byte[]) val.clone();
        switch ( ASN1.valueOf(getId()) ) {
        case UTF8String:
            string = new String(val, StandardCharsets.UTF_8);
            break;
        case IA5String:
        case PrintableString:
        case CharacterString:
        case GeneralString:
        case GraphicString:
        case NumericString:
        case TeletexString:
        case VideotexString:
        case VisibleString:
        case UTCTime:
            string = new String(val, StandardCharsets.US_ASCII);
            break;
        case BMPString: // ISO 10646-1 基本多言語面
            string = new String(val, StandardCharsets.UTF_16BE);
            break;
        default:
            throw new UnsupportedOperationException( "Unknows String " + getId() + " yet.");
        }
        System.out.println(" " + string);
    }

    @Override
    public byte[] encodeBody() {
        switch ( getId() ) {
        case UTF8String:
            return string.getBytes(StandardCharsets.UTF_8);
        case IA5String:
        case PrintableString:
        case CharacterString:
        case GeneralString:
        case GraphicString:
        case NumericString:
        case TeletexString:
        case VideotexString:
        case VisibleString:
        case UTCTime:
            return string.getBytes(StandardCharsets.US_ASCII);
        case BMPString:
            return string.getBytes(StandardCharsets.UTF_16BE);
        }
        // data を使うかエラー
        throw new java.lang.UnsupportedOperationException(" ASN String ID:" + Integer.toHexString(getId()) );
//        return data;
    }

    @Override
    public Element encodeXML( Document doc ) {
        Element ele = doc.createElement( ASN1.valueOf(getId()).toString() );
        ele.setTextContent(string);
        return ele;
    }

    @Override
    public void decodeXML( Element ele ) {
        string = ele.getTextContent();
    }

    @Override
    public String toString() {
        return string;
    }

    @Override
    public String getValue() {
        return string;
    }

    @Override
    public void setValue( String val ) {
        string = val;
    }
}
