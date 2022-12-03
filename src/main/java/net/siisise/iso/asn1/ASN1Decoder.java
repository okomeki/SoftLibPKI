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

import java.io.*;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import net.siisise.io.PEM;
import net.siisise.iso.asn1.tag.OCTETSTRING;
import net.siisise.ietf.PKCSDecode;
import net.siisise.xml.TrXML;
import org.w3c.dom.Document;

/**
 * ASN.1 のエンコード/デコード.
 * ITU-T X.680 / ISO/IEC 8824-1
 * ITU-T X.690 BER (Basic Encoding Rules) X.209 ISO 8825-1
 * ITU-T X.690 CER (Canonical Encoding Rules)
 * ITU-T X.690 DER (Distinguished Encoding Rules) X.509 (BERのサブセット)
 * ITU-T X.693 XER / ISO/IEC 8825-4
 * ITU-T X.697 JER / ISO/IEC 8825-8
 * 
 * https://www.itu.int/rec/T-REC-X.690
 * 
 * RFC 5280 Appendix A.
 *
 * CER/DERは署名のためにBERの曖昧性をいくつか取り除くための制約を設けたもの X.500 DER に対応しているつもり
 */
public class ASN1Decoder {

    enum EncodeType {
        BER,
        CER,
        DER,
        XER,
        JER
    }

    EncodeType encode = EncodeType.DER;

    /**
     * map にしたい X.690
     */
/*    static final Class[] DECODE_CLASSES = {
        NULL.class, // 0x00 みていぎも使う? // EOCへ変更?
        BOOLEAN.class, // 0x01
        INTEGER.class, // 0x02
        BITSTRING.class, // 0x03
        OCTETSTRING.class, // 0x04 OCTETSTRING.class,
        NULL.class, // 0x05
        OBJECTIDENTIFIER.class, // 0x06
        null, // 0x07 ObjectDescriptor
        null, // 0x08 EXTERNAL
        null, // 0x09 REAL (float)
        null, // 0x0A ENUMERATED
        null, // 0x0B EMBEDDED PDV
        null, // 0x0C UTF8String
        null, // 0x0D RELATIVE-OID
        null, null, // (予約)
        SEQUENCE.class, // 0x10 SEQUENCE and SEQUENCE OF
        SEQUENCE.class, // 0x11 SET and SET OF
        null, // 0x12 NumericString
        ASN1String.class, // 0x13 PrintableString
        ASN1String.class, // 0x14 TeletexString / T61String
        null, // 0x15 VideotexString
        ASN1String.class, // 0x16 IA5String
        ASN1String.class, // 0x17 UTCTime
        null, // 0x18 GeneralizedTime
        null, // 0x19 GraphicString
        null, // 0x1A VisibleString
        null, // 0x1B GeneralString
        null, // 0x1C UniversalString
        null, // 0x1D CHARACTER STRING
        ASN1String.class, // 0x1E BMPString
        null // (予約)
    }; */
    /*
     * ASN1Cls へ
     */
    // static final String[] 種類 = {"汎用", "応用", "コンテキスト特定", "プライベート"};

    /**
     * デコーダ
     * @param in
     * @return 某長さを指定しない終端のときはnull
     * @throws IOException
     */
    public static ASN1Object toASN1(InputStream in) throws IOException {
        int code = in.read();
        ASN1Cls cl = ASN1Cls.valueOf((code >> 6) & 0x03); // 上位2bit
        boolean struct = (code & 0x20) != 0; // 構造化フラグ
        BigInteger tag = readTag(code, in);
        
        switch (cl) {
            case 汎用:
            //    System.out.print("クラス0:汎用 " + Integer.toHexString(code));
                break;
            case 応用:
                System.out.print("クラス1:応用");
                break;
            case コンテキスト特定:
                System.out.print("クラス2:コンテキスト特定" + (code & 0x1f));
                break; // タグ番号
            case プライベート:
                System.out.print("クラス3:プライベート");
                break;
        }
        if ( cl != ASN1Cls.汎用) {
            System.out.print("data=0x" + Integer.toHexString(code) + " tag=0x" + tag.toString(16) + " ");
        }
//        System.out.print(" 構造:" + struct);
        

        int inlen = len(in);
        if (inlen < 0 ) {
            throw new java.lang.UnsupportedOperationException();
        }
        if (code == 0 && inlen == 0) { // 終端コード
//            System.out.println("しゅうたんA");
            System.out.println();
            return null;
        }
//        System.out.println(" LEN:"+inlen);
        return decode(cl, struct, tag, in, inlen);
    }

    static BigInteger readTag(int code, InputStream in) throws IOException {
        BigInteger tag;
        if ((code & 0x1f) != 0x1f) {
            tag = BigInteger.valueOf(code & 0x1f);
        } else {
            tag = BigInteger.ZERO;
            int d;
            do {
                d = in.read();
                tag = tag.shiftLeft(7);
                tag = tag.or(BigInteger.valueOf(d & 0x7f));
            } while ((d & 0x80) != 0);
        }
        return tag;
    }

