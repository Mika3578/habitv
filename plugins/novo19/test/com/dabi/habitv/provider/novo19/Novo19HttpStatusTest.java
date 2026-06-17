package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Novo19HttpStatusTest {

	@Test
	public void classifiesCommonStatuses() {
		assertEquals("http-401", Novo19HttpStatus.summarizeFailure(401));
		assertEquals("http-403", Novo19HttpStatus.summarizeFailure(403));
		assertEquals("http-404", Novo19HttpStatus.summarizeFailure(404));
		assertEquals("http-429", Novo19HttpStatus.summarizeFailure(429));
		assertEquals("http-5xx", Novo19HttpStatus.summarizeFailure(503));
		assertEquals("http-error", Novo19HttpStatus.summarizeFailure(418));
	}

}
