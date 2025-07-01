import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.stream.IntStream;

/**
 * Generates secure pseudo-random numbers
 *
 * @author Mister PKI
 */
public final class SecureRandomNumberGenerator {

  public static int generateRandomInt() throws NoSuchAlgorithmException {
    final SecureRandom secureRandom = SecureRandom.getInstance("NativePRNGNonBlocking");
    return secureRandom.nextInt();
  }

  public static int generateRandomIntWithUpperBound(final int bound) throws NoSuchAlgorithmException {
    final SecureRandom secureRandom = SecureRandom.getInstance("NativePRNG");
    return secureRandom.nextInt(bound);
  }

  public static IntStream generateRandomStreamOfInts(final int size, final int lowerBound, final int upperBound) throws NoSuchAlgorithmException {
    final SecureRandom secureRandom = SecureRandom.getInstance("NativePRNG");
    return secureRandom.ints(size, lowerBound, upperBound);
  }

  public static long generateRandomLong() throws NoSuchAlgorithmException {
    final SecureRandom secureRandom = SecureRandom.getInstance("NativePRNG");
    return secureRandom.nextLong();
  }

  public static float generateRandomFloat() throws NoSuchAlgorithmException {
    final SecureRandom secureRandom = SecureRandom.getInstance("NativePRNG");
    return secureRandom.nextFloat();
  }

  public static double generateRandomDouble() throws NoSuchAlgorithmException {
    final SecureRandom secureRandom = SecureRandom.getInstance("NativePRNG");
    return secureRandom.nextDouble();
  }

  public static double generateRandomGaussian() throws NoSuchAlgorithmException {
    final SecureRandom secureRandom = SecureRandom.getInstance("NativePRNG");
    return secureRandom.nextGaussian();
  }

  public static boolean generateRandomBoolean() throws NoSuchAlgorithmException {
    final SecureRandom secureRandom = SecureRandom.getInstance("NativePRNG");
    return secureRandom.nextBoolean();
  }

  public static byte[] generateRandomBytes() throws NoSuchAlgorithmException {
    final SecureRandom secureRandom = SecureRandom.getInstance("NativePRNG");
    byte[] bytes = new byte[16];
    secureRandom.nextBytes(bytes);
    return bytes;
  }

}
