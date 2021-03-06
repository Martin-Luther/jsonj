package com.github.jsonj;

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.nullValue;
import static com.github.jsonj.tools.JsonBuilder.object;
import static com.github.jsonj.tools.JsonBuilder.primitive;
import static com.github.jsonj.tools.JsonBuilder.set;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.github.jsonj.tools.JsonBuilder;

@Test
public class JsonSetTest {
    public void shouldAddNoDuplicateStrings() {
        JsonSet set = new JsonSet();
        set.add("42");
        set.add("42");
        assertThat(set.size(), is(1));
    }

    public void shouldAddNoDuplicateStringsVarargs() {
        JsonSet set = new JsonSet();
        set.add("42", "42", "43");
        assertThat(set.size(), is(2));
    }

    public void shouldAddNoDuplicatePrimitives() {
        JsonSet set = new JsonSet();
        set.add(primitive("42"), primitive("42"), primitive("43"));
        assertThat(set.size(), is(2));
    }

    public void shouldAddNoDuplicatesFromCollection() {
        List<Serializable> collection = Arrays.asList("42",primitive("42"), array(42),array(42));

        JsonSet set = new JsonSet();
        set.addAll(collection);
        assertThat(set.size(), is(2));
    }

    public void shouldAddNoDuplicatesOnConstruction() {
        List<Serializable> collection = Arrays.asList("42",primitive("42"), array(42),array(42));
        JsonSet set = new JsonSet(collection);
        assertThat(set.size(), is(2));
    }

    public void shouldAddAllElementsOfArray() {
        JsonSet set = new JsonSet();
        set.addAll(array(1,2,1));
        assertThat(set.size(), is(2));
    }

    public void shouldIterateOverObjects() {
        int i=0;
        JsonSet set = new JsonSet();
        set.add(object().put("foo", "bar").get());
        set.add(object().put("bar", "foo").get());
        for(@SuppressWarnings("unused") JsonObject o: set.objects()) {
            i++;
        }
        assertThat(i,is(2));
    }

    public void shouldAddJsonBuilderObjects() {
        JsonBuilder builder = object().put("foo", "bar");
        JsonSet set = new JsonSet();
        set.add(builder,builder);
        assertThat(set.size(), is(1));
        assertThat(set.toString(),is("[{\"foo\":\"bar\"}]"));
    }

    public void shouldSupportNulls() {
        JsonSet set = set(null, nullValue());
        assertTrue(set.contains(nullValue()),"should contain null");
        assertThat(set.size(), is(1));
    }
}
