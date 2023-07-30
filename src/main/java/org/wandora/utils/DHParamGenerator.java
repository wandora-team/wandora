/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * 
 *
 * DHParamGenerator.java
 *
 * Created on August 30, 2004, 11:50 AM
 */

package org.wandora.utils;
import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;

import javax.crypto.spec.DHParameterSpec;

/**
 *
 * Use this class to generate modulus and base that can be used with DHParameterSpec in
 * Diffie-Hellman key exchange.
 *
 * @author  olli
 */
public class DHParamGenerator {
    
    /** Creates a new instance of DHParamGenerator */
    public DHParamGenerator() {
    }

    public static void main(String args[]) throws Exception {
        AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
        paramGen.init(1024);
        AlgorithmParameters params = paramGen.generateParameters();
        DHParameterSpec paramSpec = (DHParameterSpec)params.getParameterSpec(DHParameterSpec.class);
        BigInteger modulus=paramSpec.getP();
        BigInteger base=paramSpec.getG();
        System.out.print("private static final BigInteger DHModulus = new BigInteger(1,new byte[]{");
        outputBytes(modulus.toByteArray());
        System.out.println("\n});");
        System.out.print("private static final BigInteger DHBase = new BigInteger(1,new byte[]{");
        outputBytes(base.toByteArray());
        System.out.println("\n});");
    }
    public static void outputBytes(byte[] bytes){
        for(int i=0;i<bytes.length;i++){
            if(i!=0) System.out.print(",");
            if(i%4==0) System.out.println();
            int b=bytes[i];
            if(b<0) b+=256;
            String s=Integer.toHexString(b);
            if(s.length()==1) s="0"+s;
            System.out.print("(byte)0x"+s);
        }
    }
    
}
