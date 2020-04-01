package jsonversioningdemo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * An object, holding a version.
 */
public interface VersionAware {

  @JsonIgnore int getVersion();
}
