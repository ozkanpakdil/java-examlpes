import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;

public class Main {

  public static void main (final String[] args) throws NoSuchAlgorithmException {
    Arrays.stream(Security.getProviders()).forEach(provider -> {
      System.out.println("Provider: " + provider.getName());
      System.out.println("Version: " + provider.getVersion());
      System.out.println("Info: " + provider.getInfo());
    });
    System.out.println("Generating secure int: " + SecureRandomNumberGenerator.generateRandomInt());
    System.out.println("Generating secure long: " + SecureRandomNumberGenerator.generateRandomLong());
    System.out.println("Generating secure float: " + SecureRandomNumberGenerator.generateRandomFloat());
    System.out.println("Generating secure double: " + SecureRandomNumberGenerator.generateRandomDouble());
    System.out.println("Generating secure gaussian: " + SecureRandomNumberGenerator.generateRandomGaussian());
    System.out.println("Generating secure bytes: " + Arrays.toString(SecureRandomNumberGenerator.generateRandomBytes()));

    System.out.println("Generating random int with upper bound: "
        + SecureRandomNumberGenerator.generateRandomIntWithUpperBound(1000));
    SecureRandomNumberGenerator.generateRandomStreamOfInts(3, 1, 10)
        .forEach(value -> System.out.println("Value: " + value));
  }
}
