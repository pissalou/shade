/*
 * Copyright (C) 2013 Pascal Mazars
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package app.core;

import org.junit.Test;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 */
@TestExecutionListeners(TransactionalTestExecutionListener.class)
public class TransactionPositiveTest extends TransactionTest {

    @Test(expected = IllegalTransactionStateException.class)
    public void test1() {
        transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_MANDATORY));
    }

    @Test
    public void test11() {
        transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NEVER));
    }

    @Test
    public void testTransactionalMethod() {
        doInTransaction();
    }

    @Transactional
    public void doInTransaction() {
        getCurrentTransactionStatus();
    }

    @Test
    public void test2() {
        new TransactionCommand() {
            @Override
            public void executeInTransaction() {
                getCurrentTransactionStatus();
            }
        }.executeInTransaction();
    }
}
