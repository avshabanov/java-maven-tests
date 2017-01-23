package com.alexshabanov.grfj.ftl;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

/**
 * @author Alexander Shabanov
 */
public class ModelAndView {
  private final String viewName;
  private final Map<String, Object> parameters;

  private ModelAndView(@Nonnull  String viewName, @Nonnull Map<String, Object> parameters) {
    this.viewName = Objects.requireNonNull(viewName, "viewName");
    this.parameters = Objects.requireNonNull(parameters, "parameters");
  }

  public static ModelAndView of(@Nonnull String viewName, @Nonnull Map<String, Object> parameters) {
    return new ModelAndView(viewName, ImmutableMap.copyOf(parameters));
  }

  public static ModelAndView of(@Nonnull String viewName) {
    return of(viewName, ImmutableMap.of());
  }

  public static ModelAndView of(@Nonnull String viewName, @Nonnull String parameter, @Nonnull Object value) {
    return of(viewName, ImmutableMap.of(parameter, value));
  }

  public String getViewName() {
    return viewName;
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ModelAndView)) return false;

    final ModelAndView that = (ModelAndView) o;

    return viewName.equals(that.viewName) && parameters.equals(that.parameters);
  }

  @Override
  public int hashCode() {
    int result = viewName.hashCode();
    result = 31 * result + parameters.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ModelAndView{" +
        "viewName='" + viewName + '\'' +
        ", parameters=" + parameters +
        '}';
  }
}
