/**
 * Copyright © 2014-2021 The SiteWhere Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sitewhere.registration.kafka;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

import com.sitewhere.grpc.event.EventModelConverter;
import com.sitewhere.grpc.model.DeviceEventModel.GDecodedEventPayload;
import com.sitewhere.microservice.kafka.ProcessorSupplierComponent;
import com.sitewhere.registration.spi.microservice.IDeviceRegistrationTenantEngine;
import com.sitewhere.rest.model.device.event.kafka.DecodedEventPayload;
import com.sitewhere.spi.SiteWhereException;

/**
 * Handles processing of events for devices not registered in the system.
 */
public class UnregisteredEventsProcessorSupplier extends ProcessorSupplierComponent<String, GDecodedEventPayload> {

    /*
     * @see org.apache.kafka.streams.processor.ProcessorSupplier#get()
     */
    @Override
    public Processor<String, GDecodedEventPayload> get() {
	return new Processor<String, GDecodedEventPayload>() {

	    @SuppressWarnings("unused")
	    private ProcessorContext context;

	    /*
	     * @see
	     * org.apache.kafka.streams.processor.Processor#init(org.apache.kafka.streams.
	     * processor.ProcessorContext)
	     */
	    @Override
	    public void init(ProcessorContext context) {
		this.context = context;
	    }

	    /*
	     * @see org.apache.kafka.streams.processor.Processor#process(java.lang.Object,
	     * java.lang.Object)
	     */
	    @Override
	    public void process(String key, GDecodedEventPayload event) {
		try {
		    // Convert payload to API object.
		    DecodedEventPayload payload = EventModelConverter.asApiDecodedEventPayload(event);

		    // Pass payload to registration manager.
		    ((IDeviceRegistrationTenantEngine) getTenantEngine()).getRegistrationManager()
			    .handleUnregisteredDeviceEvent(payload);
		}
		// TODO: Push errors to well-known topics.
		catch (SiteWhereException e) {
		    getLogger().error("Unable to process inbound event payload.", e);
		} catch (Throwable e) {
		    getLogger().error("Unhandled exception processing inbound event payload.", e);
		}
	    }

	    /*
	     * @see org.apache.kafka.streams.processor.Processor#close()
	     */
	    @Override
	    public void close() {
	    }
	};
    }
}
