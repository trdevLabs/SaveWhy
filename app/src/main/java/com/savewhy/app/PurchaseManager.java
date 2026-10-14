package com.savewhy.app;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.*;
import com.android.billingclient.api.*;
import java.util.*;

/** One-time, non-consumable Play product. No subscription and no debug unlock. */
public final class PurchaseManager implements AutoCloseable {
    public static final String PRODUCT="savewhy_lifetime";
    private final Activity activity;private final SharedPreferences receipts;private final Handler main=new Handler(Looper.getMainLooper());
    private BillingClient client;private ProductDetails product;private ProductDetails.OneTimePurchaseOfferDetails offer;private boolean closed,connecting;
    public String status="Google Play satın alma şu anda kullanılamıyor.";public Runnable changed=()->{};
    public PurchaseManager(Activity activity){this.activity=activity;receipts=activity.getSharedPreferences("purchase_receipt",Context.MODE_PRIVATE);if(!BuildConfig.PLAY_BILLING_PUBLIC_KEY.trim().isEmpty()){client=BillingClient.newBuilder(activity).setListener((result,purchases)->{
        if(result.getResponseCode()==BillingClient.BillingResponseCode.OK&&purchases!=null)for(Purchase purchase:purchases)process(purchase);
        else if(result.getResponseCode()==BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED)refresh();
        else notifyStatus(result.getResponseCode()==BillingClient.BillingResponseCode.USER_CANCELED?"Satın alma iptal edildi.":"Satın alma tamamlanamadı. Tekrar deneyebilirsin.");
    }).enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).enableAutoServiceReconnection().build();connect();}}
    public boolean premium(){return ReceiptVerifier.verified(BuildConfig.PLAY_BILLING_PUBLIC_KEY,receipts.getString("data",""),receipts.getString("signature",""),activity.getPackageName(),PRODUCT);}
    public String price(){return offer==null?"":offer.getFormattedPrice();}
    public boolean available(){return client!=null&&client.isReady()&&product!=null&&offer!=null&&!premium();}
    private void notifyStatus(String text){main.post(()->{if(!closed){status=text;changed.run();}});}
    private void connect(){if(closed||client==null||connecting)return;if(client.isReady()){refresh();return;}connecting=true;status="Google Play’e bağlanılıyor…";
        client.startConnection(new BillingClientStateListener(){public void onBillingSetupFinished(BillingResult result){connecting=false;if(closed)return;if(result.getResponseCode()==BillingClient.BillingResponseCode.OK)refresh();else notifyStatus("Google Play satın alma şu anda kullanılamıyor.");}public void onBillingServiceDisconnected(){connecting=false;product=null;offer=null;notifyStatus("Google Play bağlantısı kesildi. Tekrar deneyebilirsin.");}});
    }
    public void refresh(){if(closed||client==null)return;if(!client.isReady()){connect();return;}
        client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(Collections.singletonList(QueryProductDetailsParams.Product.newBuilder().setProductId(PRODUCT).setProductType(BillingClient.ProductType.INAPP).build())).build(),(result,details)->{
            if(closed)return;product=null;offer=null;
            if(result.getResponseCode()==BillingClient.BillingResponseCode.OK)for(ProductDetails item:details.getProductDetailsList())if(PRODUCT.equals(item.getProductId())){List<ProductDetails.OneTimePurchaseOfferDetails> offers=item.getOneTimePurchaseOfferDetailsList();if(offers!=null&&!offers.isEmpty()){product=item;offer=offers.get(0);break;}}
            notifyStatus(product==null?"Google Play satın alma şu anda kullanılamıyor.":"Tek seferlik ödeme. Abonelik yok.");
        });
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),(result,purchases)->{
            if(closed||result.getResponseCode()!=BillingClient.BillingResponseCode.OK)return;
            boolean owned=false;for(Purchase p:purchases)if(p.getProducts().contains(PRODUCT)){if(p.getPurchaseState()==Purchase.PurchaseState.PURCHASED)owned=true;process(p);}
            if(!owned){receipts.edit().remove("data").remove("signature").commit();main.post(()->{if(!closed)changed.run();});}
        });
    }
    private void process(Purchase purchase){if(closed||!purchase.getProducts().contains(PRODUCT))return;
        if(purchase.getPurchaseState()==Purchase.PurchaseState.PENDING){notifyStatus("Ödeme bekliyor. Tamamlandığında sınırsız kayıt açılacak.");return;}
        if(purchase.getPurchaseState()!=Purchase.PurchaseState.PURCHASED||!ReceiptVerifier.verified(BuildConfig.PLAY_BILLING_PUBLIC_KEY,purchase.getOriginalJson(),purchase.getSignature(),activity.getPackageName(),PRODUCT)){notifyStatus("Satın alma doğrulanamadı.");return;}
        Runnable unlock=()->{if(closed)return;if(!receipts.edit().putString("data",purchase.getOriginalJson()).putString("signature",purchase.getSignature()).commit()){notifyStatus("Satın alma kaydedilemedi. Satın almayı geri yükle.");return;}notifyStatus("Sınırsız kayıt açıldı. Teşekkürler!");};
        if(purchase.isAcknowledged())unlock.run();else client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build(),result->{if(result.getResponseCode()==BillingClient.BillingResponseCode.OK)unlock.run();else notifyStatus("Satın alma onayı bekliyor. Geri yüklemeyi dene.");});
    }
    public void buy(){if(premium()){notifyStatus("Sınırsız kayıt zaten açık.");return;}if(!available()){connect();notifyStatus("Google Play satın alma şu anda kullanılamıyor.");return;}
        BillingFlowParams.ProductDetailsParams details=BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product).setOfferToken(offer.getOfferToken()).build();
        BillingResult result=client.launchBillingFlow(activity,BillingFlowParams.newBuilder().setProductDetailsParamsList(Collections.singletonList(details)).build());if(result.getResponseCode()!=BillingClient.BillingResponseCode.OK)notifyStatus("Satın alma başlatılamadı. Tekrar deneyebilirsin.");
    }
    @Override public void close(){closed=true;main.removeCallbacksAndMessages(null);if(client!=null)client.endConnection();}
}
