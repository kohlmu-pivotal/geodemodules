package org.apache.geode.service.impl;

import com.google.common.base.Strings;

import org.apache.geode.service.SampleService;

public class SampleServiceImpl implements SampleService {

  public String getValue() {
    return Strings.nullToEmpty("com.google.common.base.Strings");
  }
}
