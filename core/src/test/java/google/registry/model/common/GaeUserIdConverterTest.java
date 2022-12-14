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

package google.registry.model.common;

import static com.google.common.truth.Truth.assertThat;
import static google.registry.model.ofy.ObjectifyService.auditedOfy;

import google.registry.testing.AppEngineExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Unit tests for {@link GaeUserIdConverter}. */
public class GaeUserIdConverterTest {

  @RegisterExtension
  public final AppEngineExtension appEngine = AppEngineExtension.builder().withCloudSql().build();

  @AfterEach
  void verifyNoLingeringEntities() {
    assertThat(auditedOfy().load().type(GaeUserIdConverter.class).count()).isEqualTo(0);
  }

  @Test
  void testSuccess() {
    assertThat(GaeUserIdConverter.convertEmailAddressToGaeUserId("example@example.com"))
        .matches("[0-9]+");
  }

  @Test
  void testSuccess_inTransaction() {
    auditedOfy()
        .transactNew(
            () -> {
              assertThat(GaeUserIdConverter.convertEmailAddressToGaeUserId("example@example.com"))
                  .matches("[0-9]+");
              return null;
            });
  }
}
