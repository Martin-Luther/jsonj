# Introduction

JsonJ is a framework for working with json in Java the "proper" way without mappings or model classes. This means a json array is represented as a java.util.List, a dictionary is represented as a java.util.Map, etc.

There are several reasons why you might like jsonj

- it provides really convenient builder classes for quickly constructing json datastructures without going through the trouble of having to create model classes for your particular flavor of json, piecing together lists, maps, and other types and then serializing those, or generally having to do a lot of type casts, generics juggling. JsonJ makes this very easy.
- it's simple to use
- it provides powerful extensions to the Collections API that makes extracting things from lists and nested dictionaries very easy
- it's memory efficient: you can squeeze millions of json objects in a modest amout of RAM

There are probably more reasons you can find to like JsonJ, why not give it a try?

# Get it from Maven Central

```
<dependency>
    <groupId>com.jillesvangurp</groupId>
    <artifactId>jsonj</artifactId>
    <version>1.33</version>
</dependency>
```

Note. check for the latest version. I do not always update the readme.

# Why JsonJ

The whole point of json is a straightforward serialization and deserialization of simple, data structures consisting of primitives, lists and dictionaries that you will find in a lot of languages other than Java. In Java things are slightly more complicated because lists and dictionaries are mere classes rather than native types. This means for example that you have to deal with generics; you may want to pick alternate implementations of the Map interface or skip that altogether and instead use a completely different framework. For that reason, dealing with json in Java is a lot less straightforward than it would be in for example Ruby, Python, or Javascript.

There are many frameworks for handling Json in java. So why create another one? From my point of view these frameworks are all flawed in one or more of the following ways:

- They assume the user has model classes that need to be serialized and deserialized to and from java. IMHO this is often an antipattern that leads to large amounts of completely pointless code that needs to be tested and maintained.
- They use an internal representation of the various json types that is not very friendly to work with if you want to manipulate them directly. For example lists or maps that handle bare Objects.
- They don’t treat json as a first class entity and merely try to hide the fact that your data is stored as json.
- They don’t use the Java collections framework and mostly for no good reason. If you are going to implement something that looks like a list or a map, you might as well implement the interface.
- They use final classes, so your options to work around any limitations are limited and you are stuck with whatever is included.

Despite this, some of these frameworks are excellent and indeed widely used. I've used both Gson and Jackson extensively for exampel. Also, some of these frameworks are quite OK even if you do use them the way I want to use them. However, I still find them lacking in usability. JsonJ is my attempt to fix usability from the point of view of a coder who doesn't want to create model classes and who likes compact, type safe, non cluttered Java code. JsonJ delivers a highly usable framework that enables you to write code that has a low amount of verbosity and that gets out of the way.

The JsonJ API has been finetuned over several years of using it for real work. I have done my best to eliminate the need for any code that feels like it is overly repetitive or verbose (aka the *DRY principle*). Any time I write code using JsonJ that feels like I'm repeating myself, I fix it. 

I regard this as the key selling point of the library. When manipulating and creating json structures programmatically, it is important that you don't have to jump through hoops to extract elements, iterate over things, etc. To facilitate this, the framework provides a large amount of convenient methods that help *prevent verbosity* in the form of unnecessary class casts, null checks, type conversions, generic types, etc. No other Json framework for Java comes close to the level of usability of this framework when it comes to this. Most leave you to either develop your own classes or with the bare bones API of the Java collections framework.


# Features

The purpose of the JsonJ framework is to allow you to write code that manipulates json data structures, that has a low amount of verbosity compared to other frameworks.

I’ve used it for large scale data processing, which involves processing millions of objects, retaining large amounts of objects in memory, and loads of parsing and serialization.

## Design overview and API

JsonJ provides a few easy to understand classes that extend Java’s Collections framework. Extending the Collections framework means using a familiar, and powerful API that most Java coders already know how to use. The following classes are provided:

- `public class JsonObject implements Map<String,JsonElement>, JsonElement`
- `public class JsonArray extends ArrayList<JsonElement> implements JsonElement`
- `public class JsonSet extends JsonArray` (because sometimes having duplicate free lists is a nice thing)
- `public class JsonPrimitive implements JsonElement`

As the class signatures suggest, these classes provide a type safe alternative to simply using generic maps/lists of Objects since everything implements JsonElement. 

The `JsonElement` interface specifies a lot of convenience methods that allow you to do easy type checks and to convert to/from Java native type (when needed), etc.