    /**
     * 長さフィールドの読み取り
     *
     * @return -1は可変長
     */
    static int len(InputStream in) throws IOException {
        int len;
        len = in.read();
        if (len >= 128) {
            int len2 = len & 0x7f;
            if (len == 128 && len2 == 0) {
                //     System.out.println("可変長");
                return -1;
            }
            len = 0;
            for (int cnt = 0; cnt < len2; cnt++) {
                len <<= 8;
                len += in.read();
                //     length++;
            }
        }
        return len;
    }

    /**
     * バイナリからObject に
     *
     * @param cl クラス 00:Universal(汎用) 01:Application(応用)
     * 10:Context-specific(コンテキスト特定) 11:Private(プライベート)
     * @param struct 構造化フラグ
     * @param tag タグ番号
     */
    static ASN1Object decode(ASN1Cls cl, boolean struct, BigInteger tag, InputStream in, int length) throws IOException {
        ASN1Object object;
        switch (cl) {
            case 汎用:
                if (struct) {
                //    System.out.println("構造" + cl + tag);
                    object = new ASN1Struct(cl, tag);
                } else {
//                    System.out.println("cl" + cl + "たぐs" + tag);
                    object = decodeTag(tag);
                    //    System.out.println("たぐe" + tag);
                }
                if (object == null) {
                    System.out.println("そのた 0x" + cl + ":" + struct + " tag:" + tag.toString(16) + " len:" + length);
                    if (length > 0) {
                        System.out.println("謎 0x");
                    }
                    throw new UnsupportedOperationException("unsupported encoding yet.");
                }
                break;
            case コンテキスト特定:
            case 応用:
            case プライベート:
                System.out.println(" 目印 " + tag);
                /*
             * if (inlen > 0) { tmp = new byte[inlen]; in.read(tmp); if (tmp[0]
             * == 0x30 || (code & 0x20) != 0) { ASN1 asn = new ASN1();
             * asn.toASN1(tmp); } }
             *
                 */
                if (struct) {
                    object = new ASN1Struct(cl, tag);
                    break;
                } else {
                    object = new OCTETSTRING(); // 仮
//                    throw new java.lang.UnsupportedOperationException("unsupported encoding yet.");
                }
                break;
            default:
                throw new java.lang.UnsupportedOperationException("unsupported encoding yet.");
        }
        object.decodeBody(in, length);
        return object;

    }

