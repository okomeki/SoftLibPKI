/*
 * Copyright 2022 okome.
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
package net.siisise.ietf.pkcs5;

import java.security.MessageDigest;
import java.util.Arrays;
import net.siisise.security.block.Block;

/**
 * RFC 8018 6. Encryption Schemes
 * パスワードからIVと暗号鍵を生成して暗号化する方式.
 * 費用順的なPADDINGと同じなので暗号の方に
 * @deprecated DES,RC2などを使用するため旧式
 */
public class PBES1 implements PBES {

    private Block block;
    private byte[] k;
    private byte[] iv;
    
    /**
     *
     * A.3.
     * pbeWithMD2AndDES
     * pbeWithMD2AndRC2
     * pbeWithMD5AndDES
     * pbeWithMD5AndRC2
     * pbeWithSHA1AndDES
     * pbeWithSHA1AndRC2
     * @param block DES/CBC RC2/CBC
     * @param digest PBKDFのパラメータ
     * @param password PBKDFのパラメータ
     * @param salt PBKDFのパラメータ
     * @param c ハッシュ繰り返し数 PBKDFのパラメータ
     */
    public void init(Block block, MessageDigest digest, byte[] password, byte[] salt, int c) {
        this.block = block;
        byte[] dk = PBKDF1.pbkdf1(digest, password, salt, c, 16);
        k = new byte[8];
        iv = new byte[8];
        System.arraycopy(dk, 0, k, 0, 8);
        System.arraycopy(dk, 8, iv, 0, 8);
        block.init(k,iv);
    }

    /** 2回目があれば */
    public void init() {
        block.init(k,iv);
    }

    /**
     * 基本1回のみ。2回目以降初期化せずに使えるかもしれない
     * 毎回padding付き
     * @param message M メッセージ
     * @return C = encrypt(EM)
     */
    @Override
    public byte[] encrypt(byte[] message) {
        // RFC 1423 の padding
        int padlen = 8 - (message.length % 8);
        int len = message.length + padlen;
        byte[] em = new byte[len];
        System.arraycopy(message, 0, em, 0, message.length);
        Arrays.fill(em, message.length, em.length, (byte)padlen);
        return block.encrypt(em, 0, em.length);
    }
    
    /**
     * 64bit系 DES CBC または RC2 CBC
     * 
     * @param block DES CBC または RC2 CBC
     * @param digest MD2, MD5, SHA-1
     * @param message メッセージ
     * @param password パスワード
     * @param salt 8オクテット
     * @param c 繰り返し 1000ぐらい
     * @return 
     */
    public static byte[] encrypt(Block block, MessageDigest digest, byte[] message, byte[] password, byte[] salt, int c) {
        PBES1 pb = new PBES1();
        pb.init(block, digest, password, salt, c);
        return pb.encrypt(message);
    }
    
    /**
     *
     * @param message padding されているもの
     * @return デコードされたもの
     */
    @Override
    public byte[] decrypt(byte[] message) {
//        block.init(iv, k);
        byte[] em = block.decrypt(message, message.length);
        int d = em[em.length - 1];
        if ( d > 8 || d < 1) {
            throw new SecurityException();
        }
        int len = em.length - d;
        byte[] dec = new byte[len];
        System.arraycopy(em, 0, dec, 0, len);
        return dec;
    }
    
    /**
     *
     * @param block DES CBC または RC2 CBC
     * @param digest MD2, MD5 または SHA-1
     * @param message
     * @param password
     * @param salt
     * @param c
     * @return
     */
    public static byte[] decrypt(Block block, MessageDigest digest, byte[] message, byte[] password, byte[] salt, int c) {
        PBES1 pb = new PBES1();
        pb.init(block, digest, password, salt, c);
        return pb.decrypt(message);
    }
    
}
