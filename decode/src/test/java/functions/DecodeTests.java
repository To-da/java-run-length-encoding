package functions;

import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Collections;

public class DecodeTests {

	private static class TestError extends RuntimeException {}

	Decode decode;

	@Before
	public void setUp() {
		decode = new Decode();
	}

	@Test
	public void testEmpty() {
		StepVerifier.create(Flux.<Integer>empty().as(decode))
			.verifyComplete();
	}

	@Test
	public void testSingle() {
		StepVerifier.create(Flux.just(1, 0).as(decode))
			.expectNext(0)
			.verifyComplete();
	}

	@Test
	public void testRuns() {
		StepVerifier.create(Flux.just(3, 0, 1, 1, 2, 2).as(decode))
			.expectNext(0, 0, 0, 1, 2, 2)
			.verifyComplete();
	}

	@Test
	public void testDegenerateRun() {
		StepVerifier.create(Flux.just(1, 0, 0, 1, 1, 2).as(decode))
			.expectNext(0, 2)
			.verifyComplete();
	}

	@Test
	public void testDoubleRun() {
		StepVerifier.create(Flux.just(1, 0, 1, 0).as(decode))
			.expectNext(0, 0)
			.verifyComplete();
	}

	@Test
	public void testLong() {
		StepVerifier.create(Flux.just(1000, 0).as(decode))
			.expectNextSequence(Collections.nCopies(1000, 0))
			.verifyComplete();
	}

	@Test
	public void testCountWithNoValue() {
		StepVerifier.create(Flux.just(2, 1, 0).as(decode))
			.expectNext(1, 1)
			.verifyError(IllegalArgumentException.class);
	}

	@Test
	public void testNegativeCount() {
		StepVerifier.create(Flux.just(2, 1, -1, 0).as(decode))
			.expectNext(1, 1)
			.verifyError(IllegalArgumentException.class);
	}

	@Test
	public void testErrorOnly() {
		StepVerifier.create(Flux.<Integer>error(new TestError()).as(decode))
			.verifyError(TestError.class);
	}

	@Test
	public void testError() {
		StepVerifier.create(Flux.concatDelayError(Flux.just(1, 0), Flux.error(new TestError())).as(decode))
			.expectNext(0)
			.verifyError(TestError.class);
	}

	@Test
	public void testCountWithNoValueOnError() {
		StepVerifier.create(Flux.concatDelayError(Flux.just(1), Flux.error(new TestError())).as(decode))
			.verifyError(TestError.class);
	}
}
