package com.savewhy.app;
import org.json.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
/** Verify Google Play's signed receipt; a preferences flag never grants access. */
public final class ReceiptVerifier {
    private ReceiptVerifier(){}
    public static boolean verified(String key,String data,String signed,String packageName,String product){
        if(key==null||key.trim().isEmpty()||data==null||signed==null)return false;
        try {
            PublicKey publicKey=KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(key.replaceAll("\\s",""))));
            Signature signature=Signature.getInstance("SHA1withRSA");signature.initVerify(publicKey);signature.update(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));if(!signature.verify(Base64.getDecoder().decode(signed.replaceAll("\\s",""))))return false;
            JSONObject receipt=new JSONObject(data);boolean matches=product.equals(receipt.optString("productId",""));JSONArray products=receipt.optJSONArray("productIds");if(products!=null)for(int i=0;i<products.length();i++)if(product.equals(products.getString(i)))matches=true;return packageName.equals(receipt.getString("packageName"))&&matches&&receipt.getInt("purchaseState")==0&&!receipt.optString("purchaseToken",receipt.optString("token","")).isEmpty();
        }catch(Exception e){return false;}
    }
}
