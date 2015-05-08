/*
 * Copyright 2015 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.codemodule;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/**
 */
public class LambdaSource extends MessageProducerSupport {

	protected Supplier<?> supplier;
    
//    private int producers;
//
//    private int messageSize;
//
//    private int messageCount;
//
//    private boolean generateTimestamp;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private ExecutorService executorService;

    Logger logger = LoggerFactory.getLogger(LambdaSource.class);

    public LambdaSource() {
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("lambda.ser");
            if (is == null) {
	            	logger.error("LambdaSource unable to find 'lambda.ser' resource");
	            	return;
            }
            ObjectInputStream ois = new ObjectInputStream(is);
            supplier = (Supplier<?>)ois.readObject();
            ois.close();
	    } catch (IOException e) {
	            e.printStackTrace();
	    } catch (ClassNotFoundException e) {
	            e.printStackTrace();
	    }
        
    }

    @Override
    protected void doStart() {
        executorService = Executors.newFixedThreadPool(1);
        if (running.compareAndSet(false, true)) {
            executorService.execute(new Producer());
        }
    }

    @Override
    protected void doStop() {
        if (running.compareAndSet(true, false)) {
            executorService.shutdown();
        }
    }

    protected class Producer implements Runnable {

        public Producer() {
        }

//        private void send() {
////            logger.info("Producer " + producerId + " sending " + messageCount + " messages");
//            for (int x = 0; x < messageCount; x++) {
//                final byte[] message = createPayload(x);
//                sendMessage(new TestMessage(message));
//            }
//            logger.info("All Messages Dispatched");
//        }

//        private byte[] createPayload(int sequenceNumber) {
//            byte message[] = new byte[messageSize];
//            if (generateTimestamp) {
//                try {
//                    ByteArrayOutputStream acc = new ByteArrayOutputStream();
//                    DataOutputStream d = new DataOutputStream(acc);
//                    long nano = System.nanoTime();
//                    d.writeInt(sequenceNumber);
//                    d.writeLong(nano);
//                    d.flush();
//                    acc.flush();
//                    byte[] m = acc.toByteArray();
//                    if (m.length <= messageSize) {
//                        System.arraycopy(m, 0, message, 0, m.length);
//                        return message;
//                    } else {
//                        return m;
//                    }
//                } catch (IOException ioe) {
//                    throw new IllegalStateException(ioe);
//                }
//            } else {
//                return message;
//            }
//        }

        public void run() {
        		Object message;
            while ((message=supplier.get())!=null) {
            		sendMessage(new TestMessage(message));
            }
        }

        private class TestMessage implements Message<Object> {
            private final Object message;

            private final TestMessageHeaders headers;

            public TestMessage(Object message) {
                this.message = message;
                this.headers = new TestMessageHeaders(null);
            }

            @Override
            public Object getPayload() {
                return message;
            }

            @Override
            public MessageHeaders getHeaders() {
                return headers;
            }

            class TestMessageHeaders extends MessageHeaders {
				private static final long serialVersionUID = 1L;

				public TestMessageHeaders(Map<String, Object> headers) {
                    super(headers, ID_VALUE_NONE, -1L);
                }
            }
        }
    }
}
