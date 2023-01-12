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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * SEQUENCE、SETと標準以外の構造
 *
 */
public class ASN1Struct extends ASN1Object<List<ASN1Object>> {

    private List<ASN1Object> list = new ArrayList<>();

    /**
     * falseのときふりをする?
     */
    boolean attrStruct = true;

    public ASN1Struct(byte cls, BigInteger id) {
        super(cls, id);
    }

    public ASN1Struct(ASN1Cls cls, BigInteger id) {
        super(cls, id);
    }

    public ASN1Struct(ASN1Object obj, boolean str) {
        super(ASN1.valueOf(obj.getId()));
        attrStruct = str;
    }

    /**
     * coder は 0
     *
     * @param id
     */
    public ASN1Struct(ASN1 id) {
        super(id);
    }

    @Override
    public boolean isStruct() {
        return attrStruct;
    }

    @Override
    public byte[] encodeBody() {
        byte[][] all = new byte[list.size()][];
        byte[] full;
        int off = 0;
        int len = 0;

        if (getId() == ASN1.SET.tag.intValue()) {
            Collections.sort(list);
        }

        for (ASN1Object object : list) {
            all[off] = object.encodeAll();
            len += all[off++].length;
        }
        full = new byte[len];
        off = 0;
        for (byte[] data : all) {
//            System.out.println("cpy len " + data.length );
            System.arraycopy(data, 0, full, off, data.length);
            off += data.length;
        }
        return full;
    }

    /**
     *
     * @param in
     * @param length
     * @throws IOException
     */
    @Override
    public void decodeBody(InputStream in, int length) throws IOException {
        list.clear();
        if (length >= 0) {
            byte[] data = new byte[length];
            in.read(data);
            InputStream boxIn = new ByteArrayInputStream(data);
            decodeBody(boxIn);
        } else {
            inefinite = true;
            decodeEOFList(in);
        }
        //  throw new UnsupportedOperationException("Not supported yet.");
    }

    void decodeBody(InputStream in) throws IOException {
        while (in.available() > 0) {
            ASN1Object o = ASN1Decoder.toASN1(in);
            list.add(o);
        }
    }

    void decodeEOFList(InputStream in) throws IOException {
        while (in.available() > 0) {
            ASN1Object o = ASN1Decoder.toASN1(in);
            if (o == null) {
                break;
            }
            list.add(o);
        }
    }

    @Override
    public Element encodeXML(Document doc) {
        Element ele;
        ASN1 n2 = ASN1.valueOf(getId());
        if (getASN1Cls() == ASN1Cls.汎用) {
            if (n2 == ASN1.拡張) {
                ele = doc.createElement("struct");
                ele.setAttribute("tag", getTag().toString());
                throw new UnsupportedOperationException("Not supported yet.");
            } else {
                ele = doc.createElement(n2.toString());
                if (n2 != ASN1.SEQUENCE && n2 != ASN1.SET) {
                    ele.setAttribute("struct", "" + attrStruct);
                }

            }
        } else {
            ele = doc.createElement("struct");
            ele.setAttribute("class", Integer.toString(getASN1Class()));
            ele.setAttribute("tag", getTag().toString());
        }
        for (ASN1Object obj : list) {
            ele.appendChild(obj.encodeXML(doc));
        }
        if (inefinite) {
            ele.setAttribute("inefinite", "true");
        }
        return ele;
    }

    @Override
    public void decodeXML(Element xml) {
        String inf = xml.getAttribute("inefinite");

        if (inf != null && Boolean.parseBoolean(inf)) {
            inefinite = true;
        }
        NodeList child = xml.getChildNodes();
        for (int i = 0; i < child.getLength(); i++) {
            Node n = child.item(i);
            if (n instanceof Element) {
                ASN1Object o = ASN1Util.toASN1((Element) n);
                list.add(o);
            }
        }
    }

    private String getName() {
        ASN1 n = ASN1.valueOf(getId());
        ASN1Cls c = getASN1Cls();
        if (c != ASN1Cls.汎用 || n == ASN1.拡張) {
            return "c" + c + " " + n.toString() + " struct [" + getTag().toString() + "]";
        } else {
            return n.toString();
        }
    }

    String plusIndent(String base) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new StringReader(base));
            String line = reader.readLine();
            while (line != null) {
                sb.append("\r\n ").append(line);
                line = reader.readLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(ASN1Struct.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }

    /**
     * 見やすくするだけ 再エンコード不可
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName() + " EOF:" + inefinite + " {");
        for (ASN1Object obj : list) {
            sb.append(plusIndent(obj.toString()));
        }
        sb.append("\r\n}");
        return sb.toString();
    }

    /**
     * 特定位置のものを取得する
     *
     * @param offsets
     * @return
     */
    public ASN1Object get(int... offsets) {
        ASN1Object obj = this;

        for (int i = 0; i < offsets.length; i++) {
            if (obj instanceof ASN1Struct) {
                obj = ((ASN1Struct) obj).list.get(offsets[i]); // listにしないとネストする
            } else {
                throw new NullPointerException();
                //                    return null;
            }
        }
        return obj;
    }

    /**
     * キャストが便利なだけ
     *
     * @param index
     * @return
     */
    public ASN1Struct getStruct(int... index) {
        return (ASN1Struct) get(index);
    }

    /**
     * 同タグでn番目
     *
     * @param tag
     * @param index
     * @return
     * @see #tagSize( BigInteger )
     */
    public ASN1Object get(BigInteger tag, int index) {
        for (int n = 0; n < list.size(); n++) {
            if (list.get(n).getTag().equals(tag)) {
                index--;
                if (index < 0) {
                    return list.get(n);
                }
            }
        }
        return null;
    }

    void set(int index, ASN1Object obj) {
        list.set(index, obj);
    }

    /**
     * オブジェクトを奥深くに追加/更新する.
     * @param obj
     * @param index 巧妙な位置
     */
    void set(ASN1Object obj, int... index) {
        if (index.length > 1) {
            int[] idx = new int[index.length - 1];
            System.arraycopy(index, 1, idx, 0, idx.length);
            getStruct(index[0]).set(obj, idx);
        } else {
            list.set(index[0], obj);
        }
    }

    public void add(ASN1Object obj) {
        list.add(obj);
    }

    public void add(int index, ASN1Object obj) {
        list.add(index, obj);
    }

    /**
     * オブジェクトを奥深くに追加する.
     * @param obj
     * @param index 巧妙な位置
     */
    public void add(ASN1Object obj, int... index) {
        if (index.length > 1) {
            int[] idx = new int[index.length - 1];
            System.arraycopy(index, 1, idx, 0, idx.length);
            getStruct(index[0]).add(obj, idx);
        } else {
            list.add(index[0], obj);
        }
    }

    /**
     * タグ限定サイズ
     *
     * @param tag
     */
    public int tagSize(BigInteger tag) {
        int count = 0;
        for (ASN1Object obj : list) {
            if (obj.getTag().equals(tag)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 型を変えたい このままでは使わない方がいい
     *
     * @return
     */
    @Override
    public List<ASN1Object> getValue() {
        return list;
    }

    /**
     *
     * @param val
     */
    @Override
    public void setValue(List<ASN1Object> val) {
        list = val;
    }
}
