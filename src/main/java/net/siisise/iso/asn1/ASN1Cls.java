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

public enum ASN1Cls {
    汎用(0),
    応用(1),
    コンテキスト特定(2),
    プライベート(3);

    byte cls;

    ASN1Cls(int c) {
        cls = (byte) c;
    }

    public static ASN1Cls valueOf(int id) {
        if (id >= 0x4) {
            return null;
        }
        return ASN1Cls.values()[id];
    }
}
