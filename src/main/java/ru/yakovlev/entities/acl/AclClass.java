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

package ru.yakovlev.entities.acl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * ACL class.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.3.0
 */
@Entity
@Immutable
@NoArgsConstructor
@Getter
public class AclClass {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "acl_class_seq_generator")
    @SequenceGenerator(name = "acl_class_seq_generator", sequenceName = "acl_class_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "class", unique = true, nullable = false, updatable = false, length = 100)
    @NotBlank
    @Size(min = 1, max = 100)
    private String clazz;

    @Column(unique = true, nullable = false, updatable = false, length = 100)
    @NotBlank
    @Size(min = 1, max = 100)
    private String classIdType;

}
