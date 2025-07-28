package de.uni_passau.fim.se2.sa.examples;

public class IsOdd {
    public boolean isOdd(int num) {
        String abandonedVar = "whatever";
        boolean isOdd = false;
        if (num % 2 != 0) {
            isOdd = true;
        }
        return isOdd;
    }
}
// For static analysis: ./run.sh -c de.uni_passau.fim.se2.sa.examples.IsOdd -m "isOdd:(I)Z" -v isOdd -l 10 -s src/de/uni_passau/fim/se2/sa/examples/IsOdd.java
// For dynamic analysis: ./run.sh -c de.uni_passau.fim.se2.sa.examples.IsOdd -m "isOdd:(I)Z" -v isOdd -l 10 -d isOdd_EvenNumber_ShouldReturnFalse -s src/de/uni_passau/fim/se2/sa/examples/IsOdd.java