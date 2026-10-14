package com.savewhy.app;
import java.util.Locale;
import android.content.Context;
import android.graphics.Typeface;
import androidx.core.content.res.ResourcesCompat;
final class LanguageOptions {
    static final String[] CODES={"","tr","en","zh-Hans","hi","es","ar","fr","bn","pt","id","ur","de"};
    static final String[] NAMES={"","Türkçe","English","中文（简体）","हिन्दी","Español","العربية","Français","বাংলা","Português","Bahasa Indonesia","اردو","Deutsch"};
    private LanguageOptions(){}
    static Typeface font(Context context){String language=context.getResources().getConfiguration().getLocales().get(0).getLanguage();for(String code:new String[]{"zh","hi","ar","bn","ur"})if(code.equals(language))return Typeface.create("sans-serif",Typeface.NORMAL);return ResourcesCompat.getFont(context,R.font.manrope_family);}
    static int index(String tags){if(tags==null||tags.isEmpty())return 0;String language=Locale.forLanguageTag(tags.split(",")[0]).getLanguage();for(int i=1;i<CODES.length;i++)if(Locale.forLanguageTag(CODES[i]).getLanguage().equals(language))return i;return 0;}
    static String name(String tags,String systemLabel){int index=index(tags);return index==0?systemLabel:NAMES[index];}
}
