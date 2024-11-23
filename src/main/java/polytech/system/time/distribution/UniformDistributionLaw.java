package polytech.system.time.distribution;

public class UniformDistributionLaw {
    public Double getTime(double minimum, double maximum) {
        return Math.random() * (maximum - minimum) + minimum;
    }
}
