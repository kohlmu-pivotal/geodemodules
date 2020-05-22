package org.apache.geode.service.impl;

import org.apache.commons.lang3.StringUtils;

import org.apache.geode.service.SampleService;

public class SampleServiceImpl implements SampleService {

	public String getValue() {
		return StringUtils.capitalize("org.apache.commons.lang3.StringUtils");
	}
}
