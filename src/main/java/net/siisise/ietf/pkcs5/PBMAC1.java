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

import net.siisise.security.mac.MAC;

/**
 * PKCS #5
 */
public class PBMAC1 {

    private final PBKDF2 kdf;
//    private MAC mac;
    
    /**
     * 
     * @param kdf MD 設定済みのPBKDF2
     */
    public PBMAC1(PBKDF2 kdf) {
        this.kdf = kdf;
    }
    
    public void init(MAC mac) {
        kdf.init(mac);
//        this.mac = mac;
    }

    /**
     * 
     * たぶんこんなかんじ? 
     * @param src
     * @param mac HMAC-XXXX
     * @param password
     * @param salt
     * @param c 繰り返し最小1000ぐらいから
     * @return 
     */
    public byte[] mac(byte[] src, MAC mac, byte[] password, byte[] salt, int c) {
        int dkLen = mac.getMacLength();
        byte[] dk = kdf.pbkdf(password, salt, c, dkLen);
        mac.init(dk);
        return mac.doFinal(src);
    }
}
