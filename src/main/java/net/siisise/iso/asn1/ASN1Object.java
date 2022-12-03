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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * アクセサ略
 *
 * @param <T>
 */
public abstract class ASN1Object<T> implements java.lang.Comparable<ASN1Object> {

    private ASN1Cls asn1class = ASN1Cls.汎用;
    private BigInteger tag;
    /** 可変長形式 */
    protected boolean inefinite = false;

    protected ASN1Object() {
        asn1class = ASN1Cls.汎用;
    }

    /**
     * 拡張
     * @param cls
     * @param tag
     */
    protected ASN1Object( byte cls, BigInteger tag ) {
        asn1class = ASN1Cls.valueOf(cls);
        this.tag = tag;
    }

    protected ASN1Object( ASN1Cls cls, BigInteger tag ) {
        asn1class = cls;
        this.tag = tag;
    }

    protected ASN1Object( ASN1 tag ) {
        asn1class = ASN1Cls.汎用;
        this.tag = tag.tag;
    }

    public int getASN1Class() {
        return asn1class.cls;
    }
    
    public ASN1Cls getASN1Cls() {
        return asn1class;
    }

    public boolean isStruct() {
        return false;
    }
    
    abstract public T getValue();
    abstract public void setValue(T val);

    /**
     * ヘッダ書き込みで前後するのであまり使えない
     * @param out
     * @throws java.io.IOException
     */
    public void encodeAll( OutputStream out ) throws IOException {
        out.write(encodeAll());
    }

    /**
     * 
     * @return 
     */
    public byte[] encodeAll() {
        byte[] tagNo = encodeTagNo();

//        System.out.println("" + getTag() + " {");
        byte[] body = encodeBody();
//        System.out.print(" }");

        byte[] lengthField = encodeLength(body.length);
        byte[] encoded = new byte[tagNo.length + lengthField.length + body.length + (inefinite ? 2 : 0)];
        System.arraycopy(tagNo, 0, encoded, 0, tagNo.length);
        System.arraycopy(lengthField, 0, encoded, tagNo.length, lengthField.length);
        System.arraycopy(body, 0, encoded, tagNo.length + lengthField.length, body.length);
//        System.out.println(" tagNolen " + tagNo.length + "lengf " + lengthField.length + "body " + body.length);
        if ( inefinite ) {
            System.arraycopy( EO, 0, encoded, tagNo.length + lengthField.length + body.length, EO.length );
        }
        return encoded;
    }

    private byte[] encodeTagNo() {
        BigInteger tagId = getTag();
        byte[] tagNo;
        int bitLen = tagId.bitLength();

        if ( bitLen <= 5 ) {
            tagNo = new byte[1];
            tagNo[0] = (byte) ((getASN1Class() << 6) | (isStruct() ? 0x20 : 0) | tagId.intValue());
        } else {
            int len = (bitLen + 6) / 7;
            tagNo = new byte[len + 1];
            BigInteger t = tagId;
            for ( int i = 0; i < len; i++ ) {
                tagNo[i + 1] = (byte) (((i == len - 1) ? 0x80 : 0) | t.shiftRight((len - i - 1) * 7).intValue() & 0x7f);
            }
            tagNo[0] = (byte) ((getASN1Class() << 6) | (isStruct() ? 0x20 : 0) | 0x1f);
        }
        return tagNo;
    }

    /**
     * encodeValue に変える?
     * @return 
     */
    public abstract byte[] encodeBody();
    
    static final byte[] INFLEN = {(byte)0x80};
    static final byte[] EO = {0,0};

    private byte[] encodeLength( int len ) {
        byte[] lengthField;
        if ( inefinite ) {
            return INFLEN;
        }
        if ( len < 0x80 ) {
            lengthField = new byte[1];
            lengthField[0] = (byte) (len & 0x7f);
        } else if ( len < 0x100 ) {
            lengthField = new byte[2];
            lengthField[0] = (byte) 0x81;
            lengthField[1] = (byte) (len & 0xff);
        } else if ( len < 0x10000 ) {
            lengthField = new byte[3];
            lengthField[0] = (byte) 0x82;
            lengthField[2] = (byte) (len & 0xff);
            len >>= 8;
            lengthField[1] = (byte) (len & 0xff);
        } else if ( len < 0x1000000 ) {
            lengthField = new byte[4];
            lengthField[0] = (byte) 0x83;
            lengthField[3] = (byte) (len & 0xff);
            len >>= 8;
            lengthField[2] = (byte) (len & 0xff);
            len >>= 8;
            lengthField[1] = (byte) (len & 0xff);
        } else {
            lengthField = new byte[5];
            lengthField[0] = (byte) 0x84;
            lengthField[4] = (byte) (len & 0xff);
            len >>= 8;
            lengthField[3] = (byte) (len & 0xff);
            len >>= 8;
            lengthField[2] = (byte) (len & 0xff);
            len >>= 8;
            lengthField[1] = (byte) (len & 0xff);
        }
        return lengthField;
    }

    /**
     * デコーダから呼ばれるのみ
     * OCTETSTRINGでも struct の場合は ASN1Struct を使うので length が -1 になることはないはず
     * @param in
     * @param length
     * @throws java.io.IOException
     */
    public void decodeBody( InputStream in, int length ) throws IOException {
        byte[] data = new byte[length];
        in.read(data);
        decodeBody(data);
    }

    public void decodeBody( byte[] data ) {
        throw new UnsupportedOperationException("Not supported " + getTag() + " yet.");
    }

    /** タグとデータを書き
     * @param doc
     * @return  */
    abstract public Element encodeXML( Document doc );

    /** データのみ読む
     * @param element */
    abstract public void decodeXML( Element element );

    /**
     * コンストラクタで指定するかオーバーライドするかどちらか
     * @return 
     */
    public int getId() {
        return tag.intValue();
    }

    public BigInteger getTag() {
        if ( tag == null ) {
            return BigInteger.valueOf(getId());
        }
        return tag;
    }
    
    @Override
    public int compareTo( ASN1Object o ) {
        if ( getASN1Class() != o.getASN1Class() ) {
            return getASN1Class() - o.getASN1Class();
        }
        if ( getId() != o.getId() ) {
            return getId() - o.getId();
        }
        if ( inefinite != o.inefinite) {
            return ( inefinite ? 1 : 0 ) - ( o.inefinite ? 1 : 0 );
        }
        return 0;
    }
}
