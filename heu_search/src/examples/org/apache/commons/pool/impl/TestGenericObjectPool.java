/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.pool.impl;

//import junit.framework.Test;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;

/**
 * @author Rodney Waldhoff
 * @author Dirk Verbeeck
 * @author Sandy McArthur
 * @version $Revision: 609415 $ $Date: 2008-01-06 16:45:32 -0500 (Sun, 06 Jan
 *          2008) $
 */

public class TestGenericObjectPool {
  
    public static void main(String[] args) throws Exception {
        TestGenericObjectPool test = new TestGenericObjectPool();
        test.testEvictAddObjectsImproved();        
    }

    /**
     * Tests addObject contention between ensureMinIdle triggered by minIdle > 0
     * and borrowObject.
     */
    public void testEvictAddObjectsImproved() throws Exception {
        SimpleFactory factory = new SimpleFactory();
        factory.setMakeLatency(300);
        factory.setMaxActive(2);
        final GenericObjectPool pool = new GenericObjectPool(factory);
        pool.setMaxActive(2);
        pool.setMinIdle(1);

        pool.borrowObject(); // numActive = 1, numIdle = 0

        // Create a test thread that will run once and try a borrow after
        // 150ms fixed delay
        TestThread borrower = new TestThread(pool, 1, 150, false);
        Thread borrowerThread = new Thread(borrower);

        // Create another thread that will call ensureMinIdle
        Thread minIdleThread = new Thread(new Runnable() {
            public void run() {
                try {
                    pool.evict();
                } catch (Exception e) {
                    // ignored
                }
                try {
                    pool.ensureMinIdle();
                } catch (Exception e) {
                    // Ignore
                }
            }
        });

        // Off to the races
        minIdleThread.start();
        borrowerThread.start();
        minIdleThread.join();
        borrowerThread.join();
        assert(borrower.failed()==false);
        pool.close();
    }

    class TestThread implements Runnable {
        java.util.Random _random = new java.util.Random();
        ObjectPool _pool = null;
        boolean _complete = false;
        boolean _failed = false;
        int _iter = 100;
        int _delay = 50;
        boolean _randomDelay = true;

        public TestThread(ObjectPool pool) {
            _pool = pool;
        }

        public TestThread(ObjectPool pool, int iter) {
            _pool = pool;
            _iter = iter;
        }

        public TestThread(ObjectPool pool, int iter, int delay) {
            _pool = pool;
            _iter = iter;
            _delay = delay;
        }

        public TestThread(ObjectPool pool, int iter, int delay,
                boolean randomDelay) {
            _pool = pool;
            _iter = iter;
            _delay = delay;
            _randomDelay = randomDelay;
        }

        public boolean complete() {
            return _complete;
        }

        public boolean failed() {
            return _failed;
        }

        public void run() {
            for (int i = 0; i < _iter; i++) {
                long delay = _randomDelay ? (long) _random.nextInt(_delay)
                        : _delay;
                try {
                    Thread.sleep(delay);
                } catch (Exception e) {
                    // ignored
                }
                Object obj = null;
                try {
                    obj = _pool.borrowObject();
                } catch (Exception e) {
                    e.printStackTrace();
                    _failed = true;
                    _complete = true;
                    break;
                }

                try {
                    Thread.sleep(delay);
                } catch (Exception e) {
                    // ignored
                }
                try {
                    _pool.returnObject(obj);
                } catch (Exception e) {
                    _failed = true;
                    _complete = true;
                    break;
                }
            }
            _complete = true;
        }
    }

    public class SimpleFactory implements PoolableObjectFactory {
        public SimpleFactory() {
            this(true);
        }

        public SimpleFactory(boolean valid) {
            this(valid, valid);
        }

        public SimpleFactory(boolean evalid, boolean ovalid) {
            evenValid = evalid;
            oddValid = ovalid;
        }

        void setValid(boolean valid) {
            setEvenValid(valid);
            setOddValid(valid);
        }

        void setEvenValid(boolean valid) {
            evenValid = valid;
        }

        void setOddValid(boolean valid) {
            oddValid = valid;
        }

        public void setThrowExceptionOnPassivate(boolean bool) {
            exceptionOnPassivate = bool;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public void setDestroyLatency(long destroyLatency) {
            this.destroyLatency = destroyLatency;
        }

        public void setMakeLatency(long makeLatency) {
            this.makeLatency = makeLatency;
        }

        public Object makeObject() {
            synchronized (this) {
                activeCount++;
                if (activeCount > maxActive) {
                    throw new IllegalStateException(
                            "Too many active instances: " + activeCount);
                }
            }
            if (makeLatency > 0) {
                doWait(makeLatency);
            }
            return String.valueOf(makeCounter++);
        }

        public void destroyObject(Object obj) {
            if (destroyLatency > 0) {
                doWait(destroyLatency);
            }
            synchronized (this) {
                activeCount--;
            }
        }

        public boolean validateObject(Object obj) {
            if (enableValidation) {
                return validateCounter++ % 2 == 0 ? evenValid : oddValid;
            } else {
                return true;
            }
        }

        public void activateObject(Object obj) throws Exception {
            if (exceptionOnActivate) {
                if (!(validateCounter++ % 2 == 0 ? evenValid : oddValid)) {
                    throw new Exception();
                }
            }
        }

        public void passivateObject(Object obj) throws Exception {
            if (exceptionOnPassivate) {
                throw new Exception();
            }
        }

        int makeCounter = 0;
        int validateCounter = 0;
        int activeCount = 0;
        boolean evenValid = true;
        boolean oddValid = true;
        boolean exceptionOnPassivate = false;
        boolean exceptionOnActivate = false;
        boolean enableValidation = true;
        long destroyLatency = 0;
        long makeLatency = 0;
        int maxActive = Integer.MAX_VALUE;

        public boolean isThrowExceptionOnActivate() {
            return exceptionOnActivate;
        }

        public void setThrowExceptionOnActivate(boolean b) {
            exceptionOnActivate = b;
        }

        public boolean isValidationEnabled() {
            return enableValidation;
        }

        public void setValidationEnabled(boolean b) {
            enableValidation = b;
        }

        private void doWait(long latency) {
            try {
                Thread.sleep(latency);
            } catch (InterruptedException ex) {
                // ignore
            }
        }
    }
}
