package com.savewhy.app;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class LimitTest {
    private Card card(int n){return new Card("id"+n,"https://example.com/"+n,"Title","İzle","Reason",1,false,Collections.emptyList());}
    private CardStore store(boolean premium)throws Exception{CardStore s=new CardStore(RuntimeEnvironment.getApplication().getFilesDir(),()->premium);s.load();return s;}
    @Test public void twentySavesAllowedAndTwentyFirstRejected()throws Exception {CardStore s=store(false);for(int i=0;i<20;i++)s.save(card(i));assertThrows(CardStore.LimitException.class,()->s.save(card(20)));assertEquals(20,s.all().size());s.load();assertEquals(20,s.all().size());}
    @Test public void editingAndDeletingRemainAvailableAtLimit()throws Exception {CardStore s=store(false);for(int i=0;i<20;i++)s.save(card(i));Card changed=s.find("id0").copy();changed.done=true;s.save(changed);assertTrue(s.find("id0").done);s.delete("id1");s.save(card(21));assertEquals(20,s.all().size());}
    @Test public void importBeyondLimitIsAtomic()throws Exception {CardStore s=store(false);for(int i=0;i<19;i++)s.save(card(i));assertThrows(CardStore.LimitException.class,()->s.merge(Arrays.asList(card(20),card(21))));s.load();assertEquals(19,s.all().size());}
    @Test public void licensedCapacityAndLegacyReadingArePreserved()throws Exception {CardStore premium=store(true);for(int i=0;i<25;i++)premium.save(card(i));CardStore free=store(false);assertEquals(25,free.all().size());free.save(free.find("id0").copy());assertThrows(CardStore.LimitException.class,()->free.save(card(30)));}
}
