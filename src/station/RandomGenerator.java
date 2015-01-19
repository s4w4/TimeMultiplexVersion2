package station;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RandomGenerator {
	private SecureRandom secureRandomGenerator;
	private SecureRandom secureRandom1;

	public RandomGenerator() throws NoSuchAlgorithmException {
		this.secureRandomGenerator = SecureRandom.getInstance("SHA1PRNG");
		
		byte[] randomBytes = new byte[1];
		secureRandomGenerator.nextBytes(randomBytes);
		
		int seedByteCount = 1;
		byte[] seed = secureRandomGenerator.generateSeed(seedByteCount);
		
		secureRandom1 = SecureRandom.getInstance("SHA1PRNG");
		secureRandom1.setSeed(seed);
	}
	
	public double nextInt() throws NoSuchAlgorithmException {
		
		return secureRandom1.nextDouble();

	}
}
