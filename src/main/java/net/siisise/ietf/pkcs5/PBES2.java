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

import net.siisise.security.block.Block;
import net.siisise.security.mac.MAC;

/**
 * RFC 8018 6.2. PBES2
 *
 */
public class PBES2 implements PBES {
    private final PBKDF2 kdf;
    private Block block;
    
    PBES2(PBKDF2 kdf) {
        this.kdf = kdf;
    }

    PBES2(MAC hmac) {
        kdf = new PBKDF2(hmac);
    }
    
    PBES2() {
        kdf = new PBKDF2();
    }
    
    /**
     * 
     * @param block XXX-CBC
     * @param hmac
     * @param password password
     * @param salt salt
     * @param c iteration count
     */
    public void init(Block block, MAC hmac, byte[] password, byte[] salt, int c) {
//        digest.getDigestLength();
        int[] nlen = block.getParamLength();
        kdf.init(hmac);
        byte[] dk = kdf.pbkdf(password, salt, c, nlen[0] + nlen[1]);
        byte[] k = new byte[nlen[0]];
        byte[] iv = new byte[nlen[1]];
        System.arraycopy(dk,0,k,0,nlen[0]);
        System.arraycopy(dk,nlen[0],iv,0,nlen[1]);
        
        block.init(k, iv);
        this.block = block;
    }
    
    @Override
    public byte[] encrypt(byte[] message) {
        // ToDo: padding
        return block.encrypt(message, 0, message.length);
    }

    @Override
    public byte[] decrypt(byte[] message) {
        return block.decrypt(message, 0, message.length);
    }
}