Additionally a lot of methods are polymorphic and accept different types of objects (unlike the methods in the Collections framework). For example, the add method on JsonArray is polymorphic and automatically generates primitives if you add Strings, Booleans, or Numbers. The put on JsonObject behaves the same. The add method support varargs, so you can add multiple elements in one call.

JsonElement specifies as- methods that convert to common types or throw an unchecked exception if the conversion is impossible and there are also is- methods for checking the type conditionally. This gets rid of a lot of type checks, type casts, and other ugly code.

## JsonBuilder

To facilitate creation of json objects, arrays, and primitives a builder class is included that makes creation of nested json object structures as easy as it gets in Java. It is recommended to **use static imports and to add this class as a favorite in eclipse to facilitate autocompletion**. JsonBuilder is a one stop shop for constructing very complex json objects effortlessly.

### Classic builder pattern

This works by chaining method calls that all return a `JsonBuilder` and then using the `get()` method to get to the constructed object.

```java
JsonObject o=object()
  .put("aList",array(
    "lets start with a Json list of mixed Json types, all type safe and Generic",
    1,
    2,
    object()
      .put("meaningoflife",42),
      "note that the nested builder’s get() method is called for you; it understands what to do with the builder")
    )
  )    
 .put("here", "is a simple string field")
 .put("aSet",set("a set does not exist in json; it has only arrays","but sometimes it is useful to have sets", "in JsonJ a set is just a simple variation of a list", "and of course it implements Set<JsonElement>")
 .put("nestedlists",array(
    array(1,2),
    array(3,4)
  ))
 .get()
```

### Factory methods without builders

If this is too verbose for you, JsonJ also supports a different style of doing the same using simple static factory methods:

```java
JsonObject o=object(
    field("aList",array(
        1,
        2,
        object(field("meaningoflife",42)),
        "no more builder"))
    ),
    field("another", "element"),
    field("aSet",set(1,2,3),
    field("nestedlists",array(
       array(1,2),
       array(3,4)
     ))
);
```

This uses `Map.Entry` and the field factory method returns a `Entry<String,JsonElement>` instance that you can simply add to the `JsonObject`.

### Jquery style shortened factory methods

Jquery and other javascript libraries using characters like `$` and  `_` to cut down on key strokes. It turns out you can do the same in Java. So if the above is still too verbose, you can go even less verbose with just these two characters. `$` is an alias for `object()` or `array()`, depending on the parameter type. `_` is an alias for `field()`. **Arguably this is the most DRY and minimal way to construct json objects in Java; short of parsing a json string**.

```java
JsonObject o=$(
    _("aList",$(
        1,
        2,
        $(_("meaningoflife",42)),
        "no more builder"))
    ),
    _("another", "element"),
    _("aSet",set(1,2,3),
    _("nestedlists",$(
       $(1,2),
       $(3,4))
    )
);
```

Note. It has been brought to my attention that future versions of Java may drop support for `$` and  `_` as valid function names. I've not been able to verify whether this is correct but you have been warned. In any case, I regard this style as somewhat controversial still in terms of readability.

### Misc. other builder features

The builder class also provides methods to facilitate converting from existing Maps, Lists, and other objects. For example, the fromObject method takes any Java object and tries to do the right thing. 

## Parsing and serialization

- A thread safe `JsonParser` class is provided based on json-simple, and another `JsonParserNg` that is based on jackson. There’s little difference between them and they both use the same handler class for handling parse events, which is really the most critical thing in terms of performance.
- You can serialize using `toString()` or `prettyPrint()` on any JsonElement, or you can use the `JsonSerializer` class directly.

## JRuby integration

