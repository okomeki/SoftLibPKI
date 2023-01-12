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
package net.siisise.iso.asn1;

import java.math.BigInteger;

/**
 * X.208 (1988) → X.680 (1995)
 * 
 * RFC 4049→6019 ASN.1で日付と時刻 とか
 * RFC 5280 Appendix A.
 * RFC 5911 5912 未読
 * JIS X 5603
 * デコーダー込み
 */
public enum ASN1 {
    EndOfContent(0,net.siisise.iso.asn1.tag.NULL.class),
    BOOLEAN(0x01,net.siisise.iso.asn1.tag.BOOLEAN.class),
    INTEGER(0x02,net.siisise.iso.asn1.tag.INTEGER.class),
    BITSTRING(0x03,net.siisise.iso.asn1.tag.BITSTRING.class),
    OCTETSTRING(0x04,net.siisise.iso.asn1.tag.OCTETSTRING.class),
    NULL(0x05,net.siisise.iso.asn1.tag.NULL.class),
    OBJECTIDENTIFIER(0x06,net.siisise.iso.asn1.tag.OBJECTIDENTIFIER.class),
    ObjectDescriptor(0x07,null),
    EXTERNAL(0x08,null),
    REAL(0x09,null),
    ENUMERATED(0x0A,null),
    EMBEDDED_POV(0x0B,null), // X.690
    UTF8String(0x0C,net.siisise.iso.asn1.tag.ASN1String.class),
    RELATIVE_OID(0x0D,null), // X.690
    UNDEF_0E(0x0e,null),
    UNDEF_0F(0x0f,null),
    SEQUENCE(0x10,net.siisise.iso.asn1.tag.SEQUENCE.class),
    SET(0x11,net.siisise.iso.asn1.tag.SEQUENCE.class),
    NumericString(0x12,null),
    PrintableString(0x13,net.siisise.iso.asn1.tag.ASN1String.class),
    TeletexString(0x14,net.siisise.iso.asn1.tag.ASN1String.class),
    VideotexString(0x15,null),
    IA5String(0x16,net.siisise.iso.asn1.tag.ASN1String.class),
    UTCTime(0x17,net.siisise.iso.asn1.tag.ASN1String.class),
    GeneralizedTime(0x18,null), // 2050年以降
    GraphicString(0x19,null),
    VisibleString(0x1A,null),
    GeneralString(0x1B,null),
    CharacterString(0x1C,null),
    CHARACTER_STRING(0x1d,null), // X.690
    BMPString(0x1e,net.siisise.iso.asn1.tag.ASN1String.class),
    拡張(0x1F,null);

    BigInteger tag;
    Class<? extends ASN1Object> coder;

    ASN1(int id, Class<? extends ASN1Object> dc) {
        tag = BigInteger.valueOf(id);
        coder = dc;
    }

    /**
     * タグIDとclassを格納したもの
     * @param id tag ID
     * @return 
     */
    public static ASN1 valueOf(int id) {
        if (id >= 0x1f) {
            return null;
        }
        return ASN1.values()[id];
    }

}
