package com.savewhy.app;
import java.security.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import static org.junit.Assert.*;
public class ReceiptTest {
    private static KeyPair pair;private static String key;
    @BeforeClass public static void setup()throws Exception{KeyPairGenerator generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);pair=generator.generateKeyPair();key=Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());}
    private String data(String product,int state)throws Exception{return new JSONObject().put("packageName","com.savewhy.app").put("productId",product).put("purchaseState",state).put("purchaseToken","test-token").toString();}
    private String sign(String data)throws Exception{Signature signature=Signature.getInstance("SHA1withRSA");signature.initSign(pair.getPrivate());signature.update(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));return Base64.getEncoder().encodeToString(signature.sign());}
    @Test public void onlyAuthenticPurchasedReceiptUnlocks()throws Exception{String data=data("savewhy_lifetime",0);assertTrue(ReceiptVerifier.verified(key,data,sign(data),"com.savewhy.app","savewhy_lifetime"));}
    @Test public void pendingWrongProductAndWrongPackageStayLocked()throws Exception{String data=data("savewhy_lifetime",4);assertFalse(ReceiptVerifier.verified(key,data,sign(data),"com.savewhy.app","savewhy_lifetime"));data=data("other",0);assertFalse(ReceiptVerifier.verified(key,data,sign(data),"com.savewhy.app","savewhy_lifetime"));data=data("savewhy_lifetime",0);assertFalse(ReceiptVerifier.verified(key,data,sign(data),"other.app","savewhy_lifetime"));}
    @Test public void alteredPayloadCannotUseOldSignature()throws Exception{String data=data("savewhy_lifetime",4),signature=sign(data);String modified=data("savewhy_lifetime",0);assertFalse(ReceiptVerifier.verified(key,modified,signature,"com.savewhy.app","savewhy_lifetime"));}
    @Test public void absentOrInvalidConfigurationFailsClosed()throws Exception{String data=data("savewhy_lifetime",0);assertFalse(ReceiptVerifier.verified("",data,sign(data),"com.savewhy.app","savewhy_lifetime"));assertFalse(ReceiptVerifier.verified("invalid",data,"invalid","com.savewhy.app","savewhy_lifetime"));}
}
