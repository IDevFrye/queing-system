package polytech.system.time.distribution;

public class PoissonDistributionLaw {
    public double getTime(double lambda) {
        return 1 - Math.exp(lambda*Math.random());
    }
}
