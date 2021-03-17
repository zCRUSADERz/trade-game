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

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Security service.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.3.0
 */
@Service
@AllArgsConstructor
public class SecurityService {
    private final RoleHierarchy roleHierarchy;
    private final MutableAclService aclService;

    /**
     * Return all user authorities, given the role hierarchy.
     *
     * @return all user authorities, given the role hierarchy.
     */
    public Collection<String> userAuthorities() {
        final Collection<String> result;
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            result = Collections.emptyList();
        } else {
            val authorities = this.roleHierarchy.getReachableGrantedAuthorities(authentication.getAuthorities());
            result = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        }
        return result;
    }

    /**
     * Create access control list for given entity.
     *
     * @param entity entity.
     */
    public void createAcl(Object entity) {
        val ownerPermission = new CumulativePermission();
        ownerPermission.set(BasePermission.READ);
        ownerPermission.set(BasePermission.WRITE);
        ownerPermission.set(BasePermission.CREATE);
        ownerPermission.set(BasePermission.DELETE);
        ownerPermission.set(BasePermission.ADMINISTRATION);
        val identity = new ObjectIdentityImpl(entity);
        val acl = this.aclService.createAcl(identity);
        val ownerSid = acl.getOwner();
        acl.insertAce(acl.getEntries().size(), ownerPermission, ownerSid, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.READ, new GrantedAuthoritySid("ROLE_SUPERVISOR"), true);
        this.aclService.updateAcl(acl);
    }

}
