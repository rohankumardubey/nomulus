// Copyright 2017 The Nomulus Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package google.registry.util;

import javax.annotation.concurrent.ThreadSafe;
import org.joda.time.ReadableDuration;

/**
 * An object which accepts requests to put the current thread to sleep.
 *
 * @see SystemSleeper
 */
@ThreadSafe
public interface Sleeper {

  /**
   * Puts the current thread to sleep.
   *
   * @throws InterruptedException if this thread was interrupted
   */
  void sleep(ReadableDuration duration) throws InterruptedException;

  /**
   * Puts the current thread to sleep, ignoring interrupts.
   *
   * <p>If {@link InterruptedException} was caught, then {@code Thread.currentThread().interrupt()}
   * will be called at the end of the {@code duration}.
   *
   * @see com.google.common.util.concurrent.Uninterruptibles#sleepUninterruptibly
   */
  void sleepUninterruptibly(ReadableDuration duration);

  /**
   * Puts the current thread to interruptible sleep.
   *
   * <p>This is a convenience method for {@link #sleep} that properly converts an {@link
   * InterruptedException} to a {@link RuntimeException}.
   */
  default void sleepInterruptibly(ReadableDuration duration) {
    try {
      sleep(duration);
    } catch (InterruptedException e) {
      // Restore current thread's interrupted state.
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted.", e);
    }
  }
}
