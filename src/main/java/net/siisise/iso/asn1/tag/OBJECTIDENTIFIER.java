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
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import net.siisise.iso.asn1.ASN1;
import net.siisise.iso.asn1.ASN1Object;
import net.siisise.iso.asn1.ASN1Tag;
import net.siisise.xml.XElement;
import net.siisise.xml.XMLIO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * ITU-T Rec.X.690.
 * 文字列、int[]などの型に
 * 
 */
public class OBJECTIDENTIFIER extends ASN1Object<String> implements ASN1Tag {

    private List<String> list = new ArrayList<>();
    private String identifier;
    /**
     * 名前解決用
     */
    private static OID root = new OID();
    
    static class OID {
        private String oid;
        private String name;
        private Class oidClass;
        private Map<String,OID> map = new HashMap<>();

        public String getName() {
            return name;
        }

        public void setName( String name ) {
            this.name = name;
        }
        
        public Class getOidClass() {
            return oidClass;
        }

        public Map<String,OID> getMap() {
            return map;
        }

        public void setMap( Map<String,OID> map ) {
            this.map = map;
        }
        
        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("<").append(getName()).append(" OID=\"").append(oid).append("\">");
            for ( String k : map.keySet()) {
                str.append(k).append(" : ").append( map.get( k ));
            }
            str.append("</").append(name).append(">");
            return str.toString();
        }
    }
    
    static {
        InputStream  xmlIn = OBJECTIDENTIFIER.class.getResourceAsStream("OBJECTIDENTIFIER.xml");
        try {
            Document oidNameXml = XMLIO.readXML(xmlIn);
            XElement rootElement = new XElement(oidNameXml.getDocumentElement());
            setKey( root, rootElement );
            
        } catch ( SAXException | ParserConfigurationException | IOException ex ) {
            Logger.getLogger(OBJECTIDENTIFIER.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                xmlIn.close();
            } catch ( IOException ex ) {
                Logger.getLogger(OBJECTIDENTIFIER.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void setKey( OID key, XElement ele ) {
        
        List<XElement> subKeys = ele.getElements();
        for ( XElement etag : subKeys ) {
            OID newKey = new OID();
            newKey.oid = etag.getAttribute("oid");
            newKey.name = etag.getAttribute("name");
            key.getMap().put( newKey.oid, newKey );
            setKey( newKey, etag );
        }
    }

    public OBJECTIDENTIFIER() {
        super(ASN1.OBJECTIDENTIFIER);
    }
    
    public OBJECTIDENTIFIER(String id) {
        this();
        setValue(id);
    }

    @Override
    public byte[] encodeBody() {

        List<byte[]> barray = new ArrayList<>();
        barray.add(new byte[]{ (byte) (get(0) * 40 + get(1)) });
        for ( int n = 2; n < list.size(); n++ ) {
            barray.add(encodeValue(n));
        }
        int len = 0;
        for ( byte[] d : barray ) {
            len += d.length;
        }
        byte[] dst = new byte[len];
        len = 0;
        for ( byte[] d : barray ) {
            System.arraycopy(d, 0, dst, len, d.length);
            len += d.length;
        }
        return dst;
        //      throw new UnsupportedOperationException("Not supported yet.");
    }

    byte[] encodeValue( int off ) {
        String val = list.get(off);
        BigInteger bigVal = new BigInteger(val);
        byte[] data = new byte[bigVal.bitLength() == 0 ? 1 : ((bigVal.bitLength() + 6) / 7)];
        for ( int i = 0; i < data.length; i++ ) {
            data[i] = (byte) (bigVal.shiftRight((data.length - i - 1) * 7).intValue() & 0x7F);
            if ( i < data.length - 1 ) {
                data[i] |= (byte) 0x80;
            }
        }
        return data;
    }

    @Override
    public void decodeBody( byte[] data ) {
        list.clear();
        int z = data[0] & 0xff;
        list.add(Integer.toString(z / 40));
        list.add(Integer.toString(z % 40));
        StringBuilder code; // 表示系へ
        code = new StringBuilder();
        code.append((int) z / 40);
        code.append('.');
        code.append((int) z % 40);
        int off = 0;
        //   long d;
        BigInteger bi;
        while ( off < data.length - 1 ) {
            //   d = 0;
            bi = BigInteger.ZERO;
            do {
                off++;
                //       d <<= 7;
                bi = bi.shiftLeft(7);
                //        d += data[off] & 0x7f;
                bi = bi.add(BigInteger.valueOf(data[off] & 0x7f));

            } while ( (data[off] & 0x80) != 0 );
            code.append('.');
            code.append(bi.toString());
            list.add(bi.toString());
        }
        identifier = code.toString();
//        System.out.println(" " + identifier);
//        System.out.println( " " + getName() );
    }

    // 仮
    @Override
    public String toString() {
        return "OID "+getName();
    }
    
    /**
     * ツリー上の名
     * @return 
     */
    public String getName() {
        OID key = root;
        StringBuilder name = new StringBuilder();
        for ( int i = 0; i < list.size(); i++ ) {
            if ( key != null ) key = key.getMap().get( list.get(i) );
            name.append(".");
            if ( key != null ) {
                String kname = key.getName();
                name.append(kname == null ? "Unknown" : kname);
                name.append("(").append(key.oid);
                name.append(")");
            } else {
                name.append("Unknown(");
                name.append(list.get(i));
                name.append(")");
            }
        }
        return name.substring(1);
    }
    
    private OID getOID() {
        OID key = root;
        for ( String n : list ) {
            OID newID = key.getMap().get(n);
            if ( newID == null ) {
                newID = new OID();
                newID.oid = n;
                newID.name = "Unknown" + n;
                key.map.put(n, newID);
            }
            key = newID;
        }
        return key;
    }
    
    public String getShortName() {
        OID key = root;
        String name = new String();
        for ( int i = 0; i < list.size(); i++ ) {
            if ( key != null ) {
                key = key.getMap().get( list.get(i) );
            }
            if ( key != null ) {
                name = key.getName();
            } else {
                name = "Unknown(" + list.get(i) + ")";
            }
        }
        return name;
    }
    
    private long get( int index ) {
        return Long.parseLong(list.get(index));
    }

    @Override
    public String getValue() {
        return identifier;
    }
    
    @Override
    public void setValue( String id ) {
        identifier = id;
        list = Arrays.asList( identifier.split("\\."));
    }
    
    public OBJECTIDENTIFIER sub(String id) {
        return new OBJECTIDENTIFIER(getValue() + "." + id);
    }

    public OBJECTIDENTIFIER sub(long id) {
        return sub(Long.toString(id));
    }

    @Override
    public Element encodeXML( Document doc ) {
        Element ele = doc.createElement( ASN1.OBJECTIDENTIFIER.name() );
        ele.setTextContent(identifier);
        ele.setAttribute("short", getShortName() );
        return ele;
    }

    @Override
    public void decodeXML( Element ele ) {
        setValue( ele.getTextContent() );
    }
    
    @Override
    public boolean equals(Object o) {
        if ( o != null && o instanceof OBJECTIDENTIFIER ) {
            OBJECTIDENTIFIER oid = (OBJECTIDENTIFIER) o;
            return oid.identifier.equals(identifier);
        }
        return false;
    }
}
