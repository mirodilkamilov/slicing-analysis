package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.Variable;
import de.uni_passau.fim.se2.sa.slicing.cfg.Node;

public record Definition(Node node, Variable variable) {
}
