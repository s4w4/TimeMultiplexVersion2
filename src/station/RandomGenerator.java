package station;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RandomGenerator {
	private SecureRandom secureRandomGenerator;
	private SecureRandom secureRandom1;

	public RandomGenerator() {
		try {
			this.secureRandomGenerator = SecureRandom.getInstance("SHA1PRNG");
	
		
		byte[] randomBytes = new byte[1];
		secureRandomGenerator.nextBytes(randomBytes);
		
		int seedByteCount = 1;
		byte[] seed = secureRandomGenerator.generateSeed(seedByteCount);
		
		secureRandom1 = SecureRandom.getInstance("SHA1PRNG");
		secureRandom1.setSeed(seed);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public double nextInt(){
		
		return secureRandom1.nextDouble();

	}
}
