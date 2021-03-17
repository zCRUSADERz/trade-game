/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Yakovlev Alexander
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.yakovlev.service.security;

import static org.springframework.security.acls.domain.BasePermission.CREATE;
import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.acls.domain.AccessControlEntryImpl;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

@ExtendWith(MockitoExtension.class)
class CustomPermissionGrantingStrategyTest {
    @Mock
    private Acl acl;
    @Mock
    private Sid sid;
    @Mock
    private AuditLogger logger;

    @Test
    @DisplayName("When the acl entry grants rights and there is no write right, the method must return false")
    void whenGrantedAndHasNoRightsThenIsGrantedReturnFalse() {
        val permissionStrategy = new CustomPermissionGrantingStrategy(this.logger);
        val aclEntry = new AccessControlEntryImpl(1, this.acl, this.sid, READ, true, false, false);
        val result = permissionStrategy.isGranted(aclEntry, WRITE);
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("When the acl entry grants rights and there only read right, the method must return false. "
            + "Must have read and write permissions.")
    void whenGrantedAndHasOnlyPartOfTheRightsThenIsGrantedReturnFalse() {
        val permissionStrategy = new CustomPermissionGrantingStrategy(this.logger);
        val aclEntry = new AccessControlEntryImpl(1, this.acl, this.sid, READ, true, false, false);
        val requiredRights = this.create(READ, WRITE);
        val result = permissionStrategy.isGranted(aclEntry, requiredRights);
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("When the acl entry grants rights and has required rights, isGranted must return true.")
    void whenGrantedAndHasRightsThenIsGrantedReturnTrue() {
        val permissionStrategy = new CustomPermissionGrantingStrategy(this.logger);
        val rights = this.create(READ, WRITE);
        val aclEntry = new AccessControlEntryImpl(
                1, this.acl, this.sid, rights, true, false, false);
        val requiredRights = this.create(READ, WRITE);
        val result = permissionStrategy.isGranted(aclEntry, requiredRights);
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName(
            "When the acl entry grants rights, has all required and additional rights, isGranted must return true.")
    void whenGrantedAndHasRequiresAndAdditionalRightsThenIsGrantedReturnTrue() {
        val permissionStrategy = new CustomPermissionGrantingStrategy(this.logger);
        val rights = this.create(READ, WRITE, CREATE);
        val aclEntry = new AccessControlEntryImpl(1, this.acl, this.sid, rights, true, false, false);
        val requiredRights = this.create(READ, WRITE);
        val result = permissionStrategy.isGranted(aclEntry, requiredRights);
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName(
            "When the acl entry does not grant rights and there is no required rights, isGranted must return false.")
    void whenIsNotGrantedAndHasNoRequiredRightsThenIsGrantedReturnFalse() {
        val permissionStrategy = new CustomPermissionGrantingStrategy(this.logger);
        val aclEntry = new AccessControlEntryImpl(1, this.acl, this.sid, CREATE, false, false, false);
        val result = permissionStrategy.isGranted(aclEntry, READ);
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("When the acl entry does not grant rights and has only write right, isGranted must return true.")
    void whenIsNotGrantedAndHasOnlyWriteRightThenIsGrantedReturnTrue() {
        val permissionStrategy = new CustomPermissionGrantingStrategy(this.logger);
        val aclEntry = new AccessControlEntryImpl(1, this.acl, this.sid, WRITE, false, false, false);
        val requiredRights = this.create(READ, WRITE);
        val result = permissionStrategy.isGranted(aclEntry, requiredRights);
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("When the acl entry does not grant rights and has required rights, isGranted must return true.")
    void whenIsNotGrantedAndHasRequiredRightsThenIsGrantedReturnTrue() {
        val permissionStrategy = new CustomPermissionGrantingStrategy(this.logger);
        val rights = this.create(READ, WRITE);
        val aclEntry = new AccessControlEntryImpl(1, this.acl, this.sid, rights, false, false, false);
        val requiredRights = this.create(READ, WRITE);
        val result = permissionStrategy.isGranted(aclEntry, requiredRights);
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("When the acl entry does not grant rights and has required and additional rights, "
            + "isGranted must return true.")
    void whenIsNotGrantedAndHasRequiredAndAdditionalRightsThenIsGrantedReturnTrue() {
        val permissionStrategy = new CustomPermissionGrantingStrategy(this.logger);
        val rights = this.create(READ, WRITE, CREATE);
        val aclEntry = new AccessControlEntryImpl(1, this.acl, this.sid, rights, false, false, false);
        val requiredRights = this.create(READ, WRITE);
        val result = permissionStrategy.isGranted(aclEntry, requiredRights);
        Assertions.assertTrue(result);
    }

    private Permission create(Permission ...permissions) {
        val result = new CumulativePermission();
        for (Permission p : permissions) {
            result.set(p);
        }
        return result;
    }

}