If you use jruby, you can seemlessly integrate jsonj using [jsonj-integration](https://github.com/jillesvangurp/jsonj-integration). This module uses monkey patching to add various methods to classes that allow you to convert between ruby style lists and hashes and JsonJ classes. Additionally, it adds [] and []= accessors to JsonArray, JsonSet, and JsonObject, which allows you to pretend it is all ruby. Finally, it adds `to_json` and `to_s` methods that do the right thing in Ruby as well. I use this module to mix Java and Ruby in one project and this comes in quite handy.

## Memory efficient

JsonJ implements several things that ensure it uses much less memory than might otherwise be the case:

- it uses my EfficientString library for object keys. This means instances are reused and stored as UTF8.
- it uses UTF8 byte arrays for storing String primitives.
- it uses a custom Map implementation that uses two ArrayLists. This uses a lot less memory than e.g. a LinkedHashMap. The downside is that key lookup is slower for objects with large amounts of keys. For small amounts it is actually somewhat faster. Generally, Json objects only have a handful of keys thus this is mostly a fair tradeoff that saves a lot of memory.

## Odd features you probably don't care about

- JsonElement implements `Serializable` so you can serialize jsonj objects using Java’s builtin serialization, if you really want to use that (hint, you shouldn’t).
- A utility class is included that allows you to convert json to and from XML, and to create DOM trees from json object structures. This can come in handy if you want to use e.g. xpath to query your json structures.
- Since I have a convenient builder class, I figured that I might as well add some code that generates code that uses the builder. So you can convert json to Java. I've used this to convert json queries I prototyped for elastic search into code. 

# Changelog
- 1.38 add geeky feature to generate java code to drive the JsonBuilder from the actual JsonElement. Useful to convert json fragments into code (e.g. a complex elastic search query).
- 1.37 Several fixes for escaping. It turns out we had two code paths for escaping and they weren't doing the same things. Now it uses the same codepath always. This mostly only affects edgecases where the json contains weird control characters that probably shouldn't be there to begin with.
- 1.35 Filter out characters that are not allowed in XML as well. This should fix some weird parsing issues I'm seeing with Jackson.
- 1.34 Filter out iso control codes during serialization.
- 1.33 JsonJ now deployed in Maven Central again.
- 1.30 Fix for incorrect shape type when using pointShape in GeoJsonSupport
- 1.29 Fix for efficient string race condition
- 1.27-1.28 Hopefully fix race condition with efficient string once and for all.
- 1.26 Support `$` as an alias for `array()` as well. Yay polymorphism! `JsonObject o=$(_("foo","bar"), _("list", $(1,2,3)))`
- 1.25 Create `$` and `_` aliases for `object` and `field`: `JsonObject o=$(_("foo","bar"), _("list", array(1,2,3)))` now works as well.
- 1.24 Support new style of creating JsonObjects: `JsonObject o=object(field("foo","bar"), field("list", array(1,2,3)))` now works. You can also add multiple field entries to an existing object or take the entrySet of an existing object and add those entries as fields to an object.
- 1.23
    - Add not null validation for object keys; prevents npe's 
- 1.22
    - Add arrays, strings, longs, doubles iterator methods to JsonArray so you can foreach over elements of that type without any conversions. We already had objects(). for example:
    ```java
        for(String s: jsonArray.strings()) {
            System.out.println(s); 
        }
    ```
- 1.21
    - Allow `null` values to be added as json null instead of rejecting them with an illegal argument exception.
- 1.20
    - handle json nulls when getting java boxed values and return a java null instead of throwing an npe
- 1.19
    - add asFloat, getFloat, and getLong methods
- 1.18
    - add asLong method for when asInt is too short
- 1.17
    - update efficientstring
- 1.16
    - support addition of JsonBuilder objects in arrays, sets, and objects )
    - support JsonSet in builder
- 1.15
    - add convenient objects iterable to JsonArray to allow iterating over JsonObjects without calling asObject on each JsonElement. Works for JsonSet as well.
- 1.14
    - Fix resource leak with jackson parser not being closed; make jackson not optional to fix classpath issues
- 1.13
    - Use new SimpleMap instead of HashMap in JsonObject. This is faster and more memory efficient for small amounts of keys
    - JsonArray now extends ArrayList instead of LinkedList
    - Add new parser based on jackson. Both parsers use the same handler so there is not much difference in performance.
- 1.12
    - Fix GeoJson type bugs
- 1.11
    - Add simple JsonSet implementation because sometimes it is just nice to have lists that behave like sets. This is a simple variation of the JsonArray that is not supported during parsing.
    - Fix bug with primitive(primitive(42)) ending up creating a primitive for the quoted value.
- 1.10
    - require java 1.7
    - move jsonj.rb, see my jsonj-integration project or install the gem from rubygems
    - ensure remove empty does not remove empty nested objects
    - make parseResource fall back to file input stream
    - use fixed version of efficientstring that is threadsafe
- 1.7 (and 1.5, 1.6)
    - misc API cleanup
    - no more key interning
    - adapted pretty printing
    - fix jruby integration to work again with the utf-8 related changes
- 1.4
    - Fix two bugs with the containsKey and serialization
- 1.3
    - Promote asInt,asDouble,asBoolean from JsonPrimitive to JsonElement for convenience.
- 1.2 - Use utf8 byte arrays to conserve memory
    - String values are now represented as UTF8 byte arrays rather than 16 bit characters
    - EfficientString is used for JsonObject keys.
    - JsonElement now has a new serialize method that serializes straight to an OutputStream. JsonSerializer now has a new serialize method to utilize this.
    - The String serialize(..) method now uses the new efficient serialization if pretty printing is turned off
