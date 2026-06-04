/*
 * Copyright 2026 Daniel Heid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drjekyll.adocfmt.internal.block;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BlockTrackerTest {

  @Test
  void initiallyNotOpen() {
    assertThat(new BlockTracker().isOpen()).isFalse();
  }

  @Test
  void openMakesTrackerOpen() {
    BlockTracker bt = new BlockTracker();
    bt.open("----");
    assertThat(bt.isOpen()).isTrue();
  }

  @Test
  void tryCloseWithMatchingDelimiterClosesBlock() {
    BlockTracker bt = new BlockTracker();
    bt.open("----");
    String result = bt.tryClose("----");
    assertThat(result).isEqualTo("-");
    assertThat(bt.isOpen()).isFalse();
  }

  @Test
  void tryCloseWithDifferentDelimiterCharDoesNotClose() {
    BlockTracker bt = new BlockTracker();
    bt.open("----");
    assertThat(bt.tryClose("====")).isNull();
    assertThat(bt.isOpen()).isTrue();
  }

  @Test
  void tryCloseWithTooShortLineDoesNotClose() {
    BlockTracker bt = new BlockTracker();
    bt.open("----");
    assertThat(bt.tryClose("---")).isNull();
    assertThat(bt.isOpen()).isTrue();
  }

  @Test
  void tryCloseWithMixedCharsDoesNotClose() {
    BlockTracker bt = new BlockTracker();
    bt.open("----");
    assertThat(bt.tryClose("--x-")).isNull();
    assertThat(bt.isOpen()).isTrue();
  }

  @Test
  void tryCloseWhenNotOpenReturnsNull() {
    assertThat(new BlockTracker().tryClose("----")).isNull();
  }

  @Test
  void longerClosingDelimiterClosesBlock() {
    BlockTracker bt = new BlockTracker();
    bt.open("----");
    assertThat(bt.tryClose("--------")).isEqualTo("-");
    assertThat(bt.isOpen()).isFalse();
  }

  @Test
  void tryCloseReturnsDelimiterCharAsString() {
    BlockTracker bt = new BlockTracker();
    bt.open("====");
    assertThat(bt.tryClose("====")).isEqualTo("=");
  }

  @Test
  void trackerCanBeReopenedAfterClose() {
    BlockTracker bt = new BlockTracker();
    bt.open("----");
    bt.tryClose("----");
    bt.open("====");
    assertThat(bt.isOpen()).isTrue();
    assertThat(bt.tryClose("====")).isEqualTo("=");
    assertThat(bt.isOpen()).isFalse();
  }

  @Test
  void openWithDotDelimiter() {
    BlockTracker bt = new BlockTracker();
    bt.open("....");
    assertThat(bt.isOpen()).isTrue();
    assertThat(bt.tryClose("....")).isEqualTo(".");
    assertThat(bt.isOpen()).isFalse();
  }
}
