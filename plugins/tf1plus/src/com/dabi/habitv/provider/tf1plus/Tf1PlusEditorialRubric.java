package com.dabi.habitv.provider.tf1plus;

/**
 * Editorial rubric mapping for TF1 hub grouping. Secondary slug-based rules may
 * apply when API metadata is missing ({@link Tf1PlusEditorialRubricRegistry}).
 */
final class Tf1PlusEditorialRubric {

	private final String label;
	private final String apiType;

	Tf1PlusEditorialRubric(final String label, final String apiType) {
		this.label = label;
		this.apiType = apiType;
	}

	String getLabel() {
		return label;
	}

	String getApiType() {
		return apiType;
	}

}
