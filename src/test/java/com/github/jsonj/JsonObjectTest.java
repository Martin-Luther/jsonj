/**
 * Copyright (c) 2011, Jilles van Gurp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.jsonj;

import static com.github.jsonj.tools.JsonBuilder.$;
import static com.github.jsonj.tools.JsonBuilder._;
import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.field;
import static com.github.jsonj.tools.JsonBuilder.fromObject;
import static com.github.jsonj.tools.JsonBuilder.nullValue;
import static com.github.jsonj.tools.JsonBuilder.object;
import static com.github.jsonj.tools.JsonBuilder.primitive;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.hamcrest.CoreMatchers;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.jsonj.exceptions.JsonTypeMismatchException;
import com.github.jsonj.tools.JsonBuilder;
import com.jillesvangurp.efficientstring.EfficientString;

@Test
public class JsonObjectTest {
	@DataProvider
	public Object[][] equalPairs() {
		return new Object[][] {
			{ object().put("a", object().put("b", object().put("c", "d").get()).get()).get(),
				object().put("a", object().put("b", object().put("c", "d").get()).get()).get() },
			{ object().get(), object().get() },
			{ object().put("a", "a").put("b", 42).put("c", true).put("d", array("foo", "bar")).put("e", primitive((String) null)).get(),
				object().put("b", 42).put("a", "a").put("c", true).put("d", array("foo", "bar")).put("e", primitive((String) null)).get() } };
	}

	@Test(dataProvider = "equalPairs")
	public void shouldBeEqualToSelf(final JsonObject left, final JsonObject right) {
		Assert.assertTrue(left.equals(left)); // reflexive
		Assert.assertTrue(right.equals(right)); // reflexive
		Assert.assertTrue(left.equals(right)); // symmetric
		Assert.assertTrue(right.equals(left)); // symmetric
	}

	@Test(dataProvider="equalPairs")
	public void shouldHaveSameHashCode(final JsonObject left, final JsonObject right) {
		Assert.assertEquals(left.hashCode(), right.hashCode());
	}

	@DataProvider
	public Object[][] unEqualPairs() {
		return new Object[][] {
				{ object().put("a", "b").get(),
					object().put("a", "b").put("b", 42).get() },
					{ object().put("a", "b").get(), null },
					{ object().put("a", "b").get(), primitive(42) },
					{ object().put("a", 42).get(), object().put("a", 41).get() } };
	}

	@Test(dataProvider = "unEqualPairs")
	public void shouldNotBeEqual(final JsonObject o, final JsonElement e) {
		Assert.assertNotSame(o, e);
	}

	public void shouldExtractValue() {
		JsonObject o = object().put("a",
				object().put("b", object().put("c", "d").get()).get()).get();
		Assert.assertEquals("d", o.get("a", "b", "c").asPrimitive().asString());
	}

	public void shouldCreateArray() {
		JsonObject object = new JsonObject();
		JsonArray createdArray = object.getOrCreateArray("a","b","c");
		createdArray.add("1");
		Assert.assertTrue(object.getArray("a","b","c").contains("1"), "array should have been added to the object");
	}

	public void shouldReturnExistingArray() {
		JsonObject object = object().put("a", object().put("b", array("foo")).get()).get();
		Assert.assertTrue(object.getOrCreateArray("a","b").contains("foo"));
	}

	@Test(expectedExceptions=JsonTypeMismatchException.class)
	public void shouldThrowExceptionOnElementThatIsNotAnArray() {
		JsonObject object = object().put("a", object().put("b", 42).get()).get();
		object.getOrCreateArray("a","b");
	}

	public void shouldCreateObject() {
		JsonObject object = new JsonObject();
		JsonObject createdObject = object.getOrCreateObject("a","b","c");
		createdObject.put("foo", "bar");
		Assert.assertTrue(object.getString("a","b","c", "foo").equals("bar"), "object should have been added");
	}

	public void shouldReturnExistingObject() {
		JsonObject object = object().put("a", object().put("b", object().put("foo","bar").get()).get()).get();
		JsonObject orCreateObject = object.getOrCreateObject("a","b");
		Assert.assertTrue(orCreateObject.getString("foo").equals("bar"), "return the object with foo=bar");
	}

	@Test(expectedExceptions=JsonTypeMismatchException.class)
	public void shouldThrowExceptionOnElementThatIsNotAnObject() {
		JsonObject object = object().put("a", object().put("b", 42).get()).get();
		object.getOrCreateObject("a","b");
	}

	public void shouldDoDeepClone() {
		JsonObject o = object().put("1", 42).put("2", "Hello world").get();
		JsonObject cloneOfO = o.deepClone();
		Assert.assertTrue(o.equals(cloneOfO));
		o.remove("1");
		Assert.assertFalse(o.equals(cloneOfO));
		o.put("1", cloneOfO);
		Object clone = o.clone();
		Assert.assertTrue(o.equals(clone));
		cloneOfO.remove("2");
		Assert.assertFalse(o.equals(clone));
	}

	public void shouldRemoveEmptyElements() {
		JsonObject jsonObject = object().put("empty", object().get()).put("empty2", nullValue()).put("empty3", new JsonArray()).get();
		Assert.assertTrue(jsonObject.isEmpty(), "object should be empty");
		jsonObject.removeEmpty();
		assertThat("should leave empty objects",jsonObject.getObject("empty"), is(object().get()));
		Assert.assertEquals(jsonObject.get("empty2"), null);
		Assert.assertEquals(jsonObject.get("empty3"), null);
	}

	public void shouldReturn2ndEntry() {
	    assertThat(object().put("1", 1).put("2", 2).put("3", 3).get().get(1).getValue(), is((JsonElement)primitive(2)));
	}

    public void shouldReturnFirstEntry() {
        assertThat(object().put("1", 1).put("2", 2).put("3", 3).get().first().getValue(), is((JsonElement)primitive(1)));
    }

    public void shouldSupportJavaSerialization() throws IOException, ClassNotFoundException {
        JsonObject object = object().put("42",42).get();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();
        byte[] bytes = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object object2 = ois.readObject();
        assertTrue(object.equals(object2));
    }

    public void shouldPutBuilder() {
        JsonBuilder builder = object().put("foo", "bar");
        JsonObject object1 = object().put("foobar",builder).get();
        JsonObject object2 = new JsonObject();
        object2.put("foobar", builder);
        JsonObject object3 = new JsonObject();
        object3.put("foobar", fromObject(builder));

        assertThat(object1, is(object2));
        assertThat(object1, is(object3));
        assertThat(object1.toString(), is("{\"foobar\":{\"foo\":\"bar\"}}"));
    }

    public void shouldHandleJsonNullsOnGet() {
        JsonObject o = object().put("x", JsonPrimitive.JSON_NULL).get();

        // should return the json null
        assertThat((JsonPrimitive)o.get("x"), CoreMatchers.notNullValue());
        // these should all return a java null
        assertThat(o.getInt("x"), CoreMatchers.nullValue());
        assertThat(o.getLong("x"), CoreMatchers.nullValue());
        assertThat(o.getFloat("x"), CoreMatchers.nullValue());
        assertThat(o.getDouble("x"), CoreMatchers.nullValue());
        assertThat(o.getBoolean("x"), CoreMatchers.nullValue());
        assertThat(o.getString("x"), CoreMatchers.nullValue());
        assertThat(o.getArray("x"), CoreMatchers.nullValue());
        assertThat(o.getObject("x"), CoreMatchers.nullValue());
    }

    public void shouldAllowPutOfNullValue() {
        JsonElement x=null;
        JsonObject o = object().put("x", x).get();
        assertThat(o.getInt("x"), CoreMatchers.nullValue());
    }

    public void shouldAddFields() {
        JsonObject object = object(field("meaningoflife", 42), field("foo", primitive("bar")), field("list",array("stuff")));
        assertThat(object.getInt("meaningoflife"), is(42));
        assertThat(object.getString("foo"), is("bar"));
        assertThat(object.getArray("list").get(0).asString(), is("stuff"));
    }

    public void shouldAddFieldsShortNotation() {
        JsonObject object = $(
            _("meaningoflife", 42),
            _("foo", primitive("bar")),
            _("list",$("stuff"))
        );
        assertThat(object.getInt("meaningoflife"), is(42));
        assertThat(object.getString("foo"), is("bar"));
        assertThat(object.getArray("list").get(0).asString(), is("stuff"));
    }

    public void shouldSupportConcurrentlyCreatingNewKeys() throws InterruptedException {
        // note. this test did never actually trigger the race condition so only limited confidence here.
        // this is the best I've come up with so far for actually triggering the conditions this breaks
        int factor = 666;
        int startIndex = EfficientString.nextIndex();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(20);
        final JsonObject o = new JsonObject();
        // this should create some potential for the race condition to trigger since it rapidly creates the same keys
        int total = 100000;
        for(int i=0;i<total;i++) {
            // make sure we are actually creating new Strings with no overlap with the other tests
            final String str="shouldSupportConcurrentlyCreatingNewKeys-"+ (i/factor);
            executorService.execute(new Runnable() {

                @Override
                public void run() {
                    o.put(str, str); // this should never fail with null key because of the (hopefully) now fixed EfficientString
                    EfficientString e = EfficientString.fromString(str);
                    assertThat(EfficientString.fromString(str), is(e));
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(2, TimeUnit.SECONDS);
        assertThat(EfficientString.nextIndex()-startIndex, is(total/factor+1));

    }
}
