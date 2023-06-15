package de.uni_passau.fim.se2.sa.slicing.cfg;

import java.util.Objects;

/** Represents a local variable of a method as it is in the class file's local variable table. */
public record LocalVariable(String name, String descriptor, String signature, int index) {

  @Override
  public boolean equals(Object pOther) {
    if (this == pOther) {
      return true;
    }
    if (pOther == null || getClass() != pOther.getClass()) {
      return false;
    }
    LocalVariable that = (LocalVariable) pOther;
    return index == that.index
        && Objects.equals(name, that.name)
        && Objects.equals(descriptor, that.descriptor)
        && Objects.equals(signature, that.signature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, descriptor, signature, index);
  }

  @Override
  public String toString() {
    return "LocalVariable{name='"
        + name
        + "', descriptor='"
        + descriptor
        + "', signature='"
        + signature
        + "', index="
        + index
        + "}";
  }
}
