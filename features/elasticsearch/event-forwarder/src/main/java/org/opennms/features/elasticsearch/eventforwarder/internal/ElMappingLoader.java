package org.opennms.features.elasticsearch.eventforwarder.internal;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created:
 * User: unicoletti
 * Date: 7:03 PM 6/25/15
 */
public class ElMappingLoader {
    Logger logger = LoggerFactory.getLogger(ElMappingLoader.class);

    public void process(Exchange exchange) {
        Message in = exchange.getIn();
        StringBuffer body=new StringBuffer();

        BufferedReader is=new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("elmapping.json")));
        String l;
        try {
            while((l=is.readLine())!=null) {
                body.append(l);
            }
        } catch (IOException e) {
            logger.error("Cannot read elasticsearch mapping file", e);
        }

        exchange.getOut().setBody(body.toString());
    }
}