- 1.1 - minor feature added
    - Added first and last convenience methods to JsonAray
- 1.0 - only one change relative to 0.9
    - Fixed JsonArray.equals to be more strict
- 0.9 - big release with many new features that resulted from months of using jsonj in my own project
    - fixes several serialization bugs; serialization and parsing is now very robust
    - array.addAll and the builder now play nice with collections
    - toString now serializes proper json on json strings: they now include quotes!; use asString instead).
    - added prettyPrint method to JsonElement that returns a pretty printed string representation of the element (uses JsonSerializer)
    - added a fromObject method to the builder that handles nested maps and lists and converts those to json
    - added methods to JsonArray to convert to native double, int, or string arrays
    - added asString, asDouble, asInt methods to JsonElement so that you don’t have to chain asPrimitive.asString anymore.
    - added a jsonj.rb script that integrates JsonJ into jruby and allows you to convert back and forth between JsonElement and jruby’s Hash and Array native types.
- 0.8 fix escaping, npe fix John Goodwin merged, misc minor fixes
- 0.7 Add support for converting a json element to a dom tree so that things like xpath or xsl can be used on it
- 0.6 Support for deepCloning, object sorting added. Use String.intern() on object keys to optimize frequent operations on json objects.
- 0.5 Serializer and parser now properly escape and unescape string literals.
- 0.4 Bug fixes, changed groupid, documentation.
- 0.3 Several new methods added in JsonArray and JsonObject; fixed the parser bug where nested arrays and objects were not handled correctly.
- 0.2 Several bug fixes
- 0.1 First release


# FAQ

## What’s with the name

It’s pronounced json-j, or jasonjay. It doesn’t mean anything in particular other than”json for java“, or something. Well, trying to come up with a name that is not already used is quite a challenge and I wanted to stuff the acronym json in there, keep it short, and not have the first hit on Google be something else than this. So, JsonJ it is.


## For who is this framework intended

Anyone who plans to write a lot of business logic in Java that manipulates json data structures and who doesn’t wish to write model classes in Java to hide the fact that json is being used. If you are like me, you feel somewhat stuck having to deal with awkward json frameworks while all the cool Ruby,Python, and Javascript kids get to use a serialization that is natively supported in their language. This framework is for you.

## Which version should I use?

The latest release or snapshot typically. Releases are tagged in git and I deploy them to maven central.
Beyond the version number, there is not much difference between a release and a snapshot. I tend to release often. Basically every time I add a feature or fix something, it is usually because I need it right away. When I release, the tests pass.

## Will there be a Java 8 version of JsonJ with closures?

Yes, very likely. I've not yet started using Java 8 in production but I will probably very soon start taking a look at it shortly after the release. JsonJ will very likely evolve to blend right in with the new goodies in Java 8.

## I found a bug, what should I do

File a bug on this github project, or just mail/im me directly. Either way, if I agree something is broken, I will fix it. Alternatively, feel free to clone & own. That’s what github is all about.

## How to build JsonJ

`mvn clean install` should do the trick with maven 3.x (and probably 2.x as well).

## Where is the documentation?

Javadoc is generated during the build. After building you can find it in `./target/apidocs/index.html`
Additionally, look at the tests. Particularly [this one here](src/test/java/com/github/jsonj/ShowOffTheFrameworkTest.java) shows off most of the features this framework has.

# License

The license is the [MIT license](http://en.wikipedia.org/wiki/MIT_License), a.k.a. the expat license. The rationale for choosing this license is that I want to maximize your freedom to do whatever you want with the code while limiting my liability for the damage this code could potentially do in your hands. I do appreciate attribution but not enough to require it in the license (beyond the obligatory copyright notice).

# Acknowledgements

1.  I’ve been greatly influenced by the classes representing the json primitives in the GSon framework. If only they implemented Map and List and weren’t final. But lovely framework and would use it again.
2.  I spend quite a bit of time figuring out a way of parsing Json that didn’t involve me generating a lot of source code with javacc, antlr or similar tools. Then I stumpled onto json-simple and wrote the parser for JsonJ in under two hours using a custom json-simple content handler. It seems to work and json-simple is pretty fast as well. I later experimented with a jackson backend that does the same and was not able to make it faster or slower than my original approach. Either implementation gets the job done.
3.  This code is very loosely based on work I did at work with several colleagues some years ago. No code was copy pasted but I definitely took some ideas and improved on them. You know who you are. Thanks.

