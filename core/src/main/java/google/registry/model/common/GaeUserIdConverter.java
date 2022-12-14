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

import static com.google.common.base.Preconditions.checkState;
import static google.registry.model.IdService.allocateId;
import static google.registry.model.ofy.ObjectifyService.auditedOfy;

import com.google.appengine.api.users.User;
import com.google.common.base.Splitter;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import google.registry.model.ImmutableObject;
import google.registry.model.annotations.NotBackedUp;
import google.registry.model.annotations.NotBackedUp.Reason;
import java.util.List;

/**
 * A helper class to convert email addresses to GAE user ids. It does so by persisting a User
 * object with the email address to Datastore, and then immediately reading it back.
 */
@Entity
@NotBackedUp(reason = Reason.TRANSIENT)
public class GaeUserIdConverter extends ImmutableObject {

  @Id
  public long id;

  User user;

  /**
   * Converts an email address to a GAE user id.
   *
   * @return Numeric GAE user id (in String form), or null if email address has no GAE id
   */
  public static String convertEmailAddressToGaeUserId(String emailAddress) {
    final GaeUserIdConverter gaeUserIdConverter = new GaeUserIdConverter();
    gaeUserIdConverter.id = allocateId();
    List<String> emailParts = Splitter.on('@').splitToList(emailAddress);
    checkState(emailParts.size() == 2, "'%s' is not a valid email address", emailAddress);
    gaeUserIdConverter.user = new User(emailAddress, emailParts.get(1));

    try {
      // Perform these operations in a transactionless context to avoid enlisting in some outer
      // transaction (if any).
      auditedOfy()
          .doTransactionless(
              () -> {
                auditedOfy().saveWithoutBackup().entity(gaeUserIdConverter).now();
                return null;
              });
      // The read must be done in its own transaction to avoid reading from the session cache.
      return auditedOfy()
          .transactNew(() -> auditedOfy().load().entity(gaeUserIdConverter).now().user.getUserId());
    } finally {
      auditedOfy()
          .doTransactionless(
              () -> auditedOfy().deleteWithoutBackup().entity(gaeUserIdConverter).now());
    }
  }
}