    /**
     * タグをコードから分解
     *
     * @param tag
     * @return
     */
    static ASN1Object decodeTag(BigInteger tag) {
        int tagid = tag.intValue();
    //    if ( tagid == 12) {
    //        System.err.println("ないもの" + tagid);
    //    }
            
        ASN1 tagAndClass = ASN1.valueOf(tagid);
    //    if ( tagAndClass == null) {
    //        System.err.println("ないもの" + tagid);
    //    }
        ASN1Object object;
        Class<? extends ASN1Object> decodeClass;

//        if (DECODE_CLASSES.length > tagAndClass.tag.longValue()
//               && (decodeClass = DECODE_CLASSES[tagAndClass.tag.intValue()]) != null) {
        if (ASN1.values().length > tagAndClass.tag.longValue()
                && (decodeClass = ASN1.valueOf(tagAndClass.tag.intValue()).coder) != null) {
            /*
             case OBJECTIDENTIFIER: // 0x06
             Class<? extends ASN1Object> cl;
             cl = (Class<ASN1Object>) java.lang.Class.forName("net.siisise.iso.asn1.tag." + tagAndClass.toString());
             object = cl.getConstructor().newInstance();
             break;
             */
            try {
                try {
                    Constructor<? extends ASN1Object> cnst = decodeClass.getConstructor();
                    object = cnst.newInstance();
                } catch (NoSuchMethodException e) {
                    object = decodeClass.getConstructor(ASN1.class).newInstance(tagAndClass);
                }
            } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException ex) {
                Logger.getLogger(ASN1Decoder.class.getName()).log(Level.SEVERE, null, ex);
                object = null;
            }
            return object;
        } else {
            return null;
        }
    }

    /**
     * PEM
     */
    private static Map<String,Object> base64Read(String path) throws IOException {
        return PEM.load("NEW CERTIFICATE REQUEST", path);
    }

    // http://www.trustss.co.jp/smnDataFormat430.html
    public static void main(String[] argv) {
        try {
//            byte[] data = BASE64.load("NEW CERTIFICATE REQUEST", "C:/home/201206test.csr");
//
//             Map<String,Object> src = PEM.load("PRIVATE KEY", "C:/home/pki/siisiseprivkey.pem");
//             Map<String,Object> src = PEM.load("CERTIFICATE", "C:/home/pki/siisisecert.pem");
             Map<String,Object> src = PEM.load("CERTIFICATE", "C:/home/pki/siisise-net (2).pem");
             
//            byte[] src = BASE64.load("RSA PRIVATE KEY", "C:/home/pki/clientA.key");
//            byte[] src = BASE64.load("RSA PRIVATE KEY", "C:/home/pki/okome20190714.opensshnopass.key");
//            byte[] src = BASE64.load("CERTIFICATE REQUEST", "C:/home/pki/clientA.req");
//            byte[] src = BASE64.load("CERTIFICATE", "C:/home/pki/clientA.crt");
//           byte[] src = FileIO.binRead("C:/home/p12/COMODOECCCertificationAuthority.p7c");
//           byte[] data = binRead("C:/home/pki/p12/m_sato2008.p12");
//            byte[] src = FileIO.binRead("C:/home/pki/p12/smime2009.p12");
//            byte[] src = BASE64.load("CERTIFICATE","C:/home/pki/wwwgooglecom.crt");
//            byte[] src = BASE64.load("CERTIFICATE","C:/home/pki/serverA.crt");
//        byte[] src = binRead("C:/home/m_sato20171203.p12");
//        byte[] src = FileIO.binRead("C:/home/pki/clientB.p12");
//        byte[] src = binRead("C:/home/p12/COMODOECCCertificationAuthority.der");
//           String pass = "enya1652";
//           byte[] data = binRead("C:/home/p12/nopass.p12");
            //byte[] data = BASE64.load("RSA PRIVATE KEY", "C:/home/pki/clientA.key");

            //data = binRead("cl:/home/p12/2012bb1.p12");
            //InputStream in = new FileInputStream( "cl:\\home\\pki\\clientA.p12" );
            byte[] result;
            byte[] enc = (byte[]) src.get(null);
            List<ASN1Object> asnobjs = ASN1Util.toASN1List(enc);

            System.out.println( "size: " + asnobjs.size() );
            
            try {
                // XML符号化 のテスト(独自
                for ( ASN1Object obj : asnobjs ) {
                    // ASN.1 to XML デコード
                    ASN1Struct top = (ASN1Struct) asnobjs.get(0);
                    Document doc = ASN1Util.toXML(top);

                    String txt = TrXML.plane(doc);
                    System.out.println(txt);

                    // XML to ASN.1 エンコード
                    ASN1Object robj = ASN1Util.toASN1(doc);
                    result = robj.encodeAll();
                }
                
                ASN1Struct top = (ASN1Struct) asnobjs.get(0);
                Document doc = ASN1Util.toXML(top);

                String txt = TrXML.plane(doc);
                System.out.println(txt);

                //top.get
                ASN1Object r = ASN1Util.toASN1(doc);
                result = r.encodeAll();
                for (int i = 0; i < result.length; i++) {
                    if (enc[i] != result[i]) {
                        System.out.println("違b " + Integer.toHexString(i) + " " + Integer.toHexString(enc[i] & 0xff) + " x " + Integer.toHexString(result[i] & 0xff));
                        //     } else {
                        //       System.out.println("  " + i + " " + Integer.toHexString(data[i] & 0xff));
                    }
                }
            } catch (TransformerException | ParserConfigurationException ex) {
                Logger.getLogger(ASN1Decoder.class.getName()).log(Level.SEVERE, null, ex);
            }


            ASN1Struct stc;
            
            for (ASN1Object obj : asnobjs) {
                System.out.println("p12外:" + obj);
//                System.out.println("XML:" + obj.);
                //PKCS12 pkcs12 = new PKCS12();
//            System.out.println( "PKCS#12 ? " + pkcs12.isValid( obj ) );
                if (obj instanceof ASN1Struct) {
                    stc = (ASN1Struct) obj;
                    System.out.println("ばーじょんのようなもの: " + stc.get(0));
                    PKCSDecode.decode(stc);
                    //OCTETSTRING o1 = (OCTETSTRING) stc.get(1,1,0);
                    //byte[] s = o1.getValue();
                    //ASN1Object asn1 = ASN1Util.toASN1(s);
                    //ASN1Struct o2 = new ASN1Struct(ASN1.OCTETSTRING);
                    //o2.attrStruct = false;
                    //o2.decodeBody(new ByteArrayInputStream(s));
                    //o2.add(asn1);
                    //System.out.println("内側:" + asn1);
                    //stc.set(o2,1,1,0,0);
                }
//                System.out.println("p12外2:" + obj);
            }
            // オブジェクト化

            System.out.println("データ個数 OBJs:" + asnobjs.size());

//            byte[] enc = (byte[]) src.get(null);
            int off = 0;
            // 符号化 と比較
            for (ASN1Object o : asnobjs) {
                if (o == null) {
                    System.out.println("ぬ");
                    continue;
                }

                result = o.encodeAll();

                for (int i = 0; i < result.length; i++) {
                    if (enc[off] != result[i]) {
                        System.out.println("違a " + Integer.toHexString(off) + " " + Integer.toHexString(enc[i] & 0xff) + " x " + Integer.toHexString(result[i] & 0xff));
                        //     } else {
                        //       System.out.println("  " + i + " " + Integer.toHexString(data[i] & 0xff));
                    }
                    off++;
                }
            }
            System.out.println("エンコードテスト 終");
        } catch (IOException ex) {
            Logger.getLogger(ASN1Decoder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
