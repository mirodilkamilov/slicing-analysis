package de.uni_passau.fim.se2.sa.slicing.cfg;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implements a local variable table similar to the one of a Java class file.
 *
 * @see LocalVariable
 */
public class LocalVariableTable {

  private final Map<Integer, LocalVariable> localVariableTables = new LinkedHashMap<>();

  void addEntry(final int pIndex, final LocalVariable pLocalVariable) {
    localVariableTables.computeIfAbsent(pIndex, k -> pLocalVariable);
  }

  public Optional<LocalVariable> getEntry(final int pIndex) {
    if (localVariableTables.containsKey(pIndex)) {
      return Optional.of(localVariableTables.get(pIndex));
    } else {
      return Optional.empty();
    }
  }
}
