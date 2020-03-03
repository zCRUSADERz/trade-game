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

import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Permission;

/**
 * Default permission granting strategy.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.2.0
 */
public class CustomPermissionGrantingStrategy extends DefaultPermissionGrantingStrategy {

    /**
     * Creates an instance with the logger which will be used to record granting and
     * denial of requested permissions.
     *
     * @param auditLogger logger.
     */
    public CustomPermissionGrantingStrategy(final AuditLogger auditLogger) {
        super(auditLogger);
    }

    @Override
    protected boolean isGranted(final AccessControlEntry ace, final Permission p) {
        final boolean result;
        if (ace.isGranting()) {
            result = (ace.getPermission().getMask() & p.getMask()) == p.getMask();
        } else {
            result = (ace.getPermission().getMask() & p.getMask()) != 0;
        }
        return result;
    }
}
