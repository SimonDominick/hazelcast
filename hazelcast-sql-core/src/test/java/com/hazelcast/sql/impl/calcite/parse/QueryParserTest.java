/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.sql.impl.calcite.parse;

import com.hazelcast.sql.impl.QueryException;
import com.hazelcast.sql.impl.calcite.SqlBackend;
import com.hazelcast.sql.impl.calcite.validate.HazelcastSqlValidator;
import com.hazelcast.sql.impl.calcite.validate.types.HazelcastTypeFactory;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.ParallelJVMTest;
import com.hazelcast.test.annotation.QuickTest;
import org.apache.calcite.prepare.Prepare.CatalogReader;
import org.apache.calcite.runtime.CalciteException;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.util.SqlVisitor;
import org.apache.calcite.sql.validate.SqlConformance;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelJVMTest.class})
public class QueryParserTest {

    private QueryParser parser;

    @Mock
    private CatalogReader catalogReader;

    @Mock
    private SqlConformance conformance;

    @Mock
    private SqlBackend sqlBackend;

    @Mock
    private SqlBackend jetSqlBackend;

    @Mock
    private HazelcastSqlValidator sqlValidator;

    @Mock
    private HazelcastSqlValidator jetSqlValidator;

    @Mock
    private SqlNode validatedNode;

    @Mock
    private SqlVisitor<Void> unsupportedOperatorVisitor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        parser = new QueryParser(HazelcastTypeFactory.INSTANCE, catalogReader, conformance, sqlBackend, jetSqlBackend);

        given(sqlBackend.validator(catalogReader, HazelcastTypeFactory.INSTANCE, conformance)).willReturn(sqlValidator);
        given(jetSqlBackend.validator(catalogReader, HazelcastTypeFactory.INSTANCE, conformance)).willReturn(jetSqlValidator);
    }

    @Test
    public void when_imdgCanHandleSql() {
        // given
        given(sqlValidator.validate(isA(SqlNode.class))).willReturn(validatedNode);
        given(sqlBackend.unsupportedOperationVisitor(catalogReader)).willReturn(unsupportedOperatorVisitor);

        // when
        QueryParseResult result = parser.parse("SELECT * FROM t");

        // then
        assertEquals(validatedNode, result.getNode());
        assertEquals(sqlBackend, result.getSqlBackend());
        assertEquals(sqlValidator, result.getValidator());

        verifyZeroInteractions(jetSqlBackend);
    }

    @Test(expected = QueryException.class)
    public void when_imdgCantHandleSqlAndJetIsNotAvailable_then_throwsException() {
        // given
        parser = new QueryParser(HazelcastTypeFactory.INSTANCE, catalogReader, conformance, sqlBackend, null);

        given(sqlValidator.validate(isA(SqlNode.class))).willThrow(new CalciteException("expected test exception", null));

        // when
        // then
        parser.parse("SELECT * FROM t");
    }

    @Test
    public void when_imdgCantHandleSqlButJetCan() {
        // given
        given(sqlValidator.validate(isA(SqlNode.class))).willThrow(new CalciteException("expected test exception", null));

        given(jetSqlValidator.validate(isA(SqlNode.class))).willReturn(validatedNode);
        given(jetSqlBackend.unsupportedOperationVisitor(catalogReader)).willReturn(unsupportedOperatorVisitor);

        // when
        QueryParseResult result = parser.parse("SELECT * FROM t");

        // then
        assertEquals(validatedNode, result.getNode());
        assertEquals(jetSqlBackend, result.getSqlBackend());
        assertEquals(jetSqlValidator, result.getValidator());

        verify(sqlBackend, never()).unsupportedOperationVisitor(any());
    }

    @Test(expected = QueryException.class)
    public void when_neitherImdgOrJetCanHandleSql_then_throwsException() {
        // given
        given(sqlValidator.validate(isA(SqlNode.class))).willThrow(new CalciteException("expected test exception", null));
        given(jetSqlValidator.validate(isA(SqlNode.class))).willThrow(new CalciteException("expected test exception", null));

        // when
        // then
        parser.parse("SELECT * FROM t");
    }
}
