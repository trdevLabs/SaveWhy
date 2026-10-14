package com.savewhy.app;
import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class LanguageTest {
    private Context language(String code) { Context context=RuntimeEnvironment.getApplication();Configuration config=new Configuration(context.getResources().getConfiguration());config.setLocale(Locale.forLanguageTag(code));return context.createConfigurationContext(config); }
    @Test public void turkishAndEnglishLabelsResolve() { assertEquals("Watch",I18n.text(language("en"),"İzle")); assertEquals("İzle",I18n.text(language("tr"),"İzle")); }
    @Test public void unknownSystemLanguageUsesEnglish() { assertEquals("Watch",I18n.text(language("ja"),"İzle")); }
    @Test public void customContentStaysUnchanged() { assertEquals("Benim özel içeriğim",I18n.text(language("en"),"Benim özel içeriğim")); }
    @Test public void storedIntentIsIndependentOfLanguage() throws Exception { Card c=new Card("id","https://example.com","User title","İzle","Private note",1,false,java.util.Collections.emptyList());assertEquals("İzle",CardCodec.decode(CardCodec.encode(java.util.Collections.singletonList(c))).get(0).why); }
}
