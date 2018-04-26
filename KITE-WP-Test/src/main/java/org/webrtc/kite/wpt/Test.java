/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.webrtc.kite.wpt;

import java.util.ArrayList;
import java.util.List;

public class Test {
  private String testLink;
  private String name;
  private boolean isTest;
  private int total;
  private int passed;
  private Test parent;
  private List<Test> children;

  public Test(String testLink, Test parent) {
    this.testLink = testLink;
    this.isTest = false;
    this.parent = parent;
    this.children = new ArrayList<>();
    total = 0;
    passed = 0;
  }

  public String getTestLink() {
    return testLink;
  }

  public Test getParent() {
    return parent;
  }

  public void setParent(Test parent) {
    this.parent = parent;
  }

  public void addChild(Test child) {
    this.children.add(child);
  }

  public List<Test> getChildren() {
    return children;
  }

  public void setChildren(List<Test> children) {
    this.children = children;
  }

  public boolean isTest() {
    return isTest;
  }

  public void setTest(boolean test) {
    isTest = test;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (name.endsWith("html"))
      name = name.substring(0, name.length() - 5);
    if (name.contains("historical"))
      name = this.getParent().getName() + "-" + name;
    this.name = name;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public int getPassed() {
    return passed;
  }

  public void setPassed(int passed) {
    this.passed = passed;
  }
}
