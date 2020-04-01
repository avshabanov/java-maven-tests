package jsonversioningdemo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.ImmutableList;
import jsonversioningdemo.model.VersionAware;
import jsonversioningdemo.model.VersionedObject;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.io.IOException;
import java.util.List;

/**
 * This demonstrates how Jackson could be used to implement a version-aware persistence logic.
 *
 * Problem:
 * An evolving application that serializes data to the database using JSON serialization must solve the problem of
 * dealing with evolving schema, so that newly deployed application version may refer to a schema that includes new
 * fields (and possibly excludes old ones). During deployment it is possible to have both old and new versions of
 * the application working side by side, it is also possible that new release would be rolled back and old version
 * of an application would have to deal with the newly recorded data that corresponds to the updated schema.
 *
 * If application logic permits old application version to marshal the data corresponding to a new version, it has
 * to deal with the fact, that data might have new fields that old application does not recognize.
 *
 * In order to not to lose the data in this situation, it is proposed to maintain a version and make persistence logic
 * aware of unknown fields, so that when application serializes a recently fetched object corresponding to a new schema
 * it would retain unknown fields just in the same way database would do the same for queries from the old version of
 * an application that doesn't touch unknown optional fields.
 *
 * If application logic does not permit working with the updated version, an old version of an application must fail
 * fast and skip processing of such objects. This too could be done by matching a version to certain known one.
 */
public class Main {

  @Value
  @Builder(builderClassName = "Builder", toBuilder = true)
  @JsonDeserialize(builder = FooV1_1.Builder.class)
  public static class FooV1_1 implements VersionAware {

    // version components
    public static final int VERSION = 1_001;
    @Override @JsonIgnore public int getVersion() { return VERSION; }

    // fields
    @NonNull String id;
    int foo;
    List<String> bar;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {}
  }

  @Value
  @Builder(builderClassName = "Builder", toBuilder = true)
  @JsonDeserialize(builder = FooV1_2.Builder.class)
  public static class FooV1_2 implements VersionAware {

    // version components
    public static final int VERSION = 1_002;
    @Override @JsonIgnore public int getVersion() { return VERSION; }

    // fields
    @NonNull String id;
    int foo;
    String descriptor;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {}
  }

  //
  // Demo
  //

  public static final class Demo1 {
    public static void main(String[] args) throws IOException {
      // deployed new application version -
      // save V2 object
      VersionedObject<FooV1_2> srcV2 = VersionedObject.of(FooV1_2.builder()
          .id("foo1")
          .foo(42)
          .descriptor("V2 object")
          .build());

      // origination of an object
      String record = srcV2.toJson();
      System.out.println("Saved V2 object:\n" + record + "\n---");

      // restore as V1 object which wouldn't have V2's property
      VersionedObject<FooV1_1> srcV1 = VersionedObject.fromJson(record, FooV1_1.class, FooV1_1.VERSION);
      srcV1 = srcV1.join(srcV1.getPayload().toBuilder().bar(ImmutableList.of("a", "b", "c")).build());

      // save V1 object
      record = srcV1.toJson();
      System.out.println("Saved V1 object:\n" + record + "\n---");

      // restore V2 object
      srcV2 = VersionedObject.fromJson(record, FooV1_2.class, FooV1_1.VERSION); // understand both v1_1 and v1_2 objects
      System.out.println("Restored V2 object:\nsrcV2=" + srcV2.getPayload() + "\n---");
    }
  }

  public static final class Demo2 {
    public static void main(String[] args) throws IOException {
      // save V1 object
      VersionedObject<FooV1_1> srcV1 = VersionedObject.of(FooV1_1.builder()
          .id("foo1")
          .foo(42)
          .bar(ImmutableList.of("a", "b", "c"))
          .build());

      // origination of an object
      String record = srcV1.toJson();
      System.out.println("Saved V1 object:\n" + record + "\n---");

      // restore as V2 object which wouldn't have V1's property bar
      // still make it understand V1 objects
      // NOTE: in order to abstain from forward compatibility use the following (note FooV1_2.VERSION):
      // VersionedObject<FooV1_2> srcV2 = VersionedObject.fromJson(record, FooV1_2.class, FooV1_2.VERSION);
      VersionedObject<FooV1_2> srcV2 = VersionedObject.fromJson(record, FooV1_2.class, FooV1_1.VERSION);
      srcV2 = srcV2.join(srcV2.getPayload().toBuilder().descriptor("V2 object descriptor").build());

      // save V2 object
      record = srcV2.toJson();
      System.out.println("Saved V2 object:\n" + record + "\n---");

      // restore V1 object
      srcV1 = VersionedObject.fromJson(record, FooV1_1.class, FooV1_1.VERSION);
      System.out.println("Restored V1 object:\nsrcV1=" + srcV1.getPayload() + "\n---");
    }
  }

}
